package com.spinque.utils;

import java.io.IOException;
import java.util.Iterator;

public interface ClosableIterator<T> extends Iterator<T> {
	void close() throws IOException;
}
