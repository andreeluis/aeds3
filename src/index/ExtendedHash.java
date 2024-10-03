package index;

import db.Database;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ExtendedHash implements IndexStrategy {

  private String filePath;
  private static final String dirFileName = "ExtendedHashDir";
  private static final String bucketFileName = "ExtendedHashBucket";
  private RandomAccessFile dirFile;
  private RandomAccessFile bucketFile;
  private int globalDepth;
  private static final int BUCKET_SIZE = 4; // Number of entries per bucket

  public ExtendedHash(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public void build(Database database) throws FileNotFoundException {
    try {
      dirFile = new RandomAccessFile(
        filePath + dirFileName + database.getFileExtension(),
        "rw"
      );
      bucketFile = new RandomAccessFile(
        filePath + bucketFileName + database.getFileExtension(),
        "rw"
      );

      if (dirFile.length() == 0) {
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

  private void splitBucket(
    int bucketIndex,
    int localDepth,
    int newId,
    long newPosition
  ) throws IOException {
    if (localDepth == globalDepth) {
      // Double directory size
      int oldSize = 1 << globalDepth;
      globalDepth++;
      dirFile.seek(0);
      dirFile.writeInt(globalDepth);
      dirFile.writeInt(oldSize * 2);

      for (int i = 0; i < oldSize; i++) {
        long address = dirFile.readLong();
        dirFile.seek(dirFile.getFilePointer() + oldSize * 8);
        dirFile.writeLong(address);
      }
    }

    // Create new bucket
    long newBucketAddress = bucketFile.length();
    bucketFile.seek(newBucketAddress);
    bucketFile.writeInt(localDepth + 1);
    bucketFile.writeInt(0);

    for (int i = 0; i < BUCKET_SIZE; i++) {
      bucketFile.writeInt(0);
      bucketFile.writeLong(0);
    }

    // Update directory
    int mask = 1 << localDepth;
    int dirSize = 1 << globalDepth;

    for (int i = 0; i < dirSize; i++) {
      if ((i & ((1 << localDepth) - 1)) == bucketIndex) {
        dirFile.seek(8 + i * 8);
        if ((i & mask) == 0) {
          dirFile.writeLong(getBucketAddress(bucketIndex));
        } else {
          dirFile.writeLong(newBucketAddress);
        }
      }
    }

    // Redistribute entries
    long oldBucketAddress = getBucketAddress(bucketIndex);
    bucketFile.seek(oldBucketAddress);
    bucketFile.writeInt(localDepth + 1);
    int oldEntries = bucketFile.readInt();

    List<Integer> ids = new ArrayList<>();
    List<Long> positions = new ArrayList<>();

    for (int i = 0; i < oldEntries; i++) {
      ids.add(bucketFile.readInt());
      positions.add(bucketFile.readLong());
    }

    ids.add(newId);
    positions.add(newPosition);

    bucketFile.seek(oldBucketAddress + 8);
    bucketFile.seek(newBucketAddress + 8);

    int oldCount = 0, newCount = 0;

    for (int i = 0; i < ids.size(); i++) {
      int id = ids.get(i);
      long position = positions.get(i);

      if ((hash(id) & mask) == 0) {
        bucketFile.seek(oldBucketAddress + 8 + oldCount * 12);
        bucketFile.writeInt(id);
        bucketFile.writeLong(position);
        oldCount++;
      } else {
        bucketFile.seek(newBucketAddress + 8 + newCount * 12);
        bucketFile.writeInt(id);
        bucketFile.writeLong(position);
        newCount++;
      }
    }

    bucketFile.seek(oldBucketAddress + 4);
    bucketFile.writeInt(oldCount);

    bucketFile.seek(newBucketAddress + 4);
    bucketFile.writeInt(newCount);
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
