package db.sort;

import model.Register;

public class HeapNode<T extends Register> implements Comparable<HeapNode<T>> {
  private T register;
  private int segment;

  public HeapNode(T register, int segment) {
    this.register = register;
    this.segment = segment;
  }

  public T getRegister() {
    return this.register;
  }

  public int getSegment() {
    return this.segment;
  }

  @Override
  public int compareTo(HeapNode<T> other) {
    if (this.segment != other.getSegment()) {
      return this.getSegment() - other.getSegment();
    } else {
      return this.getRegister().compareTo(other.getRegister());
    }
  }
}
