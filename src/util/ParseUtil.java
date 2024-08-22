package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtil {
  public static String[] parseCSVLine(String line) {
    String[] fields = new String[16];

    String regex = "(?:\"([^\"]*)\"|([^,\n]*))(?:,|\n|$)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(line);

    int index = 0;
    while (matcher.find() && index < fields.length) {
      fields[index] = matcher.group(1);

      if (fields[index] == null) {
        fields[index] = matcher.group(2);
      }

      index++;
    }

    return fields;
  }

  public static int parseInt(String value) {
    try {
      return Integer.parseInt(value.replace(",", "").trim());
    } catch (NumberFormatException e) {
      return -1; // Valor padrão em caso de erro
    }
  }
}
