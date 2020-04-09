package org.ivan.volunteer.claimmap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.ivan.volunteer.claimmap.geocoder.GeoCoder;
import org.ivan.volunteer.claimmap.geocoder.GeoObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class ClaimExternalFetcher {
    public static final String SPREADSHEET_URL = "https://docs.google.com/spreadsheets/d/1vTBEpyL_pp7MtnmCe7_Z-BG8WfDBg5nTapRQBjHp1z0/export?format=csv&id=1vTBEpyL_pp7MtnmCe7_Z-BG8WfDBg5nTapRQBjHp1z0&gid=0";

    private final RestOperations restClient;
    private final GeoCoder geoCoder;

    public ClaimExternalFetcher(@Autowired GeoCoder coder) {
        geoCoder = coder;
        // t0d0 define singleton RestTemplate?
        restClient = new RestTemplate();
    }

    public List<Claim> fetchClaims() {
        byte[] spreadSheetBytes = restClient.getForObject(SPREADSHEET_URL, byte[].class);
        try (InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(spreadSheetBytes))) {
            CSVParser parser = CSVFormat.DEFAULT.parse(in);
            return filterClaims(parser);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Claim> filterClaims(CSVParser parser) {
        ArrayList<Claim> claims = new ArrayList<>();
        int cnt = 0;
        for (CSVRecord csvRec : parser) {
//            if (++cnt > 5) break;
            String statusStr = csvRec.get(1);
            Claim.Status status = parseStatus(statusStr);
            if (status != Claim.Status.OPEN) {
                continue;
            }
            String rawId = csvRec.get(0);
            String address = csvRec.get(3);
            String details = csvRec.get(4);
            try {
                validateId(rawId)
                    .ifPresent(validId -> {
                        GeoObject geoObj = geoCoder.resolve(address);
                        String[] coord = new String[] {geoObj.getLatitude(), geoObj.getLongitude()};
                        claims.add(new Claim(validId, address, details, status, coord));
                    });
            }
            catch (Exception e) {
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

    private static Pattern claimIdPattern = Pattern.compile("\\d\\d\\d\\d\\d+");

    private static Optional<String> validateId(String id) {
        Matcher matcher = claimIdPattern.matcher(id);
        if (matcher.find()) {
            String validId = matcher.group();
            if (matcher.find()) {
                return Optional.empty();
            }
            else {
                return Optional.of(validId);
            }
        }
        else {
            return Optional.empty();
        }
    }
}
