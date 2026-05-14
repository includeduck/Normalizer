package com.dbms.analyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.CandidateKeyFinder;
import com.dbms.analyzer.model.CandidateKey;
import java.util.Set;

@Service
public class CandidateKeyService {

    @Autowired
    private RelationService relationService;

    @Autowired
    private FdService fdService;

    private CandidateKeyFinder candidateKeyFinder;

    public CandidateKeyService() {
        this.candidateKeyFinder = new CandidateKeyFinder();
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
