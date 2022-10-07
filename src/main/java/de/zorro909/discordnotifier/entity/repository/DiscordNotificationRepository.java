package de.zorro909.discordnotifier.entity.repository;

import de.zorro909.discordnotifier.entity.DiscordNotification;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface DiscordNotificationRepository extends JpaRepository<DiscordNotification, Long> {

    List<DiscordNotification> findBySentFalse();
}
