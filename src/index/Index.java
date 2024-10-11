package index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import model.IIndexStrategy;
import model.IInvertedListStrategy;

public class Index {
  private List<IIndexStrategy> indexes = new ArrayList<IIndexStrategy>();
  private List<IInvertedListStrategy> invertedLists = new ArrayList<IInvertedListStrategy>();
  private IIndexStrategy currentIndexStrategy;
  private Database database;

  // currentIndexStrategy
  public IIndexStrategy getCurrentIndexStrategy() {
    return currentIndexStrategy;
  }

  public void setCurrentIndexStrategy(IIndexStrategy currentIndexStrategy) {
    this.currentIndexStrategy = currentIndexStrategy;
  }

  public void changeCurrentIndexStrategy(int id) {
    this.currentIndexStrategy = indexes.get(id);
  }

  // constructor
  public Index(Database database, List<IIndexStrategy> indexes) {
    this.database = database;

    for (IIndexStrategy indexStrategy : indexes) {
      addStrategy(indexStrategy);
    }

    currentIndexStrategy = indexes.get(0);
  }

  public boolean isAvailabe() {
    return indexes.size() > 0;
  }

  public void addStrategy(IIndexStrategy indexStrategy) {
    indexes.add(indexStrategy);

    // try {
    //   indexStrategy.build(this.database);
    // } catch (FileNotFoundException e) { }
  }

  /**
   * @param position
   * register's tombstone position
   */
  public void add(int id, long position) throws IOException {
    for (IIndexStrategy indexStrategy : indexes) {
      indexStrategy.add(id, position);
    }
  }

  /**
   * Return the register's tombstone position
   */
  public long get(int id) throws IOException {
    long position = -1;

    position = currentIndexStrategy.get(id);

    return position;
  }

  public List<Integer> get(String key) throws IOException {
    List<Integer> positions = new ArrayList<>();

    for (IInvertedListStrategy invertedList : invertedLists) {
      List<Integer> invertedListPositions = invertedList.get(key);
      positions.addAll(invertedListPositions);
    }

    return positions;
  }

  public void update(int id, long newPosition) throws IOException {
    this.remove(id);
    this.add(id, newPosition);
  }

  public void remove(int id) throws IOException {
    for (IIndexStrategy indexStrategy : indexes) {
      indexStrategy.remove(id);
    }
  }

  public void clear() throws IOException {
    for (IIndexStrategy indexStrategy : indexes) {
      indexStrategy.clear();
    }
  }

  public void rebuild() throws IOException {
    for (IIndexStrategy indexStrategy : indexes) {
      indexStrategy.clear();
      indexStrategy.build(database);
    }
  }
}
