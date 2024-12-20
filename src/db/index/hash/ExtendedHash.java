package db.index.hash;

import java.io.IOException;
import java.io.RandomAccessFile;

import model.Register;
import model.interfaces.IndexStrategy;
import util.ConfigUtil;

public class ExtendedHash<T extends Register> implements IndexStrategy<T> {
	private int bucketSize;
	private int globalDepth;
	private DirectoryManager directory;
	private BucketManager bucket;

	// bucketSize
	public int getBucketSize() {
		return this.bucketSize;
	}

	public void setBucketSize(int bucketSize) {
		if (bucketSize >= 1) {
			this.bucketSize = bucketSize;
		} else {
			System.out.println("O tamanho do bucket deve ser maior ou igual a 1.");
			this.bucketSize = 1;
		}
	}

	// globalDepth
	public int getGlobalDepth() {
		return this.globalDepth;
	}

	public void setGlobalDepth(int globalDepth) {
		if (globalDepth >= 1) {
			this.globalDepth = globalDepth;
		} else {
			System.out.println("A profundidade global deve ser maior ou igual a 1.");
			this.globalDepth = 1;
		}
	}

	// directory
	public String getDirectoryPath() {
		return ConfigUtil.DB_PATH + "ExtHashDir" + ConfigUtil.FILE_EXTENSION;
	}

	public void setDirectory(DirectoryManager directory) {
		this.directory = directory;
	}

	private void setDirectory() throws IOException {
		RandomAccessFile directoryFile = new RandomAccessFile(this.getDirectoryPath(), "rw");

		this.setDirectory(new DirectoryManager(directoryFile, this.getBucketSize()));
	}

	// bucket
	public String getBucketPath() {
		return ConfigUtil.DB_PATH + "ExtHashBuck" + ConfigUtil.FILE_EXTENSION;
	}

	public void setBucket(BucketManager bucket) {
		this.bucket = bucket;
	}

	private void setBucket() throws IOException {
		RandomAccessFile bucketFile = new RandomAccessFile(this.getBucketPath(), "rw");

		this.setBucket(new BucketManager(bucketFile, this.getBucketSize()));
	}

	// constructor
	public ExtendedHash(int bucketSize) throws IOException {
		this.setBucketSize(bucketSize);

		this.setDirectory();
		this.setBucket();

		if (directory.isEmpty()) {
			this.setGlobalDepth(1);

			directory.initializeDirectory(globalDepth);
			bucket.initializeBuckets();
		} else {
			this.setGlobalDepth(directory.readGlobalDepth());
		}
	}

	@Override
	public void add(T register, long position) throws IOException {
		int id = register.getId();

		int bucketIndex = hash(id);
		long bucketAddress = directory.getBucketAddress(bucketIndex);

		if (bucket.isBucketFull(bucketAddress)) {
			splitBucket(bucketIndex, id, position);
		} else {
			bucket.addToBucket(bucketAddress, id, position);
		}
	}

	private void splitBucket(int bucketIndex, int newId, long newPosition) throws IOException {
		int localDepth = bucket.getLocalDepth(directory.getBucketAddress(bucketIndex));

		if (localDepth == globalDepth) {
			directory.doubleDirectorySize(globalDepth++);
		}

		long newBucketAddress = bucket.getBucketFile().length();
		bucket.initializeNewBucket(newBucketAddress, localDepth + 1);

		directory.updateDirectoryAfterSplit(bucketIndex, localDepth, newBucketAddress, globalDepth);

		redistributeEntries(bucketIndex, localDepth, newId, newPosition);
	}

	private void redistributeEntries(int bucketIndex, int localDepth, int newId, long newPosition) throws IOException {
		long oldBucketAddress = directory.getBucketAddress(bucketIndex);
		long newBucketAddress = directory.getBucketAddress(bucketIndex | (1 << localDepth));

		bucket.redistributeEntries(oldBucketAddress, newBucketAddress, localDepth, newId, newPosition, this::hash);
	}

	@Override
	public long get(int id) throws IOException {
		int bucketIndex = hash(id);
		long bucketAddress = directory.getBucketAddress(bucketIndex);
		return bucket.getFromBucket(bucketAddress, id);
	}

	@Override
	public void remove(T register) throws IOException {
		int id = register.getId();

		int bucketIndex = hash(id);
		long bucketAddress = directory.getBucketAddress(bucketIndex);
		bucket.removeFromBucket(bucketAddress, id);
	}

	@Override
	public void clear() throws IOException {
		directory.clear();
		bucket.clear();

		setGlobalDepth(1);
		directory.initializeDirectory(globalDepth);
		bucket.initializeBuckets();

		this.setGlobalDepth(directory.readGlobalDepth());

	}

	private int hash(int id) {
		return id & ((1 << globalDepth) - 1);
	}

	@Override
	public String getName() {
		return "Hashing Extensível";
	}
}
