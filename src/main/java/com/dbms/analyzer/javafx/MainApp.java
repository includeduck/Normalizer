package com.dbms.analyzer.javafx;

import com.dbms.analyzer.DbmsApplication;
import com.dbms.analyzer.javafx.controllers.MainController;
import com.dbms.analyzer.javafx.utils.JavaFxSpringInjector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class MainApp extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(DbmsApplication.class)
            .headless(false)
            .web(WebApplicationType.NONE)
            .run(getParameters().getRaw().toArray(new String[0]));
        JavaFxSpringInjector.setApplicationContext(applicationContext);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass().getResource("/fxml/main.fxml"));
        fxmlLoader.setControllerFactory(applicationContext::getBean);

        Parent root = fxmlLoader.load();
        MainController controller = fxmlLoader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(root, 1280, 820);
        stage.setTitle("DBMS Normalization & Functional Dependency Analyzer");
        stage.setMinWidth(1080);
        stage.setMinHeight(720);
        stage.setFullScreenExitHint("Press Esc to leave fullscreen");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (applicationContext != null) {
            applicationContext.close();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
