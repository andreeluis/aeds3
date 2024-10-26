package db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import model.Register;
import model.RegisterPosition;

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
  // private Index index;

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
  // public Database(String filePath, List<IIndex> indexes, Constructor<T> constructor) throws FileNotFoundException {
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

    // try {
    // this.index = new Index(this, indexes);
    // } catch (IOException e) {
    // System.out.println("Erro ao criar o índice.");
    // System.out.println(e);
    // }
  }

  public void insert(T register) {
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

      // TODO - add to indexes
      // if (index.isAvailable()) {
      // index.add(register, position);
      // }
    } catch (IOException e) {
      System.out.println("Erro ao escrever novo registro.");
      System.out.println(e);
    }
  }

  public Optional<T> select(int id) {
    try {
      // reads lastId
      file.seek(0);
      int lastId = file.readInt();

      // early return
      if (id > lastId) {
        return Optional.empty();
      }

      // if (index.isAvailable()) {
      // long position = index.get(id);

      // if (position != -1) {
      // T register = constructor.newInstance();

      // file.seek(position);
      // file.readBoolean(); // skip tombstone (always false)
      // int registerLength = file.readInt();

      // byte[] byteArrayRegister = new byte[registerLength];
      // file.read(byteArrayRegister);

      // register.fromByteArray(byteArrayRegister);
      // return Optional.of(register);
      // }
      // } else {
      boolean found = false;

      while (!found && !isEndOfFile()) {
        boolean tombstone = file.readBoolean();
        int registerLength = file.readInt();

        if (!tombstone) {
          byte[] byteArrayRegister = new byte[registerLength];
          file.read(byteArrayRegister);
          T register = constructor.newInstance();
          register.fromByteArray(byteArrayRegister);

          if (register.getId() == id) {
            found = true;
            return Optional.of(register);
          }
        } else {
          file.skipBytes(registerLength);
        }
      }
      // }
    } catch (IOException e) {
      System.out.println("Erro ao buscar registro.");
      System.out.println(e);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      System.out.println("Erro ao buscar registro usando índice.");
      e.printStackTrace();
    }

    return Optional.empty();
  }

  // public Optional<List<T>> select() {
  // List<T> registers = new ArrayList<>();

  // return Optional.of(registers);
  // }

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
      Optional<RegisterPosition> registerPosition = findRegisterPosition(id);

      if (registerPosition.isPresent()) {
        return updateRegister(registerPosition.get(), newRegister);
      }
    } catch (IOException e) {
      System.err.println("Erro ao atualizar registro para ID: " + id);
      e.printStackTrace();
    }

    return Optional.empty();
  }

  private Optional<RegisterPosition> findRegisterPosition(int id) throws IOException {
    long position = -1;
    int registerLength = -1;
    boolean found = false;

    try {
      // if (index.isAvailable()) {
      // // Se o índice estiver disponível, obtém a posição a partir do índice
      // position = index.get(id);

      // if (position != -1) {
      // file.seek(position);
      // file.readBoolean(); // skip tombstone (always false)
      // registerLength = file.readInt();

      // byte[] byteArrayRegister = new byte[registerLength];
      // file.read(byteArrayRegister);

      // T register = constructor.newInstance();
      // register.fromByteArray(byteArrayRegister);
      // found = register.getId() == id;
      // }
      // } else {
      // Se não houver índice, realiza busca sequencial
      while (!found && !isEndOfFile()) {
        position = file.getFilePointer();
        boolean tombstone = file.readBoolean();
        registerLength = file.readInt();

        if (!tombstone) {
          byte[] byteArrayRegister = new byte[registerLength];
          file.read(byteArrayRegister);

          T register = constructor.newInstance();
          register.fromByteArray(byteArrayRegister);
          found = register.getId() == id;
        } else {
          file.skipBytes(registerLength);
        }
      }
      // }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      System.out.println("Erro ao buscar registro.");
      e.printStackTrace();
    }

    return found ? Optional.of(new RegisterPosition(position, registerLength)) : Optional.empty();
  }

  private Optional<T> updateRegister(RegisterPosition registerPosition, T newRegister) throws IOException {
    long position = registerPosition.getPosition();
    int registerLength = registerPosition.getLength();

    byte[] newByteArrayRegister = newRegister.toByteArray();
    int newLength = newByteArrayRegister.length;

    // Vai para a posição do registro
    file.seek(position);

    if (newLength > registerLength) {
      // set tombstone to true and go to the end of file
      file.writeBoolean(true);
      long newPosition = file.length();
      file.seek(newPosition);

      // write new register at the end of file
      file.writeBoolean(false); // tombstone
      file.writeInt(newByteArrayRegister.length); // newRegisterLength
      file.write(newByteArrayRegister); // register

      // updates index
      // if (index.isAvailable()) {
      // index.update(newRegister, newPosition);
      // }
    } else {
      file.writeBoolean(false); // tombstone
      file.writeInt(registerLength); // registerLength
      file.write(newByteArrayRegister); // register
    }

    return Optional.of(newRegister);
  }

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
      Optional<RegisterPosition> registerPosition = findRegisterPosition(id);

      if (registerPosition.isPresent()) {
        return deleteRegister(registerPosition.get());
      }
    } catch (IOException e) {
      System.err.println("Erro ao excluir registro para ID: " + id);
      e.printStackTrace();
    }

    return Optional.empty();
  }

  private Optional<T> deleteRegister(RegisterPosition registerPosition) throws IOException {
    try {
      long position = registerPosition.getPosition();

      // Vai para a posição do registro
      file.seek(position);
      file.writeBoolean(true); // Marca o registro como excluído

      // Lê o registro excluído
      int registerLength = file.readInt();
      byte[] byteArrayRegister = new byte[registerLength];
      file.read(byteArrayRegister);

      T register = constructor.newInstance();
      register.fromByteArray(byteArrayRegister);

      // Remove do índice, se disponível
      // if (index.isAvailable()) {
      //   index.remove(register);
      // }

      return Optional.of(register);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      System.out.println("Erro ao excluir registro.");
      e.printStackTrace();
    }

    return Optional.empty();
  }

  public boolean isEndOfFile() throws IOException {
    return !(file.getFilePointer() < file.length());
  }

  public boolean isEmpty() throws IOException {
    return file.length() <= Integer.BYTES;
  }
}
