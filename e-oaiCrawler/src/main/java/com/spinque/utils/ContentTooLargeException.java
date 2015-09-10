package com.spinque.utils;

import java.io.IOException;

public class ContentTooLargeException extends IOException {

	private static final long serialVersionUID = 3100043788285025433L;

	public ContentTooLargeException(String url, long contentLength,
			long max_content_length) {
		super("Content for " + url + " is larger then max allowed (" + contentLength + "/" + max_content_length + " bytes)");
	}

}
