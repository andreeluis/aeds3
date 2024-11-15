package db.pattern;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Optional;

import model.interfaces.PatternMatching;

public class PatternController {
	List<PatternMatching> patternMatchings;
	PatternMatching inUsePattern;

	public List<PatternMatching> getPatternMatchings() {
		return patternMatchings;
	}

	public void setPatternMatching(int index) {
		inUsePattern = patternMatchings.get(index);
	}

	public PatternController() {
		patternMatchings = List.of(new KMP());
		inUsePattern = patternMatchings.get(1);
	}

	/**
	 * Searches for the given pattern in the file using all available pattern matching algorithms.
	 *
	 * @param file 			the file to search
	 * @param pattern 	the pattern to search for
	 * @return a list of the positions of the pattern in the file
	 */
	public Optional<List<Long>> search(RandomAccessFile file, String pattern) {
		Optional<List<Long>> result = Optional.empty();

		try {
			result = inUsePattern.search(file, pattern.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
}
