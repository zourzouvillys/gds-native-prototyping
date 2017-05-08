package io.ewok.io.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author theo
 *
 */

public class ChunkedFileStrategy {

	long bytesPerPage = 4096;
	long pagesPerChunk = 4096;

	/**
	 * The initial offset to start writting at.
	 */

	public long initialOffset() {
		return 0;
	}

	/**
	 * fetches the chunk for the given offset.
	 */

	public int chunkForOffset(long offset) {
		return (int) (offset - (offset % (this.pagesPerChunk * this.bytesPerPage)));
	}

	/**
	 * returns the path for a given chunk.
	 */

	public Path pathForChunk(int chunk) {
		return Paths.get(String.format("%16d", (chunk * (this.pagesPerChunk * this.bytesPerPage))));
	}

}
