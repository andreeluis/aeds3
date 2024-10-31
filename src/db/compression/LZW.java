package db.compression;

import java.io.IOException;
import java.io.RandomAccessFile;

import db.Database;
import model.interfaces.Compression;

public class LZW implements Compression {
	@Override
	public String getName() {
		return "Huffman";
	}

	@Override
	public String getExtension() {
		return ".lzw" + Database.getExtension();
	}

	/**
	 * Compresses a file using the LZW algorithm
	 *
	 * @param source       The file to compress
	 * @param destination  The compressed file
	 */
	@Override
	public void compress(RandomAccessFile source, RandomAccessFile destination) throws IOException {

	}

	/**
	 * Decompresses a file using the LZW algorithm
	 *
	 * @param source       The file to decompress
	 * @param destination  The decompressed file
	 */
	@Override
	public void decompress(RandomAccessFile source, RandomAccessFile destination) throws IOException {

	}
}