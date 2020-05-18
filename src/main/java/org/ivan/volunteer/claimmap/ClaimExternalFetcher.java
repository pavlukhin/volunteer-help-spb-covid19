package org.ivan.volunteer.claimmap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class ClaimExternalFetcher implements ClaimFetcher {
    @Value("${spreadsheet.url}")
    private String spreadsheetUrl;

    private final RestOperations restClient;

    public ClaimExternalFetcher() {
        // t0d0 define singleton RestTemplate?
        restClient = new RestTemplate();
    }

    @Override public byte[] fetchSpreadSheet() {
        return restClient.getForObject(spreadsheetUrl, byte[].class);
    }
}
