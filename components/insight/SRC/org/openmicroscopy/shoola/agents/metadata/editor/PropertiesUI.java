/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.ROICountLoader;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.file.modulo.ModuloInfo;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiDataObject;

import ome.model.units.BigResult;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;

/** 
 * Displays the properties of the selected object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Scott Littlewood &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class PropertiesUI
	extends AnnotationUI
	implements DocumentListener, FocusListener, 
	PropertyChangeListener
{
    
	/** The title associated to this component. */
	static final String			TITLE = "Properties";
	
	/** The default description. */
    private static final String	DEFAULT_DESCRIPTION_TEXT = "Add Description";
    
    /** The text for the creation date. */
    private static final String CREATIONDATE_TEXT = "Creation Date: ";
    
    /** Text indicating to edit the description.*/
    private static final String EDIT_DESC_TEXT = "Edit the description";
    
    /**Text indicating to edit the channels.*/
    private static final String EDIT_CHANNEL_TEXT = "Edit channel names";
    
    /** The default height of the description.*/
    private static final int HEIGHT = 120;
    
    /** The default width of the description.*/
    private static final int WIDTH = 100;
    
    /** Maximum number of characters shown per line in the
     *  channel names component
     */
    private static final int MAX_CHANNELNAMES_LENGTH_IN_CHARS = 40;
	
	/** Button to edit the description. */
	private JToggleButton				descriptionButtonEdit;
    
    /** The description before possible modification.*/
    private String				originalDescription;

    /** The component hosting the name of the <code>DataObject</code>. */
    private JTextArea			typePane;
    
    /** The component hosting the description of the <code>DataObject</code>. */
    private OMEWikiComponent	descriptionWiki;
    
    /** The component hosting the {@link #descriptionWiki}. */
    private JPanel				descriptionPanel;
    
    /** The label displaying the parent of the node. */
    private JLabel				parentLabel;
    
    /** The label displaying the parent of the node. */
    private JLabel				gpLabel;
    
    /** The label displaying the parent of the node. */
    private JLabel				wellLabel;
    
    /** The area displaying the channels information. */
	private JLabel				channelsArea;
	
	/** Reference to the control. */
	private EditorControl		controller;
	
	/** The text associated to the data object. */
	private String				text;

	/** ScrollPane hosting the {@link #descriptionWiki} component.*/
	private JScrollPane			descriptionScrollPane;
	
	/** Button to edit the channels. */
	private JButton editChannel;
	
	/** Component displayed when editing the channels.*/
	private ChannelEditUI channelEditPane;
	
	/** Components hosting the channels' details.*/
	private JComponent channelsPane;
	
	/** The label showing the ROI count */
	private JLabel roiCountLabel;
	
	/** Builds and lays out the components displaying the channel information.*/
	private void buildChannelsPane()
	{
		editChannel.setVisible(model.canEdit());
		channelsPane = channelsArea;
	}
	
	/**
	 * Returns the <code>JXTaskPane</code> hosting the component.
	 * 
	 * @param parent The value to check.
	 * @return See above.
	 */
	private Container getComponent(Container parent)
	{
		if (parent == null) return null;
		if (parent instanceof JXTaskPane) return parent;
		return getComponent(parent.getParent());
	}
	
	/** Resets the size of the components hosting this component.*/
	private void resetComponentSize()
	{
		Container pane = getComponent(getParent());
		pane.setSize(getPreferredSize());
		pane.validate();
		pane.repaint();
	}
	
	/** Modifies the UI so the user can edit the channels.*/
	private void editChannels()
	{
		Map data = model.getChannelData();
		if (data == null || data.size() == 0) return;
		if (channelEditPane == null) {
			Object ho = model.getParentRootObject();
			if (model.getParentRootObject() instanceof WellData)
				ho = model.getGrandParentRootObject();
			channelEditPane = new ChannelEditUI(model.getChannelData(), ho);
			channelEditPane.addPropertyChangeListener(this);
		}
		channelsPane = channelEditPane;
		editChannel.setVisible(false);
		removeAll();
		buildGUI();
		resetComponentSize();
	}
	
	/** Modifies the UI to display the initial channels details.*/
	private void cancelChannelsEdit()
	{
		buildChannelsPane();
		removeAll();
		buildGUI();
		resetComponentSize();
	}
	
	/** Initializes the components composing this display. */
    private void initComponents()
    {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIUtilities.BACKGROUND_COLOR);
        Font f;
       	parentLabel = new JLabel();
       	f = parentLabel.getFont(); 
       	Font newFont = f.deriveFont(f.getStyle(), f.getSize()-2);
       	parentLabel.setOpaque(false);
       	parentLabel.setFont(newFont);
       	parentLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
       	gpLabel = new JLabel();
       	gpLabel.setOpaque(false);
       	gpLabel.setFont(newFont);
       	gpLabel.setBackground(UIUtilities.BACKGROUND_COLOR);

       	wellLabel = new JLabel();
       	wellLabel.setOpaque(false);
       	wellLabel.setFont(newFont);
       	wellLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        
    	typePane = createTextPane();
    	typePane.setEditable(false);
    	newFont = f.deriveFont(f.getStyle(), f.getSize()-2);

    	
    	descriptionWiki = new OMEWikiComponent(false);
    	descriptionWiki.setDefaultText(DEFAULT_DESCRIPTION_TEXT);
    	descriptionWiki.installObjectFormatters();
    	descriptionWiki.setFont(newFont);
    	descriptionWiki.setEnabled(false);
    	descriptionWiki.setAllowOneClick(true);
    	descriptionWiki.addFocusListener(this);
    	descriptionWiki.addPropertyChangeListener(this);

    	typePane.setFont(f.deriveFont(Font.BOLD));
    	typePane.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
    	
    	f = parentLabel.getFont();
    	parentLabel.setFont(f.deriveFont(Font.BOLD));
    	parentLabel.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	f = gpLabel.getFont();
    	gpLabel.setFont(f.deriveFont(Font.BOLD));
    	gpLabel.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	f = wellLabel.getFont();
    	wellLabel.setFont(f.deriveFont(Font.BOLD));
    	wellLabel.setForeground(UIUtilities.DEFAULT_FONT_COLOR);

    	channelsArea = UIUtilities.createComponent(null);
    	
    	channelsPane = channelsArea;
    	IconManager icons = IconManager.getInstance();
		descriptionButtonEdit = new JToggleButton(icons.getIcon(IconManager.EDIT_12));
		formatButton(descriptionButtonEdit, EDIT_DESC_TEXT);
		
		editChannel = new JButton(icons.getIcon(IconManager.EDIT_12));
		formatButton(editChannel, EDIT_CHANNEL_TEXT);
		descriptionWiki.setEnabled(false);
		
		ItemListener l = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getSource() instanceof JToggleButton) {
					JToggleButton b = (JToggleButton) e.getSource();
						if (b.isSelected()) {
							expandDescriptionField(true);
							editField(descriptionWiki);
						} else
							save();
				}	
			}
		};
		descriptionButtonEdit.addItemListener(l);
		
		editChannel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editChannels();
			}
		});
		
    }
    
    /**
     * Formats the specified button.
     * 
     * @param button The button to handle.
     * @param text The tool tip text.
     * @param actionID The action command id.
     */
    private void formatButton(JButton button, String text)
    {
    	button.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(button);
		button.setBackground(UIUtilities.BACKGROUND_COLOR);
		button.setToolTipText(text);
    }
    
    /**
     * Formats the specified button.
     * 
     * @param button The button to handle.
     * @param text The tool tip text.
     * @param actionID The action command id.
     */
    private void formatButton(JToggleButton button, String text)
    {
    	button.setOpaque(false);
		button.setBackground(UIUtilities.BACKGROUND_COLOR);
		button.setBorder(new EmptyBorder(2, 2, 2, 2));
		button.setToolTipText(text);
    }
    
    /**
     * Lays out the plate fields.
     * 
     * @param plate The plate to handle.
     * @return See above.
     */
    private JPanel layoutPlateContent(PlateData plate)
    {
    	JPanel content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    	content.setLayout(new GridBagLayout());
		JLabel l = new JLabel();
    	Font font = l.getFont();
    	int size = font.getSize()-2;
    	
    	Map<JLabel, JComponent> components = 
    		new LinkedHashMap<JLabel, JComponent>();
    	String v = plate.getPlateType();
    	JLabel value;
    	if (v != null && v.trim().length() > 0) {
    		l = UIUtilities.setTextFont(EditorUtil.TYPE, Font.BOLD, size);
        	value = UIUtilities.createComponent(null);
        	value.setFont(font.deriveFont(font.getStyle(), size));
        	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
        	value.setText(v);
        	components.put(l, value);
    	}
    	l = UIUtilities.setTextFont(EditorUtil.EXTERNAL_IDENTIFIER,
    			Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setFont(font.deriveFont(font.getStyle(), size));
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	v = plate.getExternalIdentifier();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);
    	l = UIUtilities.setTextFont(EditorUtil.STATUS, Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setFont(font.deriveFont(font.getStyle(), size));
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	v = plate.getStatus();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);
    	layoutComponents(content, components);
    	return content;
    }
    
    /**
     * Lays out the well fields.
     * 
     * @param well The well to handle.
     * @return See above.
     */
    private JPanel layoutWellContent(WellData well)
    {
    	JPanel content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    	content.setLayout(new GridBagLayout());
		JLabel l = new JLabel();
    	Font font = l.getFont();
    	int size = font.getSize()-2;
    	
    	Map<JLabel, JComponent> components = 
    		new LinkedHashMap<JLabel, JComponent>();
    	String v = well.getWellType();
    	JLabel value;
    	if (v != null && v.trim().length() > 0) {
    		l = UIUtilities.setTextFont(EditorUtil.TYPE, Font.BOLD, size);
        	value = UIUtilities.createComponent(null);
        	value.setFont(font.deriveFont(font.getStyle(), size));
        	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
        	value.setText(v);
        	components.put(l, value);
    	}
    	l = UIUtilities.setTextFont(EditorUtil.EXTERNAL_DESCRIPTION, 
    			Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setFont(font.deriveFont(font.getStyle(), size));
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	v = well.getExternalDescription();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);
    	l = UIUtilities.setTextFont(EditorUtil.STATUS, Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setFont(font.deriveFont(font.getStyle(), size));
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	v = well.getStatus();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);
    	layoutComponents(content, components);
    	return content;
    }
    
    /**
     * Lays out the screen fields.
     * 
     * @param screen The screen to handle.
     * @return See above.
     */
    private JPanel layoutScreenContent(ScreenData screen)
    {
    	JPanel content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    	content.setLayout(new GridBagLayout());
		JLabel l = new JLabel();
    	Font font = l.getFont();
    	int size = font.getSize()-2;
    	
    	Map<JLabel, JComponent> components = 
    		new LinkedHashMap<JLabel, JComponent>();
    	
    	l = UIUtilities.setTextFont("Protocol Identifier:", Font.BOLD, size);
    	JLabel value = UIUtilities.createComponent(null);
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	String v = screen.getProtocolIdentifier();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);
    	
    	l = UIUtilities.setTextFont("Protocol Description:", Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setFont(font.deriveFont(font.getStyle(), size));
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	v = screen.getProtocolDescription();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);
    	
    	l = UIUtilities.setTextFont("ReagentSet Identifier:", Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setFont(font.deriveFont(font.getStyle(), size));
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	v = screen.getReagentSetIdentifier();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);
    	
    	l = UIUtilities.setTextFont("ReagentSet Description:", Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	value.setFont(font.deriveFont(font.getStyle(), size));
    	v = screen.getReagentSetDescripion();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);

    	layoutComponents(content, components);
    	return content;
    }

    /**
     * Lays out the passed components.
     * 
     * @param pane The main pane.
     * @param components The components to lay out.
     */
    private void layoutComponents(JPanel pane, 
    		Map<JLabel, JComponent> components)
    {
    	GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
        Entry<JLabel, JComponent> entry;
        
		Iterator<Entry<JLabel, JComponent>> 
		i = components.entrySet().iterator();
		c.gridy = 0;
        while (i.hasNext()) {
            c.gridx = 0;
            entry = i.next();
            ++c.gridy;
       	 	c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            pane.add(entry.getKey(), c);
            c.gridx++;
            pane.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;//end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            pane.add(entry.getValue(), c);  
        }
    }
    
    /**
     * Returns the pixels size as a string.
     * 
     * @param details The map to convert.
     * @param component The component displaying the information.
     * @return See above.
     */
    private String formatPixelsSize(Map details, JLabel component)
    {
        // First make sure that no conversion exception has occurred
        Object obj = details.get(EditorUtil.PIXEL_SIZE_X);
        if (obj instanceof BigResult) {
            MetadataViewerAgent.logBigResultExeption(this, obj, EditorUtil.PIXEL_SIZE_X);
            return "N/A";
        }
        obj = details.get(EditorUtil.PIXEL_SIZE_Y);
        if (obj instanceof BigResult) {
            MetadataViewerAgent.logBigResultExeption(this, obj, EditorUtil.PIXEL_SIZE_Y);
            return "N/A";
        }
        obj = details.get(EditorUtil.PIXEL_SIZE_Z);
        if (obj instanceof BigResult) {
            MetadataViewerAgent.logBigResultExeption(this, obj, EditorUtil.PIXEL_SIZE_Z);
            return "N/A";
        }
        
    	Length x = (Length) details.get(EditorUtil.PIXEL_SIZE_X);
    	Length y = (Length) details.get(EditorUtil.PIXEL_SIZE_Y);
    	Length z = (Length) details.get(EditorUtil.PIXEL_SIZE_Z);
    	Double dx = null, dy = null, dz = null;
    	boolean number = true;
    	NumberFormat nf = new DecimalFormat("0.00");
    	UnitsLength unit = null;
    	try {
    		x = UIUtilities.transformSize(x);
			dx = x.getValue();
			unit = x.getUnit();
		} catch (Exception e) {
			number = false;
		}
		try {
		    if (unit == null) {
		        y = UIUtilities.transformSize(y);
		        dy = y.getValue();
		        unit = y.getUnit();
		    }
		    else {
		        y = new LengthI(y, unit);
		        dy = y.getValue();
		    }
		} catch (Exception e) {
			number = false;
		}
		try {
		    if (unit == null) {
                z = UIUtilities.transformSize(z);
                dz = z.getValue();
                unit = z.getUnit();
            }
            else {
                z = new LengthI(z, unit);
                dz = z.getValue();
            }
		} catch (Exception e) {
			number = false;
		}
		
    	String label = "Pixels Size (";
    	String value = "";
    	String tooltip = "<html><body>";
    	if (dx != null && dx.doubleValue() > 0) {
    		value += nf.format(dx);
    		tooltip += "X: "+x.getValue()+" "+LengthI.lookupSymbol(x.getUnit())+"<br>";
    		label += "X";
    	}
    	if (dy != null && dy.doubleValue() > 0) {
    		if (value.length() == 0) value += nf.format(dy);
    		else value +="x"+nf.format(dy);;
    		tooltip += "Y: "+y.getValue()+" "+LengthI.lookupSymbol(y.getUnit())+"<br>";
    		label += "Y";
    	}
    	if (dz != null && dz.doubleValue() > 0) {
    		if (value.length() == 0) value += nf.format(dz);
    		else value +="x"+nf.format(dz);
    		tooltip += "Z: "+z.getValue()+" "+LengthI.lookupSymbol(z.getUnit())+"<br>";
    		label += "Z";
    	}
    	label += ") ";
    	component.setToolTipText(tooltip);

    	if (value.length() == 0) return null;
    	component.setText(value);
    	if (unit == null) unit = UnitsLength.MICROMETER;
    	label += "("+LengthI.lookupSymbol(unit)+")";
    	return label+":";
    }

	/**
     * Builds the panel hosting the information
     * 
     * @param details The information to display.
     * @param image	  The image of reference.
     * @return See above.
     */
    private JPanel buildContentPanel(Map details, ImageData image)
    {
    	JPanel content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    	content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 2, 2);
		c.gridy = 0;
		c.gridx = 0;
    	JLabel l = new JLabel();
    	Font font = l.getFont();
    	int size = font.getSize()-2;
    	JLabel label;
    	JLabel value;
    	String v = model.formatDate(image);
    	if(!CommonsLangUtils.isEmpty(v)) {
	    	label = UIUtilities.setTextFont(EditorUtil.ACQUISITION_DATE+":",
	    			Font.BOLD, size);
	    	value = UIUtilities.createComponent(null);
	    	value.setText(v);
	    	content.add(label, c);
	    	c.gridx++;
	    	content.add(value, c);
	    	c.gridy++;
	    	c.gridx = 0;
    	}
    	
    	try { //just to be on the save side
    		label = UIUtilities.setTextFont(EditorUtil.IMPORTED_DATE+":",
        			Font.BOLD, size);
        	value = UIUtilities.createComponent(null);
        	v =  UIUtilities.formatDefaultDate(image.getInserted());
        	value.setText(v);
        	content.add(label, c);
        	c.gridx++;
        	content.add(value, c);
        	c.gridy++; 
		} catch (Exception e) {
		}
    	
    	label = UIUtilities.setTextFont(EditorUtil.XY_DIMENSION+":", Font.BOLD,
    			size);
    	value = UIUtilities.createComponent(null);
    	v = (String) details.get(EditorUtil.SIZE_X);
    	v += " x ";
    	v += (String) details.get(EditorUtil.SIZE_Y);
    	value.setText(v);
    	c.gridx = 0;
    	content.add(label, c);
    	c.gridx++;
    	content.add(value, c);
    	c.gridy++;
    	label = UIUtilities.setTextFont(EditorUtil.PIXEL_TYPE+":", Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setText((String) details.get(EditorUtil.PIXEL_TYPE));
    	c.gridx = 0;
    	content.add(label, c);
    	c.gridx++;
    	content.add(value, c);
    	
    	value = UIUtilities.createComponent(null);
    	String s = formatPixelsSize(details, value);
    	if (s != null) {
    		c.gridy++;
        	label = UIUtilities.setTextFont(s, Font.BOLD, size);
        	c.gridx = 0;
        	content.add(label, c);
        	c.gridx++;
        	content.add(value, c);
    	}
    	//parse modulo T.
    	Map<Integer, ModuloInfo> modulo = model.getModulo();
    	ModuloInfo moduloT = modulo.get(ModuloInfo.T);
    	c.gridy++;
    	label = UIUtilities.setTextFont(EditorUtil.Z_T_FIELDS+":", Font.BOLD,
    			size);
    	value = UIUtilities.createComponent(null);
    	v = (String) details.get(EditorUtil.SECTIONS);
    	v += " x ";
    	if (moduloT != null) {
    	    String time = (String) details.get(EditorUtil.TIMEPOINTS);
    	    int t = Integer.parseInt(time);
    	    v += ""+(t/moduloT.getSize());
    	} else {
    	    v += (String) details.get(EditorUtil.TIMEPOINTS);
    	}
    	value.setText(v);
    	c.gridx = 0;
    	content.add(label, c);
    	c.gridx++;
    	content.add(value, c);
    	c.gridy++;
    	if (moduloT != null) {
    	    label = UIUtilities.setTextFont(EditorUtil.SMALL_T_VARIABLE,
    	            Font.BOLD, size);
            value = UIUtilities.createComponent(null);
            value.setText(""+moduloT.getSize());
            c.gridx = 0;
            content.add(label, c);
            c.gridx++;
            content.add(value, c);
            c.gridy++;
    	}
    	if (!model.isNumerousChannel() && model.getRefObjectID() > 0) {
    		label = UIUtilities.setTextFont(EditorUtil.CHANNELS+":",
    				Font.BOLD, size);
    		c.gridx = 0;
        	content.add(label, c);
        	c.gridx++;
        	c.fill = GridBagConstraints.HORIZONTAL;
        	content.add(channelsPane, c);
        	c.fill = GridBagConstraints.NONE;
        	c.gridx++;
        	content.add(editChannel, c);
        	c.gridy++;
    	}
    	
    	label = new JLabel("...");
    	label = UIUtilities.setTextFont(EditorUtil.ROI_COUNT+":", Font.BOLD, size);
        c.gridx = 0;
        content.add(label, c);
        c.gridx++;
        roiCountLabel = UIUtilities.createComponent(null);
        roiCountLabel.setText("...");
        content.add(roiCountLabel, c);
        loadROICount(image);
        
        return content;
    }
    
    /** 
     * Initializes a <code>TextPane</code>.
     * 
     * @return See above.
     */
    private JTextArea createTextPane()
    {
    	JTextArea pane = new JTextArea();
    	pane.setOpaque(false);
    	pane.setBackground(UIUtilities.BACKGROUND_COLOR);
    	return pane;
    }

    /**
     * Lays out the components using a <code>FlowLayout</code>.
     * 
     * @param button    The component to lay out.
     * @param component	The component to lay out.
     * @param sizeRow   The size of the row.
     * @return See above.
     */
    private JPanel layoutEditablefield(Component button, JComponent component)
    {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 1;
        
        p.add(component, c);
        c.gridx++;
        
        if (button != null) {
        	c.fill = GridBagConstraints.NONE;
        	c.weightx = 0;
        	c.anchor = GridBagConstraints.EAST;
            p.add(button, c);
        }
       
        return p;
    }
    
    /**
     * Builds the properties component.
     * 
     * @return See above.
     */
    private JPanel buildProperties()
    {
    	Object refObject = model.getRefObject();
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(Box.createVerticalStrut(2));
        
        int w = 10;
        JPanel l = UIUtilities.buildComponentPanel(gpLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));
        l = UIUtilities.buildComponentPanel(parentLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));
        l = UIUtilities.buildComponentPanel(wellLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));
         
         if (refObject instanceof ImageData ||
            refObject instanceof DatasetData ||
            refObject instanceof ProjectData ||
        	refObject instanceof TagAnnotationData ||
        	refObject instanceof WellSampleData ||
        	refObject instanceof PlateData ||
        	refObject instanceof ScreenData ||
        	refObject instanceof PlateAcquisitionData) {
        	
        	descriptionScrollPane = new JScrollPane(descriptionWiki);
        	descriptionScrollPane.setBorder(AnnotationUI.EDIT_BORDER);
        	
        	descriptionPanel = new JPanel(new GridBagLayout());
        	descriptionPanel.setBackground(UIUtilities.BACKGROUND_COLOR);

        	GridBagConstraints c = new GridBagConstraints();
        	c.insets = new Insets(2, 2, 2, 2);
        	
        	c.gridx = 0;
        	c.gridy = 0;
        	c.gridheight = 2;
        	c.fill = GridBagConstraints.BOTH;
        	c.weightx = 1;
        	c.weighty = 1;
        	c.anchor = GridBagConstraints.NORTHWEST;
        	descriptionPanel.add(descriptionScrollPane, c);
        	
        	c.gridx = 1;
        	c.gridy = 0;
        	c.gridheight = 1;
        	c.fill = GridBagConstraints.NONE;
        	c.weightx = 0;
        	c.weighty = 0;
        	c.anchor = GridBagConstraints.NORTHEAST;
        	descriptionPanel.add(descriptionButtonEdit, c);
        	
        	boolean hasDescription = !descriptionWiki.getText().equals(DEFAULT_DESCRIPTION_TEXT);
        	expandDescriptionField(hasDescription);
        	
        	p.add(descriptionPanel);
            p.add(Box.createVerticalStrut(5));
         }
         return p;
    }
    
    /** Expands/Collapses the description text field */
    private void expandDescriptionField(boolean expand) {
        if (descriptionScrollPane == null)
            return;

        if (expand) {
            Dimension viewportSize = new Dimension(WIDTH, HEIGHT);
            descriptionScrollPane.getViewport().setPreferredSize(viewportSize);
        } else {
            descriptionScrollPane.getViewport().setPreferredSize(null);
        }
        revalidate();
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND);
        add(buildProperties());
        Object refObject = model.getRefObject();
        PixelsData data = null;
        ImageData img = null;
        if (refObject instanceof ImageData) {
        	img = (ImageData) refObject;
        	try {
        		data = ((ImageData) refObject).getDefaultPixels();
    		} catch (Exception e) {}
        }
        else if (refObject instanceof WellData) {
            add(Box.createVerticalStrut(5));
            add(layoutWellContent((WellData) refObject));
        }
        else if (refObject instanceof WellSampleData) {
        	img = ((WellSampleData) refObject).getImage();
        	if (img != null && img.getId() > 0)
        		data = img.getDefaultPixels();
        } else if (refObject instanceof PlateData) {
        	add(Box.createVerticalStrut(5));
        	add(layoutPlateContent((PlateData) refObject));
        } else if (refObject instanceof ScreenData) {
        	add(Box.createVerticalStrut(5));
        	add(layoutScreenContent((ScreenData) refObject));
        }
        
		add(Box.createVerticalStrut(5));
		
		if (data != null) {
			add(buildContentPanel(EditorUtil.transformPixelsData(data), img));
		} else if (refObject instanceof DatasetData
				|| refObject instanceof ProjectData
				|| refObject instanceof PlateData
				|| refObject instanceof PlateAcquisitionData
				|| refObject instanceof ScreenData) {
			DataObject dob = (DataObject) refObject;
			
			Timestamp crDate = dob.getCreated();
			if (crDate != null) {
                JLabel createDateLabel = new JLabel();
                Font font = createDateLabel.getFont();
                int size = font.getSize() - 2;
                createDateLabel.setFont((new JLabel()).getFont().deriveFont(
                        Font.BOLD, size));
                createDateLabel.setText(CREATIONDATE_TEXT);

                JLabel createDateValue = new JLabel();
                createDateValue.setFont((new JLabel()).getFont().deriveFont(
                        Font.PLAIN, size));
                createDateValue.setText(UIUtilities.formatDefaultDate(crDate));

                JPanel p = new JPanel();
                p.setLayout(new GridLayout(1, 2));
                p.add(createDateLabel);
                p.add(createDateValue);
                p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                p.setBackground(UIUtilities.BACKGROUND_COLOR);
                add(p);
			}
		}
    }

	/**
	 * Modifies the passed components depending on the value of the
	 * passed flag.
	 * 
	 * @param panel     The panel to handle.
	 * @param field		The field to handle.
	 * @param editable	Pass <code>true</code> if  to <code>edit</code>,
	 * 					<code>false</code> otherwise.
	 */
	private void editField(JComponent field)
	{
		descriptionWiki.setEnabled(true);
		descriptionScrollPane.setBorder(EDIT_BORDER_BLACK);
		field.requestFocus();
	}
	
	/**
	 * Returns the label associated to the well.
	 * 
	 * @param well The object to handle.
	 * @param columnIndex Indicates how to label the columns.
	 * @param rowIndex Indicates how to label the rows.
	 * @return See above.
	 */
	private String getWellLabel(WellData well, int columnIndex, int rowIndex)
	{
		int k = well.getRow()+1;
		String rowText = "";
		if (rowIndex == PlateData.ASCENDING_LETTER)
			rowText = UIUtilities.LETTERS.get(k);
		else if (rowIndex == PlateData.ASCENDING_NUMBER)
			rowText = ""+k;
		k = well.getColumn()+1;
		String columnText = "";
		if (columnIndex == PlateData.ASCENDING_LETTER)
			columnText = UIUtilities.LETTERS.get(k);
		else if (columnIndex == PlateData.ASCENDING_NUMBER)
			columnText = ""+k;
		String value = rowText+"-"+columnText;
		return value;
	}
	
	/** Sets the text of the parent label. */
	private void setParentLabel()
	{
		Object parent = model.getParentRootObject();
		if (parent instanceof WellData) {
			WellData well = (WellData) parent;
			PlateData plate = well.getPlate();
			String text = plate.getName();
			String s = UIUtilities.formatPartialName(text);
			parentLabel.setText("Plate: "+s);
			parentLabel.setToolTipText(text);
			parentLabel.repaint();
			text = "Well "+getWellLabel(well, plate.getColumnSequenceIndex(),
					plate.getRowSequenceIndex());
			wellLabel.setText(text);
		}
		parent = model.getGrandParentRootObject();
		if (parent instanceof ScreenData) {
			ScreenData screen = (ScreenData) parent;
			text = "Screen: ";
			text += screen.getName();
			gpLabel.setText(text);
			gpLabel.repaint();
		}
	}
	
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link EditorModel}.
     * Mustn't be <code>null</code>.
     * @param controller Reference to the {@link EditorControl}.
     * Mustn't be <code>null</code>.
     */
    PropertiesUI(EditorModel model, EditorControl controller)
    {
       super(model);
       if (controller == null)
    	   throw new IllegalArgumentException("No control.");
       this.controller = controller;
       title = TITLE;
       initComponents();
    }

    /** Allows to edit or not the name and description.*/
    private void editNames()
    {
        Object refObject = model.getRefObject();
        boolean b = model.canEdit();
        if (refObject instanceof FileData) {
                b = false;
        }
        descriptionButtonEdit.setEnabled(b);
    }

    /**
     * Overridden to lay out the UI.
     * @see AnnotationUI#buildUI()
     */
    protected void buildUI()
    {
        removeAll();
        Object refObject = model.getRefObject();
        text = model.getObjectTypeAsString(refObject);
        if (model.isMultiSelection())
            return;
        descriptionWiki.removeDocumentListener(this);

        boolean b = model.canEdit();
        
        originalDescription = model.getRefObjectDescription();
        if (CommonsLangUtils.isEmpty(originalDescription))
            originalDescription = DEFAULT_DESCRIPTION_TEXT;
        descriptionWiki.setText(originalDescription);
        expandDescriptionField(!originalDescription.equals(DEFAULT_DESCRIPTION_TEXT));
        descriptionWiki.setCaretPosition(0);
        descriptionWiki.setBackground(UIUtilities.BACKGROUND_COLOR);
        descriptionWiki.setForeground(UIUtilities.DEFAULT_FONT_COLOR);

        editNames();
        if (b) {
            descriptionWiki.addDocumentListener(this);
        }
        setParentLabel();
        buildChannelsPane();
        buildGUI();
    }
	
	/** 
	 * Sets the <code>enabled</code> flag to <code>false</code> when the
	 * channels are loading.
	 */
	void onChannelDataLoading()
	{
		editChannel.setEnabled(false);
	}
	
    /** Sets the focus on the name area. */
	void setFocusOnName() { //namePane.requestFocus();
	}
	
	/** Updates the data object. */
	void setParentRootObject()
	{
		Object object =  model.getRefObject();
		setParentLabel();
		if (!(object instanceof WellSampleData)) return;
		originalDescription = model.getRefObjectDescription();
		if (originalDescription == null || originalDescription.length() == 0)
			originalDescription = DEFAULT_DESCRIPTION_TEXT;
		descriptionWiki.setText(originalDescription);
		expandDescriptionField(!originalDescription.equals(DEFAULT_DESCRIPTION_TEXT));
        boolean b = model.canEdit();
        descriptionButtonEdit.setEnabled(b);
        descriptionButtonEdit.setSelected(false);
        if (b) {
        	descriptionWiki.addDocumentListener(this);
        }
	}
	
	void save() {
		updateDataObject();
		model.fireAnnotationSaving(null, Collections.emptyList(), false);
	}
	
	/** Updates the data object. */
	void updateDataObject() 
	{
		if (!hasDataToSave()) return;
		Object object =  model.getRefObject();
		String desc = descriptionWiki.getText().trim();
		if (desc == null) desc = "";
		if (object instanceof ProjectData) {
			ProjectData p = (ProjectData) object;
			p.setDescription(desc);
		} else if (object instanceof DatasetData) {
			DatasetData p = (DatasetData) object;
			p.setDescription(desc);
		} else if (object instanceof ImageData) {
			ImageData p = (ImageData) object;
			p.setDescription(desc);
		} else if (object instanceof TagAnnotationData) {
			TagAnnotationData p = (TagAnnotationData) object;
			p.setTagDescription(desc);
		} else if (object instanceof ScreenData) {
			ScreenData p = (ScreenData) object;
			p.setDescription(desc);
		} else if (object instanceof PlateData) {
			PlateData p = (PlateData) object;
			p.setDescription(desc);
		} else if (object instanceof WellSampleData) {
			WellSampleData well = (WellSampleData) object;
			ImageData img = well.getImage();
			img.setDescription(desc);
		} else if (object instanceof FileData) {
			FileData f = (FileData) object;
			if (f.getId() > 0) return;
		} else if (object instanceof PlateAcquisitionData) {
			PlateAcquisitionData pa = (PlateAcquisitionData) object;
			pa.setDescription(desc);
		}
	}
	
	/**
	 * Sets the channels when loaded.
	 * 
	 * @param channels The value to set.
	 */
	void setChannelData(Map channels)
	{
		if (channels == null) return;
		editChannel.setEnabled(model.canEdit());
		int n = channels.size()-1;
		Iterator k = channels.keySet().iterator();
		int j = 0;
		StringBuffer buffer = new StringBuffer();
		while (k.hasNext()) {
			buffer.append(((ChannelData) k.next()).getChannelLabeling());
			if (j != n) 
				buffer.append(", ");
			j++;
		}
		
		String text = CommonsLangUtils.wrap(buffer.toString(),
		        MAX_CHANNELNAMES_LENGTH_IN_CHARS, "<br>", true);
		channelsArea.setText("<html>"+text+"</html>");
		channelsArea.revalidate();
		channelsArea.repaint();
	}
	
	/**
	 * Returns the text.
	 * 
	 * @return See above.
	 */
	String getText() { return text; }
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return TITLE; }

	/**
	 * No implementation in this case.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<Object> getAnnotationToRemove() { return null; }

	/**
	 * No implementation in this case.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave() { return null; }
	
	/**
	 * Returns <code>true</code> if the data object has been edited,
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (model.isMultiSelection()) 
		    return false;
		
		String name = originalDescription;
		String value = descriptionWiki.getText();
		value = value.trim();
		if (name == null) return value.length() != 0;
		name = OMEWikiComponent.prepare(name.trim(), true);
		value = OMEWikiComponent.prepare(value.trim(), true);
		if (DEFAULT_DESCRIPTION_TEXT.equals(name) && 
				DEFAULT_DESCRIPTION_TEXT.equals(value)) return false;
		return !name.equals(value);
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData(Object)
	 */
	protected void clearData(Object oldObject)
	{
	    originalDescription = model.getRefObjectDescription();
	    descriptionWiki.removeDocumentListener(this);
	    if (CommonsLangUtils.isEmpty(originalDescription))
	        originalDescription = DEFAULT_DESCRIPTION_TEXT;
	    descriptionWiki.setText(originalDescription);
	    expandDescriptionField(!originalDescription.equals(DEFAULT_DESCRIPTION_TEXT));
	    descriptionButtonEdit.setSelected(false);
	    descriptionWiki.addDocumentListener(this);
	    channelEditPane = null;
	    descriptionWiki.setEnabled(false);
	    editNames();
	    if (oldObject == null) return;
	    if (!model.isSameObject(oldObject)) {
	        channelsArea.setText("");
	        parentLabel.setText("");
	        wellLabel.setText("");
	        gpLabel.setText("");
	    }
	}

	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() {}

	/**
	 * Sets the title of the component.
	 * @see AnnotationUI#setComponentTitle()
	 */
	protected void setComponentTitle() {}
	
        /**
         * Fires property indicating that some text has been entered.
         * 
         * @see DocumentListener#insertUpdate(DocumentEvent)
         */
        public void insertUpdate(DocumentEvent e) {
        }

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
	}
	
	/**
	 * Resets the default text of the text fields if <code>null</code> or
	 * length <code>0</code>.
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		String text = descriptionWiki.getText();
		editNames();
		if (CommonsLangUtils.isBlank(text)) {
			descriptionWiki.removeDocumentListener(this);
			descriptionWiki.setText(DEFAULT_DESCRIPTION_TEXT);
			descriptionWiki.addDocumentListener(this);
			firePropertyChange(EditorControl.SAVE_PROPERTY,
					Boolean.valueOf(false), Boolean.valueOf(true));
		}
	}
	
	/**
	 * Sets the position of the caret and selects the text depending on the
	 * source.
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e) {}
	
	/** 
	 * Listens to property changes fired by the {@link #descriptionWiki}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@SuppressWarnings("unchecked")
   	 public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		if (OMEWikiComponent.WIKI_DATA_OBJECT_PROPERTY.equals(name)) {
			WikiDataObject object = (WikiDataObject) evt.getNewValue();
			long id = object.getId();
			switch (object.getIndex()) {
				case WikiDataObject.IMAGE:
					if (id > 0) {
						ViewImage event = new ViewImage(
								model.getSecurityContext(),
								new ViewImageObject(id), null);
						event.setPlugin(MetadataViewerAgent.runAsPlugin());
						bus.post(event);
					}
			}
		} else if (OMEWikiComponent.WIKI_DATA_OBJECT_ONE_CLICK_PROPERTY.equals(
				name)) {
			
			WikiDataObject object = (WikiDataObject) evt.getNewValue();
			long id = object.getId();
			switch (object.getIndex()) {
				case WikiDataObject.IMAGE:
					bus.post(new DataObjectSelectionEvent(ImageData.class, id));
					break;
				case WikiDataObject.DATASET:
					bus.post(new DataObjectSelectionEvent(DatasetData.class, 
							id));
					break;
				case WikiDataObject.PROJECT:
					bus.post(new DataObjectSelectionEvent(
							ProjectData.class, id));
			}
		} else if (ChannelEditUI.CANCEL_PROPERTY.equals(name)) {
			cancelChannelsEdit();
		} else if (ChannelEditUI.SAVE_PROPERTY.equals(name)) {
			List<ChannelData> channels = (List<ChannelData>) evt.getNewValue();
			model.fireChannelSaving(channels, false);
			cancelChannelsEdit();
		} else if (ChannelEditUI.APPLY_TO_ALL_PROPERTY.equals(name)) {
			List<ChannelData> channels = (List<ChannelData>) evt.getNewValue();
			model.fireChannelSaving(channels, true);
			cancelChannelsEdit();
		} else if (OMEWikiComponent.TEXT_UPDATE_PROPERTY.equals(name)) {
		    firePropertyChange(EditorControl.SAVE_PROPERTY,
                    Boolean.valueOf(false), Boolean.valueOf(true));
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
        
        /** 
         * Starts an asyc. call to load the number of ROIs
         */
        void loadROICount(ImageData image) {
            ROICountLoader l = new ROICountLoader(new SecurityContext(image.getGroupId()), this, image.getId());
            l.load();
        }
        
        /**
         * Updates label showing the ROI count
         * @param n Number of ROIs of the current image
         */
        public void updateROICount(int n) {
            roiCountLabel.setText(""+n);
        }
}
