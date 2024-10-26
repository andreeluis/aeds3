package util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RAF {
  public static boolean isEOF(RandomAccessFile file) throws IOException {
    return file.getFilePointer() >= file.length();
  }
}
