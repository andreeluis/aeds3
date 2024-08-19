package util;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
    String defaultCSVPath = "../dataset/imdb_top_1000.csv";

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

        // Escrita (TODO)

      }

      csvScanner.close();
    } catch (FileNotFoundException e) {
      System.out.println("Erro ao abrir o arquivo CSV.");
      sc.nextLine(); // Espera um enter
      menu();
    }
  }
}
