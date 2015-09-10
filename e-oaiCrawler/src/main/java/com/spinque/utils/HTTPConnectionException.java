package com.spinque.utils;

import java.io.IOException;

public class HTTPConnectionException extends IOException {

	private static final long serialVersionUID = 8355257854899268164L;
	private final int _reponseCode;

	public HTTPConnectionException(int responseCode, String error) {
		super(error);
		_reponseCode = responseCode;
	}
	
	public int getResponseCode() {
		return _reponseCode;
	}

}
