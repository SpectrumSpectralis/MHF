package org.mhf.mhf.logic;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MenuController {
    @FXML
    private Button tobiButton;

    @FXML
    private Button teoButton;

    public MenuController(){

    }

    @FXML
    public void initialize(){
    }

    @FXML
    private void huntTobi(ActionEvent event) throws IOException {
        HuntController.monsterToHunt="TobiKadachi";
        loadHunt(event);
    }

    @FXML
    private void huntTeo(ActionEvent event) throws IOException {
        HuntController.monsterToHunt="Teostra";
        loadHunt(event);
    }

    private void loadHunt(ActionEvent event) throws IOException {
        String fxmlDocPath = "src/main/java/org/mhf/mhf/view/hunt.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
        FXMLLoader loader = new FXMLLoader();
        AnchorPane huntWindow = loader.load(fxmlStream);
        Stage mainWindow; //Here is the magic. We get the reference to main Stage.
        mainWindow = (Stage)  ((Node)event.getSource()).getScene().getWindow();
        Scene huntScene = new Scene(huntWindow);
        mainWindow.setScene(huntScene);
    }
}
