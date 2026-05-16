package com.dbms.analyzer.javafx.controllers;

import com.dbms.analyzer.algorithm.FdUtil;
import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.service.BcnfDecompositionService;
import com.dbms.analyzer.service.CandidateKeyService;
import com.dbms.analyzer.service.ClosureService;
import com.dbms.analyzer.service.DecompositionAnalysisService;
import com.dbms.analyzer.service.ExplanationService;
import com.dbms.analyzer.service.FdService;
import com.dbms.analyzer.service.MinimalCoverService;
import com.dbms.analyzer.service.NormalFormService;
import com.dbms.analyzer.service.RelationService;
import com.dbms.analyzer.service.ThreeNfService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    private static final String VALIDATION_ERROR = "validation-error";
    private static final String VALIDATION_OK = "validation-ok";
    private static final String STATUS_ERROR = "status-error";
    private static final String STATUS_OK = "status-ok";

    private final RelationService relationService;
    private final FdService fdService;
    private final ClosureService closureService;
    private final CandidateKeyService candidateKeyService;
    private final NormalFormService normalFormService;
    private final BcnfDecompositionService bcnfDecompositionService;
    private final MinimalCoverService minimalCoverService;
    private final ThreeNfService threeNfService;
    private final DecompositionAnalysisService decompositionAnalysisService;
    private final ExplanationService explanationService;

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
            DecompositionAnalysisService decompositionAnalysisService,
            ExplanationService explanationService) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.closureService = closureService;
        this.candidateKeyService = candidateKeyService;
        this.normalFormService = normalFormService;
        this.bcnfDecompositionService = bcnfDecompositionService;
        this.minimalCoverService = minimalCoverService;
        this.threeNfService = threeNfService;
        this.decompositionAnalysisService = decompositionAnalysisService;
        this.explanationService = explanationService;
    }

    @FXML
    private TextField relationInputField;
    @FXML
    private TextField fdInputField;
    @FXML
    private TextField closureInputField;

    @FXML
    private Label relationValidationLabel;
    @FXML
    private Label fdValidationLabel;
    @FXML
    private Label closureValidationLabel;

    @FXML
    private Label relationLabel;
    @FXML
    private Label attributeSummaryLabel;
    @FXML
    private Label fdCountLabel;
    @FXML
    private Label keySummaryLabel;
    @FXML
    private Label normalFormSummaryLabel;
    @FXML
    private Label decompositionSummaryLabel;
    @FXML
    private Label statusLabel;

    @FXML
    private ListView<String> fdListView;

    @FXML
    private Button addFdButton;
    @FXML
    private Button removeFdButton;
    @FXML
    private Button computeClosureButton;
    @FXML
    private Button candidateKeysButton;
    @FXML
    private Button normalFormButton;
    @FXML
    private Button minimalCoverButton;
    @FXML
    private Button explanationButton;
    @FXML
    private Button bcnfButton;
    @FXML
    private Button threeNfButton;
    @FXML
    private Button checkDecompositionButton;

    @FXML
    private TabPane resultTabPane;
    @FXML
    private Tab closureTab;
    @FXML
    private Tab keysTab;
    @FXML
    private Tab normalFormTab;
    @FXML
    private Tab minimalCoverTab;
    @FXML
    private Tab decompositionTab;
    @FXML
    private Tab explanationTab;

    @FXML
    private TextArea closureResultArea;
    @FXML
    private TextArea keysResultArea;
    @FXML
    private TextArea normalFormResultArea;
    @FXML
    private TextArea minimalCoverResultArea;
    @FXML
    private TextArea decompositionResultArea;
    @FXML
    private TextArea explanationResultArea;

    @FXML
    public void initialize() {
        relationInputField.textProperty().addListener(
            (observable, oldValue, newValue) -> clearValidation(relationValidationLabel));
        fdInputField.textProperty().addListener(
            (observable, oldValue, newValue) -> clearValidation(fdValidationLabel));
        closureInputField.textProperty().addListener(
            (observable, oldValue, newValue) -> clearValidation(closureValidationLabel));
        fdListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateControls());

        clearResultAreas();
        updateRelationDisplay();
        setStatus("Ready", false);
    }

    // ---------------------------------------------------------------
    // Relation & FD management
    // ---------------------------------------------------------------

    @FXML
    private void handleCreateRelation() {
        String input = relationInputField.getText().trim();
        if (input.isEmpty()) {
            setValidation(
                relationValidationLabel,
                "Enter a relation schema like R(A,B,C).",
                true);
            relationInputField.requestFocus();
            setStatus("Relation schema required", true);
            return;
        }

        createRelation(input);
    }

    @FXML
    private void handleAddFd() {
        if (!ensureRelation(fdValidationLabel)) {
            return;
        }

        String input = fdInputField.getText().trim();
        if (input.isEmpty()) {
            setValidation(
                fdValidationLabel,
                "Enter a dependency like A,B -> C.",
                true);
            fdInputField.requestFocus();
            setStatus("Functional dependency required", true);
            return;
        }

        addFunctionalDependency(input);
    }

    @FXML
    private void handleRemoveFd() {
        if (!ensureRelation(fdValidationLabel)) {
            return;
        }

        String selected = fdListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setValidation(
                fdValidationLabel,
                "Select a dependency in the summary list first.",
                true);
            setStatus("No dependency selected", true);
            return;
        }

        for (FunctionalDependency fd : fdService.getAllDependencies()) {
            if (fd.toString().equals(selected)) {
                fdService.removeFunctionalDependency(fd);
                lastDecomposition = null;
                updateRelationDisplay();
                setValidation(fdValidationLabel, "Dependency removed.", false);
                setStatus("Functional dependency removed", false);
                return;
            }
        }

        setValidation(fdValidationLabel, "Selected dependency could not be found.", true);
        setStatus("Remove failed", true);
    }

    @FXML
    private void handleClearAll() {
        relationService.clear();
        lastDecomposition = null;
        relationInputField.clear();
        fdInputField.clear();
        closureInputField.clear();
        clearValidations();
        clearResultAreas();
        updateRelationDisplay();
        setStatus("Session cleared", false);
    }

    // ---------------------------------------------------------------
    // Analysis actions
    // ---------------------------------------------------------------

    @FXML
    private void handleComputeClosure() {
        if (!ensureRelation(closureValidationLabel)) {
            return;
        }

        String input = closureInputField.getText().trim();
        if (input.isEmpty()) {
            setValidation(
                closureValidationLabel,
                "Enter one or more attributes.",
                true);
            closureInputField.requestFocus();
            setStatus("Closure input required", true);
            return;
        }

        computeClosure(input);
    }

    @FXML
    private void handleFindCandidateKeys() {
        if (!ensureRelation(null)) {
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

            keysResultArea.setText(result.toString());
            selectTab(keysTab);
            updateRelationDisplay();
            setStatus("Candidate keys computed", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), keysResultArea, keysTab);
        }
    }

    @FXML
    private void handleAnalyzeNormalForm() {
        if (!ensureRelation(null)) {
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

            normalFormResultArea.setText(result.toString());
            selectTab(normalFormTab);
            updateRelationDisplay();
            setStatus("Normal form analysis complete", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), normalFormResultArea, normalFormTab);
        }
    }

    @FXML
    private void handleMinimalCover() {
        if (!ensureRelation(null)) {
            return;
        }

        try {
            List<String> steps = minimalCoverService.computeMinimalCoverWithSteps();
            minimalCoverResultArea.setText(renderLines(steps));
            selectTab(minimalCoverTab);
            setStatus("Minimal cover computed", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), minimalCoverResultArea, minimalCoverTab);
        }
    }

    @FXML
    private void handleBcnfDecomposition() {
        if (!ensureRelation(null)) {
            return;
        }

        try {
            List<String> explanation =
                    bcnfDecompositionService.decomposeWithExplanation();
            lastDecomposition = bcnfDecompositionService.decomposeToBcnf();

            decompositionResultArea.setText(renderLines(explanation));
            selectTab(decompositionTab);
            updateRelationDisplay();
            setStatus("BCNF decomposition complete", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), decompositionResultArea, decompositionTab);
        }
    }

    @FXML
    private void handleThreeNfSynthesis() {
        if (!ensureRelation(null)) {
            return;
        }

        try {
            List<String> steps = threeNfService.synthesizeWithSteps();
            lastDecomposition = threeNfService.synthesize();

            decompositionResultArea.setText(renderLines(steps));
            selectTab(decompositionTab);
            updateRelationDisplay();
            setStatus("3NF synthesis complete", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), decompositionResultArea, decompositionTab);
        }
    }

    @FXML
    private void handleCheckDecomposition() {
        if (!ensureRelation(null)) {
            return;
        }

        if (lastDecomposition == null || lastDecomposition.isEmpty()) {
            showActionError(
                "Run a decomposition (BCNF or 3NF) first.",
                explanationResultArea,
                explanationTab);
            return;
        }

        try {
            StringBuilder result = new StringBuilder();

            List<String> depSteps =
                    decompositionAnalysisService
                            .checkDependencyPreservationWithSteps(lastDecomposition);
            depSteps.forEach(s -> result.append(s).append("\n"));
            result.append("\n");

            List<String> joinSteps =
                    decompositionAnalysisService
                            .checkLosslessJoinWithSteps(lastDecomposition);
            joinSteps.forEach(s -> result.append(s).append("\n"));

            explanationResultArea.setText(result.toString());
            selectTab(explanationTab);
            setStatus("Decomposition analysis complete", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), explanationResultArea, explanationTab);
        }
    }

    @FXML
    private void handleShowExplanation() {
        if (!ensureRelation(null)) {
            return;
        }

        try {
            List<String> explanation = new ArrayList<>();
            explanation.addAll(explanationService.explainRelation());
            explanation.add("");
            explanation.addAll(explanationService.explainCandidateKeys());
            explanation.add("");
            explanation.addAll(explanationService.explainNormalForm());

            explanationResultArea.setText(renderLines(explanation));
            selectTab(explanationTab);
            updateRelationDisplay();
            setStatus("Explanation refreshed", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), explanationResultArea, explanationTab);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void updateRelationDisplay() {
        refreshFdList();
        refreshSummary();
        updateControls();
    }

    private void refreshFdList() {
        fdListView.getItems().clear();
        if (!relationService.hasRelation()) {
            return;
        }

        fdService.getAllDependencies().stream()
            .map(Object::toString)
            .sorted()
            .forEach(fdListView.getItems()::add);
    }

    private void refreshSummary() {
        if (!relationService.hasRelation()) {
            relationLabel.setText("No relation defined");
            attributeSummaryLabel.setText("{}");
            fdCountLabel.setText("0 dependencies");
            keySummaryLabel.setText("Not computed");
            normalFormSummaryLabel.setText("Not analyzed");
            decompositionSummaryLabel.setText("No decomposition run");
            return;
        }

        Relation relation = relationService.getCurrentRelation();
        Set<String> attributes = relationService.getAttributes();
        Set<FunctionalDependency> fds = fdService.getAllDependencies();

        relationLabel.setText(relation.toString());
        attributeSummaryLabel.setText(formatSet(attributes));
        fdCountLabel.setText(formatCount(fds.size(), "dependency", "dependencies"));

        try {
            Set<CandidateKey> keys = candidateKeyService.findAllCandidateKeys();
            keySummaryLabel.setText(formatKeys(keys));
        } catch (RuntimeException e) {
            keySummaryLabel.setText("Unavailable");
        }

        try {
            normalFormSummaryLabel.setText(
                normalFormService.analyzeNormalForm()
                    .getHighestNormalForm()
                    .toString());
        } catch (RuntimeException e) {
            normalFormSummaryLabel.setText("Unavailable");
        }

        if (lastDecomposition == null || lastDecomposition.isEmpty()) {
            decompositionSummaryLabel.setText("No decomposition run");
        } else {
            decompositionSummaryLabel.setText(
                formatCount(lastDecomposition.size(), "relation", "relations")
                    + ": "
                    + lastDecomposition.stream()
                        .map(Relation::toString)
                        .sorted()
                        .collect(Collectors.joining("; ")));
        }
    }

    private void updateControls() {
        boolean hasRelation = relationService.hasRelation();
        addFdButton.setDisable(!hasRelation);
        removeFdButton.setDisable(
            !hasRelation || fdListView.getSelectionModel().getSelectedItem() == null);
        computeClosureButton.setDisable(!hasRelation);
        candidateKeysButton.setDisable(!hasRelation);
        normalFormButton.setDisable(!hasRelation);
        minimalCoverButton.setDisable(!hasRelation);
        explanationButton.setDisable(!hasRelation);
        bcnfButton.setDisable(!hasRelation);
        threeNfButton.setDisable(!hasRelation);
        checkDecompositionButton.setDisable(
            !hasRelation || lastDecomposition == null || lastDecomposition.isEmpty());
    }

    private void createRelation(String input) {
        try {
            relationService.createRelation(input);
            lastDecomposition = null;
            fdInputField.clear();
            closureInputField.clear();
            clearResultAreas();
            updateRelationDisplay();
            setValidation(relationValidationLabel, "Relation ready.", false);
            setStatus("Relation created", false);
        } catch (IllegalArgumentException e) {
            setValidation(relationValidationLabel, e.getMessage(), true);
            setStatus("Relation could not be created", true);
        }
    }

    private void addFunctionalDependency(String input) {
        try {
            FunctionalDependency candidate =
                    FdUtil.parseFD(input, relationService.getAttributes());
            if (fdService.getAllDependencies().contains(candidate)) {
                setValidation(
                    fdValidationLabel,
                    "Duplicate FD: " + candidate + " already exists.",
                    true);
                setStatus("Duplicate dependency", true);
                return;
            }

            fdService.addFunctionalDependency(input);
            lastDecomposition = null;
            fdInputField.clear();
            updateRelationDisplay();
            setValidation(fdValidationLabel, "Dependency added.", false);
            setStatus("Functional dependency added", false);
        } catch (RuntimeException e) {
            setValidation(fdValidationLabel, e.getMessage(), true);
            setStatus("Dependency could not be added", true);
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

            closureResultArea.setText(result.toString());
            selectTab(closureTab);
            setValidation(closureValidationLabel, "Closure computed.", false);
            setStatus("Closure computed", false);
        } catch (RuntimeException e) {
            setValidation(closureValidationLabel, e.getMessage(), true);
            showActionError(e.getMessage(), closureResultArea, closureTab);
        }
    }

    private boolean ensureRelation(Label validationLabel) {
        if (relationService.hasRelation()) {
            return true;
        }

        if (validationLabel != null) {
            setValidation(validationLabel, "Create a relation first.", true);
        }
        setValidation(relationValidationLabel, "Create a relation before running this action.", true);
        relationInputField.requestFocus();
        setStatus("Create a relation first", true);
        return false;
    }

    private void showActionError(String message, TextArea area, Tab tab) {
        area.setText("Error: " + message);
        selectTab(tab);
        setStatus(message, true);
    }

    private void selectTab(Tab tab) {
        resultTabPane.getSelectionModel().select(tab);
    }

    private void setValidation(Label label, String message, boolean error) {
        label.setText(message == null ? "" : message);
        label.getStyleClass().removeAll(VALIDATION_ERROR, VALIDATION_OK);
        if (message != null && !message.isBlank()) {
            label.getStyleClass().add(error ? VALIDATION_ERROR : VALIDATION_OK);
        }
    }

    private void clearValidation(Label label) {
        setValidation(label, "", false);
    }

    private void clearValidations() {
        clearValidation(relationValidationLabel);
        clearValidation(fdValidationLabel);
        clearValidation(closureValidationLabel);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText("Status: " + message);
        statusLabel.getStyleClass().removeAll(STATUS_ERROR, STATUS_OK);
        statusLabel.getStyleClass().add(error ? STATUS_ERROR : STATUS_OK);
    }

    private void clearResultAreas() {
        closureResultArea.clear();
        keysResultArea.clear();
        normalFormResultArea.clear();
        minimalCoverResultArea.clear();
        decompositionResultArea.clear();
        explanationResultArea.clear();
        selectTab(closureTab);
    }

    private static String formatSet(Collection<String> values) {
        return values.stream()
            .sorted()
            .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String formatKeys(Set<CandidateKey> keys) {
        if (keys.isEmpty()) {
            return "None";
        }

        return keys.stream()
            .map(CandidateKey::toString)
            .sorted()
            .collect(Collectors.joining(", "));
    }

    private static String formatCount(
            int count,
            String singular,
            String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    private static String renderLines(List<String> lines) {
        return String.join(System.lineSeparator(), lines);
    }
}
