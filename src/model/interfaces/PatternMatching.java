package model.interfaces;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Optional;

public interface PatternMatching {
	public Optional<List<Long>> search(RandomAccessFile file, byte[] pattern) throws IOException;
}
