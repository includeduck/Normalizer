package com.dbms.analyzer.javafx.utils;

import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxSpringInjector {

    private static ConfigurableApplicationContext applicationContext;

    /**
     * Sets the Spring application context
     */
    public static void setApplicationContext(
            ConfigurableApplicationContext context) {
        applicationContext = context;
    }

    /**
     * Gets a bean from the Spring context
     */
    public static <T> T getBean(Class<T> type) {
        if (applicationContext == null) {
            throw new IllegalStateException(
                "Application context not initialized");
        }
        return applicationContext.getBean(type);
    }

    /**
     * Gets a bean by name from the Spring context
     */
    public static Object getBean(String name) {
        if (applicationContext == null) {
            throw new IllegalStateException(
                "Application context not initialized");
        }
        return applicationContext.getBean(name);
    }
}
