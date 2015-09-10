package com.spinque.uv.extractor.oaicrawler;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for OaiCrawler DPU.
 *
 * @author none
 */
public class OaiCrawlerVaadinDialog extends AbstractDialog<OaiCrawlerConfig_V1> {

	private static final long serialVersionUID = -7984174778840470581L;
	
	private TextField txtHarvestURL;
    private TextField txtMetadataPrefix;
    private TextField txtSetSpec;
    private TextField txtMaxDocs;
    private TextField txtGroupSize;
    
    public OaiCrawlerVaadinDialog() {
        super(OaiCrawler.class);
    }

    @Override
    public void setConfiguration(OaiCrawlerConfig_V1 config) throws DPUConfigException {
    	txtHarvestURL.setValue(config.getHarvestURL());
    	txtMetadataPrefix.setValue(config.getMetadataPrefix());
    	txtSetSpec.setValue(config.getSetSpec());
    	txtMaxDocs.setValue("" + config.getMaxDocs());
    	txtGroupSize.setValue("" + config.getGroupSize());
    }

    @Override
    public OaiCrawlerConfig_V1 getConfiguration() throws DPUConfigException {
    	
    	/* check correctness */
    	boolean isValid = txtHarvestURL.isValid() && txtMetadataPrefix.isValid();
    	
    	try {
    		int number = Integer.parseInt(txtMaxDocs.getValue());
    		if (number < 0)	isValid = false;
    		number = Integer.parseInt(txtGroupSize.getValue());
    		if (number < 0)	isValid = false;
    	} catch (NumberFormatException e) {
    		isValid = false;
    	}
    	if (!isValid) {
    		throw new DPUConfigException(ctx.tr("dialog.errors.params"));
    	}

        final OaiCrawlerConfig_V1 config = new OaiCrawlerConfig_V1();
        config.setHarvestURL(txtHarvestURL.getValue());
        config.setMetadataPrefix(txtMetadataPrefix.getValue());
        config.setSetSpec(txtSetSpec.getValue());
        config.setMaxDocs(Integer.parseInt(txtMaxDocs.getValue()));
        config.setGroupSize(Integer.parseInt(txtGroupSize.getValue()));
        return config;
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
        txtHarvestURL.setCaption(ctx.tr("OAIDpu.dialog.config.harvesturl"));
        txtHarvestURL.setRequired(true);
        txtHarvestURL.setNullRepresentation("");
        txtHarvestURL.setWidth("100%");
        mainLayout.addComponent(txtHarvestURL);
        
        txtMetadataPrefix = new TextField();
        txtMetadataPrefix.setCaption(ctx.tr("OAIDpu.dialog.config.metadataprefix"));
        txtMetadataPrefix.setRequired(true);
        txtMetadataPrefix.setNullRepresentation("");
        txtMetadataPrefix.setWidth("100%");
        mainLayout.addComponent(txtMetadataPrefix);
        
        txtSetSpec = new TextField();
        txtSetSpec.setCaption(ctx.tr("OAIDpu.dialog.config.setspec"));
        txtSetSpec.setRequired(false);
        txtSetSpec.setNullRepresentation("");
        txtSetSpec.setWidth("100%");
        mainLayout.addComponent(txtSetSpec);
        
        txtMaxDocs = new TextField();
        txtMaxDocs.setCaption(ctx.tr("OAIDpu.dialog.config.maxdocs"));
        txtMaxDocs.setRequired(true);
        txtMaxDocs.setNullRepresentation("");
        txtMaxDocs.setWidth("100%");
        mainLayout.addComponent(txtMaxDocs);
        
        txtGroupSize = new TextField();
        txtGroupSize.setCaption(ctx.tr("OAIDpu.dialog.config.groupsize"));
        txtGroupSize.setRequired(true);
        txtGroupSize.setNullRepresentation("");
        txtGroupSize.setWidth("100%");
        mainLayout.addComponent(txtGroupSize);

        setCompositionRoot(mainLayout);
    }
}
