package de.zorro909.discordnotifier.entity.repository;

import de.zorro909.discordnotifier.entity.DiscordNotification;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface DiscordNotificationRepository extends CrudRepository<DiscordNotification, Long> {

    List<DiscordNotification> findBySentFalse();

    List<DiscordNotification> findByMarkedForDeletionTrue();
}
