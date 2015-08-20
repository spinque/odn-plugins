package com.spinque.odn.uv.transformer.jsondpu;

/**
 * Configuration class for JSONDpu.
 */
public class JSONDpuConfig_V1 {
	
	/* whether files should be read as a whole (one json object), 
	 * or whether files would be read per line (one json object per line).
	 */
	private boolean _oneObjectPerLine;

    public JSONDpuConfig_V1() {

    }
    
    public void setOneObjectPerLine(boolean oneObjectPerLine) {
		_oneObjectPerLine = oneObjectPerLine;
	}

	public boolean getOneObjectPerLine() {
		return _oneObjectPerLine;
	}

	@Override
	public String toString() {
		return "[JSON configuration " + (_oneObjectPerLine ? "one-object-per-line" : "one-object-per-file") + "]";
	}
}
