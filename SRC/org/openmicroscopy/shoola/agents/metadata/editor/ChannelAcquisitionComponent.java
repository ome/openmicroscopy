/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ChannelAcquisitionComponent 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import omero.model.AcquisitionMode;
import omero.model.ContrastMethod;
import omero.model.Illumination;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;

/** 
 * Displays the metadata related to the channel.
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
class ChannelAcquisitionComponent
	extends JPanel
{
	
	/** Identifies the Laser type. */
	private static final String LASER_TYPE = "Laser";
	
	/** Identifies the Arc type. */
	private static final String ARC_TYPE = "Arc";
	
	/** Identifies the Filament type. */
	private static final String FILAMENT_TYPE = "Filament";
	
	/** Identifies the Emitting Diode type. */
	private static final String EMITTING_DIODE_TYPE = "Emitting Diode";
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI					parent;
	
	/** The channel data. */
	private ChannelData 						channel;
	
	/** The component displaying the illumination's options. */
	private OMEComboBox							illuminationBox;
	
	/** The component displaying the contrast Method options. */
	private OMEComboBox							contrastMethodBox;
	
	/** The component displaying the contrast Method options. */
	private OMEComboBox							modeBox;
	
	/** The component displaying the binning options. */
	private OMEComboBox							binningBox;
	
	/** The component displaying the detector options. */
	private OMEComboBox							detectorBox;
	
	/** The component displaying the laser options. */
	private OMEComboBox							laserMediumBox;
	
	/** The component displaying the types of supported ligth. */
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
	
	/** The fields displaying the metadata. */
	private Map<String, AcquisitionComponent> 	fieldsGeneral;
	
	/** The fields displaying the metadata. */
	private Map<String, AcquisitionComponent> 	fieldsDetector;
	
	/** The fields displaying the metadata. */
	private Map<String, AcquisitionComponent> 	fieldsLight;
	
	/** Button to show or hides the unset fields of the light. */
	private JButton								unsetLight;
	
	/** Flag indicating the unset fields for the light are displayed. */
	private boolean								unsetLightShown;
	
	/** The UI component hosting the objective metadata. */
	private JPanel								lightPane;
	
	/** Button to show or hides the unset fields of the detector. */
	private JButton								unsetDetector;
	
	/** Flag indicating the unset fields for the detector are displayed. */
	private boolean								unsetDetectorShown;
	
	/** The UI component hosting the detector metadata. */
	private JPanel								detectorPane;
	
	/** Button to show or hides the unset fields of the detector. */
	private JButton								unsetGeneral;
	
	/** Flag indicating the unset fields for the general are displayed. */
	private boolean								unsetGeneralShown;
	
	/** The UI component hosting the general metadata. */
	private JPanel								generalPane;
	
	/** Flag indicating if the components have been initialized. */
	private boolean								init;
	
	/** Reference to the Model. */
	private EditorModel							model;

	/** Resets the various boxes with enumerations. */
	private void resetBoxes()
	{
		List<EnumerationObject> l; 
		l = model.getChannelEnumerations(Editor.ILLUMINATION_TYPE);
		EnumerationObject[] array = new EnumerationObject[l.size()+1];
		Iterator<EnumerationObject> j = l.iterator();
		int i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		illuminationBox = EditorUtil.createComboBox(array);
		
		l = model.getChannelEnumerations(Editor.CONTRAST_METHOD);
		array = new EnumerationObject[l.size()+1];
		j = l.iterator();
		i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		contrastMethodBox = EditorUtil.createComboBox(array);
		
		l = model.getChannelEnumerations(Editor.MODE);
		array = new EnumerationObject[l.size()+1];
		j = l.iterator();
		i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		modeBox = EditorUtil.createComboBox(array);
		
		l = model.getChannelEnumerations(Editor.BINNING);
		array = new EnumerationObject[l.size()+1];
		j = l.iterator();
		i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		binningBox = EditorUtil.createComboBox(array);
		
		l = model.getChannelEnumerations(Editor.DETECTOR_TYPE);
		detectorBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.LASER_TYPE);
		laserTypeBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.ARC_TYPE);
		arcTypeBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.FILAMENT_TYPE);
		filamentTypeBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.LASER_MEDIUM);
		laserMediumBox = EditorUtil.createComboBox(l);
		
		l = model.getChannelEnumerations(Editor.LASER_PULSE);
		array = new EnumerationObject[l.size()+1];
		j = l.iterator();
		i = 0;
		while (j.hasNext()) {
			array[i] = j.next();
			i++;
		}
		array[i] = new EnumerationObject(AnnotationDataUI.NO_SET_TEXT);
		laserPulseBox = EditorUtil.createComboBox(array);
	}
	
	/** Initializes the components */
	private void initComponents()
	{
		resetBoxes();
		
		String[] values = new String[3];
		values[0] = AcquisitionDataUI.BOOLEAN_YES;
		values[1] = AcquisitionDataUI.BOOLEAN_NO;
		values[2] = AnnotationDataUI.NO_SET_TEXT;
		laserTuneableBox = EditorUtil.createComboBox(values);
		laserPockelCellBox = EditorUtil.createComboBox(values);
		
		values = new String[5];
		values[0] = ARC_TYPE;
		values[1] = EMITTING_DIODE_TYPE;
		values[2] = FILAMENT_TYPE;
		values[3] = LASER_TYPE;
		values[4] = AnnotationDataUI.NO_SET_TEXT;
		lightTypeBox = EditorUtil.createComboBox(values);
		lightTypeBox.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				handleLightSourceSelection();
			}
		});
		fieldsGeneral = new LinkedHashMap<String, AcquisitionComponent>();
		fieldsDetector = new LinkedHashMap<String, AcquisitionComponent>();
		fieldsLight = new LinkedHashMap<String, AcquisitionComponent>();
		
		unsetDetector = null;
		unsetDetectorShown = false;
		detectorPane = new JPanel();
		detectorPane.setBorder(BorderFactory.createTitledBorder("Camera"));
		detectorPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		detectorPane.setLayout(new GridBagLayout());
		unsetLight = null;
		unsetLightShown = false;
		lightPane = new JPanel();
		lightPane.setBorder(BorderFactory.createTitledBorder("Light"));
		lightPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		lightPane.setLayout(new GridBagLayout());
		unsetGeneral = null;
		unsetGeneralShown = false;
		generalPane = new JPanel();
		generalPane.setBorder(BorderFactory.createTitledBorder("Info"));
		generalPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		generalPane.setLayout(new GridBagLayout());
	}
	
	/** Handles the selection of the light source. */
	private void handleLightSourceSelection()
	{
		ChannelAcquisitionData data = model.getChannelAcquisitionData(
    			channel.getIndex());

		String selected = (String) lightTypeBox.getSelectedItem();
		String kind = "";
		if (LASER_TYPE.equals(selected))
			kind = ChannelAcquisitionData.LASER;
		else if (FILAMENT_TYPE.equals(selected))
			kind = ChannelAcquisitionData.FILAMENT;
		else if (ARC_TYPE.equals(selected))
			kind = ChannelAcquisitionData.ARC;
		else if (EMITTING_DIODE_TYPE.equals(selected))
			kind = ChannelAcquisitionData.LIGHT_EMITTING_DIODE;
		
		fieldsLight.clear();
		Map<String, Object> d = EditorUtil.transformLightSource(kind, data);
		d.remove(EditorUtil.LIGHT_TYPE);
		transformLightSource(kind, d);
		parent.layoutFields(lightPane, unsetLight, fieldsLight, 
				unsetLightShown);
		revalidate();
		repaint();
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetLightFields()
	{
		unsetLightShown = !unsetLightShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetLightShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetLight.setText(s);
		parent.layoutFields(lightPane, unsetLight, fieldsLight, 
				unsetLightShown);
	}
	
	/**
	 * Transforms the light metadata.
	 * 
	 * @param kind 		The kind of light source.
	 * @param details 	The value to transform.
	 */
	private void transformLightSource(String kind, Map<String, Object> details)
	{
		String title = "Light Source";
		if (ChannelAcquisitionData.LASER.equals(kind))
			title = LASER_TYPE;
		else if (ChannelAcquisitionData.ARC.equals(kind))
			title = ARC_TYPE;
		else if (ChannelAcquisitionData.FILAMENT.equals(kind))
			title = FILAMENT_TYPE;
		else if (ChannelAcquisitionData.LIGHT_EMITTING_DIODE.equals(kind))
			title = EMITTING_DIODE_TYPE;
		lightPane.setBorder(BorderFactory.createTitledBorder(title));
		
		AcquisitionComponent comp;
		JLabel label;
		JComponent area = null;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		Object selected;
		List<String> notSet = (List<String>) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetLight == null) {
			unsetLight = parent.formatUnsetFieldsControl();
			unsetLight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					displayUnsetLightFields();
				}
			});
		}

		boolean set;
		Iterator i = details.keySet().iterator();
        while (i.hasNext()) {
            key = (String) i.next();
            set = !notSet.contains(key);
            value = details.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            if (ChannelAcquisitionData.LASER.equals(kind)) {
            	if (key.equals(EditorUtil.TYPE)) {
            		selected = model.getChannelEnumerationSelected(
                			Editor.LASER_TYPE, (String) value);
                	if (selected != null) {
                		laserTypeBox.setSelectedItem(selected);
                		if (AcquisitionDataUI.UNSET_ENUM.contains(selected))
    						set = false;
                	}
                	laserTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = laserTypeBox;
            	} else if (key.equals(EditorUtil.MEDIUM)) {
                	selected = model.getChannelEnumerationSelected(
                			Editor.LASER_MEDIUM, 
                			(String) value);
                	if (selected != null) {
                		laserMediumBox.setSelectedItem(selected);
                		if (AcquisitionDataUI.UNSET_ENUM.contains(selected))
    						set = false;
                	}
                	laserMediumBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = laserMediumBox;
            	} else if (key.equals(EditorUtil.PULSE)) {
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
                	area = laserPulseBox;
            	} else if (key.equals(EditorUtil.TUNEABLE)) { 
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
            					AnnotationDataUI.NO_SET_TEXT);
            			set = false;
            		}
            		laserTuneableBox.setEditedColor(UIUtilities.EDITED_COLOR);
            		area = laserTuneableBox;
            	} else if (key.equals(EditorUtil.POCKEL_CELL)) {
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
            					AnnotationDataUI.NO_SET_TEXT);
            		}
            		laserPockelCellBox.setEditedColor(UIUtilities.EDITED_COLOR);
            		area = laserPockelCellBox;
            	} 
            } else if (ChannelAcquisitionData.ARC.equals(kind)) {
            	if (key.equals(EditorUtil.TYPE)) {
            		selected = model.getChannelEnumerationSelected(
            				Editor.ARC_TYPE, (String) value);
                	if (selected != null) {
                		arcTypeBox.setSelectedItem(selected);
                		if (AcquisitionDataUI.UNSET_ENUM.contains(selected))
    						set = false;
                	}
                	arcTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = arcTypeBox;
            	}
            } else if (ChannelAcquisitionData.FILAMENT.equals(kind)) {
            	if (key.equals(EditorUtil.TYPE)) {
            		selected = model.getChannelEnumerationSelected(
                			Editor.FILAMENT_TYPE, (String) value);
                	if (selected != null) {
                		filamentTypeBox.setSelectedItem(selected);
                		if (AcquisitionDataUI.UNSET_ENUM.contains(selected))
    						set = false;
                	}
                	filamentTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = filamentTypeBox;
            	}
            } else if (ChannelAcquisitionData.LIGHT_EMITTING_DIODE.equals(
            		kind)) {
            	if (key.equals(EditorUtil.TYPE)) {
            		
                	area = new JLabel();
            	}
            } else {
            	lightTypeBox.setSelectedIndex(lightTypeBox.getItemCount()-1);
            	area = lightTypeBox;
            }
            if (value instanceof Number) {
            	area = UIUtilities.createComponent(NumericalTextField.class, 
            			null);
            	if (value instanceof Double) 
            		((NumericalTextField) area).setNumberType(Double.class);
            	else if (value instanceof Float) 
            		((NumericalTextField) area).setNumberType(Float.class);
            	((NumericalTextField) area).setText(""+value);
            	((NumericalTextField) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            } else if (key.equals(EditorUtil.PUMP)) {
            	boolean b = (Boolean) value;
            	area = UIUtilities.createComponent(OMETextArea.class, null);
            	/*
            	area = UIUtilities.createComponent(OMETextArea.class, null);
            	if (value == null || value.equals("")) 
                	value = AnnotationUI.DEFAULT_TEXT;
            	 ((OMETextArea) area).setEditable(false);
            	 ((OMETextArea) area).setText((String) value);
            	 ((OMETextArea) area).setEditedColor(
            			 UIUtilities.EDITED_COLOR);
            			 */
            } else if (key.equals(EditorUtil.MODEL) || 
            		key.equals(EditorUtil.MANUFACTURER) ||
            		key.equals(EditorUtil.SERIAL_NUMBER)) {
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
            if (area != null) {
            	area.setEnabled(!set);
            	comp = new AcquisitionComponent(label, area);
    			comp.setSetField(!notSet.contains(key));
    			fieldsLight.put(key, comp);
            }
        }
	}
	
	/** Shows or hides the unset fields. */
	private void displayUnsetDetectorFields()
	{
		unsetDetectorShown = !unsetDetectorShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetDetectorShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetDetector.setText(s);
		parent.layoutFields(detectorPane, unsetDetector, fieldsDetector, 
				unsetDetectorShown);
	}
	
	/**
	 * Transforms the detector metadata.
	 * 
	 * @param details The value to transform.
	 */
	private void transformDetectorSource(Map<String, Object> details)
	{
		AcquisitionComponent comp;
		JLabel label;
		JComponent area;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		Object selected;
		List<String> notSet = (List<String>) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetDetector == null) {
			unsetDetector = parent.formatUnsetFieldsControl();
			unsetDetector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					displayUnsetDetectorFields();
				}
			});
		}

		Iterator i = details.keySet().iterator();
		boolean set;
		while (i.hasNext()) {
            key = (String) i.next();
            set = !notSet.contains(key);
            value = details.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            if (key.equals(EditorUtil.BINNING)) {
            	selected = model.getChannelEnumerationSelected(Editor.BINNING, 
            			(String) value);
            	if (selected != null) binningBox.setSelectedItem(selected);
            	else {
            		set = false;
            		binningBox.setSelectedIndex(binningBox.getItemCount()-1);
            	}
            	binningBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = binningBox;
            } else if (key.equals(EditorUtil.TYPE)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.DETECTOR_TYPE, 
            			(String) value);
            	if (selected != null) {
            		detectorBox.setSelectedItem(selected);
            		if (AcquisitionDataUI.UNSET_ENUM.contains(selected))
						set = false;
            	}
            	detectorBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = detectorBox;
            } else if (value instanceof Number) {
            	area = UIUtilities.createComponent(NumericalTextField.class, 
            			null);
            	if (value instanceof Double) 
            		((NumericalTextField) area).setNumberType(Double.class);
            	else if (value instanceof Float) 
            		((NumericalTextField) area).setNumberType(Float.class);
            	((NumericalTextField) area).setText(""+value);
            	((NumericalTextField) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            } else {
            	area = UIUtilities.createComponent(OMETextArea.class, null);
            	if (value == null || value.equals("")) 
                	value = AnnotationUI.DEFAULT_TEXT;
            	 ((OMETextArea) area).setEditable(false);
            	 ((OMETextArea) area).setText((String) value);
            	 ((OMETextArea) area).setEditedColor(UIUtilities.EDITED_COLOR);
            }
            area.setEnabled(!set);
            comp = new AcquisitionComponent(label, area);
			comp.setSetField(!notSet.contains(key));
			fieldsDetector.put(key, comp);
        }
	}
    
	/** Shows or hides the unset fields. */
	private void displayUnsetGeneralFields()
	{
		unsetGeneralShown = !unsetGeneralShown;
		String s = AcquisitionDataUI.SHOW_UNSET;
		if (unsetGeneralShown) s = AcquisitionDataUI.HIDE_UNSET;
		unsetGeneral.setText(s);
		parent.layoutFields(generalPane, unsetGeneral, fieldsGeneral, 
				unsetGeneralShown);
	}
	
	/**
	 * Transforms the detector metadata.
	 * 
	 * @param details The value to transform.
	 */
	private void transformGeneralSource(Map<String, Object> details)
	{
		AcquisitionComponent comp;
		JLabel label;
		JComponent area;
		String key;
		Object value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		Object selected;
		List<String> notSet = (List<String>) details.get(EditorUtil.NOT_SET);
		details.remove(EditorUtil.NOT_SET);
		if (notSet.size() > 0 && unsetGeneral == null) {
			unsetGeneral = parent.formatUnsetFieldsControl();
			unsetGeneral.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					displayUnsetGeneralFields();
				}
			});
		}

		Iterator i = details.keySet().iterator();
		boolean set;
		while (i.hasNext()) {
            key = (String) i.next();
            set = !notSet.contains(key);
            value = details.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            if (key.equals(EditorUtil.ILLUMINATION)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.ILLUMINATION_TYPE, 
            			(String) value);
            	if (selected != null) illuminationBox.setSelectedItem(selected);
            	else {
            		set = false;
            		illuminationBox.setSelectedIndex(
            				illuminationBox.getItemCount()-1);
            	}
            			
            	illuminationBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = illuminationBox;
            } else if (key.equals(EditorUtil.CONTRAST_METHOD)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.ILLUMINATION_TYPE, 
            			(String) value);
            	if (selected != null) 
            		contrastMethodBox.setSelectedItem(selected);
            	else {
            		set = false;
            		contrastMethodBox.setSelectedIndex(
            				contrastMethodBox.getItemCount()-1);
            	}
            			
            	contrastMethodBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = contrastMethodBox;
            } else if (key.equals(EditorUtil.MODE)) {
            	selected = model.getChannelEnumerationSelected(Editor.MODE, 
            			(String) value);
            	if (selected != null) modeBox.setSelectedItem(selected);
            	else {
            		set = false;
            		modeBox.setSelectedIndex(modeBox.getItemCount()-1);
            	}
            	modeBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = modeBox;
            } else {
            	if (value instanceof Number) {
            		area = UIUtilities.createComponent(
             				 NumericalTextField.class, null);
            		if (value instanceof Double) 
                		((NumericalTextField) area).setNumberType(Double.class);
                	else if (value instanceof Float) 
            			((NumericalTextField) area).setNumberType(Float.class);
             		 ((NumericalTextField) area).setText(""+value);
             		((NumericalTextField) area).setEditedColor(
             				UIUtilities.EDITED_COLOR);
            	} else {
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
            }
            area.setEnabled(!set);
            comp = new AcquisitionComponent(label, area);
			comp.setSetField(!notSet.contains(key));
			fieldsGeneral.put(key, comp);
        }
	}
	
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		parent.layoutFields(detectorPane, unsetDetector, fieldsDetector, 
				unsetDetectorShown);
		parent.layoutFields(lightPane, unsetLight, fieldsLight, 
				unsetLightShown);
		parent.layoutFields(generalPane, unsetGeneral, fieldsGeneral, 
				unsetGeneralShown);
    	add(generalPane);;
    	add(detectorPane);
    	add(lightPane);

    	parent.attachListener(fieldsGeneral);
    	parent.attachListener(fieldsDetector);
    	parent.attachListener(fieldsLight);
    }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	Reference to the Parent. Mustn't be <code>null</code>.
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 * @param channel 	The channel to display. Mustn't be <code>null</code>.
	 */
	ChannelAcquisitionComponent(AcquisitionDataUI parent, 
			EditorModel model, ChannelData channel)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (parent == null)
			throw new IllegalArgumentException("No parent.");
		if (channel == null)
			throw new IllegalArgumentException("No channel.");
		this.model = model;
		this.channel = channel;
		this.parent = parent;
		initComponents();
	}
	
	/**
	 * Displays the acquisition data for the passed channel.
	 * 
	 * @param index The index of the channel.
	 */
	void setChannelAcquisitionData(int index)
	{
		if (channel.getIndex() != index) return;
		if (!init) {
			init = true;
			resetBoxes();
			removeAll();
	    	fieldsGeneral.clear();
	    	fieldsDetector.clear();
	    	fieldsLight.clear();
			transformGeneralSource(EditorUtil.transformChannelData(channel));
			ChannelAcquisitionData data = model.getChannelAcquisitionData(
	    			channel.getIndex());
			transformDetectorSource(EditorUtil.transformDetector(data));
			Map<String, Object> details = EditorUtil.transformLightSource(null, 
					data);
			String kind = (String) details.get(EditorUtil.LIGHT_TYPE);
			details.remove(EditorUtil.LIGHT_TYPE);
			transformLightSource(kind, details);
			buildGUI();
		}
	}

	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		boolean b = parent.hasDataToSave(fieldsGeneral);
		if (b) return true;
		b = parent.hasDataToSave(fieldsDetector);
		if (b) return true;
		b = parent.hasDataToSave(fieldsLight);
		if (b) return true;
		return false;
	}

	/**
	 * Prepares the data to save.
	 * 
	 * @return See above.
	 */
	List<Object> prepareDataToSave()
	{
		List<Object> data = new ArrayList<Object>();
		if (!hasDataToSave()) return data;
		String key;
		AcquisitionComponent comp;
		Object value;
		EnumerationObject enumObject;
		Number number;
		Iterator<String> i; 
		if (channel.isDirty()) {
			i = fieldsGeneral.keySet().iterator();
			while (i.hasNext()) {
				key = i.next();
				comp = fieldsGeneral.get(key);
				if (comp.isDirty()) {
					value = comp.getAreaValue();
					if (EditorUtil.NAME.equals(key)) {
						channel.setName((String) value);
					} else if (EditorUtil.PIN_HOLE_SIZE.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Float.class);
						if (number != null)
							channel.setPinholeSize((Float) number);
					} else if (EditorUtil.ND_FILTER.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Float.class);
						if (number != null)
							channel.setNDFilter((Float) number);
					} else if (EditorUtil.POCKEL_CELL.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Integer.class);
						if (number != null)
							channel.setPockelCell((Integer) value);
					} else if (EditorUtil.EM_WAVE.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Integer.class);
						if (number != null)
							channel.setEmissionWavelength((Integer) number);
					} else if (EditorUtil.EX_WAVE.equals(key)) {
						number = UIUtilities.extractNumber((String) value, 
								Integer.class);
						if (number != null)
							channel.setExcitationWavelength((Integer) number);
					} else if (EditorUtil.ILLUMINATION.equals(key)) {
						enumObject = (EnumerationObject) value;
						if (enumObject.getObject() instanceof Illumination)
							channel.setIllumination(
								(Illumination) enumObject.getObject());
					} else if (EditorUtil.MODE.equals(key)) {
						enumObject = (EnumerationObject) value;
						if (enumObject.getObject() instanceof AcquisitionMode)
							channel.setMode(
								(AcquisitionMode) enumObject.getObject());
					} else if (EditorUtil.CONTRAST_METHOD.equals(key)) {
						enumObject = (EnumerationObject) value;
						if (enumObject.getObject() instanceof ContrastMethod)
							channel.setContrastMethod(
								(ContrastMethod) enumObject.getObject());
					}
				}
			}
			data.add(channel);
		}
		
		boolean added = false;
		ChannelAcquisitionData metadata = model.getChannelAcquisitionData(
    			channel.getIndex());
		i = fieldsDetector.keySet().iterator();
		
		while (i.hasNext()) {
			key = i.next();
			comp = fieldsDetector.get(key);
			if (comp.isDirty()) {
				value = comp.getAreaValue();
				if (EditorUtil.MODEL.equals(key)) {
					metadata.setDetectorModel((String) value);
				} else if (EditorUtil.MANUFACTURER.equals(key)) {
					metadata.setDetectorManufacturer((String) value);
				} else if (EditorUtil.SERIAL_NUMBER.equals(key)) {
					metadata.setDetectorSerialNumber((String) value);
				} else if (EditorUtil.GAIN.equals(key)) {
					//metadata.setPockelCell((Integer) value);
				} else if (EditorUtil.VOLTAGE.equals(key)) {
					//metadata.setEmissionWavelength((Integer) value);
				} else if (EditorUtil.OFFSET.equals(key)) {
					//channel.setExcitationWavelength((Integer) value);
				} else if (EditorUtil.READ_OUT_RATE.equals(key)) {
					//	channel.setExcitationWavelength((Integer) value);
				} else if (EditorUtil.ZOOM.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null)
						metadata.setDetectorZoom((Float) number);
				} else if (EditorUtil.AMPLIFICATION.equals(key)) {
					number = UIUtilities.extractNumber((String) value, 
							Float.class);
					if (number != null)
						metadata.setDetectorAmplificationGain((Float) number);
				}
			}
			added = true;
			data.add(metadata);
		}
		return data;
	}

}
