package com.dbms.analyzer.javafx.controllers;

import com.dbms.analyzer.algorithm.FdUtil;
import com.dbms.analyzer.algorithm.FdOrder;
import com.dbms.analyzer.javafx.ResultFormatter;
import com.dbms.analyzer.javafx.SampleRelations;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
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

    private Stage stage;
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

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.fullScreenProperty().addListener(
            (observable, oldValue, newValue) -> updateWindowModeButtons());
        this.stage.maximizedProperty().addListener(
            (observable, oldValue, newValue) -> updateWindowModeButtons());
        updateWindowModeButtons();
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
    private ListView<FunctionalDependency> fdListView;
    @FXML
    private ComboBox<String> sampleComboBox;

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
    private ToggleButton windowedButton;
    @FXML
    private ToggleButton maximizedButton;
    @FXML
    private ToggleButton fullscreenButton;

    @FXML
    private TabPane resultTabPane;
    @FXML
    private Tab tutorialTab;
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
    private TextArea tutorialResultArea;
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
    private Tab checkResultTab;
    @FXML
    private TextArea checkResultArea;
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
        sampleComboBox.getItems().setAll(
            SampleRelations.ALL.stream()
                .map(SampleRelations.Sample::name)
                .collect(Collectors.toList()));
        sampleComboBox.getSelectionModel().selectFirst();

        tutorialResultArea.setText(ResultFormatter.tutorialText());
        clearResultAreas();
        updateRelationDisplay();
        updateValidationVisibility();
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

        FunctionalDependency selected = fdListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setValidation(
                fdValidationLabel,
                "Select a dependency in the summary list first.",
                true);
            setStatus("No dependency selected", true);
            return;
        }

        fdService.removeFunctionalDependency(selected);
        lastDecomposition = null;
        updateRelationDisplay();
        setValidation(fdValidationLabel, "Dependency removed.", false);
        setStatus("Functional dependency removed", false);
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

    @FXML
    private void handleLoadSample() {
        SampleRelations.Sample sample = SampleRelations.byName(
            sampleComboBox.getSelectionModel().getSelectedItem());

        try {
            // All-or-nothing: the current relation only changes if every FD is valid
            fdService.loadRelation(sample.schema(), sample.dependencies());

            lastDecomposition = null;
            relationInputField.setText(sample.schema());
            fdInputField.clear();
            closureInputField.setText(sample.closureSeed());
            clearValidations();
            clearResultAreas();
            updateRelationDisplay();
            setValidation(
                relationValidationLabel,
                "Loaded example: " + sample.name() + ".",
                false);
            setStatus("Example loaded", false);
        } catch (RuntimeException e) {
            updateRelationDisplay();
            setValidation(relationValidationLabel, e.getMessage(), true);
            setStatus("Example could not be loaded", true);
        }
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
            keysResultArea.setText(ResultFormatter.candidateKeysReport(
                candidateKeyService.findAllCandidateKeys(),
                candidateKeyService.getPrimeAttributes(),
                candidateKeyService.getNonPrimeAttributes()));
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
            normalFormResultArea.setText(ResultFormatter.normalFormReport(
                normalFormService.analyzeNormalForm()));
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
            minimalCoverResultArea.setText(ResultFormatter.renderLines(steps));
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
            var decomposition = bcnfDecompositionService.decomposeWithTrace();
            lastDecomposition = decomposition.relations();

            decompositionResultArea.setText(ResultFormatter.renderLines(decomposition.explanation()));
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
            var synthesis = threeNfService.synthesizeWithTrace();
            lastDecomposition = synthesis.relations();

            decompositionResultArea.setText(ResultFormatter.renderLines(synthesis.steps()));
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
                checkResultArea,
                checkResultTab);
            return;
        }

        try {
            checkResultArea.setText(ResultFormatter.decompositionCheckReport(
                decompositionAnalysisService
                    .checkDependencyPreservationWithSteps(lastDecomposition),
                decompositionAnalysisService
                    .checkLosslessJoinWithSteps(lastDecomposition)));
            selectTab(checkResultTab);
            setStatus("Decomposition analysis complete", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), checkResultArea, checkResultTab);
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

            explanationResultArea.setText(ResultFormatter.renderLines(explanation));
            selectTab(explanationTab);
            updateRelationDisplay();
            setStatus("Explanation refreshed", false);
        } catch (RuntimeException e) {
            showActionError(e.getMessage(), explanationResultArea, explanationTab);
        }
    }

    @FXML
    private void handleWindowedMode() {
        if (stage == null) {
            return;
        }

        stage.setFullScreen(false);
        stage.setMaximized(false);
        updateWindowModeButtons();
        setStatus("Windowed mode", false);
    }

    @FXML
    private void handleMaximizedMode() {
        if (stage == null) {
            return;
        }

        stage.setFullScreen(false);
        stage.setMaximized(true);
        updateWindowModeButtons();
        setStatus("Maximized mode", false);
    }

    @FXML
    private void handleFullscreenMode() {
        if (stage == null) {
            return;
        }

        stage.setFullScreen(true);
        updateWindowModeButtons();
        setStatus("Fullscreen mode", false);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void updateWindowModeButtons() {
        if (stage == null
                || windowedButton == null
                || maximizedButton == null
                || fullscreenButton == null) {
            return;
        }

        boolean fullscreen = stage.isFullScreen();
        boolean maximized = !fullscreen && stage.isMaximized();

        fullscreenButton.setSelected(fullscreen);
        maximizedButton.setSelected(maximized);
        windowedButton.setSelected(!fullscreen && !maximized);
    }

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

        // The list holds the FD objects themselves; cells show toString()
        fdListView.getItems().setAll(FdOrder.sorted(fdService.getAllDependencies()));
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
        attributeSummaryLabel.setText(ResultFormatter.formatSet(attributes));
        fdCountLabel.setText(ResultFormatter.formatCount(
            fds.size(), "dependency", "dependencies"));

        try {
            Set<CandidateKey> keys = candidateKeyService.findAllCandidateKeys();
            keySummaryLabel.setText(ResultFormatter.formatKeys(keys));
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

        decompositionSummaryLabel.setText(
            ResultFormatter.decompositionSummary(lastDecomposition));
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
            // The trace ends with the final closure, so one computation suffices
            var steps = closureService.computeClosureWithSteps(input);
            closureResultArea.setText(ResultFormatter.closureReport(input, steps));
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
        boolean hasMessage = message != null && !message.isBlank();
        label.setManaged(hasMessage);
        label.setVisible(hasMessage);
        label.getStyleClass().removeAll(VALIDATION_ERROR, VALIDATION_OK);
        if (hasMessage) {
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

    private void updateValidationVisibility() {
        updateValidationVisibility(relationValidationLabel);
        updateValidationVisibility(fdValidationLabel);
        updateValidationVisibility(closureValidationLabel);
    }

    private void updateValidationVisibility(Label label) {
        boolean hasMessage = label.getText() != null && !label.getText().isBlank();
        label.setManaged(hasMessage);
        label.setVisible(hasMessage);
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
        checkResultArea.clear();
        explanationResultArea.clear();
        selectTab(tutorialTab);
    }
}
