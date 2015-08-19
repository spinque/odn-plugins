package com.spinque.odn.uv.transformer.jsondpu;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for JSONDpu.
 */
public class JSONDpuVaadinDialog extends AbstractDialog<JSONDpuConfig_V1> {

	private static final long serialVersionUID = -7984174778840470581L;
	
    private CheckBox checkboxOneObjectPerLine;
    
    public JSONDpuVaadinDialog() {
        super(JSONDpu.class);
    }

    @Override
    public void setConfiguration(JSONDpuConfig_V1 config) throws DPUConfigException {
    	checkboxOneObjectPerLine.setValue(config.getOneObjectPerLine());
    }

    @Override
    public JSONDpuConfig_V1 getConfiguration() throws DPUConfigException {
        final JSONDpuConfig_V1 c = new JSONDpuConfig_V1();
        c.setOneObjectPerLine(checkboxOneObjectPerLine.getValue());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label(ctx.tr("JSONDpu.dialog.label")));
        
        checkboxOneObjectPerLine = new CheckBox();
        checkboxOneObjectPerLine.setCaption(ctx.tr("JSONDpu.dialog.config.oneobjectperline"));
        checkboxOneObjectPerLine.setRequired(true);
        checkboxOneObjectPerLine.setWidth("100%");
        mainLayout.addComponent(checkboxOneObjectPerLine);

        setCompositionRoot(mainLayout);
    }
}
