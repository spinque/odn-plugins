package com.spinque.uv.extractor.oaicrawler.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.w3c.dom.Element;

import com.spinque.utils.CompoundIterator;
import com.spinque.utils.DateUtils;
import com.spinque.utils.GetNextIterator;
import com.spinque.utils.HTTP;
import com.spinque.utils.Utils;

/**
 * 
 */
public class OAIPMHCrawler implements Iterable<Element> {
	
	private static final int REQUEST_TIMEOUT = 600000 /* 600 seconds timeout */;
	final public static String POISON = "POISON";
	protected static final long MINIMUM_INTERVAL = 2000;
	
	/* The amount of data that a single OAI-request can produce. 
	 * 1MB seems to be too little. 4MB in one request should be ample. */
	private static final int MAX_BATCH_SIZE = 4 * 1024 * 1024; /* in bytes */

	private final URL _harvesturl;
	private final String _metadataPrefix; 
	private final DateTime _fromDate;
	private final DateTime _untilDate;
	private final String _setSpec;
	
	long _lastDuration = 0; // number of milliseconds it took for the OAI-provider to return the last resultset
							// this number can be used to slow down crawling if the server seems too busy.
							// waiting at least 10 times this duration would be polite. (FIXME: implement) 
							// Of course, always waiting at least the MINIMUM INTERVAL should be respected too. 
	
	OAIBatch _lastBatch = null;

	public enum Verb { 
		ListIdentifiers, ListRecords;

		public String getClause() {
			switch (this) {
			case ListIdentifiers:
				return "verb=ListIdentifiers";
			case ListRecords:
				return "verb=ListRecords";
			default:
				throw new IllegalStateException();
			}
		} 
	};
	
	private final Verb _verb;
	private boolean _stripHeader;

	public OAIPMHCrawler(URL harvesturl, String metadataPrefix,
			String setSpec, DateTime fromDate, DateTime untilDate, Verb verb, boolean stripHeader) {
		if (fromDate != null)
			System.out.println("Crawling from " + DateUtils.printBasicDate(fromDate));
		if (untilDate != null)
			System.out.println("Crawling until " + DateUtils.printBasicDate(untilDate));
		_harvesturl = harvesturl;
		_metadataPrefix = metadataPrefix; // usually oai_dc
		_setSpec = setSpec; // may be null
		_fromDate = fromDate; // format: yyyy-MM-dd  (null means all)
		_untilDate = untilDate;
		_verb = verb;
		_stripHeader = stripHeader;
	}
	
	public void setResumptionToken(String resumptionToken) {
		_lastBatch = new OAIBatch(resumptionToken, _stripHeader);
	}

	private OAIBatch fetchOAIBatch(OAIBatch previousBatch) throws IOException {
		// if resumptionToken == null -> fetch initial batch.
		URL fetchURL = (previousBatch == null) ?
				makeInitialURL() : makeResumptionURL(previousBatch.getResumptionToken());
		
		long duration = - System.currentTimeMillis();
		byte[] data = HTTP.getURL(Proxy.NO_PROXY, fetchURL, 0, MAX_BATCH_SIZE, REQUEST_TIMEOUT);
		
		/* FIXME: WARNING: special hack for OpenSKOS: they don't provide XML (see also ALIGN-35) */
		try {
			data = new String(data, "UTF-8").replaceAll("<openskos:status>[^<]*</openskos:status>", "").getBytes("UTF-8");
		} catch (Throwable e){
			e.printStackTrace();
		}
		
		duration += System.currentTimeMillis();
		_lastDuration = duration;
		OAIBatch batch = new OAIBatch(Utils.parseXML(data), _stripHeader);
		if (batch.getError() != null)
			throw new IOException("OAI ERROR:" + fetchURL + " : " + batch.getError().toString());
		
		return batch;
	}
	
	/**
	 * Generate the initial request URL.
	 * 
	 * This URL consists of the harvest-root and metadata-type.
	 * It also specifies the optional from and until date.
	 * 
	 * All subsequent requests are made via a 'resumptionToken'.
	 * 
	 * @return
	 * @throws MalformedURLException
	 */
	private URL makeInitialURL() throws MalformedURLException {
		String attrs = (_fromDate == null ? "" : "&from=" + DateUtils.printYearMonthDay(_fromDate))
						+ (_untilDate == null ? "" : "&until=" + DateUtils.printYearMonthDay(_untilDate));
		if (_setSpec != null)
			attrs = attrs + "&set=" + _setSpec;
		return new URL(_harvesturl + "?" + _verb.getClause() + "&metadataPrefix=" + _metadataPrefix + attrs);
	}
	
	private URL makeResumptionURL(String resumptionToken) throws IOException {
		return new URL(_harvesturl + "?" + _verb.getClause() + "&resumptionToken=" + URLEncoder.encode(resumptionToken, "UTF-8"));
	}
	
	public Iterator<Element> iterator() {
		return new CompoundIterator<Element>(new Iterator<Iterator<Element>>() {
			Iterator<OAIBatch> _iter = iteratorOAIBatch();
			@Override
			public boolean hasNext() {
				return _iter.hasNext();
			}

			@Override
			public Iterator<Element> next() {
				try {
					if (!_iter.hasNext()) 
						return null;
					switch (_verb) {
					case ListRecords:
						return _iter.next().getRecords().iterator();
					case ListIdentifiers:
						return _iter.next().getIdentifiers().iterator();
					default:
						throw new IllegalStateException();
					}
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		});
	}
	
	public Iterator<OAIBatch> iteratorOAIBatch() {
		return new GetNextIterator<OAIBatch>(new GetNextIterator.NextIterator<OAIBatch>() {
			long _lastFetch = 0;
			
			@Override
			public OAIBatch getNext() {
				try {
					if (_lastBatch == null) {
						// do initial query...
						OAIBatch batch = fetchOAIBatch(_lastBatch);
						_lastBatch = batch;
						_lastFetch = System.currentTimeMillis();
						return _lastBatch;
					}  
					while (_lastBatch != OAIBatch.POISON_BATCH) {
						if (_lastBatch.getResumptionToken() == POISON) {
							_lastBatch = OAIBatch.POISON_BATCH;
							return null;
						}
						if (System.currentTimeMillis() - _lastFetch < MINIMUM_INTERVAL) {
							System.out.println("[Politeness measure: wait at least " + MINIMUM_INTERVAL + "ms between each request]");
							Thread.sleep(Math.max(0, _lastFetch + MINIMUM_INTERVAL - System.currentTimeMillis()));
						}
						OAIBatch batch = fetchOAIBatch(_lastBatch);
						_lastBatch = batch;
						_lastFetch = System.currentTimeMillis();
						return batch;
					}
				} catch (IOException e) {
					System.err.println("ERROR: failed to crawl " + _harvesturl + ", due to IOException " + e.getMessage());
					storeResumptionToken(_lastBatch);
					_lastBatch = OAIBatch.POISON_BATCH;
				} catch (InterruptedException e) {
					System.err.println("ERROR: failed to crawl " + _harvesturl + ", got interrupted.");
					storeResumptionToken(_lastBatch);
					_lastBatch = OAIBatch.POISON_BATCH;
				}
				return null;
			}

			@Override
			public void close() {
				/* do nothing */
			}
		});
	}

	protected void storeResumptionToken(OAIBatch batch) {
		if (batch == null)
			System.err.println("No resumptiontoken...");
		else
			System.err.println("Last resumptiontoken: '" + batch.getResumptionToken() + "'");
	}

}
