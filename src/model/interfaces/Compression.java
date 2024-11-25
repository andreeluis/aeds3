package model.interfaces;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface Compression {
	public String getName();
	public String getExtension();

	/**
	 * Compresses a file using the algorithm
	 *
	 * @param source       The file to compress
	 * @param destination  The compressed file
	 */
	public void compress(RandomAccessFile source, RandomAccessFile destination) throws IOException;

	/**
	 * Decompresses a file using the algorithm
	 *
	 * @param source       The compressed file
	 * @param destination  The decompressed file
	 */
	public void decompress(RandomAccessFile source, RandomAccessFile destination) throws IOException;
}
