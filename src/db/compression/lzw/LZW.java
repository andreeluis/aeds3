package db.compression.lzw;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.interfaces.Compression;
import util.ConfigUtil;

public class LZW implements Compression {
	private static final int MAX_DICTIONARY_SIZE = 4096; // 2^12

	@Override
	public String getName() {
		return "LZW";
	}

	@Override
	public String getExtension() {
		return ".lzw" + ConfigUtil.FILE_EXTENSION;
	}

	/**
	 * Compresses a file using the LZW algorithm
	 *
	 * @param source      The file to compress
	 * @param destination The compressed file
	 */
	@Override
	public void compress(RandomAccessFile source, RandomAccessFile destination) throws IOException {
		// Initialize the dictionary with all possible single-character strings
		Map<List<Byte>, Integer> dictionary = new HashMap<>();
		for (int i = 0; i < 256; i++) {
			dictionary.put(List.of((byte) i), i);
		}

		// Read the input file in buffers and compress using the LZW algorithm
		List<Integer> compressedData = new ArrayList<>();
		List<Byte> w = new ArrayList<>();
		byte[] buffer = new byte[ConfigUtil.BUFFER_SIZE];
		int bytesRead;

		while ((bytesRead = source.read(buffer)) != -1) {
			for (int i = 0; i < bytesRead; i++) {
				byte k = buffer[i];
				List<Byte> wk = new ArrayList<>(w);
				wk.add(k);

				if (dictionary.containsKey(wk)) {
					w = wk;
				} else {
					compressedData.add(dictionary.get(w));

					if (dictionary.size() < MAX_DICTIONARY_SIZE) {
						dictionary.put(wk, dictionary.size());
					}

					w = List.of(k);
				}
			}
		}
		
		if (!w.isEmpty()) {
			compressedData.add(dictionary.get(w));
		}

		// Write the compressed data to the output file using 12-bit codes
		writeCompressedData(destination, compressedData);
	}

	private void writeCompressedData(RandomAccessFile destination, List<Integer> compressedData) throws IOException {
		int buffer = 0;
		int bufferSize = 0;

		for (int code : compressedData) {
			buffer = (buffer << 12) | code;
			bufferSize += 12;

			while (bufferSize >= 8) {
				bufferSize -= 8;
				destination.write(buffer >> bufferSize);
				buffer &= (1 << bufferSize) - 1;
			}
		}

		if (bufferSize > 0) {
			destination.write(buffer << (8 - bufferSize));
		}
	}

	/**
	 * Decompresses a file using the LZW algorithm
	 *
	 * @param source      The file to decompress
	 * @param destination The decompressed file
	 */
	@Override
	public void decompress(RandomAccessFile source, RandomAccessFile destination) throws IOException {
		// Initialize the dictionary with all possible single-character strings
		List<List<Byte>> dictionary = new ArrayList<>();
		for (int i = 0; i < 256; i++) {
			dictionary.add(List.of((byte) i));
		}

		// Read the compressed data from the input file in buffers
		List<Integer> compressedData = new ArrayList<>();
		readCompressedData(source, compressedData);

		// Decompress the data using the LZW algorithm
		List<Byte> w = new ArrayList<>(dictionary.get(compressedData.remove(0)));
		List<Byte> decompressedData = new ArrayList<>(w);
		for (int k : compressedData) {
			List<Byte> entry;
			if (k < dictionary.size()) {
				entry = new ArrayList<>(dictionary.get(k));
			} else if (k == dictionary.size()) {
				entry = new ArrayList<>(w);
				entry.add(w.get(0));
			} else {
				throw new IllegalArgumentException("Bad compressed k: " + k);
			}

			decompressedData.addAll(entry);

			// Add w+entry[0] to the dictionary
			List<Byte> newEntry = new ArrayList<>(w);
			newEntry.add(entry.get(0));
			dictionary.add(newEntry);

			w = entry;
		}

		// Write the decompressed data to the output file
		for (byte b : decompressedData) {
			destination.write(b);
		}
	}

	private void readCompressedData(RandomAccessFile source, List<Integer> compressedData) throws IOException {
		int buffer = 0;
		int bufferSize = 0;

		while (source.getFilePointer() < source.length()) {
			buffer = (buffer << 8) | (source.read() & 0xFF);
			bufferSize += 8;

			if (bufferSize >= 12) {
				bufferSize -= 12;
				compressedData.add(buffer >> bufferSize);
				buffer &= (1 << bufferSize) - 1;
			}
		}
	}
}
