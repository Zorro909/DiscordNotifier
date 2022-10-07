package de.zorro909.discordnotifier;

import de.zorro909.discordnotifier.api.DiscordNotificationDto;
import de.zorro909.discordnotifier.api.SubmitNotificationDto;
import de.zorro909.discordnotifier.config.mapper.DiscordNotificationMapper;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import de.zorro909.discordnotifier.service.NotificationService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;

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
public class DiscordNotifierController {

    private final NotificationService notificationService;

    @Post(uri = "/notification", consumes = "application/json", produces = "application/json")
    public DiscordNotificationDto submitNotification(
            @RequestBody SubmitNotificationDto submitNotificationDto,
            Principal principal) {
        DiscordNotification discordNotification =
                notificationService.submitNotification(principal.getName(), submitNotificationDto.message());
        return DiscordNotificationMapper.INSTANCE.mapToDto(discordNotification);
    }

    // Fetch DiscordNotificationDto Status by ID
    @Get(uri = "/notification/{id}", produces = "application/json")
    public HttpResponse<DiscordNotificationDto> getNotification(Long id) {
        Optional<DiscordNotification> discordNotification = notificationService.notificationStatus(id);
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
        return password + ":" +
                Base64.getEncoder().encodeToString(encoded) + ":" +
                Base64.getEncoder().encodeToString(salt);
    }
}