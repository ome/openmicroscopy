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
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.Mapper;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
	private EditorModel	model;
	
	/** The component hosting the various immersion values. */
	private JComboBox 	immersionBox;
	
	/** The component hosting the various coating values. */
	private JComboBox 	coatingBox;
	
	/** The component hosting the various medium values. */
	private JComboBox 	mediumBox;
	
	/** Initiliases the components. */
	private void initComponents()
	{
		immersionBox = new JComboBox(Mapper.IMMERSIONS);
		immersionBox.setBackground(UIUtilities.BACKGROUND_COLOR);
		Font f = immersionBox.getFont();
		int size = f.getSize()-2;
		immersionBox.setBorder(null);
		immersionBox.setFont(f.deriveFont(f.getStyle(), size));
		coatingBox = new JComboBox(Mapper.COATING);
		coatingBox.setBackground(UIUtilities.BACKGROUND_COLOR);
		coatingBox.setBorder(null);
		coatingBox.setFont(f.deriveFont(f.getStyle(), size));
		mediumBox = new JComboBox(Mapper.MEDIUM);
		mediumBox.setBackground(UIUtilities.BACKGROUND_COLOR);
		mediumBox.setBorder(null);
		mediumBox.setFont(f.deriveFont(f.getStyle(), size));
	}
	
	/** 
	 * Builds and lays out the stage label.
	 * 
	 * @return See above.
	 */
	private JPanel buildStageLabel()
	{
		JPanel content = new JPanel();
		content.setBorder(
				BorderFactory.createTitledBorder("Stage Label"));
		content.setBackground(UIUtilities.BACKGROUND_COLOR);
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		Map<String, String> details = EditorUtil.transformStageLabel(null);
		Iterator i = details.keySet().iterator();
        JLabel label;
        JTextArea area;
        String key, value;
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
            area = (JTextArea) UIUtilities.createComponent(
            		JTextArea.class, null);
            if (value == null || value.equals(""))
             	value = AnnotationUI.DEFAULT_TEXT;
            	area.setText(value);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
		return content;
	}
	
	/** 
	 * Builds and lays out the setting relative to the objective.
	 * 
	 * @return See above.
	 */
	private JPanel buildObjectiveSetting()
	{
		JPanel content = new JPanel();
		content.setBorder(
				BorderFactory.createTitledBorder("Objective's Settings"));
		content.setBackground(UIUtilities.BACKGROUND_COLOR);
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		Map<String, String> 
			details = EditorUtil.transformObjectiveSettings(null);
		Iterator i = details.keySet().iterator();
        JLabel label;
        JComponent area;
        String key, value;
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
            if (key.equals(EditorUtil.MEDIUM)) {
            	area = mediumBox;
            } else {
            	 area = UIUtilities.createComponent(JTextArea.class, null);
                 ((JTextArea) area).setEditable(false);
                 if (value == null || value.equals(""))
                 	value = AnnotationUI.DEFAULT_TEXT;
                 ((JTextArea) area).setText(value);
            }
           
            label.setLabelFor(area);
            c.gridx = 1;
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
	 * @return See above.
	 */
	private JPanel buildObjective()
	{
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createTitledBorder("Objective"));
		content.setBackground(UIUtilities.BACKGROUND_COLOR);
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		Map<String, String> details = EditorUtil.transformObjective(null);
		Iterator i = details.keySet().iterator();
        JLabel label;
        JComponent area;
        String key, value;
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
            if (key.equals(EditorUtil.IMMERSION)) {
            	area = immersionBox;	
            } else if (key.equals(EditorUtil.COATING)) {
            	area = coatingBox;
            } else {
            	 area = UIUtilities.createComponent(JTextArea.class, null);
                 ((JTextArea) area).setEditable(false);
                 if (value == null || value.equals(""))
                 	value = AnnotationUI.DEFAULT_TEXT;
                 ((JTextArea) area).setText(value);
            }
           
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
		return content;
	}
	
	/** 
	 * Builds and lays out the imaging environment.
	 * 
	 * @return See above.
	 */
	public JPanel buildImagingEnvironment()
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
		Map<String, String> 
			details = EditorUtil.transformImageEnvironment(null);
		Iterator i = details.keySet().iterator();
        JLabel label;
        JTextArea area;
        String key, value;
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
            area = (JTextArea) UIUtilities.createComponent(JTextArea.class, 
            		null);
            if (value == null || value.equals(""))
             	value = AnnotationUI.DEFAULT_TEXT;

            area.setEditable(false);
            area.setText(value);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
		return content;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(buildObjective());
		add(buildObjectiveSetting());
		add(buildImagingEnvironment());
		add(buildStageLabel());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model	Reference to the Model. Mustn't be <code>null</code>.
	 */
	ImageAcquisitionComponent(EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		initComponents();
		buildGUI();
	}
	
}
