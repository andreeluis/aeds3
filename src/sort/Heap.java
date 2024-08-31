package sort;

import java.io.IOException;
import java.io.RandomAccessFile;

import model.Movie;

public class Heap {
  private int length;
  private int elements;
  private HeapNode[] movies;

  public Heap(int length) {
    this.length = length;
    this.elements = 0;
    this.movies = new HeapNode[length + 1];
  }

  public boolean hasElements() {
    return elements > 0;
  }

  public void fill(RandomAccessFile file) throws IOException {
    for (int i = 0; i < length && file.getFilePointer() < file.length(); i++) {
      boolean lapide = file.readBoolean();
      int len = file.readInt();

      if (!lapide) {
        byte[] byteArrayMovie = new byte[len];
        file.read(byteArrayMovie);

        insert(new Movie(byteArrayMovie), 0);
      } else {
        file.skipBytes(len);
      }
    }
  }

  public void insert(Movie movie, int seg) {
    movies[++elements] = new HeapNode(movie, seg);
    heapifyUp(elements);
  }

  public HeapNode remove() {
    HeapNode removedMovie = movies[1];
    movies[1] = movies[elements--];
    heapifyDown(1);

    return removedMovie;
  }

  private void heapifyUp(int index) {
    while (index > 1 && movies[parent(index)].compareTo(movies[index]) > 0) {
      swap(index, parent(index));
      index = parent(index);
    }
  }

  private void heapifyDown(int index) {
    int smallest = index;
    int left = leftChild(index);
    int right = rightChild(index);

    if (left <= elements && movies[left].compareTo(movies[smallest]) < 0) {
      smallest = left;
    }

    if (right <= elements && movies[right].compareTo(movies[smallest]) < 0) {
      smallest = right;
    }

    if (smallest != index) {
      swap(index, smallest);
      heapifyDown(smallest);
    }
  }

  private void swap(int i, int j) {
    HeapNode temp = movies[i];
    movies[i] = movies[j];
    movies[j] = temp;
  }

  private int parent(int i) {
    return i / 2;
  }

  private int leftChild(int i) {
    return 2 * i;
  }

  private int rightChild(int i) {
    return (2 * i) + 1;
  }
}
