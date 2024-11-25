package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileUtil {
	/**
	 * Get all files in the configured directory with the configured extension
	 *
	 * @return a list of files with the configured extension
	 */
	public static Optional<List<String>> getAllFiles() {
		return getAllFiles(ConfigUtil.FILE_EXTENSION);
	}

	/**
	 * Get all files in the configured directory with the given extension
	 *
	 * @param extension the extension of the files to be listed
	 *
	 * @return a list of files with the given extension
	 */
	public static Optional<List<String>> getAllFiles(String extension) {
		File folder = new File(ConfigUtil.DB_PATH);

		List<String> files = Arrays.stream(folder.listFiles((dir, name) -> name.endsWith(extension)))
				.map(File::getName)
				.collect(Collectors.toList());

		return files.isEmpty() ? Optional.empty() : Optional.of(files);
	}

	/**
	 * Get all files in the configured directory with the given extensions
	 *
	 * @param extensions the extensions of the files to be listed
	 *
	 * @return a list of files with the given extensions
	 */
	public static Optional<List<String>> getAllFiles(List<String> extensions) {
		List<String> files = new ArrayList<>();

		for (String extension : extensions) {
			Optional<List<String>> filesWithExtension = getAllFiles(extension);

			if (filesWithExtension.isPresent()) {
				files.addAll(filesWithExtension.get());
			}
		}

		return files.isEmpty() ? Optional.empty() : Optional.of(files);
	}
}
