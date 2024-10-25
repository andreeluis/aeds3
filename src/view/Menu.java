package view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import controller.DatabaseControler;
import model.Register;
import model.interfaces.IMenuFactory;

public class Menu<T extends Register> {
  private static Scanner scanner = new Scanner(System.in);
  private DatabaseControler<T> dbControler;
  private IMenuFactory<T> menuFactory;
  private String entityName;

  public Menu(DatabaseControler<T> dbControler, IMenuFactory<T> menuFactory) {
    this.dbControler = dbControler;
    this.menuFactory = menuFactory;
    this.entityName = menuFactory.getEntityName();

    menu();
  }

  private void showMenu() {
    clearTerminal();

    System.out.println("Gerenciador de " + this.entityName + "s");
    System.out.println("  1 - Carregar " + this.entityName.toLowerCase() + "s de um .csv");
    System.out.println("  2 - Adicionar novo(a) " + this.entityName.toLowerCase());
    System.out.println("  3 - Buscar por " + this.entityName.toLowerCase() + "s");
    System.out.println("  4 - Atualizar um(a) " + this.entityName.toLowerCase());
    System.out.println("  5 - Excluir um(a) " + this.entityName.toLowerCase());
    System.out.println("  6 - Ordenar (e limpar) registros");
    System.out.println("  7 - Configurar índices");

    System.out.println("  0 - Sair");

    System.out.print("Selecione a opção desejada: ");
  }

  private void menu() {
    int option;

    do {
      showMenu();
      option = scanner.nextInt();
      scanner.nextLine(); // limpa o buffer

      clearTerminal();

      switch (option) {
        case 1:
          readFromCSV();
          break;
        case 2:
          createRegister();
          break;
        case 3:
          searchRegister();
          break;
        case 4:
          editRegister();
          break;
        case 5:
          deleteRegister();
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

      scanner.nextLine(); // Espera um enter
    } while (option != 0);
  }

  private void readFromCSV() {
    System.out.print("Qual caminho do arquivo a ser lido? ");
    String path = scanner.nextLine();
    path = path.isBlank() ? "../dataset/movies.csv" : path;

    dbControler.readFromCSV(path);
  }

  private void createRegister() {
    T register = menuFactory.createRegister(scanner);
    dbControler.insertRegister(register);
  }

  private void searchRegister() {
    int option = menuFactory.searchRegister(scanner);
    scanner.nextLine(); // Limpa o buffer

    List<T> registers = new ArrayList<>();
    String title, description;
    switch (option) {
      case 1:
        System.out.print("Qual o ID do(a) " + entityName.toLowerCase() + "? ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpa o buffer

        dbControler.searchById(id)
          .ifPresent(registers::add);

        break;
      case 2:
        System.out.print("Qual o título do(a) " + entityName.toLowerCase() + "? ");
        title = scanner.nextLine();

        dbControler.searchByFields(new String[]{"title"}, new String[]{title})
          .forEach(opt -> opt.ifPresent(registers::add));

        break;
      case 3:
        System.out.print("Qual a descrição do(a) " + entityName.toLowerCase() + "? ");
        description = scanner.nextLine();

        dbControler.searchByFields(new String[]{"description"}, new String[]{description})
          .forEach(opt -> opt.ifPresent(registers::add));

        break;
      case 4:
        System.out.print("Qual o título do(a) " + entityName.toLowerCase() + "? ");
        title = scanner.nextLine();

        System.out.print("Qual a descrição do(a) " + entityName.toLowerCase() + "? ");
        description = scanner.nextLine();

        dbControler.searchByFields(new String[]{"title", "description"}, new String[]{title, description})
          .forEach(opt -> opt.ifPresent(registers::add));

        break;
      default:
        break;
    }

    if (!registers.isEmpty()) {
      System.out.println(entityName + "s encontrados(as):");
      for (T register : registers) {
        System.out.println(register);
      }
    } else {
      System.out.println("Nenhum(a)" + entityName.toLowerCase() + " foi encontrado(a).");
    }
  }

  private void editRegister() {
    System.out.print("Qual o ID do(a) " + entityName.toLowerCase() + " a ser alterado(a)? ");
    int id = scanner.nextInt();
    scanner.nextLine(); // Limpa o buffer

    Optional<T> register = dbControler.searchById(id);

    if (register.isPresent()) {
      System.out.println("Editando " + entityName.toLowerCase() + " " + register.get() + ":");

      T newRegister = menuFactory.editRegister(scanner, register.get());
      dbControler.updateRegister(id, newRegister);

      System.out.println(entityName+ " " + newRegister + " atualizado(a) com sucesso!");
    } else {
      System.out.println(entityName + " não encontrado(a) para alteração.");
    }
  }

  private void deleteRegister() {
    System.out.print("Qual o ID do(a) " + entityName.toLowerCase() + " a ser excluído(a)? ");
    int id = scanner.nextInt();
    scanner.nextLine(); // Limpa o buffer

    Optional<T> register = dbControler.deleteRegister(id);

    if (register.isPresent()) {
      System.out.println(entityName + register.get() + " excluído(a) com sucesso!");
    } else {
      System.out.println(entityName + " não encontrado(a) para exclusão.");
    }
  }

  private void sortRegisters() {
    // System.out.print("Quantos caminhos (arquivos temporários) para a ordenação?
    // ");
    // database.setSortPathsNumber(ParseUtil.parseInt(sc.nextLine()));

    // System.out.print("Quantos registros em memória primária para a ordenação? ");
    // database.setSortInMemoryRegisters(ParseUtil.parseInt(sc.nextLine()));

    // if (database.sortRegisters()) {
    // System.out.println("Registros ordenados com sucesso!");
    // } else {
    // System.out.println("Os registros não foram ordenados.");
    // }
  }

  private void index() {
    System.out.println("Qual índice deseja configurar?");
    System.out.println("  1 - Definir índice padrão");
    System.out.println("  2 - Árvore B+");
    System.out.println("  3 - Hash Extensível");
    System.out.print("Selecione a opção desejada: ");
    int op = scanner.nextInt();
    scanner.nextLine(); // Limpa o buffer

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
