package com.spinque.odn.uv.extractor.oaidpu;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
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
@DPU.AsExtractor
public class OAIDpu extends AbstractDpu<OAIDpuConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(OAIDpu.class);
		
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;
    
    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit output;
    
    @DataUnit.AsOutput(name = "outputRDF")
    public WritableRDFDataUnit outputRDF;

	public OAIDpu() {
		super(OAIDpuVaadinDialog.class, ConfigHistory.noHistory(OAIDpuConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendShortInfo(ctx, "OAIDpu.message");
        
        
        LOG.info("Configuration: " + config.toString());
        try {
        	RepositoryConnection rc = outputRDF.getConnection();
        	try {
        		/* TODO: crawl the OAI repository */
        		/* TODO: insert RDF fragments into the RepositoryConnection */
        	} finally {
        		rc.close();
        	}
		} catch (RepositoryException e) {
			throw new DPUException(e);
        } catch (DataUnitException e) {
			throw new DPUException(e);
		} 
    }
	
}
