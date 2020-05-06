package org.ivan.volunteer.claimmap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Notifier {
    private final TelegramMessageSender telegramMessageSender;

    public Notifier(@Autowired TelegramMessageSender sender) {
        telegramMessageSender = sender;
    }

    public void sendNotification() {
        telegramMessageSender.sendMessage();
    }
}
