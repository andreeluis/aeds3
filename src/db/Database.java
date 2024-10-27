package db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.Optional;

import model.Register;
import util.RAF;
import util.RegisterUtil;

/**
 * The database file is a sequential file with the following structure:
 * LAST_ID; [tombstone1; registerLength1; register1]; [tombstone2;
 * registerLength2; register2]; ...
 *
 * @param <T> Type of the register
 */
public class Database<T extends Register> {
  private String filePath;
  private RandomAccessFile file;

  private Constructor<T> constructor;

  // fileExtension
  public static String getFileExtension() {
    return ".aeds3";
  }

  // filePath
  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  // file
  public RandomAccessFile getFile() {
    return this.file;
  }

  // constructor
  public Database(String filePath, Constructor<T> constructor) throws FileNotFoundException {
    setFilePath(filePath);
    this.constructor = constructor;

    String dbFilePath = this.filePath + "dados" + getFileExtension();
    this.file = new RandomAccessFile(dbFilePath, "rw");

    try {
      if (file.length() == 0) {
        file.writeInt(-1);
        file.seek(0);
      }
    } catch (IOException e) {
      System.out.println("Erro ao criar o arquivo de banco de dados.");
      System.out.println(e);
    }
  }

  /**
   * Inserts a new register in the database.
   * @param register
   * @return the position of the inserted register or -1 if the register was not inserted.
   */
  public long insert(T register) {
    try {
      // reads the lastId and assigns it to the new register
      file.seek(0);
      int lastId = file.readInt();
      register.setId(++lastId);

      // updates lastId
      file.seek(0);
      file.writeInt(lastId);

      // save positon to insert and go to end of file
      long position = file.length();
      file.seek(position);

      // write register
      byte[] byteArrayRegister = register.toByteArray();
      file.writeBoolean(false); // tombstone
      file.writeInt(byteArrayRegister.length); // registerLength
      file.write(byteArrayRegister); // register

      return position;
    } catch (IOException e) {
      System.out.println("Erro ao escrever novo registro.");
      System.out.println(e);

      return -1;
    }
  }

  /**
   * Selects a register by its ID.
   * @param id
   * @return the register or an empty optional if the register was not found.
   */
  public Optional<T> select(int id) {
    try {
      // reads lastId
      file.seek(0);
      int lastId = file.readInt();

      // early return
      if (id > lastId) {
        return Optional.empty();
      }

      boolean found = false;

      while (!found && !RAF.isEOF(file)) {
        Optional<T> register = RegisterUtil.getNextValidRegister(file, constructor);

        if (register.isPresent() && register.get().getId() == id) {
          found = true;
          return Optional.of(register.get());
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao buscar registro.");
      System.out.println(e);
    }

    return Optional.empty();
  }

  /**
   * Selects a register by its position.
   * @param position
   * @return the register or an empty optional if the register was not found.
   */
  public Optional<T> select(long position) {
    try {
      file.seek(position);

      Optional<T> register = RegisterUtil.getNextRegister(file, constructor);

      if (register.isPresent()) {
        return register;
      }
    } catch (IOException e) {
      System.out.println("Erro ao buscar registro.");
      e.printStackTrace();
    }

    return Optional.empty();
  }

  /**
   * Update a register by its ID.
   * @param id the ID of the register to be updated.
   * @param newRegister the new register to be written in the position.
   * @return
   */
  public Optional<T> update(int id, T newRegister) {
    try {
      // reads lastId
      file.seek(0);
      int lastId = file.readInt();

      // early return
      if (id > lastId) {
        return Optional.empty();
      }

      // find register position
      Optional<Long> registerPosition = getRegisterPosition(id);

      if (registerPosition.isPresent() && update(registerPosition.get(), newRegister).isPresent()) {
        return Optional.of(newRegister);
      }
    } catch (IOException e) {
      System.err.println("Erro ao atualizar registro de ID: " + id);
      e.printStackTrace();
    }

    return Optional.empty();
  }

  /**
   * Find the position of a register by its ID.
   * @param id the ID of the register to be found.
   * @return the position of the register's tombstone or an empty optional if the register was not found.
   */
  private Optional<Long> getRegisterPosition(int id) {
    long position = -1;
    boolean found = false;

    try {
      while (!found && !RAF.isEOF(file)) {
        position = file.getFilePointer();

        Optional<T> register = RegisterUtil.getNextRegister(file, constructor);

        if (register.isPresent() && register.get().getId() == id) {
          found = true;
        }
      }
    } catch (IOException e) {
      System.out.println("Erro ao buscar a posição do registro.");
      e.printStackTrace();
    }

    return found ? Optional.of(position) : Optional.empty();
  }

  /**
   * Update a register by its position in the file.
   * @param registerPosition the position of the register to be updated.
   * @param newRegister the new register to be written in the position.
   * @return the position of the updated register or an empty optional if the register was not updated.
   */
  public Optional<Long> update(long registerPosition, T newRegister) {
    try {
      byte[] newByteArrayRegister = newRegister.toByteArray();
      int newRegisterLength = newByteArrayRegister.length;
      long position = registerPosition;

      // Vai para a posição do registro
      file.seek(position);
      file.readBoolean(); // tombstone
      int registerLength = file.readInt(); // registerLength

      if (newRegisterLength > registerLength) {
        // set tombstone to true and go to the end of file
        file.seek(position);
        file.writeBoolean(true);

        long newPosition = file.length();
        file.seek(newPosition);

        // write new register at the end of file
        file.writeBoolean(false); // tombstone
        file.writeInt(newByteArrayRegister.length); // newRegisterLength
        file.write(newByteArrayRegister); // register

        return Optional.of(newPosition);
      } else {
        file.seek(position);
        file.writeBoolean(false); // tombstone
        file.writeInt(registerLength); // registerLength
        file.write(newByteArrayRegister); // register

        return Optional.of(position);
      }
    } catch (IOException e) {
      System.out.println("Erro ao atualizar registro.");
      e.printStackTrace();

      return Optional.empty();
    }
  }

  /**
   * Delete a register by its ID.
   * @param id the ID of the register to be deleted.
   * @return the deleted register or an empty optional if the register was not deleted.
   */
  public Optional<T> delete(int id) {
    try {
      // Lê o último ID
      file.seek(0);
      int lastId = file.readInt();

      // Retorno antecipado se o ID for maior que o último ID
      if (id > lastId) {
        return Optional.empty();
      }

      // Encontra a posição do registro
      Optional<Long> registerPosition = getRegisterPosition(id);

      if (registerPosition.isPresent()) {
        return delete(registerPosition.get());
      }
    } catch (IOException e) {
      System.err.println("Erro ao excluir registro para ID: " + id);
      e.printStackTrace();
    }

    return Optional.empty();
  }

  /**
   * Delete a register by its position in the file.
   * @param registerPosition the position of the register to be deleted.
   * @return the deleted register or an empty optional if the register was not deleted.
   */
  public Optional<T> delete(long registerPosition) {
    try {
      file.seek(registerPosition);

      Optional<T> register = RegisterUtil.getNextRegister(file, constructor);

      if (register.isPresent()) {
        file.seek(registerPosition);
        file.writeBoolean(true); // Marca o registro como excluído

        return register;
      }
    } catch (IOException e) {
      System.out.println("Erro ao excluir registro.");
      e.printStackTrace();
    }

    return Optional.empty();
  }
}
