package db.index.bplustree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BPlusRegister implements Comparable<BPlusRegister> {
  private int key;
  private long position;

  // key
  public int getKey() {
    return this.key;
  }

  public void setKey(int key) {
    this.key = key;
  }

  // position
  public long getPosition() {
    return this.position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

  // constructors
  public BPlusRegister() {
    this(0, 0);
  }

  public BPlusRegister(int key, long position) {
    this.setKey(key);
    this.setPosition(position);
  }

  public BPlusRegister(byte[] buffer) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(buffer);
    DataInputStream data = new DataInputStream(input);

    this.setKey(data.readInt());
    this.setPosition(data.readLong());
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(output);

    data.writeInt(this.getKey());
    data.writeLong(this.getPosition());

    return output.toByteArray();
  }

  public static int getSize() {
    // key + position
    return Integer.BYTES + Long.BYTES;
  }

  @Override
  public int compareTo(BPlusRegister other) {
    return this.getKey() - other.getKey();
  }

  public int compareTo(int key) {
    return this.getKey() - key;
  }

  @Override
  public String toString() {
    return "key: " + this.getKey() + ", position: " + this.getPosition();
  }

  @Override
  public BPlusRegister clone() {
    return new BPlusRegister(this.getKey(), this.getPosition());
  }
}
