package com.spinque.utils;

import java.io.IOException;
import java.util.Iterator;

/**
 * Builds one large iterator from several iterators that return the same type of object.
 * Will first return all entries of the first iterator, then all entries from the second, etc.
 * 
 */
public class CompoundIterator<C> implements ClosableIterator<C> {

	private Iterator<C> _iter;
	private final Iterator<Iterator<C>> _listIter;

	public CompoundIterator(Iterator<Iterator<C>> iterator) {
		_listIter = iterator;
		_iter = null;
	}
	
	@Override
	public boolean hasNext() {
		if (_iter == null || !_iter.hasNext())
			getNextIterator();
		return _iter != null && _iter.hasNext();
	}

	private void getNextIterator() {
		try {
			if (_iter != null && _iter instanceof ClosableIterator) {
				((ClosableIterator<?>) _iter).close();
				_iter = null;
			}
			while (_listIter.hasNext()) {
				_iter = _listIter.next();
				if (_iter.hasNext())
					break;
				if (_iter instanceof ClosableIterator) {
					((ClosableIterator<?>) _iter).close();
					_iter = null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public C next() {
		if (!hasNext())
			throw new IllegalStateException();
		return _iter.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		while (_iter != null) {
			if (_iter instanceof ClosableIterator) {
				((ClosableIterator<?>) _iter).close();
				_iter = null;
			}
			getNextIterator();
		}
	}
}
