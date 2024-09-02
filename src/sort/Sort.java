package sort;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import db.Database;
import model.Movie;

public class Sort {
  private static int pathsNumber = 2;
  private static int inMemoryRegisters = 4;
  private RandomAccessFile[] tmpFiles;
  private Database database;
  private Heap moviesHeap;

  public Sort(Database database) throws IOException {
    this.database = database;

    // starts and fill moviesHeap
    moviesHeap = new Heap(inMemoryRegisters);
  }

  private void openTmpFiles() throws IOException {
    String filePath = database.getFilePath();
    String fileExtension = Database.getFileExtension();

    tmpFiles = new RandomAccessFile[pathsNumber];

    for (int i = 0; i < pathsNumber; i++) {
      String tmpFilePath = filePath + (i + 1) + fileExtension;

      // start and clean files[i]
      tmpFiles[i] = new RandomAccessFile(tmpFilePath, "rw");
      tmpFiles[i].setLength(0);
      tmpFiles[i].seek(0);
    }
  }

  public void closeTmpFiles() throws IOException {
    if (tmpFiles == null) {
      return;
    }

    // close files
    for (int i = 0; i < pathsNumber; i++) {
      if (tmpFiles[i] != null) {
        tmpFiles[i].close();
      }
    }

    // delete files
    String filePath = database.getFilePath();
    String fileExtension = Database.getFileExtension();

    for (int i = 0; i < pathsNumber; i++) {
      String tmpFilePath = filePath + (i + 1) + fileExtension;
      File file = new File(tmpFilePath);

      if (file.exists()) {
        file.delete();
      }
    }

    tmpFiles = null;
  }

  public int distribution() throws IOException {
    RandomAccessFile data = database.getDatabase();

    // skips the lastId
    data.seek(0);
    data.readInt();

    moviesHeap.fill(database.getDatabase());

    int segment = 0;
    openTmpFiles();

    while (moviesHeap.hasElements()) {
      // remove from heap and get info
      HeapNode heapNode = moviesHeap.remove();
      segment = heapNode.getSegment();
      Movie movie = heapNode.getMovie();
      int movieId = movie.getId();

      // add to temp file
      int currentFile = segment % pathsNumber;
      tmpFiles[currentFile].seek(tmpFiles[currentFile].length());

      byte[] byteArrayMovie = movie.toByteArray();
      tmpFiles[currentFile].writeBoolean(false); // tombstone
      tmpFiles[currentFile].writeInt(byteArrayMovie.length); // register length
      tmpFiles[currentFile].write(byteArrayMovie); // register

      // if has more register, add to heap
      boolean added = false; // this var ensure that heap is always full
      while (!added && data.getFilePointer() < data.length()) {
        boolean tombstone = data.readBoolean();
        int registerLength = data.readInt();

        if (!tombstone) {
          added = true;

          // read register and creates Movie object
          byte[] newByteArrayMovie = new byte[registerLength];
          data.read(newByteArrayMovie);
          Movie newMovie = new Movie(newByteArrayMovie);

          // if the current id is smaller than the previous
          int newSegment = (newMovie.getId() < movieId) ? segment + 1 : segment;
          moviesHeap.insert(newMovie, newSegment);
        } else {
          data.skipBytes(registerLength);
        }
      }
    }

    // last used segment
    return segment;
  }

  public void intercalation() throws IOException {
    database.cleanDatabaseRegisters();

    RandomAccessFile data = database.getDatabase();

    Movie[] movies = new Movie[pathsNumber];
    boolean[] endOfFiles = new boolean[pathsNumber];
    boolean finished = false;

    for (int i = 0; i < pathsNumber; i++) {
      tmpFiles[i].seek(0);
      endOfFiles[i] = false;
    }

    while (!finished) {
      // fill movies array
      for (int i = 0; i < pathsNumber; i++) {
        if (!endOfFiles[i] && tmpFiles[i].getFilePointer() < tmpFiles[i].length()) {
          if (movies[i] == null) {
            tmpFiles[i].readBoolean(); // skip tombstone (always false)
            int registerLength = tmpFiles[i].readInt();

            // read register and creates Movie object
            byte[] byteArrayMovie = new byte[registerLength];
            tmpFiles[i].read(byteArrayMovie);
            movies[i] = new Movie(byteArrayMovie);
          }
        } else {
          endOfFiles[i] = true;
        }
      }

      // selection for the smaller
      int smaller = -1;
      for (int i = 0; i < pathsNumber; i++) {
        if (movies[i] != null) {
          if (smaller == -1 || movies[i].compareTo(movies[smaller]) < 0) {
            smaller = i;
          }
        }
      }

      if (smaller != -1) {
        // remove movies[smaller]
        byte[] byteArrayMovie = movies[smaller].toByteArray();
        movies[smaller] = null;

        data.writeBoolean(false); // tombstone
        data.writeInt(byteArrayMovie.length); // registerLength
        data.write(byteArrayMovie); // register
      }

      // verify finish
      finished = true;
      for (int i = 0; i < pathsNumber; i++) {
        if (!endOfFiles[i]) {
          finished = false;
        }
      }
    }

    closeTmpFiles();
  }
}
