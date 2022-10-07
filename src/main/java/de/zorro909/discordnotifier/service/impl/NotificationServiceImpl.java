package de.zorro909.discordnotifier.service.impl;

import de.zorro909.discordnotifier.entity.DiscordNotification;
import de.zorro909.discordnotifier.entity.repository.DiscordNotificationRepository;
import de.zorro909.discordnotifier.service.NotificationService;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

@Singleton
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final DiscordNotificationRepository discordNotificationRepository;

    // Send Discord Messages into the channel channelId
    public DiscordNotification submitNotification(@NonNull String source,@NonNull String message) {
        var discordNotification = DiscordNotification
                .builder()
                .source(source)
                .message(message)
                .build();
        return discordNotificationRepository.save(discordNotification);
    }

    public Optional<DiscordNotification> notificationStatus(@NonNull Long id) {
        return discordNotificationRepository.findById(id);
    }

}
