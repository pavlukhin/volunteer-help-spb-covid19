package org.ivan.volunteer.claimmap;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "telegramMessageSender", url = "https://api.telegram.org")
public interface TelegramMessageSender {
    @GetMapping(path = "${telegram.msg.urlpath}")
    void sendMessage();
}