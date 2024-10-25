package model;

import java.io.IOException;

public abstract class Register implements Comparable<Register> {
  private int id;

  // id
  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Register() {
    this(-1);
  }

  public Register(int id) {
    this.id = id;
  }

  public abstract String getEntityName();
  public abstract void fromCSVLine(String csvLine);
  public abstract void fromByteArray(byte[] byteArray) throws IOException;
  public abstract byte[] toByteArray() throws IOException;
  public abstract String toString();

  @Override
  public int compareTo(Register other) {
    return this.id - other.id;
  }
}
