package com.spinque.utils.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLUtils {
	
	static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	static final TransformerFactory tf = TransformerFactory.newInstance();
	static DocumentBuilder db = null;
	
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
	}

	/**
	 * Creates an Element with the given nameSpace and nodeName, and adds all childNodes as
	 * children to that node.
	 *   
	 * @param doc
	 * @param nameSpace
	 * @param nodeName
	 * @param childNodes a list of nodes that should become childnodes of the newly created node.
	 * @return newly created element
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
	 * @return newly created element with the text content added
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
}
