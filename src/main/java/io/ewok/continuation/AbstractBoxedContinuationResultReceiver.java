package io.ewok.continuation;

public abstract class AbstractBoxedContinuationResultReceiver implements ContinuationResultReceiver {

	@Override
	public void returnValue(boolean value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(byte value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(char value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(short value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(int value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(long value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(float value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(double value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(Object[] value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(boolean[] value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(byte[] value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(char[] value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(short[] value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(int[] value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(long[] value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(float[] value) {
		this.returnValue((Object)value);
	}

	@Override
	public void returnValue(double[] value) {
		this.returnValue((Object)value);
	}

}
