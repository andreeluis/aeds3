package db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import model.Movie;
import sort.Sort;

// Estrutura do arquivo sequencial
// LAST_ID; [lapide1; tam1; id1; reg1]; [lapide2; tam2; id2; reg2];

public class Database {
  private static final String fileExtension = ".aeds3";
  private String filePath;

  private RandomAccessFile database;
  private Sort sort;

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public Database(String filePath, Sort sort) throws FileNotFoundException {
    setFilePath(filePath);

    String dbFilePath = this.filePath + fileExtension;
    this.database = new RandomAccessFile(dbFilePath, "rw");

    this.sort = sort;

    try {
      if (database.length() == 0) {
        database.writeInt(-1);
        database.seek(0);
      }
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de banco de dados.");
      System.out.println(e);
    }
  }

  public void create(Movie movie) {
    try {
      // reads the lastId and assigns it to the new register
      database.seek(0);
      int lastId = database.readInt();
      movie.setId(++lastId);

      // updates lastId
      database.seek(0);
      database.writeInt(lastId);

      // go to end of file
      database.seek(database.length());

      // write register
      byte[] byteArrayMovie = movie.toByteArray();
      database.writeBoolean(false); // tombstone
      database.writeInt(byteArrayMovie.length); // registerLength
      database.write(byteArrayMovie); // register
    } catch (IOException e) {
      System.out.println("Erro ao escrever novo registro.");
      System.out.println(e);
    }
  }

  public Movie read(int id) {
    Movie movie = null;
    boolean found = false;

    try {
      // reads the lastId
      database.seek(0);
      int lastId = database.readInt();

      while (!found && !isEndOfFile() && id <= lastId) {
        boolean tombstone = database.readBoolean();
        int registerLength = database.readInt();

        if (!tombstone) {
          byte[] byteArrayMovie = new byte[registerLength];
          database.read(byteArrayMovie);
          movie = new Movie(byteArrayMovie);

          found = movie.getId() == id;
        } else {
          database.skipBytes(registerLength);
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao buscar registro.");
      System.out.println(e);
    }

    return found ? movie : null;
  }

  public Movie update(int id, Movie newMovie) {
    boolean updated = false;

    try {
      // reads the lastId
      database.seek(0);
      int lastId = database.readInt();

      while (!updated && !isEndOfFile() && id <= lastId) {
        // save tombstone position
        long tombstonePosition = database.getFilePointer();

        boolean tombstone = database.readBoolean();
        int length = database.readInt();

        if (!tombstone) {
          byte[] byteArrayMovie = new byte[length];
          database.read(byteArrayMovie);

          Movie movie = new Movie(byteArrayMovie);
          if (movie.getId() == id) {
            byte[] newByteArrayMovie = newMovie.toByteArray();
            int newLength = newByteArrayMovie.length;

            // go to tombstonePosition
            database.seek(tombstonePosition);

            if (newLength > length) {
              // set tombstone to true and go to end of file
              database.writeBoolean(true);
              database.seek(database.length());

              // write new register at end of file
              database.writeBoolean(false); // tombstone
              database.writeInt(newByteArrayMovie.length); // registerLength
              database.write(newByteArrayMovie); // register
            } else {
              database.writeBoolean(false); // tombstone
              database.writeInt(length); // registerLength
              database.write(newByteArrayMovie); // register
            }

            updated = true;
          }
        } else {
          database.skipBytes(length);
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao atualizar registro.");
      System.out.println(e);
    }

    return updated ? newMovie : null;
  }

  public Movie delete(int id) {
    Movie movie = null;
    boolean deleted = false;

    try {
      // reads the lastId
      database.seek(0);
      int lastId = database.readInt();

      while (!deleted && !isEndOfFile() && id <= lastId) {
        // save tombstone position
        long tombstonePosition = database.getFilePointer();

        boolean tombstone = database.readBoolean();
        int registerLength = database.readInt();

        if (!tombstone) {
          byte[] byteArrayMovie = new byte[registerLength];
          database.read(byteArrayMovie);

          movie = new Movie(byteArrayMovie);

          if (movie.getId() == id) {
            // go to tombstonePosition and set it to true
            database.seek(tombstonePosition);
            database.writeBoolean(true);

            deleted = true;
          }
        } else {
          database.skipBytes(registerLength);
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao excluir registro.");
      System.out.println(e);
    }

    return deleted ? movie : null;
  }

  public void sortRegisters() {
    try {
      // skips the lastId
      database.seek(0);
      database.readInt();

      this.sort.createTmpFiles(filePath, fileExtension);
      this.sort.distribution(database);

    } catch (IOException e) {
      System.out.println("Erro ao ordenar registros.");
      System.out.println(e);
    }
  }

  private boolean isEndOfFile() throws IOException {
    return !(database.getFilePointer() < database.length());
  }
}
