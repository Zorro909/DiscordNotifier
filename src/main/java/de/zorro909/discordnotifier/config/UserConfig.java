package de.zorro909.discordnotifier.config;

import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

public class UserConfig {

    @Inject
    private List<User> users;

    public Optional<User> getUser(String name){
        return users.stream().filter(user -> user.getName().equals(name)).findFirst();
    }

}
