package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.Scanner;

import db.Database;
import model.Register;

public class CSVReader<T extends Register> {
  public void readCSV(String path, Database<T> database, Constructor<T> constructor) throws FileNotFoundException, ReflectiveOperationException {
    File file = new File(path);
    Scanner scanner = new Scanner(file);

    scanner.nextLine(); // skip header

    while (scanner.hasNextLine()) {
      T register = constructor.newInstance();
      register.fromCSVLine(scanner.nextLine());

      database.insert(register);
    }

    scanner.close();
  }
}
