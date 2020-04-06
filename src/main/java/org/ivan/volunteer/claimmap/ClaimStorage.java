package org.ivan.volunteer.claimmap;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClaimStorage {
    private final ClaimExternalFetcher claimFetcher;
    private volatile List<Claim> claims = Collections.emptyList();

    public ClaimStorage(@Autowired ClaimExternalFetcher fetcher) {
        claimFetcher = fetcher;
        reloadClaims();
    }

    @Scheduled(fixedRate = 60_000)
    private void reloadClaims() {
        // t0d0 configure logging
        claims = claimFetcher.fetchClaims();
    }

    public List<Claim> getClaims() {
        return claims;
    }
}
