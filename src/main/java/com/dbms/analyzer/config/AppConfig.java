package com.dbms.analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.dbms.analyzer.algorithm.ClosureComputer;
import com.dbms.analyzer.algorithm.CandidateKeyFinder;
import com.dbms.analyzer.algorithm.NormalFormChecker;
import com.dbms.analyzer.algorithm.BcnfDecomposer;
import com.dbms.analyzer.algorithm.MinimalCoverComputer;
import com.dbms.analyzer.algorithm.ThreeNfSynthesizer;
import com.dbms.analyzer.algorithm.DependencyPreservationChecker;
import com.dbms.analyzer.algorithm.LosslessJoinChecker;
import com.dbms.analyzer.algorithm.FdProjector;

@Configuration
/**
 * Wires the algorithm classes with shared, stateless collaborators. Each
 * algorithm also has a no-argument constructor for tests and simple use.
 */
public class AppConfig {

    @Bean
    public ClosureComputer closureComputer() {
        return new ClosureComputer();
    }

    @Bean
    public CandidateKeyFinder candidateKeyFinder() {
        return new CandidateKeyFinder();
    }

    @Bean
    public NormalFormChecker normalFormChecker(ClosureComputer closureComputer) {
        return new NormalFormChecker(closureComputer);
    }

    @Bean
    public MinimalCoverComputer minimalCoverComputer(ClosureComputer closureComputer) {
        return new MinimalCoverComputer(closureComputer);
    }

    @Bean
    public FdProjector fdProjector(MinimalCoverComputer minimalCoverComputer) {
        return new FdProjector(minimalCoverComputer);
    }

    @Bean
    public BcnfDecomposer bcnfDecomposer(FdProjector fdProjector) {
        return new BcnfDecomposer(fdProjector);
    }

    @Bean
    public ThreeNfSynthesizer threeNfSynthesizer(
            MinimalCoverComputer minimalCoverComputer,
            CandidateKeyFinder candidateKeyFinder,
            FdProjector fdProjector) {
        return new ThreeNfSynthesizer(
            minimalCoverComputer, candidateKeyFinder, fdProjector);
    }

    @Bean
    public DependencyPreservationChecker dependencyPreservationChecker(
            ClosureComputer closureComputer) {
        return new DependencyPreservationChecker(closureComputer);
    }

    @Bean
    public LosslessJoinChecker losslessJoinChecker(ClosureComputer closureComputer) {
        return new LosslessJoinChecker(closureComputer);
    }
}
