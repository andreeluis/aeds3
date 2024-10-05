package index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import db.Database;

public class ExtendedHash implements IndexStrategy {

  private String filePath;
  private static final String dirFileName = "ExtendedHashDir";
  private static final String bucketFileName = "ExtendedHashBucket";
  private RandomAccessFile dirFile;
  private RandomAccessFile bucketFile;
  private int globalDepth;
  private static final int BUCKET_SIZE = 20; // Number of entries per bucket

  public ExtendedHash(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public void build(Database database) throws FileNotFoundException {
    try {
      dirFile = new RandomAccessFile(
          filePath + dirFileName + Database.getFileExtension(),
          "rw");
      bucketFile = new RandomAccessFile(
          filePath + bucketFileName + Database.getFileExtension(),
          "rw");

      if (dirFile.length() == 0) {
        globalDepth = 1;
        dirFile.writeInt(globalDepth);
        dirFile.writeInt(2); // Initial number of buckets
        dirFile.writeLong(0); // Bucket 0 address
        dirFile.writeLong(8 + BUCKET_SIZE * 12); // Bucket 1 address

        // Initialize buckets
        for (int i = 0; i < 2; i++) {
          bucketFile.writeInt(1); // Local depth
          bucketFile.writeInt(0); // Number of entries
          for (int j = 0; j < BUCKET_SIZE; j++) {
            bucketFile.writeInt(-1); // ID
            bucketFile.writeLong(-1); // Position
          }
        }
      } else {
        globalDepth = dirFile.readInt();
      }

      // Insert all records from the database
      database.getDatabase().seek(4); // Skip lastId
      while (!database.isEndOfFile()) {
        boolean tombstone = database.getDatabase().readBoolean();
        int length = database.getDatabase().readInt();

        if (!tombstone) {
          int id = database.getDatabase().readInt();
          long position = database.getDatabase().getFilePointer() - 4;
          add(id, position);
        }

        database.getDatabase().skipBytes(length - 4);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void add(int id, long position) throws IOException {
    int bucketIndex = hash(id);
    long bucketAddress = getBucketAddress(bucketIndex);

    bucketFile.seek(bucketAddress);
    int localDepth = bucketFile.readInt();
    int entries = bucketFile.readInt();

    if (entries < BUCKET_SIZE) {
      // Add to existing bucket
      bucketFile.skipBytes(entries * 12);
      bucketFile.writeInt(id);
      bucketFile.writeLong(position);
      bucketFile.seek(bucketAddress + 4);
      bucketFile.writeInt(entries + 1);
    } else {
      // Split bucket
      splitBucket(bucketIndex, localDepth, id, position);
    }
  }

  private void splitBucket(int bucketIndex, int localDepth, int newId, long newPosition) throws IOException {
    if (localDepth == globalDepth) {
      // Double the directory size
      doubleDirectorySize();
    }

    // Create a new bucket
    long newBucketAddress = bucketFile.length();

    // Initialize the new bucket
    bucketFile.seek(newBucketAddress);
    bucketFile.writeInt(localDepth + 1); // Local depth
    bucketFile.writeInt(0); // Number of entries
    for (int i = 0; i < BUCKET_SIZE; i++) {
      bucketFile.writeInt(-1); // ID
      bucketFile.writeLong(-1); // Position
    }

    // Update the directory
    updateDirectoryAfterSplit(bucketIndex, localDepth, newBucketAddress);

    // Redistribute entries
    redistributeEntries(bucketIndex, localDepth, newId, newPosition);
  }

  private void doubleDirectorySize() throws IOException {
    int oldSize = 1 << globalDepth;
    globalDepth++;

    // Update global depth in the directory file
    dirFile.seek(0);
    dirFile.writeInt(globalDepth);

    // Update number of buckets
    dirFile.writeInt(oldSize * 2);

    // Duplicate the bucket addresses
    for (int i = 0; i < oldSize; i++) {
      dirFile.seek(8 + i * 8);
      long address = dirFile.readLong();
      dirFile.seek(8 + (i + oldSize) * 8);
      dirFile.writeLong(address);
    }
  }

  private void updateDirectoryAfterSplit(int bucketIndex, int localDepth, long newBucketAddress) throws IOException {
    int dirSize = 1 << globalDepth;
    int splitMask = 1 << localDepth;

    for (int i = 0; i < dirSize; i++) {
      if ((i & (splitMask - 1)) == bucketIndex) {
        dirFile.seek(8 + i * 8);
        if ((i & splitMask) == 0) {
          // This points to the original bucket
          long oldBucketAddress = dirFile.readLong();
          dirFile.seek(dirFile.getFilePointer() - 8);
          dirFile.writeLong(oldBucketAddress);
        } else {
          // This points to the new bucket
          dirFile.writeLong(newBucketAddress);
        }
      }
    }
  }

  private void redistributeEntries(int bucketIndex, int localDepth, int newId, long newPosition) throws IOException {
    long oldBucketAddress = getBucketAddress(bucketIndex);
    long newBucketAddress = getBucketAddress(bucketIndex | (1 << localDepth));

    // Read all entries from the old bucket
    bucketFile.seek(oldBucketAddress);
    int oldLocalDepth = bucketFile.readInt();
    int oldEntries = bucketFile.readInt();

    List<Integer> ids = new ArrayList<>();
    List<Long> positions = new ArrayList<>();

    for (int i = 0; i < oldEntries; i++) {
      ids.add(bucketFile.readInt());
      positions.add(bucketFile.readLong());
    }

    // Add the new entry
    ids.add(newId);
    positions.add(newPosition);

    // Clear the old bucket
    bucketFile.seek(oldBucketAddress);
    bucketFile.writeInt(oldLocalDepth + 1); // Increase local depth
    bucketFile.writeInt(0); // Reset entry count

    // Clear the new bucket
    bucketFile.seek(newBucketAddress);
    bucketFile.writeInt(oldLocalDepth + 1); // Set local depth
    bucketFile.writeInt(0); // Reset entry count

    // Redistribute entries
    int oldCount = 0, newCount = 0;
    int mask = 1 << oldLocalDepth;

    for (int i = 0; i < ids.size(); i++) {
      int id = ids.get(i);
      long position = positions.get(i);

      if ((hash(id) & mask) == 0) {
        writeToBucket(oldBucketAddress, oldCount++, id, position);
      } else {
        writeToBucket(newBucketAddress, newCount++, id, position);
      }
    }

    // Update entry counts
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

  @Override
  public long get(int id) throws IOException {
    int bucketIndex = hash(id);
    long bucketAddress = getBucketAddress(bucketIndex);

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

  @Override
  public void remove(int id) throws IOException {
    int bucketIndex = hash(id);
    long bucketAddress = getBucketAddress(bucketIndex);

    bucketFile.seek(bucketAddress);
    int localDepth = bucketFile.readInt();
    int entries = bucketFile.readInt();

    for (int i = 0; i < entries; i++) {
      long entryAddress = bucketFile.getFilePointer();
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

  @Override
  public void clear() throws IOException {
    dirFile.setLength(0);
    bucketFile.setLength(0);

    globalDepth = 1;
    dirFile.writeInt(globalDepth);
    dirFile.writeInt(2); // Initial number of buckets
    dirFile.writeLong(0); // Bucket 0 address
    dirFile.writeLong(BUCKET_SIZE * 12); // Bucket 1 address

    // Initialize buckets
    for (int i = 0; i < 2; i++) {
      bucketFile.writeInt(1); // Local depth
      bucketFile.writeInt(0); // Number of entries
      for (int j = 0; j < BUCKET_SIZE; j++) {
        bucketFile.writeInt(0); // ID
        bucketFile.writeLong(0); // Position
      }
    }
  }

  private int hash(int id) {
    return id & ((1 << globalDepth) - 1);
  }

  private long getBucketAddress(int bucketIndex) throws IOException {
    dirFile.seek(8 + bucketIndex * 8);
    return dirFile.readLong();
  }
}
