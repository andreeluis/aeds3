package index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import model.Movie;

class BPlusTreePage {
  private boolean leaf;
  private List<Integer> keys;
  private List<BPlusTreePage> children;
  private BPlusTreePage next;

  public boolean isLeaf() {
    return leaf;
  }

  public BPlusTreePage() {
    this(true);
  }

  public BPlusTreePage(boolean leaf) {
    this.leaf = leaf;
    this.keys = new ArrayList<>();
    this.children = new ArrayList<>();
    this.next = null;
  }

  public BPlusTreePage(byte[] byteArray) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(byteArray);
    DataInputStream data = new DataInputStream(input);

    // TODO
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(output);

    // TODO

    return output.toByteArray();
  }
}

public class BPlusTree implements IndexStrategy {
  private String filePath;
  private static final String fileName = "BStarIndex";
  private RandomAccessFile indexFile;

  private BPlusTreePage root;
  private int order;

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public BPlusTreePage getRoot() {
    return root;
  }

  public void setRoot(BPlusTreePage root) {
    this.root = root;
  }

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

  public BPlusTree(int order, String filePath) {
    setFilePath(filePath);
    setOrder(order);
    setRoot(new BPlusTreePage());
  }

  @Override
  public void build(Database database) throws FileNotFoundException {
    String indexFilePath = this.filePath + fileName + Database.getFileExtension();
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


  private long savePageToFile(BPlusTreePage page) throws IOException {
    // TODO
    throw new UnsupportedOperationException("Unimplemented method 'savePageToFile'");
  }
}
