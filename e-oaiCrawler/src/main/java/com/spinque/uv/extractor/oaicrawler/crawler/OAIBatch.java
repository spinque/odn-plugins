package com.spinque.uv.extractor.oaicrawler.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.spinque.utils.Utils;
import com.spinque.uv.extractor.oaicrawler.oai.OAIError;
import com.spinque.uv.extractor.oaicrawler.oai.OAIUtils;

public class OAIBatch {
	
	protected static final OAIBatch POISON_BATCH = new OAIBatch((String) null, true);

	private final Document _batchXML;
	private final String _resumptionToken;
	
	private boolean _stripHeaderInfo = true;

	public OAIBatch(Document batchXML, boolean stripHeader) {
		_batchXML = batchXML;
		_resumptionToken = null;
		_stripHeaderInfo = stripHeader;
	}
	public OAIBatch(String resumptionToken, boolean stripHeader) {
		_batchXML = null;
		_resumptionToken = resumptionToken;
		_stripHeaderInfo = stripHeader;
	}

	public String getResumptionToken() {
		if (_resumptionToken != null)
			return _resumptionToken;
		try {
			Element recordListElem = com.spinque.utils.Utils.getOptionalSingleChildNS(_batchXML.getDocumentElement(), OAIUtils.OAI_NS, "ListRecords");
			if (recordListElem == null)
				recordListElem = Utils.getOptionalSingleChildNS(_batchXML.getDocumentElement(), OAIUtils.OAI_NS, "ListIdentifiers");
			if (recordListElem == null) return OAIPMHCrawler.POISON;
			Element resumptionTokenElem = Utils.getOptionalSingleChildNS(recordListElem, OAIUtils.OAI_NS, "resumptionToken");
			if (resumptionTokenElem == null)
				return OAIPMHCrawler.POISON;
			String token = resumptionTokenElem.getTextContent(); 
			if (token == null || token.isEmpty())
				return OAIPMHCrawler.POISON; 	
			return token;
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
	
	public OAIError getError() throws IOException {
		Element errorElem = Utils.getOptionalSingleChildNS(_batchXML.getDocumentElement(), OAIUtils.OAI_NS, OAIError.ELEM_NAME);
		if (errorElem != null)
			return OAIError.parseXML(errorElem);
		return null;
	}

	/**
	 * 
	 * @return the elements returned from a verb=ListRecords request.
	 * @throws IOException
	 */
	public List<Element> getRecords() throws IOException {
		Element recordListElem = Utils.getOptionalSingleChildNS(_batchXML.getDocumentElement(), OAIUtils.OAI_NS, "ListRecords");
		if (recordListElem == null) {
			return Collections.emptyList();
		}
		NodeList nl = Utils.getChildElementsByTagNameNS(recordListElem, OAIUtils.OAI_NS, "record");
		List<Element> result = new ArrayList<Element>();
		
		for (int i = 0; i < nl.getLength(); i++) {
			Element recordElem = (Element) nl.item(i);
			if (_stripHeaderInfo) {
				Element metadata = Utils.getOptionalSingleChildNS(recordElem, OAIUtils.OAI_NS, "metadata");
				if (metadata != null) {
					result.add(Utils.getSingleChild(metadata));
				}
			} else {
				result.add(recordElem);
			}
		}
		return result;
	}
	
	/**
	 * @return the elements returned from a verb=ListIdentifiers request.
	 * @throws IOException
	 */
	public List<Element> getIdentifiers() throws IOException {
		Element recordListElem = Utils.getOptionalSingleChildNS(_batchXML.getDocumentElement(), OAIUtils.OAI_NS, "ListIdentifiers");
		if (recordListElem == null) {
			return Collections.emptyList();
		}
		NodeList nl = Utils.getChildElementsByTagNameNS(recordListElem, OAIUtils.OAI_NS, "header");
		List<Element> result = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element recordElem = (Element) nl.item(i);
			result.add(recordElem);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "[OAIBatch " + _resumptionToken + " hasXML=" + (_batchXML != null) + "]";
	}
}