package com.spinque.utils.json;

import java.io.IOException;

import com.spinque.utils.SimpleReader;
import com.spinque.utils.json.JSONDOM.ArrayNode;
import com.spinque.utils.json.JSONDOM.Node;
import com.spinque.utils.json.JSONDOM.ObjectNode;

/**
 * Not fully correct.
 * - TODO: needs better support for parsing numbers
 * - TODO: strings are not de-escaped.
 */
public class JSONParser {
	
	public static JSONDOM parse(String dataStr) throws IOException {
		SimpleReader r = new SimpleReader(dataStr);
		JSONDOM result = new JSONDOM();
		return parse(result, r);
	}
	
	public static JSONDOM parse(JSONDOM result, SimpleReader r) throws IOException {
		
		r.skipWhitespace();
		if (r.eol()) throw new IOException("no data");
		
		Node n = parseAny(result, r);
		result.setRoot(n);
		
		return result;
	}

	private static Node parseAny(JSONDOM result, SimpleReader r) throws IOException {
		if (r.eol())
			throw new IOException("invalid JSON: incomplete object (EOF came too soon)");
		switch (r.current()) {
		case '{':
			return parseObject(result, r);
		case '[':
			return parseArray(result, r);
		case '"':
			return parseLiteralString(result, r);
		case '-': 
		case '0': case '1': case '2': case '3': case '4': 
		case '5': case '6': case '7': case '8': case '9':
			return parseLiteralNumber(result, r);
		case 't':
		case 'f':
			return parseLiteralBoolean(result, r);
		case 'n':
			return parseNull(result, r);
		default:
			throw new IOException("failed to parse " + r.toString());
		}
	}

	private static Node parseNull(JSONDOM result, SimpleReader r) throws IOException {
		String nullStr = r.readExact(new String[] { "null" });
		if (nullStr == null) {
			throw new IOException("could not parse a null value from: " + r.remainder(10));
		}
		return JSONDOM.NULL_NODE;
	}

	private static Node parseObject(JSONDOM dom, SimpleReader r) throws IOException {
		if (r == null)
			throw new IOException("no reader");
		
		if (r.current() != '{')
			throw new IOException();
		
		ObjectNode result = dom.createObjectNode();
		r.take();
		r.skipWhitespace();
		boolean first = true;
		while (r.current() != '}') {
			if (!first) {
				if (r.current() != ',') {
					throw new IOException("expected comma at " + r);
				}
				r.take(); 
			} else {
				first = false;
			}
			r.skipWhitespace();
			if (r.current() != '"') {
				throw new IOException("expected double quote at " + r);
			}
			r.take();
			String key = r.takeToken('"', false);
			r.skipWhitespace();
			if (r.current() != ':') 
				throw new IOException("expected colon at " + r);
			r.take();
			r.skipWhitespace();
			Node n = parseAny(dom, r);
			r.skipWhitespace();
			result.addAttribute(key, n);
		}
		r.take(); // eat '}'
		return result;
	}

	private static Node parseArray(JSONDOM dom, SimpleReader r) throws IOException {
		ArrayNode result = dom.createArrayNode();
		if (r.current() != '[')
			throw new IOException();
		
		r.take();
		r.skipWhitespace();
		boolean first = true;
		while (r.current() != ']') {
			if (!first) {
				if (r.current() != ',') {
					throw new IOException("expected ',', but got " + r.current());
				}
				r.take(); 
				r.skipWhitespace();
			} else {
				first = false;
			}
			Node n = parseAny(dom, r);
			r.skipWhitespace();
			result.addItem(n);
		}
		r.take(); // eat ']'
		return result;
	}

	private static Node parseLiteralBoolean(JSONDOM dom, SimpleReader r) throws IOException {
		String bool = r.readExact(new String[] { "true", "false" });
		if (bool == null) {
			throw new IOException("could not parse a boolean from: " + r.remainder(10));
		}
		return dom.createLiteralBoolean(bool.equals("true"));
	}

	private static Node parseLiteralNumber(JSONDOM dom, SimpleReader r) throws IOException {
		String n = r.readWhile("0123456789.-eE");
		try {
			long l = Long.parseLong(n);
			return dom.createLiteralInteger(l);
		} catch (NumberFormatException e) {
			// not an int... perhaps a double?
		}
		try {
			double d = Double.parseDouble(n); // FIXME: not very proper parsing
			return dom.createLiteralNumber(d);
		} catch (NumberFormatException e) {
			throw new IOException("could not parse number from: '" + n + "'");
		}
	}

	private static Node parseLiteralString(JSONDOM dom, SimpleReader r) throws IOException {
		if (r.current() != '"') throw new IOException("string expected!");
		r.take();
		String data = r.takeToken('"', true);
		data = JSONUtils.deescape(data);
		return dom.createLiteralNode(data);
	}
}
