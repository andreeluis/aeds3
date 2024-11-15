package db.pattern;

import java.io.RandomAccessFile;
import java.util.List;
import java.util.Optional;

import db.pattern.KMP.KMP;
import model.interfaces.PatternMatching;

public class PatternController {
	List<PatternMatching> patternMatchings;

	public PatternController() {
		patternMatchings = List.of(new KMP()); // TODO: Add all pattern matching algorithms
	}

	/**
	 * Searches for the given pattern in the file using all available pattern matching algorithms.
	 *
	 * @param file 			the file to search
	 * @param pattern 	the pattern to search for
	 * @return a list of the positions of the pattern in the file
	 */
	public Optional<List<Long>> search(RandomAccessFile file, String pattern) {
		List<Long> results = null;

		for (PatternMatching patternMatching : patternMatchings) {
			Optional<List<Long>> result = patternMatching.search(file, pattern.getBytes());

			if (result.isPresent()) {
				results = result.get();
			}
		}

		return results == null ? Optional.empty() : Optional.of(results);
	}
}
