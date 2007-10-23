package xmlMVC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.JButton;

public class FormFieldCustom extends FormField {
	
	
	AttributesDialog attDialog;
	JButton showAttributesButton;
	boolean attributesDialogVisible = false;

	public FormFieldCustom(DataField dataField) {
		super(dataField);
		
		showAttributesButton = new JButton(ImageFactory.getInstance().getIcon(ImageFactory.NOTE_PAD));
		//showAttributesButton.addMouseListener(new FormPanelMouseListener());
		showAttributesButton.addActionListener(new ShowAttributesListener());
		showAttributesButton.setBorder(null);
		horizontalBox.add(showAttributesButton);
	}
	
	// toggle visibility of attributes dialog
	public class ShowAttributesListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			// first select the field (clears ALL fields - all dialogs hidden) then highlights this field
			int clickType = event.getModifiers();
			if (clickType == XMLView.SHIFT_CLICK) {
				panelClicked(false);
			} else
				panelClicked(true);
			
			// ...NOW show this dialog
			attributesDialogVisible = !attributesDialogVisible;
			showAttributes(attributesDialogVisible);
		}
	}
	
	public void showAttributes(boolean visible) {
		
		if (attDialog == null) attDialog = new AttributesDialog(this, dataField);
		
		attributesDialogVisible = visible;
		
		if (visible) attDialog.showAttributesDialog();	// also repositions dialog
		else attDialog.dispose();
	}
	
	// called when user clicks on panel
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		
		// always hide attributes dialog when de-selecting a field
		if (!highlight) showAttributes(false);
	}
	
}
