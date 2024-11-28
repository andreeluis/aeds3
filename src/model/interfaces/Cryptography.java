package model.interfaces;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface Cryptography {
	public String getName();
	public String getExtension();

	/**
	 * Encrypts a file using the algorithm
	 *
	 * @param source       The file to encrypt
	 * @param destination  The encrypted file
	 */
	public void encrypt(RandomAccessFile source, RandomAccessFile destination) throws IOException;

	/**
	 * Decrypts a file using the algorithm
	 *
	 * @param source       The encrypted file
	 * @param destination  The decrypted file
	 */
	public void decrypt(RandomAccessFile source, RandomAccessFile destination) throws IOException;
}
