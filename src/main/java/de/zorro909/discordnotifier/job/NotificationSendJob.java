package de.zorro909.discordnotifier.job;

import com.agorapulse.worker.annotation.FixedRate;
import de.zorro909.discordnotifier.config.DiscordConfig;
import de.zorro909.discordnotifier.entity.repository.DiscordNotificationRepository;
import io.micronaut.context.annotation.Bean;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.CompletableFuture;

@Bean
@AllArgsConstructor
@Slf4j
public class NotificationSendJob {

    private final DiscordNotificationRepository discordNotificationRepository;
    private final JDA                           jda;
    private final DiscordConfig                 discordConfig;

    @FixedRate("1m")
    public void sendNotification() {
        TextChannel textChannel = jda.getTextChannelById(discordConfig.getChannelId());

        if (textChannel == null) {
            log.error("Channel with id {} not found", discordConfig.getChannelId());
            return;
        }

        discordNotificationRepository
                .findBySentFalse()
                .stream()
                .map(discordNotification -> textChannel
                        .sendMessage(discordNotification.getMessage())
                        .submit()
                        .thenAccept((message) -> {
                            discordNotification.setSent(true);
                            discordNotificationRepository.update(discordNotification);
                            log.info("Sent notification '{}' with id {}", discordNotification.getMessage(),
                                     discordNotification.getId());
                        })
                        .exceptionally((throwable) -> {
                            log.error("Error sending message", throwable);
                            return null;
                        }))
                .reduce(CompletableFuture::allOf)
                .orElseGet(() -> CompletableFuture.completedFuture(null))
                .join();
    }

}
