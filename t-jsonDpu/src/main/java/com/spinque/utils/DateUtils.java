package com.spinque.utils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Element;

/**
 * Note: for parsing text snippets and converting them into date-objects, 
 */
public class DateUtils {

	public static DateTime getDateAttribute(Element root, String attributeName,
				DateTime defaultValue) throws IOException {
			if (root.hasAttribute(attributeName)) {
				String dateValue = root.getAttribute(attributeName);
				try {
					synchronized (DateUtils.XML_DATE_TIME_FORMAT) {
						DateTime dt = DateUtils.XML_DATE_TIME_FORMAT.parseDateTime(dateValue);
						return dt;
					}
				} catch (IllegalArgumentException e) {
					System.out.println("not a ISO date '" + dateValue + "'");
					try {
						Date d = new SimpleDateFormat().parse(dateValue);
						return new DateTime(d.getTime());
					} catch (ParseException e1) {
						System.out.println("Could not parse: " + dateValue);
						return defaultValue;
					}
				}
			}
			return defaultValue;
		}

	public static DateTime parseDateTime(String dateTimeStr) {
		synchronized (DateUtils.XML_DATE_TIME_FORMAT) {
			return DateUtils.XML_DATE_TIME_FORMAT.parseDateTime(dateTimeStr);
		}
	}


	public static DateTime parseISODate(String dateTimeStr) {
		synchronized (DateUtils.XML_DATE_FORMAT) {
			return DateUtils.XML_DATE_FORMAT.parseDateTime(dateTimeStr);
		}
	}

	public static String printDate(DateTime date) {
		synchronized (DateUtils.XML_DATE_TIME_FORMAT) {
			return DateUtils.XML_DATE_TIME_FORMAT.print(date);
		}
	}
	
	private static final DateTimeFormatter XML_DATE_TIME_FORMAT =
			ISODateTimeFormat.dateTimeNoMillis();
	private static final DateTimeFormatter XML_DATE_FORMAT =
			ISODateTimeFormat.dateOptionalTimeParser();

	public static String printBasicDate(DateTime date) {
		if (date == null)
			return null;
		return date.getDayOfMonth() + "-" + date.getMonthOfYear() + "-" + date.getYear();
	}
	
	public static String printYearMonthDay(DateTime date) {
		if (date == null)
			return null;
		return String.format("%04d-%02d-%02d", date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
	}

}
