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
  public BTreePage(int order) {
    this.elements = 0;
    this.keys = new int[order - 1];
    this.keysPos = new long[order - 1];
    this.childrens = new long[order];

    Arrays.fill(keysPos, -1L);
    Arrays.fill(childrens, -1L);

    setOrder(order);
  }

  public BTreePage(byte[] byteArray, int order) throws IOException {
    this.order = order;
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

  public static int pageFileLength(int order) {
    return Integer.BYTES + (Long.BYTES + Integer.BYTES + Long.BYTES) * (order - 1) + Long.BYTES;
  }
}
