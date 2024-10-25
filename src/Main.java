import controller.DatabaseControler;
import db.Database;
import model.Movie;
import model.MovieMenuFactory;
import view.Menu;

public class Main {
  private static String dbPath = "./db/";

  public static void main(String[] args) throws Exception {
    Database<Movie> movieDB = new Database<Movie>(dbPath, Movie.class.getConstructor());
    
    DatabaseControler<Movie> movieDBControler = new DatabaseControler<Movie>(movieDB, Movie.class.getConstructor());

    new Menu<Movie>(movieDBControler, new MovieMenuFactory());
  }
}
