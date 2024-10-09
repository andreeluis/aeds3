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
  private BTreePage root;

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

  // root
  public BTreePage getRoot() {
    return root;
  }

  public void setRoot(BTreePage page) throws IOException {
    indexFile.seek(0);
    indexFile.writeLong(page.getPosition());

    this.root = page;
  }

  public void setRoot(long position) throws IOException {
    indexFile.seek(0);
    indexFile.writeLong(position);

    byte[] buffer = new byte[BTreePage.pageSize(order)];

    indexFile.seek(position);
    indexFile.read(buffer);

    this.root = new BTreePage(buffer, order, position);
  }

  // constructors
  public BTree(int order, String filePath) throws IOException {
    setOrder(order);
    setFilePathName(filePath);

    this.indexFile = new RandomAccessFile(this.filePathName, "rw");
    if (indexFile.length() <= Long.BYTES) {
      indexFile.seek(0);
      indexFile.writeLong(-1);
      this.root = null;
    } else {
      indexFile.seek(0);
      long rootPosition = indexFile.readLong();

      setRoot(rootPosition);
    }
  }

  @Override
  public void add(int id, long position) throws IOException {
    if (this.root == null) {
      BTreePage newRoot = new BTreePage(order, indexFile.length());
      newRoot.insert(id, position);
      savePage(newRoot);

      setRoot(newRoot);
    } else {
      setRoot(insert(root, id, position));
    }
  }

  private long insert(BTreePage page, int id, long position) throws IOException {
    if (page.isLeaf()) {
      if (!page.isFull()) {
        page.insert(id, position); // Insere o valor
        savePage(page); // Salva a página atualizada
        return page.getPosition(); // Retorna a posição da página
      } else {
        // Divide a página e cria uma nova
        long newPagePosition = indexFile.length();
        BTreePage newPage = page.split(newPagePosition);

        savePage(page);
        savePage(newPage);

        BTreePage parent = new BTreePage(order, indexFile.length());

        // Insere o valor na página apropriada
        if (id < page.getKey(0)) {
          page.insert(id, position);

          int promotedKey = page.getKey(page.getElements() - 1);
          long promotedPosition = page.getKeyPos(page.getElements());
          page.removeKey(page.getElements() - 1);

          parent.insert(promotedKey, promotedPosition);

          parent.setChild(0, page.getPosition());
          parent.setChild(1, newPage.getPosition());
        } else {
          newPage.insert(id, position);

          int promotedKey = newPage.getKey(0);
          long promotedPosition = newPage.getKeyPos(0);
          newPage.removeKey(0);

          parent.insert(promotedKey, promotedPosition);

          parent.setChild(0, page.getPosition());
          parent.setChild(1, newPage.getPosition());
        }

        savePage(page);
        savePage(newPage);
        savePage(parent);

        System.out.println("Old page: " + page);
        System.out.println("New page: " + newPage);
        System.out.println("Parent page: " + parent);

        // Propaga a divisão para cima
        return parent.getPosition();
      }
    } else {
      // Encontra a página apropriada para inserir o valor
      int i = 0;
      while (i < page.getElements() && id > page.getKey(i)) {
        i++;
      }

      long childPosition = page.getChild(i);
      BTreePage childPage = loadPage(childPosition);

      long newChildPosition = insert(childPage, id, position);
      if (newChildPosition != childPosition) {
        page.merge(loadPage(newChildPosition));
        savePage(page);
      }

      return page.getPosition();
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

  private void savePage(BTreePage page) throws IOException {
    indexFile.seek(page.getPosition());
    indexFile.write(page.toByteArray());
  }

  private BTreePage loadPage(long position) throws IOException {
    indexFile.seek(position);

    byte[] buffer = new byte[BTreePage.pageSize(order)];
    indexFile.read(buffer);

    return new BTreePage(buffer, order, position);
  }

}
