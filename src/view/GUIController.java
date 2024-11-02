package view;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import controller.AppController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.Register;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class GUIController<T extends Register> implements Initializable {
  private AppController<T> dbControler;
  private String entityName;

  public GUIController(AppController<T> dbControler, String entityName) {
    this.dbControler = dbControler;
    this.entityName = entityName;
  }

  @FXML
  FlowPane root;

  @FXML
  VBox main;

  @FXML
  Label fileChooserLabel;

  @FXML
  Button fileChooserButton;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    root.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
    root.getStyleClass().add("root");
    main.setAlignment(Pos.CENTER);

    fileChooserLabel.setText("Nenhuma base encontrada, carregue um arquivo CSV para começar");
    fileChooserButton.setText("Carregar arquivo CSV");

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(entityName);
    fileChooserButton.setOnAction(_ -> {
      File file = fileChooser.showOpenDialog(null);
      dbControler.readFromCSV(file.getPath());
      fileChooserLabel.setText("Arquivo carregado com sucesso");
    });
    fileChooserButton.setCursor(javafx.scene.Cursor.HAND);
    fileChooserButton.getStyleClass().add("file-chooser-button");
    fileChooserLabel.getStyleClass().add("file-chooser-label");
  }
}
