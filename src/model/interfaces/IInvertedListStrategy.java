package model.interfaces;

import java.io.IOException;
import java.util.List;

import model.Movie;

public interface IInvertedListStrategy extends IIndex {
  String getField();

  void add(Movie movie) throws IOException;
  List<Integer> get(String key) throws IOException;
  void remove(Movie movie) throws IOException;
}
