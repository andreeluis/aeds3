package index.InvertedList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import model.interfaces.IInvertedListStrategy;
import model.interfaces.InvertedListRegister;

public class InvertedList implements IInvertedListStrategy {
    private String filePath;
    private RandomAccessFile indexFile;
    private RandomAccessFile dataFile;
    private String field;

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
        // capitalize first letter
        this.field = field;
    }

    // constructor
    public InvertedList(String filePath, String field) throws IOException {
        this.setField(field);
        this.setFilePath(filePath);
        this.setIndexFile();
        this.setDataFile();
    }

    @Override
    public void add(String key, int id) throws IOException {
        // try {
        //     List<Integer> stringPositions = get(key);
        //     // long wordPosition = getWordPosition(key);

        //     if (stringPositions.isEmpty()) {
        //         reversedListByNameFile.seek(reversedListByNameFile.length());
        //         reversedListByNameFile.writeBoolean(false);
        //         reversedListByNameFile.writeUTF(key);
        //         reversedListByNameFile.writeInt(1);
        //         reversedListByNameFile.writeInt(id);
        //     } else {
        //     ids.add(id);
        // }

        // InvertedListRegister invertedListRegister = new InvertedListRegister(key, ids);

        // long position = file.length();
        // file.seek(position);
        // file.write(invertedListRegister.toByteArray());
    }

    @Override
    public List<Integer> get(String key) throws IOException {
        // List<Long> positions = new ArrayList<>();
        // try {
        // reversedListByNameFile.seek(0);
        // while (reversedListByNameFile.getFilePointer() <
        // reversedListByNameFile.length()) {
        // boolean tombstone = reversedListByNameFile.readBoolean();
        // String currentWord = reversedListByNameFile.readUTF();
        // int entries = reversedListByNameFile.readInt();
        // if (currentWord == key && !tombstone) {
        // for (int i = 0; i < entries; i++) {
        // long position = reversedListByNameFile.readLong();
        // positions.add(position);
        // }
        // return positions;
        // }
        // reversedListByNameFile.skipBytes(entries * 8);
        // }
        // return positions.isEmpty() ? positions : new ArrayList<Long>();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // return positions.isEmpty() ? positions : new ArrayList<Long>();
        // }

        return new ArrayList<Integer>();
    }

    @Override
    public void remove(String key) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void clear() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    // @Override
    // public void build(Database database) throws FileNotFoundException {
    //     // try {
    //     // reversedListByNameFile = new RandomAccessFile(
    //     // filePath + reversedListByNameFileName + Database.getFileExtension(),
    //     // "rw");

    //     // database.getDatabase().readInt(); // skips last id
    //     // while (!database.isEndOfFile()) {
    //     // boolean tombstone = database.getDatabase().readBoolean();
    //     // int length = database.getDatabase().readInt();
    //     // if (!tombstone) {
    //     // byte[] byteArrayMovie = new byte[length];
    //     // Movie movie = new Movie(byteArrayMovie);
    //     // long position = database.getDatabase().getFilePointer();
    //     // addRegistryByKey(movie.getTitle(), position);
    //     // }
    //     // }
    //     // } catch (IOException e) {
    //     // e.printStackTrace();
    //     // }
    // }

    private static boolean isWordValid(String word) {
        return false; // TODO: implement this method
    }

    private long getWordPosition(String word) {
        // // return word position in inverted list file
        // try {
        //     reversedListByNameFile.seek(0);
        //     while (reversedListByNameFile.getFilePointer() < reversedListByNameFile.length()) {
        //         long currentWordPosition = reversedListByNameFile.getFilePointer();
        //         boolean tombstone = reversedListByNameFile.readBoolean();
        //         String currentWord = reversedListByNameFile.readUTF();
        //         int entries = reversedListByNameFile.readInt();
        //         if (currentWord.equals(word) && !tombstone) {
        //             return currentWordPosition;
        //         }
        //         reversedListByNameFile.skipBytes(entries * 4);
        //     }
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
        return -1;
    }
}
