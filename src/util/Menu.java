package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import db.Database;
import model.Movie;

public class Menu {
  Scanner sc = new Scanner(System.in);

  public Menu() {
    menu();
  }

  private void showMenu() {
    // limpa o terminal
    System.out.print("\033[H\033[2J");
    System.out.flush();

    // mostra as opções disponiveis
    System.out.println("| Gerenciador de filmes\n|");
    System.out.println("|   1 - Carregar filmes de um .csv");
    System.out.println("|   2 - Visualizar filme");
    System.out.println("|   3 - Atualizar filme");
    System.out.println("|   4 - Excluir filme");

    System.out.println("|   0 - Sair");

    System.out.print("| Selecione a opção desejada: ");
  }

  private void menu() {
    showMenu();
    int op = sc.nextInt();
    sc.nextLine(); // limpa o buffer

    switch (op) {
      case 0:
        return;
      case 1:
        readFromCSV();
        break;
      case 2:

        break;
      case 3:

        break;
      case 4:

        break;
      default:
        menu();
        break;
    }
  }

  private void readFromCSV() {
    String defaultCSVPath = "../dataset/movies.csv";

    System.out.print("Qual caminho do arquivo? ");
    String path = sc.nextLine();
    if (path == "") {
      path = defaultCSVPath;
    }

    // Leitura do CSV
    try {
      File csvFile = new File(path);
      Scanner csvScanner = new Scanner(csvFile);

      csvScanner.nextLine(); // skip csv header
      while (csvScanner.hasNextLine()) {
        // Cria objeto
        Movie movie = new Movie(csvScanner.nextLine());
        // Escrita
        Database.create(movie);
      }

      csvScanner.close();
    } catch (FileNotFoundException e) {
      System.out.println("Erro ao abrir o arquivo CSV.");
      sc.nextLine(); // Espera um enter
      menu();
    }
  }

  // private void updateMovie() {
  // System.out.print("Qual o ID do filme a ser alterado? ");
  // int id = sc.nextInt();
  // Movie movie = Database.read(id);

  // if (movie == null) {
  // System.out.println("Filme não encontrado para alteração.");
  // sc.nextLine(); // Espera um enter
  // return;
  // }

  // // Recebe novos dados
  // System.out.println("Editando filme ID[" + movie.getId() + "]");

  // System.out.print("Nome do filme: ");
  // movie.setTitle(sc.nextLine());

  // System.out.print("Resumo: ");
  // movie.setMovieInfo(sc.nextLine());

  // System.out.print("Ano de lançamento: ");
  // movie.setYear(ParseUtil.parseInt(sc.nextLine()));

  // System.out.print("Distribuidor: ");
  // movie.setDistributor(sc.nextLine());

  // System.out.print("Valor: ");
  // movie.setBudget(ParseUtil.parseInt(sc.nextLine()));

  // System.out.print("Domestic Opening: ");
  // movie.setDomesticOpening(ParseUtil.parseInt(sc.nextLine()));

  // System.out.print("Domestic Sales: ");
  // movie.setDomesticSales(ParseUtil.parseInt(sc.nextLine()));

  // System.out.print("International Sales: ");
  // movie.setInternationalSales(ParseUtil.parseInt(sc.nextLine()));

  // System.out.print("World Wide Sales: ");
  // movie.setWorldWideSales(ParseUtil.parseInt(sc.nextLine()));

  // System.out.print("Release Date: ");
  // movie.setReleaseDate(ParseUtil.parseInt(sc.nextLine()));

  // System.out.print("Gênero: ");
  // movie.setGenre(sc.nextLine().split(","));

  // System.out.print("Duração: ");
  // movie.setRunningTime(sc.nextLine());

  // System.out.print("Licença: ");
  // movie.setLicense(sc.nextLine());

  // // atualiza o filme
  // //movie = Database.update(id, movie);

  // menu();
  // }

  // private void deleteMovie() {
  // System.out.print("Qual o ID do filme a ser excluído? ");
  // int id = sc.nextInt();

  // if (Database.delete(id))
  // System.out.println("Filme excluído com sucesso!");
  // else
  // System.out.println("Erro ao excluir filme.");

  // sc.nextLine(); // Espera um enter
  // menu();
  // }
}
