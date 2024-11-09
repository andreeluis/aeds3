package db.sort;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.ArrayList;

import db.Database;
import model.Register;
import util.ConfigUtil;
import util.RAF;
import util.RegisterUtil;

public class Sort<T extends Register> {
	private int pathsNumber = 3; // default pathsNumber
	private int inMemoryRegisters = 10; // default inMemoryRegisters
	private String filePath;

	private PriorityQueue<HeapNode<T>> heap;
	private RandomAccessFile[] tempFiles;

	private RandomAccessFile databaseFile;
	private Constructor<T> constructor;

	// pathsNumber
	public void setPathsNumber(int pathsNumber) {
		if (pathsNumber >= 2) {
			this.pathsNumber = pathsNumber;
		}
	}

	// inMemoryRegisters
	public void setInMemoryRegisters(int inMemoryRegisters) {
		if (inMemoryRegisters >= 2) {
			this.inMemoryRegisters = inMemoryRegisters;
		}
	}

	public Sort(Database<T> database, Constructor<T> constructor) {
		this.databaseFile = database.getFile();
		this.filePath = ConfigUtil.DB_PATH + "tmp";

		this.constructor = constructor;

		heap = new PriorityQueue<HeapNode<T>>(this.inMemoryRegisters);
	}

	public boolean sort(int pathsNumber, int inMemoryRegisters) {
		this.setPathsNumber(pathsNumber);
		this.setInMemoryRegisters(inMemoryRegisters);

		try {
			int segments;

			do {
				segments = distribution();
				intercalation();
        // if all paths are completed sorted, the next intercalation will result one sorted segment
			} while (segments > this.pathsNumber);

			return true;
		} catch (IOException e) {
			System.out.println("Erro ao ordenar registros.");
			return false;
		}
	}

	private void createTempFiles() throws IOException {
		tempFiles = new RandomAccessFile[this.pathsNumber];

		for (int i = 0; i < this.pathsNumber; i++) {
			String tempFilePath = filePath + (i + 1) + ConfigUtil.FILE_EXTENSION;

			// start and clean files[i]
			tempFiles[i] = new RandomAccessFile(tempFilePath, "rw");
			tempFiles[i].setLength(0);
			tempFiles[i].seek(0);
		}
	}

	private void deleteTempFiles() throws IOException {
		// early return
		if (tempFiles == null) {
			return;
		}

		// close files
		for (int i = 0; i < this.pathsNumber; i++) {
			if (tempFiles[i] != null) {
				tempFiles[i].close();
			}
		}

		// delete files
		for (int i = 0; i < pathsNumber; i++) {
			String tempFilePath = filePath + (i + 1) + ConfigUtil.FILE_EXTENSION;
			File file = new File(tempFilePath);

			if (file.exists()) {
				file.delete();
			}
		}

		tempFiles = null;
	}

	private void fillHeap(RandomAccessFile file) throws IOException {
		while (heap.size() < inMemoryRegisters && !RAF.isEOF(databaseFile)) {
			Optional<T> register = RegisterUtil.getNextValidRegister(file, constructor);

			if (register.isPresent()) {
				heap.add(new HeapNode<T>(register.get(), 0));
			} else {
				break;
			}
		}
	}

	/**
	 * Distribute registers in temp files
	 *
	 * @return last used segment
	 */
	public int distribution() throws IOException {
		// skips the lastId
		databaseFile.seek(0);
		databaseFile.readInt();

		this.fillHeap(databaseFile);

		T register;
		int segment = 0;
		createTempFiles();

		while (!heap.isEmpty()) {
			// remove from heap and get info
			HeapNode<T> heapNode = heap.poll();
			segment = heapNode.getSegment();
			register = heapNode.getRegister();

			// add to temp file
			int currentFile = segment % pathsNumber;
			tempFiles[currentFile].seek(tempFiles[currentFile].length());

			// write register
			byte[] byteArrayRegister = register.toByteArray();
			tempFiles[currentFile].writeBoolean(false); // tombstone
			tempFiles[currentFile].writeInt(byteArrayRegister.length);
			tempFiles[currentFile].write(byteArrayRegister); // register

			// if has more register, add to heap
			Optional<T> newRegister = RegisterUtil.getNextValidRegister(databaseFile, constructor);
			if (newRegister.isPresent()) {
				int newSegment;

				if (newRegister.get().compareTo(register) < 0) {
					newSegment = segment + 1;
				} else {
					newSegment = segment;
				}

				heap.add(new HeapNode<T>(newRegister.get(), newSegment));
			}
		}

		return segment;
	}

	/**
	 * Intercale registers from temp files to database file
	 */
	public void intercalation() throws IOException {
		cleanDatabaseRegisters();

		ArrayList<T> registers = new ArrayList<>(pathsNumber);
		boolean[] endOfFiles = new boolean[pathsNumber];
		boolean finished = false;

		for (int i = 0; i < pathsNumber; i++) {
			tempFiles[i].seek(0);
			endOfFiles[i] = false;
			registers.add(null);
		}

		while (!finished) {
			for (int i = 0; i < pathsNumber; i++) {
				Optional<T> register = RegisterUtil.getNextValidRegister(tempFiles[i], constructor);

				if (register.isPresent()) {
					registers.set(i, register.get());
				} else {
					endOfFiles[i] = true;
				}
			}

			// selection for the smaller
			int smallerPosition = -1;
			for (int i = 0; i < pathsNumber; i++) {
				if (registers.get(i) != null) {
					if (smallerPosition == -1 || registers.get(i).compareTo(registers.get(smallerPosition)) < 0) {
						smallerPosition = i;
					}
				}
			}

			if (smallerPosition != -1) {
				// remove movies[smaller]
				byte[] byteArrayMovie = registers.get(smallerPosition).toByteArray();
				registers.set(smallerPosition, null);

				databaseFile.writeBoolean(false); // tombstone
				databaseFile.writeInt(byteArrayMovie.length); // registerLength
				databaseFile.write(byteArrayMovie); // register
			}

			// verify finish
			finished = true;
			for (int i = 0; i < pathsNumber; i++) {
				if (!endOfFiles[i]) {
					finished = false;
				}
			}
		}

		deleteTempFiles();
	}

	/**
	 * Clean registers and keep lastId
	 */
	private void cleanDatabaseRegisters() throws IOException {
		databaseFile.seek(0);
		int lastId = databaseFile.readInt();

		databaseFile.setLength(0);
		databaseFile.writeInt(lastId);
	}
}
