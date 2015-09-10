package com.spinque.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Utils {
	
	private Utils() { /** Prevents instantiation. */ }
	
	static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	static final TransformerFactory tf = TransformerFactory.newInstance();
	static DocumentBuilder db = null;
	static DocumentBuilderFactory dbfns = null; 
	static DocumentBuilder dbns = null;
	
	private static synchronized void initialize() {
		if (db != null)
			return;
		try {
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			db = dbf.newDocumentBuilder();
			dbfns = DocumentBuilderFactory.newInstance();
			dbfns.setNamespaceAware(true);
			dbns = dbfns.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

//	ThreadLocal<NumberFormat> NUMBER_SERIALIZER = new ThreadLocal<NumberFormat>();
	
	private final static NumberFormat NUMBER_SERIALIZER = NumberFormat.getNumberInstance(Locale.ROOT);
	static {
		NUMBER_SERIALIZER.setMaximumFractionDigits(8);
		NUMBER_SERIALIZER.setMinimumFractionDigits(1);
		NUMBER_SERIALIZER.setGroupingUsed(false);
	}
	
	/**
     * Convenience method for directly writing an XML tree to a file.
     * Uses: writeXML(OutputStream out, Element elem)
     */
	public static void writeXML(File output, Element elem) throws IOException {
		OutputStream out = new FileOutputStream(output);
		try {
			writeXML(out, elem, false);
		} finally {
			out.close();
		}
	}
	
	/**
     * Convenience method for directly writing an list of XML elements to a file.
     * Uses: writeXML(File output, Element elem)
     */
	public static void writeXML(File output, List<Element> elems, String rootTagName) throws IOException {
		if (db == null) initialize();
		synchronized (db) { 
			Document doc = db.newDocument();
			Element rootElement = doc.createElement(rootTagName);
			doc.appendChild(rootElement);
			for (Element e : elems) {
				// Import nodes, because they might have been created by different Document objects
				rootElement.appendChild(doc.importNode(e,true));
			}
			writeXML(output, doc.getDocumentElement());
		}
	}

	/**
	 * Warning: don't parse large documents with 
	 * this... (it creates an in-memory representation 
	 * of the document!)
	 * 
	 * @param doc
	 * @return a string representation of the document.
	 * @throws IOException
	 */
	public static void writeXML(OutputStream out, Element elem, boolean hideXMLDeclaration) throws IOException {
		try {
			Transformer tr = tf.newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD,"xml");
			tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, hideXMLDeclaration ? "yes" : "no");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			tr.transform( new DOMSource(elem),new StreamResult(out));
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Warning: don't parse large documents with 
	 * this... (it creates an in-memory representation 
	 * of the document!)
	 * 
	 * @param doc
	 * @return a string representation of the document.
	 * @throws IOException
	 */
	public static void writeXML(Writer out, Node elem) throws IOException {
		try {
			Transformer tr = tf.newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD,"xml");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			tr.transform( new DOMSource(elem),new StreamResult(out));
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}
	
	public static void writeXML(File destination, XMLSerializable obj) throws IOException {
		if (db == null) initialize();
		synchronized (db) { 
			Document doc = db.newDocument();
			Element e = obj.toXML(doc);
			doc.appendChild(e);
			writeXML(destination, doc.getDocumentElement());
		}
	}
	
	public static void writeXML(Writer destination, XMLSerializable obj) throws IOException {
		if (db == null) initialize();
		synchronized (db) { 
			Document doc = db.newDocument();
			Element e = obj.toXML(doc);
			doc.appendChild(e);
			writeXML(destination, doc.getDocumentElement());
		}
	}
	
	public static void writeXML(OutputStream destination, XMLSerializable obj,
			boolean hideXMLDeclaration) throws IOException {
		if (db == null) initialize();
		synchronized (db) { 
			Document doc = db.newDocument();
			Element e = obj.toXML(doc);
			doc.appendChild(e);
			writeXML(destination, doc.getDocumentElement(), hideXMLDeclaration);
		}		
	}
	

	public static void writeXML(PrintStream destination, XMLSerializable obj,
			boolean hideXMLDeclaration) throws IOException {
		if (db == null) initialize();
		synchronized (db) { 
			Document doc = db.newDocument();
			Element e = obj.toXML(doc);
			doc.appendChild(e);
			writeXML(destination, doc.getDocumentElement(), hideXMLDeclaration);
		}		
	}
	
	/**
	 * Warning: don't parse large documents with 
	 * this... (it creates an in-memory representation 
	 * of the document!)
	 * 
	 * @param doc
	 * @return a string representation of the document.
	 * @throws IOException
	 */
	public static String processXML(Document doc, boolean hideXMLDeclaration) throws IOException {
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		writeXML(fos, doc.getDocumentElement(), hideXMLDeclaration);
		return new String(fos.toByteArray(), "UTF-8");
	}

	public static String processXML(Element elem, boolean hideXMLDeclaration) throws IOException {
		if (elem == null)
			throw new NullPointerException();
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		writeXML(fos, elem, hideXMLDeclaration);
		return new String(fos.toByteArray(), "UTF-8");
	}
	
	public static String processXMLDebug(Element elem) throws IOException {
		return processXML(elem, true);
	}
	
	public static String processXMLDebug(Document elem) throws IOException {
		return processXML(elem, true);
	}

	/**
	 * copies part of a byte array into a new byte array.
	 * 
	 * @param source
	 * @param offset
	 * @param size
	 * @return
	 */
	public static byte[] subarray(byte[] source, int offset, int size) {
		if (offset + size > source.length)
			throw new IllegalArgumentException();
		if (offset < 0 || size < 0)
			throw new IllegalArgumentException("offset=" + offset + ",size=" + size);
		
		byte[] result = new byte[size];
		for (int i = 0; i < size; i++) {
			result[i] = source[offset + i];
		}
		return result;
	}
	
	/**
	 * escapes a string with double quotes
	 * @param name
	 * @return
	 */
	public static String escapeDoubleQuote(String name) {
		if (name == null)
			return null;
		return "\"" + name.replace("\\","\\\\").replace("\"", "\\\"") + "\"";
	}
	
	/**
	 * Reverse of double quoted escaping.
	 * (FIXME: poor implementation)
	 * 
	 * It unquotes \-escaped values, as well
	 * as double double-quotes. 
	 * 
	 * @param name value to de-escape
	 * @return
	 */
	public static String deescapeDoubleQuote(String name) {
		if (name == null)
			throw new NullPointerException();
		if (!(name.length() >= 2 &&
				name.charAt(0) == '"' &&
				name.charAt(name.length()-1) == '"')) {
			throw new IllegalArgumentException("not a valid escaped string: " + name);
		}
		char[] buf = new char[name.length() - 2];
		int pos = 0;
		boolean escaped = false;
		for (int i = 1; i < name.length() - 1; i++) {
			char c = name.charAt(i);
			if (escaped) {
				switch (c) {
				case '"': buf[pos++] = '"'; break;
				case '\\': buf[pos++] = '\\'; break;

				case 't': buf[pos++] = '\t'; break;
				case 'n': buf[pos++] = '\n'; break;
				case 'r': buf[pos++] = '\r'; break;
				default:
					buf[pos++] = c; break;
				}
				escaped = false;
			} else {
				switch (c) {
				case '"':
					if ((i < name.length() - 2) && (name.charAt(i+1) == '"')) {
						i++; // skip next char...
					} 
					buf[pos++] = '"';
					break;
				case '\\':
					escaped=true;
					break;
				default:
					buf[pos++] = c;
				}
			}
		}
		return new String(buf, 0, pos);
	}

	public static String escapeJavaSingleQuote(String name) {
		return "'" + name.replace("\\","\\\\").replace("'", "\\'") + "'";
	}
	
	/**
	 * Optimized version of doing:
	 * return "'" + name.replace("\\","\\\\").replace("'", "\\'").replace("\n", "\\n") + "'";
	 * Does exactly the same, except 10 times faster. 
	 */
	public static String escapeSQLSingleQuote(String str) {
		if (str == null)
			return "NULL";
		char[] buffer = new char[str.length() * 2 + 2];
		int pos = 0;
		buffer[pos++] = '\'';
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
			case '\n':
				buffer[pos++] = '\\';
				buffer[pos++] = 'n';
				break;
			case '\r':
				buffer[pos++] = '\\';
				buffer[pos++] = 'r';
				break;
			case '\\': 
			case '\'':
				buffer[pos++] = '\\';
			default:
				buffer[pos++] = str.charAt(i);
			}
		}
		buffer[pos++] = '\'';
		return new String(buffer, 0, pos); 
	}
	
	public static String escapeSQLSingleQuote2(String name) {
		return "'" + name.replace("\\","\\\\").replace("'", "\\'").replace("\n", "\\n") + "'";
	}
	
	public static Element getSingleChild(Element elem, String nodeName) throws IOException {
		NodeList descrs = getChildElementsByTagName(elem, nodeName);
		if (descrs.getLength() != 1) {
			throw new IOException("there should be 1 '" +  nodeName + "'-element, got: " + descrs.getLength());
		}
		return (Element) descrs.item(0);
	}

	public static Element getSingleChildNS(Element sourceElem, String nsUri,
			String localName) throws IOException {
		NodeList descrs = getChildElementsByTagNameNS(sourceElem, nsUri, localName);
		if (descrs.getLength() != 1) {
			throw new IOException("there should be 1 '" +  nsUri + ":" + localName + "'-element, got: " + descrs.getLength());
		}
		return (Element) descrs.item(0);
	}
	
	public static Element getSingleChild(Element elem) throws IOException {
		NodeList descrs = elem.getChildNodes();
		Element result = null;
		for (int i = 0; i < descrs.getLength(); i++) {
			Node n = descrs.item(i);
			if (n instanceof Element) {
				if (result != null)
					throw new IOException("there should be 1 element, got more than one");
				result = (Element) n;
			}
		}
		return result;
	}
	
	/**
	 * @param elem
	 * @param nodeName
	 * @return null if no child is present, the single child if there is 1 node with such a name, otherwise an {@link IOException}.
	 * @throws IOException 
	 */
	public static Element getOptionalSingleChild(Element elem, String nodeName) throws IOException {
		NodeList descrs = getChildElementsByTagName(elem, nodeName);
		switch (descrs.getLength()) {
		case 0:
			return null;
		case 1:
			return (Element) descrs.item(0);
		default:
			throw new IOException("there should be at most 1 " +  nodeName + " element, got: " + descrs.getLength());
		}
	}
	
	public static Element getOptionalSingleChildNS(Element elem, String nsUri, String nodeName) throws IOException {
		NodeList descrs = getChildElementsByTagNameNS(elem, nsUri, nodeName);
		switch (descrs.getLength()) {
		case 0:
			return null;
		case 1:
			return (Element) descrs.item(0);
		default:
			throw new IOException("there should be at most 1 " +  nodeName + " element, got: " + descrs.getLength());
		}
	}

	public static Document parseXML(File source) throws  IOException {
		FileInputStream is = new FileInputStream(source);
		try {
			return Utils.parseXML(is);
		} finally {
			is.close();
		}
	}

	public static Document parseXML(InputStream is) throws  IOException {
		if (is == null)
			throw new NullPointerException();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(is);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		}
	}
	
	public static Document parseXML(byte[] data) throws IOException {
		if (data.length == 0)
			throw new IOException("Not valid XML content.");
		InputStream in = new ByteArrayInputStream(data);
		try {
			return parseXML(in);
		} catch (IOException e) {
			throw new IOException("failed to parse: (error-message: " + e.getMessage() + ")");
		} finally {
			in.close();
		}
	}

	public static Document parseXML(String data) throws IOException {
		if (!data.trim().startsWith("<"))
			throw new IOException("Content is not allowed in prolog");
		return parseXML(data.getBytes(Charset.forName("UTF-8")));
	}
	
	public static Document newDocument() {
		return newDocument(false);
	}
	
	public static org.w3c.dom.Document newDocument(boolean namespaceAware) {
		if (namespaceAware) {
			if (db == null) initialize();
			synchronized (db) {
				return db.newDocument();
			}
		} else {
			if (dbns == null) initialize();
			synchronized (dbns) {
				return dbns.newDocument();
			}
		}
	}

	/**
	 * joins strings just like python's join:
	 * 
	 * @param keywords
	 * @param separator
	 * @throws NullPointerException if keywords or separator is null;
	 * @return
	 */
	public static String joinStrings(String[] keywords, String separator) {
		if (separator == null || keywords == null)
			throw new NullPointerException();
		if (keywords.length == 0) return "";
		
		StringBuilder sb = new StringBuilder();
		sb.append(keywords[0]);
		for (int i = 1; i < keywords.length; i++) {
			sb.append(separator);
			sb.append(keywords[i]);
		}
		return sb.toString();
	}
	
	public static String joinStrings(Collection<String> keywords, String separator) {
		if (keywords.size() == 0) return "";
		
		Iterator<String> iter = keywords.iterator();
		StringBuilder sb = new StringBuilder();
		while (true) {
			String item = iter.next();
			if (item == null)
				continue;
			sb.append(item);
			if (!iter.hasNext())
				break;
			sb.append(separator);
		}
		return sb.toString();
	}

	public static String getTextContentOfChild(Element resultNode, String string) throws IOException {
		return getTextContentOfChild(resultNode, string, false);
	}
	
	/**
	 *  
	 * 
	 * @param resultNode
	 * @param string
	 * @param trimWhitespace Iff true, additional whitespace will be trimmed from the string.
	 * @return
	 * @throws IOException
	 */
	public static String getTextContentOfChild(Element resultNode, String string, boolean trimWhitespace) throws IOException {
		Element n = getSingleChild(resultNode, string);
		if (n == null) {
			throw new IOException("no such element: " + string);
		}
		return trimWhitespace ? n.getTextContent().trim() : n.getTextContent(); 
	}
	
	public static String getTextContentOfChildNS(Element sourceElem,
			String nsUri, String localName) throws IOException {
		Element n = getSingleChildNS(sourceElem, nsUri, localName);
		if (n == null) {
			throw new IOException("no such element: " + nsUri + ":" + localName);
		}
		return n.getTextContent();
	}

	/** check whether "static class" causes a memory leak. */ 
	static class SimpleNodeList implements NodeList {
		private final List<Element> _nodes;
		public SimpleNodeList(List<Element> nodes) {
			_nodes = nodes;
		}
		public int getLength() {
			return _nodes.size();
		}

		public Node item(int index) {
			return _nodes.get(index);
		}
	}
	
	public static NodeList getChildElements(Element parent) {
		NodeList children = parent.getChildNodes();
		List<Element> result = new ArrayList<Element>();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element)) {
				continue; // skip non-element-nodes
			}
			Element childElement = (Element) children.item(i);
			result.add(childElement);
		}
		return new SimpleNodeList(result);
	}
	
	/**
	 * 
	 * @param parent
	 * @param tagName
	 * @return a list of {@link Element}s with the given tagName. Items in {@link NodeList} 
	 * 		   are guaranteed to be {@link Element} objects. So it is possible to
	 * 	       directlty cast "Element x = (Element) nl.item(i);" without check.
	 */
	public static NodeList getChildElementsByTagName(Element parent, String tagName) {
		NodeList children = parent.getChildNodes();
		List<Element> result = new ArrayList<Element>();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element)) {
				continue; // skip non-element-nodes
			}
			Element childElement = (Element) children.item(i);
			if (childElement.getTagName().equals(tagName)) {
				result.add(childElement);
			}
		}
		return new SimpleNodeList(result);
	}

	public static NodeList getChildElementsByTagNameNS(Element parent,
			String nsUri, String localName) {
		if (nsUri == null) 
			return getChildElementsByTagName(parent, localName);
		
		NodeList children = parent.getChildNodes();
		List<Element> result = new ArrayList<Element>();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element)) {
				continue; // skip non-element-nodes
			}
			Element childElement = (Element) children.item(i);
			if (nsUri.equals(childElement.getNamespaceURI()) && childElement.getLocalName().equals(localName)) {
				result.add(childElement);
			}
		}
		return new SimpleNodeList(result);
	}

	public static NodeList getDescendentElements(Element parent,
			String tagName) {
		List<Element> result = new ArrayList<Element>();
		getDescendentElements(parent, tagName, result);
		return new SimpleNodeList(result);
	}
	
	private static void getDescendentElements(Element parent, String tagName,
			List<Element> result) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element)) {
				continue; // skip non-element-nodes
			}
			Element childElement = (Element) children.item(i);
			if (childElement.getNodeName().equals(tagName)) {
				result.add(childElement);
			}
			getDescendentElements(childElement, tagName, result);
		}
	}

	/**
	 * Same as getTextContextOfChild with the only difference being
	 * that it will not throw an exception when there is no such element.
	 * However, it will throw an IOException if more than 1 element 
	 * with the given name is encountered. 
	 * 
	 * @param resultNode
	 * @param string
	 * @return
	 * @throws IOException 
	 */
	public static String getOptionalTextContentOfChild(Element resultNode, String tagName) throws IOException {
		if (resultNode == null)
			throw new NullPointerException();
		
		NodeList nodes = getChildElementsByTagName(resultNode, tagName);
		if (nodes.getLength() > 1) {
			throw new IOException("Expected at most one '" + tagName + "' element, got " + nodes.getLength());
		}
		
		if (nodes.getLength() == 0) {
			return null;
		}
		
		return (nodes.item(0)).getTextContent();
	}
	
	public static String getOptionalTextContentOfChildNS(Element resultNode,
			String nsUri, String tagName) throws IOException {
		if (resultNode == null)
			throw new NullPointerException();
		
		NodeList nodes = getChildElementsByTagNameNS(resultNode, nsUri, tagName);
		if (nodes.getLength() > 1) {
			throw new IOException("Expected at most one '" + tagName + "' element, got " + nodes.getLength());
		}
		
		if (nodes.getLength() == 0) {
			return null;
		}
		
		return (nodes.item(0)).getTextContent();
	}

	/**
	 * Finds all files that match the fileFilter either in the
	 * root directory or any sub-directory below it.
	 * 
	 * @param root
	 * @param fileFilter
	 * @return
	 */
	public static Collection<File> recursiveList(File root,
			FileFilter fileFilter) {
		List<File> result = new ArrayList<File>();
		result.addAll(Arrays.asList(root.listFiles(fileFilter)));

		// add all files in subdirs
		for (File dir : root.listFiles(new FileFilter(){
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
			result.addAll(recursiveList(dir, fileFilter));
		}
		return result;
	}
	
	static final FileFilter DIR_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() && !pathname.getName().startsWith(".");
		}
	};

	public static boolean contentEqual(File tempFile, File tempFile2) throws IOException {
		if (!tempFile.canRead() || !tempFile2.canRead()) {
			throw new IOException("cannot read the files");
		}
		
		if (tempFile.length() != tempFile2.length()) {
			return false;
		}
		InputStream in1 = new FileInputStream(tempFile);
		try {
			InputStream in2 = new FileInputStream(tempFile2);
			try {
				return contentEqual(in1, in2);
			} finally {
				in2.close();
			}
		} finally {
			in1.close();

		}
	}

	private static boolean contentEqual(InputStream in1, InputStream in2) throws IOException {
		int BUF_SIZE = 4096;
		while (true) {
			byte[] buf1 = new byte[BUF_SIZE];
			byte[] buf2 = new byte[BUF_SIZE];

			int result1 = in1.read(buf1);
			int result2 = in2.read(buf2);
			if (result1 < 0 && result2 < 0)
				return true;
			if (result1 < 0 || result2 < 0) 
				return false;
			if (result1 != result2)  
				throw new IllegalStateException("reading chunks of different sizes," +
				" the current implementation cannot deal with that");

			for (int i = 0; i < result1; i++) {
				if (buf1[i] != buf2[i]) {
					return false;
				}
			}
		}
	}
	
	public static <A,B> void printMap(
			Map<A, B> cells) {
		if (cells == null) {
			System.out.println("Map: " + null);
			return;
		} 
		System.out.println("Map: " + cells.toString());
		for (Entry<A, B> entry : cells.entrySet()) {
			System.out.println("Entry: " + entry.getKey() + " = " + entry.getValue());
		}
	}

	private static final Set<String> trueValues = new HashSet<String>(Arrays.asList(new String[] { "true", "y", "yes", "1" }));
	private static final Set<String> falseValues = new HashSet<String>(Arrays.asList(new String[] { "false", "n", "no", "0" }));
	
	public static boolean getBooleanAttribute(Element elem,
			String attributeName, boolean defaultValue) {
		return parseBoolean(elem.getAttribute(attributeName), defaultValue);
	}
	
	public static String getStringAttribute(Element root, String attributeName,
			String defaultValue) {
		if (root.hasAttribute(attributeName))
			return root.getAttribute(attributeName);
		return defaultValue;
	}
	
	public static String getStringAttributeNS(Element root,
			String nsUri, String localName, String defaultValue) {
		if (root.hasAttributeNS(nsUri, localName))
			return root.getAttributeNS(nsUri, localName);
		return defaultValue;
	}
	
	public static int getIntegerAttribute(Element elem, String attributeName,
			int defaultValue) {
		try {
			int result = Integer.parseInt(elem.getAttribute(attributeName));
			return result;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	public static int getIntegerAttributeNS(Element elem, String namespaceURI, 
			String localName, int defaultValue) {
		try {
			int result = Integer.parseInt(elem.getAttributeNS(namespaceURI, localName));
			return result;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	
	public static long getLongAttribute(Element elem, String attributeName,
			long defaultValue) {
		try {
			long result = Long.parseLong(elem.getAttribute(attributeName));
			return result;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	/**
	 * Tries to find the number in the child-element 'elemName', but
	 * will return the defaultValue if either the element does not exist,
	 * or the content of the element cannot be parsed into a number.  
	 * 
	 * @param elem
	 * @param elemName
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static long getLongOfChild(Element elem, String elemName, long defaultValue) throws IOException {
		try {
			Element childElem = Utils.getOptionalSingleChild(elem, elemName);
			if (childElem == null)
				return defaultValue;
			long result = Long.parseLong(childElem.getTextContent().trim());
			return result;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	public static Double getDoubleAttribute(Element elem, String attributeName,
			Double defaultValue) {
		try {
			Double result = Double.parseDouble(elem.getAttribute(attributeName));
			return result;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static Boolean parseBoolean(String stringRepresentation, Boolean defaultValue) {
		if (stringRepresentation == null)
			return defaultValue;
		String value = stringRepresentation.toLowerCase().trim();
		if (trueValues.contains(value)) return Boolean.TRUE;
		if (falseValues.contains(value)) return Boolean.FALSE;
		return defaultValue;
	}

	public static boolean parseBoolean(String stringRepresentation) {
		return parseBoolean(stringRepresentation, null);
	}

	public static boolean getBooleanProperty(Properties props, String key,
			boolean defaultvalue) {
		if (!props.containsKey(key)) 
			return defaultvalue;
		return parseBoolean(props.getProperty(key), defaultvalue);
	}
	
	public static Integer parseInteger(String str, Integer defaultResult) {
		if (str == null)
			return defaultResult;
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return defaultResult;
		}
	}
	
	public static Long parseLong(String str, Long defaultResult) {
		if (str == null)
			return defaultResult;
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {
			return defaultResult;
		}
	}

	public static Double parseDouble(String str, Double defaultResult) {
		if (str == null)
			return defaultResult;
		try {
			synchronized (NUMBER_SERIALIZER) {
				return NUMBER_SERIALIZER.parse(str).doubleValue();
			}
		} catch (ParseException e) {
			return defaultResult;
		}
	}

	public static void writeUInt16(byte[] buf, int pos, int data) {
		buf[pos] = (byte) (data & 0xff);
		buf[pos+1] = (byte) ((data >>> 8) & 0xff);
	}

	public static int readUInt16(byte[] pageData, int offset) {
		int result = 0;
		result +=   pageData[offset]   & 0xff;
		result += ((pageData[offset+1] & 0xff) << 8);
		return result;
	}
	
	public static long readUInt32(byte[] pageData, int offset) {
		long result = 0;
		result +=   pageData[offset]   & 0xff;
		result += ((pageData[offset+1] & 0xff) << 8);
		result += ((pageData[offset+2] & 0xff) << 16);
		result += ((pageData[offset+3] & 0xff) << 24);
		return result;
	}

	public static String byteArrayToString(byte[] bytes) {
		final char[] VALUES = { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length;i++) {
			sb.append(VALUES[(bytes[i] >> 4) & 15]);
			sb.append(VALUES[bytes[i] & 15]);
		}
		return sb.toString();
	}

	public static byte[] stringToByteArray(String str) {
		byte[] data = new byte[str.length() / 2];
		for (int i = 0; i < str.length(); i+=2) {
			int d = Integer.parseInt(str.substring(i, i+2), 16);
			data[i/2] = (byte) (d & 0xff); 
		}
		return data;
	}
	
	/**
	 * Determines whether the given file contains a valid XML document
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public static boolean validXML(File source) throws IOException {
		InputStream in = new FileInputStream(source);
		try {
			return validXML(in);
		} finally {
			in.close();
		}
	}
	
	/**
	 * Determines whether the given data represents a valid XML document
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static boolean validXML(InputStream in) throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.parse(in);
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		} catch (SAXException e) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Creates a string that represents the relative path between root
	 * and source.
	 * 
	 * @param root
	 * @param source
	 * @return
	 */
	public static String relativePath(File root, File source) {
		File parent = source;
		Stack<String> result = new Stack<String>();
		while (parent != null && !parent.equals(root)) {
			result.push(parent.getName());
			parent = parent.getParentFile();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(".");
		while (!result.isEmpty()) {
			sb.append(result.pop()).append('/');
		}
		return sb.toString();
	}

	public static String substringAfter(String haystack, String needle) {
		return substringAfter(haystack, needle, null);
	}
	
	public static String substringAfter(String haystack, String needle, String elseValue) {
		int index = haystack.indexOf(needle);
		if (index == -1)
			return elseValue;
		return haystack.substring(index + needle.length());
	}

	public static String substringAfterLast(String haystack, String needle) {
		return substringAfterLast(haystack, needle, null);
	}
	
	public static String substringAfterLast(String haystack, String needle,
			String elseValue) {
		int index = haystack.lastIndexOf(needle);
		if (index == -1)
			return elseValue;
		return haystack.substring(index + needle.length());
	}

	public static String substringBefore(String haystack, String needle) {
		int index = haystack.indexOf(needle);
		if (index == -1)
			return null;
		return haystack.substring(0, index);
	}

	public static String substringBeforeLast(String haystack, String needle) {
		int index = haystack.lastIndexOf(needle);
		if (index == -1)
			return null;
		return haystack.substring(0, index);
	}

	public interface XMLSerializable {
		Element toXML(Document doc);
	}
	
	public static String toXMLString(XMLSerializable obj, boolean hideXMLDeclaration) throws IOException {
		if (obj == null) return null;
		if (db == null) initialize();
		synchronized (db) { 
			Document doc = db.newDocument();
			Element e = obj.toXML(doc);
			doc.appendChild(e);
			return processXML(doc, hideXMLDeclaration);
		}
	}

	/**
	 * Be careful... not very safe to use this function: it might 
	 * cause dangling threads, as it doesn't wait for them to finish. 
	 * 
	 * Typical invocation:
	 * 
	 * 	Utils.runProcess(new String[] { "ps", "aux" }, System.out, System.err);
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int runProcess(String[] command, PrintStream stdout, int stdoutCap, PrintStream stderr, int stderrCap) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(command);
		Process p = pb.start();
		Thread reader1 = new Thread(new ProcessReader(p.getInputStream(), stdout, stdoutCap)); 
		Thread reader2 = new Thread(new ProcessReader(p.getErrorStream(), stderr, stderrCap));
		
		/* start the readers. It is not needed to do this before starting the process, the 
		 * output will block when full. */
		reader1.start(); reader2.start();
		
		/* wait till the process finishes */
		int result = p.waitFor();
		
		/* Collect the output, wait for the readers to finish, they might not yet have written 
		 * everything to the PrintStreams */
		reader1.join(); reader2.join();
		
		return result;
	}
	
	private static class ProcessReader implements Runnable {
		private final BufferedReader _is;
		private PrintStream _out;
		private final int _cap;
		private int _size = 0;
		
		/**
		 * 
		 * @param is
		 * @param out
		 * @param cap maximum number of bytes to record. After that, it will
		 * continue reading the stream, but throw all data away (to prevent memory to fill up)
		 */
		ProcessReader(InputStream is, PrintStream out, int cap) {			
			_is = new BufferedReader(new InputStreamReader(is));
			_out  =out;
			_cap = cap;
		}
		
		@Override
		public void run() {
			try {
				while (true) {
					String line = _is.readLine();
					if (line == null)
						break;
					if (_size < _cap) {
						if (_size + line.length() > _cap)
							line = line.substring(0, _cap-_size); 
						_out.println(line);
						_size += line.length();
					} else {
						// don't record the stream anymore (has reached the cap)
					}
					
				}
			} catch (IOException e) {
				_out.print("ERROR " + e.getClass() + " : " + e.getMessage());
				e.printStackTrace();
			} finally {
				close();
			}
		}
		
		public void close() {
			_out.flush();
		}
	}
	
	/**
	 * Checks whether the lists are equal.
	 * Make sure that the components in the list have properly implemented 
	 * the equals() method.
	 * 
	 * @param <C>
	 * @param a
	 * @param b
	 * @return true iff the lists have the same contents in the same order, 
	 *         false otherwise. 
	 */
	public static <C> boolean listEquals(List<C> a,
			List<C> b) {
		if (a.size() != b.size()) return false;
		for (int i = 0; i < a.size(); i++) {
			C a1 = a.get(i);
			C b1 = b.get(i);
			if (a1 == b1)
				continue;
			if (a1 != null && a1.equals(b1))
				continue;
			return false;
		}
		return true;
	}
	
	public static int findString(String[] haystack, String needle) {
		for (int i = 0; i < haystack.length; i++) {
			if (haystack[i].equals(needle))
				return i;
		}
		return -1;
	}

	public static String repeatString(String content, int count) {
		StringBuilder sb = new StringBuilder(content.length()*count);
		for (int i = 0; i < count; i++) {
			sb.append(content);
		}
		return sb.toString();
	}
	
	/**
	 * Removes the arguments that are accepted
	 * @param arguments a list of arguments excluding the '--' prefix
	 * @param flagStrings
	 * @return
	 */
	public static Set<String> parseFlags(List<String> arguments, String[] flagStrings) {
		Set<String> flags = new HashSet<String>();
		for (String flag : flagStrings) flags.add(flag);
		
		boolean[] toDelete = new boolean[arguments.size()];
		for (int i = 0; i < toDelete.length;i++) toDelete[i] = false;
		
		Set<String> result = new HashSet<String>();
		for (int i = 0; i < arguments.size(); i++) {
			String arg = arguments.get(i);
			if (arg.startsWith("--") 
					&& (flags.contains(arg.substring(2)) || 
						(arg.endsWith("=true") && flags.contains(arg.substring(2, arg.indexOf("=true"))))
						)) {
				result.add(arg.substring(2).replace("=true", ""));
				toDelete[i] = true;
			}
		}
		
		// remove params that have been parsed
		for (int i = toDelete.length-1; i >=0; i--) {
			if (toDelete[i]) arguments.remove(i);
		}
		return result;
	}

	/**
	 * Removes the arguments that are accepted
	 * 
	 * @param arguments
	 * @param paramStrings
	 * @return
	 */
	public static Map<String, String> parseParams(List<String> arguments,
			String[] paramStrings) {
		Set<String> params = new HashSet<String>();
		for (String param : paramStrings) params.add(param);
		
		Map<String, String> result = new HashMap<String, String>();
		boolean[] toDelete = new boolean[arguments.size()];
		for (int i = 0; i < toDelete.length;i++) toDelete[i] = false;
		for (int i = 0; i < arguments.size(); i++) {
			if (arguments.get(i).startsWith("--")) {
				if (i < arguments.size() - 1 && params.contains(arguments.get(i).substring(2))) {
					// either with a space between param and value
					result.put(arguments.get(i).substring(2), arguments.get(i + 1));
					toDelete[i] = true;
					toDelete[i+1] = true;
					i++;
				} else if (arguments.get(i).contains("=")) {
					// or with an '=' between param and value
					int isPos = arguments.get(i).indexOf('=');
					if (!params.contains(arguments.get(i).substring(2, isPos))) {
						continue;
					}
					String key = arguments.get(i).substring(2,isPos);
					String value = arguments.get(i).substring(isPos+1);
					result.put(key, value);
					toDelete[i] = true;
				}
			}
		}
		
		// remove params that have been parsed
		for (int i = toDelete.length-1; i >=0; i--) {
			if (toDelete[i]) arguments.remove(i);
		}
		
		return result;
	}

	public static void printList(List<? extends Object> params, PrintStream out) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for (Object o : params) {
			if (first) { first=false; } else { sb.append(","); }
			sb.append(o.toString());
		}
		sb.append("]");
		out.println(sb.toString());
	}
	
	public static List<String> readList(Element elem, String tagName) {
		List<String> result = new ArrayList<String>();
		NodeList nl = Utils.getChildElementsByTagName(elem, tagName);
		for (int i = 0; i < nl.getLength(); i++) {
			Element itemElem = (Element) nl.item(i);
			String item = itemElem.getTextContent();
			result.add(item);
		}
		return result;
	}

	/**
	 * In contrast to Arrays.asList, this method creates a <i>modifiable</i>
	 * list.
	 * 
	 * @param <O>
	 * @param array
	 * @return
	 */
	public static <O> List<O> toArrayList(O[] array) {
		List<O> list = new ArrayList<O>(array.length);
		for (O obj : array) {
			list.add(obj);
		}
		return list;
	}

	public static String formatDouble(double value) {
		synchronized (NUMBER_SERIALIZER) {
			return NUMBER_SERIALIZER.format(value);
		}
	}
	
	public static String formatDouble(double value, int digits) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ROOT);
		nf.setMaximumFractionDigits(digits);
		nf.setMinimumFractionDigits(digits);
		nf.setGroupingUsed(false);
		return nf.format(value);
	}
	
	public static void appendFile(File destination, File source) throws IOException {
		if (!source.canRead()) {
			throw new IOException("cannot read source " + source);
		}
		if (!destination.exists()) {
			throw new IOException("destination " + destination + "  does not exist");
		}
		InputStream in = new FileInputStream(source);
		try {
			OutputStream out = new FileOutputStream(destination, true);
			try {
				while (true) {
					byte[] buf = new byte[4096];
					int result = in.read(buf);
					if (result == -1) 
						break;
					out.write(buf, 0, result);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}
	



	/**
	 * Text in XML is often indented with tabs, and preceded by newlines.
	 * This function makes a best effort at creating a readable text without
	 * superfluous indentation.
	 * 
	 * @param value
	 * @return
	 */
	public static String nicifyXMLText(String value) {
		String description = value.trim();
		while (description.contains("\n\t") || description.contains("\n ")) {
			description = description.replaceAll("\\n ", "\\\n");
			description = description.replaceAll("\\n\\t", "\\\n");
		}
		return description;
	}

	public static File createFolder(File parent, String name) throws IOException {
		File newFolder = new File(parent, name);
		if (!newFolder.mkdir()) {
			throw new IOException("could not create folder: " + newFolder.getAbsolutePath());
		}
		return newFolder;
	}

	/**
	 * Tries to pretty-print a duration.
	 * 
	 * @param duration
	 * @return
	 */
	public static String formatTime(long duration) {
		// if total time less than 1.2 seconds, print time in milliseconds
		if (duration < 1200) return duration + "ms";
		// if total time less than 70 seconds, print time in seconds
		if (duration < 70000) return (duration / 1000) + "." + (duration/10) % 10 + "s";
		// if total time less than +- 65 minutes, print time in minutes + seconds
		if (duration < 4000000) return 
				(duration / 60000) + "m" + (duration/1000) % 60 + "s";
		// otherwise, print time in hours + minutes
		return
			(duration / 3600000) + "h" + (duration/60000) % 60 + "m";
	}

	public static byte[] buffer(InputStream inputStream, int maxBufferSize) throws IOException {
		int pos = 0;
		byte[] buf = new byte[64*1024];
		while (true) {
			int result = inputStream.read(buf, pos, buf.length - pos);
			if (result == -1)
				break;
			pos += result; 
			if (pos == buf.length) {
				// extend array...
				byte[] bufTmp = new byte[buf.length *  2];
				System.arraycopy(buf, 0, bufTmp, 0, pos);
				buf = bufTmp;
			}
		}
		return Arrays.copyOf(buf, pos);
	}

	private static char hexChar(int n) {
		return (n > 9) 
				? (char) ('a' + (n - 10))
				: (char) ('0' + n);
	}
	
	/**
	 * Small helper method to convert a byte string to a hexadecimal
	 * string representation.
	 *
	 * @param digest the byte array to convert
	 * @return the byte array as hexadecimal string
	 */
	public static String toHexString(byte[] digest) {
		char[] result = new char[digest.length * 2];
		int pos = 0;
		for (int i = 0; i < digest.length; i++) {
			result[pos++] = hexChar((digest[i] & 0xf0) >> 4);
			result[pos++] = hexChar(digest[i] & 0x0f);
		}
		return new String(result);
	}

	public static String formatPercentage(long count, long all) {
		return formatPercentage(1d * count / all);
	}
	
	public static String formatPercentage(double fraction) {
		return String.format("%02.01f", fraction * 100.0) + "%";
	}
	
	public static String formatRate(double count, double all, String unit) {
		double rate = count / all;
		if (rate < 1) {
			return String.format("%01.03f", rate) + " " + unit;
		} else if (rate < 1200) {
			return String.format("%04.01f", rate) + " " + unit;
		} else if (rate < 1200000) {
			return String.format("%02.02fk", rate / 1000) + " " + unit;
		} else {
			return String.format("%02.02fm", rate / 1000000) + " " + unit;
		}
	}


	public static String formatNumber(long number) {
		if (number < 1200L) return "" + number;
		if (number < 12000L) return "" + (number / 1000L) + "." + (number % 10L) + "k";
		if (number < 1200000L) return "" + (number / 1000L) + "k";
		if (number < 12000000L) return "" + (number / 1000000L) + "." + ((number / 100000L)  % 10) + "m";
		if (number < 1200000000L) return "" + (number / 1000000L) + "m";
		if (number < 12000000000L) return "" + (number / 1000000000L) + "." + ((number / 100000000L)  % 10) + "g";
		if (number < 1200000000000L) return "" + (number / 1000000000L) + "g";
		if (number < 12000000000000L) return "" + (number / 1000000000000L) + "." + ((number / 100000000000L)  % 10) + "t";
		if (number < 1200000000000000L) return "" + (number / 1000000000000L) + "t";
		return "" + number;
	}
	

	public static String firstNonNull(String... items) {
		for (int i = 0; i < items.length; i++)
			if (items[i] != null)
				return items[i];
		return null;
	}

	/**
	 * Prints a stacktrace, but stops at the first function that is mentioned in the 
	 * list of terminators.
	 * 
	 * @param e
	 * @param terminators
	 */
	public static void printSimplifiedStackTrace(Throwable e, Set<String> terminators) {
		printSimplifiedStackTrace(e, terminators, 10);
	}

	/**
	 * Prints a stacktrace, but stops at the first function that is mentioned in the 
	 * list of terminators.
	 * 
	 * @param e
	 * @param terminators
	 */
	public static void printSimplifiedStackTrace(Throwable e, Set<String> terminators, int maxDepth) {
		int i = 0;
		while (e != null && i < maxDepth) {
			System.err.println(e.getClass().getName() + ":" + e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				String line = elem.toString();
				System.err.println(line);
				if (terminators.contains(substringBefore(line, "(")))
					break;
			}
			e = e.getCause();
			i++;
		}
	}

}
