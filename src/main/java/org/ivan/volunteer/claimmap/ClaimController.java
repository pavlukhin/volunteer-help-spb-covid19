package org.ivan.volunteer.claimmap;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClaimController {
    private final ClaimStorage claimStorage;

    public ClaimController(@Autowired ClaimStorage storage) {
        claimStorage = storage;
    }

    @GetMapping("/api/claims")
    public List<Claim> claims() {
        return claimStorage.getClaims();
    }
}
