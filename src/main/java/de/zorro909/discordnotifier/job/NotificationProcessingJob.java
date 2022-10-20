package de.zorro909.discordnotifier.job;

import com.agorapulse.worker.annotation.FixedRate;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@AllArgsConstructor
@Slf4j
public class NotificationProcessingJob {

    private final NotificationSendJob notificationSendJob;
    private final NotificationDeletionJob notificationDeletionJob;

    @TransactionalAdvice
    @FixedRate("20s")
    public void processNotifications() {
        log.debug("Start processing Notifications");

        notificationSendJob.sendNotifications();
        notificationDeletionJob.deleteNotifications();

        log.debug("Finished processing Notifications");
    }




}
