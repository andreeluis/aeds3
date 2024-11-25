package util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import model.Register;

public class RegisterUtil {
	/**
	 * Read the next register from the file in the current position.
	 *
	 * @param <T>         the type of the returned register.
	 * @param constructor used to create a new instance of the register.
	 *
	 * @return the next register or empty if the file is at the end or the register
	 *         is not valid.
	 */
	public static <T extends Register> Optional<T> getNextRegister(RandomAccessFile file, Constructor<T> constructor)
			throws IOException {
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
	 * Read the next valid register from the file in the current position.
	 *
	 * @param <T>         the type of the returned register.
	 * @param constructor used to create a new instance of the register.
	 *
	 * @return the next valid register or empty if the file is at the end.
	 */
	public static <T extends Register> Optional<T> getNextValidRegister(RandomAccessFile file, Constructor<T> constructor)
			throws IOException {
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

	/**
	 * Get the register that the position is inside of it in the file.
	 *
	 * @param <T>         the type of the returned register.
	 * @param position    the position inside the register in the file.
	 * @param file        the file to read the register from.
	 * @param constructor used to create a new instance of the register.
	 *
	 * @return the register or empty if the file is at the end or the register is not valid.
	 */
	public static <T extends Register> Optional<T> getRegister(long position, RandomAccessFile file, Constructor<T> constructor) {
		try {
			file.seek(0);
			file.readInt(); // skip header

			while (!RAF.isEOF(file)) {
				long currentPosition = file.getFilePointer();

				boolean tombstone = file.readBoolean();
				int registerLength = file.readInt();

				if (!tombstone && (currentPosition <= position && position < file.getFilePointer() + registerLength)) {
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}
