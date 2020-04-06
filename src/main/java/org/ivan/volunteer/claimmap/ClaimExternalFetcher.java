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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class ClaimExternalFetcher {
    public static final String SPREADSHEET_URL = "https://docs.google.com/spreadsheets/d/1vTBEpyL_pp7MtnmCe7_Z-BG8WfDBg5nTapRQBjHp1z0/export?format=csv&id=1vTBEpyL_pp7MtnmCe7_Z-BG8WfDBg5nTapRQBjHp1z0&gid=0";
    private final RestOperations restClient;

    public ClaimExternalFetcher() {
        // t0d0 define singleton RestTemplate?
        restClient = new RestTemplate();
    }

    public List<Claim> fetchClaims() {
        ResponseEntity<byte[]> entity = restClient.getForEntity(SPREADSHEET_URL, byte[].class);
        try (InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(entity.getBody()))) {
            CSVParser parser = CSVFormat.DEFAULT.parse(in);
            return filterClaims(parser);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Claim> filterClaims(CSVParser parser) {
        ArrayList<Claim> claims = new ArrayList<>();
        for (CSVRecord csvRec : parser) {
            String status = csvRec.get(1);
            if (!status.toLowerCase().contains("свободна")) {
                continue;
            }
            String rawId = csvRec.get(0);
            String address = csvRec.get(3);
            String details = csvRec.get(4);
            validateId(rawId)
                .ifPresent(validId -> claims.add(new Claim(validId, address, details)));
        }
        return claims;
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
