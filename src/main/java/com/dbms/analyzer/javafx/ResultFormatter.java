package com.dbms.analyzer.javafx;

import com.dbms.analyzer.algorithm.FdOrder;
import com.dbms.analyzer.algorithm.NormalFormChecker;
import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.NormalFormResult;
import com.dbms.analyzer.model.Relation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Turns analysis results into the text shown in the UI. Kept free of
 * JavaFX types so it can be unit-tested without a display.
 */
public final class ResultFormatter {

    private static final String TUTORIAL_RESOURCE = "/text/tutorial.txt";

    private ResultFormatter() {
    }

    public static String candidateKeysReport(
            Set<CandidateKey> keys, Set<String> prime, Set<String> nonPrime) {
        StringBuilder result = new StringBuilder("Candidate Keys\n");
        if (keys.isEmpty()) {
            result.append("No candidate keys found.\n");
        } else {
            FdOrder.sortedKeys(keys)
                .forEach(key -> result.append("- ").append(key).append("\n"));
        }
        return result.append("\nPrime Attributes: ").append(formatSet(prime))
            .append("\nNon-Prime Attributes: ").append(formatSet(nonPrime))
            .toString();
    }

    public static String normalFormReport(NormalFormResult analysis) {
        StringBuilder result = new StringBuilder("Normal Form Analysis\n")
            .append("Highest Normal Form: ")
            .append(analysis.getHighestNormalForm())
            .append("\n\n")
            .append("1NF: ").append(NormalFormChecker.FIRST_NF_ASSUMPTION).append("\n")
            .append("2NF: ").append(analysis.satisfies(NormalFormResult.NormalForm.SECOND_NF)).append("\n")
            .append("3NF: ").append(analysis.satisfies(NormalFormResult.NormalForm.THIRD_NF)).append("\n")
            .append("BCNF: ").append(analysis.satisfies(NormalFormResult.NormalForm.BCNF)).append("\n");

        if (!analysis.getViolations().isEmpty()) {
            result.append("\nViolations\n");
            analysis.getViolations()
                .forEach(violation -> result.append("- ").append(violation).append("\n"));
        }

        if (analysis.getExplanation() != null && !analysis.getExplanation().isBlank()) {
            result.append("\n").append(analysis.getExplanation());
        }
        return result.toString();
    }

    public static String closureReport(String input, List<String> steps) {
        return "Closure Input: " + input + "\n\n" + renderLines(steps);
    }

    public static String decompositionCheckReport(
            List<String> preservationSteps, List<String> losslessSteps) {
        return renderLines(preservationSteps) + "\n\n" + renderLines(losslessSteps);
    }

    public static String decompositionSummary(Set<Relation> decomposition) {
        if (decomposition == null || decomposition.isEmpty()) {
            return "No decomposition run";
        }
        return formatCount(decomposition.size(), "relation", "relations")
            + ": "
            + decomposition.stream()
                .map(Relation::toString)
                .sorted()
                .collect(Collectors.joining("; "));
    }

    public static String formatSet(Collection<String> values) {
        return values.stream()
            .sorted()
            .collect(Collectors.joining(", ", "{", "}"));
    }

    public static String formatKeys(Set<CandidateKey> keys) {
        if (keys.isEmpty()) {
            return "None";
        }
        return FdOrder.sortedKeys(keys).stream()
            .map(CandidateKey::toString)
            .collect(Collectors.joining(", "));
    }

    public static String formatCount(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    public static String renderLines(List<String> lines) {
        return String.join("\n", lines);
    }

    /** The "App Tutorial" tab text, from {@value #TUTORIAL_RESOURCE}. */
    public static String tutorialText() {
        try (InputStream in = ResultFormatter.class.getResourceAsStream(TUTORIAL_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + TUTORIAL_RESOURCE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
