package org.ivan.volunteer.claimmap;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class ClaimExternalFetcher implements ClaimFetcher {
    private static final String SPREADSHEET_URL = "https://docs.google.com/spreadsheets/d/1vTBEpyL_pp7MtnmCe7_Z-BG8WfDBg5nTapRQBjHp1z0/export?format=csv&id=1vTBEpyL_pp7MtnmCe7_Z-BG8WfDBg5nTapRQBjHp1z0&gid=0";

    private final RestOperations restClient;

    public ClaimExternalFetcher() {
        // t0d0 define singleton RestTemplate?
        restClient = new RestTemplate();
    }

    @Override public byte[] fetchSpreadSheet() {
        return restClient.getForObject(SPREADSHEET_URL, byte[].class);
    }
}
