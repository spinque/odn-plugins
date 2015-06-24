package com.spinque.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

public class HTTP {
	
	/** 
	 * Convenience method
	 */
	public static String sendFile(URL url, String param, String value, Charset responseEncoding) throws IOException {
		return sendFile(url, Collections.singletonMap(param, value), responseEncoding);
	}
	
	/**
	 * @param url
	 * @param paramMap
	 * @return
	 * @throws IOException  
	 */
	public static String sendFile(URL url, Map<String, String> paramMap, Charset responseEncoding) throws IOException {
		return sendFile(url, "GET", paramMap, responseEncoding);
	}
	
	public static String sendFile(URL url, String method, Map<String, String> paramMap, Charset responseEncoding) throws IOException {
		String data = encodePostParams(paramMap);
		
		if (!url.getProtocol().equals("http")
				&& !url.getProtocol().equals("https"))
			throw new IOException("unknown protocol: " + url.getProtocol() + " (only know http and https)");
		
		// Send data
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setReadTimeout(10000); // 10 seconds
        conn.setConnectTimeout(10000); 
		
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		try {
			wr.write(data);
			wr.flush();

			// Get the response
			if (conn.getResponseCode() != 200) {
				// On error...
				String error = readUsingLineReader(conn.getErrorStream(), responseEncoding);
				throw new HTTPConnectionException(conn.getResponseCode(), error);
			} else {
				// On success...
				return readUsingLineReader(conn.getInputStream(), responseEncoding);
			}
		} finally {
			wr.close();
		}
	}
	
	/**
	 * Note: closes the stream.
	 * 
	 * @param is
	 * @param responseEncoding
	 * @return
	 * @throws IOException
	 */
	private static String readUsingLineReader(InputStream is,
			Charset responseEncoding) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, responseEncoding));
		try {
			StringBuilder sb = new StringBuilder();
			while (true) {
				String line = rd.readLine();
				if (line == null) break; 
				// Process line...
				sb.append(line).append('\n');
			}
			return sb.toString();
		} finally {
			rd.close();
		}
	}

	/**
	 * 
	 * @param url
	 * @param maxSize maximum size of content that will be downloaded. If more content is available, it will be truncated. 
	 *                maxSize is measured in bytes (not in characters)
	 * @param charSet the method to encode the data into characters.
	 * @return
	 * @throws IOException
	 */
	public static String getURLAsString(Proxy p, URL url, int maxSize, Charset charSet) throws IOException { // { // , Map<String, String> paramMap) 
		return new String(getURL(p, url, maxSize), charSet);
	}
	

	public static URL getRedirectedURL(URL url) throws IOException { 
		// HttpURLConnection
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		URL redirectedUrl = null;
		String redirectedUrlString = null;
		
		conn.setConnectTimeout(20000); // 20 seconds
		conn.setInstanceFollowRedirects( false );
		try {
			conn.connect();
			redirectedUrlString = conn.getHeaderField("Location");
			if (redirectedUrlString != null)
				redirectedUrl = new URL(redirectedUrlString);
		} finally {
			conn.disconnect();
		}
		
		return redirectedUrl;
	}

	public static byte[] getURL(Proxy p, URL url, int maxSize) throws IOException { 
		return getURL(p, url, 0, maxSize, 20000 /* 20 seconds */);
	}
	
	/**
	 * 
	 * @param p
	 * @param url
	 * @param min_bytes_per_second
	 * @param max_content_length
	 * @param timeout in milliseconds
	 * @return
	 * @throws IOException
	 */
	public static byte[] getURL(Proxy p, URL url, int min_bytes_per_second, long max_content_length, int timeout) throws IOException { 
		
		if (min_bytes_per_second < 0)
			min_bytes_per_second = 0;
		
		// Send data
		// HttpURLConnection
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(p);
		conn.setConnectTimeout(timeout); 
		conn.setReadTimeout(timeout); // 20 seconds
		try {
			conn.connect();
			/* skip content if larger than max allowed */
			if (max_content_length < Long.MAX_VALUE) {
				try {
					long contentLength = Long.parseLong(conn.getHeaderField("Content-Length"));
					if (contentLength > max_content_length)
						throw new ContentTooLargeException(url.toString(), contentLength, max_content_length);
				} catch (NumberFormatException e) {
					/* content length not available, bail out */
				}
			}

			/* Get the response */
			InputStream is = conn.getInputStream();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					long start_timestamp = System.currentTimeMillis();
					long total_read = 0L;
					while (true) {
						byte[] buf = new byte[16*1024];
						int count = is.read(buf);
						if (count == -1)
							break;
						baos.write(buf, 0, count);
						long elapsed = Math.max(System.currentTimeMillis() - start_timestamp, 1);
						total_read += count;
						int bytes_per_second = 1000 * (int) (total_read / elapsed);
						// System.out.println("[INFO] Reading from " + url.toString() + " at " + bytes_per_second + " B/s (MIN: " + min_bytes_per_second + ")");
						if (bytes_per_second < min_bytes_per_second)
							throw new IOException("Reading from " + url.toString() + " at " + bytes_per_second + " B/s (MIN: " + min_bytes_per_second + ")");
						if (baos.size() > max_content_length)
							throw new IOException("Content for " + url.toString() + " is larger then max allowed (" + max_content_length + " bytes)");
					}
					return baos.toByteArray();
				} finally {
					baos.close();
				}

			} finally { 
				is.close(); 
			}
		} finally {
			conn.disconnect();
		}
	}


	
	/**
	 * Converts a map into a string that can be sent as parameters for
	 * a POST HTTP-request.
	 * It uses UTF-8 as encoding.
	 */
	public static String encodePostParams(Map<String, String> paramMap) throws UnsupportedEncodingException {
		StringBuilder data = new StringBuilder();
		
		/* construct POST parameters */
		boolean firstParam = true;
		for (Entry<String, String> entry : paramMap.entrySet()) {
			if (!firstParam) data.append("&");
			data.append(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + 
					URLEncoder.encode(entry.getValue(), "UTF-8"));
			firstParam = false;
		}
		return data.toString();
	}
}
