package model.interfaces;

import java.io.IOException;

public interface IIndexStrategy extends IIndex {
  void add(int id, long position) throws IOException;
  long get(int id) throws IOException;
  void remove(int id) throws IOException;
}
