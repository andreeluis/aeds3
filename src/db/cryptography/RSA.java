package db.cryptography;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

import model.interfaces.Cryptography;
import util.ConfigUtil;

public class RSA implements Cryptography {
	private BigInteger p;
	private BigInteger q;

	private BigInteger n;
	private BigInteger d;
	private BigInteger e;

	@Override
	public String getName() {
		return "RSA";
	}

	@Override
	public String getExtension() {
		return ".rsa" + ConfigUtil.FILE_EXTENSION;
	}

	public RSA() {
		int bitLength = 1024;
		
		SecureRandom random = new SecureRandom();

		this.p = BigInteger.probablePrime(bitLength / 2, random);
		this.q = BigInteger.probablePrime(bitLength / 2, random);

		n = p.multiply(q);
		BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
		e = BigInteger.probablePrime(bitLength / 2, random);

		while (phi.gcd(e).intValue() > 1) {
			e = e.add(BigInteger.ONE);
		}

		d = e.modInverse(phi);
	}

	@Override
	public void encrypt(RandomAccessFile source, RandomAccessFile destination) throws IOException {
		FileInputStream inputStream = new FileInputStream(source.getFD());
		FileOutputStream outputStream = new FileOutputStream(destination.getFD());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

		int b;
		while ((b = inputStream.read()) != -1) {
			byte[] encrypted = (new BigInteger(new byte[] { (byte) b })).modPow(e, n).toByteArray();
			String encoded = Base64.getEncoder().encodeToString(encrypted);
			writer.write(encoded);
			writer.newLine();
		}

		inputStream.close();
		writer.close();
		outputStream.close();
	}

	@Override
	public void decrypt(RandomAccessFile source, RandomAccessFile destination) throws IOException {
		FileInputStream inputStream = new FileInputStream(source.getFD());
		FileOutputStream outputStream = new FileOutputStream(destination.getFD());

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		String line;
		while ((line = reader.readLine()) != null) {
			byte[] encrypted = Base64.getDecoder().decode(line);
			byte[] decrypted = (new BigInteger(encrypted)).modPow(d, n).toByteArray();
			outputStream.write(decrypted);
		}

		outputStream.close();
		reader.close();
		inputStream.close();
	}
}
