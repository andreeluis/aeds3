package index.extendedHash;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import db.Database;
import index.IndexStrategy;

public class ExtendedHash implements IndexStrategy {

  private String filePath;
  private static final String dirFileName = "ExtendedHashDir";
  private static final String bucketFileName = "ExtendedHashBucket";
  private RandomAccessFile dirFile;
  private RandomAccessFile bucketFile;
  private int globalDepth;
  private static final int BUCKET_SIZE = 20; // Number of entries per bucket

  private DirectoryManager directoryManager;
  private BucketManager bucketManager;

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

      directoryManager = new DirectoryManager(dirFile);
      bucketManager = new BucketManager(bucketFile, BUCKET_SIZE);

      if (dirFile.length() == 0) {
        globalDepth = 1;
        directoryManager.initializeDirectory(globalDepth);
        bucketManager.initializeBuckets();
      } else {
        globalDepth = directoryManager.readGlobalDepth();
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
    long bucketAddress = directoryManager.getBucketAddress(bucketIndex);

    if (bucketManager.isBucketFull(bucketAddress)) {
      splitBucket(bucketIndex, id, position);
    } else {
      bucketManager.addToBucket(bucketAddress, id, position);
    }
  }

  private void splitBucket(int bucketIndex, int newId, long newPosition) throws IOException {
    int localDepth = bucketManager.getLocalDepth(directoryManager.getBucketAddress(bucketIndex));

    if (localDepth == globalDepth) {
      directoryManager.doubleDirectorySize(globalDepth++);
    }

    long newBucketAddress = bucketFile.length();
    bucketManager.initializeNewBucket(newBucketAddress, localDepth + 1);

    directoryManager.updateDirectoryAfterSplit(bucketIndex, localDepth, newBucketAddress, globalDepth);

    redistributeEntries(bucketIndex, localDepth, newId, newPosition);
  }

  private void redistributeEntries(int bucketIndex, int localDepth, int newId, long newPosition) throws IOException {
    long oldBucketAddress = directoryManager.getBucketAddress(bucketIndex);
    long newBucketAddress = directoryManager.getBucketAddress(bucketIndex | (1 << localDepth));

    bucketManager.redistributeEntries(oldBucketAddress, newBucketAddress, localDepth, newId, newPosition, this::hash);
  }

  @Override
  public long get(int id) throws IOException {
    int bucketIndex = hash(id);
    long bucketAddress = directoryManager.getBucketAddress(bucketIndex);
    return bucketManager.getFromBucket(bucketAddress, id);
  }

  @Override
  public void remove(int id) throws IOException {
    int bucketIndex = hash(id);
    long bucketAddress = directoryManager.getBucketAddress(bucketIndex);
    bucketManager.removeFromBucket(bucketAddress, id);
  }

  @Override
  public void clear() throws IOException {
    dirFile.setLength(0);
    bucketFile.setLength(0);
    globalDepth = 1;
    directoryManager.initializeDirectory(globalDepth);
    bucketManager.initializeBuckets();
  }

  private int hash(int id) {
    return id & ((1 << globalDepth) - 1);
  }
}