package db.compression;

import java.io.IOException;
import java.io.RandomAccessFile;

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
	 * @param source       the file to be compressed
	 * @param destination  the compressed file
	 */
	@Override
	public void compress(RandomAccessFile source, RandomAccessFile destination) throws IOException {
		destination.writeBytes("Huffman");
	}

	/**
	 * Decompresses the file using the Huffman algorithm
	 *
	 * @param source       the file to be decompressed
	 * @param destination  the decompressed file
	 */
	@Override
	public void decompress(RandomAccessFile source, RandomAccessFile destination) throws IOException {

	}
}
