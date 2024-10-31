package db.index.inverted;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import db.Database;
import model.Register;
import model.interfaces.InvertedIndexStrategy;
import util.WordUtil;

public class InvertedIndex<T extends Register> implements InvertedIndexStrategy<T> {
	private String filePath;
	private RandomAccessFile indexFile;
	private RandomAccessFile dataFile;
	private String field;
	private Function<T, String> indexFunction;

	// filePathName
	public String getFilePath() {
		return this.filePath;
	}

	public String getFilePath(String mode) {
		return this.filePath + "InvertedList" + getField() + mode + Database.getExtension();
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	// indexFile
	public RandomAccessFile getFile() {
		return this.indexFile;
	}

	public void setIndexFile(RandomAccessFile indexFile) throws IOException {
		this.indexFile = indexFile;
	}

	private void setIndexFile() throws IOException {
		this.setIndexFile(new RandomAccessFile(this.getFilePath("Index"), "rw"));
	}

	// dataFile
	public RandomAccessFile getDataFile() {
		return this.dataFile;
	}

	public void setDataFile(RandomAccessFile dataFile) throws IOException {
		this.dataFile = dataFile;
	}

	private void setDataFile() throws IOException {
		this.setDataFile(new RandomAccessFile(this.getFilePath("Data"), "rw"));
	}

	// field
	public String getField() {
		return this.field;
	}

	public void setField(String field) {
		this.field = field;
	}

	// indexFunction
	public Function<T, String> getIndexFunction() {
		return this.indexFunction;
	}

	public void setIndexFunction(Function<T, String> indexFunction) {
		this.indexFunction = indexFunction;
	}

	// constructor
	public InvertedIndex(String filePath, String field, Function<T, String> indexFunction) throws IOException {
		this.setField(field);
		this.setFilePath(filePath);
		this.setIndexFunction(indexFunction);
		this.setIndexFile();
		this.setDataFile();
	}

	@Override
	public void add(T register, long position) throws IOException {
		Set<String> processedWords = processWords(this.getAttribute(register));

		for (String word : processedWords) {
			if (WordUtil.isWordValid(word)) {
				long wordPositionInIndex = this.findWordInIndex(word);
				if (wordPositionInIndex == -1) {
					// word dooesnt exist in index
					indexFile.seek(indexFile.length());
					indexFile.writeUTF(word);
					long wordPositionInDataFile = dataFile.length();
					indexFile.writeLong(wordPositionInDataFile);

					// add word to data file
					dataFile.seek(dataFile.length());
					dataFile.writeInt(register.getId());
					dataFile.writeLong(-1);
				} else {
					// word exists in index
					indexFile.seek(wordPositionInIndex + word.length() + 2); // utf format
					long wordPositionInDataFile = indexFile.readLong();
					long newWordPositionInDataFile = dataFile.length();

					// Update index to point to new data
					indexFile.seek(wordPositionInIndex + word.length() + 2);
					indexFile.writeLong(newWordPositionInDataFile);

					dataFile.seek(newWordPositionInDataFile);
					dataFile.writeInt(register.getId());
					dataFile.writeLong(wordPositionInDataFile); // point to the old address
				}
			}
		}
	}

	@Override
	public Optional<List<Integer>> get(String key) throws IOException {
		String processedKey = key.toLowerCase().trim();

		// early return
		if (!WordUtil.isWordValid(processedKey)) {
			return Optional.empty();
		}

		List<Integer> ids = new ArrayList<>();
		long wordPositionInIndex = this.findWordInIndex(processedKey);
		if (wordPositionInIndex != -1) {
			indexFile.seek(wordPositionInIndex + key.length() + 2);
			long dataPosition = indexFile.readLong();

			while (dataPosition != -1) {
				dataFile.seek(dataPosition);
				int registerId = dataFile.readInt();
				ids.add(registerId);
				dataPosition = dataFile.readLong();
			}
		}

		return Optional.of(ids);
	}

	@Override
	public void remove(T register) throws IOException {
		Set<String> processedWords = processWords(this.getAttribute(register));

		for (String word : processedWords) {
			if (WordUtil.isWordValid(word)) {
				long indexPosition = findWordInIndex(word);
				if (indexPosition != -1) {
					indexFile.seek(indexPosition + word.length() + 2); // +2 for UTF encoding
					long dataPosition = indexFile.readLong();
					long prevDataPosition = -1;

					while (dataPosition != -1) {
						dataFile.seek(dataPosition);
						int currentId = dataFile.readInt();
						long nextDataPosition = dataFile.readLong();

						if (currentId == register.getId()) {
							// Remove this entry
							if (prevDataPosition == -1) {
								// It's the first entry, update index
								indexFile.seek(indexPosition + word.length() + 2);
								indexFile.writeLong(nextDataPosition);
							} else {
								// Update previous entry to skip this one
								dataFile.seek(prevDataPosition + 4); // +4 to skip the ID
								dataFile.writeLong(nextDataPosition);
							}
							break;
						}

						prevDataPosition = dataPosition;
						dataPosition = nextDataPosition;
					}
				}
			}
		}
	}

	@Override
	public void clear() throws IOException {
		indexFile.setLength(0);
		dataFile.setLength(0);
	}

	private long findWordInIndex(String word) throws IOException {
		indexFile.seek(0);
		while (indexFile.getFilePointer() < indexFile.length()) {
			long currentPosition = indexFile.getFilePointer();
			String currentWord = indexFile.readUTF();
			if (currentWord.equals(word)) {
				return currentPosition;
			}
			indexFile.skipBytes(8);
		}
		return -1;
	}

	private Set<String> processWords(String input) {
		Set<String> processedWords = new HashSet<>();
		String[] words = input.toLowerCase().split("[\\s,.:]+");
		for (String word : words) {
			if (WordUtil.isWordValid(word)) {
				processedWords.add(word);
			}
		}
		return processedWords;
	}

	private String getAttribute(T register) {
		return this.getIndexFunction().apply(register);
	}

	@Override
	public String getName() {
		return "Lista Invertida" + this.getField();
	}
}
