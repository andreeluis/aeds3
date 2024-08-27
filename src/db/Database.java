package db;

import java.io.IOException;
import java.io.RandomAccessFile;

import model.Movie;

public class Database {
  // Estrutura do arquivo sequencial
  // LAST_ID; [lapide1; tam1; id1; reg1]; [lapide2; tam2; id2; reg2];
  private static String fileExtension = ".aeds3";
  private static String defaultDBPath = "./db/dados" + fileExtension;

  public static void create(Movie movie) {
    try {
      RandomAccessFile data = new RandomAccessFile(defaultDBPath, "rw");

      // criação do arquivo
      if (data.length() == 0) {
        data.writeInt(-1);
        data.seek(0);
      }

      // le o lastId e atribui id ao filme
      int lastId = data.readInt();
      movie.setId(++lastId);

      // atualiza o lastId
      data.seek(0);
      data.writeInt(lastId);

      // escreve o filme no final do arquivo
      data.seek(data.length());

      byte[] byteArrayMovie = movie.toByteArray();
      data.writeBoolean(false); // lapide
      data.writeInt(byteArrayMovie.length); // tam registro
      data.write(byteArrayMovie); // registro

      data.close();
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de banco de dados. " + e);
    }
  }

  public static Movie read(int id) {
    Movie movie = null;
    boolean found = false;

    try {
      RandomAccessFile data = new RandomAccessFile(defaultDBPath, "r");

      // lastId
      int lastId = data.readInt();

      if (id <= lastId) {
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
      System.out.println("Erro ao buscar registro. " + e);
    }

    return found ? movie : null;
  }

  public static Movie update(int id, Movie movie) {
    boolean updated = false;

    try {
      byte[] newByteArrayMovie = movie.toByteArray();
      int newLen = newByteArrayMovie.length;

      RandomAccessFile data = new RandomAccessFile(defaultDBPath, "rw");

      // lastId
      int lastId = data.readInt();

      if (id <= lastId) {
        do {
          // salva a posição da lapide
          long lapidePosition = data.getFilePointer();

          boolean lapide = data.readBoolean();
          int len = data.readInt();

          if (!lapide) {
            byte[] byteArrayMovie = new byte[len];
            data.read(byteArrayMovie);

            Movie newMovie = new Movie(byteArrayMovie);

            if (newMovie.getId() == id) {
              // posiciona na lapide
              data.seek(lapidePosition);

              // caso o novo registro seja maior
              if (newLen > len) {
                data.writeBoolean(true);

                // move para o final do arquivo
                data.seek(data.length());

                data.writeBoolean(false); // lapide
                data.writeInt(newByteArrayMovie.length); // tam registro
                data.write(newByteArrayMovie); // registro
              } else {
                data.writeBoolean(false); // lapide
                data.writeInt(len); // tam registro
                data.write(newByteArrayMovie); // registro
              }

              updated = true;
            }
          } else {
            data.skipBytes(len);
          }
        } while (!updated && data.getFilePointer() < data.length());
      }

      data.close();
    } catch (IOException e) {
      System.out.println("Erro ao atualizar registro. " + e);
    }

    return updated ? movie : null;
  }

  public static Movie delete(int id) {
    boolean deleted = false;
    Movie movie = null;

    try {
      RandomAccessFile data = new RandomAccessFile(defaultDBPath, "rw");

      // lastId
      int lastId = data.readInt();

      if (id <= lastId) {
        do {
          // salva a posição da lapide
          long lapidePosition = data.getFilePointer();

          boolean lapide = data.readBoolean();
          int len = data.readInt();

          if (!lapide) {
            byte[] byteArrayMovie = new byte[len];
            data.read(byteArrayMovie);

            movie = new Movie(byteArrayMovie);

            if (movie.getId() == id) {
              data.seek(lapidePosition);
              data.writeBoolean(true);
              deleted = true;
            }
          } else {
            data.skipBytes(len);
          }
        } while (!deleted && data.getFilePointer() < data.length());
      }

      data.close();
    } catch (IOException e) {
      System.out.println("Erro ao excluir registro. " + e);
    }

    return deleted ? movie : null;
  }
}
