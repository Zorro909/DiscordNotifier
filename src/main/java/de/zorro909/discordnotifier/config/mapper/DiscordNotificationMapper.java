package de.zorro909.discordnotifier.config.mapper;

import de.zorro909.discordnotifier.api.DiscordNotificationDto;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DiscordNotificationMapper {

    DiscordNotificationMapper INSTANCE = Mappers.getMapper(DiscordNotificationMapper.class);

    DiscordNotificationDto mapToDto(DiscordNotification discordNotification);
}
