package index;

import java.io.FileNotFoundException;
import java.io.IOException;

import db.Database;

public interface IndexStrategy {
  void build(Database database) throws FileNotFoundException;
  void add(int id, long position) throws IOException;
  long get(int id) throws IOException;
  void remove(int id) throws IOException;
  void clear() throws IOException;
}
