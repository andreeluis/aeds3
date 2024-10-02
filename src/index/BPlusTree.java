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
import java.util.Optional;

import db.Database;
import model.Movie;

class BPlusPage {
  public List<Integer> keys = new ArrayList<>();
  public List<Long> positions = new ArrayList<>();
  public List<BPlusPage> children = new ArrayList<>();
  private int order;
  private boolean leaf;

  public BPlusPage(int order, boolean leaf) {
    this.order = order;
    this.leaf = leaf;
  }

  public BPlusPage insert(int key, long position) {
    if (leaf) {
      return insertInLeaf(key, position);
    } else {
      return insertInInternal(key, position);
    }
  }

  private BPlusPage insertInLeaf(int key, long position) {
    int index = 0;

    while (index < keys.size() && keys.get(index) < key) {
      index++;
    }

    keys.add(index, key);
    positions.add(index, position);

    if (keys.size() > order - 1) {
      return split();
    }

    return null;
  }

  private BPlusPage insertInInternal(int key, long position) {
    int index = 0;

    while (index < keys.size() && keys.get(index) < key) {
      index++;
    }

    BPlusPage newNode = children.get(index).insert(key, position);

    if (newNode != null) {
      keys.add(index, newNode.getFirstKey());
      children.add(index + 1, newNode);
    }

    if (keys.size() > order - 1) {
      return split();
    }

    return null;
  }

  private BPlusPage split() {
    BPlusPage newNode = new BPlusPage(order, leaf);
    int midIndex = keys.size() / 2;

    // Mover metade das chaves e posições para o novo nó
    newNode.keys.addAll(keys.subList(midIndex, keys.size()));
    newNode.positions.addAll(positions.subList(midIndex, positions.size()));

    // Remover as chaves e posições do nó atual
    keys.subList(midIndex, keys.size()).clear();
    positions.subList(midIndex, positions.size()).clear();

    if (!leaf) {
      newNode.children.addAll(children.subList(midIndex + 1, children.size()));
      children.subList(midIndex + 1, children.size()).clear();
    }

    return newNode;
  }

  public Integer getFirstKey() {
    return keys.isEmpty() ? null : keys.get(0);
  }

  public BPlusPage(byte[] byteArray) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(byteArray);
    DataInputStream data = new DataInputStream(input);

    // [qt, p0, id0, pos0, p1, ... , idn, posn, pn+1]
    // [int] + n[int] + 2n[long] + 1[long]

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

  private BPlusPage root;
  private int order;

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public BPlusPage getRoot() {
    return root;
  }

  public void setRoot(BPlusPage root) {
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
    setRoot(new BPlusPage(order, true));
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
    BPlusPage newPage = root.insert(id, position);

    if (newPage != null) {
      // Se o nó retornado não é nulo, significa que houve divisão do nó raiz
      BPlusPage newRoot = new BPlusPage(order, false);

      newRoot.keys.add(root.getFirstKey());
      newRoot.children.add(root);
      newRoot.children.add(newPage);

      root = newRoot;
    }
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

  private long savePageToFile(BPlusPage page) throws IOException {
    // TODO
    throw new UnsupportedOperationException("Unimplemented method 'savePageToFile'");
  }
}
