package controller;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import db.Database;
import model.Register;
import util.CSVReader;

public class DatabaseControler<T extends Register> {
  private Constructor<T> constructor;
  private Database<T> database;
  private CSVReader<T> csvReader;

  public DatabaseControler(Database<T> database, Constructor<T> constructor) {
    this.database = database;
    this.constructor = constructor;

    this.csvReader = new CSVReader<T>();
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
}
