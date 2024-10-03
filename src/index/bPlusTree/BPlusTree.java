package index.bPlusTree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import db.Database;
import index.IndexStrategy;

public class BPlusTree implements IndexStrategy {
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
    this.filePathName = filePath + "BPlusIndex" + Database.getFileExtension();
  }

  public BPlusTree(int order, String filePath) throws IOException {
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
    if (indexFile.length() == Long.BYTES) {
      // Se a árvore está vazia

      BPlusPage newLeaf = new BPlusPage(order);
      newLeaf.insert(id, position);

      // Escreve a nova página folha no arquivo
      long newLeafPosition = indexFile.length();
      indexFile.seek(newLeafPosition);
      indexFile.write(newLeaf.toByteArray());

      // Atualiza a raiz da árvore
      indexFile.seek(0);
      indexFile.writeLong(newLeafPosition);
    } else {
      // Encontra a página folha apropriada para inserir o valor
      BPlusPage leafPage = getLeafPage(id);

      // Insere o valor na página folha
      leafPage.insert(id, position);

      // Se a página folha estiver cheia após a inserção, divide a página
      if (leafPage.isFull()) {
        // Divide a página
        BPlusPage newLeaf = leafPage.split();

        // Escreve a nova página no arquivo
        long newLeafPos = indexFile.length();
        indexFile.seek(newLeafPos);
        indexFile.write(newLeaf.toByteArray());

        // Atualiza a página folha original no arquivo
        indexFile.seek(leafPage.getPosition());
        indexFile.write(leafPage.toByteArray());

        // Atualiza a raiz da árvore
        indexFile.seek(0);
        indexFile.writeLong(newLeafPos);
      } else {
        // Atualiza a página folha original no arquivo
        indexFile.seek(leafPage.getPosition());
        indexFile.write(leafPage.toByteArray());
      }
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

  @Override
  public void build(Database database) throws FileNotFoundException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'build'");
  }

  private long rootPosition() throws IOException {
    indexFile.seek(0);
    return indexFile.readLong();
  }

  private BPlusPage getRoot() throws IOException {
    long rootPosition = rootPosition();

    indexFile.seek(rootPosition);

    byte[] bytes = new byte[BPlusPage.pageFileLength(order)];
    indexFile.read(bytes);

    return new BPlusPage(bytes, order, rootPosition);
  }

  private BPlusPage getLeafPage(int id) throws IOException {
    BPlusPage currPage = getRoot();

    while (!currPage.isLeaf()) {
      int i;
      for (i = 0; i < currPage.getElements() && id >= currPage.getKey(i); i++);

      long childPagePos = currPage.getChild(i);
      indexFile.seek(childPagePos);

      byte[] bytes = new byte[BPlusPage.pageFileLength(order)];
      indexFile.read(bytes);
      currPage = new BPlusPage(bytes, order, childPagePos);
    }

    return currPage;
  }
}
