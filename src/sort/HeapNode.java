package sort;

import model.Movie;

public class HeapNode implements Comparable<HeapNode> {
  private Movie movie;
  private int segment;

  public HeapNode(Movie movie, int seg) {
    this.movie = movie;
    this.segment = seg;
  }

  public Movie getMovie() {
    return this.movie;
  }

  public int getSegment() {
    return this.segment;
  }

  @Override
  public int compareTo(HeapNode other) {
    if (this.segment != other.getSegment()) {
      return this.getSegment() - other.getSegment();
    } else {
      return this.getMovie().compareTo(other.getMovie());
    }
  }

  @Override
  public String toString() {
    return "<" + this.getSegment() + ":" + this.movie.getId() + ">";
  }
}
