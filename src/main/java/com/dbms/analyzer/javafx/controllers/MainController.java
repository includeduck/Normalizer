package com.dbms.analyzer.javafx.controllers;

import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.service.BcnfDecompositionService;
import com.dbms.analyzer.service.CandidateKeyService;
import com.dbms.analyzer.service.ClosureService;
import com.dbms.analyzer.service.RelationService;
import com.dbms.analyzer.service.FdService;
import com.dbms.analyzer.service.NormalFormService;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    private final RelationService relationService;
    private final FdService fdService;
    private final ClosureService closureService;
    private final CandidateKeyService candidateKeyService;
    private final NormalFormService normalFormService;
    private final BcnfDecompositionService bcnfDecompositionService;

    public MainController(
            RelationService relationService,
            FdService fdService,
            ClosureService closureService,
            CandidateKeyService candidateKeyService,
            NormalFormService normalFormService,
            BcnfDecompositionService bcnfDecompositionService) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.closureService = closureService;
        this.candidateKeyService = candidateKeyService;
        this.normalFormService = normalFormService;
        this.bcnfDecompositionService = bcnfDecompositionService;
    }

    @FXML
    private Label relationLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<String> fdListView;

    @FXML
    private TextArea resultTextArea;

    private static String formatSet(Collection<String> values) {
        return values.stream()
            .sorted()
            .collect(Collectors.joining(", ", "{", "}"));
    }

    @FXML
    public void initialize() {
        updateRelationDisplay();
        setStatus("Ready");
    }

    @FXML
    private void handleCreateRelation() {
        TextInputDialog dialog = new TextInputDialog("R(A,B,C)");
        dialog.setTitle("Create Relation");
        dialog.setHeaderText(null);
        dialog.setContentText("Relation schema:");

        dialog.showAndWait()
            .map(String::trim)
            .filter(input -> !input.isEmpty())
            .ifPresent(this::createRelation);
    }

    @FXML
    private void handleAddFd() {
        if (!ensureRelation()) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog("A -> B");
        dialog.setTitle("Add Functional Dependency");
        dialog.setHeaderText(null);
        dialog.setContentText("Functional dependency:");

        dialog.showAndWait()
            .map(String::trim)
            .filter(input -> !input.isEmpty())
            .ifPresent(this::addFunctionalDependency);
    }

    @FXML
    private void handleComputeClosure() {
        if (!ensureRelation()) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Compute Closure");
        dialog.setHeaderText(null);
        dialog.setContentText("Attribute set:");

        dialog.showAndWait()
            .map(String::trim)
            .filter(input -> !input.isEmpty())
            .ifPresent(this::computeClosure);
    }

    @FXML
    private void handleFindCandidateKeys() {
        if (!ensureRelation()) {
            return;
        }

        try {
            Set<CandidateKey> keys = candidateKeyService.findAllCandidateKeys();
            StringBuilder result = new StringBuilder("Candidate Keys\n");
            if (keys.isEmpty()) {
                result.append("No candidate keys found.\n");
            } else {
                keys.stream()
                    .map(CandidateKey::toString)
                    .sorted()
                    .forEach(key -> result.append("- ").append(key).append("\n"));
            }

            result.append("\nPrime Attributes: ")
                .append(formatSet(candidateKeyService.getPrimeAttributes()))
                .append("\nNon-Prime Attributes: ")
                .append(formatSet(candidateKeyService.getNonPrimeAttributes()));

            resultTextArea.setText(result.toString());
            setStatus("Candidate keys computed");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleAnalyzeNormalForm() {
        if (!ensureRelation()) {
            return;
        }

        try {
            var analysis = normalFormService.analyzeNormalForm();
            StringBuilder result = new StringBuilder("Normal Form Analysis\n");
            result.append("Highest Normal Form: ")
                .append(analysis.getHighestNormalForm())
                .append("\n\n");
            result.append("1NF: ").append(normalFormService.is1NF()).append("\n");
            result.append("2NF: ").append(normalFormService.is2NF()).append("\n");
            result.append("3NF: ").append(normalFormService.is3NF()).append("\n");
            result.append("BCNF: ").append(normalFormService.isBCNF()).append("\n");

            if (!analysis.getViolations().isEmpty()) {
                result.append("\nViolations\n");
                analysis.getViolations().stream()
                    .sorted()
                    .forEach(violation -> result.append("- ")
                        .append(violation)
                        .append("\n"));
            }

            if (analysis.getExplanation() != null
                    && !analysis.getExplanation().isBlank()) {
                result.append("\n").append(analysis.getExplanation());
            }

            resultTextArea.setText(result.toString());
            setStatus("Normal form analysis complete");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleBcnfDecomposition() {
        if (!ensureRelation()) {
            return;
        }

        try {
            Set<Relation> relations = bcnfDecompositionService.decomposeToBcnf();
            StringBuilder result = new StringBuilder("BCNF Decomposition\n");
            if (relations.size() == 1
                    && relations.iterator().next().toString().equals(
                        relationService.getCurrentRelation().toString())) {
                result.append("Relation is already in BCNF.\n");
            } else {
                relations.stream()
                    .map(Relation::toString)
                    .sorted()
                    .forEach(relation -> result.append("- ")
                        .append(relation)
                        .append("\n"));
            }

            resultTextArea.setText(result.toString());
            setStatus("BCNF decomposition complete");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void updateRelationDisplay() {
        if (relationService.hasRelation()) {
            relationLabel.setText(
                relationService.getCurrentRelation().toString());
            fdListView.getItems().clear();
            fdService.getAllDependencies().stream()
                .map(Object::toString)
                .sorted()
                .forEach(fdListView.getItems()::add);
        } else {
            relationLabel.setText("No relation defined");
            fdListView.getItems().clear();
        }
    }

    private void createRelation(String input) {
        try {
            relationService.createRelation(input);
            updateRelationDisplay();
            resultTextArea.clear();
            setStatus("Relation created");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void addFunctionalDependency(String input) {
        try {
            fdService.addFunctionalDependency(input);
            updateRelationDisplay();
            setStatus("Functional dependency added");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void computeClosure(String input) {
        try {
            var closure = closureService.computeClosure(input);
            var steps = closureService.computeClosureWithSteps(input);
            StringBuilder result = new StringBuilder("Closure Input: ")
                .append(input)
                .append("\n\n");

            steps.forEach(step -> result.append(step).append("\n"));
            result.append("\nFinal Closure: ").append(formatSet(closure));

            resultTextArea.setText(result.toString());
            setStatus("Closure computed");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private boolean ensureRelation() {
        if (relationService.hasRelation()) {
            return true;
        }

        showError("Create a relation before running this action.");
        return false;
    }

    private void showError(String message) {
        resultTextArea.setText("Error: " + message);
        setStatus("Error");
    }

    private void setStatus(String message) {
        statusLabel.setText("Status: " + message);
    }
}
