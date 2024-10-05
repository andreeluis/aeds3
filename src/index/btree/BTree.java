package index.btree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import db.Database;
import index.IndexStrategy;

public class BTree implements IndexStrategy {
  private int order;
  private String filePathName;
  private RandomAccessFile indexFile;

  // order
  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    if (order >= 3) {
      this.order = order;
    } else {
      System.out.println("A ordem precisa ser no minimo 3!");
      this.order = 3;
    }
  }

  // filePathName
  public String getFilePathName() {
    return filePathName;
  }

  public void setFilePathName(String filePath) {
    this.filePathName = filePath + "BTreeIndex" + Database.getFileExtension();
  }

  public BTree(int order, String filePath) throws IOException {
    setOrder(order);
    setFilePathName(filePath);

    this.indexFile = new RandomAccessFile(this.filePathName, "rw");
    if (indexFile.length() < Long.BYTES) {
      indexFile.seek(0);
      indexFile.writeLong(-1);
    }
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

  @Override
  public void build(Database database) throws FileNotFoundException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'build'");
  }
}
