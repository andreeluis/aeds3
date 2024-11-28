package db.cryptography;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import model.interfaces.Cryptography;
import util.ConfigUtil;

public class CryptographyController {
	private List<Cryptography> cryptographies;

	public CryptographyController() {

	}

	public Optional<List<String>> getSupportedExtensions() {
		List<String> extensions = new ArrayList<>();

		for (Cryptography cryptography : cryptographies) {
			extensions.add(cryptography.getExtension());
		}

		return extensions.isEmpty() ? Optional.empty() : Optional.of(extensions);
	}

	/**
	 * Encrypts the file at the given path using all supported encryption algorithms.
	 *
	 * @param filePath the path to the file to encrypt
	 * @return
	 */
	public void encrypt(String filePath) {
		for (Cryptography cryptography : cryptographies) {
			String extension = cryptography.getExtension();

			if (filePath.endsWith(extension)) {
				encrypt(filePath, cryptography);
			}
		}
	}

	/**
	 * Decrypts the file at the given path using appropriate decryption algorithm.
	 *
	 * @param filePath the path to the file to decrypt
	 */
	public void decrypt(String filePath) {
		for (Cryptography cryptography : cryptographies) {
			String extension = cryptography.getExtension();

			if (filePath.endsWith(extension)) {
				decrypt(filePath, cryptography);
			}
		}
	}

	private void encrypt(String filePath, Cryptography cryptography) {
		try {
			String encryptedFilePath = filePath.replace(ConfigUtil.FILE_EXTENSION, cryptography.getExtension());

			RandomAccessFile file = new RandomAccessFile(filePath, "r");
			RandomAccessFile encryptedFile = new RandomAccessFile(encryptedFilePath, "rw");

			cryptography.encrypt(file, encryptedFile);

			file.close();
			encryptedFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void decrypt(String filePath, Cryptography cryptography) {
		try {
			String decryptedFilePath = filePath.replace(cryptography.getExtension(), ConfigUtil.FILE_EXTENSION);

			RandomAccessFile file = new RandomAccessFile(filePath, "r");
			RandomAccessFile decryptedFile = new RandomAccessFile(decryptedFilePath, "rw");

			cryptography.decrypt(file, decryptedFile);

			file.close();
			decryptedFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
