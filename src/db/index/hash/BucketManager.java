package db.index.hash;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

class BucketManager {
    private RandomAccessFile bucketFile;
    private int bucketSize;

    // bucketFile
    public RandomAccessFile getBucketFile() {
        return this.bucketFile;
    }

    // bucketSize
    public int getBucketSize() {
        return this.bucketSize;
    }

    public BucketManager(RandomAccessFile bucketFile, int bucketSize) {
        this.bucketFile = bucketFile;
        this.bucketSize = bucketSize;
    }

    public void initializeBuckets() throws IOException {
        for (int i = 0; i < 2; i++) {
            bucketFile.writeInt(1); // Local depth
            bucketFile.writeInt(0); // Number of entries
            for (int j = 0; j < bucketSize; j++) {
                bucketFile.writeInt(-1); // ID
                bucketFile.writeLong(-1); // Position
            }
        }
    }

    public boolean isBucketFull(long bucketAddress) throws IOException {
        bucketFile.seek(bucketAddress + 4);
        return bucketFile.readInt() >= bucketSize;
    }

    public void addToBucket(long bucketAddress, int id, long position) throws IOException {
        bucketFile.seek(bucketAddress);
        bucketFile.readInt(); // Skip local depth
        int entries = bucketFile.readInt();

        bucketFile.skipBytes(entries * 12);
        bucketFile.writeInt(id);
        bucketFile.writeLong(position);
        bucketFile.seek(bucketAddress + 4);
        bucketFile.writeInt(entries + 1);
    }

    public int getLocalDepth(long bucketAddress) throws IOException {
        bucketFile.seek(bucketAddress);
        return bucketFile.readInt();
    }

    public void initializeNewBucket(long bucketAddress, int localDepth) throws IOException {
        bucketFile.seek(bucketAddress);
        bucketFile.writeInt(localDepth);
        bucketFile.writeInt(0);
        for (int i = 0; i < bucketSize; i++) {
            bucketFile.writeInt(-1);
            bucketFile.writeLong(-1);
        }
    }

    public void redistributeEntries(long oldBucketAddress, long newBucketAddress, int localDepth, int newId, long newPosition, IntUnaryOperator hashFunction) throws IOException {
        List<Integer> ids = new ArrayList<>();
        List<Long> positions = new ArrayList<>();

        // Read entries from old bucket
        bucketFile.seek(oldBucketAddress + 4);
        int entries = bucketFile.readInt();
        for (int i = 0; i < entries; i++) {
            ids.add(bucketFile.readInt());
            positions.add(bucketFile.readLong());
        }

        // Add new entry
        ids.add(newId);
        positions.add(newPosition);

        // Clear old bucket
        bucketFile.seek(oldBucketAddress);
        bucketFile.writeInt(localDepth + 1);
        bucketFile.writeInt(0);

        // Clear new bucket
        bucketFile.seek(newBucketAddress);
        bucketFile.writeInt(localDepth + 1);
        bucketFile.writeInt(0);

        // Redistribute entries
        int oldCount = 0, newCount = 0;
        int mask = 1 << localDepth;

        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            long position = positions.get(i);

            if ((hashFunction.applyAsInt(id) & mask) == 0) {
                writeToBucket(oldBucketAddress, oldCount++, id, position);
            } else {
                writeToBucket(newBucketAddress, newCount++, id, position);
            }
        }

        updateBucketCount(oldBucketAddress, oldCount);
        updateBucketCount(newBucketAddress, newCount);
    }

    private void writeToBucket(long bucketAddress, int index, int id, long position) throws IOException {
        bucketFile.seek(bucketAddress + 8 + index * 12);
        bucketFile.writeInt(id);
        bucketFile.writeLong(position);
    }

    private void updateBucketCount(long bucketAddress, int count) throws IOException {
        bucketFile.seek(bucketAddress + 4);
        bucketFile.writeInt(count);
    }

    public long getFromBucket(long bucketAddress, int id) throws IOException {
        bucketFile.seek(bucketAddress);
        bucketFile.readInt(); // Skip local depth
        int entries = bucketFile.readInt();

        for (int i = 0; i < entries; i++) {
            int currentId = bucketFile.readInt();
            long position = bucketFile.readLong();

            if (currentId == id) {
                return position;
            }
        }

        return -1; // Not found
    }

    public void removeFromBucket(long bucketAddress, int id) throws IOException {
        bucketFile.seek(bucketAddress);
        bucketFile.readInt(); // Skip local depth
        int entries = bucketFile.readInt();

        for (int i = 0; i < entries; i++) {
            int currentId = bucketFile.readInt();
            bucketFile.skipBytes(8);

            if (currentId == id) {
                // Remove entry by shifting subsequent entries
                for (int j = i + 1; j < entries; j++) {
                    bucketFile.seek(bucketAddress + 8 + j * 12);
                    int shiftId = bucketFile.readInt();
                    long shiftPosition = bucketFile.readLong();
                    bucketFile.seek(bucketAddress + 8 + (j - 1) * 12);
                    bucketFile.writeInt(shiftId);
                    bucketFile.writeLong(shiftPosition);
                }

                bucketFile.seek(bucketAddress + 4);
                bucketFile.writeInt(entries - 1);
                return;
            }
        }
    }

    public void clear() throws IOException {
        bucketFile.setLength(0);
    }
}
