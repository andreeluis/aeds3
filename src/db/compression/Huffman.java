package db.compression;

import java.io.IOException;
import java.io.RandomAccessFile;

import db.Database;
import model.interfaces.Compression;

public class Huffman implements Compression {
	@Override
	public String getName() {
		return "Huffman";
	}

	@ Override
	public String getExtension() {
		return ".huff" + Database.getExtension();
	}

	/**
	 * Compresses the file using the Huffman algorithm
	 *
	 * @param source       the file to be compressed
	 * @param destination  the compressed file
	 */
	@Override
	public void compress(RandomAccessFile source, RandomAccessFile destination) throws IOException {

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
