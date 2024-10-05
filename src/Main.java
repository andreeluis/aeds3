import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import index.ExtendedHash;
import index.IndexStrategy;
import index.btree.BTree;
import view.Menu;

public class Main {
  private static String dbPath = "./db/";
  private static List<IndexStrategy> indexes;

  public static void main(String[] args) {
    indexes = new ArrayList<IndexStrategy>();
    try {
      indexes.add(new BTree(3, dbPath));
      indexes.add(new ExtendedHash(dbPath));
    } catch (IOException e) {}

    try {
      Database database = new Database(dbPath, indexes);

      new Menu(database);
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo não encontrado.");
      System.out.println(e);
    }
  }
}
