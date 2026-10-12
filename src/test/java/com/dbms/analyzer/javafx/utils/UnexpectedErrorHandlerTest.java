package com.dbms.analyzer.javafx.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnexpectedErrorHandlerTest {

    @Test
    public void testOutOfMemoryIsDescribedForUsers() {
        String message = UnexpectedErrorHandler.describe(
            new OutOfMemoryError("Java heap space"));
        assertTrue(message.contains("ran out of memory"), message);
    }

    @Test
    public void testStackOverflowIsDescribedForUsers() {
        String message = UnexpectedErrorHandler.describe(new StackOverflowError());
        assertTrue(message.contains("too deep"), message);
    }

    @Test
    public void testOtherErrorsShowTypeAndMessage() {
        assertEquals("IllegalStateException: broken",
            UnexpectedErrorHandler.describe(new IllegalStateException("broken")));
        assertEquals("NullPointerException",
            UnexpectedErrorHandler.describe(new NullPointerException()));
    }
}
