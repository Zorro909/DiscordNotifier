package de.zorro909.discordnotifier.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("discord")
public class DiscordConfig {
    private String botToken;
    private String channelId;

    public DiscordConfig(){}

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }



}
