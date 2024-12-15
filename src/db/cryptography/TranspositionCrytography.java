package db.cryptography;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import model.interfaces.Cryptography;
import util.ConfigUtil;

public class TranspositionCrytography implements Cryptography {

    @Override
    public String getName() {
        return "Transposition";
    }

    @Override
    public String getExtension() {
        return ".transposition" + ConfigUtil.FILE_EXTENSION;
    }

    @Override
    public void encrypt(RandomAccessFile source, RandomAccessFile destination)
            throws IOException {
        // Reset file pointers
        source.seek(0);
        destination.seek(0);

        // Retrieve the key from ConfigUtil
        String key = ConfigUtil.TRANSPOSITION_KEY;
        int[] keyOrder = getKeyOrder(key);
        int keyLength = keyOrder.length;

        // Process file in chunks matching the key length
        byte[] chunk = new byte[keyLength];
        int bytesRead;
        while ((bytesRead = source.read(chunk)) != -1) {
            if (bytesRead < keyLength) {
                // Pad incomplete chunk
                Arrays.fill(chunk, bytesRead, keyLength, (byte) 0);
            }
            // Transpose the chunk
            byte[] transposed = transpose(chunk, keyOrder);
            destination.write(transposed);
        }
    }

    @Override
    public void decrypt(RandomAccessFile source, RandomAccessFile destination)
            throws IOException {
        // Reset file pointers
        source.seek(0);
        destination.seek(0);

        // Retrieve the key from ConfigUtil
        String key = ConfigUtil.TRANSPOSITION_KEY;
        int[] keyOrder = getKeyOrder(key);
        int keyLength = keyOrder.length;

        // Process file in chunks matching the key length
        byte[] chunk = new byte[keyLength];
        int bytesRead;
        while ((bytesRead = source.read(chunk)) != -1) {
            // Detranspose the chunk
            byte[] detransposed = detranspose(chunk, keyOrder);

            // Write only the valid bytes (exclude padding)
            if (bytesRead < keyLength) {
                destination.write(detransposed, 0, bytesRead);
            } else {
                destination.write(detransposed);
            }
        }
    }

    // Utility method to determine the column order based on the key
    private int[] getKeyOrder(String key) {
        Character[] characters = new Character[key.length()];
        for (int i = 0; i < key.length(); i++) {
            characters[i] = key.charAt(i);
        }
        Arrays.sort(characters, (a, b) -> Character.compare(a, b));
        int[] order = new int[key.length()];
        for (int i = 0; i < key.length(); i++) {
            order[i] = key.indexOf(characters[i]);
        }
        return order;
    }

    // Utility method to transpose data using the key order
    private byte[] transpose(byte[] data, int[] keyOrder) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < keyOrder.length; i++) {
            result[keyOrder[i]] = data[i];
        }
        return result;
    }

    // Utility method to detranspose data using the key order
    private byte[] detranspose(byte[] data, int[] keyOrder) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < keyOrder.length; i++) {
            result[i] = data[keyOrder[i]];
        }
        return result;
    }
}
