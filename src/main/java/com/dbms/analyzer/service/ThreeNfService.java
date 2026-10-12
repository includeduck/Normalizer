package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.ThreeNfSynthesizer;


@Service
public class ThreeNfService {

    private final RelationService relationService;
    private final ThreeNfSynthesizer threeNfSynthesizer;

    public ThreeNfService(
            RelationService relationService,
            ThreeNfSynthesizer threeNfSynthesizer) {
        this.relationService = relationService;
        this.threeNfSynthesizer = threeNfSynthesizer;
    }

    /**
     * Synthesizes a 3NF decomposition of the current relation, returning the
     * relations and the step-by-step trace from the same run.
     */
    public ThreeNfSynthesizer.Synthesis synthesizeWithTrace() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        return threeNfSynthesizer.synthesizeWithTrace(
                relationService.getCurrentRelation());
    }
}
