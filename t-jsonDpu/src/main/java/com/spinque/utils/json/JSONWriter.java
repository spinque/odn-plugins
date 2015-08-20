package com.spinque.utils.json;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;

import com.spinque.utils.DateUtils;
import com.spinque.utils.Utils;
import com.spinque.utils.json.JSONUtils.JSONSerializable;

/**
 * Simple JSON serializer
 * 
 * There is now a default-setting for outputting 'non-dense' or 'dense' formatting. 
 * This basically means either new-lines (and indentation) or no-new-lines between items.
 * 
 * Per object dense/non-dense formatting can be explicitly set. This setting will hold for
 * that object and all objects below (unless descendants explicitly overrule the setting 
 * again).
 * 
 * If nothing is specified when starting the JSONWriter, non-dense formatting is used.
 * 
 * Features:
 * - Properly escapes string values 
 * - Remembers when to put comma's, indentation, etc.
 * - Tries to detect some errors, but it is a best effort attempt. 
 *   The caller should make sure valid JSON is generated.
 */
public class JSONWriter {

	private static final int MAX_TREE_DEPTH = 32;

	public static boolean DEBUG = false; // be careful: it will generate invalid JSON if debug is set
	
	private static final String WHITESPACE = "                                                       ";
	private static final int TAB_WIDTH = 4;
	private final Writer _out;
	
	private static final int OBJ_TYPE = 1;
	private static final int SEQ_TYPE = 2;
	
	/* state */
	private int[] _types = new int[MAX_TREE_DEPTH]; // 0 = root
	private boolean[] _dense = new boolean[MAX_TREE_DEPTH]; // 0 = root (== unused)
	private boolean _firstEntry = true; /* determines whether the next item needs to be comma-separated from the first */
	private boolean _keyWritten = true; /* determines whether from a key-value-pair the key has already been written */
	
	private int _level = 0;
	
	/**
	 * Creates a writer that writes JSON. It defaults to non-dense formatting.
	 *  
	 * @param writer
	 */
	public JSONWriter(Writer writer) {
		this(writer, false);
	}
	
	/**
	 * Creates a writer that writes JSON. Depending on defaultDense 
	 * it will default to dense or non-dense formatting.
	 *  
	 * @param writer
	 */
	public JSONWriter(Writer writer, boolean defaultDense) {
		_out = writer;
		_dense[0] = defaultDense;
	}
	
	/* Object methods */
	public JSONWriter startObject() throws IOException {
		return startObject(_dense[_level]);
	}
	
	public JSONWriter startObject(boolean dense) throws IOException {
		if (_types[_level] == OBJ_TYPE && !_keyWritten) throw new IllegalStateException();
		
		if (!_keyWritten && !_firstEntry) _out.write(_dense[_level] ? "," : ",\n");
		if (!_keyWritten && !_dense[_level]) doIndent();
		_out.write(dense ? "{" : "{\n");
		
		/* update state */
		_level++;
		_dense[_level] = dense;
		_types[_level] = OBJ_TYPE;
		_firstEntry = true;
		_keyWritten = false;
		return this;
	}

	public JSONWriter startObject(String name) throws IOException {
		return startObject(name, _dense[_level]);
	}
	public JSONWriter startObject(String name, boolean dense) throws IOException {
		if (_types[_level] == SEQ_TYPE || _keyWritten) throw new IllegalStateException();
		if (!_firstEntry) _out.write(_dense[_level] ? "," : ",\n");
		if (!_dense[_level]) doIndent();
		_out.write(JSONUtils.escape(name) + (dense ? ": {" : ": {\n"));
		_firstEntry = true;
		_keyWritten = false;
		_level++;
		_dense[_level] = dense;
		_types[_level] = OBJ_TYPE;
		return this;
	}
	
	public JSONWriter endObject() throws IOException {
		if (_keyWritten) throw new IllegalStateException();
		if (!_dense[_level]) _out.write("\n");
		_level--;
		if (!_dense[_level+1]) doIndent();
		
		
		_out.write("}");
		_firstEntry = false;
		
		if (_level == 0) _out.write('\n'); // end-of-document
		return this;
	}
	
	/* Array methods */
	
	public JSONWriter startSeq() throws IOException {
		return startSeq(_dense[_level]);
	}
	public JSONWriter startSeq(boolean dense) throws IOException {
		if (_types[_level] == OBJ_TYPE && !_keyWritten) throw new IllegalStateException("no key written");
		if (!_firstEntry && !_keyWritten) _out.write(_dense[_level] ? "," : ",\n");
		if (!_dense[_level] && !_keyWritten) doIndent();
		_out.write(dense ? "[" : "[\n");
		_firstEntry = true;
		_level++;
		_dense[_level] = dense;
		_types[_level] = SEQ_TYPE;
		_keyWritten = false;
		return this;
	}
	
	public JSONWriter startSeq(String name) throws IOException {
		return startSeq(name, _dense[_level]);
	}
	
	public JSONWriter startSeq(String name, boolean dense) throws IOException {
		if (_types[_level] == SEQ_TYPE) throw new IllegalStateException();
		if (!_firstEntry) _out.write(_dense[_level] ? "," : ",\n");
		if (!_dense[_level]) doIndent();
		_out.write(JSONUtils.escape(name) + (dense ? ": [" : ": [\n"));
		_firstEntry = true;
		_level++;
		_dense[_level] = dense;
		_types[_level] = SEQ_TYPE;
		return this;
	}
	
	public JSONWriter endSeq() throws IOException {
		if (_keyWritten)
			throw new IllegalStateException();
		if (!_dense[_level]) _out.write("\n");
		_level--;
		if (!_dense[_level+1]) doIndent();
		_out.write("]");
		_firstEntry = false;
		
		if (_level == 0) _out.write('\n');
		return this;
	}
	
	/* Literal methods */
	
	public JSONWriter writeStringValue(String key, String value) throws IOException {
		if (_keyWritten)
			throw new IllegalStateException();
		if (!_firstEntry) _out.write(_dense[_level] ? "," : ",\n");
		if (!_dense[_level]) doIndent();
		_out.write(JSONUtils.escape(key) + ": " + JSONUtils.escape(value));
		_firstEntry = false;
		return this;
	}
	
	public JSONWriter writeDateValue(String key, DateTime value) throws IOException {
		return writeStringValue(key, DateUtils.printDate(value));
	}

	/**
	 * @param value
	 * @return the writer itself
	 * @throws IOException
	 */
	public JSONWriter writeString(String value) throws IOException {
		if (!_keyWritten && !_firstEntry) _out.write(_dense[_level] ? "," : ",\n");
		if (!_keyWritten && !_dense[_level]) doIndent();
		_out.write(JSONUtils.escape(value));
		_firstEntry = false;
		_keyWritten = false;
		return this;
	}

	public JSONWriter writeBooleanValue(String key, boolean b) throws IOException {
		if (_keyWritten || _types[_level] != OBJ_TYPE)
			throw new IllegalStateException();
		if (!_firstEntry) _out.write(",\n");
		doIndent();
		_out.write(JSONUtils.escape(key) + ": " + (b ? "true" : "false"));
		_firstEntry = false;
		_keyWritten = false;
		return this;
	}

	public JSONWriter writeIntegerValue(String key, long n) throws IOException {
		if (_keyWritten || _types[_level] != OBJ_TYPE)
			throw new IllegalStateException();
//		if (n > Integer.MAX_VALUE || n < Integer.MIN_VALUE)
//			throw new IOException("Javascript does not natively support integers larger than 32 bit.");
		if (!_firstEntry) _out.write(",\n");
		doIndent();
		_out.write(JSONUtils.escape(key) + ": " + n);
		_firstEntry = false;
		_keyWritten = false;
		return this;
	}

	public JSONWriter writeIntegerArray(String key, int[] values, boolean dense) throws IOException {
		startSeq(key, dense);
		for (int n : values) {
			writeInteger(n);
		}
		endSeq();
		return this;
	}

	public JSONWriter writeDoubleValue(String key, double r) throws IOException {
		if (_keyWritten || _types[_level] != OBJ_TYPE)
			throw new IllegalStateException();
		if (!_firstEntry) _out.write(_dense[_level] ? "," : ",\n");
		if (!_dense[_level]) doIndent();
		_out.write(JSONUtils.escape(key) + ": " + Utils.formatDouble(r));
		_firstEntry = false;
		_keyWritten = false;
		return this;
	}

	/**
	 * Warning: json must be valid json (no checks are performed).
	 * 
	 * @param key
	 * @param json
	 * @return the writer itself
	 * @throws IOException
	 */
	public JSONWriter writeRawValue(String key, String json) throws IOException {
		if (_keyWritten || _types[_level] != OBJ_TYPE)
			throw new IllegalStateException();
		if (!_firstEntry) _out.write(_dense[_level] ? "," : ",\n");
		if (!_dense[_level]) doIndent();
		_out.write(JSONUtils.escape(key) + ": " + json);
		_firstEntry = false;
		return this;
	}

	public JSONWriter writeNullValue(String key) throws IOException {
		if (_keyWritten || _types[_level] != OBJ_TYPE)
			throw new IllegalStateException();
		writeRawValue(key, "null");
		return this;
	}
	
	/* special methods: for not writing a key-value pair at once, but as separate items */
	
	public JSONWriter writeKey(String value) throws IOException {
		if (_keyWritten || _types[_level] != OBJ_TYPE)
			throw new IllegalStateException();
		if (!_firstEntry) _out.write(",\n");
		doIndent();
		_out.write(JSONUtils.escape(value) + ": ");
		_keyWritten = true;
//		_firstEntry = false;
		return this;
	}

	public JSONWriter writeNull() throws IOException {
		if (_types[_level] == OBJ_TYPE && !_keyWritten)
			throw new IllegalStateException();
		if (_types[_level] == SEQ_TYPE && !_firstEntry)
			 _out.write(",\n");
		_out.write("null");
		_firstEntry = false;
		_keyWritten = false; 
		return this;
	}

	public JSONWriter writeValue(String value) throws IOException {
		if (_types[_level] == OBJ_TYPE && !_keyWritten)
			throw new IllegalStateException();
		
		if (_types[_level] == SEQ_TYPE && !_firstEntry)
			 _out.write(",\n");
		_out.write(JSONUtils.escape(value));
		_firstEntry = false;
		_keyWritten = false;
		return this;
	}

	public JSONWriter writeBoolean(boolean value) throws IOException {
		if (_types[_level] == OBJ_TYPE && !_keyWritten)
			throw new IllegalStateException();
		if (_types[_level] == SEQ_TYPE && !_firstEntry)
			 _out.write(",\n");
		_out.write(value ? "true" : "false");
		_firstEntry = false;
		_keyWritten = false;
		return this;
	}

	public JSONWriter writeDouble(double value) throws IOException {
		if (_types[_level] == OBJ_TYPE && !_keyWritten)
			throw new IllegalStateException();
		if (_types[_level] == SEQ_TYPE && !_firstEntry)
			 _out.write(",\n");
		_out.write("" + value);
		_firstEntry = false;
		_keyWritten = false;
		return this;		
	}
	
	public JSONWriter writeInteger(long value) throws IOException {
		if (_types[_level] == OBJ_TYPE && !_keyWritten)
			throw new IllegalStateException();
		if (_types[_level] == SEQ_TYPE && !_firstEntry)
			 _out.write(_dense[_level] ? "," : ",\n");
		_out.write("" + value);
		_firstEntry = false;
		_keyWritten = false;
		return this;		
	}
	
	/* document utilities (not sure whether they should exist). */
	
	public void flush() throws IOException {
		_out.flush();
	}

	public void close() throws IOException {
		_out.close();
	}
	
	/* other utilities */
	
	/* pretty print utility */ 
	private void doIndent() throws IOException {
		_out.write(WHITESPACE, 0, Math.min(_level*TAB_WIDTH, WHITESPACE.length()));
	}
	
	public JSONWriter writeNode(String key, JSONSerializable value) throws IOException {
		writeKey(key);
		if (value == null) {
			writeNull();
		} else {
			value.toJSON(this);
		}
		return this;
	}

	public <C extends JSONSerializable> JSONWriter writeSeq(String key, List<C> values, boolean dense) throws IOException {
		startSeq(key, dense);
		if (values == null) { 
			writeNull();
		} else {
			for (C item : values) {
				item.toJSON(this);
			}
		}
		endSeq();
		return this;
	}

	public <C extends JSONSerializable> JSONWriter writeSeq(List<C> values, boolean dense) throws IOException {
		startSeq(dense);
		if (values == null) { 
			writeNull();
		} else {
			for (C item : values) {
				item.toJSON(this);
			}
		}
		endSeq();
		return this;		
	}

	/**
	 * After having written this content, it is assumed that the writer
	 * is not half-way writing a key-value pair. 
	 * 
	 * Note: this method may not be used to write a key of an object.
	 * 
	 * @param json Some json data as a string, may contain object, literal value
	 * 			   sequence. No checks are performed on this value. 
	 * 			   
	 */
	public JSONWriter writeRaw(String json) throws IOException {
		if (!_keyWritten && !_firstEntry) _out.write(",\n");
		doIndent();
		_out.write(json);
		_firstEntry = false;
		_keyWritten = false;
		return this;
	}

	public void writeNode(JSONSerializable obj) throws IOException {
		if (obj == null)
			writeNull();
		else
			obj.toJSON(this);
	}

	/**
	 * Writes the map as a new object, each entry in the map becomes a key/value-item 
	 * in the JSON object.
	 * 
	 * @param key
	 * @param options
	 * @return the writer itself
	 * @throws IOException
	 */
	public JSONWriter writeObjectStrings(String key, Map<String, String> options) throws IOException {
		startObject(key);
		for (Entry<String, String> entry : options.entrySet()) {
			writeStringValue(entry.getKey(), entry.getValue());
		}
		endObject();
		return this;
	}
	
	/**
	 * Writes the map as a new object, each entry in the map becomes a key/value-item 
	 * in the JSON object.
	 * 
	 * @param key
	 * @param options
	 * @return the writer itself
	 * @throws IOException
	 */
	public <C extends JSONSerializable> JSONWriter writeObject(String key, Map<String, C> options) throws IOException {
		startObject(key);
		for (Entry<String, C> entry : options.entrySet()) {
			writeNode(entry.getKey(), entry.getValue());
		}
		endObject();
		return this;
	}
}
