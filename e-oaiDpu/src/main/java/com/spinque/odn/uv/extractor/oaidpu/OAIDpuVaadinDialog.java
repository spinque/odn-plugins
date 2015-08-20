package com.spinque.odn.uv.extractor.oaidpu;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for OAIDpu.
 *
 * @author none
 */
public class OAIDpuVaadinDialog extends AbstractDialog<OAIDpuConfig_V1> {

	private static final long serialVersionUID = -7984174778840470581L;
	
	private TextField txtHarvestURL;
    private TextField txtMetadataPrefix;
    private TextField txtSetSpec;
    private TextField txtMaxDocs;
    
    public OAIDpuVaadinDialog() {
        super(OAIDpu.class);
    }

    @Override
    public void setConfiguration(OAIDpuConfig_V1 config) throws DPUConfigException {
    	txtHarvestURL.setValue(config.getHarvestURL());
    	txtMetadataPrefix.setValue(config.getMetadataPrefix());
    	txtSetSpec.setValue(config.getSetSpec());
    	txtMaxDocs.setValue("" + config.getMaxDocs());
    }

    @Override
    public OAIDpuConfig_V1 getConfiguration() throws DPUConfigException {
    	
    	/* check correctness */
    	boolean isValid = txtHarvestURL.isValid() && txtMetadataPrefix.isValid();
    	
    	try {
    		int number = Integer.parseInt(txtMaxDocs.getValue());
    		if (number < 0)
    			isValid = false;
    	} catch (NumberFormatException e) {
    		isValid = false;
    	}
    	if (!isValid) {
    		throw new DPUConfigException(ctx.tr("dialog.errors.params"));
    	}

        final OAIDpuConfig_V1 c = new OAIDpuConfig_V1();
        c.setHarvestURL(txtHarvestURL.getValue());
        c.setMetadataPrefix(txtMetadataPrefix.getValue());
        c.setSetSpec(txtSetSpec.getValue());
        c.setMaxDocs(Integer.parseInt(txtMaxDocs.getValue()));
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label(ctx.tr("OAIDpu.dialog.label")));
        
        /* add configuration, ask for:
         * - harvestURL
         * - metadataPrefix
         * - setSpec */

        txtHarvestURL = new TextField();
        txtHarvestURL.setCaption(ctx.tr("dialog.config.harvesturl"));
        txtHarvestURL.setRequired(true);
        txtHarvestURL.setNullRepresentation("");
        txtHarvestURL.setWidth("100%");
        mainLayout.addComponent(txtHarvestURL);
        
        txtMetadataPrefix = new TextField();
        txtMetadataPrefix.setCaption(ctx.tr("dialog.config.metadataprefix"));
        txtMetadataPrefix.setRequired(true);
        txtMetadataPrefix.setNullRepresentation("");
        txtMetadataPrefix.setWidth("100%");
        mainLayout.addComponent(txtMetadataPrefix);
        
        txtSetSpec = new TextField();
        txtSetSpec.setCaption(ctx.tr("dialog.config.setspec"));
        txtSetSpec.setRequired(false);
        txtSetSpec.setNullRepresentation("");
        txtSetSpec.setWidth("100%");
        mainLayout.addComponent(txtSetSpec);
        
        txtMaxDocs = new TextField();
        txtMaxDocs.setCaption(ctx.tr("dialog.config.maxdocs"));
        txtMaxDocs.setRequired(true);
        txtMaxDocs.setNullRepresentation("");
        txtMaxDocs.setWidth("100%");
        mainLayout.addComponent(txtMaxDocs);
        
        // (optionally: size=..., to limit the crawl when testing)

        setCompositionRoot(mainLayout);
    }
}
