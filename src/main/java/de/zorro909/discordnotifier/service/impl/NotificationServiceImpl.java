package de.zorro909.discordnotifier.service.impl;

import de.zorro909.discordnotifier.api.EditNotificationDto;
import de.zorro909.discordnotifier.api.SubmitNotificationDto;
import de.zorro909.discordnotifier.config.mapper.NotificationSubmissionMapper;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import de.zorro909.discordnotifier.entity.component.DiscordInteractionComponent;
import de.zorro909.discordnotifier.entity.repository.DiscordNotificationRepository;
import de.zorro909.discordnotifier.service.NotificationService;
import org.jetbrains.annotations.NotNull;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Singleton
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final DiscordNotificationRepository discordNotificationRepository;

    // Send Discord Messages into the channel channelId
    public DiscordNotification submitNotification(
            @NotNull String source, @NotNull SubmitNotificationDto submitNotificationDto) {
        var discordNotification = NotificationSubmissionMapper.INSTANCE.mapToEntity(submitNotificationDto, source);
        discordNotification = discordNotificationRepository.save(discordNotification);

        log.info("User {} submitted a new notification with id {}", source, discordNotification.getId());
        log.debug("Notification with id {} was submitted to the database as {}", discordNotification.getId(),
                  discordNotification);

        return discordNotification;
    }

    public Optional<DiscordNotification> notificationStatus(
            @NotNull String source, @NotNull Long id) {
        Optional<DiscordNotification> notification = discordNotificationRepository.findById(id);
        if (notification.isPresent() && notification.get().getSource().equals(source)) {
            log.debug("User {} requested the status of notification with id {}", source, id);
            return notification;
        }
        log.info("User {} requested the status of an unknown notification with id {}", source, id);
        return Optional.empty();
    }

    @Override
    public Optional<DiscordNotification> updateNotification(
            @NotNull String source, @NotNull Long id, @NotNull EditNotificationDto editNotificationDto) {
        Optional<DiscordNotification> notification = notificationStatus(source, id);

        if (notification.isEmpty()) {
            return Optional.empty();
        }
        DiscordNotification discordNotification = notification.get();

        // Update DiscordNotification with data from EditNotificationDto if not null. Use fluent accessors.
        if (editNotificationDto.getMessage() != null) {
            discordNotification.setMessage(editNotificationDto.getMessage());
        }
        if (editNotificationDto.getTitle() != null) {
            discordNotification.setTitle(editNotificationDto.getTitle());
        }
        if (editNotificationDto.getWebhookUrl() != null) {
            discordNotification.setWebhookUrl(editNotificationDto.getWebhookUrl());
        }
        if (editNotificationDto.getImageUrl() != null) {
            discordNotification.setImageUrl(editNotificationDto.getImageUrl());
        }
        if (editNotificationDto.getComponents() != null) {
            List<DiscordInteractionComponent> components =
                    NotificationSubmissionMapper.INSTANCE.mapComponents(editNotificationDto.getComponents());
            discordNotification.setComponents(components);
        }

        log.info("Updating notification with id {}", id);
        log.debug("User {} updated notification with id {} with following changes {}", source, id, editNotificationDto);

        return Optional.of(discordNotificationRepository.update(discordNotification));
    }

    @Override
    public Optional<DiscordNotification> deleteNotification(@NotNull String source, @NotNull Long id) {
        return notificationStatus(source, id)
                .map(DiscordNotification::markForDeletion)
                .map(discordNotificationRepository::update);
    }

}
