package org.ivan.volunteer.claimmap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class ClaimExternalFetcher implements ClaimFetcher {
    private final String spreadsheetUrl;

    private final RestOperations restClient;

    public ClaimExternalFetcher(@Value("${spreadsheet.url}") String spreadsheetUrl) {
        this.spreadsheetUrl = spreadsheetUrl;
        // t0d0 define singleton RestTemplate?
        // t0d0 feign client here?
        restClient = new RestTemplate();
    }

    @Override
    public byte[] fetchSpreadSheet() {
        return restClient.getForObject(spreadsheetUrl, byte[].class);
    }
}
