package db.compression.huffman;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

import model.interfaces.Compression;
import util.ConfigUtil;

public class Huffman implements Compression {
	@Override
	public String getName() {
		return "Huffman";
	}

	@Override
	public String getExtension() {
		return ".huff" + ConfigUtil.FILE_EXTENSION;
	}

	/**
	 * Compresses the file using the Huffman algorithm
	 *
	 * @param source      the file to be compressed
	 * @param destination the compressed file
	 */
	@Override
	public void compress(RandomAccessFile source, RandomAccessFile destination) throws IOException {
		Optional<Map<Byte, Integer>> frequencies = getFrequencies(source);

		// early return
		if (!frequencies.isPresent()) {
			return;
		}

		HuffmanNode root = buildTree(frequencies.get());

		Map<Byte, String> codes = buildCodes(root);

		// Write the frequencies to the compressed file
		destination.writeInt(frequencies.get().size());
		for (Map.Entry<Byte, Integer> entry : frequencies.get().entrySet()) {
			destination.writeByte(entry.getKey());
			destination.writeInt(entry.getValue());
		}

		// Write the compressed data to the compressed file
		source.seek(0);
		int bitBuffer = 0;
		int bitCount = 0;
		int bit;
		while ((bit = source.read()) != -1) {
			String code = codes.get((byte) bit);

			for (int i = 0; i < code.length(); i++) {
				bitBuffer <<= 1;
				bitBuffer |= code.charAt(i) - '0';
				bitCount++;

				if (bitCount == 8) {
					destination.write(bitBuffer);
					bitBuffer = 0;
					bitCount = 0;
				}
			}
		}

		if (bitCount > 0) {
			bitBuffer <<= 8 - bitCount;
			destination.write(bitBuffer);
		}
	}

	/**
	 * Decompresses the file using the Huffman algorithm
	 *
	 * @param source      the file to be decompressed
	 * @param destination the decompressed file
	 */
	@Override
	public void decompress(RandomAccessFile source, RandomAccessFile destination) throws IOException {
		Map<Byte, Integer> frequencies = new HashMap<>();

		int size = source.readInt();
		for (int i = 0; i < size; i++) {
			byte key = source.readByte();
			int value = source.readInt();

			frequencies.put(key, value);
		}

		HuffmanNode root = buildTree(frequencies);

		List<Byte> data = new ArrayList<>();
		HuffmanNode node = root;
		int bit;
		while ((bit = source.read()) != -1) {
			for (int i = 7; i >= 0; i--) {
				int bitValue = (bit >> i) & 1;

				if (bitValue == 0) {
					node = node.getLeft();
				} else {
					node = node.getRight();
				}

				if (node.isLeaf()) {
					data.add(node.getData());
					node = root;
				}
			}
		}

		byte[] bytes = new byte[data.size()];
		for (int i = 0; i < data.size(); i++) {
			bytes[i] = data.get(i);
		}

		destination.write(bytes);
	}

	/**
	 * Returns the frequencies of each byte in the file
	 *
	 * @param file the file
	 * @return a map containing the frequencies of each byte or an empty optional if an error occurs
	 */
	private static Optional<Map<Byte, Integer>> getFrequencies(RandomAccessFile file) throws IOException {
		Map<Byte, Integer> frequencies = new HashMap<>();

		file.seek(0);
		byte[] buffer = new byte[ConfigUtil.BUFFER_SIZE];

		while ((file.read(buffer)) != -1) {
			for (byte b : buffer) {
				frequencies.put(b, frequencies.getOrDefault(b, 0) + 1);
			}
		}

		return frequencies.isEmpty() ? Optional.empty() : Optional.of(frequencies);
	}

	/**
	 * Builds the Huffman tree
	 *
	 * @param frequencies the frequencies of the bytes
	 * @return the root of the Huffman tree
	 */
	private HuffmanNode buildTree(Map<Byte, Integer> frequencies) {
		PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();

		for (Map.Entry<Byte, Integer> entry : frequencies.entrySet()) {
			queue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
		}

		while (queue.size() > 1) {
			HuffmanNode left = queue.poll();
			HuffmanNode right = queue.poll();

			HuffmanNode parent = new HuffmanNode(left, right);

			queue.add(parent);
		}

		return queue.poll();
	}

	/**
	 * Builds the codes for each byte
	 *
	 * @param root the root of the Huffman tree
	 * @return a map containing the codes for each byte
	 */
	private Map<Byte, String> buildCodes(HuffmanNode root) {
		Map<Byte, String> codes = new HashMap<>();

		this.buildCodes(root, new StringBuilder(), codes);

		return codes;
	}

	/**
	 * Builds the codes for each byte - recursive method
	 *
	 * @param node   the current node
	 * @param prefix the prefix
	 * @param codes  the map containing the codes
	 */
	private void buildCodes(HuffmanNode node, StringBuilder prefix, Map<Byte, String> codes) {
		if (node.isLeaf()) {
			codes.put(node.getData(), prefix.toString());
		} else {
			prefix.append('0');
			buildCodes(node.getLeft(), prefix, codes);
			prefix.deleteCharAt(prefix.length() - 1);

			prefix.append('1');
			buildCodes(node.getRight(), prefix, codes);
			prefix.deleteCharAt(prefix.length() - 1);
		}
	}
}
