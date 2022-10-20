package de.zorro909.discordnotifier.service;

import de.zorro909.discordnotifier.api.EditNotificationDto;
import de.zorro909.discordnotifier.api.SubmitNotificationDto;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface NotificationService {

    DiscordNotification submitNotification(
            @NotNull String source, @NotNull SubmitNotificationDto submitNotificationDto);

    Optional<DiscordNotification> notificationStatus(@NotNull String source, @NotNull Long id);

    Optional<DiscordNotification> updateNotification(
            @NotNull String source, @NotNull Long id, @NotNull EditNotificationDto editNotificationDto);

    Optional<DiscordNotification> deleteNotification(@NotNull String source, @NotNull Long id);
}
