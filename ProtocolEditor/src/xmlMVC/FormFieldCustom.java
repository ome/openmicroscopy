package xmlMVC;

public class FormFieldCustom extends FormField {
	
	
	AttributesDialog attDialog;

	public FormFieldCustom(DataField dataField) {
		super(dataField);
		
		// System.out.println("FormFieldCustom Constructor..");
		
		// if dataField is created from an imported XML-element, need to initialise input_type
		// dataField.setAttribute(DataField.INPUT_TYPE, DataField.CUSTOM, false);
		
	}

	
	// called when user clicks on panel
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		
		showAttributes(highlight);	
		
		this.requestFocusInWindow();
	}
	
	public void showAttributes(boolean visible) {
		
		if (attDialog == null) attDialog = new AttributesDialog(this, dataField);
		
		if (visible) attDialog.showAttributesDialog();
		else attDialog.closeAttributeDialog();
	}
	
}
