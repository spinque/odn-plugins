package com.spinque.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLUtils {
	
	static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	static final TransformerFactory tf = TransformerFactory.newInstance();
	static DocumentBuilder db = null;
	
	private static synchronized void initialize() {
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * tries to integrate the document source
	 * into the document dest under the element destElem.
	 * 
	 * @param dest
	 * @param destElem
	 * @param source
	 */
	public static void integrate(Document dest, Element destElem, Document source) {
		Element importedNode = (Element) dest.importNode(source.getDocumentElement(), true);
		destElem.appendChild(importedNode);
//		// FIXME: is this similar to the method: Element.importNode()?
//		Element e = integrate(dest, source.getDocumentElement());
//		destElem.appendChild(e);
	}

	/**
	 * Creates an Element with the given nameSpace and nodeName, and adds all childNodes as
	 * children to that node.
	 *   
	 * @param doc
	 * @param nameSpace
	 * @param nodeName
	 * @param childNodes a list of nodes that should become childnodes of the newly created node.
	 * @return
	 */
	public static Element createElementNS(Document doc, String nameSpace, String nodeName,
			Element... childNodes) {
		Element result = doc.createElementNS(nameSpace, nodeName);
		for (Node childNode : childNodes)
			result.appendChild(childNode);
		return result;
	}

	/**
	 * Creates a node and adds text (as TextContent) to it.
	 * 
	 * @param doc
	 * @param nodeName
	 * @param textContent
	 * @return
	 */
	public static Element createElementWithText(Document doc, String nodeName, String textContent) {
		Element result = doc.createElement(nodeName);
		result.setTextContent(textContent);
		return result;
	}

	public static Element createElementWithTextNS(Document doc, String nsUri,
			String tagName, String data) {
		Element resultElem = doc.createElementNS(nsUri, tagName);
		resultElem.setTextContent(data);
		return resultElem;
	}

//	private static Element integrate(Document dest, 
//			Element source) {
//		String tagName = source.getTagName();
//		Element result = dest.createElement(tagName);
//		
//		NamedNodeMap nnm = source.getAttributes();
//		for (int i = 0; i < nnm.getLength(); i++) {
//			Node n = nnm.item(i);
//			result.setAttribute(n.getNodeName(), n.getNodeValue());
//		}
//		
//		NodeList nl = source.getChildNodes();
//		for (int i = 0; i < nl.getLength(); i++) {
//			Node n = nl.item(i);
//			if (n instanceof Element) {
//				Element e = (Element) n;
//				Element c = integrate(dest, e);
//				result.appendChild(c);
//			} else if (n instanceof Text) {
//				Text t = (Text) n;
//				Text destT = dest.createTextNode(t.getTextContent());
//				result.appendChild(destT);
//			} else if (n instanceof ProcessingInstruction) {
//				// do nothing
//			} else if (n instanceof Comment) {
//				// do nothing
//				Comment c = (Comment) n;
//				Comment destC = dest.createComment(c.getTextContent());
//				result.appendChild(destC);
//			} else if (n instanceof Entity) {
//				// do nothing
//			} else if (n instanceof EntityReference) {
//				// do nothing
//			} else if (n instanceof DocumentType) {
//				// do nothing
//			} else {
//				System.err.println("Don't know what to do with: " + n.getClass().getName());
//			}
//		}
//		return result;
//	}
	
	
	
}
