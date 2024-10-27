package model.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import model.Register;

public interface InvertedIndexStrategy<T extends Register> extends BaseIndexStrategy<T> {
  String getField();

  Optional<List<Integer>> get(String key) throws IOException;
}
