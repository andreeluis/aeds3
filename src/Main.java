import java.io.FileNotFoundException;

import db.Database;
import sort.Sort;
import view.Menu;

public class Main {
  private static String dbPath = "./db/dados";
  private static int pathsNumber = 2;
  private static int inMemoryRegisters = 4;

  public static void main(String[] args) {
    try {
      Sort sort = new Sort(pathsNumber, inMemoryRegisters);
      Database database = new Database(dbPath, sort);

      new Menu(database);
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo não encontrado.");
      System.out.println(e);
    }

  }
}
