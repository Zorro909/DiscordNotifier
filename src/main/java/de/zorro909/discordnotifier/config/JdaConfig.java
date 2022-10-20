package de.zorro909.discordnotifier.config;

import de.zorro909.discordnotifier.listener.DiscordInteractionListener;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@Factory
public class JdaConfig {

    @Singleton
    public JDA jda(DiscordConfig discordConfig, DiscordInteractionListener interactionListener) throws InterruptedException {
        return JDABuilder
                .createDefault(discordConfig.getBotToken())
                .addEventListeners(interactionListener)
                .build()
                .awaitReady();
    }

}
