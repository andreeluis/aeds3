package model.interfaces;

import java.io.IOException;

import model.Register;

public interface BaseIndexStrategy<T extends Register> {
  String getName();

  void add(T register, long position) throws IOException;
  void remove(T register) throws IOException;
  void clear() throws IOException;
}
