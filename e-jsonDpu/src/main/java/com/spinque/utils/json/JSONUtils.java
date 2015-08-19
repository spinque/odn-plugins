package com.spinque.utils.json;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spinque.utils.json.JSONDOM.ArrayNode;
import com.spinque.utils.json.JSONDOM.LiteralBooleanNode;
import com.spinque.utils.json.JSONDOM.LiteralIntegerNode;
import com.spinque.utils.json.JSONDOM.LiteralNode;
import com.spinque.utils.json.JSONDOM.LiteralNumberNode;
import com.spinque.utils.json.JSONDOM.Node;
import com.spinque.utils.json.JSONDOM.ObjectNode;

public class JSONUtils {

	public interface JSONSerializable {
		void toJSON(JSONWriter out) throws IOException;
	}
	
	public interface JSONDOMSerializable {

		public Node asJSON(JSONDOM jdom);
	}


	public static final String EMPTY_OBJECT_STR =  "{ }";

	/**
	 * Serializes a JSON object to a Writer.
	 * 
	 * Does not close the writer.
	 * 
	 * @param object
	 * @param out
	 * @throws IOException
	 */
	public static void serialize(JSONDOMSerializable object, Writer out) throws IOException {
		serialize(object, new JSONWriter(out));
	}
	
	/**
	 * Serializes a JSON object to a JSONWriter.
	 * 
	 * Does not close the writer.
	 * 
	 * @param object
	 * @param out
	 * @throws IOException
	 */
	public static void serialize(JSONDOMSerializable object, JSONWriter out) throws IOException {
		JSONDOM jdom = new JSONDOM();
		Node n = object.asJSON(jdom);
		jdom.setRoot(n);
		jdom.toJSON(out);
	}
	
	/**
	 * Serializes to a {@link OutputStream}, using character set UTF-8.
	 * 
	 * Does not close the {@link OutputStream}.
	 * 
	 * @param object
	 * @param out
	 * @throws IOException
	 */
	public static void serialize(JSONDOMSerializable object, OutputStream out) throws IOException {
		Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		serialize(object, w);
	}
	
	public static void serialize(JSONDOMSerializable object, File output) throws IOException {
		OutputStream osw = new FileOutputStream(output);
		try {
			Writer w = new OutputStreamWriter(osw, Charset.forName("UTF-8"));
			serialize(object, w);
		} finally {
			osw.close();
		}
	}
	
	/**
	 * Serializes to a {@link String}.
	 * Warning: do not use for potentially large JSON objects. 
	 * 
	 * @param object
	 * @return the serialized form
	 * @throws IOException
	 */
	public static String serialize(JSONDOMSerializable object) throws IOException {
		StringWriter sw = new StringWriter();
		serialize(object, sw);
		return sw.toString();
	}
	
	public static String serializeJSON(List<? extends JSONSerializable> sd) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JSONWriter out = new JSONWriter(new OutputStreamWriter(baos));
		try {
			out.startSeq();
			for (JSONSerializable s : sd) {
				s.toJSON(out);
			}
			out.endSeq();
		} finally {
			out.close();
		}
		return baos.toString("UTF-8");
	}

	/**
	 * Non-dense serialization.
	 */
	public static String serializeJSON(JSONSerializable sd) {
		return serializeJSON(sd, false);
	}

	public static String serializeJSON(JSONSerializable sd, boolean defaultDense) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JSONWriter out = new JSONWriter(new OutputStreamWriter(baos), defaultDense);
			try {
				sd.toJSON(out);
			} finally {
				out.close();
			}
			return baos.toString("UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static String escape(String value) {
		return escape(value, false);
	}
	
	private static String escape(String value, boolean debug) {
		if (value == null) return "null";

		StringBuilder sb = new StringBuilder();
		sb.append('"');
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			switch (ch) {
			case '\\':
				sb.append("\\\\");
				break;
			case '"':
				sb.append("\\\"");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append((debug) ? '\n' : "\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				// escaping non-system Unicode characters seems optional. 
				// - to be on the safe side: use Googles list of characters 
				// (http://code.google.com/p/json-simple/source/browse/trunk/src/org/json/simple/JSONValue.java)
				// - com.google.gwt.core.client.JsonUtils uses the following list:
				//   \x00-\x1f \xad \u0600-\u0603 \u06dd \u070f \u17b4 \u17b5 \u200c-\u200f 
				//   \u2028-\u202e \u2060-\u2063 \u206a-\u206f \ufeff \ufff9-\ufffb"\\
				// - some other implementations (http://www.json.org/java/index.html) don't encode \u007f (they start at \u0080)
				// Reference: http://www.ietf.org/rfc/rfc4627.txt
				// Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if((ch < '\u0020') || (ch>='\u007F' && ch<'\u00A0') || (ch>='\u2000' && ch<'\u2100')){
		                String ss=Integer.toHexString(ch);
		                sb.append("\\u");
		                for(int k=0;k<4-ss.length();k++){
		                        sb.append('0');
		                }
		                sb.append(ss.toUpperCase());
		        }
		        else{
		                sb.append(ch);
		        }
			}
		}
		sb.append('"');
		return sb.toString();
	}

	/**
	 * Inverse of the JSONUtils.escape() method.
	 * 
	 * Expects a string with the double-quotes already removed.
	 * 
	 * @param data
	 * @return the de-escaped string
	 * @throws IOException
	 */
	public static String deescape(String data) throws IOException {
		char[] result = new char[data.length()];
		int pos = 0;
		boolean escaped = false;
		for (int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			if (escaped) {
				switch (c) {
				case '\\':
					result[pos++] = '\\';
					break;
				case '"':
					result[pos++] = '"';
					break;
				case 'b':
					result[pos++] = '\b';
					break;
				case 'f':
					result[pos++] = '\f';
					break;
				case '/':
					result[pos++] = '/';
					break;
				case 'r':
					result[pos++] = '\r';
					break;
				case 't':
					result[pos++] = '\t';
					break;
				case 'n':
					result[pos++] = '\n';
					break;
				case 'u':
					if (data.length() < i+5)
						throw new IOException("incomplete code-point: " + data.substring(i+1));
					try {
						int codePoint = Integer.parseInt(data.substring(i+1, i+5), 16);
						pos += Character.toChars(codePoint, result, pos);
						i+=4; // increment the string pointer...
					} catch (NumberFormatException e) {
						throw new IOException("no valid code point: \\u" + data.substring(i+1, i+5));
					}
					break;
				default:
					throw new IOException("dont know how to de-escape: " + c);
				}
				escaped = false;
			} else {
				switch (c) {
				case '\\':
					escaped = true;
					break;
				default:
					result[pos++] = c;
				}
			}
		}
		return new String(result, 0, pos);
	}

	@Deprecated
	public static void json2XML(Document doc, Element root, JSONDOM source) {
		if (root.getOwnerDocument() != doc)
			throw new IllegalArgumentException();
		
		Node n = source.getRoot();
		json2XML(root, n);
	}

	private static void json2XML(Element root, Node n) {
		if (n instanceof ObjectNode)
			object2XML(root, (ObjectNode) n);
		else if (n instanceof ArrayNode)
			array2XML(root, (ArrayNode) n);
		else if (n instanceof LiteralNode) 
			literalString2XML(root, (LiteralNode) n);
		else if (n instanceof LiteralBooleanNode) 
			literalBoolean2XML(root, (LiteralBooleanNode) n);
		else if (n instanceof LiteralIntegerNode) 
			literalInteger2XML(root, (LiteralIntegerNode) n);
		else if (n instanceof LiteralNumberNode) 
			literalNumber2XML(root, (LiteralNumberNode) n);
		else if (n == JSONDOM.NULL_NODE)
			null2XML(root, (LiteralNode) n);
		else {
			throw new IllegalStateException("dont know what to do with " + n);
		}
	}

	private static void null2XML(Element root, LiteralNode n) {
		// do nothing...
	}

	private static void literalString2XML(Element root, LiteralNode n) {
		root.setTextContent(n.getValue());
	}
	private static void literalBoolean2XML(Element root, LiteralBooleanNode n) {
		root.setTextContent("" + n.getValue());
	}
	private static void literalInteger2XML(Element root, LiteralIntegerNode n) {
		root.setTextContent("" + n.getValue());
	}
	private static void literalNumber2XML(Element root, LiteralNumberNode n) {
		root.setTextContent("" + n.getValue());
	}

	private static void array2XML(Element root, ArrayNode n) {
		for (int i = 0; i < n.getItemCount(); i++) {
			Element valueElem = root.getOwnerDocument().createElement("item");
			root.appendChild(valueElem);
			json2XML(valueElem, n.getItem(i));
		}
	}

	private static void object2XML(Element root, ObjectNode n) {
		for (String key : n.getKeys()) {
			Element valueElem = root.getOwnerDocument().createElement("property");
			root.appendChild(valueElem);
			valueElem.setAttribute("name", key);
			json2XML(valueElem, n.getChild(key));
		}
	}

	public static JSONDOM createJSONSequence(Collection<? extends Object> objs) throws IOException {
		JSONDOM dom = new JSONDOM();
		ArrayNode l = dom.createArrayNode();
		dom.setRoot(l);
		
		for (Object n : objs) {
			if (n instanceof String) {
				l.addItem(dom.createLiteralNode((String) n));
			} else if (n instanceof Long) {
				l.addItem(dom.createLiteralInteger((Long) n));
			} else if (n instanceof Double) {
				l.addItem(dom.createLiteralNumber((Double) n));
			} else if (n instanceof Boolean) {
				l.addItem(dom.createLiteralBoolean((Boolean) n));
			} else {
				System.err.println("Don't know how to handle: " + n);
			}
		}
		
		return dom;
	}

	public static Collection<String> asStringList(ArrayNode node) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < node.getItemCount(); i++) {
			result.add(node.getItem(i).asLiteralNode().getValue());
		}
		return result;
	}

	public static JSONDOM asJSONDOM(JSONSerializable data) throws IOException {
		String json = JSONUtils.serializeJSON(data);
		return JSONParser.parse(json);
	}
}
