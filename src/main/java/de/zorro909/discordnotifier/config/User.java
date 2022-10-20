package de.zorro909.discordnotifier.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@EachProperty("user")
@Getter
public class User {

    private final String              name;
    @Setter
    private       String              password;
    @Setter
    private       String              salt;
    @Setter
    private       Map<String, String> channelIds;

    public User(@Parameter String name) {
        this.name = name;
    }

}
