package view;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import db.Database;
import model.Movie;
import util.ParseUtil;

public class Menu {
  private static Scanner sc = new Scanner(System.in);
  private Database database;

  public Menu(Database database) {
    this.database = database;
    menu();
  }

  private static void showMenu() {
    clearTerminal();

    // mostra as opções disponiveis
    System.out.println("Gerenciador de filmes");
    System.out.println("  1 - Carregar filmes de um .csv");
    System.out.println("  2 - Adicionar novo filme");
    System.out.println("  3 - Buscar por filmes");
    System.out.println("  4 - Atualizar um filme");
    System.out.println("  5 - Excluir um filme");
    System.out.println("  6 - Ordenar (e limpar) registros");
    System.out.println("  7 - Configurar índices");

    System.out.println("  0 - Sair");

    System.out.print("Selecione a opção desejada: ");
  }

  private void menu() {
    int op;

    do {
      showMenu();
      op = sc.nextInt();
      sc.nextLine(); // limpa o buffer

      clearTerminal();

      switch (op) {
        case 1:
          readFromCSV();
          break;
        case 2:
          addMovie();
          break;
        case 3:
          findMovie();
          break;
        case 4:
          updateMovie();
          break;
        case 5:
          deleteMovie();
          break;
        case 6:
          sortRegisters();
          break;
        case 7:
          index();
          break;
        case 0:
          return;
        default:
          break;
      }

      sc.nextLine(); // Espera um enter
    } while (op != 0);
  }

  private void readFromCSV() {
    System.out.print("Qual caminho do arquivo? ");
    String path = sc.nextLine();
    path = path.isBlank() ? "../dataset/movies.csv" : path;

    // Leitura do CSV
    try {
      File csvFile = new File(path);
      Scanner csvScanner = new Scanner(csvFile);

      csvScanner.nextLine(); // skip csv header
      while (csvScanner.hasNextLine()) {
        // Cria objeto
        Movie movie = new Movie(csvScanner.nextLine());

        // Escrita
        database.create(movie);
      }

      csvScanner.close();
      System.out.println("Dados carregados com sucesso.");
    } catch (FileNotFoundException e) {
      System.out.println("Erro ao abrir o arquivo CSV.");
    }
  }

  private void addMovie() {
    Movie movie = new Movie();

    // Recebe os dados
    System.out.print("  - Nome do filme: ");
    movie.setTitle(sc.nextLine());

    System.out.print("  - Resumo: ");
    movie.setMovieInfo(sc.nextLine());

    System.out.print("  - Ano de lançamento: ");
    movie.setYear(ParseUtil.parseInt(sc.nextLine()));

    System.out.print("  - Distribuidor: ");
    movie.setDistributor(sc.nextLine());

    System.out.print("  - Orçamento: ");
    movie.setBudget(ParseUtil.parseInt(sc.nextLine()));

    System.out.print("  - Domestic Opening: ");
    movie.setDomesticOpening(ParseUtil.parseInt(sc.nextLine()));

    System.out.print("  - Domestic Sales: ");
    movie.setDomesticSales(ParseUtil.parseInt(sc.nextLine()));

    System.out.print("  - International Sales: ");
    movie.setInternationalSales(ParseUtil.parseInt(sc.nextLine()));

    System.out.print("  - World Wide Sales: ");
    movie.setWorldWideSales(ParseUtil.parseInt(sc.nextLine()));

    System.out.print("  - Data de lançamento: ");
    movie.setReleaseDate(ParseUtil.parseLong(sc.nextLine()));

    System.out.print("  - Gênero: ");
    movie.setGenre(sc.nextLine().split(","));

    System.out.print("  - Duração: ");
    movie.setRunningTime(sc.nextLine());

    System.out.print("  - Licença: ");
    movie.setLicense(sc.nextLine());

    // atualiza o filme
    database.create(movie);
  }

  private void findMovie() {
    System.out.println("Como você quer buscar pelo filme? ");
    System.out.println("  1 - Por ID");
    System.out.println("  2 - Por título");
    System.out.println("  3 - Por descrição");
    System.out.println("Selecione a opção desejada:");

    int op = sc.nextInt();
    sc.nextLine(); // Limpa o buffer

    List<Movie> movies = new ArrayList<>();
    switch (op) {
      case 1:
        System.out.print("Qual o ID do filme? ");
        int id = sc.nextInt();
        sc.nextLine(); // Limpa o buffer
        movies.add(database.read(id));
        break;
      case 2:
        //System.out.print("Qual o título do filme? ");
        //String title = sc.nextLine();
        //movies = database.read(title, "title");
        break;
      case 3:
        //System.out.print("Qual a descrição do filme? ");
        //String description = sc.nextLine();
        //movies = database.read(description, "description");
        break;
      default:
        break;
    }

    if (!movies.isEmpty() && movies.get(0) != null) {
      System.out.println("Filmes encontrados:");
      for (Movie movie : movies) {
        System.out.println(movie);
      }
    } else {
      System.out.println("Nenhum filme foi encontrado.");
    }
  }

  private void updateMovie() {
    System.out.print("Qual o ID do filme a ser alterado? ");
    int id = sc.nextInt();
    sc.nextLine(); // Limpa o buffer
    Movie movie = database.read(id);

    if (movie != null) {
      // Recebe novos dados
      System.out.println("Editando filme " + movie + ":");

      System.out.print("  - Nome do filme: ");
      movie.setTitle(sc.nextLine());

      System.out.print("  - Resumo: ");
      movie.setMovieInfo(sc.nextLine());

      System.out.print("  - Ano de lançamento: ");
      movie.setYear(ParseUtil.parseInt(sc.nextLine()));

      System.out.print("  - Distribuidor: ");
      movie.setDistributor(sc.nextLine());

      System.out.print("  - Orçamento: ");
      movie.setBudget(ParseUtil.parseInt(sc.nextLine()));

      System.out.print("  - Domestic Opening: ");
      movie.setDomesticOpening(ParseUtil.parseInt(sc.nextLine()));

      System.out.print("  - Domestic Sales: ");
      movie.setDomesticSales(ParseUtil.parseInt(sc.nextLine()));

      System.out.print("  - International Sales: ");
      movie.setInternationalSales(ParseUtil.parseInt(sc.nextLine()));

      System.out.print("  - World Wide Sales: ");
      movie.setWorldWideSales(ParseUtil.parseInt(sc.nextLine()));

      System.out.print("  - Data de lançamento: ");
      movie.setReleaseDate(ParseUtil.parseLong(sc.nextLine()));

      System.out.print("  - Gênero: ");
      movie.setGenre(sc.nextLine().split(","));

      System.out.print("  - Duração: ");
      movie.setRunningTime(sc.nextLine());

      System.out.print("  - Licença: ");
      movie.setLicense(sc.nextLine());

      // atualiza o filme
      movie = database.update(id, movie);

      if (movie != null) {
        System.out.println("Filme " + movie + " atualizado com sucesso!");
      } else {
        System.out.println("O filme não foi atualizado.");
      }
    } else {
      System.out.println("Filme não encontrado para alteração.");
    }
  }

  private void deleteMovie() {
    System.out.print("Qual o ID do filme a ser excluído? ");
    int id = sc.nextInt();
    sc.nextLine(); // Limpa o buffer

    Movie movie = database.delete(id);

    if (movie != null) {
      System.out.println("Filme " + movie + " excluído com sucesso!");
    } else {
      System.out.println("O filme não foi excluído.");
    }
  }

  private void sortRegisters() {
    System.out.print("Quantos caminhos (arquivos temporários) para a ordenação? ");
    database.setSortPathsNumber(ParseUtil.parseInt(sc.nextLine()));

    System.out.print("Quantos registros em memória primária para a ordenação? ");
    database.setSortInMemoryRegisters(ParseUtil.parseInt(sc.nextLine()));

    if (database.sortRegisters()) {
      System.out.println("Registros ordenados com sucesso!");
    } else {
      System.out.println("Os registros não foram ordenados.");
    }
  }

  private void index() {
    System.out.println("Qual índice deseja configurar?");
    System.out.println("  1 - Definir índice padrão");
    System.out.println("  2 - Árvore B+");
    System.out.println("  3 - Hash Extensível");
    System.out.print("Selecione a opção desejada: ");
    int op = sc.nextInt();
    sc.nextLine(); // Limpa o buffer

    switch (op) {
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      default:
        break;
    }
  }

  private static void clearTerminal() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }
}
