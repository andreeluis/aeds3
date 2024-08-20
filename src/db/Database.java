package db;

import java.io.FileOutputStream;
import java.io.IOException;

import model.Movie;

public class Database {
  private static String fileExtension = ".aeds3";
  private static String defaultDBPath = "./db/dados" + fileExtension;

  public static void create(Movie movie) {
    try {
      FileOutputStream database = new FileOutputStream(defaultDBPath, true);
      database.write(movie.toByteArray());

      database.close();
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de banco de dados.");
    }

  }

  // public static Movie read(int id) {

  // }

  // public static Movie update(int id, Movie movie) {

  // }

  // public static boolean delete(int id) {

  // }
}
