/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ObjectiveComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Font;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Component displaying details of an objective.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ObjectiveComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** Reference to the Model. */
	private EditorModel					model;
	
	/** Reference to the main acquisition component. */
	private AcquisitionDataUI			parent;

	/** Button to show or hides the unset fields. */
	private JLabelButton				unsetObjective;
	
	/** Flag indicating the unset fields for the objective are displayed. */
	private boolean						unsetObjectiveShown;
	
	/** The fields displaying the metadata. */
	private Map<String, DataComponent>	fieldsObjective;
	
	/** The component hosting the various immersion values. */
	private OMEComboBox					immersionBox;
	
	/** The component hosting the various coating values. */
	private OMEComboBox 				coatingBox;
	
	/** The component hosting the various medium values. */
	private OMEComboBox 				mediumBox;
	
	/** The component displaying the iris option for an objective. */
	private OMEComboBox					irisBox;
	
	/** Shows or hides the unset fields. */
	private void displayUnsetObjectiveFields()
	{
		unsetObjectiveShown = !unsetObjectiveShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetObjectiveShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetObjective.setText(s);
		parent.layoutFields(this, unsetObjective, fieldsObjective, 
				unsetObjectiveShown);
	}
	
	/**
	 * Transforms the objective metadata into the corresponding UI objects.
	 * 
	 * @param details The metadata to transform.
	 */
	private void transformObjective(Map<String, Object> details)
	{
		DataComponent comp;
		JLabel label;
		JComponent area;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		Object selected;
		List notSet = (List) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetObjective == null) {
			unsetObjective = parent.formatUnsetFieldsControl();
			unsetObjective.addPropertyChangeListener(this);
		}

		Set entrySet = details.entrySet();
		Entry entry;
		boolean set;
		boolean b;
		Iterator i = entrySet.iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			set = !notSet.contains(key);
			value = entry.getValue();
			label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
			label.setBackground(UIUtilities.BACKGROUND_COLOR);
			if (key.equals(EditorUtil.IMMERSION)) {
				selected = model.getImageEnumerationSelected(
						Editor.IMMERSION, (String) value);
				if (selected != null) {
					immersionBox.setSelectedItem(selected);
					if (AcquisitionDataUI.UNSET_ENUM.contains(
							selected.toString()))
						set = false;
				}
				immersionBox.setEditedColor(UIUtilities.EDITED_COLOR);
				area = immersionBox;//parent.replaceCombobox(immersionBox);
			} else if (key.equals(EditorUtil.CORRECTION)) {
				selected = model.getImageEnumerationSelected(
						Editor.CORRECTION, (String) value);
				if (selected != null) {
					coatingBox.setSelectedItem(selected);
					if (AcquisitionDataUI.UNSET_ENUM.contains(
							selected.toString()))
						set = false;
				}
				coatingBox.setEditedColor(UIUtilities.EDITED_COLOR);
				area = coatingBox;//parent.replaceCombobox(coatingBox);
			} else if (key.equals(EditorUtil.MEDIUM)) {
				selected = model.getImageEnumerationSelected(
						Editor.MEDIUM, (String) value);
				if (selected != null) {
					mediumBox.setSelectedItem(selected);
				} else {
					set = false;
					mediumBox.setSelectedIndex(mediumBox.getItemCount()-1);
				}
				mediumBox.setEditedColor(UIUtilities.EDITED_COLOR);
				area = mediumBox;//parent.replaceCombobox(mediumBox);
			} else if (key.equals(EditorUtil.IRIS)) {
        		if (value != null) {
        			b = (Boolean) value;
        			if (b) 
        				irisBox.setSelectedItem(AcquisitionDataUI.BOOLEAN_YES);
        			else irisBox.setSelectedItem(AcquisitionDataUI.BOOLEAN_NO);
        		} else {
        			set = false;
        			irisBox.setSelectedItem(AnnotationUI.NO_SET_TEXT);
        		}
        		irisBox.setEditedColor(UIUtilities.EDITED_COLOR);
        		area = irisBox;//parent.replaceCombobox(irisBox);
			} else {
				if (value instanceof Number) {
					area = UIUtilities.createComponent(
							NumericalTextField.class, null);
					Number n = (Number) value;
					String rounded = "";
					if (value instanceof Double) {
						rounded = ""+UIUtilities.roundTwoDecimals(
								n.doubleValue());
						((NumericalTextField) area).setNumberType(
								Double.class);
					} else if (value instanceof Float) {
						rounded = ""+UIUtilities.roundTwoDecimals(
								n.doubleValue());
						((NumericalTextField) area).setNumberType(
								Float.class);
					} else if (value instanceof Integer)
						rounded = ""+n.intValue();
					((NumericalTextField) area).setText(rounded);
					((NumericalTextField) area).setToolTipText(""+value);
					((NumericalTextField) area).setEditedColor(
							UIUtilities.EDITED_COLOR);
				} else {
					area = UIUtilities.createComponent(OMETextArea.class, 
							null);
					if (value == null || value.equals(""))
						value = AnnotationUI.DEFAULT_TEXT;
					((OMETextArea) area).setText((String) value);
					((OMETextArea) area).setEditedColor(
							UIUtilities.EDITED_COLOR);
				}
			}
			area.setEnabled(!set);
			comp = new DataComponent(label, area);
			comp.setEnabled(false);
			comp.setSetField(!notSet.contains(key));
			fieldsObjective.put(key, comp);
		}
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		unsetObjective = null;
		unsetObjectiveShown = false;
		fieldsObjective = new LinkedHashMap<String, DataComponent>();
		resetBoxes();
		String[] values = new String[3];
		values[0] = AcquisitionDataUI.BOOLEAN_YES;
		values[1] = AcquisitionDataUI.BOOLEAN_NO;
		values[2] = AnnotationUI.NO_SET_TEXT;
		irisBox = EditorUtil.createComboBox(values);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(BorderFactory.createTitledBorder("Objective"));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
	}

	/** Resets the various boxes with enumerations. */
	private void resetBoxes()
	{
		List<EnumerationObject> l; 	
		l = model.getImageEnumerations(Editor.IMMERSION);
		immersionBox = EditorUtil.createComboBox(l);
		l = model.getImageEnumerations(Editor.CORRECTION);
		coatingBox = EditorUtil.createComboBox(l);
		l = model.getImageEnumerations(Editor.MEDIUM);
		EnumerationObject[] array = new EnumerationObject[l.size()+1];
		Iterator<EnumerationObject> j = l.iterator();
		int i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationUI.NO_SET_TEXT);
		mediumBox = EditorUtil.createComboBox(array);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent 	The UI reference.
	 * @param model		Reference to the model.
	 */
	ObjectiveComponent(AcquisitionDataUI parent, EditorModel model)
	{
		this.parent = parent;
		this.model = model;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Transforms the objective metadata into the corresponding UI objects.
	 * 
	 * @param details The metadata to transform.
	 */
	void displayObjective(Map<String, Object> details)
	{
		resetBoxes();
		setOpaque(true);
		fieldsObjective.clear();
		transformObjective(details);
		parent.layoutFields(this, unsetObjective, fieldsObjective, 
				unsetObjectiveShown);
		parent.attachListener(fieldsObjective);
	}
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		return parent.hasDataToSave(fieldsObjective);
	}
	
	/** Prepares the data to save. */
	void prepareDataToSave()
	{
		
	}
	
	/**
	 * Reacts to property fired by the <code>JLabelButton</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (JLabelButton.SELECTED_PROPERTY.equals(name)) {
			displayUnsetObjectiveFields();
		}
	}
	
}

