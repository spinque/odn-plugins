package com.spinque.utils.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.spinque.utils.json.JSONUtils.JSONSerializable;

/**
 * In-memory representation of JSON object.
 * 
 */
public class JSONDOM implements JSONSerializable {
	
	public interface Node extends JSONSerializable {
		ObjectNode asObjectNode();
		ArrayNode asArrayNode();
		LiteralNode asLiteralNode();
	}
	
	public static final Node NULL_NODE = new Node() {
		@Override
		public void toJSON(JSONWriter out) throws IOException {
			out.writeNull();
		}

		@Override
		public ObjectNode asObjectNode() {
			throw new IllegalStateException();
		}

		@Override
		public ArrayNode asArrayNode() {
			throw new IllegalStateException();
		}

		@Override
		public LiteralNode asLiteralNode() {
			throw new IllegalStateException();
		}
	};
	
	public class ArrayNode implements Node {
		private final List<Node> _items = new ArrayList<Node>();

		@Override
		public void toJSON(JSONWriter out) throws IOException {
			out.startSeq();
			for (Node item : _items) {
				item.toJSON(out);
			}
			out.endSeq();
		}
		
		public void addItem(Node n) {
			_items.add(n);
		}
		
		public int getItemCount() {
			return _items.size();
		}
		public Node getItem(int i) {
			return _items.get(i);
		}
		
		@Override
		public ObjectNode asObjectNode() {
			throw new IllegalStateException();
		}
		@Override
		public ArrayNode asArrayNode() {
			return this;
		}

		@Override
		public LiteralNode asLiteralNode() {
			throw new IllegalStateException();
		}
	}
	
	public class LiteralNode implements Node {
		private final String _value;
		public LiteralNode(String value) {
			_value = value;
		}
		@Override
		public void toJSON(JSONWriter out) throws IOException {
			out.writeValue(_value);
		}
		
		public String getValue() {
			return _value;
		}
		
		@Override
		public ObjectNode asObjectNode() {
			throw new IllegalStateException();
		}			
		@Override
		public ArrayNode asArrayNode() {
			throw new IllegalStateException();
		}
		@Override
		public String toString() {
			return "[LiteralNode: " + _value + "]";
		}
		@Override
		public LiteralNode asLiteralNode() {
			return this;
		}
	}
	
	public class RawNode implements Node {
		private final String _value;
		public RawNode(String value) {
			_value = value;
		}
		@Override
		public void toJSON(JSONWriter out) throws IOException {
			out.writeRaw(_value);
		}
		
		public String getValue() {
			return _value;
		}
		
		@Override
		public ObjectNode asObjectNode() {
			throw new IllegalStateException();
		}			
		@Override
		public ArrayNode asArrayNode() {
			throw new IllegalStateException();
		}
		@Override
		public String toString() {
			return "[RawNode: " + _value + "]";
		}
		@Override
		public LiteralNode asLiteralNode() {
			throw new IllegalStateException();
		}
	}
	
	public class LiteralBooleanNode implements Node {
		private final boolean _value;
		public LiteralBooleanNode(boolean value) {
			_value = value;
		}
		
		public boolean getValue() {
			return _value;
		}
		
		@Override
		public void toJSON(JSONWriter out) throws IOException {
			out.writeBoolean(_value);
		}
		
		@Override
		public ObjectNode asObjectNode() {
			throw new IllegalStateException();
		}
		@Override
		public ArrayNode asArrayNode() {
			throw new IllegalStateException();
		}
		@Override
		public String toString() {
			return "[LiteralBoolean: " + _value + "]";
		}

		@Override
		public LiteralNode asLiteralNode() {
			throw new IllegalStateException();
		}
	}

	public class LiteralNumberNode implements Node {
		private final double _value;
		public LiteralNumberNode(double value) {
			_value = value;
		}
		public double getValue() {
			return _value;
		}
		
		@Override
		public void toJSON(JSONWriter out) throws IOException {
			out.writeDouble(_value);
		}
		
		@Override
		public ObjectNode asObjectNode() {
			throw new IllegalStateException();
		}
		@Override
		public ArrayNode asArrayNode() {
			throw new IllegalStateException();
		}
		@Override
		public LiteralNode asLiteralNode() {
			throw new IllegalStateException();
		}
		@Override
		public String toString() {
			return "[LiteralNumber: " + _value + "]";
		}
	}
	
	public class LiteralIntegerNode implements Node {
		private final long _value;
		public LiteralIntegerNode(long value) {
			_value = value;
		}
		public long getValue() {
			return _value;
		}
		
		@Override
		public void toJSON(JSONWriter out) throws IOException {
			out.writeInteger(_value);
		}
		
		@Override
		public ObjectNode asObjectNode() {
			throw new IllegalStateException();
		}
		@Override
		public ArrayNode asArrayNode() {
			throw new IllegalStateException();
		}
		
		@Override
		public String toString() {
			return "[LiteralInteger: " + _value + "]";
		}
		@Override
		public LiteralNode asLiteralNode() {
			throw new IllegalStateException();
		}
	}
	
	public class ObjectNode implements Node {
		private final Map<String, Node> _attributes = new HashMap<String, Node>();
		
		public void addAttribute(String key, Node value) {
			if (key == null) {
				throw new NullPointerException();
			}
 			_attributes.put(key, value == null ? NULL_NODE : value);
		}
		
		/**
		 * Helper function: creates a LiteralNode from the given string value
		 * and adds the LiteralNode as an attribute of this ObjectNode.
		 * Equivalent to:
		 *   addAttribute(key, jsondom.createLiteralNode(value));
		 *  
		 * @param key
		 * @param value
		 */
		public void addLiteralAttribute(String key, String value) {
			_attributes.put(key, value == null ? NULL_NODE : new LiteralNode(value));
		}

		public void addIntegerAttribute(String key, Long value) {
			_attributes.put(key, value == null ? NULL_NODE : new LiteralIntegerNode(value));
		}
		
		public void addBooleanAttribute(String key, boolean value) {
			if (key == null)
				throw new NullPointerException();
			_attributes.put(key, new LiteralBooleanNode(value));
		}

		@Override
		public void toJSON(JSONWriter out) throws IOException {
			out.startObject();
			for (Entry<String, Node> attr : _attributes.entrySet()) {
				out.writeKey(attr.getKey());
				attr.getValue().toJSON(out);
			}
			out.endObject();
		}
		
		public Node getChild(String key) {
			return _attributes.get(key);
		}
		
		public Set<String> getKeys() {
			return _attributes.keySet();
		}
		
		@Override
		public ObjectNode asObjectNode() {
			return this;
		}
		@Override
		public ArrayNode asArrayNode() {
			throw new IllegalStateException();
		}
		@Override
		public LiteralNode asLiteralNode() {
			throw new IllegalStateException();
		}

		public String getLiteralString(String key) {
			if (_attributes.get(key) == NULL_NODE) return null;
			LiteralNode ln = (LiteralNode) _attributes.get(key);
			return ln == null ? null : ln._value;
		}

		public boolean getLiteralBoolean(String key) {
			LiteralBooleanNode lbn = (LiteralBooleanNode) _attributes.get(key);
			return lbn == null ? null : lbn._value;
		}
		public long getLiteralInteger(String key) {
			LiteralIntegerNode lbn = (LiteralIntegerNode) _attributes.get(key);
			return lbn == null ? null : lbn._value;
		}
		public Double getLiteralDouble(String key) {
			if (_attributes.get(key) == null)
				return null;
			if (_attributes.get(key) instanceof LiteralIntegerNode)
				return 1d * ((LiteralIntegerNode) _attributes.get(key))._value;
			LiteralNumberNode lbn = (LiteralNumberNode) _attributes.get(key);
			return lbn == null ? null : lbn._value;
		}

		public void addNumberAttribute(String key, Double value) {
			_attributes.put(key, value == null ? NULL_NODE : new LiteralNumberNode(value));
		}

		public void addAttribute(String key, JSONSerializable value) {
			_attributes.put(key, new RawNode(JSONUtils.serializeJSON(value)));
		}
	}
	
	private Node _rootNode = null;
	
	public JSONDOM() {
	}
	
	@SuppressWarnings("unchecked")
	public <C extends Node> C getRoot() {
		return (C) _rootNode;
	}
	public void setRoot(Node node) {
		_rootNode = node;
	}
	
	public ArrayNode createArrayNode() {
		return new ArrayNode();
	}
	
	public ObjectNode createObjectNode() {
		return new ObjectNode();
	}
	
	public Node createLiteralNode(String value) {
		if (value == null)
			return NULL_NODE;
		return new LiteralNode(value);
	}

	@Override
	public void toJSON(JSONWriter out) throws IOException {
		_rootNode.toJSON(out);
	}

	public Node createNullNode() {
		return NULL_NODE;
	}

	public Node createLiteralNumber(double value) {
		return new LiteralNumberNode(value);
	}

	public Node createLiteralBoolean(boolean value) {
		return new LiteralBooleanNode(value);
	}

	public Node createLiteralInteger(long value) {
		return new LiteralIntegerNode(value);
	}
}
