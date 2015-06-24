package com.spinque.utils;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Iterators work by asking if it has a next one, and if it has, getting the next one.
 * 
 * In some cases, if null is not a valid item in an iterator, it is easier to just
 * directly request the next item. 
 * 
 *  In this way, the class that wants to offer an iterator, only needs to implement the NextIterator class.
 * 
 * @param <C>
 */
public class GetNextIterator<C> implements ClosableIterator<C> {

		private final NextIterator<C> _iter;
		private C _next = null;
		private boolean _eof = false;

		public interface NextIterator<D> {
			/**
			 * @return null iff end of iterator has been reached, otherwise next element.
			 */
			D getNext();
			void close();
		}
		
		public GetNextIterator(NextIterator<C> iter) {
			_iter = iter;
		}
		
		@Override
		public boolean hasNext() {
			if (!_eof && _next == null) 
				_next = _iter.getNext();
			if (_next == null)
				_eof = true;
			return _next != null;
		}

		@Override
		public C next() {
			if (!_eof && _next == null) 
				_next = _iter.getNext();
			if (_next == null)
				throw new NoSuchElementException();
			C result = _next;
			_next = null;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() throws IOException {
			_iter.close();
		}
}
