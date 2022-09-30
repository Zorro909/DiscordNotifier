package de.zorro909;

import com.agorapulse.worker.annotation.Cron;
import jakarta.inject.Singleton;

@Singleton
public class EmailDigestSimpleJob {

    private final EmailDigestService emailDigestService;

    public EmailDigestSimpleJob(EmailDigestService emailDigestService) {
        this.emailDigestService = emailDigestService;
    }

    @Cron("45 6 * * *")
    public void sendDigestEmail() {
        emailDigestService.sendEmail("user@example.com");
    }

}