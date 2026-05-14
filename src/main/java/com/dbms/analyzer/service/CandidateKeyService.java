package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.CandidateKeyFinder;
import com.dbms.analyzer.model.CandidateKey;
import java.util.Set;

@Service
public class CandidateKeyService {

    private final RelationService relationService;
    private final FdService fdService;
    private final CandidateKeyFinder candidateKeyFinder;

    public CandidateKeyService(
            RelationService relationService,
            FdService fdService,
            CandidateKeyFinder candidateKeyFinder) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.candidateKeyFinder = candidateKeyFinder;
    }

    /**
     * Finds all candidate keys for the current relation
     */
    public Set<CandidateKey> findAllCandidateKeys() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        return candidateKeyFinder.findAllCandidateKeys(
            relationService.getAttributes(),
            fdService.getAllDependencies());
    }

    /**
     * Identifies prime attributes from candidate keys
     */
    public Set<String> getPrimeAttributes() {
        Set<CandidateKey> keys = findAllCandidateKeys();
        Set<String> primeAttributes = new java.util.HashSet<>();

        for (CandidateKey key : keys) {
            primeAttributes.addAll(key.getAttributes());
        }

        return primeAttributes;
    }

    /**
     * Gets non-prime attributes
     */
    public Set<String> getNonPrimeAttributes() {
        Set<String> all = relationService.getAttributes();
        Set<String> primeAttrs = getPrimeAttributes();
        Set<String> nonPrimeAttrs = new java.util.HashSet<>(all);
        nonPrimeAttrs.removeAll(primeAttrs);
        return nonPrimeAttrs;
    }
}
