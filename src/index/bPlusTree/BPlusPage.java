package index.bPlusTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

class BPlusPage {
  private int order;
  public int elements;
  public int[] keys;
  public Long[] keysPos;
  public Long[] childrens;
  private long next;
  private long position;

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

  // elements
  public int getElements() {
    return elements;
  }

  public void setElements(int elements) {
    this.elements = elements;
  }

  // position
  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

  // constructors
  public BPlusPage(int order) {
    this.elements = 0;
    this.keys = new int[order - 1];
    this.keysPos = new Long[order - 1];
    this.childrens = new Long[order];

    Arrays.fill(keysPos, -1L);
    Arrays.fill(childrens, -1L);

    setOrder(order);
  }

  public BPlusPage(byte[] byteArray, int order, long position) throws IOException {
    setOrder(order);
    setPosition(position);

    ByteArrayInputStream input = new ByteArrayInputStream(byteArray);
    DataInputStream data = new DataInputStream(input);

    this.elements = data.readInt();

    for (int i = 0; i < order - 1; i++) {
      this.childrens[i] = data.readLong();
      this.keys[i] = data.readInt();
      this.keysPos[i] = data.readLong();
    }

    this.childrens[order - 1] = data.readLong();

    this.next = data.readLong();
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(output);

    data.writeInt(this.elements);

    for (int i = 0; i < elements; i++) {
      data.writeLong(childrens[i]);
      data.writeInt(keys[i]);
      data.writeLong(keysPos[i]);
    }

    data.writeLong(childrens[elements]);

    byte[] emptyKeys = new byte[(Integer.BYTES + Long.BYTES)];
    for (int i = elements; i < order - 1; i++) {
      data.write(emptyKeys);
      data.writeLong(childrens[i + 1]);
    }

    data.writeLong(next);

    return output.toByteArray();
  }

  public void insert(int key, long position) {
    int index = elements - 1;

    while (index >= 0 && keys[index] > key) {
      keys[index + 1] = keys[index];
      keysPos[index + 1] = keysPos[index];
      childrens[index + 2] = childrens[index + 1];
      index--;
    }

    keys[index + 1] = key;
    keysPos[index + 1] = position;
    elements++;
  }

  public void remove(int key) {

  }

  public BPlusPage split() {
    BPlusPage newPage = new BPlusPage(order);

    int middle = elements / 2;
    newPage.elements = elements - middle - 1;

    for (int i = 0; i < newPage.elements; i++) {
      newPage.keys[i] = keys[middle + i + 1];
      newPage.keysPos[i] = keysPos[middle + i + 1];
      newPage.childrens[i + 1] = childrens[middle + i + 1];
    }

    newPage.childrens[0] = childrens[middle];
    elements = middle;

    return newPage;
  }

  public boolean isFull() {
    return elements == (order - 1);
  }

  public boolean isLeaf() {
    return childrens[0] == -1;
  }

  public int getKey(int index) {
    return keys[index];
  }

  public long getChild(int index) {
    return childrens[index];
  }

  public static int pageFileLength(int order) {
    return Integer.BYTES + (Long.BYTES + Integer.BYTES + Long.BYTES) * (order - 1) + Long.BYTES * 2;
  }
}
