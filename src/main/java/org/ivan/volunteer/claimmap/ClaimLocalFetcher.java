package org.ivan.volunteer.claimmap;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Primary
@Profile("testing")
public class ClaimLocalFetcher implements ClaimFetcher {
    @Override
    public byte[] fetchSpreadSheet() {
        return ("" +
            "1,Свободна,,\"Бадаева 14к1\",Мой дом\n" +
            "2,в обработке,Иван Павлухин,\"Джона Рида 2к2\",Пенсионный фонд\n" +
            "3/4,Свободна,,\"Бадаева 2\",KFC\n").getBytes(UTF_8);
    }
}
