package model.interfaces;

import java.io.IOException;

import model.Register;

public interface IndexStrategy<T extends Register> extends BaseIndexStrategy<T> {
  long get(int id) throws IOException;
}
