package db.compression;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import db.Database;
import model.interfaces.Compression;

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
	 * @return a list of compression ratios and times for each compression
	 */
	public Optional<List<Map<String, Map<Float, Float>>>> compress(String filePath) {
		List<Map<String, Map<Float, Float>>> results = new ArrayList<>();

		for (Compression compression : compressions) {
			Optional<Map<Float, Float>> result = compress(filePath, compression);

			if (result.isPresent()) {
				results.add(Map.of(compression.getName(), result.get()));
			}
		}

		return results.isEmpty() ? Optional.empty() : Optional.of(results);
	}

	/**
	 * Decompresses the file at the given path using all available compressions.
	 *
	 * @param filePath the path to the file to decompress
	 * @return a list of times for each decompression
	 */
	public Optional<List<Float>> decompress(String filePath) {
		List<Float> results = new ArrayList<>();

		for (Compression compression : compressions) {
			String extension = compression.getExtension() + Database.getExtension();

			if (filePath.endsWith(extension)) {
				Optional<Float> result = decompress(filePath, compression);

				if (result.isPresent()) {
					results.add(result.get());
				}
			}
		}

		return results.isEmpty() ? Optional.empty() : Optional.of(results);
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
			String decompressedFilePath = filePath.replace(compression.getExtension(), Database.getExtension());

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