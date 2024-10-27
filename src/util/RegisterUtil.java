package util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import model.Register;

public class RegisterUtil {
	/**
	 * Read the next register from the file.
	 * @param <T> the type of the returned register.
	 * @param constructor used to create a new instance of the register.
	 * @return the next register or empty if the file is at the end or the register is not valid.
	 * @throws IOException
	 */
	public static <T extends Register> Optional<T> getNextRegister(RandomAccessFile file, Constructor<T> constructor) throws IOException {
		try {
			if (!RAF.isEOF(file)) {
				boolean tombstone = file.readBoolean();
				int registerLength = file.readInt();

				if (!tombstone) {
					// read register
					byte[] byteArrayRegister = new byte[registerLength];
					file.read(byteArrayRegister);

					T register = constructor.newInstance();
					register.fromByteArray(byteArrayRegister);

					return Optional.of(register);
				} else {
					file.skipBytes(registerLength);
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	/**
	 * Read the next valid register from the file.
	 * @param <T> the type of the returned register.
	 * @param constructor used to create a new instance of the register.
	 * @return the next valid register or empty if the file is at the end.
	 * @throws IOException
	 */
	public static <T extends Register> Optional<T> getNextValidRegister(RandomAccessFile file, Constructor<T> constructor) throws IOException {
		try {
			while (!RAF.isEOF(file)) {
				boolean tombstone = file.readBoolean();
				int registerLength = file.readInt();

				if (!tombstone) {
					// read register
					byte[] byteArrayRegister = new byte[registerLength];
					file.read(byteArrayRegister);

					T register = constructor.newInstance();
					register.fromByteArray(byteArrayRegister);

					return Optional.of(register);
				} else {
					file.skipBytes(registerLength);
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}
