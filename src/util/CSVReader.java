package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.Scanner;

import db.Database;
import model.Register;

public class CSVReader<T extends Register> {
  /**
   * Reads a CSV file and inserts its content into a database.
   *
   * @param path the path to the CSV file
   * @param database the database to insert the content
   * @param constructor the constructor of the Register class
   * @throws FileNotFoundException
   */
  public void readCSV(String path, Database<T> database, Constructor<T> constructor) throws FileNotFoundException {
    File file = new File(path);
    Scanner scanner = new Scanner(file);

    scanner.nextLine(); // skip header

    try {
      while (scanner.hasNextLine()) {
        T register = constructor.newInstance();
        register.fromCSVLine(scanner.nextLine());

        database.insert(register);
      }
    } catch (ReflectiveOperationException e) {
      System.out.println("Erro ao instanciar o objeto: " + e.getMessage());
    }

    scanner.close();
  }
}
