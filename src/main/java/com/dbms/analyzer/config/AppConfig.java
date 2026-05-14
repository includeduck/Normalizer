package com.dbms.analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.dbms.analyzer.algorithm.ClosureComputer;
import com.dbms.analyzer.algorithm.CandidateKeyFinder;
import com.dbms.analyzer.algorithm.NormalFormChecker;
import com.dbms.analyzer.algorithm.BcnfDecomposer;

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
}
