package index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import db.Database;

public class Index {
  private List<IndexStrategy> strategies = new ArrayList<IndexStrategy>();
  private Database database;

  public Index(Database database, List<IndexStrategy> indexes) {
    this.database = database;

    for (IndexStrategy indexStrategy : indexes) {
      addStrategy(indexStrategy);
    }
  }

  public void addStrategy(IndexStrategy indexStrategy) {
    strategies.add(indexStrategy);

    try {
      indexStrategy.build(this.database);
    } catch (FileNotFoundException e) { }
  }

  public void add(int id, long position) {
    try {
      for (IndexStrategy indexStrategy : strategies) {
        indexStrategy.add(id, position);
      }
    } catch (IOException e) { }
  }

  public long get(int id) {
    long position = -1;

    try {
      for (IndexStrategy indexStrategy : strategies) {
        position = indexStrategy.get(id);
      }
    } catch (IOException e) { }

    return position;
  }

  public void remove(int id) {
    try {
      for (IndexStrategy indexStrategy : strategies) {
        indexStrategy.remove(id);
      }
    } catch (IOException e) { }
  }

  public void rebuild() throws IOException {
    for (IndexStrategy indexStrategy : strategies) {
      indexStrategy.clear();
      indexStrategy.build(database);
    }
  }
}
