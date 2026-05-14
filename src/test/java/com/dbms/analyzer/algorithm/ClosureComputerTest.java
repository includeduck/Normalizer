package com.dbms.analyzer.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import com.dbms.analyzer.model.FunctionalDependency;

public class ClosureComputerTest {

    private ClosureComputer closureComputer = new ClosureComputer();

    @Test
    public void testSimpleClosure() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        
        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("A"), Set.of("B")));
        
        Set<String> closure = closureComputer.computeClosure(
            attributes, fds);
        
        assertTrue(closure.contains("A"));
        assertTrue(closure.contains("B"));
    }

    @Test
    public void testTransitiveClosure() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        
        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("A"), Set.of("B")));
        fds.add(new FunctionalDependency(
            Set.of("B"), Set.of("C")));
        
        Set<String> closure = closureComputer.computeClosure(
            attributes, fds);
        
        assertTrue(closure.contains("A"));
        assertTrue(closure.contains("B"));
        assertTrue(closure.contains("C"));
    }

    @Test
    public void testEmptyFds() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        
        Set<FunctionalDependency> fds = new HashSet<>();
        
        Set<String> closure = closureComputer.computeClosure(
            attributes, fds);
        
        assertEquals(attributes, closure);
    }
}
