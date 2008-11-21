/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ImageAcquisitionComponent 
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
import pojos.ImageAcquisitionData;

/** 
 * Displays the acquisition metadata related to the image itself.
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
class ImageAcquisitionComponent 
	extends JPanel
{

	/** Reference to the Model. */
	private EditorModel				model;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI		parent;
	
	/** The component hosting the various immersion values. */
	private OMEComboBox				immersionBox;
	
	/** The component hosting the various coating values. */
	private OMEComboBox 			coatingBox;
	
	/** The component hosting the various medium values. */
	private OMEComboBox 			mediumBox;
	
	/** The fields displaying the metadata. */
	private Map<String, JComponent> fields;
	
	/** Flag indicating if the components have been initialized. */
	private boolean					init;
	
	/** Initiliases the components. */
	private void initComponents()
	{
		init = true;
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		List l = model.getImageEnumerations(Editor.IMMERSION);
		immersionBox = EditorUtil.createComboBox(l);
		l = model.getImageEnumerations(Editor.CORRECTION);
		coatingBox = EditorUtil.createComboBox(l);
		l = model.getImageEnumerations(Editor.MEDIUM);
		l.add(new EnumerationObject(AnnotationDataUI.NO_SET_TEXT));
		mediumBox = EditorUtil.createComboBox(l);
		fields = new HashMap<String, JComponent>();
	}
	
	/** 
	 * Builds and lays out the stage label.
	 * 
	 * @param details The data to lay out.
	 * @return See above.
	 */
	private JPanel buildStageLabel(Map<String, Object> details)
	{
		JPanel content = new JPanel();
		content.setBorder(
				BorderFactory.createTitledBorder("Position"));
		content.setBackground(UIUtilities.BACKGROUND_COLOR);
		content.setLayout(new GridBagLayout());
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
            if (value instanceof String) {
            	area = UIUtilities.createComponent(OMETextArea.class, null);
                if (value == null || value.equals(""))
                 	value = AnnotationUI.DEFAULT_TEXT;
                ((OMETextArea) area).setText((String) value);
           	 	((OMETextArea) area).setEditedColor(UIUtilities.EDITED_COLOR);
            } else {
            	area = UIUtilities.createComponent(NumericalTextField.class, 
            			null);
            	if (value instanceof Double) 
            		((NumericalTextField) area).setNumberType(Double.class);
            	else if (value instanceof Float) 
        			((NumericalTextField) area).setNumberType(Float.class);
            	((NumericalTextField) area).setText(""+value);
            	((NumericalTextField) area).setEditedColor(
            			UIUtilities.EDITED_COLOR);
            	
            }
            
            label.setLabelFor(area);
            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
		return content;
	}
	
	/** 
	 * Builds and lays out the objective's data.
	 * 
	 * @param details The data to lay out.
	 * @param detailsSettings The settings of the detector.
	 * @return See above.
	 */
	private JPanel buildObjective(Map<String, Object> details, 
			Map<String, Object> detailsSettings)
	{
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createTitledBorder("Objective"));
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
        
        area = parent.formatManufacturer(m, sizeLabel, fields);
    	
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
            if (key.equals(EditorUtil.IMMERSION)) {
            	selected = model.getImageEnumerationSelected(Editor.IMMERSION, 
            			(String) value);
            	if (selected != null) immersionBox.setSelectedItem(selected);
            	immersionBox.setEditedColor(UIUtilities.EDITED_COLOR);
            	area = immersionBox;
            } else if (key.equals(EditorUtil.CORRECTION)) {
            	selected = model.getImageEnumerationSelected(Editor.CORRECTION, 
            			(String) value);
            	if (selected != null) coatingBox.setSelectedItem(selected);
            	area = coatingBox;
            	coatingBox.setEditedColor(UIUtilities.EDITED_COLOR);
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
                     ((OMETextArea) area).setText((String) value);
                     ((OMETextArea) area).setEditedColor(
                			 UIUtilities.EDITED_COLOR);
            	}
            }
           
            label.setLabelFor(area);
            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  

            fields.put(key, area);
        }
        i = detailsSettings.keySet().iterator();
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = detailsSettings.get(key);
            label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
            label.setBackground(UIUtilities.BACKGROUND_COLOR);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            if (key.equals(EditorUtil.MEDIUM)) {
            	area = mediumBox;
            } else {
            	 area = UIUtilities.createComponent(NumericalTextField.class, 
            			 null);
            	 if (value instanceof Double) 
            		 ((NumericalTextField) area).setNumberType(Double.class);
            	 else if (value instanceof Float) 
            		 ((NumericalTextField) area).setNumberType(Float.class);
                 ((NumericalTextField) area).setText(""+value);
                 ((NumericalTextField) area).setEditedColor(
                		 UIUtilities.EDITED_COLOR);
            }
           
            label.setLabelFor(area);
            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  

            fields.put(key, area);
        }
		return content;
	}
	
	/** 
	 * Builds and lays out the imaging environment.
	 * 
	 * @param details The data to lay out.
	 * @return See above.
	 */
	public JPanel buildImagingEnvironment(Map<String, Object> details)
	{
		JPanel content = new JPanel();
		content.setBorder(
				BorderFactory.createTitledBorder("Environment"));
		content.setBackground(UIUtilities.BACKGROUND_COLOR);
		content.setLayout(new GridBagLayout());
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
	
            if (value instanceof Number) {
            	 area = UIUtilities.createComponent(
            			 NumericalTextField.class, null);
            	 if (EditorUtil.HUMIDITY.equals(key) ||
            		EditorUtil.CO2_PERCENT.equals(key)) {
            		 //((NumericalTextField) area).setMinimum(0.0);
            		 //((NumericalTextField) area).setMaximum(1.0);
            	 }
            	 if (value instanceof Double) 
            		 ((NumericalTextField) area).setNumberType(Double.class);
            	 else if (value instanceof Float) 
            		 ((NumericalTextField) area).setNumberType(Float.class);
            	 ((NumericalTextField) area).setText(""+value);
            	 ((NumericalTextField) area).setEditedColor(
            			 UIUtilities.EDITED_COLOR);
            } else {
            	 area = UIUtilities.createComponent(
            			 OMETextArea.class, null);
            	 if (value == null || value.equals(""))
                  	value = AnnotationUI.DEFAULT_TEXT;
                 ((OMETextArea) area).setText((String) value);
                 ((OMETextArea) area).setEditedColor(
            			 UIUtilities.EDITED_COLOR);
            }
           
            label.setLabelFor(area);
            c.gridx++;
            content.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
            fields.put(key, area);
        }
		return content;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		fields.clear();
		ImageAcquisitionData data = model.getImageAcquisitionData();
		add(buildObjective(EditorUtil.transformObjective(data), 
				EditorUtil.transformObjectiveSettings(data)));
		add(buildImagingEnvironment(
				EditorUtil.transformImageEnvironment(data)));
		add(buildStageLabel(EditorUtil.transformStageLabel(data)));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	Reference to the Parent. Mustn't be <code>null</code>.
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 */
	ImageAcquisitionComponent(AcquisitionDataUI parent, EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (parent == null)
			throw new IllegalArgumentException("No parent.");
		this.parent = parent;
		this.model = model;
		//initComponents();
		//buildGUI();
	}
	
	/** Sets the metadata. */
	void setImageAcquisitionData()
	{
		if (!init) initComponents();
		removeAll();
		buildGUI();
	}
	
}
