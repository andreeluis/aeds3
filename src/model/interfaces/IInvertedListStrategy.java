package model.interfaces;

import java.io.IOException;
import java.util.List;

public interface IInvertedListStrategy extends IIndex {
  String getField();

  void add(String key, int id) throws IOException;
  List<Integer> get(String key) throws IOException;
  void remove(String key) throws IOException;
}
