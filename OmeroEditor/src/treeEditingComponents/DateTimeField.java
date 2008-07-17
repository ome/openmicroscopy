 /*
 * treeEditingComponents.DateTimeField 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package treeEditingComponents;

//Java imports

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import fields.DateTimeValueObject;
import fields.FieldPanel;
import fields.IField;

import omeroCal.view.DatePicker;
import ui.components.CustomComboBox;
import uiComponents.HrsMinsEditor;


/** 
 * This is the UI component for editing a DateTime experimental value. 
 * It includes a Date-Picker for picking a specific date.
 * Alternatively, users can use a comboBox to choose a "relative" date 
 * (eg 3 days after a previous date in the experiment).
 * Finally, a checkBox allows you to "Set Time", in which case, a 
 * time field is shown. 
 * 
 * The Date-Picker and ComboBox are mutually exclusive: Only one can show a value.
 * All components cause the IFieldValue to be updated, by calling
 * setAttribute(name, value);
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DateTimeField 
	extends JPanel 
	implements PropertyChangeListener {
	
	private IField field;
	
	
	DatePicker datePicker;
	
	CustomComboBox daySelector;
	
	JCheckBox timeChosen;
	
	HrsMinsEditor hrsMinsEditor;
	
	public DateTimeField (IField field) {
		
		this.field = field;
		
		this.setBackground(null);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		datePicker = new DatePicker();
		
		
		this.add(datePicker);
		this.add(new JLabel(" OR "));
		
		// A combo-box for days
		String[] dayOptions = {"Pick Day", "0 days", "1 day", "2 days","3 days","4 days",
				"5 days","6 days","7 days","8 days","9 days","10 days"};
		daySelector = new CustomComboBox(dayOptions);
		daySelector.setMaximumWidth(110);
		daySelector.setMaximumRowCount(dayOptions.length);
		
		this.add(daySelector);
		this.add(Box.createHorizontalStrut(10));
		this.add(new JLabel( " Set Time?"));
		
		String timeInSecs = field.getAttribute(DateTimeValueObject.TIME_ATTRIBUTE);
		
		timeChosen = new JCheckBox();
		timeChosen.setBackground(null);
		timeChosen.setSelected(timeInSecs != null);
		timeChosen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeSelected();
			}
		});
		this.add(timeChosen);
		
		
		hrsMinsEditor = new HrsMinsEditor();
		if (timeInSecs != null) {
			hrsMinsEditor.setHrsMins(new Integer(timeInSecs));
		}
		hrsMinsEditor.setVisible(timeInSecs != null);
		hrsMinsEditor.addPropertyChangeListener(HrsMinsEditor.TIME_IN_SECONDS, this);
		this.add(hrsMinsEditor);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (HrsMinsEditor.TIME_IN_SECONDS.equals(evt.getPropertyName())) {
			field.setAttribute(DateTimeValueObject.TIME_ATTRIBUTE, 
					evt.getNewValue().toString());
		}
		
	}
	
	public void timeSelected() {
		boolean showTime = timeChosen.isSelected();
		hrsMinsEditor.setVisible(showTime);
		
		if (showTime) {
			hrsMinsEditor.firePropertyChange(
					HrsMinsEditor.TIME_IN_SECONDS, 
					-1, 	// has to be an artificial "oldValue" so it's not 
							// the same as newValue (won't fire). Can't use null
					hrsMinsEditor.getDisplayedTimeInSecs());
		} else {
			field.setAttribute(DateTimeValueObject.TIME_ATTRIBUTE, 
					null);
		}
		/*
		 * Need to resize...
		 */
		this.firePropertyChange(FieldPanel.SIZE_CHANGED_PROPERTY, null, null);	
	}
	

}
