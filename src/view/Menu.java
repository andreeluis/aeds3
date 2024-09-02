package view;

import java.io.File;
import java.io.FileNotFoundException;
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
    // limpa o terminal
    System.out.print("\033[H\033[2J");
    System.out.flush();

    // mostra as opções disponiveis
    System.out.println("Gerenciador de filmes");
    System.out.println("  1 - Carregar filmes de um .csv");
    System.out.println("  2 - Visualizar filme");
    System.out.println("  3 - Atualizar filme");
    System.out.println("  4 - Excluir filme");
    System.out.println("  5 - Ordenar (e limpar) registros");

    System.out.println("  0 - Sair");

    System.out.print("Selecione a opção desejada: ");
  }

  private void menu() {
    int op;

    do {
      showMenu();
      op = sc.nextInt();
      sc.nextLine(); // limpa o buffer

      switch (op) {
        case 1:
          readFromCSV();
          break;
        case 2:
          findMovie();
          break;
        case 3:
          updateMovie();
          break;
        case 4:
          deleteMovie();
          break;
        case 5:
          sortRegisters();
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

  private void findMovie() {
    System.out.print("Qual ID do filme buscado? ");
    int id = sc.nextInt();
    sc.nextLine(); // Limpa o buffer

    Movie movie = database.read(id);

    if (movie != null) {
      System.out.println(movie);
    } else {
      System.out.println("O filme não foi encontrado.");
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
    database.sortRegisters();
  }
}
