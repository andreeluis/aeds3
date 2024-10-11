package model;

import java.io.IOException;
import java.util.List;

public interface IInvertedListStrategy extends IIndex {
  void add(InvertedListRegister register) throws IOException;
  List<Integer> get(String key) throws IOException;
  void remove(String key) throws IOException;
}
