package db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import model.Movie;

public class Database {
  // Estrutura do arquivo sequencial
  // LAST_ID; [lapide1; tam1; id1; reg1]; [lapide2; tam2; id2; reg2];
  private static String fileExtension = ".aeds3";
  private static String defaultDBPath = "./db/dados" + fileExtension;

  public static void create(Movie movie) {
    try {
      FileOutputStream file = new FileOutputStream(defaultDBPath, true);
      DataOutputStream database = new DataOutputStream(file);

      byte[] byteArrayMovie = movie.toByteArray();
      database.writeBoolean(false);
      database.writeInt(byteArrayMovie.length);
      database.write(byteArrayMovie);

      database.close();
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de banco de dados.");
    }
  }

  public static Movie read(int id) {
    Movie movie = null;

    try {
      FileInputStream database = new FileInputStream(defaultDBPath);
      DataInputStream data = new DataInputStream(database);

      // Testar last int (TODO)
      //int lastId = data.readInt();
      int lastId = Movie.getLastId();
      if (lastId <= id) {
        do {
          boolean lapide = data.readBoolean();
          int len = data.readInt();

          if (!lapide) {
            byte[] byteArrayMovie = new byte[len];
            data.read(byteArrayMovie);

            movie = new Movie(byteArrayMovie);
          } else {
            data.skip(len);
          }
        } while (movie.getId() != id && database.available() > 0);
      }

      data.close();
    } catch (IOException e) {
      System.out.println(e);
    }

    return movie;
  }

  public static Movie update(int id, Movie movie) {
    // TODO
    return movie;
  }

  // public static boolean delete(int id) {

  // }
}
