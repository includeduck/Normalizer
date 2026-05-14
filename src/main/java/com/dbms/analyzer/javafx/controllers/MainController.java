package com.dbms.analyzer.javafx.controllers;

import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.service.BcnfDecompositionService;
import com.dbms.analyzer.service.CandidateKeyService;
import com.dbms.analyzer.service.ClosureService;
import com.dbms.analyzer.service.DecompositionAnalysisService;
import com.dbms.analyzer.service.FdService;
import com.dbms.analyzer.service.MinimalCoverService;
import com.dbms.analyzer.service.NormalFormService;
import com.dbms.analyzer.service.RelationService;
import com.dbms.analyzer.service.ThreeNfService;

import java.util.Collection;
import java.util.List;
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
    private final MinimalCoverService minimalCoverService;
    private final ThreeNfService threeNfService;
    private final DecompositionAnalysisService decompositionAnalysisService;

    // Cache last decomposition for analysis actions
    private Set<Relation> lastDecomposition;

    public MainController(
            RelationService relationService,
            FdService fdService,
            ClosureService closureService,
            CandidateKeyService candidateKeyService,
            NormalFormService normalFormService,
            BcnfDecompositionService bcnfDecompositionService,
            MinimalCoverService minimalCoverService,
            ThreeNfService threeNfService,
            DecompositionAnalysisService decompositionAnalysisService) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.closureService = closureService;
        this.candidateKeyService = candidateKeyService;
        this.normalFormService = normalFormService;
        this.bcnfDecompositionService = bcnfDecompositionService;
        this.minimalCoverService = minimalCoverService;
        this.threeNfService = threeNfService;
        this.decompositionAnalysisService = decompositionAnalysisService;
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

    // ---------------------------------------------------------------
    // Relation & FD management
    // ---------------------------------------------------------------

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
    private void handleRemoveFd() {
        if (!ensureRelation()) return;

        String selected = fdListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a functional dependency from the list to remove.");
            return;
        }

        // Find and remove the matching FD
        Set<FunctionalDependency> fds = fdService.getAllDependencies();
        for (FunctionalDependency fd : fds) {
            if (fd.toString().equals(selected)) {
                fdService.removeFunctionalDependency(fd);
                updateRelationDisplay();
                setStatus("Functional dependency removed");
                return;
            }
        }
        showError("Could not find the selected FD.");
    }

    @FXML
    private void handleClearAll() {
        relationService.clear();
        lastDecomposition = null;
        resultTextArea.clear();
        updateRelationDisplay();
        setStatus("Session cleared");
    }

    // ---------------------------------------------------------------
    // Analysis actions
    // ---------------------------------------------------------------

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
    private void handleMinimalCover() {
        if (!ensureRelation()) return;

        try {
            List<String> steps = minimalCoverService.computeMinimalCoverWithSteps();
            StringBuilder result = new StringBuilder();
            steps.forEach(s -> result.append(s).append("\n"));
            resultTextArea.setText(result.toString());
            setStatus("Minimal cover computed");
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
            List<String> explanation =
                    bcnfDecompositionService.decomposeWithExplanation();
            lastDecomposition = bcnfDecompositionService.decomposeToBcnf();

            StringBuilder result = new StringBuilder();
            explanation.forEach(line -> result.append(line).append("\n"));

            resultTextArea.setText(result.toString());
            setStatus("BCNF decomposition complete");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleThreeNfSynthesis() {
        if (!ensureRelation()) return;

        try {
            List<String> steps = threeNfService.synthesizeWithSteps();
            lastDecomposition = threeNfService.synthesize();

            StringBuilder result = new StringBuilder();
            steps.forEach(s -> result.append(s).append("\n"));
            resultTextArea.setText(result.toString());
            setStatus("3NF synthesis complete");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCheckDecomposition() {
        if (!ensureRelation()) return;

        if (lastDecomposition == null || lastDecomposition.isEmpty()) {
            showError("Run a decomposition (BCNF or 3NF) first.");
            return;
        }

        try {
            StringBuilder result = new StringBuilder();

            // Dependency preservation
            List<String> depSteps =
                    decompositionAnalysisService
                            .checkDependencyPreservationWithSteps(lastDecomposition);
            depSteps.forEach(s -> result.append(s).append("\n"));
            result.append("\n");

            // Lossless join
            List<String> joinSteps =
                    decompositionAnalysisService
                            .checkLosslessJoinWithSteps(lastDecomposition);
            joinSteps.forEach(s -> result.append(s).append("\n"));

            resultTextArea.setText(result.toString());
            setStatus("Decomposition analysis complete");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

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
            lastDecomposition = null;
            updateRelationDisplay();
            resultTextArea.clear();
            setStatus("Relation created");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void addFunctionalDependency(String input) {
        try {
            // Check for duplicate before adding
            FunctionalDependency candidate =
                    com.dbms.analyzer.algorithm.FdUtil.parseFD(input);
            if (fdService.getAllDependencies().contains(candidate)) {
                showError("Duplicate FD: " + candidate + " already exists.");
                return;
            }

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
