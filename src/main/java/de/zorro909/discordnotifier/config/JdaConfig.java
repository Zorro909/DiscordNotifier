package de.zorro909.discordnotifier.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@Factory
public class JdaConfig {

     @Bean
     public JDA jda(DiscordConfig discordConfig) throws InterruptedException {
         return JDABuilder.createDefault(discordConfig.getBotToken()).build().awaitReady();
     }

}
