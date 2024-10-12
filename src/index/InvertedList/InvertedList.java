package index.InvertedList;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
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
        return this.filePath + "InvertedList" + mode + Database.getFileExtension();
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
        String[] words = this.getAttribute(movie).split(" ");

        for (String word : words) {
            if (isWordValid(word)) {
                // TODO: implement this method
            }
        }
    }

    @Override
    public List<Integer> get(String key) throws IOException {

        return new ArrayList<Integer>();
    }

    @Override
    public void remove(Movie movie) throws IOException {
        String[] words = this.getAttribute(movie).split(" ");

        for (String word : words) {
            if (isWordValid(word)) {
                // TODO: implement this method
            }
        }
    }

    @Override
    public void clear() throws IOException {
        // TODO: implement this method
    }

    private static boolean isWordValid(String word) {
        return false; // TODO: implement this method
    }

    private String getAttribute(Movie movie) {
        return this.getIndexFunction().apply(movie);
    }
}
