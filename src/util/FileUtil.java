package util;

import java.io.File;
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
		File folder = new File(ConfigUtil.DB_PATH);

		List<String> files = Arrays.stream(folder.listFiles((dir, name) -> name.endsWith(ConfigUtil.FILE_EXTENSION)))
				.map(File::getName)
				.collect(Collectors.toList());

		return files.isEmpty() ? Optional.empty() : Optional.of(files);
	}
}
