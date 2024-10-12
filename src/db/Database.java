package db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import index.Index;
import model.interfaces.IIndex;
import model.Movie;
import sort.Sort;

// Estrutura do arquivo sequencial
// LAST_ID; [lapide1; tam1; id1; reg1]; [lapide2; tam2; id2; reg2];

public class Database {
  private static final String fileExtension = ".aeds3";
  private String filePath;
  private RandomAccessFile file;
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

  // file
  public RandomAccessFile getFile() {
    return this.file;
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
  public Database(String filePath, List<IIndex> indexes) throws FileNotFoundException {
    setFilePath(filePath);

    String dbFilePath = this.filePath + "dados" + fileExtension;
    this.file = new RandomAccessFile(dbFilePath, "rw");

    try {
      if (file.length() == 0) {
        file.writeInt(-1);
        file.seek(0);
      }
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de banco de dados.");
      System.out.println(e);
    }

    try {
      this.index = new Index(this, indexes);
    } catch (IOException e) {
      System.out.println("Erro ao criar o índice.");
      System.out.println(e);
    }
  }

  public void create(Movie movie) {
    try {
      // reads the lastId and assigns it to the new register
      file.seek(0);
      int lastId = file.readInt();
      movie.setId(++lastId);

      // updates lastId
      file.seek(0);
      file.writeInt(lastId);

      // save positon to insert and go to end of file
      long position = file.length();
      file.seek(position);

      // write register
      byte[] byteArrayMovie = movie.toByteArray();
      file.writeBoolean(false); // tombstone
      file.writeInt(byteArrayMovie.length); // registerLength
      file.write(byteArrayMovie); // register

      // add to indexes
      if (index.isAvailabe()) {
        index.add(movie, position);
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
      file.seek(0);
      int lastId = file.readInt();

      // early return
      if (id > lastId) {
        return null;
      }

      if (index.isAvailabe()) {
        // if has index availabe, get position from index
        long position = index.get(id);

        if (position != -1) {
          file.seek(position);

          file.readBoolean(); // skip tombstone (always false)
          int registerLength = file.readInt();

          byte[] byteArrayMovie = new byte[registerLength];
          file.read(byteArrayMovie);
          movie = new Movie(byteArrayMovie);

          found = movie.getId() == id;
        }
      } else {
        // if has no index perform linear search
        while (!found && !isEndOfFile()) {
          boolean tombstone = file.readBoolean();
          int registerLength = file.readInt();

          if (!tombstone) {
            byte[] byteArrayMovie = new byte[registerLength];
            file.read(byteArrayMovie);
            movie = new Movie(byteArrayMovie);

            found = movie.getId() == id;
          } else {
            file.skipBytes(registerLength);
          }
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao buscar registro.");
      System.out.println(e);
    }

    return found ? movie : null;
  }

  public List<Movie> searchByField(String searchString, String field) {
    List<Integer> ids = new ArrayList<>();
    List<Movie> movies = new ArrayList<>();
    String[] words = searchString.split(" ");

    try {
      for (String word : words) {
        if (ids == null || ids.isEmpty()) {
          ids.addAll(index.get(field, word));
        } else {
          ids.retainAll(index.get(field, word));
        }
      }

      for (int id : ids) {
        Movie movie = read(id);
        if (movie != null) {
          movies.add(movie);
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao buscar registros por título.");
      System.out.println(e);
    }

    return movies;
  }

  public List<Movie> searchByMultipleFields(String[] searchString, String[] fields) {
    // early return
    if (searchString.length != fields.length) {
      System.out.println("Erro ao buscar registros por múltiplos campos.");
      return null;
    }

    List<Integer> ids = new ArrayList<>();
    List<Movie> movies = new ArrayList<>();

    try {
      for (int i = 0; i < fields.length; i++) {
        String[] words = searchString[i].split(" ");

        for (String word : words) {
          if (ids == null || ids.isEmpty()) {
            ids.addAll(index.get(fields[i], word));
          } else {
            ids.retainAll(index.get(fields[i], word));
          }
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao buscar registros por título.");
      System.out.println(e);
    }

    return movies;
  }

  public Movie update(int id, Movie newMovie) {
    boolean updated = false;

    try {
      // reads the lastId
      file.seek(0);
      int lastId = file.readInt();

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
          file.seek(position);

          file.readBoolean(); // skip tombstone (always false)
          registerLength = file.readInt();

          byte[] byteArrayMovie = new byte[registerLength];
          file.read(byteArrayMovie);

          found = new Movie(byteArrayMovie).getId() == id;
        }
      } else {
        while (!found && !isEndOfFile()) {
          // if has no index perform linear search
          position = file.getFilePointer();
          boolean tombstone = file.readBoolean();
          registerLength = file.readInt();

          if (!tombstone) {
            byte[] byteArrayMovie = new byte[registerLength];
            file.read(byteArrayMovie);

            found = new Movie(byteArrayMovie).getId() == id;
          } else {
            file.skipBytes(registerLength);
          }
        }
      }

      // write new register
      if (found) {
        byte[] newByteArrayMovie = newMovie.toByteArray();
        int newLength = newByteArrayMovie.length;

        // go to register position
        file.seek(position);

        if (newLength > registerLength) {
          // set tombstone to true and go to end of file
          file.writeBoolean(true);
          long newPosition = file.length();
          file.seek(newPosition);

          // write new register at end of file
          file.writeBoolean(false); // tombstone
          file.writeInt(newByteArrayMovie.length); // registerLength
          file.write(newByteArrayMovie); // register

          index.update(newMovie, newPosition);
        } else {
          file.writeBoolean(false); // tombstone
          file.writeInt(registerLength); // registerLength
          file.write(newByteArrayMovie); // register
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
      file.seek(0);
      int lastId = file.readInt();

      // early return
      if (id > lastId) {
        return null;
      }

      if (index.isAvailabe()) {
        // if has index availabe, get position from index
        long position = index.get(id);

        if (position != -1) {
          file.seek(position);

          file.readBoolean(); // skip tombstone (always false)
          int registerLength = file.readInt();

          byte[] byteArrayMovie = new byte[registerLength];
          file.read(byteArrayMovie);

          movie = new Movie(byteArrayMovie);

          if (movie.getId() == id) {
            file.seek(position);
            file.writeBoolean(true);

            index.remove(movie);

            deleted = true;
          }
        }
      } else {
        // if has no index perform linear search
        while (!deleted && !isEndOfFile()) {
          long position = file.getFilePointer();
          boolean tombstone = file.readBoolean();
          int registerLength = file.readInt();

          if (!tombstone) {
            byte[] byteArrayMovie = new byte[registerLength];
            file.read(byteArrayMovie);

            movie = new Movie(byteArrayMovie);

            if (movie.getId() == id) {
              file.seek(position);
              file.writeBoolean(true);

              deleted = true;
            }
          } else {
            file.skipBytes(registerLength);
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
    return !(file.getFilePointer() < file.length());
  }

  public boolean isEmpty() throws IOException {
    return file.length() <= Integer.BYTES;
  }

  /**
   * Clean registers and keep lastId
   */
  public void cleanRegisters() throws IOException {
    // reads the lastId
    file.seek(0);
    int lastId = file.readInt();

    file.setLength(0);
    file.writeInt(lastId);

    index.clear();
  }
}
