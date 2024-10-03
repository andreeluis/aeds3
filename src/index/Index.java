package index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import db.Database;

public class Index {
  private List<IndexStrategy> indexes = new ArrayList<IndexStrategy>();
  private IndexStrategy currentIndexStrategy;
  private Database database;

  public IndexStrategy getCurrentIndexStrategy() {
    return currentIndexStrategy;
  }

  public void setCurrentIndexStrategy(IndexStrategy currentIndexStrategy) {
    this.currentIndexStrategy = currentIndexStrategy;
  }

  public void changeCurrentIndexStrategy(int id) {
    this.currentIndexStrategy = indexes.get(id);
  }

  public Index(Database database, List<IndexStrategy> indexes) {
    this.database = database;

    for (IndexStrategy indexStrategy : indexes) {
      addStrategy(indexStrategy);
    }

    currentIndexStrategy = indexes.get(0);
  }

  public boolean isAvailabe() {
    return indexes.size() > 0;
  }

  public void addStrategy(IndexStrategy indexStrategy) {
    indexes.add(indexStrategy);

    try {
      indexStrategy.build(this.database);
    } catch (FileNotFoundException e) { }
  }

  /**
   * @param position
   * register's tombstone position
   */
  public void add(int id, long position) {
    try {
      for (IndexStrategy indexStrategy : indexes) {
        indexStrategy.add(id, position);
      }
    } catch (IOException e) { }
  }

  /**
   * Return the register's tombstone position
   */
  public long get(int id) {
    long position = -1;

    try {
      position = currentIndexStrategy.get(id);
    } catch (IOException e) { }

    return position;
  }

  public void update(int id, long newPosition) {
    remove(id);
    add(id, newPosition);
  }

  public void remove(int id) {
    try {
      for (IndexStrategy indexStrategy : indexes) {
        indexStrategy.remove(id);
      }
    } catch (IOException e) { }
  }

  public void rebuild() throws IOException {
    for (IndexStrategy indexStrategy : indexes) {
      indexStrategy.clear();
      indexStrategy.build(database);
    }
  }
}
