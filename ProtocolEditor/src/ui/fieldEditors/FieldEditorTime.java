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
	
	String timeValue;
	
	int defaultTimeInSecs;
	
	
	
	AlarmSetter alarmSetter;
	
	public FieldEditorTime(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		attributeFieldsPanel.add(new TimeEditor(dataField, DataFieldConstants.DEFAULT, "Default: "));
		
	}

}
