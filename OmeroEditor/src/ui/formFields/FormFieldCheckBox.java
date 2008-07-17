package ui.formFields;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import fields.FieldPanel;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;

public class FormFieldCheckBox extends FieldPanel {
	
	boolean checkedValue;
	JCheckBox checkBox;
	
	public FormFieldCheckBox (IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		checkedValue = dataField.isAttributeTrue(DataFieldConstants.VALUE);
		
		checkBox = new JCheckBox(" ", checkedValue);
		checkBox.addActionListener(new CheckBoxListener());
		checkBox.addFocusListener(componentFocusListener);
		checkBox.setBackground(null);
		
		horizontalBox.add(checkBox);
		
		// enable or disable components based on the locked status of this field
		refreshLockedStatus();
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		
		if (checkBox != null)	// just in case!
			checkBox.setEnabled(enabled);
	}
	
	/**
	 * Gets the names of the attributes where this field stores its "value"s.
	 * This is used eg. (if a single value is returned)
	 * as the destination to copy the default value when defaults are loaded.
	 * Also used by EditClearFields to set all values back to null. 
	 * Mostly this is DataFieldConstants.VALUE, but this method should be over-ridden by 
	 * subclasses if they want to store their values under a different attributes (eg "seconds" for TimeField)
	 * 
	 * @return	the name of the attribute that holds the "value" of this field
	 */
	public String[] getValueAttributes() {
		return new String[] {DataFieldConstants.VALUE};
	}
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * To fill out an unChecked value for this field, user will have to select, then
	 * unSelect the checkBox. 
	 * 
	 * @see FormField.isFieldFilled()
	 * @return	True if the field has been filled out by user (Required values are not null)
	 */
	public boolean isFieldFilled() {
		return (dataField.getAttribute(DataFieldConstants.VALUE) != null);
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		checkedValue = dataField.isAttributeTrue(DataFieldConstants.VALUE);
		checkBox.setSelected(checkedValue);
	} 
	
	
	
	
	public class CheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			 checkedValue = checkBox.isSelected();
			 
			 String value = Boolean.toString(checkedValue);
			 dataField.setAttribute(DataFieldConstants.VALUE, value, true);
		}
		
	}
	
	public void setSelected(boolean highlight) {
		super.setSelected(highlight);
		// if the user highlighted this field by clicking the field (not the checkBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (!checkBox.hasFocus()))
			checkBox.requestFocusInWindow();
	}
	
}
