package de.zorro909.discordnotifier.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("discord")
public class DiscordConfig {
    private String botToken;

    public DiscordConfig() {
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }


}
