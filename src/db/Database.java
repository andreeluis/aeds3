package db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import index.Index;
import model.IIndexStrategy;
import model.Movie;
import sort.Sort;

// Estrutura do arquivo sequencial
// LAST_ID; [lapide1; tam1; id1; reg1]; [lapide2; tam2; id2; reg2];

public class Database {
  private static final String fileExtension = ".aeds3";
  private String filePath;
  private RandomAccessFile database;
  private int sortPathsNumber;
  private int sortInMemoryRegisters;
  private Index index;

  // fileExtension
  public static String getFileExtension() {
    return fileExtension;
  }

  // filePath
  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  // database
  public RandomAccessFile getDatabase() {
    return this.database;
  }

  public int getSortPathsNumber() {
    return sortPathsNumber;
  }

  // sortPathsNumber
  public void setSortPathsNumber(int sortPathsNumber) {
    if (sortPathsNumber > 0) {
      this.sortPathsNumber = sortPathsNumber;
    }
  }

  // sortInMemoryRegisters
  public int getSortInMemoryRegisters() {
    return sortInMemoryRegisters;
  }

  public void setSortInMemoryRegisters(int sortInMemoryRegisters) {
    if (sortInMemoryRegisters > 0) {
      this.sortInMemoryRegisters = sortInMemoryRegisters;
    }
  }

  // constructor
  public Database(String filePath, List<IIndexStrategy> indexes) throws FileNotFoundException {
    setFilePath(filePath);

    String dbFilePath = this.filePath + "dados" + fileExtension;
    this.database = new RandomAccessFile(dbFilePath, "rw");

    try {
      if (database.length() == 0) {
        database.writeInt(-1);
        database.seek(0);
      }
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de banco de dados.");
      System.out.println(e);
    }

    this.index = new Index(this, indexes);
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

      // save positon to insert and go to end of file
      long position = database.length();
      database.seek(position);

      // write register
      byte[] byteArrayMovie = movie.toByteArray();
      database.writeBoolean(false); // tombstone
      database.writeInt(byteArrayMovie.length); // registerLength
      database.write(byteArrayMovie); // register

      // add to indexes
      if (index.isAvailabe()) {
        index.add(lastId, position);
      }
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

      // early return
      if (id > lastId) {
        return null;
      }

      if (index.isAvailabe()) {
        // if has index availabe, get position from index
        long position = index.get(id);

        if (position != -1) {
          database.seek(position);

          database.readBoolean(); // skip tombstone (always false)
          int registerLength = database.readInt();

          byte[] byteArrayMovie = new byte[registerLength];
          database.read(byteArrayMovie);
          movie = new Movie(byteArrayMovie);

          found = movie.getId() == id;
        }
      } else {
        // if has no index perform linear search
        while (!found && !isEndOfFile()) {
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

      // early return
      if (id > lastId) {
        return null;
      }

      // find register position and lenght
      long position = -1;
      int registerLength = -1;
      boolean found = false;
      if (index.isAvailabe()) {
        // if has index availabe, get position from index
        position = index.get(id);

        if (position != -1) {
          database.seek(position);

          database.readBoolean(); // skip tombstone (always false)
          registerLength = database.readInt();

          byte[] byteArrayMovie = new byte[registerLength];
          database.read(byteArrayMovie);

          found = new Movie(byteArrayMovie).getId() == id;
        }
      } else {
        while (!found && !isEndOfFile()) {
          // if has no index perform linear search
          position = database.getFilePointer();
          boolean tombstone = database.readBoolean();
          registerLength = database.readInt();

          if (!tombstone) {
            byte[] byteArrayMovie = new byte[registerLength];
            database.read(byteArrayMovie);

            found = new Movie(byteArrayMovie).getId() == id;
          } else {
            database.skipBytes(registerLength);
          }
        }
      }

      // write new register
      if (found) {
        byte[] newByteArrayMovie = newMovie.toByteArray();
        int newLength = newByteArrayMovie.length;

        // go to register position
        database.seek(position);

        if (newLength > registerLength) {
          // set tombstone to true and go to end of file
          database.writeBoolean(true);
          long newPosition = database.length();
          database.seek(newPosition);

          // write new register at end of file
          database.writeBoolean(false); // tombstone
          database.writeInt(newByteArrayMovie.length); // registerLength
          database.write(newByteArrayMovie); // register

          index.update(id, newPosition);
        } else {
          database.writeBoolean(false); // tombstone
          database.writeInt(registerLength); // registerLength
          database.write(newByteArrayMovie); // register
        }

        updated = true;
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

      // early return
      if (id > lastId) {
        return null;
      }

      if (index.isAvailabe()) {
        // if has index availabe, get position from index
        long position = index.get(id);

        if (position != -1) {
          database.seek(position);

          database.readBoolean(); // skip tombstone (always false)
          int registerLength = database.readInt();

          byte[] byteArrayMovie = new byte[registerLength];
          database.read(byteArrayMovie);

          movie = new Movie(byteArrayMovie);

          if (movie.getId() == id) {
            database.seek(position);
            database.writeBoolean(true);

            index.remove(id);

            deleted = true;
          }
        }
      } else {
        // if has no index perform linear search
        while (!deleted && !isEndOfFile()) {
          long position = database.getFilePointer();
          boolean tombstone = database.readBoolean();
          int registerLength = database.readInt();

          if (!tombstone) {
            byte[] byteArrayMovie = new byte[registerLength];
            database.read(byteArrayMovie);

            movie = new Movie(byteArrayMovie);

            if (movie.getId() == id) {
              database.seek(position);
              database.writeBoolean(true);

              deleted = true;
            }
          } else {
            database.skipBytes(registerLength);
          }
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao excluir registro.");
      System.out.println(e);
    }

    return deleted ? movie : null;
  }

  public boolean sortRegisters() {
    try {
      Sort sort = new Sort(this);
      int segments;

      do {
        segments = sort.distribution();

        sort.intercalation();
      } while (segments > sortPathsNumber); // if a path is completed sorted, the next intercalation will result one sorted segment

      index.rebuild();

      return true;
    } catch (IOException e) {
      System.out.println("Erro ao ordenar registros.");
      return false;
    }
  }

  public boolean isEndOfFile() throws IOException {
    return !(database.getFilePointer() < database.length());
  }

  /**
   * Clean registers and keep lastId
   */
  public void cleanDatabaseRegisters() throws IOException {
    // reads the lastId
    database.seek(0);
    int lastId = database.readInt();

    database.setLength(0);
    database.writeInt(lastId);

    index.clear();
  }
}
