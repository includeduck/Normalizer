package com.dbms.analyzer.javafx.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Last-resort handler for errors that escape the controllers (which only
 * catch {@link RuntimeException}), such as {@link OutOfMemoryError}.
 * Logs the error and tells the user instead of failing silently, which
 * matters for the packaged .exe where there is no console.
 */
public final class UnexpectedErrorHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UnexpectedErrorHandler.class);

    private final AtomicBoolean dialogOpen = new AtomicBoolean();

    /** Installs this handler as the default for all threads. */
    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(new UnexpectedErrorHandler());
    }

    @Override
    public void uncaughtException(Thread thread, Throwable error) {
        LOG.error("Uncaught error on thread {}", thread.getName(), error);
        try {
            // Deferred so the dialog never opens during layout or animation processing
            Platform.runLater(() -> showDialog(error));
        } catch (Throwable dialogFailure) {
            LOG.error("Could not schedule error dialog", dialogFailure);
        }
    }

    private void showDialog(Throwable error) {
        if (!dialogOpen.compareAndSet(false, true)) {
            return;
        }
        try {
            showErrorAlert("Unexpected error",
                "The last action could not be completed.", error);
        } catch (Throwable dialogFailure) {
            LOG.error("Could not show error dialog", dialogFailure);
        } finally {
            dialogOpen.set(false);
        }
    }

    /**
     * Shows a blocking dialog for a failure during application start-up.
     * Must be called on the JavaFX Application Thread.
     */
    public static void showStartupFailure(Throwable error) {
        LOG.error("Application failed to start", error);
        try {
            showErrorAlert("Startup failed",
                "The application could not start.", error);
        } catch (Throwable dialogFailure) {
            LOG.error("Could not show startup error dialog", dialogFailure);
        }
    }

    /** User-facing one-line description of an error. */
    public static String describe(Throwable error) {
        if (error instanceof OutOfMemoryError) {
            return "The application ran out of memory. "
                + "Try a smaller relation or fewer dependencies.";
        }
        if (error instanceof StackOverflowError) {
            return "The computation was too deep to complete. "
                + "Try a smaller relation.";
        }
        String message = error.getMessage();
        return error.getClass().getSimpleName()
            + (message == null || message.isBlank() ? "" : ": " + message);
    }

    private static void showErrorAlert(String title, String header, Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(describe(error));

        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        TextArea details = new TextArea(stackTrace.toString());
        details.setEditable(false);
        details.setWrapText(false);
        alert.getDialogPane().setExpandableContent(details);

        alert.showAndWait();
    }
}
