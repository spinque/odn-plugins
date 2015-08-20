package com.spinque.odn.uv.transformer.jsondpu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.spinque.utils.Utils;
import com.spinque.utils.json.JSONDOM;
import com.spinque.utils.json.JSONParser;
import com.spinque.utils.json.JSONXMLWrapper;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit.Iteration;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

/**
 * Main data processing unit class.
 *
 * @author none
 */
@DPU.AsTransformer
public class JSONDpu extends AbstractDpu<JSONDpuConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(JSONDpu.class);

	private static final int MAX_SERIALIZED_JSON_SIZE = 1024*1024;
		
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;
    
    @DataUnit.AsInput(name = "input")
    public FilesDataUnit input;
    
    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit output;
    
	public JSONDpu() {
		super(JSONDpuVaadinDialog.class, ConfigHistory.noHistory(JSONDpuConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendShortInfo(ctx, "JSONDpu.message");
        
        
        LOG.info("Configuration: " + config.toString());
        try {
        	/* open input and output */
        	Iteration inputIterator = input.getIteration();
        	try {
        		while (inputIterator.hasNext()) {
        			FilesDataUnit.Entry entry = inputIterator.next();
        			if (config.getOneObjectPerLine()) {
        				BufferedReader br = new BufferedReader(new FileReader(new File(URI.create(entry.getFileURIString()))));
        				try {
        					String line = br.readLine();
        					if (line == null)
        						break;
        					emitJSONObject(line);
        				} finally {
        					br.close();
        				}
        			} else {
        				File inputFile = new File(URI.create(entry.getFileURIString()));
        				String data = Utils.readIntoString(inputFile, Charset.forName("UTF-8"), MAX_SERIALIZED_JSON_SIZE);
        				emitJSONObject(data);
        			}
        		}
			} finally {
        		inputIterator.close();
        	}
        } catch (MalformedURLException e) {
        	throw new DPUException(e);
		} catch (IOException e) {
			throw new DPUException(e);
        } catch (DataUnitException e) {
			throw new DPUException(e);
		} 
    }

	private void emitJSONObject(String line) throws DataUnitException, IOException {
		String fileName = output.addNewFile(UUID.randomUUID().toString());
		File outputFile = new File(fileName);
		
		/* seems like we have to make our own folders */
		outputFile.getParentFile().mkdirs();
		
		JSONDOM result = JSONParser.parse(line);
		JSONXMLWrapper wrapper = new JSONXMLWrapper(result);
		Document doc = Utils.newDocument();
		Utils.writeXML(outputFile, wrapper.toXML(doc));
	}	
}
