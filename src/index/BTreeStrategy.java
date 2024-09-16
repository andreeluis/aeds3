package index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import db.Database;

public class BTreeStrategy implements IndexStrategy {
  private String filePath = "btree";
  private RandomAccessFile indexFile;

  public BTreeStrategy() {

  }

  @Override
  public void build(Database database) throws FileNotFoundException {
    filePath = "btree" + Database.getFileExtension();
    this.indexFile = new RandomAccessFile(filePath, "rw");

    // TODO - buscar registros de database e criar o arquivo de index
  }

  @Override
  public void add(int id, long position) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'add'");
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
