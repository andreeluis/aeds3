package db.pattern;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import model.interfaces.PatternMatching;

public class BoyerMoore implements PatternMatching {
	@Override
	public String getName() {
		return "Boyer-Moore";
	}

	/**
	 * @param file    the file to search the pattern
	 * @param pattern the pattern to search
	 *
	 * @return a list with the positions of the pattern in the file or empty
	 *         optional if the pattern is not found
	 */
	@Override
	public Optional<List<Long>> search(RandomAccessFile file, byte[] pattern) throws IOException {
		Map<Byte, Integer> badCharacterHeuristic = buildBadCharacter(pattern);
		int[] goodSuffixHeuristic = buildGoodSuffix(pattern);

		List<Long> patternMatches = new ArrayList<>();
		long fileLength = file.length();
		long bytesRead = 0;

		while (bytesRead <= fileLength - pattern.length) {
			byte[] buffer = new byte[pattern.length];
			file.seek(bytesRead);
			file.read(buffer);

			int i = pattern.length - 1;
			while (i >= 0 && buffer[i] == pattern[i]) {
				i--;
			}

			if (i < 0) {
				patternMatches.add(bytesRead);
				bytesRead += (bytesRead + pattern.length < fileLength)
						? pattern.length - badCharacterHeuristic.getOrDefault(buffer[pattern.length - 1], -1)
						: 1;
			} else {
				int badCharacterShift = badCharacterHeuristic.getOrDefault(buffer[i], -1);
				int goodSuffixShift = goodSuffixHeuristic[i];
				bytesRead += Math.max(goodSuffixShift, i - badCharacterShift);
			}
		}

		return patternMatches.isEmpty() ? Optional.empty() : Optional.of(patternMatches);
	}

	/**
	 * @param pattern the pattern to build the bad character heuristic
	 *
	 * @return a map with the bad character heuristic
	 */
	private Map<Byte, Integer> buildBadCharacter(byte[] pattern) {
		Map<Byte, Integer> badCharacter = new HashMap<>();

		for (int i = 0; i < pattern.length - 1; i++) {
			badCharacter.put(pattern[i], i);
		}

		return badCharacter;
	}

	/**
	 * @param pattern the pattern to build the good suffix heuristic
	 *
	 * @return an array with the good suffix heuristic
	 */
	private int[] buildGoodSuffix(byte[] pattern) {
		int[] goodSuffix = new int[pattern.length];
		int lastPrefixPosition = pattern.length;

		for (int i = pattern.length - 1; i >= 0; i--) {
			if (isPrefix(pattern, i + 1))
				lastPrefixPosition = i + 1;
			goodSuffix[i] = lastPrefixPosition + (pattern.length - 1 - i);
		}

		for (int i = 0; i < pattern.length - 1; i++) {
			int suffixLength = 0;
			int j = i;
			while (j >= 0 && pattern[j] == pattern[pattern.length - 1 - suffixLength]) {
				j--;
				suffixLength++;
			}

			goodSuffix[pattern.length - 1 - suffixLength] = pattern.length - 1 - i + suffixLength;
		}

		return goodSuffix;
	}

	private boolean isPrefix(byte[] pattern, int p) {
		for (int i = p, j = 0; i < pattern.length; i++, j++) {
			if (pattern[i] != pattern[j]) {
				return false;
			}
		}

		return true;
	}
}
