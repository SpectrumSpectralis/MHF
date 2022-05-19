package org.mhf.mhf.logic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //Create FXMLLoader
        FXMLLoader loader = new FXMLLoader();
        //Path to FXML file
        String fxmlDocPath = "src/main/java/org/mhf/mhf/view/menu.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);

        AnchorPane root = loader.load(fxmlStream);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("A simple FXML example");
        stage.show();
    }

}
