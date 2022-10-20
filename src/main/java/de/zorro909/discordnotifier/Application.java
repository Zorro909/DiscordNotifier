package de.zorro909.discordnotifier;

import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.jetbrains.annotations.NotNull;

@OpenAPIDefinition(
    info = @Info(
            title = "DiscordNotifier",
            version = "0.0"
    )
)
public class Application {
    @ContextConfigurer
    public static class DefaultEnvironmentConfigurer implements ApplicationContextConfigurer {
        @Override
        public void configure(@NotNull ApplicationContextBuilder builder) {
            builder.defaultEnvironments("dev");
        }
    }
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

}
