package com.spinque.utils.json;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spinque.utils.json.JSONDOM.ArrayNode;
import com.spinque.utils.json.JSONDOM.LiteralBooleanNode;
import com.spinque.utils.json.JSONDOM.LiteralIntegerNode;
import com.spinque.utils.json.JSONDOM.LiteralNode;
import com.spinque.utils.json.JSONDOM.LiteralNumberNode;
import com.spinque.utils.json.JSONDOM.Node;
import com.spinque.utils.json.JSONDOM.ObjectNode;

/**
 * Provides a mapping from a JSON object to an XML object.
 * 
 * There is no standard as far as is known at the moment about how
 * to do this. Therefore the mapping is done using our own (made-up).
 * 
 * <table border="1">
 * <tr><td>JSON</td><td>XML</td></tr>
 * <tr><td>{}</td><td>&lt;object&gt; - element with &lt;property&gt;-elements for each property. Each property-element has a name-attribute to denote its key.</td></th>  
 * <tr><td>[]</td><td>&lt;array&gt; - element.</td></th>
 * <tr><td>literals</td><td>&lt;literal&gt; - element with the value as the text-content of the element.</td></th>
 * </table> 
 * 
 * Example:
 * <pre>
 * { 
 *  "a": 100,
 *  "b": [ "aap", "noot" ]
 * }
 * </pre>
 * would be converted to:
 * <pre>
 *   &lt;object&gt;
 *     &lt;property name="a"&gt;&lt;literal&gt;100&lt;/literal&gt;&lt;/a&gt;
 *     &lt;property name="b"&gt;
 *       &lt;array&gt;
 *         &lt;literal&gt;aap&lt;/literal&gt;
 *         &lt;literal&gt;noot&lt;/literal&gt;
 *       &lt;/array&gt;
 *     &lt;/property&gt;
 *   &lt;/object&gt;
 * </pre>
 */
public class JSONXMLWrapper {
	
	/* the names below make up the mapping to XML. */
	private static final String LITERAL_NAME = "literal";
	private static final String ARRAY_NAME = "array";
	private static final String OBJECT_NODE_NAME = "object";
	private static final String OBJECT_ATTRIBUTE_NAME = "property";
	private static final String OBJECT_ATTRIBUTE_KEY_NAME = "name"; /* written as an attribute in XML */
	private static final String NULL_NAME = "null";
	
	private JSONDOM _jdom;

	public JSONXMLWrapper(JSONDOM jdom) {
		_jdom = jdom;
	}
	
//	@Override
	public Element toXML(Document doc) {
		Node root = _jdom.getRoot();
		return toXML(root, doc);
	}

	private Element toXML(Node node, Document doc) {
		if (node instanceof ObjectNode) {
			ObjectNode on = (ObjectNode) node;
			Element result = doc.createElement(OBJECT_NODE_NAME);
			for (String key : on.getKeys()) {
				Element attrElem = doc.createElement(OBJECT_ATTRIBUTE_NAME);
				attrElem.setAttribute(OBJECT_ATTRIBUTE_KEY_NAME, key);
				attrElem.appendChild(toXML(on.getChild(key), doc));
				result.appendChild(attrElem);
			}
			return result;
		}
		if (node instanceof ArrayNode) {
			ArrayNode on = (ArrayNode) node;
			Element result = doc.createElement(ARRAY_NAME);
			for (int i = 0; i < on.getItemCount(); i++) {
				result.appendChild(toXML(on.getItem(i), doc));
			}
			return result;
		}
		if (node == JSONDOM.NULL_NODE) {
			Element result = doc.createElement(NULL_NAME);
			return result;
		}
		if (node instanceof LiteralNode) {
			LiteralNode on = (LiteralNode) node;
			Element result = doc.createElement(LITERAL_NAME);
			result.setAttribute("type", "string");
			result.setTextContent(on.getValue());
			return result;
		}
		if (node instanceof LiteralBooleanNode) {
			LiteralBooleanNode on = (LiteralBooleanNode) node;
			Element result = doc.createElement(LITERAL_NAME);
			result.setAttribute("type", "boolean");
			result.setTextContent("" + on.getValue());
			return result;
		}
		if (node instanceof LiteralIntegerNode) {
			LiteralIntegerNode on = (LiteralIntegerNode) node;
			Element result = doc.createElement(LITERAL_NAME);
			result.setAttribute("type", "integer");
			result.setTextContent("" + on.getValue());
			return result;
		}
		if (node instanceof LiteralNumberNode) {
			LiteralNumberNode on = (LiteralNumberNode) node;
			Element result = doc.createElement(LITERAL_NAME);
			result.setAttribute("type", "number");
			result.setTextContent("" + on.getValue());
			return result;
		}
		throw new IllegalArgumentException("don't know how to handle " + node.getClass());
	}
}
