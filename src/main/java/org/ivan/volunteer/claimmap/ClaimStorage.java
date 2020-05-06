package org.ivan.volunteer.claimmap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.ivan.volunteer.claimmap.geocoder.GeoCoder;
import org.ivan.volunteer.claimmap.geocoder.GeoObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class ClaimStorage {
    private final ClaimFetcher claimFetcher;
    private final GeoCoder geoCoder;
    private final Notifier notifier;

    private volatile List<Claim> claims = Collections.emptyList();

    public ClaimStorage(
        @Autowired ClaimFetcher claimFetcher,
        @Autowired GeoCoder geoCoder,
        @Autowired Notifier notifier) {
        this.claimFetcher = claimFetcher;
        this.geoCoder = geoCoder;
        this.notifier = notifier;

        claims = loadClaims();
    }

    public List<Claim> getClaims() {
        return claims;
    }

    @Scheduled(fixedRate = 60_000)
    private void reloadClaims() {
        List<Claim> oldClaims = claims;
        List<Claim> actualClaims;
        claims = actualClaims = loadClaims();

        sendNotificationIfNeeded(oldClaims, actualClaims);
    }

    private void sendNotificationIfNeeded(List<Claim> oldClaims, List<Claim> actualClaims) {
        actualClaims.stream()
            .filter(ac -> isOpenClaimNotSeenBefore(ac, oldClaims))
            .findAny().ifPresent(c -> notifier.sendNotification());
    }

    private boolean isOpenClaimNotSeenBefore(Claim ac, List<Claim> oldClaims) {
        return ac.getStatus() == Claim.Status.OPEN
            && oldClaims.stream().noneMatch(oc -> oc.getId().equals(ac.getId()));
    }

    private List<Claim> loadClaims() {
        // t0d0 configure logging
        return deserialize(claimFetcher.fetchSpreadSheet());
    }

    private List<Claim> deserialize(byte[] spreadSheetBytes) {
        try (InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(spreadSheetBytes), UTF_8)) {
            CSVParser parser = CSVFormat.DEFAULT.parse(in);
            return extractClaims(parser);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Claim> extractClaims(CSVParser parser) {
        ArrayList<Claim> claims = new ArrayList<>();
        int cnt = 0;
        for (CSVRecord csvRec : parser) {
//            if (++cnt > 5) break;
            String statusStr = csvRec.get(1);
            Claim.Status status = parseStatus(statusStr);
            if (status == null) {
                continue;
            }
            String id = csvRec.get(0);
            String assignee = csvRec.get(2);
            String address = csvRec.get(3);
            String details = csvRec.get(4);
            try {
                if (validateId(id)) {
                    GeoObject geoObj = geoCoder.resolve("Санкт-Петербург, " + address);
                    String[] coord = new String[] {geoObj.getLatitude(), geoObj.getLongitude()};
                    claims.add(new Claim(id, address, details, status, coord, assignee));
                }
            }
            catch (Exception e) {
                // t0d0 handling or logging
                e.printStackTrace();
            }
        }
        return claims;
    }

    private static Pattern inProgressClaimPattern = Pattern.compile("в +обработке", Pattern.CASE_INSENSITIVE);

    private static Claim.Status parseStatus(String statusStr) {
        if (statusStr.toLowerCase().contains("свободна")) {
            return Claim.Status.OPEN;
        }
        Matcher matcher = inProgressClaimPattern.matcher(statusStr);
        if (matcher.find()) {
            return Claim.Status.IN_PROGRESS;
        }
        return null;
    }

    private static Pattern claimIdPattern = Pattern.compile("\\d+");

    private static boolean validateId(String id) {
        Matcher matcher = claimIdPattern.matcher(id);
        return matcher.find();
    }
}
