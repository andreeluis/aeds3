import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import model.interfaces.IIndexStrategy;
import index.extendedHash.ExtendedHash;
import index.bplustree.BPlusTree;
import view.Menu;

public class Main {
  private static String dbPath = "./db/";
  private static List<IIndexStrategy> indexes;

  public static void main(String[] args) {
    indexes = new ArrayList<IIndexStrategy>();
    try {
      indexes.add(new ExtendedHash(20, dbPath));
      indexes.add(new BPlusTree(3, dbPath));
    } catch (Exception e) {
      System.out.println("Erro ao criar índices.");
      System.out.println(e);
    }

    try {
      Database database = new Database(dbPath, indexes);

      new Menu(database);
    } catch (FileNotFoundException e) {
      System.out.println("Arquivo não encontrado.");
      System.out.println(e);
    }
  }
}
