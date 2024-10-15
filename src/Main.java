import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import index.InvertedList.InvertedList;
import index.bplustree.BPlusTree;
import index.extendedHash.ExtendedHash;
import model.Movie;
import model.interfaces.IIndex;
import view.Menu;

public class Main {
  private static String dbPath = "./db/";
  private static List<IIndex> indexes;

  public static void main(String[] args) {
    indexes = new ArrayList<IIndex>();
    try {
      indexes.add(new ExtendedHash(20, dbPath));
      indexes.add(new BPlusTree(3, dbPath));
      indexes.add(new InvertedList(dbPath, "title", Movie::getTitle));
      indexes.add(new InvertedList(dbPath, "description", Movie::getMovieInfo));
      //indexes.add(new InvertedList(dbPath, "description"));
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
