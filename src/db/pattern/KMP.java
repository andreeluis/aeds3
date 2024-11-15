package db.pattern;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.interfaces.PatternMatching;

public class KMP implements PatternMatching {
	@Override
	public String getName() {
		return "Knuth-Morris-Pratt";
	}

	@Override
	public Optional<List<Long>> search(RandomAccessFile file, byte[] pattern) throws IOException {
		long fileSize = file.length();
		int patternLength = pattern.length;

		ArrayList<Long> results = new ArrayList<>();

		int[] lps = buildLongestPrefixSuffix(pattern);

		long i = 0;
		int j = 0;

		while (i < fileSize) {
			file.seek(i);
			int fileByte = file.read();

			if (fileByte == -1) {
				break;
			}

			// If characters match, move both pointers forward
			if (fileByte == pattern[j]) {
				i++;
				j++;

				// If the entire pattern is matched store the start index in result
				if (j == patternLength) {
					results.add(i - j);

					// Use LPS of previous index to skip unnecessary comparisons
					j = lps[j - 1];
				}
			} else {
				if (j != 0) {
					j = lps[j - 1];
				} else {
					i++;
				}
			}
		}

		return results.isEmpty() ? Optional.empty() : Optional.of(results);
	}

	private int[] buildLongestPrefixSuffix(byte[] pattern) {
		int lenght = 0;
		int[] longestPrefixSuffix = new int[pattern.length];
		longestPrefixSuffix[0] = 0;

		int i = 1;
		while (i < pattern.length) {
			if (pattern[i] == pattern[lenght]) {
				lenght++;
				longestPrefixSuffix[i] = lenght;
				i++;
			} else {
				if (lenght != 0) {
					lenght = longestPrefixSuffix[lenght - 1];
				} else {
					longestPrefixSuffix[i] = 0;
					i++;
				}
			}
		}

		return longestPrefixSuffix;
	}
}
