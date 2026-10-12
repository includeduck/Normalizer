package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.CandidateKeyFinder;
import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;
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

    // Key search is exponential and is requested many times per UI action,
    // so the last result is kept until the attributes or FDs change.
    private Set<String> cachedAttributes;
    private Set<FunctionalDependency> cachedDependencies;
    private Set<CandidateKey> cachedKeys;

    /**
     * Finds all candidate keys for the current relation
     */
    public synchronized Set<CandidateKey> findAllCandidateKeys() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Set<String> attributes = relationService.getAttributes();
        Set<FunctionalDependency> dependencies = fdService.getAllDependencies();
        if (!attributes.equals(cachedAttributes)
                || !dependencies.equals(cachedDependencies)) {
            cachedKeys = candidateKeyFinder.findAllCandidateKeys(
                attributes, dependencies);
            cachedAttributes = attributes;
            cachedDependencies = dependencies;
        }

        return new java.util.HashSet<>(cachedKeys);
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
