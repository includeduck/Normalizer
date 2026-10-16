package com.dbms.analyzer.service;

import com.dbms.analyzer.algorithm.FdOrder;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Reads and writes the plain-text relation definition used by the
 * Definition box and by "Open File…" / "Save…":
 *
 * <pre>
 * # comment
 * relation = R(A, B, C)
 * FD = {
 *     A -> B
 *     B, C -> A
 * }
 * closure = A          # optional
 * </pre>
 *
 * <ul>
 *   <li>Keys are case-insensitive; {@code FDs} is accepted for {@code FD}.</li>
 *   <li>One FD per line inside the braces; {@code ;} also separates FDs, so
 *       {@code FD = { A -> B; B -> C }} works on one line.</li>
 *   <li>{@code #} starts a comment (it cannot appear in a valid name).</li>
 * </ul>
 *
 * Only the structure is checked here; names and FDs are validated when the
 * definition is applied ({@link FdService#loadDefinition}), with the same
 * rules as the input fields. Every element keeps its line number for error
 * messages.
 */
public final class RelationDefinitionFormat {

    /** A piece of the definition and the 1-based line it came from. */
    public record Line(int number, String text) {
    }

    /** The parsed definition; {@code closure} may be null. */
    public record Definition(Line relation, List<Line> dependencies, Line closure) {
    }

    private RelationDefinitionFormat() {
    }

    /**
     * @throws IllegalArgumentException with a "Line N: ..." message if the
     *         text is not a well-formed definition
     */
    public static Definition parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                "The definition is empty. Start with a line like: relation = R(A, B, C)");
        }

        String[] lines = text.replace("﻿", "").split("\\R", -1);
        Line relation = null;
        Line closure = null;
        List<Line> dependencies = new ArrayList<>();
        boolean sawDependencies = false;
        int blockStart = -1;

        for (int i = 0; i < lines.length; i++) {
            int number = i + 1;
            String line = stripComment(lines[i]).strip();
            if (line.isEmpty()) {
                continue;
            }

            if (blockStart > 0) {
                int close = line.indexOf('}');
                if (close >= 0) {
                    requireNothingAfter(line, close, number);
                    addDependencies(line.substring(0, close), number, dependencies);
                    blockStart = -1;
                } else {
                    if (line.indexOf('{') >= 0) {
                        throw error(number, "unexpected '{' inside the FD block");
                    }
                    addDependencies(line, number, dependencies);
                }
                continue;
            }

            int equals = line.indexOf('=');
            if (equals < 0) {
                throw error(number, "expected 'relation = ...', 'FD = { ... }' or"
                    + " 'closure = ...' but found '" + line + "'");
            }
            String key = line.substring(0, equals).strip().toLowerCase(Locale.ROOT);
            String value = line.substring(equals + 1).strip();

            switch (key) {
                case "relation" -> {
                    if (relation != null) {
                        throw error(number, "the relation is already defined on line "
                            + relation.number());
                    }
                    if (value.isEmpty()) {
                        throw error(number, "missing relation after '=', e.g. R(A, B, C)");
                    }
                    relation = new Line(number, value);
                }
                case "fd", "fds" -> {
                    if (sawDependencies) {
                        throw error(number, "FDs are already defined; use a single FD = { ... } block");
                    }
                    sawDependencies = true;
                    if (!value.startsWith("{")) {
                        throw error(number, "expected '{' after 'FD =', e.g. FD = { A -> B }");
                    }
                    String rest = value.substring(1);
                    int close = rest.indexOf('}');
                    if (close >= 0) {
                        requireNothingAfter(rest, close, number);
                        addDependencies(rest.substring(0, close), number, dependencies);
                    } else {
                        addDependencies(rest, number, dependencies);
                        blockStart = number;
                    }
                }
                case "closure" -> {
                    if (closure != null) {
                        throw error(number, "closure is already defined on line "
                            + closure.number());
                    }
                    if (!value.isEmpty()) {
                        closure = new Line(number, value);
                    }
                }
                default -> throw error(number, "unknown setting '" + key
                    + "'; use relation, FD or closure");
            }
        }

        if (blockStart > 0) {
            throw error(blockStart, "the FD block opened here is never closed with '}'");
        }
        if (relation == null) {
            throw new IllegalArgumentException(
                "No relation defined. Add a line like: relation = R(A, B, C)");
        }
        return new Definition(relation, List.copyOf(dependencies), closure);
    }

    /** Writes a relation (and an optional closure input) in this format. */
    public static String format(Relation relation, String closure) {
        StringBuilder text = new StringBuilder("relation = ")
            .append(relation.getName()).append("(")
            .append(String.join(", ", new TreeSet<>(relation.getAttributes())))
            .append(")\n\nFD = {\n");
        for (FunctionalDependency fd : FdOrder.sorted(relation.getFunctionalDependencies())) {
            text.append("    ")
                .append(String.join(", ", new TreeSet<>(fd.getLeftSide())))
                .append(" -> ")
                .append(String.join(", ", new TreeSet<>(fd.getRightSide())))
                .append("\n");
        }
        text.append("}\n");
        if (closure != null && !closure.isBlank()) {
            text.append("\nclosure = ").append(closure.strip()).append("\n");
        }
        return text.toString();
    }

    private static void addDependencies(String text, int number, List<Line> out) {
        for (String part : text.split(";", -1)) {
            String dependency = part.strip();
            if (dependency.isEmpty()) {
                continue;
            }
            if (dependency.indexOf('{') >= 0 || dependency.indexOf('=') >= 0
                    && !dependency.contains("->")) {
                throw error(number, "expected an FD like A -> B but found '" + dependency + "'");
            }
            out.add(new Line(number, dependency));
        }
    }

    private static void requireNothingAfter(String line, int closeIndex, int number) {
        if (!line.substring(closeIndex + 1).isBlank()) {
            throw error(number, "unexpected text after '}'");
        }
    }

    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        return hash >= 0 ? line.substring(0, hash) : line;
    }

    static IllegalArgumentException error(int number, String message) {
        return new IllegalArgumentException("Line " + number + ": " + message);
    }
}
