package db;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import db.sort.Sort;
import model.Register;
import util.CSVReader;

public class DatabaseControler<T extends Register> {
  private Constructor<T> constructor;
  private Database<T> database;
  private CSVReader<T> csvReader;
  private Sort<T> sort;
  //private Index<T> index;

  public DatabaseControler(String filePath, Constructor<T> constructor) throws FileNotFoundException {
    this.database = new Database<>(filePath, constructor);
    this.constructor = constructor;

    this.csvReader = new CSVReader<T>();
    this.sort = new Sort<T>(database, constructor);
  }

  public void readFromCSV(String csvPath) {
    try {
      this.csvReader.readCSV(csvPath, this.database, this.constructor);
    } catch (Exception e) {
      System.out.println("Erro ao ler o arquivo CSV: " + e.getMessage());
    }
  }

  public void insertRegister(T register) {
    this.database.insert(register);
  }

  public Optional<T> searchById(int id) {
    return this.database.select(id);
  }

  public List<Optional<T>> searchByFields(String[] fields, String[] values) {
    // TODO: implementar
    return null;
  }

  public Optional<T> updateRegister(int id, T register) {
    return this.database.update(id, register);
  }

  public Optional<T> deleteRegister(int id) {
    return this.database.delete(id);
  }

  public boolean sort(int pathsNumber, int inMemoryRegisters) {
    boolean sorted = this.sort.sort(pathsNumber, inMemoryRegisters);

    // index.rebuild();

    return sorted;
  }
}
