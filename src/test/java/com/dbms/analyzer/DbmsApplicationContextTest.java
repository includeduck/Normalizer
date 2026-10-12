package com.dbms.analyzer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.dbms.analyzer.javafx.controllers.MainController;
import com.dbms.analyzer.service.RelationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DbmsApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoadsCoreBeans() {
        assertNotNull(applicationContext.getBean(RelationService.class));
        assertNotNull(applicationContext.getBean(MainController.class));
    }

    @Test
    void algorithmBeansShareTheirCollaborators() {
        // CQ-002: algorithms used to build private copies of their helpers
        Object closureComputer = applicationContext.getBean(
            com.dbms.analyzer.algorithm.ClosureComputer.class);
        Object fdProjector = applicationContext.getBean(
            com.dbms.analyzer.algorithm.FdProjector.class);

        assertSame(closureComputer, ReflectionTestUtils.getField(
            applicationContext.getBean(com.dbms.analyzer.algorithm.NormalFormChecker.class),
            "closureComputer"));
        assertSame(closureComputer, ReflectionTestUtils.getField(
            applicationContext.getBean(com.dbms.analyzer.algorithm.MinimalCoverComputer.class),
            "closureComputer"));
        assertSame(fdProjector, ReflectionTestUtils.getField(
            applicationContext.getBean(com.dbms.analyzer.algorithm.BcnfDecomposer.class),
            "fdProjector"));
        assertSame(fdProjector, ReflectionTestUtils.getField(
            applicationContext.getBean(com.dbms.analyzer.algorithm.ThreeNfSynthesizer.class),
            "fdProjector"));
    }
}
