package com.spinque.odn.uv.extractor.oaidpu;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for OAIDpu.
 *
 * @author none
 */
public class OAIDpuVaadinDialog extends AbstractDialog<OAIDpuConfig_V1> {

    public OAIDpuVaadinDialog() {
        super(OAIDpu.class);
    }

    @Override
    public void setConfiguration(OAIDpuConfig_V1 c) throws DPUConfigException {
    	// TODO: set value of the configurable fields
    	// -- harvestURL
    	// -- metadataPrefix
    	// -- setSpec
    }

    @Override
    public OAIDpuConfig_V1 getConfiguration() throws DPUConfigException {
        final OAIDpuConfig_V1 c = new OAIDpuConfig_V1();

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label(ctx.tr("OAIDpu.dialog.label")));
        
        // TODO: add configuration, ask for:
        // -- harvestURL
        // -- metadataPrefix
        // -- setSpec
        // (optionally: size=..., to limit the crawl when testing)

        setCompositionRoot(mainLayout);
    }
}
