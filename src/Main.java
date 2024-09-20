import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import index.BStarTree;
import index.IndexStrategy;
import view.Menu;

public class Main {
  private static String dbPath = "./db/";
  private static List<IndexStrategy> indexes;

  public static void main(String[] args) {
    indexes = new ArrayList<IndexStrategy>();
    indexes.add(new BStarTree(dbPath));
    //indexes.add(new DynamicHash(dbPath));

    try {
      Database database = new Database(dbPath, indexes);

      new Menu(database);
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo não encontrado.");
      System.out.println(e);
    }

  }
}
