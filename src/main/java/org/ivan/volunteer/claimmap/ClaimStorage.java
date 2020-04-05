package org.ivan.volunteer.claimmap;

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
import org.springframework.stereotype.Component;

@Component
public class ClaimStorage {
    private final List<Claim> claims;

    public ClaimStorage() {
        claims = loadClaims();
    }

    public List<Claim> getClaims() {
        return claims;
    }

    private static List<Claim> loadClaims() {
        try (InputStreamReader in = new InputStreamReader(ClaimStorage.class.getResourceAsStream("/claims.csv"))) {
            CSVParser parser = CSVFormat.DEFAULT.parse(in);
            ArrayList<Claim> claims = new ArrayList<>();
            for (CSVRecord csvRec : parser) {
                String status = csvRec.get(1);
                if (!status.toLowerCase().contains("свободна")) {
                    continue;
                }
                String rawId = csvRec.get(0);
                String address = csvRec.get(3);
                validateId(rawId)
                    .ifPresent(validId -> claims.add(new Claim(validId, address)));
            }
            return claims;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
