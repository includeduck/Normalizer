package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.BcnfDecomposer;
import com.dbms.analyzer.model.Relation;

import java.util.List;
import java.util.Set;

@Service
public class BcnfDecompositionService {

    private final RelationService relationService;
    private final BcnfDecomposer bcnfDecomposer;

    public BcnfDecompositionService(
            RelationService relationService,
            BcnfDecomposer bcnfDecomposer) {
        this.relationService = relationService;
        this.bcnfDecomposer = bcnfDecomposer;
    }

    /**
     * Decomposes the current relation to BCNF.
     */
    public Set<Relation> decomposeToBcnf() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Relation current = relationService.getCurrentRelation();
        return bcnfDecomposer.decomposeToBcnf(current);
    }

    /**
     * Produces a readable step-by-step explanation of the BCNF decomposition.
     */
    public List<String> decomposeWithExplanation() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Relation current = relationService.getCurrentRelation();
        return bcnfDecomposer.decomposeWithExplanation(current);
    }
}
