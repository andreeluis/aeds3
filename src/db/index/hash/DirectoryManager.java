package db.index.hash;

import java.io.IOException;
import java.io.RandomAccessFile;

class DirectoryManager {
    private RandomAccessFile dirFile;
    private int bucketSize;

    public DirectoryManager(RandomAccessFile dirFile, int bucketSize) {
        this.dirFile = dirFile;
        this.bucketSize = bucketSize;
    }

    public void initializeDirectory(int globalDepth) throws IOException {
        System.out.println("inicializar diretorio");
        dirFile.writeInt(globalDepth);
        dirFile.writeInt(2); // Initial number of buckets
        dirFile.writeLong(0); // Bucket 0 address
        dirFile.writeLong(8 + this.bucketSize * 12); // Bucket 1 address
    }

    public int readGlobalDepth() throws IOException {
        dirFile.seek(0);
        return dirFile.readInt();
    }

    public long getBucketAddress(int bucketIndex) throws IOException {
        dirFile.seek(8 + bucketIndex * 8);
        return dirFile.readLong();
    }

    public void doubleDirectorySize(int globalDepth) throws IOException {
        int oldSize = 1 << globalDepth;
        dirFile.seek(0);
        dirFile.writeInt(globalDepth + 1);
        dirFile.writeInt(oldSize * 2);

        for (int i = 0; i < oldSize; i++) {
            dirFile.seek(8 + i * 8);
            long address = dirFile.readLong();
            dirFile.seek(8 + (i + oldSize) * 8);
            dirFile.writeLong(address);
        }
    }

    public void updateDirectoryAfterSplit(int bucketIndex, int localDepth, long newBucketAddress, int globalDepth) throws IOException {
        int dirSize = 1 << globalDepth;
        int splitMask = 1 << localDepth;

        for (int i = 0; i < dirSize; i++) {
            if ((i & (splitMask - 1)) == bucketIndex) {
                dirFile.seek(8 + i * 8);
                if ((i & splitMask) == 0) {
                    long oldBucketAddress = dirFile.readLong();
                    dirFile.seek(dirFile.getFilePointer() - 8);
                    dirFile.writeLong(oldBucketAddress);
                } else {
                    dirFile.writeLong(newBucketAddress);
                }
            }
        }
    }

    public boolean isEmpty() throws IOException {
        return dirFile.length() == 0;
    }

	public void clear() throws IOException {
        dirFile.setLength(0);
	}
}
