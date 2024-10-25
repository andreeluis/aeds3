package model;

public class RegisterPosition {
  private long position;
  private int length;

  // position
  public long getPosition() {
    return this.position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

  // length
  public int getLength() {
    return this.length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public RegisterPosition(long position, int length) {
    this.setPosition(position);
    this.setLength(length);
  }
}
