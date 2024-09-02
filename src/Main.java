import java.io.FileNotFoundException;

import db.Database;
import view.Menu;

public class Main {
  private static String dbPath = "./db/dados";

  public static void main(String[] args) {
    try {
      Database database = new Database(dbPath);

      new Menu(database);
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo não encontrado.");
      System.out.println(e);
    }

  }
}
