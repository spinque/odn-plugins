package com.spinque.odn.uv.extractor.oaidpu;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.spinque.odn.uv.extractor.oaidpu.crawler.OAIPMHCrawler;
import com.spinque.odn.uv.extractor.oaidpu.crawler.OAIPMHCrawler.Verb;
import com.spinque.utils.Utils;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

/**
 * Main data processing unit class.
 *
 * @author none
 */
@DPU.AsExtractor
public class OAIDpu extends AbstractDpu<OAIDpuConfig_V1> implements DPU {

    private static final Logger LOG = LoggerFactory.getLogger(OAIDpu.class);
		
    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit output;
    
	public OAIDpu() {
		super(OAIDpuVaadinDialog.class, ConfigHistory.noHistory(OAIDpuConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendShortInfo(ctx, "OAIDpu.message");
        
        
        LOG.info("Configuration: " + config.toString());
        try {
        	
        	/* crawl the OAI repository */
        	OAIPMHCrawler crawler = new OAIPMHCrawler(new URL(config.getHarvestURL()), config.getMetadataPrefix(), config.getSetSpec(), null, null, Verb.ListRecords);
        	Iterator<Element> iter = crawler.iterator();
        	while (iter.hasNext()) {
        		Element oaiRecord = iter.next();
        		String fileName = output.addNewFile(UUID.randomUUID().toString());
        		String data = Utils.processXML(oaiRecord, false);

        		/* add the data as a file to the ODN */
        		FileWriter fw = new FileWriter(fileName);
        		try {
        			fw.write(data);
        		} finally {
        			fw.close();
        		}
        	}
        } catch (MalformedURLException e) {
        	throw new DPUException(e);
		} catch (IOException e) {
			throw new DPUException(e);
        } catch (DataUnitException e) {
			throw new DPUException(e);
		} 
    }
	
}