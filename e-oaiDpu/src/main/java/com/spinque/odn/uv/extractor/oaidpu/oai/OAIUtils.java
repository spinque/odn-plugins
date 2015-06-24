package com.spinque.odn.uv.extractor.oaidpu.oai;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class OAIUtils {

	public enum OAIStatus { DELETED,;
		public String getValue() {
			return name().toLowerCase();
		} 
	}
	
	public static final String OAI_NS = "http://www.openarchives.org/OAI/2.0/";
	
	private final static DateTimeFormatter timestampFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(DateTimeZone.UTC);
	public static String printTimestamp(DateTime timestamp) {
		synchronized (timestampFormatter) {
			return timestamp.toString(timestampFormatter);
		}
	}

	private final static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	public static String printDate(DateTime date) {
		synchronized (dateFormatter) {
			return date.toString(dateFormatter);
		}
	}
}
