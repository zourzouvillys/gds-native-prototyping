package io.ewok.io;

@FunctionalInterface
public interface BlockAccessCallback<T> {

	void complete(BlockAccessResult result, T attachment);


}
