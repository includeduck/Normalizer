package com.dbms.analyzer.javafx.controllers;

import com.dbms.analyzer.algorithm.FdUtil;
import com.dbms.analyzer.model.FunctionalDependency;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Smoke test of the real window: loads main.fxml with the Spring context
 * and clicks through a session. Needs a display, so it is skipped on
 * headless machines (such as CI).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MainControllerUiTest {

    private static boolean fxAvailable;

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, Object> ui;

    @BeforeAll
    static void startJavaFx() {
        boolean linuxWithoutDisplay = System.getProperty("os.name").startsWith("Linux")
            && (System.getenv("DISPLAY") == null || System.getenv("DISPLAY").isBlank());
        if (linuxWithoutDisplay) {
            return;
        }
        try {
            CountDownLatch started = new CountDownLatch(1);
            Platform.startup(started::countDown);
            fxAvailable = started.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            fxAvailable = true;
        } catch (Throwable noDisplay) {
            fxAvailable = false;
        }
        if (fxAvailable) {
            Platform.setImplicitExit(false);
        }
    }

    @AfterAll
    static void stopJavaFx() {
        if (fxAvailable) {
            Platform.exit();
        }
    }

    @Test
    void fullSessionThroughTheUi() throws Exception {
        assumeTrue(fxAvailable, "JavaFX needs a display; skipped on headless machines");

        onFx(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            loader.load();
            ui = loader.getNamespace();
            return null;
        });

        onFx(() -> {
            button("clearAllButton").fire();

            text("relationInputField").setText("R(A,B,C,D)");
            button("createRelationButton").fire();
            assertEquals("R(A,B,C,D)", label("relationLabel").getText());

            addFd("A -> B");
            addFd("B -> C");
            addFd("A -> B");
            assertTrue(label("fdValidationLabel").getText().startsWith("Duplicate FD"),
                label("fdValidationLabel").getText());

            // BUG-015: the list holds the FD objects themselves
            ListView<FunctionalDependency> fds = fdList();
            assertEquals(List.of(FdUtil.parseFD("A -> B"), FdUtil.parseFD("B -> C")),
                fds.getItems());
            fds.getSelectionModel().select(0);
            button("removeFdButton").fire();
            assertEquals(List.of(FdUtil.parseFD("B -> C")), fds.getItems());

            addFd("A -> B");
            button("bcnfButton").fire();
            assertTrue(area("decompositionResultArea").getText()
                .contains("Final BCNF relations:"));

            // BUG-017: the check gets its own tab and leaves Explanation alone
            button("explanationButton").fire();
            String explanation = area("explanationResultArea").getText();
            button("checkDecompositionButton").fire();
            String check = area("checkResultArea").getText();
            assertTrue(check.contains("=== Dependency Preservation Check ==="), check);
            assertTrue(check.contains("=== Lossless Join Check ==="), check);
            assertSame(ui.get("checkResultTab"),
                ((TabPane) ui.get("resultTabPane")).getSelectionModel().getSelectedItem());
            assertEquals(explanation, area("explanationResultArea").getText());

            @SuppressWarnings("unchecked")
            ComboBox<String> samples = (ComboBox<String>) ui.get("sampleComboBox");
            samples.getSelectionModel().select("Library Loans");
            button("loadSampleButton").fire();
            assertTrue(label("relationLabel").getText().startsWith("Loan("));
            assertEquals(3, fdList().getItems().size());

            button("candidateKeysButton").fire();
            assertTrue(area("keysResultArea").getText()
                .contains("- {isbn,loan_date,member_id}"), area("keysResultArea").getText());

            button("normalFormButton").fire();
            assertTrue(area("normalFormResultArea").getText().contains("1NF: assumed"));
            return null;
        });
    }

    private void addFd(String fd) {
        text("fdInputField").setText(fd);
        button("addFdButton").fire();
    }

    private Button button(String id) {
        return (Button) ui.get(id);
    }

    private TextField text(String id) {
        return (TextField) ui.get(id);
    }

    private Label label(String id) {
        return (Label) ui.get(id);
    }

    private TextArea area(String id) {
        return (TextArea) ui.get(id);
    }

    @SuppressWarnings("unchecked")
    private ListView<FunctionalDependency> fdList() {
        return (ListView<FunctionalDependency>) ui.get("fdListView");
    }

    /** Runs {@code action} on the JavaFX thread and rethrows its failure here. */
    private static <T> T onFx(Callable<T> action) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(action.call());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        try {
            return result.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw (Exception) e.getCause();
        }
    }
}
