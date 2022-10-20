package de.zorro909.discordnotifier.job;

import de.zorro909.discordnotifier.config.User;
import de.zorro909.discordnotifier.config.UserConfig;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import de.zorro909.discordnotifier.entity.repository.DiscordNotificationRepository;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Singleton
@AllArgsConstructor
@Slf4j
public class NotificationDeletionJob {

    private final DiscordNotificationRepository discordNotificationRepository;
    private final JDA                           jda;
    private final UserConfig                    userConfig;

    public void deleteNotifications() {
        discordNotificationRepository.findByMarkedForDeletionTrue().forEach(notification -> {
            if (notification.isSent() && notification.getMessageId() != null) {

                val channel = fetchChannel(notification);

                if (channel == null) {
                    return;
                }

                channel.deleteMessageById(notification.getMessageId()).submit().thenAccept(aVoid -> {
                    discordNotificationRepository.delete(notification);
                    log.info("Deleted notification with id {}", notification.getId());
                }).exceptionally(throwable -> {
                    log.error("Could not delete notification with id {}", notification.getId(), throwable);
                    return null;
                });
            } else {
                discordNotificationRepository.delete(notification);
                log.info("Deleted unsent notification with id {}", notification.getId());
            }
        });
    }

    @Nullable
    private TextChannel fetchChannel(DiscordNotification notification) {
        Optional<String> channelId = userConfig
                .getUser(notification.getSource())
                .map(User::getChannelIds)
                .map(channels -> channels.get(notification.getChannelName()));
        Optional<TextChannel> textChannel = channelId.map(jda::getTextChannelById);

        if (channelId.isEmpty()) {
            log.error("No Channel Id is configured for the channel name {} of user {}", notification.getChannelName(),
                      notification.getSource());
            return null;
        }
        if (textChannel.isEmpty()) {
            log.error("Channel with id {} not found", channelId.get());
            return null;
        }

        return textChannel.get();
    }
}
