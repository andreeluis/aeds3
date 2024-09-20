package index;

import db.Database;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ExtendedHash implements IndexStrategy {

  // Estrutura arquivo diretorio - [p_global | [num_bucket | end_bucket ...]]
  // Estrutura arquivo bucket - [p_local | qtd_bucket | [id | end_reg ...] ...]

  private static String hashFilePath;
  private static final String dirFileName = "ExtendedHashDir"
  private static final String bucketFileName = "ExtendedHashBuket"

  public String getHashFilePath() {
    return hashFilePath;
  }
  public void setHashFilePath(String hashFilePath) {
    this.hashFilePath = hashFilePath;
  }

  public ExtendedHash(String hashFilePath) {
    setHashFilePath(hashFilePath);
  }

  @Override
  public void build(Database database) throws FileNotFoundException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'build'");
  }

  @Override
  public void add(int id, long position) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'add'");
  }

  @Override
  public long get(int id) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'get'");
  }

  @Override
  public void remove(int id) throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  public void clear() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'clear'");
  }
}
