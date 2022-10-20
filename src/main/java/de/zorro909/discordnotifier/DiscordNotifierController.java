package de.zorro909.discordnotifier;

import de.zorro909.discordnotifier.api.DiscordNotificationDto;
import de.zorro909.discordnotifier.api.EditNotificationDto;
import de.zorro909.discordnotifier.api.SubmitNotificationDto;
import de.zorro909.discordnotifier.config.mapper.DiscordNotificationMapper;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import de.zorro909.discordnotifier.service.NotificationService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Optional;

@Controller("/discord/notifier/api")
@Secured(SecurityRule.IS_ANONYMOUS)
@AllArgsConstructor
@Slf4j
public class DiscordNotifierController {

    private final NotificationService notificationService;

    @Error
    public HttpResponse<?> handleBadRequest(HttpRequest<?> request, Throwable e) {
        log.error("Bad Request", e);
        log.error(request.getBody().get().toString());
        return HttpResponse.badRequest();
    }

    @Post(uri = "/notification", consumes = "application/json", produces = "application/json")
    public DiscordNotificationDto submitNotification(
            @RequestBody @Body SubmitNotificationDto submitNotificationDto, Principal principal) {
        DiscordNotification discordNotification =
                notificationService.submitNotification(principal.getName(), submitNotificationDto);
        return DiscordNotificationMapper.INSTANCE.mapToDto(discordNotification);
    }

    @Put(uri = "/notification/{id}", consumes = "application/json", produces = "application/json")
    public HttpResponse<DiscordNotificationDto> updateNotification(
            Long id, @RequestBody @Body EditNotificationDto editNotificationDto, Principal principal) {
        Optional<DiscordNotification> discordNotification =
                notificationService.updateNotification(principal.getName(), id, editNotificationDto);
        return discordNotification
                .map(notification -> HttpResponse.ok(DiscordNotificationMapper.INSTANCE.mapToDto(notification)))
                .orElse(HttpResponse.notFound());
    }

    @Delete(uri = "/notification/{id}", consumes = "application/json", produces = "application/json")
    public HttpResponse<DiscordNotificationDto> deleteNotification(
            Long id, Principal principal) {
        Optional<DiscordNotification> discordNotification =
                notificationService.deleteNotification(principal.getName(), id);
        return discordNotification
                .map(notification -> HttpResponse.ok(DiscordNotificationMapper.INSTANCE.mapToDto(notification)))
                .orElse(HttpResponse.notFound());
    }

    // Fetch DiscordNotificationDto Status by ID
    @Get(uri = "/notification/{id}", produces = "application/json")
    public HttpResponse<DiscordNotificationDto> getNotification(Long id, Principal principal) {
        Optional<DiscordNotification> discordNotification =
                notificationService.notificationStatus(principal.getName(), id);
        return discordNotification
                .map(DiscordNotificationMapper.INSTANCE::mapToDto)
                .map(HttpResponse::ok)
                .orElseGet(HttpResponse::notFound);
    }

    @Post(uri = "/generate-password", consumes = "text/plain", produces = "text/plain")
    public String generatePassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        byte[] passwordBytes = new byte[64];
        random.nextBytes(passwordBytes);
        String password = Base64.getEncoder().encodeToString(passwordBytes);

        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec ks = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKey s = f.generateSecret(ks);

        byte[] encoded = s.getEncoded();
        return password + ":" + Base64.getEncoder().encodeToString(encoded) + ":" + Base64
                .getEncoder()
                .encodeToString(salt);
    }
}