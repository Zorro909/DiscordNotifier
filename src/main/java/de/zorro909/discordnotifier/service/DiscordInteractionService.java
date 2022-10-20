package de.zorro909.discordnotifier.service;

import de.zorro909.discordnotifier.api.webhook.DiscordInteractionDto;
import de.zorro909.discordnotifier.entity.component.button.DiscordButtonComponent;
import de.zorro909.discordnotifier.entity.repository.DiscordNotificationRepository;
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
import lombok.val;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor
@Singleton
@Slf4j
public class DiscordInteractionService {

    private final DiscordNotificationRepository discordNotificationRepository;

    @TransactionalAdvice
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // Fetch DiscordButtonComponent from database
        Long id = Long.valueOf(event.getComponentId().split("-")[0]);
        Optional<DiscordButtonComponent> buttonComponent = discordNotificationRepository
                .findById(id)
                .flatMap(discordNotification -> discordNotification
                        .getComponents()
                        .stream()
                        .filter(DiscordButtonComponent.class::isInstance)
                        .map(DiscordButtonComponent.class::cast)
                        .filter(component -> event.getComponentId().equals(id + "-" + component.getInteractionId()))
                        .findFirst());

        if (buttonComponent.isEmpty()) {
            log.warn("User {} clicked on an unknown button with id {}", event.getUser().getAsMention(),
                     event.getComponentId());
            errorReply(event::reply, event.getComponentId().split("-", 2)[1]);
            return;
        }

        val button = buttonComponent.get();

        if (button.getModalTextInputs() == null || button.getModalTextInputs().size() == 0) {
            // No modal text inputs, just send the request
            String webhookUrl = button.getParent().getWebhookUrl();

            if (webhookUrl == null) {
                log.error(
                        "User {} clicked on a button with id {} but the webhook url of the notification with id {} is null",
                        event.getUser().getAsMention(), event.getComponentId(), id);
                errorReply(event::reply, button.getInteractionId());
                return;
            }

            DiscordInteractionDto discordInteractionDto =
                    DiscordInteractionDto.builder().interactionId(button.getInteractionId()).build();

            sendWebhookRequest(webhookUrl, discordInteractionDto, event.getUser().getAsMention(),
                               button.getParent().getId().toString(), button.getInteractionId()).ifPresentOrElse(
                    response -> {
                        if (response.getStatus() == HttpStatus.OK) {
                            log.debug("Successful Webhook Request for button with id {}", button.getInteractionId());
                            event.deferEdit().submit();
                        } else {
                            errorReply(event::reply, button.getInteractionId());
                            log.warn("Webhook Request for button with id {} has a unexpected status {}: {}\n{}",
                                     button.getInteractionId(), response.getStatus().getCode(),
                                     response.getStatus().getReason(), response.getBody().orElse(""));
                        }
                    }, () -> {
                        log.warn("Unable to send Webhook to URL '{}' (InteractionId: {})", webhookUrl,
                                 button.getInteractionId());
                        errorReply(event::reply, button.getInteractionId());
                    });
        } else {
            // Create Modal from DiscordButtonComponent modalTextInputs
            Modal.Builder modal = Modal.create(event.getComponentId(), button.getLabel());

            button.getModalTextInputs().forEach(textInputComponent -> {
                TextInput.Builder textInput =
                        TextInput.create(textInputComponent.getInteractionId(), textInputComponent.getLabel(),
                                         textInputComponent.isMultiline() ? TextInputStyle.PARAGRAPH : TextInputStyle.SHORT);
                // Fill in textInput from textInputComponent
                textInput.setPlaceholder(textInputComponent.getPlaceholder());
                textInput.setMinLength(textInputComponent.getMinLength());
                textInput.setMaxLength(textInputComponent.getMaxLength());
                textInput.setRequired(textInputComponent.isRequired());

                modal.addActionRow(textInput.build());
            });

            event.replyModal(modal.build()).submit();
        }
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
        log.debug("Sending Webhook Request to '{}' with '{}'", request.getUri(), interactionDto);
        return Optional.of(client.exchange(request));
    }
}
