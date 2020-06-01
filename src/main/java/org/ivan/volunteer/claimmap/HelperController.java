package org.ivan.volunteer.claimmap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelperController {
    private final Notifier notifier;

    public HelperController(@Autowired Notifier notifier) {
        this.notifier = notifier;
    }

    @PostMapping(path = "/api/sendNotification")
    public void sendNotification() {
        notifier.sendNotification();
    }
}
