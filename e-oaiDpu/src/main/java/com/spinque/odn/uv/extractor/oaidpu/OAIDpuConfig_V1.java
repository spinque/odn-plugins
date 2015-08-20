package com.spinque.odn.uv.extractor.oaidpu;

/**
 * Configuration class for OAIDpu.
 *
 * @author none
 */
public class OAIDpuConfig_V1 {
	
	private String _harvestURL;
	private String _metadataPrefix;
	private String _setSpec; /* optional */
	private int _maxDocs = 0;

    public OAIDpuConfig_V1() {

    }

	public String getHarvestURL() {
		return _harvestURL;
	}

	public String getMetadataPrefix() {
		return _metadataPrefix;
	}

	public String getSetSpec() {
		return _setSpec;
	}

	public void setHarvestURL(String value) {
		_harvestURL = value;
	}

	public void setMetadataPrefix(String value) {
		_metadataPrefix = value;
	}

	public void setSetSpec(String value) {
		_setSpec = value;
	}
	
	public int getMaxDocs() {
		return _maxDocs;
	}
	public void setMaxDocs(int maxDocs) {
		_maxDocs = maxDocs;
	}
	
	@Override
	public String toString() {
		return "[OAI-PMH configuration " + _harvestURL + " " + _metadataPrefix + (_setSpec != null ? " (" + _setSpec + ")" : "");
	}
}
