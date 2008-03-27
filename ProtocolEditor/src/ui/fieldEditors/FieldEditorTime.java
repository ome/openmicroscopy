package ui.fieldEditors;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.AlarmSetter;
import ui.components.TimeEditor;

public class FieldEditorTime extends FieldEditor {
	

	TimeEditor timeEditor;
	
	public FieldEditorTime(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		timeEditor = new TimeEditor(dataField, DataFieldConstants.DEFAULT, "Default: ");
		
		attributeFieldsPanel.add(timeEditor);
		
		// this is called by the super() constructor, but at that time
		// not all components will have been instantiated. Calls enableEditing()
		refreshLockedStatus();
	}
	
	/**
	 * This is called by the superclass FieldEditor.dataFieldUpdated().
	 * Need to refresh the enabled status of additional components in this subclass. 
	 */
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);
		
		// need to check != null because this is called by the super() constructor
		// before all subclass components have been instantiated. 
		if (timeEditor != null) {
			timeEditor.setEnabled(enabled);
		}
	}

}
