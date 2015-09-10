package com.spinque.uv.extractor.oaicrawler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.spinque.utils.Utils;
import com.spinque.uv.extractor.oaicrawler.crawler.OAIPMHCrawler;
import com.spinque.uv.extractor.oaicrawler.crawler.OAIPMHCrawler.Verb;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

/**
 * Main data processing unit class.
 *
 * @author none
 */
@DPU.AsExtractor
public class OaiCrawler extends AbstractDpu<OaiCrawlerConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(OaiCrawler.class);
    
    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit output;
    
	public OaiCrawler() {
		super(OaiCrawlerVaadinDialog.class, ConfigHistory.noHistory(OaiCrawlerConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendShortInfo(ctx, "OAIDpu.message");
        
        LOG.info("Configuration: " + config.toString());
        try {
        	List<Element> le = new ArrayList<Element>();
        	
        	/* crawl the OAI repository */
        	OAIPMHCrawler crawler = new OAIPMHCrawler(new URL(config.getHarvestURL()), config.getMetadataPrefix(), config.getSetSpec(), null, null, Verb.ListRecords, true);
        	Iterator<Element> iter = crawler.iterator();
        	int count = 0;
        	while (iter.hasNext()) {
        		le.add(iter.next());
        		
        		if (config.getGroupSize() > 0 && ++count % config.getGroupSize() == 0) {
        			flushElements(le);
        		}
        		if (count % 1000 == 0)
        			LOG.info("Crawled " + count + " OAI records so far...");

        		if (config.getMaxDocs() != 0 && count >= config.getMaxDocs())
        			break;
        	}
        	flushElements(le);	
        } catch (MalformedURLException e) {
        	throw new DPUException(e);
		} catch (IOException e) {
			throw new DPUException(e);
        } catch (DataUnitException e) {
			throw new DPUException(e);
		} 
    }
    
    private void flushElements(List<Element> le) throws DataUnitException, IOException {
    	if (le.isEmpty())
    		return;
    	
		/* The output is an XML file. If RDF is needed, then a XSLT DPU should be used after this */
		FilesDataUnit.Entry e = FilesDataUnitUtils.createFile(output,UUID.randomUUID().toString() + ".xml");
		File outputFile = new File(URI.create(e.getFileURIString()));
		
		Utils.writeXML(outputFile, le, "records");
		le.clear();    	
    }
	
}
