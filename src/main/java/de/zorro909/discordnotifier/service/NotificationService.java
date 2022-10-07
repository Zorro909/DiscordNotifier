package de.zorro909.discordnotifier.service;

import de.zorro909.discordnotifier.entity.DiscordNotification;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

public interface NotificationService {

    DiscordNotification submitNotification(@NonNull String source, @NonNull String message);

    Optional<DiscordNotification> notificationStatus(@NonNull Long id);

}
