package db.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import db.Database;
import model.Register;
import model.interfaces.BaseIndexStrategy;
import model.interfaces.IndexStrategy;
import model.interfaces.InvertedIndexStrategy;
import util.RAF;
import util.RegisterUtil;

public class IndexController<T extends Register> {
  private List<BaseIndexStrategy<T>> indexes;

  private RandomAccessFile databaseFile;
  private Constructor<T> constructor;

  // base indexes
  public void setIndexes(List<BaseIndexStrategy<T>> indexes) {
    this.indexes = indexes;
  }

  // indexes
  public List<IndexStrategy<T>> getIndexes() {
    List<IndexStrategy<T>> indexStrategies = new ArrayList<>();

    for (BaseIndexStrategy<T> index : indexes) {
      if (index instanceof IndexStrategy) {
        indexStrategies.add((IndexStrategy<T>) index);
      }
    }

    return indexStrategies;
  }

  public boolean hasIdIndexAvailable() {
    return !getIndexes().isEmpty();
  }

  // invertedIndex
  public List<InvertedIndexStrategy<T>> getInvertedIndexes() {
    List<InvertedIndexStrategy<T>> invertedIndexStrategies = new ArrayList<>();

    for (BaseIndexStrategy<T> index : indexes) {
      if (index instanceof InvertedIndexStrategy) {
        invertedIndexStrategies.add((InvertedIndexStrategy<T>) index);
      }
    }

    return invertedIndexStrategies;
  }

  // constructor
  public IndexController(List<BaseIndexStrategy<T>> indexes, Database<T> database, Constructor<T> constructor) {
    this.setIndexes(indexes);

    this.databaseFile = database.getFile();
    this.constructor = constructor;
  }

  /**
   * @param register register to be added
   * @param position register's tombstone position
   */
  public void add(T register, long position) {
    if (indexes.isEmpty()) {
      return;
    }

    try {
      for (BaseIndexStrategy<T> index : indexes) {
        index.add(register, position);
      }
    } catch (IOException e) {
      System.out.println("Erro ao adicionar o registro nos índices.");
      e.printStackTrace();
    }
  }

  /**
   * Return the register's tombstone position
   */
  public long get(int id) {
    // select a random index to get the position
    IndexStrategy<T> currentIndex = getIndexes().get(new Random().nextInt(getIndexes().size()));

    try {
      return currentIndex.get(id);
    } catch (IOException e) {
      System.out.println("Erro ao buscar o registro no índice.");
      e.printStackTrace();

      return -1;
    }
  }

  /**
   * Return a list of IDs that contains the key
   */
  public Optional<List<Integer>> get(String[] fields, String[] values) {
    List<Integer> ids = new ArrayList<>();

    try {
      for (int i = 0; i < fields.length; i++) {
        InvertedIndexStrategy<T> inUseIndex = null;

        for (InvertedIndexStrategy<T> index : getInvertedIndexes()) {
          if (index.getField().equals(fields[i])) {
            inUseIndex = index;
            break;
          }
        }

        if (inUseIndex == null) {
          return Optional.empty();
        }

        String[] words = values[i].split(" ");

        for (String word : words) {
          if (ids.isEmpty()) {
            inUseIndex.get(word).ifPresent(ids::addAll);
          } else {
            inUseIndex.get(word).ifPresent(ids::retainAll);
          }
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao buscar o registro na Lista Invertida.");
      e.printStackTrace();
    }

    return ids.isEmpty() ? Optional.empty() : Optional.of(ids);
  }

  /**
   * Update the register's in the indexes
   */
  public void update(T register, long newPosition) {
    // early return
    if (indexes.isEmpty()) {
      return;
    }

    this.remove(register);
    this.add(register, newPosition);
  }

  /**
   * Remove the register from the indexes
   */
  public void remove(T register) {
    try {
      for (BaseIndexStrategy<T> index : indexes) {
        index.remove(register);
      }
    } catch (IOException e) {
      System.out.println("Erro ao remover o registro dos índices.");
      e.printStackTrace();
    }
  }

  /**
   * Rebuild the indexes
   */
  public void rebuild() {
    clear();
    build();
  }

  public void build() {
    try {
      this.databaseFile.seek(0);
      this.databaseFile.readInt(); // skip header

      while (!RAF.isEOF(this.databaseFile)) {
        long position = this.databaseFile.getFilePointer();

        Optional<T> register = RegisterUtil.getNextRegister(this.databaseFile, this.constructor);

        if (register.isPresent()) {
          add(register.get(), position);
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao construir os índices.");
      e.printStackTrace();
    }
  }

  public void clear() {
    try {
      for (BaseIndexStrategy<T> index : indexes) {
        index.clear();
      }
    } catch (IOException e) {
      System.out.println("Erro ao limpar os índices.");
      e.printStackTrace();
    }
  }
}
