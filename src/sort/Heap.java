package sort;

import java.io.IOException;
import java.io.RandomAccessFile;

import model.Movie;

class HeapNode implements Comparable<HeapNode> {
  Movie movie;
  int seg;

  public HeapNode(Movie movie, int seg) {
    this.movie = movie;
    this.seg = seg;
  }

  public Movie getMovie() {
    return this.movie;
  }

  @Override
  public int compareTo(HeapNode other) {
    return this.getMovie().compareTo(other.getMovie());
  }
}

public class Heap {
  private int length;
  private int elements;
  private HeapNode[] movies;

  public Heap(int length) {
    this.length = length;
    this.elements = 0;
    this.movies = new HeapNode[length + 1];
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
    if (elements >= length) {
      throw new IllegalStateException("Heap is full");
    }

    movies[++elements] = new HeapNode(movie, seg);
    heapifyUp(elements);
  }

  public Movie remove(int index) {
    if (elements == 0) {
      throw new IllegalStateException("Heap is empty");
    }

    Movie removedMovie = movies[index].getMovie();

    movies[1] = movies[elements--];
    heapifyDown(1);

    return removedMovie;
  }

  private void heapifyUp(int index) {
    while (index > 1 && movies[parent(index)].compareTo(movies[index]) < 0) {
      swap(index, parent(index));
      index = parent(index);
    }
  }

  private void heapifyDown(int index) {
    int largest = index;
    int left = leftChild(index);
    int right = rightChild(index);

    if (left <= elements && movies[left].compareTo(movies[largest]) > 0) {
      largest = left;
    }

    if (right <= elements && movies[right].compareTo(movies[largest]) > 0) {
      largest = right;
    }

    if (largest != index) {
      swap(index, largest);
      heapifyDown(largest);
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
