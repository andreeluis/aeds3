package model;

import java.io.FileNotFoundException;
import java.io.IOException;

import db.Database;

public interface IIndex {
  void build(Database database) throws FileNotFoundException;
  void clear() throws IOException;
}
