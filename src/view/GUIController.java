package view;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import controller.AppController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.CompressionStats;
import model.Movie;
import model.Register;
import util.ParseUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class GUIController<T extends Register> implements Initializable {
  private AppController<T> dbControler;
  private String entityName;
  private String filePath;

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
    // Button loadCsvButton = new Button("1 - Carregar novos registros do CSV");
    // Button addRegisterButton = new Button("2 - Adicionar registro");
    Button searchRegisterButton = new Button("1 - Buscar registros");
    Button updateRegisterButton = new Button("2 - Atualizar registro");
    Button deleteRegisterButton = new Button("3 - Excluir registro");
    Button sortRegistersButton = new Button("4 - Ordenar registros");
    Button compressFileButton = new Button("5 - Comprimir arquivo");
    Button decompressFileButton = new Button("6 - Descomprimir arquivo");
    // Button configureIndexesButton = new Button("7 - Configurar índices");
    Button exitButton = new Button("0 - Sair");

    // Configura eventos para cada botão
    // loadCsvButton.setOnAction(_ -> loadCsv());
    // addRegisterButton.setOnAction(_ -> addRegister());
    searchRegisterButton.setOnAction(_ -> searchRegister());
    updateRegisterButton.setOnAction(_ -> updateRegister());
    deleteRegisterButton.setOnAction(_ -> deleteRegister());
    sortRegistersButton.setOnAction(_ -> sortRegisters());
    compressFileButton.setOnAction(_ -> compressFile());
    decompressFileButton.setOnAction(_ -> decompressFile());
    // configureIndexesButton.setOnAction(_ -> configureIndexes());
    exitButton.setOnAction(_ -> System.exit(0));

    // Adiciona os botões ao layout
    VBox menuBox = new VBox(10, searchRegisterButton,
        updateRegisterButton, deleteRegisterButton, sortRegistersButton, exitButton, compressFileButton,
        decompressFileButton);
    menuBox.setAlignment(Pos.CENTER);

    main.getChildren().add(menuBox);
  }

  /**
   * Métodos para cada ação do menu (a serem implementados ou conectados à lógica
   * existente).
   */
  // private void loadCsv() {
  // // Lógica para carregar um novo arquivo CSV
  // System.out.println("Carregar novos registros do CSV");
  // }

  // private void addRegister() {
  // // Lógica para adicionar um novo registro
  // System.out.println("Adicionar novo registro");
  // }

  private void searchRegister() {
    // Lógica para buscar registros
    showSearchScreen();
  }

  private void updateRegister() {
    // Limpa a tela atual
    main.getChildren().clear();

    // Título da página
    Label titleLabel = new Label("Atualizar Registro");
    titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

    // Campo para inserir o ID
    TextField idField = new TextField();
    idField.setPromptText("Digite o ID do registro a ser atualizado");
    idField.setPrefWidth(300);

    // Botão para buscar o registro
    Button searchButton = new Button("Buscar Registro");
    searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
    searchButton.setCursor(javafx.scene.Cursor.HAND);

    VBox initialBox = new VBox(10, titleLabel, idField, searchButton);
    initialBox.setAlignment(Pos.CENTER);

    main.getChildren().add(initialBox);

    searchButton.setOnAction(_ -> {
      String idText = idField.getText().trim();
      if (idText.isEmpty()) {
        showError("Por favor, insira o ID do registro.");
        return;
      }

      try {
        int id = Integer.parseInt(idText);
        Optional<T> register = dbControler.searchById(id);

        if (register.isPresent()) {
          showEditScreen(id, (Movie) register.get());
        } else {
          showError("Registro com ID " + id + " não encontrado.");
        }
      } catch (NumberFormatException e) {
        showError("Por favor, insira um ID válido.");
      }
    });
  }

  private void showEditScreen(int id, Movie currentMovie) {
    // Limpa a tela atual
    main.getChildren().clear();

    // Título da página
    Label titleLabel = new Label("Editando Filme ID: " + id);
    titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

    // Campos para editar os dados do filme
    TextField titleField = new TextField(currentMovie.getTitle());
    titleField.setPromptText("Título (Atual: " + currentMovie.getTitle() + ")");

    TextField movieInfoField = new TextField(currentMovie.getMovieInfo());
    movieInfoField.setPromptText("Resumo (Atual: " + currentMovie.getMovieInfo() + ")");

    TextField yearField = new TextField(String.valueOf(currentMovie.getYear()));
    yearField.setPromptText("Ano de lançamento (Atual: " + currentMovie.getYear() + ")");

    TextField distributorField = new TextField(currentMovie.getDistributor());
    distributorField.setPromptText("Distribuidor (Atual: " + currentMovie.getDistributor() + ")");

    TextField budgetField = new TextField(String.valueOf(currentMovie.getBudget()));
    budgetField.setPromptText("Orçamento (Atual: " + currentMovie.getBudget() + ")");

    TextField domesticOpeningField = new TextField(String.valueOf(currentMovie.getDomesticOpening()));
    domesticOpeningField.setPromptText("Domestic Opening (Atual: " + currentMovie.getDomesticOpening() + ")");

    TextField domesticSalesField = new TextField(String.valueOf(currentMovie.getDomesticSales()));
    domesticSalesField.setPromptText("Domestic Sales (Atual: " + currentMovie.getDomesticSales() + ")");

    TextField internationalSalesField = new TextField(String.valueOf(currentMovie.getInternationalSales()));
    internationalSalesField.setPromptText("International Sales (Atual: " + currentMovie.getInternationalSales() + ")");

    TextField worldWideSalesField = new TextField(String.valueOf(currentMovie.getWorldWideSales()));
    worldWideSalesField.setPromptText("World Wide Sales (Atual: " + currentMovie.getWorldWideSales() + ")");

    TextField releaseDateField = new TextField(String.valueOf(currentMovie.getReleaseDate()));
    releaseDateField.setPromptText("Data de lançamento (Atual: " + currentMovie.getReleaseDate() + ")");

    TextField genreField = new TextField(String.join(",", currentMovie.getGenre()));
    genreField.setPromptText("Gênero (Atual: " + String.join(",", currentMovie.getGenre()) + ")");

    TextField runningTimeField = new TextField(currentMovie.getRunningTime());
    runningTimeField.setPromptText("Duração (Atual: " + currentMovie.getRunningTime() + ")");

    TextField licenseField = new TextField(currentMovie.getLicense());
    licenseField.setPromptText("Licença (Atual: " + currentMovie.getLicense() + ")");

    // Botão para salvar as alterações
    Button saveButton = new Button("Salvar Alterações");
    saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
    saveButton.setCursor(javafx.scene.Cursor.HAND);

    // Botão para voltar
    Button backButton = new Button("Voltar");
    backButton.setOnAction(_ -> showMenu());

    VBox editBox = new VBox(10, titleLabel, titleField, movieInfoField, yearField, distributorField,
        budgetField, domesticOpeningField, domesticSalesField, internationalSalesField, worldWideSalesField,
        releaseDateField, genreField, runningTimeField, licenseField, saveButton, backButton);
    editBox.setAlignment(Pos.CENTER);

    main.getChildren().add(editBox);

    saveButton.setOnAction(event -> {
      // Criação de um novo objeto Movie com os dados editados
      Movie newMovie = currentMovie;

      if (!titleField.getText().trim().isEmpty()) {
        newMovie.setTitle(titleField.getText().trim());
      }
      if (!movieInfoField.getText().trim().isEmpty()) {
        newMovie.setMovieInfo(movieInfoField.getText().trim());
      }
      if (!yearField.getText().trim().isEmpty()) {
        newMovie.setYear(ParseUtil.parseInt(yearField.getText().trim()));
      }
      if (!distributorField.getText().trim().isEmpty()) {
        newMovie.setDistributor(distributorField.getText().trim());
      }
      if (!budgetField.getText().trim().isEmpty()) {
        newMovie.setBudget(ParseUtil.parseInt(budgetField.getText().trim()));
      }
      if (!domesticOpeningField.getText().trim().isEmpty()) {
        newMovie.setDomesticOpening(ParseUtil.parseInt(domesticOpeningField.getText().trim()));
      }
      if (!domesticSalesField.getText().trim().isEmpty()) {
        newMovie.setDomesticSales(ParseUtil.parseInt(domesticSalesField.getText().trim()));
      }
      if (!internationalSalesField.getText().trim().isEmpty()) {
        newMovie.setInternationalSales(ParseUtil.parseInt(internationalSalesField.getText().trim()));
      }
      if (!worldWideSalesField.getText().trim().isEmpty()) {
        newMovie.setWorldWideSales(ParseUtil.parseInt(worldWideSalesField.getText().trim()));
      }
      if (!releaseDateField.getText().trim().isEmpty()) {
        newMovie.setReleaseDate(ParseUtil.parseLong(releaseDateField.getText().trim()));
      }
      if (!genreField.getText().trim().isEmpty()) {
        newMovie.setGenre(genreField.getText().split(","));
      }
      if (!runningTimeField.getText().trim().isEmpty()) {
        newMovie.setRunningTime(runningTimeField.getText().trim());
      }
      if (!licenseField.getText().trim().isEmpty()) {
        newMovie.setLicense(licenseField.getText().trim());
      }

      // Atualiza o filme no controlador
      dbControler.updateRegister(id, (T) newMovie);

      showInfo("Filme atualizado com sucesso!");
      showMenu();
    });
  }

  private void deleteRegister() {
    // Criação do modal para perguntar o ID do registro a ser deletado
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Excluir Registro");
    alert.setHeaderText("Digite o ID do registro que deseja excluir:");

    // Adiciona um campo de texto ao modal
    TextField idField = new TextField();
    idField.setPromptText("ID do registro");

    VBox content = new VBox(10, new Label("ID:"), idField);
    content.setAlignment(Pos.CENTER);

    alert.getDialogPane().setContent(content);

    // Configuração dos botões do modal
    Button deleteButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    deleteButton.setText("Excluir");

    alert.setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return ButtonType.OK;
      }
      return null;
    });

    // Mostrar o modal e capturar o resultado
    alert.showAndWait().ifPresent(buttonType -> {
      if (buttonType == ButtonType.OK) {
        String idText = idField.getText();
        try {
          int id = Integer.parseInt(idText.trim());
          Optional<T> result = dbControler.deleteRegister(id);
          if (result.isPresent()) {
            showInfo("Registro excluído com sucesso.");
          } else {
            showError("Registro com ID " + id + " não encontrado.");
          }
        } catch (NumberFormatException e) {
          showError("Por favor, insira um ID válido.");
        }
      }
    });
  }

  private void sortRegisters() {
    // Criação do modal para perguntar os parâmetros
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Ordenar Registros");
    alert.setHeaderText("Configurar parâmetros de ordenação:");

    // Campos de entrada para os parâmetros
    TextField pathsField = new TextField();
    pathsField.setPromptText("Número de caminhos");

    TextField recordsField = new TextField();
    recordsField.setPromptText("Número de registros em memória primária");

    VBox content = new VBox(10, new Label("Número de caminhos:"), pathsField,
        new Label("Número de registros em memória primária:"), recordsField);
    content.setAlignment(Pos.CENTER);

    alert.getDialogPane().setContent(content);

    // Configuração dos botões do modal
    Button sortButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    sortButton.setText("Ordenar");

    final String[] inputs = new String[2];

    alert.setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        inputs[0] = pathsField.getText();
        inputs[1] = recordsField.getText();
        return ButtonType.OK;
      }
      return null;
    });

    // Mostrar o modal e capturar o resultado
    alert.showAndWait().ifPresent(_ -> {
      try {
        int numberOfPaths = Integer.parseInt(inputs[0].trim());
        int recordsInMemory = Integer.parseInt(inputs[1].trim());

        // Chama o método de ordenação no controlador
        dbControler.sort(numberOfPaths, recordsInMemory);

        // Exibe mensagem de sucesso
        showInfo("Registros ordenados com sucesso.");
      } catch (NumberFormatException e) {
        showError("Por favor, insira valores numéricos válidos para os parâmetros.");
      } catch (Exception e) {
        showError("Ocorreu um erro ao ordenar os registros: " + e.getMessage());
      }
    });
  }

  // private void configureIndexes() {
  // // Lógica para configurar índices
  // System.out.println("Configurar índices");
  // }

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

    // Botões de ação
    Button searchButton = new Button("Buscar");
    searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
    Button backButton = new Button("Voltar");

    // TableView para exibir os resultados
    TableView<T> resultsTable = new TableView<>();
    resultsTable.setPlaceholder(new Label("Nenhum resultado encontrado."));
    resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    // Colunas da tabela
    TableColumn<T, String> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

    TableColumn<T, String> nameColumn = new TableColumn<>("Nome");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

    TableColumn<T, String> descriptionColumn = new TableColumn<>("Descrição");
    descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("movieInfo"));

    resultsTable.getColumns().addAll(Arrays.asList(idColumn, nameColumn, descriptionColumn));

    // Expandir a tabela para ocupar o espaço disponível
    VBox.setVgrow(resultsTable, Priority.ALWAYS);

    // Contêiner para os botões de ação
    HBox buttonBox = new HBox(10, searchButton, backButton);
    buttonBox.setAlignment(Pos.CENTER);

    // Contêiner para os elementos de busca
    VBox searchBox = new VBox(10, searchField, buttonBox);
    searchBox.setAlignment(Pos.CENTER);
    searchBox.setMaxWidth(600);

    // Contêiner principal
    VBox searchScreenBox = new VBox(20, instructionLabel, searchById, searchByName, searchByDescription, searchBox,
        resultsTable);
    searchScreenBox.setPadding(new Insets(20));
    searchScreenBox.setAlignment(Pos.TOP_LEFT);
    VBox.setVgrow(searchScreenBox, Priority.ALWAYS);
    VBox.setVgrow(resultsTable, Priority.ALWAYS);

    // Evento do botão de busca
    searchButton.setOnAction(_ -> {
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

      try {
        if (selectedOption == searchById) {
          dbControler.searchById(Integer.parseInt(searchTerm)).ifPresent(registers::add);
        } else if (selectedOption == searchByName) {
          dbControler.searchByFields(new String[] { "Title" }, new String[] { searchTerm })
              .ifPresent(registers::addAll);
        } else if (selectedOption == searchByDescription) {
          dbControler.searchByFields(new String[] { "Description" }, new String[] { searchTerm })
              .ifPresent(registers::addAll);
        }

        // Limpar e adicionar os resultados corretamente
        resultsTable.getItems().clear();
        if (!registers.isEmpty()) {
          resultsTable.getItems().addAll(registers);
        } else {
          resultsTable.setPlaceholder(new Label("Nenhum(a) " + entityName.toLowerCase() + " foi encontrado(a)."));
        }
      } catch (Exception e) {
        showError("Erro ao realizar a busca: " + e.getMessage());
      }
    });

    backButton.setOnAction(_ -> showMenu());

    // Adicionar à tela principal
    main.getChildren().add(searchScreenBox);

    // Expandir para ocupar toda a tela principal
    VBox.setVgrow(searchScreenBox, Priority.ALWAYS);
  }

  private void compressFile() {

  }

  private void decompressFile() {

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

  /**
   * Exibe uma mensagem informativa em um alerta.
   */
  private void showInfo(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Informação");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

}
