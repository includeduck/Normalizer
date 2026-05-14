package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.ThreeNfSynthesizer;
import com.dbms.analyzer.model.Relation;

import java.util.List;
import java.util.Set;

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
     * Synthesizes a 3NF decomposition of the current relation.
     */
    public Set<Relation> synthesize() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        return threeNfSynthesizer.synthesize(
                relationService.getCurrentRelation());
    }

    /**
     * Synthesizes with step-by-step explanation.
     */
    public List<String> synthesizeWithSteps() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        return threeNfSynthesizer.synthesizeWithSteps(
                relationService.getCurrentRelation());
    }
}
