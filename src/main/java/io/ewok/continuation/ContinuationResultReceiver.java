package io.ewok.continuation;

/**
 * When an async method is called, this is passed in to receive the result,
 * based on the expected type. This avoids us having to box/unbox primitives.
 */

public interface ContinuationResultReceiver {

	/**
	 *
	 */

	void returnWithException(Throwable t);

	/**
	 *
	 */

	void returnValue(Object value);

	//

	void returnValue(boolean value);

	void returnValue(byte value);

	void returnValue(char value);

	void returnValue(short value);

	void returnValue(int value);

	void returnValue(long value);

	void returnValue(float value);

	void returnValue(double value);

	// --

	void returnValue(Object[] value);

	void returnValue(boolean[] value);

	void returnValue(byte[] value);

	void returnValue(char[] value);

	void returnValue(short[] value);

	void returnValue(int[] value);

	void returnValue(long[] value);

	void returnValue(float[] value);

	void returnValue(double[] value);

}
