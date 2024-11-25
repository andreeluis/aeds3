package model;

public class CompressionStats {
	public enum CompressionType {
		COMPRESS, DECOMPRESS
	}
	private CompressionType type;
	private String name;
	private long originalSize;
	private long compressedSize;
	private long timeTaken;

	// name
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// originalSize
	public long getOriginalSize() {
		if (type == CompressionType.DECOMPRESS) {
			throw new UnsupportedOperationException("Decompression does not have original size");
		}

		return originalSize;
	}

	public void setOriginalSize(long originalSize) {
		this.originalSize = originalSize;
	}

	// compressedSize
	public long getCompressedSize() {
		if (type == CompressionType.DECOMPRESS) {
			throw new UnsupportedOperationException("Decompression does not have compressed size");
		}

		return compressedSize;
	}

	public void setCompressedSize(long compressedSize) {
		this.compressedSize = compressedSize;
	}

	// timeTaken
	public long getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	// constructors
	public CompressionStats(String name, long originalSize, long compressedSize, long timeTaken) {
		this.type = CompressionType.COMPRESS;
		this.name = name;
		this.originalSize = originalSize;
		this.compressedSize = compressedSize;
		this.timeTaken = timeTaken;
	}

	public CompressionStats(String name, long timeTaken) {
		this.type = CompressionType.DECOMPRESS;
		this.name = name;
		this.timeTaken = timeTaken;
	}

	public float getCompressionRatio() {
		if (type == CompressionType.DECOMPRESS) {
			throw new UnsupportedOperationException("Decompression does not have compression ratio");
		}

		return 1 - ((float) compressedSize / originalSize);
	}

	@Override
	public String toString() {
		if (type == CompressionType.COMPRESS) {
			return String.format("%s: %.2f%% de taxa de compressão, %.2f segundos", name, getCompressionRatio() * 100, timeTaken / 1_000_000_000.0f);
		} else {
			return String.format("%s: %.2f segundos", name, timeTaken / 1_000_000_000.0f);
		}
	}
}
