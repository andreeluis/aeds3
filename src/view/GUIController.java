package view;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import controller.AppController;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
      if (file != null) {
        dbControler.readFromCSV(file.getPath());
        fileChooserLabel.setText("Arquivo carregado com sucesso");
        showMenu(); // Chama o método que troca a tela
      }
    });

    fileChooserButton.setCursor(javafx.scene.Cursor.HAND);
    fileChooserButton.getStyleClass().add("file-chooser-button");
    fileChooserLabel.getStyleClass().add("file-chooser-label");
  }

  /**
   * Atualiza a tela inicial para exibir as opções do menu.
   */
  private void showMenu() {
    // Limpa os elementos da tela inicial
    main.getChildren().clear();

    // Cria os botões do menu
    Button loadCsvButton = new Button("1 - Carregar novos registros do CSV");
    Button addRegisterButton = new Button("2 - Adicionar registro");
    Button searchRegisterButton = new Button("3 - Buscar registros");
    Button updateRegisterButton = new Button("4 - Atualizar registro");
    Button deleteRegisterButton = new Button("5 - Excluir registro");
    Button sortRegistersButton = new Button("6 - Ordenar registros");
    Button configureIndexesButton = new Button("7 - Configurar índices");
    Button exitButton = new Button("0 - Sair");

    // Configura eventos para cada botão
    loadCsvButton.setOnAction(e -> loadCsv());
    addRegisterButton.setOnAction(e -> addRegister());
    searchRegisterButton.setOnAction(e -> searchRegister());
    updateRegisterButton.setOnAction(e -> updateRegister());
    deleteRegisterButton.setOnAction(e -> deleteRegister());
    sortRegistersButton.setOnAction(e -> sortRegisters());
    configureIndexesButton.setOnAction(e -> configureIndexes());
    exitButton.setOnAction(e -> System.exit(0));

    // Adiciona os botões ao layout
    VBox menuBox = new VBox(10, loadCsvButton, addRegisterButton, searchRegisterButton,
        updateRegisterButton, deleteRegisterButton, sortRegistersButton, configureIndexesButton, exitButton);
    menuBox.setAlignment(Pos.CENTER);

    main.getChildren().add(menuBox);
  }

  /**
   * Métodos para cada ação do menu (a serem implementados ou conectados à lógica
   * existente).
   */
  private void loadCsv() {
    // Lógica para carregar um novo arquivo CSV
    System.out.println("Carregar novos registros do CSV");
  }

  private void addRegister() {
    // Lógica para adicionar um novo registro
    System.out.println("Adicionar novo registro");
  }

  private void searchRegister() {
    // Lógica para buscar registros
    showSearchScreen();
  }

  private void updateRegister() {
    // Lógica para atualizar um registro
    System.out.println("Atualizar registro");
  }

  private void deleteRegister() {
    // Lógica para excluir um registro
    System.out.println("Excluir registro");
  }

  private void sortRegisters() {
    // Lógica para ordenar registros
    System.out.println("Ordenar registros");
  }

  private void configureIndexes() {
    // Lógica para configurar índices
    System.out.println("Configurar índices");
  }

  private void showSearchScreen() {
    // Limpa a tela atual
    main.getChildren().clear();

    // Criar rótulo de instrução
    Label instructionLabel = new Label("Selecione o tipo de busca e insira o termo:");
    instructionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

    // Botões para escolher o tipo de busca
    ToggleGroup searchTypeGroup = new ToggleGroup();
    RadioButton searchById = new RadioButton("Buscar por ID");
    RadioButton searchByName = new RadioButton("Buscar por Nome");
    RadioButton searchByDescription = new RadioButton("Buscar por Descrição");

    searchById.setToggleGroup(searchTypeGroup);
    searchByName.setToggleGroup(searchTypeGroup);
    searchByDescription.setToggleGroup(searchTypeGroup);

    // Campo de entrada para o termo de busca
    TextField searchField = new TextField();
    searchField.setPromptText("Digite o termo de busca...");
    searchField.setPrefWidth(300);

    // Botão para realizar a busca
    Button searchButton = new Button("Buscar");
    searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
    searchButton.setCursor(javafx.scene.Cursor.HAND);
    Button backButton = new Button("Voltar");

    // Contêiner para organizar os componentes
    VBox searchBox = new VBox(10, instructionLabel, searchById, searchByName, searchByDescription, searchField,
        searchButton, backButton);
    searchBox.setAlignment(Pos.CENTER);

    // Adicionar evento ao botão de busca
    searchButton.setOnAction(event -> {
      RadioButton selectedOption = (RadioButton) searchTypeGroup.getSelectedToggle();
      if (selectedOption == null) {
        showError("Por favor, selecione um tipo de busca.");
        return;
      }

      String searchTerm = searchField.getText().trim();
      if (searchTerm.isEmpty()) {
        showError("Por favor, insira o termo de busca.");
        return;
      }

      List<T> registers = new ArrayList<>();

      if (selectedOption == searchById) {
        dbControler.searchById(Integer.parseInt(searchTerm)).ifPresent(registers::add);
      } else if (selectedOption == searchByName) {
        dbControler
            .searchByFields(new String[] { "Title" }, new String[] { searchTerm })
            .ifPresent(registers::addAll);
      } else if (selectedOption == searchByDescription) {
        dbControler
            .searchByFields(new String[] { "Description" }, new String[] { searchTerm })
            .ifPresent(registers::addAll);
      }
      if (!registers.isEmpty()) {
        System.out.println(entityName + "s encontrados(as):");
        for (T register : registers) {
          System.out.println(register);
        }
      } else {
        System.out.println("Nenhum(a) " + entityName.toLowerCase() + " foi encontrado(a).");
      }
    });

    backButton.setOnAction(event -> showMenu());

    // Adicionar à tela principal
    main.getChildren().add(searchBox);
  }

  /**
   * Exibe uma mensagem de erro em um alerta.
   */
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Erro");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

}
