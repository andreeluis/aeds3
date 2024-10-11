package model;

import java.io.IOException;
import java.util.List;

public interface IInvertedListStrategy extends IIndex {
  void add(String key, int id) throws IOException;
  List<Integer> get(String key) throws IOException;
  void remove(String key) throws IOException;
}
