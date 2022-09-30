package de.zorro909;

import io.micronaut.http.annotation.*;

@Controller("/discordNotifier")
public class DiscordNotifierController {

    @Get(uri="/", produces="text/plain")
    public String index() {
        return "Example Response";
    }
}