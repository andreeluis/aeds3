package sort;

import java.io.IOException;
import java.io.RandomAccessFile;

import model.Movie;

public class Sort {
  private int pathsNumber = 2;
  private int inMemoryRegisters = 4;
  private RandomAccessFile[] files;
  private Heap moviesHeap;

  public Sort(int pathsNumber, int inMemoryRegisters) {
    this.pathsNumber = pathsNumber;
    this.inMemoryRegisters = inMemoryRegisters;

    this.moviesHeap = new Heap(inMemoryRegisters);
    // TODO - start heap
  }

  public Sort() { }

  public void createTmpFiles(String filePath, String fileExtension) throws IOException {
    this.files = new RandomAccessFile[pathsNumber];

    for (int i = 0; i < pathsNumber; i++) {
      String tmpFilePath = filePath + (i + 1) + fileExtension;

      // start and clean files[i]
      this.files[i] = new RandomAccessFile(tmpFilePath, "rw");
      this.files[i].setLength(0);
    }
  }

  // TODO
  public void deleteTmpFiles() { }

  public void distribution(RandomAccessFile data) throws IOException {
    moviesHeap.fill(data);
    System.out.println(moviesHeap);

    while (moviesHeap.hasElements()) {
      // remove from heap and get info
      HeapNode heapNode = moviesHeap.remove();
      int segment = heapNode.getSegment();
      Movie movie = heapNode.getMovie();
      int movieId = movie.getId();

      // add to temp file
      int currentFile = segment % this.pathsNumber;
      files[currentFile].seek(files[currentFile].length());

      byte[] byteArrayMovie = movie.toByteArray();
      files[currentFile].writeBoolean(false); // tombstone
      files[currentFile].writeInt(byteArrayMovie.length); // register length
      files[currentFile].write(byteArrayMovie); // register

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
  }
}
