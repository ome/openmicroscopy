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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
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

	/** Indicates if a boolean has been set to <code>true</code>. */
	private static final String 	BOOLEAN_YES = "Yes";
	
	/** Indicates if a boolean has been set to <code>false</code>. */
	private static final String 	BOOLEAN_NO = "No";
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI		parent;
	
	/** The channel data. */
	private ChannelData 			channel;
	
	/** The component displaying the illumination's options. */
	private OMEComboBox				illuminationBox;
	
	/** The component displaying the contrast Method options. */
	private OMEComboBox				contrastMethodBox;
	
	/** The component displaying the contrast Method options. */
	private OMEComboBox				modeBox;
	
	/** The component displaying the binning options. */
	private OMEComboBox				binningBox;
	
	/** The component displaying the detector options. */
	private OMEComboBox				detectorBox;
	
	/** The component displaying the laser options. */
	private OMEComboBox				laserMediumBox;
	
	/** The component displaying the types of arc. */
	private OMEComboBox				arcTypeBox;
	
	/** The component displaying the types of filament. */
	private OMEComboBox				filamentTypeBox;
	
	/** The component displaying the types of laser. */
	private OMEComboBox				laserTypeBox;
	
	/** The component displaying the tuneable option for a  laser. */
	private OMEComboBox				laserTuneableBox;
	
	/** The component displaying the pockel cell option for a  laser. */
	private OMEComboBox				laserPockelCellBox;
	
	/** The fields displaying the metadata. */
	private Map<String, JComponent> fieldsGeneral;
	
	/** The fields displaying the metadata. */
	private Map<String, JComponent> fieldsDetector;
	
	/** The fields displaying the metadata. */
	private Map<String, JComponent> fieldsLight;
	
	/** Flag indicating if the components have been initialized. */
	private boolean					init;
	
	/** Reference to the Model. */
	private EditorModel				model;
	
	/** Initializes the components */
	private void initComponents()
	{
		init = true;
		List l = model.getChannelEnumerations(Editor.ILLUMINATION_TYPE);
		l.add(new EnumerationObject(AnnotationDataUI.NO_SET_TEXT));
		illuminationBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.CONTRAST_METHOD);
		l.add(new EnumerationObject(AnnotationDataUI.NO_SET_TEXT));
		contrastMethodBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.MODE);
		l.add(new EnumerationObject(AnnotationDataUI.NO_SET_TEXT));
		modeBox = EditorUtil.createComboBox(l);
		l = model.getChannelEnumerations(Editor.BINNING);
		l.add(new EnumerationObject(AnnotationDataUI.NO_SET_TEXT));
		binningBox = EditorUtil.createComboBox(l);
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
		String[] values = new String[3];
		values[0] = BOOLEAN_YES;
		values[1] = BOOLEAN_NO;
		values[2] = AnnotationDataUI.NO_SET_TEXT;
		laserTuneableBox = EditorUtil.createComboBox(values);
		laserPockelCellBox = EditorUtil.createComboBox(values);
		fieldsGeneral = new HashMap<String, JComponent>();
		fieldsDetector = new HashMap<String, JComponent>();
		fieldsLight = new HashMap<String, JComponent>();
	}
	
	/**
	 * Builds and lays out the details of the source of light
	 * 
	 * @param details The data to lay out.
	 * @return See above.
	 */
	private JPanel buildLightSource(Map<String, Object> details)
	{
		JPanel content = new JPanel();
		String kind = (String) details.get(EditorUtil.LIGHT_TYPE);
		details.remove(EditorUtil.LIGHT_TYPE);
		String title = "Light Source";
		if (ChannelAcquisitionData.LASER.equals(kind))
			title = "Laser";
		else if (ChannelAcquisitionData.ARC.equals(kind))
			title = "Arc";
		else if (ChannelAcquisitionData.FILAMENT.equals(kind))
			title = "Filament";
		
		content.setBorder(BorderFactory.createTitledBorder(title));
		content.setBackground(UIUtilities.BACKGROUND_COLOR);
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 2, 2, 0);
        JLabel label;
        JComponent area;
        String key;
        Object value;
        label = new JLabel();
        Font font = label.getFont();
        int sizeLabel = font.getSize()-2;
        Object selected;
        
        
        Map<String, Object> m = new LinkedHashMap<String, Object>(3);
        m.put(EditorUtil.MANUFACTURER, details.get(EditorUtil.MANUFACTURER));
        details.remove(EditorUtil.MANUFACTURER);
        m.put(EditorUtil.MODEL, details.get(EditorUtil.MODEL));
        details.remove(EditorUtil.MODEL);
        m.put(EditorUtil.SERIAL_NUMBER, details.get(EditorUtil.SERIAL_NUMBER));
        details.remove(EditorUtil.SERIAL_NUMBER);
        
        area = parent.formatManufacturer(m, sizeLabel, fieldsLight);
    	
    	c.gridx = 0;
    	label = UIUtilities.setTextFont(AnnotationDataUI.MANUFACTURER, 
    			Font.BOLD, sizeLabel);
        label.setBackground(UIUtilities.BACKGROUND_COLOR);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        label.setLabelFor(area);
        c.gridx++;
        content.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(area, c);
        
        c.gridy = 1;
		Iterator i = details.keySet().iterator();

        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = details.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            if (ChannelAcquisitionData.LASER.equals(kind)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.LASER_TYPE, (String) value);
            	if (selected != null) laserTypeBox.setSelectedItem(selected);
            	laserTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = laserTypeBox;
            	if (key.equals(EditorUtil.MEDIUM)) {
                	selected = model.getChannelEnumerationSelected(
                			Editor.LASER_MEDIUM, 
                			(String) value);
                	if (selected != null) 
                		laserMediumBox.setSelectedItem(selected);
                	laserMediumBox.setEditedColor(UIUtilities.EDITED_COLOR);
                	area = laserMediumBox;
            	} else if (key.equals(EditorUtil.TUNEABLE)) { 
            		boolean b;
            		if (value != null) {
            			b = (Boolean) value;
            			if (b) laserTuneableBox.setSelectedItem(BOOLEAN_YES);
            			else laserTuneableBox.setSelectedItem(BOOLEAN_NO);
            		} else 
            			laserTuneableBox.setSelectedItem(
            					AnnotationDataUI.NO_SET_TEXT);
            	} else if (key.equals(EditorUtil.POCKEL_CELL)) {
            		boolean b;
            		if (value != null) {
            			b = (Boolean) value;
            			if (b) laserPockelCellBox.setSelectedItem(BOOLEAN_YES);
            			else laserPockelCellBox.setSelectedItem(BOOLEAN_NO);
            		} else 
            			laserPockelCellBox.setSelectedItem(
            					AnnotationDataUI.NO_SET_TEXT);
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
                	 ((OMETextArea) area).setEditedColor(
                			 UIUtilities.EDITED_COLOR);
                }
            } else if (ChannelAcquisitionData.ARC.equals(kind)) {
            	selected = model.getChannelEnumerationSelected(Editor.ARC_TYPE, 
            			(String) value);
            	if (selected != null) arcTypeBox.setSelectedItem(selected);
            	arcTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = arcTypeBox;
            } else if (ChannelAcquisitionData.FILAMENT.equals(kind)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.FILAMENT_TYPE, (String) value);
            	if (selected != null) filamentTypeBox.setSelectedItem(selected);
            	filamentTypeBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = filamentTypeBox;
            }

            label.setLabelFor(area);
            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
            fieldsDetector.put(key, area);
        }
		return content;
	}
	
	/**
	 * Builds and lays out the body displaying the detector.
	 * 
	 * @param details The data to lay out.
	 * @return See above.
	 */
    private JPanel buildDetector(Map<String, Object> details)
    {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder("Camera"));
        content.setBackground(UIUtilities.BACKGROUND_COLOR);
        content.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 2, 2, 0);
        JLabel label;
        JComponent area;
        String key;
        Object value;
        label = new JLabel();
        Font font = label.getFont();
        int sizeLabel = font.getSize()-2;
        Object selected;
        
        
        Map<String, Object> m = new LinkedHashMap<String, Object>(3);
        m.put(EditorUtil.MANUFACTURER, details.get(EditorUtil.MANUFACTURER));
        details.remove(EditorUtil.MANUFACTURER);
        m.put(EditorUtil.MODEL, details.get(EditorUtil.MODEL));
        details.remove(EditorUtil.MODEL);
        m.put(EditorUtil.SERIAL_NUMBER, details.get(EditorUtil.SERIAL_NUMBER));
        details.remove(EditorUtil.SERIAL_NUMBER);
        
        area = parent.formatManufacturer(m, sizeLabel, fieldsDetector);
    	
    	c.gridx = 0;
    	label = UIUtilities.setTextFont(AnnotationDataUI.MANUFACTURER, 
    			Font.BOLD, sizeLabel);
        label.setBackground(UIUtilities.BACKGROUND_COLOR);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(label, c);
        label.setLabelFor(area);
        c.gridx++;
        content.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(area, c);
        
        c.gridy = 1;
		Iterator i = details.keySet().iterator();

        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = details.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            if (key.equals(EditorUtil.BINNING)) {
            	selected = model.getChannelEnumerationSelected(Editor.BINNING, 
            			(String) value);
            	if (selected != null) binningBox.setSelectedItem(selected);
            	else binningBox.setSelectedIndex(binningBox.getItemCount()-1);
            	binningBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = binningBox;
            } else if (key.equals(EditorUtil.TYPE)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.DETECTOR_TYPE, 
            			(String) value);
            	if (selected != null) detectorBox.setSelectedItem(selected);
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
            
            label.setLabelFor(area);
            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
            fieldsDetector.put(key, area);
        }
        return content;
    }
    
	 /**
     * Builds and lays out the body displaying the channel info.
     * 
     * @param details The data to lay out.
     * @return See above.
     */
    private JPanel buildChannelInfo(Map<String, Object> details)
    {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createTitledBorder("Info"));
        content.setBackground(UIUtilities.BACKGROUND_COLOR);
        content.setLayout(new GridBagLayout());
        //content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 2, 2, 0);
        Iterator i = details.keySet().iterator();
        JLabel label;
        JComponent area;
        String key;
        Object value;
        label = new JLabel();
        Font font = label.getFont();
        int sizeLabel = font.getSize()-2;
        Object selected;
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = details.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            
            if (key.equals(EditorUtil.ILLUMINATION)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.ILLUMINATION_TYPE, 
            			(String) value);
            	if (selected != null) illuminationBox.setSelectedItem(selected);
            	else illuminationBox.setSelectedIndex(
            			illuminationBox.getItemCount()-1);
            	illuminationBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = illuminationBox;
            } else if (key.equals(EditorUtil.CONTRAST_METHOD)) {
            	selected = model.getChannelEnumerationSelected(
            			Editor.ILLUMINATION_TYPE, 
            			(String) value);
            	if (selected != null) 
            		contrastMethodBox.setSelectedItem(selected);
            	else contrastMethodBox.setSelectedIndex(
            			contrastMethodBox.getItemCount()-1);
            	contrastMethodBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = contrastMethodBox;
            } else if (key.equals(EditorUtil.MODE)) {
            	selected = model.getChannelEnumerationSelected(Editor.MODE, 
            			(String) value);
            	if (selected != null) modeBox.setSelectedItem(selected);
            	else modeBox.setSelectedIndex(modeBox.getItemCount()-1);
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
                	if (value == null || value.equals(""))
                    	value = AnnotationUI.DEFAULT_TEXT;
                	((OMETextArea) area).setEditable(false);
                	((OMETextArea) area).setText((String) value);
                	((OMETextArea) area).setEditedColor(
                			UIUtilities.EDITED_COLOR);
            	}
            }
            
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
            fieldsGeneral.put(key, area);
        }
        return content;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	fieldsGeneral.clear();
    	fieldsDetector.clear();
    	ChannelAcquisitionData data = model.getChannelAcquisitionData(
    			channel.getIndex());
    	setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	add(buildChannelInfo(EditorUtil.transformChannelData(channel)));
    	add(buildDetector(EditorUtil.transformDectector(data)));
    	//add(buildLightSource(EditorUtil.transformLightSource(data)));
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
	}
	
	/**
	 * Displays the acquisition data for the passed channel.
	 * 
	 * @param index The index of the channel.
	 */
	void setChannelAcquisitionData(int index)
	{
		if (channel.getIndex() != index) return;
		if (!init) initComponents();
		removeAll();
		buildGUI();
	}
}
