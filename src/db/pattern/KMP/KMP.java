package db.pattern.KMP;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.interfaces.PatternMatching;

public class KMP implements PatternMatching {

  private int[] constructLps(byte[] pattern) {
    int len = 0;
    int[] lps = new int[pattern.length];
    lps[0] = 0;

    int i = 1;
    while (i < pattern.length) {
      if (pattern[i] == pattern[len]) {
        len++;
        lps[i] = len;
        i++;
      } else {
        if (len != 0) {
          len = lps[len - 1];
        } else {
          lps[i] = 0;
          i++;
        }
      }
    }

    return lps;
  }

  @Override
  public Optional<List<Long>> search(RandomAccessFile file, byte[] pattern) {
    try {
      long fileSize = file.length();
      int patternLength = pattern.length;

      ArrayList<Long> results = new ArrayList<>();

      int[] lps = constructLps(pattern);

      long i = 0;
      int j = 0;

      while (i < fileSize) {
        file.seek(i);
        int fileByte = file.read();

        if (fileByte == -1)
          break;
        // If characters match, move both pointers forward
        if (fileByte == pattern[j]) {
          i++;
          j++;

          // If the entire pattern is matched
          // store the start index in result
          if (j == patternLength) {
            results.add(i - j);

            // Use LPS of previous index to
            // skip unnecessary comparisons
            j = lps[j - 1];
          }
        } else {
          if (j != 0)
            j = lps[j - 1];
          else
            i++;
        }
      }

      return Optional.of(results);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return Optional.of(new ArrayList<>());
    }
  }

}
