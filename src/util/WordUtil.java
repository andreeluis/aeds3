package util;

import java.util.Set;

public class WordUtil {
  private static Set<String> invalidWords = Set.of(
      "about", "which", "there", "after", "before", "while", "their", "would", "could", "should", "these", "those",
      "where", "since", "being", "under", "other", "every", "first", "second", "third", "around", "among", "again",
      "between", "because", "during", "through", "against", "within", "without", "across", "toward", "having", "might",
      "until", "always", "almost", "anyone", "anything", "behind", "beside", "beyond", "cannot", "coming", "either",
      "enough", "except", "following", "further", "herself", "himself", "inside", "itself", "likely", "little",
      "making", "myself", "nothing", "nowhere", "others", "outside", "people", "rather", "seeing", "taking", "thanks",
      "things", "though", "unless", "wanted", "wanting", "wholly", "yourself", "ourselves", "anybody", "everybody",
      "someone", "everyone", "whatever", "wherever", "however", "whenever", "although", "whereby", "neither",
      "nonetheless", "besides", "themselves", "whose", "anymore", "hence", "notwithstanding", "therefore", "whereas",
      "furthermore", "moreover", "whereupon", "nevertheless", "regardless", "whoever");

  public static boolean isWordValid(String word) {
    return word != null && word.length() > 4 && !invalidWords.contains(word);
  }
}
