package db.compression;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.CompressionStats;
import model.interfaces.Compression;
import util.ConfigUtil;

public class CompressionController {
	private List<Compression> compressions;

	public CompressionController() {
		//compressions = List.of(new LZW(), new Huffman());
		compressions = List.of(new Huffman());
	}

	public List<Compression> getCompressions() {
		return compressions;
	}

	/**
	 * Compresses the file at the given path using all available compressions.
	 *
	 * @param filePath the path to the file to compress
	 * @return a list of CompressionStats objects containing the statistics for each compression or an empty optional if no compressions were successful
	 */
	public Optional<List<CompressionStats>> compress(String filePath) {
		List<CompressionStats> results = new ArrayList<>();

		for (Compression compression : compressions) {
			Optional<CompressionStats> result = compress(filePath, compression);

			if (result.isPresent()) {
				results.add(result.get());
			}
		}

		return results.isEmpty() ? Optional.empty() : Optional.of(results);
	}

	/**
	 * Decompresses the file at the given path using the appropriate decompression based on the file extension.
	 *
	 * @param filePath the path to the file to decompress
	 * @return a CompressionStats object containing the statistics for the decompression or an empty optional if no decompression was successful
	 */
	public Optional<CompressionStats> decompress(String filePath) {
		for (Compression compression : compressions) {
			String extension = compression.getExtension();

			if (filePath.endsWith(extension)) {
				Optional<CompressionStats> result = decompress(filePath, compression);

				return result.isPresent() ? result : Optional.empty();
			}
		}

		return Optional.empty();
	}

	private Optional<CompressionStats> compress(String filePath, Compression compression) {
		try {
			String compressedFilePath = filePath.replace(ConfigUtil.FILE_EXTENSION, compression.getExtension());

			RandomAccessFile file = new RandomAccessFile(filePath, "r");
			RandomAccessFile compressedFile = new RandomAccessFile(compressedFilePath, "rw");

			// start timer
			long startTime = System.nanoTime();

			compression.compress(file, compressedFile);

			long endTime = System.nanoTime();
			long compressionTime = endTime - startTime;

			CompressionStats compressionStats = new CompressionStats(compression.getName(), file.length(), compressedFile.length(), compressionTime);

			file.close();
			compressedFile.close();

			return Optional.of(compressionStats);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private Optional<CompressionStats> decompress(String filePath, Compression compression) {
		try {
			String decompressedFilePath = filePath.replace(compression.getExtension(), ConfigUtil.FILE_EXTENSION);

			RandomAccessFile file = new RandomAccessFile(filePath, "r");
			RandomAccessFile decompressedFile = new RandomAccessFile(decompressedFilePath, "rw");

			// start timer
			long startTime = System.nanoTime();

			compression.decompress(file, decompressedFile);

			long endTime = System.nanoTime();
			long decompressionTime = endTime - startTime;

			CompressionStats compressionStats = new CompressionStats(compression.getName(), decompressionTime);

			file.close();
			decompressedFile.close();

			return Optional.of(compressionStats);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}
