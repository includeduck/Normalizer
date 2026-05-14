package com.dbms.analyzer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dbms.analyzer.javafx.controllers.MainController;
import com.dbms.analyzer.service.RelationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DbmsApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoadsCoreBeans() {
        assertNotNull(applicationContext.getBean(RelationService.class));
        assertNotNull(applicationContext.getBean(MainController.class));
    }
}
