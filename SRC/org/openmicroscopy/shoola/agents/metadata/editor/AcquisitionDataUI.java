/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AcquisitionDataUI 
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
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ChannelData;

/** 
 * Component displaying the acquisition information.
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
class AcquisitionDataUI 
	extends JPanel
	implements PropertyChangeListener
{

	/** Text to show the unset fields. */
	static final String					SHOW_UNSET = "Show unset fields";
	
	/** Text to hide the unset fields. */
	static final String					HIDE_UNSET = "Hide unset fields";
	
	/** The default text of the channel. */
	private static final String			DEFAULT_CHANNEL_TEXT = "Channel ";
	
	/** Reference to the controller. */
	private EditorControl				controller;
	
	/** Reference to the Model. */
	private EditorModel					model;
		
	/** Reference to the Model. */
	private EditorUI					view;
	
	/** The component hosting the image acquisition data. */
	private ImageAcquisitionComponent	imageAcquisition;
	
	/** The collection of channel acquisition data. */
	private Map<JXTaskPane, ChannelData> channelAcquisitionPanes;
	
	/** The component hosting the image info. */
	private JXTaskPane 					imagePane;
	
	/** Collection of components hosting the channel. */
	private List<ChannelAcquisitionComponent> channelComps;
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		channelComps = new ArrayList<ChannelAcquisitionComponent>();
		channelAcquisitionPanes = new HashMap<JXTaskPane, ChannelData>();
		imageAcquisition = new ImageAcquisitionComponent(this, model);
		imageAcquisition.addPropertyChangeListener(this);
		imagePane = EditorUtil.createTaskPane("Image");
		imagePane.add(imageAcquisition);
		imagePane.addPropertyChangeListener(
				UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		removeAll();
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BorderLayout(0, 0));
		JPanel container = new JPanel();
		container.setBackground(UIUtilities.BACKGROUND_COLOR);
		container.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 2, 2, 0);
        
        c.weightx = 1.0;
        container.add(imagePane, c);
        c.gridy = 1;
        Iterator<JXTaskPane> i = channelAcquisitionPanes.keySet().iterator();
        while (i.hasNext()) {
            ++c.gridy;
            container.add(i.next(), c);  
        }
        add(container, BorderLayout.NORTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view			Reference to the View. Mustn't be <code>null</code>.
	 * @param model			Reference to the Model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the Control. 
	 * 						Mustn't be <code>null</code>.
	 */
	AcquisitionDataUI(EditorUI view, EditorModel model, 
				EditorControl controller)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.model = model;
		this.controller = controller;
		this.view = view;
		initComponents();
	}
	
	/**
	 * Formats the button used to hide or show the unset fields
	 * for acquisition metadata.
	 * 
	 * @return See above.
	 */
	JButton formatUnsetFieldsControl()
	{
		JButton button = new JButton(AcquisitionDataUI.SHOW_UNSET);
		Font font = button.getFont();
		int sizeLabel = font.getSize()-2;
    	UIUtilities.unifiedButtonLookAndFeel(button);
    	button.setFont(font.deriveFont(Font.ITALIC, sizeLabel));
    	button.setText(AcquisitionDataUI.SHOW_UNSET);
    	button.setBackground(UIUtilities.BACKGROUND_COLOR);
    	button.setForeground(UIUtilities.HYPERLINK_COLOR);
    	return button;
	}
	
	/** 
	 * Lays out the passed component.
	 * 
	 * @param pane 		The main component.
	 * @param button	The button to show or hide the unset fields.
	 * @param fields	The fields to lay out.
	 * @param shown		Pass <code>true</code> to show the unset fields,
	 * 					<code>false</code> to hide them.
	 */
	void layoutFields(JPanel pane, JButton button, 
			Map<String, AcquisitionComponent> fields, boolean shown)
	{
		pane.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
        AcquisitionComponent comp;
        String key;
		Iterator i = fields.keySet().iterator();
        while (i.hasNext()) {
            c.gridx = 0;
            key = (String) i.next();
            comp = fields.get(key);
            if (comp.isSetField() || shown) {
            	 ++c.gridy;
            	 c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
                 c.fill = GridBagConstraints.NONE;      //reset to default
                 c.weightx = 0.0;  
                 pane.add(comp.getLabel(), c);
                 c.gridx++;
                 pane.add(Box.createHorizontalStrut(5), c); 
                 c.gridx++;
                 c.gridwidth = GridBagConstraints.REMAINDER;     //end row
                 c.fill = GridBagConstraints.HORIZONTAL;
                 c.weightx = 1.0;
                 pane.add(comp.getArea(), c);  
            } 
        }
        ++c.gridy;
        c.gridx = 0;
        //c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        //c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        if (button != null) pane.add(button, c);
	}
	
	/**
	 * Formats the manufacturer. 
	 * 
	 * @param details 	The value to format.
	 * @param sizeLabel The size of the label.
	 * @param fields	The fields to keep track of.
	 * @return See above
	 */
	JComponent formatManufacturer(Map<String, Object> details, int sizeLabel, 
			 Map<String, JComponent> fields)
	{
		JLabel l = UIUtilities.setTextFont(
				AnnotationDataUI.MANUFACTURER_DETAILS, Font.ITALIC, sizeLabel);
    	l.setBackground(UIUtilities.BACKGROUND_COLOR);
    	l.setForeground(UIUtilities.HYPERLINK_COLOR);
    	l.setToolTipText(AnnotationDataUI.MANUFACTURER_TOOLTIP);
    	//Format details
    	JPanel content = new JPanel();
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
            area = UIUtilities.createComponent(OMETextArea.class, null);
            if (value == null || value.equals(""))
             	value = AnnotationUI.DEFAULT_TEXT;
            ((OMETextArea) area).setText((String) value);
       	 	((OMETextArea) area).setEditedColor(UIUtilities.EDITED_COLOR);
            
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
        l.addMouseListener(controller.createManufacturerAction(content));
    	return l;
	}
	
	/** 
	 * Sets the data when data are loaded, builds the UI.
	 */
	void setChannelData()
	{
		List channels = model.getChannelData();
		channelAcquisitionPanes.clear();
		if (channels != null) {
			ChannelData channel;
			Iterator i = channels.iterator();
			ChannelAcquisitionComponent comp;
			JXTaskPane p;
			while (i.hasNext()) {
				channel = (ChannelData) i.next();
				comp = new ChannelAcquisitionComponent(this, model, channel);
				p = EditorUtil.createTaskPane(DEFAULT_CHANNEL_TEXT+
						channel.getEmissionWavelength());
				p.add(comp);
				p.addPropertyChangeListener(
						UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
				channelAcquisitionPanes.put(p, channel);
				channelComps.add(comp);
			}
		}
		buildGUI();
	}
	
	/** Sets the metadata. */
	void setImageAcquisitionData()
	{
		imageAcquisition.setImageAcquisitionData();
	}
	
	/**
	 * Displays the acquisition data for the passed channel.
	 * 
	 * @param index The index of the channel.
	 */
	void setChannelAcquisitionData(int index)
	{
		Iterator<ChannelAcquisitionComponent> i = channelComps.iterator();
		ChannelAcquisitionComponent comp;
		while (i.hasNext()) {
			comp = i.next();
			comp.setChannelAcquisitionData(index);
		}
	}
	
	/**
	 * Loads the acquisition metadata.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (!UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE.equals(name)) return;
		Object src = evt.getSource();
		if (src == imagePane) {
			controller.loadImageAcquisitionData();
		} else {
			ChannelData channel = channelAcquisitionPanes.get(src);
			controller.loadChannelAcquisitionData(channel);
		}
	}

	
	
}
