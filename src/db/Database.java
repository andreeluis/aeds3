package db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import model.Movie;

public class Database {
  // Estrutura do arquivo sequencial
  // LAST_ID; [lapide1; tam1; id1; reg1]; [lapide2; tam2; id2; reg2];
  private static String fileExtension = ".aeds3";
  private static String defaultDBPath = "./db/dados" + fileExtension;

  public static void create(Movie movie) {
    RandomAccessFile data = null;
    try {
      data = new RandomAccessFile(defaultDBPath, "rw");
      // write last id
      data.seek(0);
      data.writeInt(Movie.getLastId());
      data.seek(data.length());

      byte[] byteArrayMovie = movie.toByteArray();
      data.writeBoolean(false);
      data.writeInt(byteArrayMovie.length);
      data.write(byteArrayMovie);

      data.close();
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de banco de dados.");
    }
  }

  public static Movie read(int id) {
    Movie movie = null;
    boolean found = false;

    try {
      RandomAccessFile data = new RandomAccessFile(defaultDBPath, "r");

      // Testar last int (TODO)
      int lastId = data.readInt();
      // int lastId = Movie.getLastId();
      if (lastId > id) {
        do {
          boolean lapide = data.readBoolean();
          int len = data.readInt();

          if (!lapide) {
            byte[] byteArrayMovie = new byte[len];
            data.read(byteArrayMovie);

            movie = new Movie(byteArrayMovie);
            if (movie.getId() == id) {
              found = true;
            }
          } else {
            data.skipBytes(len);
          }
        } while (!found && data.getFilePointer() < data.length());
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

  public static boolean delete(int id) {
    Movie movie = null;
    RandomAccessFile data = null;

    try {
      data = new RandomAccessFile(defaultDBPath, "rw");

      int lastId = data.readInt();
      if (lastId > id) {
        while (data.getFilePointer() < data.length()) {
          long lapidePosition = data.getFilePointer();
          boolean lapide = data.readBoolean();
          int len = data.readInt();
          System.out.println(len);
          if (!lapide) {
            byte[] byteArrayMovie = new byte[len];
            data.read(byteArrayMovie);

            movie = new Movie(byteArrayMovie);
            System.out.println(movie);

            if (movie.getId() == id) {
              data.seek(lapidePosition);
              data.writeBoolean(true);
              return true;
            }
          } else {
            data.skipBytes(len);
          }
        }
      }
    } catch (IOException e) {
      System.out.println(e);
      return false;
    } finally {
      // Fecha o RandomAccessFile, garantindo a liberação de recursos
      if (data != null) {
        try {
          data.close();
        } catch (IOException e) {
          System.out.println("Erro ao fechar o arquivo: " + e);
        }
      }
    }

    return false;
  }
}
