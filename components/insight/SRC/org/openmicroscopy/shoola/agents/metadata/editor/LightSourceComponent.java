/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ChannelAcquisitionData;
import omero.gateway.model.LightSourceData;

/** 
 * Component displaying information about the light source i.e.
 * arc, filament, laser or emitting diode.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class LightSourceComponent 
	extends JPanel
	implements PropertyChangeListener
{
	
	/** The fields displaying the metadata. */
	private Map<String, DataComponent> 			fieldsLight;
	
	/** Button to show or hides the unset fields of the light. */
	private JLabelButton						unsetLight;
	
	/** Flag indicating the unset fields for the light are displayed. */
	private boolean								unsetLightShown;
	
	/** The component displaying the laser options. */
	private OMEComboBox							laserMediumBox;
	
	/** The component displaying the types of supported light. */
	private OMEComboBox							lightTypeBox;
	
	/** The component displaying the types of arc. */
	private OMEComboBox							arcTypeBox;
	
	/** The component displaying the types of filament. */
	private OMEComboBox							filamentTypeBox;
	
	/** The component displaying the types of laser. */
	private OMEComboBox							laserTypeBox;
	
	/** The component displaying the tuneable option for a  laser. */
	private OMEComboBox							laserTuneableBox;
	
	/** The component displaying the pockel cell option for a  laser. */
	private OMEComboBox							laserPockelCellBox;
	
	/** The component displaying the types of laser. */
	private OMEComboBox							laserPulseBox;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI					parent;
	
	/** Reference to the Model. */
	private EditorModel							model;

	/** The index of the channel or <code>-1</code>. */
	private int									channelIndex;
	
	/** Shows or hides the unset fields. */
	private void displayUnsetLightFields()
	{
		unsetLightShown = !unsetLightShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetLightShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetLight.setText(s);
		parent.layoutFields(this, unsetLight, fieldsLight, 
				unsetLightShown);
	}
	
	/**
	 * Transforms the light source.
	 * 
	 * @param kind 		The kind of light source.
	 * @param details 	The value to transform.
	 */
	private void transformLightSource(String kind, Map<String, Object> details)
	{
		String title = EditorUtil.getLightSourceType(kind);
		setBorder(BorderFactory.createTitledBorder(title));
		
		DataComponent comp;
		JLabel label;
		JComponent area = null;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		Object selected;
		List notSet = (List) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetLight == null) {
			unsetLight = parent.formatUnsetFieldsControl();
			unsetLight.addPropertyChangeListener(this);
		}

		Set entrySet = details.entrySet();
		Entry entry;
		boolean set;
		String v;
		Iterator i = entrySet.iterator();
        while (i.hasNext()) {
        	entry = (Entry) i.next();
            key = (String) entry.getKey();
            set = !notSet.contains(key);
            value = entry.getValue();
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            if (LightSourceData.LASER.equals(kind)) {
            	if (EditorUtil.TYPE.equals(key)) {
            		selected = model.getChannelEnumerationSelected(
                			Editor.LASER_TYPE, (String) value);
                	if (selected != null) {
                		laserTypeBox.setSelectedItem(selected);
                		if (AcquisitionDataUI.UNSET_ENUM.contains(
                				selected.toString()))
    						set = false;
                	}
                	laserTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = laserTypeBox;//parent.replaceCombobox(laserTypeBox);
            	} else if (EditorUtil.MEDIUM.equals(key)) {
                	selected = model.getChannelEnumerationSelected(
                			Editor.LASER_MEDIUM, 
                			(String) value);
                	if (selected != null) {
                		laserMediumBox.setSelectedItem(selected);
                		if (AcquisitionDataUI.UNSET_ENUM.contains(
                				selected.toString()))
    						set = false;
                	}
                	laserMediumBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = laserMediumBox;//parent.replaceCombobox(laserMediumBox);
            	} else if (EditorUtil.PULSE.equals(key)) {
            		selected = model.getChannelEnumerationSelected(
                			Editor.LASER_PULSE, 
                			(String) value);
                	if (selected != null) {
                		laserPulseBox.setSelectedItem(selected);
                	} else {
                		set = false;
                		laserPulseBox.setSelectedIndex(
                				laserPulseBox.getItemCount()-1);
                	}
                		
                	laserPulseBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = laserPulseBox;//parent.replaceCombobox(laserPulseBox);
            	} else if (EditorUtil.TUNEABLE.equals(key)) { 
            		boolean b;
            		if (value != null) {
            			b = (Boolean) value;
            			if (b) 
            				laserTuneableBox.setSelectedItem(
            						AcquisitionDataUI.BOOLEAN_YES);
            			else 
            				laserTuneableBox.setSelectedItem(
            						AcquisitionDataUI.BOOLEAN_NO);
            		} else {
            			laserTuneableBox.setSelectedItem(
            			        AnnotationUI.NO_SET_TEXT);
            			set = false;
            		}
            		laserTuneableBox.setEditedColor(UIUtilities.EDITED_COLOR);
            		area = laserTuneableBox;//parent.replaceCombobox(laserTuneableBox);
            	} else if (EditorUtil.POCKEL_CELL.equals(key)) {
            		boolean b;
            		if (value != null) {
            			b = (Boolean) value;
            			if (b) 
            				laserPockelCellBox.setSelectedItem(
            					AcquisitionDataUI.BOOLEAN_YES);
            			else 
            				laserPockelCellBox.setSelectedItem(
            						AcquisitionDataUI.BOOLEAN_NO);
            		} else {
            			set = false;
            			laserPockelCellBox.setSelectedItem(
            			        AnnotationUI.NO_SET_TEXT);
            		}
            		laserPockelCellBox.setEditedColor(UIUtilities.EDITED_COLOR);
            		area = laserPockelCellBox;//parent.replaceCombobox(laserPockelCellBox);
            	} 
            } else if (LightSourceData.ARC.equals(kind)) {
            	if (EditorUtil.TYPE.equals(key)) {
            		selected = model.getChannelEnumerationSelected(
            				Editor.ARC_TYPE, (String) value);
                	if (selected != null) {
                		arcTypeBox.setSelectedItem(selected);
                		if (AcquisitionDataUI.UNSET_ENUM.contains(
                				selected.toString()))
    						set = false;
                	}
                	arcTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = arcTypeBox;//parent.replaceCombobox(arcTypeBox);
            	}
            } else if (LightSourceData.FILAMENT.equals(kind)) {
            	if (EditorUtil.TYPE.equals(key)) {
            		selected = model.getChannelEnumerationSelected(
                			Editor.FILAMENT_TYPE, (String) value);
                	if (selected != null) {
                		filamentTypeBox.setSelectedItem(selected);
                		if (AcquisitionDataUI.UNSET_ENUM.contains(
                				selected.toString()))
    						set = false;
                	}
                	filamentTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = filamentTypeBox;//parent.replaceCombobox(filamentTypeBox);
            	}
            } else if (LightSourceData.LIGHT_EMITTING_DIODE.equals(
            		kind)) {
            	if (EditorUtil.TYPE.equals(key)) {
            		
                	area = new JLabel();
            	}
            } else {
            	lightTypeBox.setSelectedIndex(lightTypeBox.getItemCount()-1);
            	area = lightTypeBox;//parent.replaceCombobox(lightTypeBox);
            }
            if (value instanceof Number) {
            	area = UIUtilities.createComponent(NumericalTextField.class, 
            			null);
            	if (value instanceof Double) {
            		v = ""+UIUtilities.roundTwoDecimals(
            				((Number) value).doubleValue());
            		((NumericalTextField) area).setNumberType(Double.class);
            	} else if (value instanceof Float) {
            		v = ""+UIUtilities.roundTwoDecimals(
            				((Number) value).doubleValue());
            		((NumericalTextField) area).setNumberType(Float.class);
            	} else v = ""+value;
            	((NumericalTextField) area).setText(v);
            	((NumericalTextField) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            } else if (EditorUtil.PUMP.equals(key)) {
            	area = UIUtilities.createComponent(OMETextArea.class, null);
            	((OMETextArea) area).setEditable(false);
            	((OMETextArea) area).setText((String) value);
            	((OMETextArea) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            } else if (EditorUtil.MODEL.equals(key) || 
            		EditorUtil.MANUFACTURER.equals(key) ||
            		EditorUtil.SERIAL_NUMBER.equals(key) ||
            		EditorUtil.LOT_NUMBER.equals(key)) {
            	area = UIUtilities.createComponent(OMETextArea.class, null);
            	if (value == null || value.equals("")) {
            		set = false;
            		value = AnnotationUI.DEFAULT_TEXT;
            	}
            	((OMETextArea) area).setEditable(false);
            	((OMETextArea) area).setText((String) value);
            	((OMETextArea) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            }
            else {
            	area = UIUtilities.createComponent(OMETextArea.class, null);
            	((OMETextArea) area).setEditable(false);
            	((OMETextArea) area).setText((String) value);
            	((OMETextArea) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            }
            if (area != null) {
            	area.setEnabled(!set);
            	comp = new DataComponent(label, area);
            	comp.setEnabled(false);
    			comp.setSetField(!notSet.contains(key));
    			fieldsLight.put(key, comp);
            }
        }
	}
	
	/** Resets the various boxes with enumerations. */
	private void resetBoxes()
	{
		List<EnumerationObject> l; 
		
		l = model.getChannelEnumerations(Editor.LASER_TYPE);
		laserTypeBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.ARC_TYPE);
		arcTypeBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.FILAMENT_TYPE);
		filamentTypeBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.LASER_MEDIUM);
		laserMediumBox = EditorUtil.createComboBox(l);
		
		l = model.getChannelEnumerations(Editor.LASER_PULSE);
		EnumerationObject[] array = new EnumerationObject[l.size()+1];
		Iterator<EnumerationObject> j = l.iterator();
		int i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationUI.NO_SET_TEXT);
		laserPulseBox = EditorUtil.createComboBox(array);
	}
	
	/** Handles the selection of the light source. */
	private void handleLightSourceSelection()
	{
		ChannelAcquisitionData data = model.getChannelAcquisitionData(
    			channelIndex);

		String selected = (String) lightTypeBox.getSelectedItem();
		String kind = "";
		if (EditorUtil.LASER_TYPE.equals(selected))
			kind = LightSourceData.LASER;
		else if (EditorUtil.FILAMENT_TYPE.equals(selected))
			kind = LightSourceData.FILAMENT;
		else if (EditorUtil.ARC_TYPE.equals(selected))
			kind = LightSourceData.ARC;
		else if (EditorUtil.EMITTING_DIODE_TYPE.equals(selected))
			kind = LightSourceData.LIGHT_EMITTING_DIODE;
		
		fieldsLight.clear();
		Map<String, Object> d = EditorUtil.transformLightSource(
				data.getLightSource());
		d.remove(EditorUtil.LIGHT_TYPE);
		transformLightSource(kind, d);
		parent.layoutFields(this, unsetLight, fieldsLight, 
				unsetLightShown);
		revalidate();
		repaint();
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		unsetLight = null;
		unsetLightShown = false;
		fieldsLight = new LinkedHashMap<String, DataComponent>();
		resetBoxes();
		String[] values = new String[3];
		values[0] = AcquisitionDataUI.BOOLEAN_YES;
		values[1] = AcquisitionDataUI.BOOLEAN_NO;
		values[2] = AnnotationUI.NO_SET_TEXT;
		laserTuneableBox = EditorUtil.createComboBox(values);
		laserPockelCellBox = EditorUtil.createComboBox(values);
		
		values = new String[5];
		values[0] = EditorUtil.ARC_TYPE;
		values[1] = EditorUtil.EMITTING_DIODE_TYPE;
		values[2] = EditorUtil.FILAMENT_TYPE;
		values[3] = EditorUtil.LASER_TYPE;
		values[4] = AnnotationUI.NO_SET_TEXT;
		lightTypeBox = EditorUtil.createComboBox(values);
		/*
		lightTypeBox.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				handleLightSourceSelection();
			}
		});
		*/
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(BorderFactory.createTitledBorder("Light"));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new GridBagLayout());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent 	The UI reference.
	 * @param model		Reference to the model.
	 */
	LightSourceComponent(AcquisitionDataUI parent, EditorModel model)
	{
		this.parent = parent;
		this.model = model;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Transforms the light metadata.
	 * 
	 * @param kind 		The kind of light source.
	 * @param details 	The value to transform.
	 */
	void displayLightSource(String kind, Map<String, Object> details)
	{
		resetBoxes();
		fieldsLight.clear();
		transformLightSource(kind, details);
		parent.layoutFields(this, unsetLight, fieldsLight, unsetLightShown);
    	parent.attachListener(fieldsLight);
	}
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		return parent.hasDataToSave(fieldsLight);
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
		if (JLabelButton.SELECTED_PROPERTY.equals(name)) 
			displayUnsetLightFields();
	}
	
}
