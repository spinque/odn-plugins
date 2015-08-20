package com.spinque.utils;

import java.io.IOException;

public class SimpleReader {

	private final String _data;
	private final int _end; 
	
	int _offset;
	int _mark = 0;
	
	public SimpleReader(String data) {
		this(data, data.length());
	}

	public SimpleReader(String data, int end) {
		_offset = 0;
		_data = data;
		_end = end;
	}
	
	@Override
	public String toString() {
		String causeStr = _data.substring(0, _offset);
		if (causeStr.length() > 50)
			causeStr = causeStr.substring(causeStr.length() - 40);
		String causeStr2 = _data.substring(_offset+1);
		if (causeStr2.length() > 50)
			causeStr2 = causeStr2.substring(0, 40);
		return "[SimpleReader " + causeStr + " __ " + _data.charAt(_offset) + " __ " + causeStr2 + ")]";
	}
	
	public int mark() {
		_mark = _offset;
		return _offset;
	}
	public CharSequence getMarked() {
		return _data.subSequence(_mark, _offset);
	}
	public char current() {
		return _data.charAt(_offset);
	}
	public void take() {
		_offset++;
	}
	private void take(int length) {
		_offset+=length;
	}

	public boolean eol() {
		return _offset >= _end;
	}
	public String remainder(int size) {
		return _data.substring(_offset , Math.min(_offset+size, _end));
	}

	public String takeToken(char x, boolean allowEmptyValue) throws IOException {
		mark();
		boolean escaped = false;
		while (!eol()) {
			if (!escaped && current() == x)
				break;
			escaped = escaped ? false : (current() == '\\');
			take();
		}

		if (eol() || (getMarked().length() == 0 && !allowEmptyValue)) {
			throw new IOException("unexpected end of token name: " + remainder(40));
		}
		String name = getMarked().toString();

		take();
		return name;
	}

	public void skipWhitespace() {
		while (!eol() && "\r\n \t".indexOf(current()) >=0 )
			take();
	}

	public String readWhile(String objs) throws IOException {
		mark();
		while (!eol() && objs.indexOf(current()) >= 0) 
			take();

		if (eol() || getMarked().length() == 0) {
			throw new IOException("unexpected end of token name: " + getMarked() + " - " + remainder(40));
		}
		String name = getMarked().toString();
		return name;
	}

	public String readExact(String[] options) {
		for (String option : options) {
			if (remainder(option.length()).startsWith(option)) {
				take(option.length());
				return option;
			}
		}
		return null;
	}
}