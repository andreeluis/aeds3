package util;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtil {
  /**
   * Parse a CSV line into an array of strings.
   * @param line the CSV line
   * @return an array of strings
   */
  public static String[] parseCSVLine(String line) {
    String[] fields = new String[16];

    String regex = "(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|([^,\n]*))(?:,|\n|$)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(line);

    int index = 0;
    while (matcher.find() && index < fields.length) {
      fields[index] = matcher.group(1);

      if (fields[index] == null) {
        fields[index] = matcher.group(2);
      }

      // Substitui aspas duplas duplas por uma única aspas dupla
      if (fields[index] != null) {
        fields[index] = fields[index].replace("\"\"", "\"");
      }

      index++;
    }

    return fields;
  }

  /**
   * Parse a string into an integer.
   * @param value the string to parse
   * @return the integer value or -1 if the string is not a valid integer.
   */
  public static int parseInt(String value) {
    try {
      return Integer.parseInt(value.replace(",", "").trim());
    } catch (NumberFormatException e) {
      return -1; // Valor padrão em caso de erro
    }
  }

  /**
   * Parse a string into a long.
   * @param value the string to parse
   * @return the long value or -1 if the string is not a valid long.
   */
  public static long parseLong(String value) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");

    try {
      return formatter.parse(value).getTime();
    } catch (Exception e) {
      return -1;
    }
  }
}
