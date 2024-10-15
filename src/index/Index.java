package index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import model.Movie;
import model.interfaces.IIndex;
import model.interfaces.IIndexStrategy;
import model.interfaces.IInvertedListStrategy;

public class Index {
  private List<IIndexStrategy> indexes = new ArrayList<>();
  private List<IInvertedListStrategy> invertedLists = new ArrayList<>();
  private IIndexStrategy currentIndex;
  private Database database;

  // currentIndexStrategy
  public IIndexStrategy getCurrentIndex() {
    return this.currentIndex;
  }

  public void setCurrentIndex(IIndexStrategy currentIndex) {
    this.currentIndex = currentIndex;
  }

  public void changeCurrentIndex(int id) {
    this.setCurrentIndex(this.indexes.get(id));
  }

  // database
  public Database getDatabase() {
    return this.database;
  }

  public void setDatabase(Database database) {
    this.database = database;
  }

  // constructor
  public Index(Database database, List<IIndex> indexes) throws IOException {
    this.setDatabase(database);

    for (IIndex index : indexes) {
      if (index instanceof IIndexStrategy) {
        this.addStrategy((IIndexStrategy) index);
      } else if (index instanceof IInvertedListStrategy) {
        this.addInvertedList((IInvertedListStrategy) index);
      }
    }

    build();
    changeCurrentIndex(0);
  }

  public void build() throws IOException {
    RandomAccessFile dbFile = this.getDatabase().getFile();
    dbFile.seek(0);
    dbFile.skipBytes(Integer.BYTES); // skip lastId

    while (!getDatabase().isEndOfFile()) {
      long position = dbFile.getFilePointer();
      boolean tombstone = dbFile.readBoolean();
      int registerLength = dbFile.readInt();

      if (!tombstone) {
        byte[] byteArrayMovie = new byte[registerLength];
        dbFile.read(byteArrayMovie);

        add(new Movie(byteArrayMovie), position);
      } else {
        dbFile.skipBytes(registerLength);
      }
    }
  }

  public void addInvertedList(IInvertedListStrategy invertedList) {
    invertedLists.add(invertedList);
  }

  public void addStrategy(IIndexStrategy indexStrategy) throws IOException {
    indexes.add(indexStrategy);
  }

  /**
   * @param position
   * register's tombstone position
   */
  public void add(Movie movie, long position) throws IOException {
    for (IIndexStrategy indexStrategy : indexes) {
      indexStrategy.add(movie.getId(), position);
    }

    for (IInvertedListStrategy invertedList : invertedLists) {
      invertedList.add(movie);
    }
  }

  /**
   * Return the register's tombstone position
   */
  public long get(int id) throws IOException {
    return this.getCurrentIndex().get(id);
  }

  /**
   * Return a list of IDs that contains the key
   */
  public List<Integer> get(String key, String field) throws IOException {
    System.out.println(key);
    List<Integer> ids = new ArrayList<>();

    for (IInvertedListStrategy invertedList : invertedLists) {
      if (invertedList.getField().equals(field)) {
        ids = invertedList.get(key);
        break;
      }
    }

    return ids;
  }

  public void update(Movie movie, long newPosition) throws IOException {
    this.remove(movie);
    this.add(movie, newPosition);
  }

  public void remove(Movie movie) throws IOException {
    for (IIndexStrategy indexStrategy : indexes) {
      indexStrategy.remove(movie.getId());
    }

    for (IInvertedListStrategy invertedList : invertedLists) {
      invertedList.remove(movie);
    }
  }

  public void clear() throws IOException {
    for (IIndexStrategy indexStrategy : indexes) {
      indexStrategy.clear();
    }
  }

  public void rebuild() throws IOException {
    clear();
    build();
  }

  public boolean isAvailabe() {
    return indexes.size() > 0;
  }

  public boolean isInvertedListAvailable() {
    return invertedLists.size() > 0;
  }
}
