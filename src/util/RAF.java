package util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RAF {
  /**
   * Check if the file pointer is at the end of the file.
   * @throws IOException
   */
  public static boolean isEOF(RandomAccessFile file) throws IOException {
    return file.getFilePointer() >= file.length();
  }
}
