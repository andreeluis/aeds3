package index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.xml.crypto.Data;

import db.Database;
import model.Movie;

public class BStarTree implements IndexStrategy {
  private String filePath = "btree";
  private RandomAccessFile indexFile;

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public BStarTree(String filePath) {
    setFilePath(filePath);
  }

  @Override
  public void build(Database database) throws FileNotFoundException {
    String indexFilePath = this.filePath + Database.getFileExtension();
    this.indexFile = new RandomAccessFile(indexFilePath, "rw");

    try {
      if (indexFile.length() == 0) {
        indexFile.seek(0);

        // skips the lastId
        database.getDatabase().seek(0);
        database.getDatabase().readInt();

        // read all registers
        while (!database.isEndOfFile()) {
          long position = database.getDatabase().getFilePointer();
          boolean tombstone = database.getDatabase().readBoolean();
          int registerLength = database.getDatabase().readInt();

          if (!tombstone) {
            byte[] byteArrayMovie = new byte[registerLength];
            database.getDatabase().read(byteArrayMovie);

            int id = new Movie(byteArrayMovie).getId();

            this.add(id, position);
          } else {
            database.getDatabase().skipBytes(registerLength);
          }
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de index (BStarTree).");
      System.out.println(e);
    }
  }

  @Override
  public void add(int id, long position) throws IOException {
    // test
    indexFile.seek(indexFile.length());

    indexFile.writeInt(id);
    indexFile.writeLong(position);
  }

  @Override
  public long get(int id) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'get'");
  }

  @Override
  public void remove(int id) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  public void clear() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'clear'");
  }
}
