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

@Configuration
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
    public NormalFormChecker normalFormChecker() {
        return new NormalFormChecker();
    }

    @Bean
    public BcnfDecomposer bcnfDecomposer() {
        return new BcnfDecomposer();
    }

    @Bean
    public MinimalCoverComputer minimalCoverComputer() {
        return new MinimalCoverComputer();
    }

    @Bean
    public ThreeNfSynthesizer threeNfSynthesizer() {
        return new ThreeNfSynthesizer();
    }

    @Bean
    public DependencyPreservationChecker dependencyPreservationChecker() {
        return new DependencyPreservationChecker();
    }

    @Bean
    public LosslessJoinChecker losslessJoinChecker() {
        return new LosslessJoinChecker();
    }
}
