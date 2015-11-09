/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTaskPane;

import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.JLabelButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ChannelData;
import omero.gateway.model.FileAnnotationData;

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
	
	/** Indicates if a boolean has been set to <code>true</code>. */
	static final String 				BOOLEAN_YES = "Yes";
	
	/** Indicates if a boolean has been set to <code>false</code>. */
	static final String 				BOOLEAN_NO = "No";
	
	/** The collection of enumerations supported corresponding to unset value.*/
	static final List<String>			UNSET_ENUM;
	
	/** The default text of the channel. */
	private static final String			DEFAULT_CHANNEL_TEXT = "";//"Channel ";
	
	static {
		UNSET_ENUM = new ArrayList<String>();
		UNSET_ENUM.add("Other");
		UNSET_ENUM.add("Unknown");
	}
	
	/** Reference to the controller. */
	private EditorControl						controller;
	
	/** Reference to the Model. */
	private EditorModel							model;
		
	/** Reference to the Model. */
	private EditorUI							view;
	
	/** The component hosting the image acquisition data. */
	private ImageAcquisitionComponent			imageAcquisition;
	
	/** 
	 * The component hosting information about the instrument used to capture
	 * the image.
	 */
	private InstrumentComponent					instrument;
	
	/** The collection of channel acquisition data. */
	private Map<JXTaskPane, ChannelData> 		channelAcquisitionPanes;
	
	/** The component hosting the image info. */
	private JXTaskPane 							imagePane;
	
	/** The component hosting the instrument info. */
	private JXTaskPane 							instrumentPane;
	
	/** The component hosting the original metadata. */
	private JXTaskPane							originalMetadataPane;
	
	/** Collection of components hosting the channel. */
	private List<ChannelAcquisitionComponent> 	channelComps;
	
	/** The UI component hosting the <code>JXTaskPane</code>s. */
	private JPanel								container;
	
	/** The constraints used to lay out the components in the container. */
	private GridBagConstraints					constraints;
	
	/** Component displaying the original metadata. */
	private OriginalMetadataComponent			originalMetadata;
	
	/** The component hosting the companion files. */
	private JXTaskPane 							companionFilesPane;
	
	/** Flag indicating to build the UI once. */
	private boolean 							init;

	/** Components displaying the companion files. */
	private JPanel 								docPane;
	
	/** Initializes the UI components. */
	private void initComponents()
	{  
		container = new JPanel();
		channelComps = new ArrayList<ChannelAcquisitionComponent>();
		channelAcquisitionPanes = new LinkedHashMap<JXTaskPane, ChannelData>();
		imageAcquisition = new ImageAcquisitionComponent(this, model);
		imageAcquisition.addPropertyChangeListener(this);
		imagePane = EditorUtil.createTaskPane("Image");
		imagePane.add(imageAcquisition);
		imagePane.addPropertyChangeListener(
				UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
		instrument = new InstrumentComponent(this, model);
		instrumentPane = EditorUtil.createTaskPane("Microscope");
		instrumentPane.add(instrument);
		instrumentPane.addPropertyChangeListener(
				UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
		originalMetadataPane = EditorUtil.createTaskPane("Original Metadata");
		originalMetadataPane.addPropertyChangeListener(
				UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
		originalMetadata = new OriginalMetadataComponent(model);
		originalMetadataPane.add(originalMetadata);
		companionFilesPane = EditorUtil.createTaskPane("Companion Files");
		docPane = new JPanel();
		docPane.setLayout(new BoxLayout(docPane, BoxLayout.Y_AXIS));
		docPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		companionFilesPane.add(docPane);
	}
	
	/** Builds and lays out the components. */
	private void buildGUI()
	{
		container.setBackground(UIUtilities.BACKGROUND_COLOR);
		container.setLayout(new GridBagLayout());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BorderLayout(0, 0));
		constraints = new GridBagConstraints();
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 2, 2, 0);
		constraints.weightx = 1.0;
		container.add(companionFilesPane, constraints);
		constraints.gridy++;
		container.add(originalMetadataPane, constraints);
        constraints.gridy++;
        container.add(instrumentPane, constraints);
        constraints.gridy++;
        container.add(imagePane, constraints);
        add(container, BorderLayout.NORTH);
	}
	
	/** Lays out the channels. */
	private void layoutUI()
	{
		if (!init) {
			buildGUI();
			init = true;
		}
		int n = container.getComponentCount();
		if (n == 0) return;
		Component[] comps = container.getComponents();
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] != imagePane && comps[i] != instrumentPane &&
					comps[i] != originalMetadataPane && 
					comps[i] != companionFilesPane)
				container.remove(comps[i]);
		}
		constraints.gridy = 4;
        Iterator<JXTaskPane> i = channelAcquisitionPanes.keySet().iterator();
        while (i.hasNext()) {
            ++constraints.gridy;
            container.add(i.next(), constraints);  
        }
        revalidate();
        repaint();
        
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
		init = false;
		//buildGUI();
	}
	
	/**
	 * Attaches listener to each item of the map.
	 * 
	 * @param map The map to handle.
	 */
	void attachListener(Map<String, DataComponent> map)
	{
		Iterator i = map.entrySet().iterator();
		Entry entry;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			((DataComponent) entry.getValue()).attachListener(controller);
		}
	}
	
	/**
	 * Returns <code>true</code> if one of the components has been modified,
	 * <code>false</code> otherwise.
	 * 
	 * @param map The map to handle.
	 * @return See above.
	 */
	boolean hasDataToSave(Map<String, DataComponent> map)
	{
		if (map == null) return false;
		Iterator i = map.entrySet().iterator();
		DataComponent comp;
		Entry entry;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			comp = (DataComponent) entry.getValue();
			if (comp.isDirty()) return true;
		}
		return false;
	}
	
	/**
	 * Formats the button used to hide or show the unset fields
	 * for acquisition metadata.
	 * 
	 * @return See above.
	 */
	JLabelButton formatUnsetFieldsControl()
	{
		JLabelButton button = new JLabelButton(AcquisitionDataUI.SHOW_UNSET);
		Font font = button.getFont();
		int sizeLabel = font.getSize()-2;
    	//UIUtilities.unifiedButtonLookAndFeel(button);
    	button.setFont(font.deriveFont(Font.ITALIC, sizeLabel));
    	button.setBackground(UIUtilities.BACKGROUND_COLOR);
    	button.setForeground(UIUtilities.HYPERLINK_COLOR);
    	return button;
	}
	
	/**
	 * Formats the component.
	 * 
	 * @param comp  The component to format.
	 * @param title The title to add to the border.
	 */
	void format(JComponent comp, String title)
	{
		if (comp == null) return;
		if (title == null) title = "";
		comp.setBorder(BorderFactory.createTitledBorder(title));
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		comp.setLayout(new GridBagLayout());
	}
	
	/**
	 * Replaces the combox by a label, due to a painting problem since we
	 * switched to openGL.
	 * 
	 * @param box The box to change.
	 * @return See above.
	 */
	JComponent replaceCombobox(JComboBox box)
	{
		JLabel l = new JLabel();
		l.setBackground(UIUtilities.BACKGROUND_COLOR);
		if (box != null) {
			l.setFont(box.getFont());
			l.setText(box.getSelectedItem().toString());
		}
		return l;
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
	void layoutFields(JPanel pane, JComponent button, 
			Map<String, DataComponent> fields, boolean shown)
	{
		pane.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		DataComponent comp;
        Set set = fields.entrySet();
        Entry entry;
        
		Iterator i = set.iterator();
		c.gridy = 0;
        while (i.hasNext()) {
            c.gridx = 0;
            entry = (Entry) i.next();
            comp = (DataComponent) entry.getValue();
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
        if (c.gridy != 0) ++c.gridy;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        if (button != null) pane.add(button, c);
	}
	
	/**
	 * Returns the boolean value corresponding to the passed string
	 * or <code>null</code>.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	Boolean convertToBoolean(String value)
	{
		if (value == null) return null;
		if (BOOLEAN_YES.equals(value)) return Boolean.valueOf(true);
		else if (BOOLEAN_NO.equals(value)) return Boolean.valueOf(false);
		return null;
	}
	
	/** Sets the data when data are loaded, builds the UI. */
	void setChannelData()
	{
		Map channels = model.getChannelData();
		channelAcquisitionPanes.clear();
		channelComps.clear();
		if (channels != null) {
			ChannelData channel;
			Iterator i = channels.entrySet().iterator();
			ChannelAcquisitionComponent comp;
			JXTaskPane p;
			Entry entry;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				channel = (ChannelData) entry.getKey();
				comp = new ChannelAcquisitionComponent(this, model, channel);
				comp.setChannelColor((Color) entry.getValue()); 
				p = EditorUtil.createTaskPane(DEFAULT_CHANNEL_TEXT+
						channel.getChannelLabeling());
				p.setIcon(comp.getIcon());
				p.add(comp);
				p.addPropertyChangeListener(
						UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
				channelAcquisitionPanes.put(p, channel);
				channelComps.add(comp);
			}
		}
		layoutUI();
	}
	
	/**
	 * Indicates that the color of the passed channel has changed.
	 * 
	 * @param index The index of the channel.
	 */
	void onChannelColorChanged(int index)
	{
		ChannelAcquisitionComponent comp;
		Iterator<ChannelAcquisitionComponent> i = channelComps.iterator();
		ChannelData data;
		while (i.hasNext()) {
			comp = i.next();
			if (comp.getChannelIndex() == index) {
				comp.setChannelColor(model.getChannelColor(index));
				comp.repaint();
				break;
			}
		}
	}
	
	/** Sets the metadata. */
	void setImageAcquisitionData()
	{
		imageAcquisition.setImageAcquisitionData();
		revalidate();
	}
	
	/** Sets the instrument and its components. */
	void setInstrumentData() { 
		instrument.setInstrumentData();
		revalidate();
	}
	
	/**
	 * Displays the acquisition data for the passed channel.
	 * 
	 * @param index The index of the channel.
	 */
	void setChannelAcquisitionData(int index)
	{
		Iterator<ChannelAcquisitionComponent> i = channelComps.iterator();
		while (i.hasNext()) 
			i.next().setChannelAcquisitionData(index);
		revalidate();
	}
	
	/**
	 * Sets the plane info for the specified channel.
	 * 
	 * @param index  The index of the channel.
	 */
	void setPlaneInfo(int index)
	{
		Iterator<ChannelAcquisitionComponent> i = channelComps.iterator();
		while (i.hasNext()) 
			i.next().setPlaneInfo(index);
	}

	/** 
	 * Updates display when the new root node is set. 
	 * Loads the acquisition data if the passed parameter is <code>true</code>
	 * and the {@link #imagePane} is expanded.
	 * 
	 * @param load 	Pass <code>true</code> to load the image data,
	 * 				<code>false</code> otherwise.
	 */
	void setRootObject(boolean load)
	{
		instrument.setRootObject();
		imageAcquisition.setRootObject();
		channelAcquisitionPanes.clear();
		originalMetadata.clear();
		layoutUI();
		repaint();
		if (!imagePane.isCollapsed() && load)
			controller.loadImageAcquisitionData();
		if (!instrumentPane.isCollapsed() && load)
			controller.loadInstrumentData();
		originalMetadataPane.setCollapsed(true);
		if (MetadataViewerAgent.isBinaryAvailable()) {
			originalMetadataPane.setVisible(true);
		} else originalMetadataPane.setVisible(false);
	}
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave()
	{
		return false;
		//For Beta4.0.0,metadata not editable.
		/*
		boolean b = imageAcquisition.hasDataToSave();
		if (b) return b;
		Iterator<ChannelAcquisitionComponent> i = channelComps.iterator();
		while (i.hasNext()) {
			b = i.next().hasDataToSave();
			if (b) return true;
		}
		return false;
		*/
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
		Object object = imageAcquisition.prepareDataToSave();
		if (object != null) data.add(object);
		Iterator<ChannelAcquisitionComponent> i = channelComps.iterator();
		List<Object> objects;
		while (i.hasNext()) {
			objects = i.next().prepareDataToSave();
			if (objects.size() > 0)
				data.addAll(objects);
		}
		return data;
	}
	
	/**
	 * Loads the planes for the specified channel.
	 * 
	 * @param index The index of the channel.
	 */
	void loadPlaneInfo(int index)
	{
		if (model.getChannelPlaneInfo(index) == null) {
			model.firePlaneInfoLoading(index, 0);
			view.setStatus(true);
		}
	}
	
	/** Lays out the companion files if any. */
	void layoutCompanionFiles()
	{
		boolean b = model.hasOriginalMetadata() &&
			MetadataViewerAgent.isBinaryAvailable();
		originalMetadataPane.setVisible(b);
		if (b) originalMetadataPane.setCollapsed(true);
		Collection list = model.getCompanionFiles();
		if (list == null || list.size() == 0) {
			companionFilesPane.setVisible(false);
			revalidate();
			repaint();
			return;
		}
		companionFilesPane.setVisible(true);
		Iterator i = list.iterator();
		FileAnnotationData file;
		DocComponent doc;
		docPane.removeAll();
		while (i.hasNext()) {
			docPane.add(new DocComponent(i.next(), model, false, false));
		}
		revalidate();
		repaint();
	}
	
	/** Refreshes the view. */
	void refresh()
	{
		/*
		instrument.setRootObject();
		imageAcquisition.setRootObject();
		originalMetadata.clear();
		layoutUI();
		repaint();
		originalMetadataPane.setCollapsed(true);
		instrumentPane.setCollapsed(true);
		imagePane.setCollapsed(true);
		*/
	}

	/**
	 * Loads the acquisition data.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (!UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE.equals(name)) return;
		Object src = evt.getSource();
		if (src == imagePane) {
			controller.loadImageAcquisitionData();
		} else if (src == instrumentPane) {
			controller.loadInstrumentData();
		} else if (src == originalMetadataPane) {
			if (MetadataViewerAgent.isBinaryAvailable()) {
				if (model.hasOriginalMetadata() && model.getImage() != null) {
					if (!originalMetadata.isMetadataLoaded())
						model.loadOriginalMetadata(originalMetadata);
				} else originalMetadataPane.setCollapsed(true);
			}
		} else {
			ChannelData channel = channelAcquisitionPanes.get(src);
			controller.loadChannelAcquisitionData(channel);
		}
	}

}
