package com.spinque.odn.uv.extractor.oaidpu;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public OAIDpu() {
		super(OAIDpuVaadinDialog.class, ConfigHistory.noHistory(OAIDpuConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "OAIDpu.message");
        
    }
	
}
