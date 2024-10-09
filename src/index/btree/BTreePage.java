package index.btree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

class BTreePage {
  private int order;
  public int elements;
  public int[] keys;
  public long[] keysPos;
  public long[] childrens;
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
  public BTreePage(int order, long position) {
    this.elements = 0;
    this.keys = new int[order - 1];
    this.keysPos = new long[order - 1];
    this.childrens = new long[order];

    Arrays.fill(keysPos, -1L);
    Arrays.fill(childrens, -1L);

    setOrder(order);
    setPosition(position);
  }

  public BTreePage(byte[] byteArray, int order, long position) throws IOException {
    this.keys = new int[order - 1];
    this.keysPos = new long[order - 1];
    this.childrens = new long[order];

    ByteArrayInputStream input = new ByteArrayInputStream(byteArray);
    DataInputStream data = new DataInputStream(input);

    this.elements = data.readInt();

    for (int i = 0; i < order - 1; i++) {
      this.childrens[i] = data.readLong();
      this.keys[i] = data.readInt();
      this.keysPos[i] = data.readLong();
    }

    this.childrens[order - 1] = data.readLong();

    setOrder(order);
    setPosition(position);
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

  public static int pageSize(int order) {
    return Integer.BYTES + (Long.BYTES + Integer.BYTES + Long.BYTES) * (order - 1) + Long.BYTES;
  }

  public boolean isFull() {
    return elements == order - 1;
  }

  public boolean isLeaf() {
    return childrens[0] == -1;
  }

  public BTreePage split(long position) throws IOException {
    BTreePage newPage = new BTreePage(order, position);

    int middle = elements / 2;
    int i;

    // Move the second half of the keys and children to the new page
    for (i = 0; i < middle; i++) {
      newPage.keys[i] = this.keys[i + middle];
      newPage.keysPos[i] = this.keysPos[i + middle];
      newPage.childrens[i] = this.childrens[i + middle];

      this.keys[i + middle] = 0;
      this.keysPos[i + middle] = -1;
      this.childrens[i + middle] = -1;

      newPage.elements++;
      this.elements--;
    }

    newPage.childrens[i] = this.childrens[i + middle];
    this.childrens[i + middle] = -1;

    return newPage;
  }

  public int getKey(int i) {
    return keys[i];
  }

  public void removeKey(int i) {
    for (int j = i; j < elements - 1; j++) {
      keys[j] = keys[j + 1];
      keysPos[j] = keysPos[j + 1];
      childrens[j + 1] = childrens[j + 2];
    }

    keys[elements - 1] = 0;
    keysPos[elements - 1] = -1;
    childrens[elements] = -1;

    elements--;
  }

  public long getKeyPos(int i) {
    return keysPos[i];
  }

  public long getChild(int i) {
    return childrens[i];
  }

  public void setChild(int i, long child) {
    childrens[i] = child;
  }

  public BTreePage searchChild(int key) {
    int i;
    for (i = 0; i < elements && key > keys[i]; i++);

    return new BTreePage(order, childrens[i]);
  }

  public BTreePage merge(BTreePage sibling) {
    BTreePage merged = new BTreePage(order, position);

    int i;
    for (i = 0; i < elements; i++) {
      merged.keys[i] = keys[i];
      merged.keysPos[i] = keysPos[i];
      merged.childrens[i] = childrens[i];
      merged.elements++;
    }

    merged.childrens[i] = childrens[i];
    merged.elements++;

    for (i = 0; i < sibling.elements; i++) {
      merged.keys[merged.elements] = sibling.keys[i];
      merged.keysPos[merged.elements] = sibling.keysPos[i];
      merged.childrens[merged.elements] = sibling.childrens[i];
      merged.elements++;
    }

    merged.childrens[merged.elements] = sibling.childrens[i];
    merged.elements++;

    return merged;
  }

  @Override
  public String toString() {
    return "BTreePage{" +
      " el: " + elements +
      ", keys=" + Arrays.toString(keys) +
      ", keysPos=" + Arrays.toString(keysPos) +
      ", childrens=" + Arrays.toString(childrens) +
      ", position=" + position +
      '}';
  }
}
