package index.InvertedList;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import db.Database;
import model.Movie;
import model.interfaces.IInvertedListStrategy;

public class InvertedList implements IInvertedListStrategy {
    private String filePath;
    private RandomAccessFile indexFile;
    private RandomAccessFile dataFile;
    private String field;
    private Function<Movie, String> indexFunction;

    // filePathName
    public String getFilePath() {
        return this.filePath;
    }

    public String getFilePath(String mode) {
        return this.filePath + "InvertedList" + getField() + mode + Database.getFileExtension();
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
    public Function<Movie, String> getIndexFunction() {
        return this.indexFunction;
    }

    public void setIndexFunction(Function<Movie, String> indexFunction) {
        this.indexFunction = indexFunction;
    }

    // constructor
    public InvertedList(String filePath, String field, Function<Movie, String> indexFunction) throws IOException {
        this.setField(field);
        this.setFilePath(filePath);
        this.setIndexFunction(indexFunction);
        this.setIndexFile();
        this.setDataFile();
    }

    @Override
    public void add(Movie movie) throws IOException {
        Set<String> processedWords = processWords(this.getAttribute(movie));

        for (String word : processedWords) {
            if (isWordValid(word)) {
                long wordPositionInIndex = this.findWordInIndex(word);
                if (wordPositionInIndex == -1) {
                    // word dooesnt exist in index
                    indexFile.seek(indexFile.length());
                    indexFile.writeUTF(word);
                    long wordPositionInDataFile = dataFile.length();
                    indexFile.writeLong(wordPositionInDataFile);

                    // add word to data file
                    dataFile.seek(dataFile.length());
                    dataFile.writeInt(movie.getId());
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
                    dataFile.writeInt(movie.getId());
                    dataFile.writeLong(wordPositionInDataFile); // point to the old address
                }
            }
        }
    }

    @Override
    public List<Integer> get(String key) throws IOException {
        String processedKey = key.toLowerCase().trim();
        if (!isWordValid(processedKey)) {
            return new ArrayList<>();
        }

        List<Integer> ids = new ArrayList<>();
        long wordPositionInIndex = this.findWordInIndex(processedKey);
        if (wordPositionInIndex != -1) {
            indexFile.seek(wordPositionInIndex + key.length() + 2);
            long dataPosition = indexFile.readLong();
            while (dataPosition != -1) {
                dataFile.seek(dataPosition);
                int movieId = dataFile.readInt();
                ids.add(movieId);
                dataPosition = dataFile.readLong();
            }
        }
        return ids;
    }

    @Override
    public void remove(Movie movie) throws IOException {
        Set<String> processedWords = processWords(this.getAttribute(movie));

        for (String word : processedWords) {
            if (isWordValid(word)) {
                long indexPosition = findWordInIndex(word);
                if (indexPosition != -1) {
                    indexFile.seek(indexPosition + word.length() + 2); // +2 for UTF encoding
                    long dataPosition = indexFile.readLong();
                    long prevDataPosition = -1;
                    
                    while (dataPosition != -1) {
                        dataFile.seek(dataPosition);
                        int currentId = dataFile.readInt();
                        long nextDataPosition = dataFile.readLong();
                        
                        if (currentId == movie.getId()) {
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

    private static boolean isWordValid(String word) {
        Set<String> STOP_WORDS = Set.of(
        "about", "which", "there", "after", "before", "while", "their", "would", 
        "could", "should", "these", "those", "where", "since", "being", "under",
        "other", "every", "first", "second", "third", "around", "among", "again", 
        "between", "because", "during", "through", "against", "within", "without",
        "across", "toward", "having", "might", "until", "always", "almost", 
        "anyone", "anything", "behind", "beside", "beyond", "cannot", 
        "coming", "either", "enough", "except", "following", "further", "herself", 
        "himself", "inside", "itself", "likely", "little", "making", "myself", 
        "nothing", "nowhere", "others", "outside", "people", "rather", "seeing",
        "taking", "thanks", "things", "though", "unless", "wanted", 
        "wanting", "wholly", "yourself", "ourselves", "anybody", "everybody",
        "someone", "everyone", "whatever", "wherever", "however", "whenever",
        "although", "whereby", "neither", "nonetheless", "besides",
        "themselves", "whose", "anymore", 
        "hence", "notwithstanding", "therefore", "whereas",
        "furthermore", "moreover", "whereupon", "nevertheless", "regardless", "whoever"
        // Add even more words as needed
    );
        return word != null && word.length() > 4 && !STOP_WORDS.contains(word); 
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
            if (isWordValid(word)) {
                processedWords.add(word);
            }
        }
        return processedWords;
    }

    private String getAttribute(Movie movie) {
        return this.getIndexFunction().apply(movie);
    }
}
