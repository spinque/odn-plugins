package com.spinque.uv.extractor.oaicrawler.oai;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.spinque.utils.XMLUtils;

public class OAIError {
	
	public static final String ELEM_NAME = "error";
	
	public static final OAIError NO_METADATA_FORMATS = new OAIError("noMetadataFormats", "There are no metadata formats available for the specified item.");
	public static final OAIError NO_SET_HIERARCHY = new OAIError("noSetHierarchy", "The repository does not support sets.");

	private final String _code;
	private final String _message;
	
	private OAIError(String code, String message) {
		_code = code;
		_message = message;
	}

	public Element toXML(Document doc) {
		Element result = XMLUtils.createElementNS(doc, OAIUtils.OAI_NS, ELEM_NAME);
		result.setAttribute("code", _code);
		result.setTextContent(_message);
		return result;
	}
	
	public static OAIError parseXML(Element elem) throws IOException {
		if (!elem.getTagName().equals(ELEM_NAME))
			throw new IOException("Not an OAI Error element.");
		String code = elem.getAttribute("code");
		String message = elem.getTextContent();
		return new OAIError(code, message);
	}
	
	@Override
	public String toString() {
		return "[OAIError " + _code + " : " + _message + "]";
	}
}
