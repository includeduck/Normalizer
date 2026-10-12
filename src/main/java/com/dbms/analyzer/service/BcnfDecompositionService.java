package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.BcnfDecomposer;

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
     * Decomposes the current relation to BCNF, returning the relations and
     * the step-by-step explanation from the same run.
     */
    public BcnfDecomposer.Decomposition decomposeWithTrace() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        return bcnfDecomposer.decomposeWithTrace(
            relationService.getCurrentRelation());
    }
}
