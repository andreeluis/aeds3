package index.reversedList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.ArrayList;

import db.Database;
import model.Movie;

public class ReversedList implements ReversedListStrategy {
    private String filePath;
    private static final String reversedListByNameFileName = "reversedListByName";
    private static final String reversedListByInfoFileName = "reversedListByInfo";
    private RandomAccessFile reversedListByNameFile;
    private RandomAccessFile reversedListByInfoFile;

    public ReversedList(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void build(Database database) throws FileNotFoundException {
        try {
            reversedListByNameFile = new RandomAccessFile(
                filePath + reversedListByNameFileName + Database.getFileExtension(),
                "rw"
            );

            database.getDatabase().readInt(); // skips last id
            while (!database.isEndOfFile()) {
                boolean tombstone = database.getDatabase().readBoolean();
                int length = database.getDatabase().readInt();
                if(!tombstone) {
                    byte[] byteArrayMovie = new byte[length];
                    Movie movie = new Movie(byteArrayMovie);
                    long position = database.getDatabase().getFilePointer();
                    addRegistryByKey(movie.getTitle(), position);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addRegistryByKey(String key, long position) {
        String[] keys = key.split(" ");
        try {
            for (String string : keys) {
                string = string.toLowerCase();
                List<Long> stringPositions = getPositionByKey(string);

                if(stringPositions.isEmpty()) {
                    reversedListByNameFile.seek(reversedListByNameFile.length());
                    reversedListByNameFile.writeBoolean(false);
                    reversedListByNameFile.writeUTF(string);
                    reversedListByNameFile.writeInt(1);
                    reversedListByNameFile.writeLong(position);
                } else {
                    // mark as tomb, add to eof
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void add(int id, long position) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public long get(int id) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public void remove(int id) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void clear() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    @Override
    public List<Long> getPositionByKey(String key) {
        List<Long> positions = new ArrayList<>();
        try {
            reversedListByNameFile.seek(0);
            while (reversedListByNameFile.getFilePointer() < reversedListByNameFile.length()) {
                boolean tombstone = reversedListByNameFile.readBoolean();
                String currentWord = reversedListByNameFile.readUTF();
                int entries = reversedListByNameFile.readInt();
                if(currentWord == key && !tombstone) {
                    for (int i = 0; i < entries; i++) {
                        long position = reversedListByNameFile.readLong();
                        positions.add(position);
                    }
                    return positions;
                }
                reversedListByNameFile.skipBytes(entries * 8);
            }
            return positions.isEmpty() ? positions : new ArrayList<Long>();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return positions.isEmpty() ? positions : new ArrayList<Long>();
        }
    }

}
