package index.InvertedList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import db.Database;
import model.IInvertedListStrategy;

public class InvertedList implements IInvertedListStrategy {
    private String filePath;
    private static final String reversedListByNameFileName = "reversedListByName";
    private static final String reversedListByInfoFileName = "reversedListByInfo";
    private RandomAccessFile reversedListByNameFile;
    private RandomAccessFile reversedListByInfoFile;

    public InvertedList(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void add(String key, int id) throws IOException {
        try {
            List<Integer> stringPositions = get(key);
            // long wordPosition = getWordPosition(key);

            if (stringPositions.isEmpty()) {
                reversedListByNameFile.seek(reversedListByNameFile.length());
                reversedListByNameFile.writeBoolean(false);
                reversedListByNameFile.writeUTF(key);
                reversedListByNameFile.writeInt(1);
                reversedListByNameFile.writeInt(id);
            } else {
                // mark as tomb, add to eof
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public List<Integer> get(String key) throws IOException {
        // List<Long> positions = new ArrayList<>();
        // try {
        //     reversedListByNameFile.seek(0);
        //     while (reversedListByNameFile.getFilePointer() < reversedListByNameFile.length()) {
        //         boolean tombstone = reversedListByNameFile.readBoolean();
        //         String currentWord = reversedListByNameFile.readUTF();
        //         int entries = reversedListByNameFile.readInt();
        //         if (currentWord == key && !tombstone) {
        //             for (int i = 0; i < entries; i++) {
        //                 long position = reversedListByNameFile.readLong();
        //                 positions.add(position);
        //             }
        //             return positions;
        //         }
        //         reversedListByNameFile.skipBytes(entries * 8);
        //     }
        //     return positions.isEmpty() ? positions : new ArrayList<Long>();
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        //     return positions.isEmpty() ? positions : new ArrayList<Long>();
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

    @Override
    public void build(Database database) throws FileNotFoundException {
        // try {
        //     reversedListByNameFile = new RandomAccessFile(
        //             filePath + reversedListByNameFileName + Database.getFileExtension(),
        //             "rw");

        //     database.getDatabase().readInt(); // skips last id
        //     while (!database.isEndOfFile()) {
        //         boolean tombstone = database.getDatabase().readBoolean();
        //         int length = database.getDatabase().readInt();
        //         if (!tombstone) {
        //             byte[] byteArrayMovie = new byte[length];
        //             Movie movie = new Movie(byteArrayMovie);
        //             long position = database.getDatabase().getFilePointer();
        //             addRegistryByKey(movie.getTitle(), position);
        //         }
        //     }
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    private static boolean isWordValid(String word) {
        return false; // TODO: implement this method
    }

    private long getWordPosition(String word) {
        // return word position in inverted list file
        try {
            reversedListByNameFile.seek(0);
            while (reversedListByNameFile.getFilePointer() < reversedListByNameFile.length()) {
                long currentWordPosition = reversedListByNameFile.getFilePointer();
                boolean tombstone = reversedListByNameFile.readBoolean();
                String currentWord = reversedListByNameFile.readUTF();
                int entries = reversedListByNameFile.readInt();
                if (currentWord.equals(word) && !tombstone) {
                    return currentWordPosition;
                }
                reversedListByNameFile.skipBytes(entries * 4);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
