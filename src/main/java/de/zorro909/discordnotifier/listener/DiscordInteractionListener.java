package de.zorro909.discordnotifier.listener;


import de.zorro909.discordnotifier.api.webhook.DiscordInteractionDto;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import de.zorro909.discordnotifier.entity.repository.DiscordNotificationRepository;
import de.zorro909.discordnotifier.service.DiscordInteractionService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

@Singleton
@AllArgsConstructor
@Slf4j
public class DiscordInteractionListener extends ListenerAdapter {

    private final DiscordNotificationRepository discordNotificationRepository;

    private final DiscordInteractionService discordInteractionService;

    @Override
    @TransactionalAdvice
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        discordInteractionService.onButtonInteraction(event);
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        // Generate DiscordInteractionDto from selected options and send it to the webhook
        DiscordInteractionDto.DiscordInteractionDtoBuilder builder = DiscordInteractionDto.builder();
        builder.interactionId(event.getComponentId().split("-", 2)[1]);

        event.getValues().forEach(value -> builder.value(value, "true"));

        DiscordInteractionDto discordInteractionDto = builder.build();

        String webhookUrl = discordNotificationRepository
                .findById(Long.valueOf(event.getComponentId().split("-", 2)[0]))
                .map(DiscordNotification::getWebhookUrl)
                .orElse(null);

        String selectMenuId = event.getComponentId().split("-", 2)[1];

        if (webhookUrl == null) {
            log.error(
                    "User {} clicked on a select menu with id {} but the webhook url of the notification with id {} is null",
                    event.getUser().getAsMention(), selectMenuId, event.getComponentId().split("-", 2)[0]);
            errorReply(event::reply, selectMenuId);
            return;
        }

        sendWebhookRequest(webhookUrl, discordInteractionDto, event.getUser().getAsMention(),
                           event.getComponentId().split("-", 2)[0], selectMenuId).ifPresentOrElse(response -> {
            if (response.getStatus() == HttpStatus.OK) {
                log.debug("Successful Webhook Request for select menu with id {}", selectMenuId);
                event.deferEdit().submit();
            } else {
                log.warn("Webhook Request for select menu with id {} has a unexpected status {}: {}\n{}", selectMenuId,
                         response.getStatus().getCode(), response.getStatus().getReason(),
                         response.getBody().orElse(""));
                errorReply(event::reply, selectMenuId);
            }
        }, () -> {
            log.warn("Unable to send Webhook to URL '{}' (InteractionId: {})", webhookUrl, selectMenuId);
            errorReply(event::reply, selectMenuId);
        });
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        // Generate DiscordInteractionDto from text inputs of the modal and send it to the webhook
        DiscordInteractionDto.DiscordInteractionDtoBuilder builder = DiscordInteractionDto.builder();
        builder.interactionId(event.getModalId().split("-", 2)[1]);

        event.getValues().forEach((mapping) -> builder.value(mapping.getId(), mapping.getAsString()));

        DiscordInteractionDto discordInteractionDto = builder.build();

        String webhookUrl = discordNotificationRepository
                .findById(Long.valueOf(event.getModalId().split("-", 2)[0]))
                .map(DiscordNotification::getWebhookUrl)
                .orElse(null);

        String modalId = event.getModalId().split("-", 2)[1];

        if (webhookUrl == null) {
            log.error(
                    "User {} clicked on a modal with id {} but the webhook url of the notification with id {} is null",
                    event.getUser().getAsMention(), modalId, event.getModalId().split("-", 2)[0]);
            errorReply(event::reply, modalId);
        }

        sendWebhookRequest(webhookUrl, discordInteractionDto, event.getUser().getAsMention(),
                           event.getModalId().split("-", 2)[0], modalId).ifPresentOrElse(response -> {
            if (response.getStatus() == HttpStatus.OK) {
                log.debug("Successful Webhook Request for modal with id {}", modalId);
                event.deferEdit().submit();
            } else {
                log.warn("Webhook Request for modal with id {} has a unexpected status {}: {}\n{}", modalId,
                         response.getStatus().getCode(), response.getStatus().getReason(),
                         response.getBody().orElse(""));
                errorReply(event::reply, modalId);
            }
        }, () -> {
            log.warn("Unable to send Webhook to URL '{}' (InteractionId: {})", webhookUrl, modalId);
            errorReply(event::reply, modalId);
        });
    }

    private void errorReply(Function<String, ReplyCallbackAction> replyCallbackFunction, String interactionId) {
        replyCallbackFunction
                .apply("An error occurred while processing your request regarding Interaction with the ID '" + interactionId + ". Please contact the server administrator.")
                .setEphemeral(true)
                .queue();
    }

    private Optional<HttpResponse<String>> sendWebhookRequest(
            String webhookUrl, DiscordInteractionDto interactionDto, String userMention, String componentId,
            String notificationId) {
        BlockingHttpClient client = HttpClient.create(null).toBlocking();
        MutableHttpRequest<DiscordInteractionDto> request = HttpRequest.POST(webhookUrl, interactionDto);
        log.info("Sending Webhook Request to '{}' with '{}'", request.getUri(), interactionDto);
        return Optional.of(client.exchange(request));
    }

}

