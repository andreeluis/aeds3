package controller;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import db.Database;
import db.compression.CompressionController;
import db.cryptography.CryptographyController;
import db.index.IndexController;
import db.pattern.PatternController;
import db.sort.Sort;
import model.CompressionStats;
import model.Register;
import model.interfaces.BaseIndexStrategy;
import model.interfaces.IndexStrategy;
import model.interfaces.InvertedIndexStrategy;
import util.CSVReader;
import util.RegisterUtil;

public class AppController<T extends Register> {
  private Constructor<T> constructor;
  private Database<T> database;
  private CSVReader<T> csvReader;
  private Sort<T> sort;
  private IndexController<T> index;
	private CompressionController compression;
	private PatternController pattern;
  private CryptographyController cryptography;

  public AppController(Constructor<T> constructor, List<BaseIndexStrategy<T>> indexes) throws FileNotFoundException {
    this.database = new Database<>(constructor);
    this.constructor = constructor;

    this.csvReader = new CSVReader<T>();
    this.sort = new Sort<T>(database, constructor);

    this.index = new IndexController<>(indexes, this.database, this.constructor);
		this.compression = new CompressionController();
		this.pattern = new PatternController();

    this.cryptography = new CryptographyController();
  }

  public void readFromCSV(String csvPath) {
    index.clear();

    try {
      this.csvReader.readCSV(csvPath, this.database, this.constructor);
    } catch (FileNotFoundException e) {
      System.out.println("Erro ao ler o arquivo CSV. Verifique o caminho informado.");
      e.printStackTrace();
    }

    index.build();
  }

  public void insertRegister(T register) {
    long position = this.database.insert(register);

    index.add(register, position);
  }

  public Optional<T> searchById(int id) {
    if (index.hasIdIndexAvailable()) {
      long position = index.get(id);

      if (position != -1) {
        return this.database.select(position);
      } else {
        return Optional.empty();
      }
    } else {
      return this.database.select(id);
    }
  }

  public Optional<List<T>> searchByFields(String[] fields, String[] values) {
    if (fields.length != values.length) {
      System.out.println("Erro ao buscar registros. O número de campos e valores informados é diferente.");
      return Optional.empty();
    }

    List<T> registers = new ArrayList<>();
    Optional<List<Integer>> ids = index.get(fields, values);

    if (ids.isPresent()) {
      ids.get().sort((a, b) -> a - b);

      for (int id : ids.get()) {
        Optional<T> register = database.select(id);

        if (register.isPresent()) {
          registers.add(register.get());
        }
      }

      return Optional.of(registers);
    }

    return Optional.empty();
  }

  public Optional<T> updateRegister(int id, T newRegister) {
    if (index.hasIdIndexAvailable()) {
      long position = index.get(id);

      if (position != -1) {
        Optional<Long> newPosition = this.database.update(position, newRegister);

        if (newPosition.isPresent()) {
          index.update(newRegister, newPosition.get());
          return Optional.of(newRegister);
        }
      }

      return Optional.empty();
    } else {
      return this.database.update(id, newRegister);
    }
  }

  public Optional<T> deleteRegister(int id) {
    if (index.hasIdIndexAvailable()) {
      long position = index.get(id);

      if (position != -1) {
        Optional<T> register = this.database.delete(position);

        if (register.isPresent()) {
          index.remove(register.get());
          return register;
        }
      }

      return Optional.empty();
    } else {
      return this.database.delete(id);
    }
  }

	// sort
  public boolean sort(int pathsNumber, int inMemoryRegisters) {
    boolean sorted = this.sort.sort(pathsNumber, inMemoryRegisters);

    index.rebuild();

    return sorted;
	}

	// index
	public Optional<List<IndexStrategy<T>>> getAvailableIndexes() {
		return Optional.of(this.index.getIndexes());
	}

	public Optional<List<InvertedIndexStrategy<T>>> getAvailableInvertedIndexes() {
		return Optional.of(this.index.getInvertedIndexes());
	}

	// compression
	public Optional<List<CompressionStats>> compressFile(String filePath) {
		return this.compression.compress(filePath);
	}

	public Optional<CompressionStats> decompressFile(String filePath) {
		return this.compression.decompress(filePath);
	}

	public Optional<List<String>> getCompressionSupportedExtensions() {
		return this.compression.getSupportedExtensions();
	}

	/**
	 * Search for a pattern and return the registers that match the pattern.
	 */
	public Optional<List<T>> searchPattern(String pattern) {
		Optional<List<Long>> positions = this.pattern.search(this.database.getFile(), pattern);

		if (positions.isPresent()) {
			List<T> registers = new ArrayList<>();

			for (long position : positions.get()) {
				Optional<T> register = RegisterUtil.getRegister(position, this.database.getFile(), this.constructor);

				register.ifPresent(r -> {
					if (registers.stream().noneMatch(existingRegister -> existingRegister.getId() == r.getId())) {
						registers.add(r);
					}
				});
			}

			return Optional.of(registers);
		}

		return Optional.empty();
	}

  // cryptography
	public void encryptFile(String filePath) {
    this.cryptography.encrypt(filePath);
	}

	public void decryptFile(String filePath) {
    this.cryptography.decrypt(filePath);
	}

	public Optional<List<String>> getCryptographySupportedExtensions() {
    return this.cryptography.getSupportedExtensions();
	} 
}
