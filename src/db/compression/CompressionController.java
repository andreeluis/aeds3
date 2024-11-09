package db.compression;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import model.interfaces.Compression;
import util.ConfigUtil;

public class CompressionController {
	private List<Compression> compressions;

	public CompressionController() {
		compressions = List.of(new LZW(), new Huffman());
	}

	public List<Compression> getCompressions() {
		return compressions;
	}

	/**
	 * Compresses the file at the given path using all available compressions.
	 *
	 * @param filePath the path to the file to compress
	 * @return a list of compression names, ratios and times for each compression or an empty optional if no compressions were successful
	 */
	public Optional<Map<String, Map<Float, Float>>> compress(String filePath) {
		Map<String, Map<Float, Float>> results = new HashMap<>();

		for (Compression compression : compressions) {
			Optional<Map<Float, Float>> result = compress(filePath, compression);

			if (result.isPresent()) {
				results.put(compression.getName(), result.get());
			}
		}

		return results.isEmpty() ? Optional.empty() : Optional.of(results);
	}

	/**
	 * Decompresses the file at the given path using the appropriate decompression based on the file extension.
	 *
	 * @param filePath the path to the file to decompress
	 * @return a map of decompression name and time
	 */
	public Optional<Map<String, Float>> decompress(String filePath) {
		for (Compression compression : compressions) {
			String extension = compression.getExtension() + ConfigUtil.FILE_EXTENSION;

			if (filePath.endsWith(extension)) {
				Optional<Float> result = decompress(filePath, compression);

				if (result.isPresent()) {
					return Optional.of(Map.of(compression.getName(), result.get()));
				}
			}
		}

		return Optional.empty();
	}

	private Optional<Map<Float, Float>> compress(String filePath, Compression compression) {
		try {
			RandomAccessFile file = new RandomAccessFile(filePath, "r");
			RandomAccessFile compressedFile = new RandomAccessFile(filePath + compression.getExtension(), "rw");

			// start timer
			long startTime = System.nanoTime();

			compression.compress(file, compressedFile);

			long endTime = System.nanoTime();
			float compressionTime = (endTime - startTime) / 1_000_000.0f;
			float compressionRatio = file.length() / (float) compressedFile.length();

			file.close();
			compressedFile.close();

			return Optional.of(Map.of(compressionRatio, compressionTime));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<Float> decompress(String filePath, Compression compression) {
		try {
			String decompressedFilePath = filePath.replace(compression.getExtension(), ConfigUtil.FILE_EXTENSION);

			RandomAccessFile file = new RandomAccessFile(filePath, "r");
			RandomAccessFile decompressedFile = new RandomAccessFile(decompressedFilePath, "rw");

			// start timer
			long startTime = System.nanoTime();

			compression.decompress(file, decompressedFile);

			long endTime = System.nanoTime();
			float decompressionTime = (endTime - startTime) / 1_000_000.0f;

			file.close();
			decompressedFile.close();

			return Optional.of(decompressionTime);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}
