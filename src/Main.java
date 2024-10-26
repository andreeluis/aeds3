import java.io.FileNotFoundException;

import db.DatabaseControler;
import model.Movie;
import model.MovieMenuFactory;
import view.Menu;

public class Main {
  private static String dbPath = "./db/";

  public static void main(String[] args) {
    DatabaseControler<Movie> movieDBControler;
    try {
      movieDBControler = new DatabaseControler<>(dbPath, Movie.class.getConstructor());

      new Menu<Movie>(movieDBControler, new MovieMenuFactory());

    } catch (FileNotFoundException e) {
      System.out.println("Erro ao criar ao criar o arquivo de banco de dados.");
      e.printStackTrace();
    } catch (NoSuchMethodException | SecurityException e) {
      System.out.println("Erro ao usar o construtor da classe.");
      e.printStackTrace();
    }
  }
}
