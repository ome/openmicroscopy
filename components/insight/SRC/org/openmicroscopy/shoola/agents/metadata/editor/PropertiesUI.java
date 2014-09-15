/*
 * org.openmicroscopy.shoola.agents.util.editor.PropertiesUI 
 *
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



//Java imports
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.ROICountLoader;
import org.openmicroscopy.shoola.agents.metadata.actions.ViewAction;
import org.openmicroscopy.shoola.agents.metadata.util.FilesetInfoDialog;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.file.modulo.ModuloInfo;
import org.openmicroscopy.shoola.util.ui.ClickableTooltip;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.UnitsObject;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiDataObject;

import pojos.AnnotationData;
import pojos.ChannelData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FileData;
import pojos.ImageData;
import pojos.MultiImageData;
import pojos.PixelsData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;

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
	implements ActionListener, DocumentListener, FocusListener, 
	PropertyChangeListener
{
    
	/** The title associated to this component. */
	static final String			TITLE = "Properties";
	
	/** The default description. */
    private static final String	DEFAULT_DESCRIPTION_TEXT = "Description";
    
    /** The text for the id. */
    private static final String ID_TEXT = "ID: ";
    
    /** The text for the owner. */
    private static final String OWNER_TEXT = "Owner: ";
    
    /** Action ID indicating to edit the name.*/
    private static final int	EDIT_NAME = 0;
    
    /** Action ID indicating to edit the description.*/
    private static final int	EDIT_DESC = 1;
    
    /** Action ID indicating to edit the channels.*/
    private static final int	EDIT_CHANNEL = 2;
    
    /** Text indicating to edit the name.*/
    private static final String EDIT_NAME_TEXT = "Edit the name";
    
    /** Text indicating to edit the description.*/
    private static final String EDIT_DESC_TEXT = "Edit the description.";
    
    /**Text indicating to edit the channels.*/
    private static final String EDIT_CHANNEL_TEXT = "Edit the channels.";

    /**Text indicating that this file is an inplace import*/
    private static final String INPLACE_IMPORT_TOOLTIP_TEXT = "<html>This file is an in-place import.<br>The data might not be located within the<br>OMERO data directory.</html>";
    
    private static final String INPLACE_IMPORT_TOOLTIP_ACTION_TEXT = "Show File Paths...";
    
    /** The default height of the description.*/
    private static final int HEIGHT = 120;
    
    /** The default width of the description.*/
    private static final int WIDTH = 100;
    
    /** Button to edit the name. */
	private JButton				editName;
	
	/** Button to edit the description. */
	private JButton				descriptionButtonEdit;
	
    /** The name before possible modification.*/
    private String				originalName;
    
    /** The name before possible modification.*/
    private String				originalDisplayedName;
    
    /** The description before possible modification.*/
    private String				originalDescription;
    
    /** The component hosting the name of the <code>DataObject</code>.*/
    private JTextArea			namePane;
    
    /** The component hosting the name of the <code>DataObject</code>. */
    private JTextArea			typePane;
    
    /** The component hosting the description of the <code>DataObject</code>. */
    private OMEWikiComponent	descriptionWiki;
    
    /** The component hosting the {@link #namePane}. */
    private JPanel				namePanel;
    
    /** The component hosting the {@link #descriptionWiki}. */
    private JPanel				descriptionPanel;
    
    /** The component hosting the id of the <code>DataObject</code>. */
    private JLabel				idLabel;
    
    /** The component hosting the icon for inplace imported images */
    private JLabel 				inplaceIcon;
    
    /** 
     * The component hosting the owner of the <code>DataObject</code>.
     * if not the current user. 
     */
    private JLabel				ownerLabel;
    
    /** The label displaying the parent of the node. */
    private JLabel				parentLabel;
    
    /** The label displaying the parent of the node. */
    private JLabel				gpLabel;
    
    /** The label displaying the parent of the node. */
    private JLabel				wellLabel;
    
    /** The area displaying the channels information. */
	private JLabel				channelsArea;

	/** The new full name. */
	private String				modifiedName;
	
	/** The default border of the name and description components. */
	private Border				defaultBorder;
	
	/** Reference to the control. */
	private EditorControl		controller;
	
	/** The text associated to the data object. */
	private String				text;

	/** ScrollPane hosting the {@link #descriptionWiki} component.*/
	private JScrollPane			descriptionScrollPane;

	/** The menu displaying the view options.*/
	private JPopupMenu			viewMenu;

	/** Flag indicating that the name is editable mode or not.*/
	private boolean editableName;
	
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
       	
       	idLabel = UIUtilities.setTextFont("");
       	idLabel.setName("ID label");
       	inplaceIcon = new JLabel(IconManager.getInstance().getIcon(IconManager.INPLACE_IMPORT));
       	ClickableTooltip inplaceIconTooltip = new ClickableTooltip(INPLACE_IMPORT_TOOLTIP_TEXT, createInplaceIconAction());
        inplaceIconTooltip.attach(inplaceIcon);
        
       	ownerLabel = new JLabel();
       	ownerLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
    	namePane = createTextPane();
    	namePane.setEditable(false);
    	editableName = false;
    	typePane = createTextPane();
    	typePane.setEditable(false);
    	namePane.addFocusListener(this);
    	f = namePane.getFont(); 
    	newFont = f.deriveFont(f.getStyle(), f.getSize()-2);
    	
    	descriptionWiki = new OMEWikiComponent(false);
    	descriptionWiki.installObjectFormatters();
    	descriptionWiki.setFont(newFont);
    	descriptionWiki.setEnabled(false);
    	descriptionWiki.setAllowOneClick(true);
    	descriptionWiki.addFocusListener(this);
    	descriptionWiki.addPropertyChangeListener(this);
    	
    	defaultBorder = namePane.getBorder();
    	namePane.setFont(f.deriveFont(Font.BOLD));
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
    	
    	f = ownerLabel.getFont();
    	ownerLabel.setFont(f.deriveFont(Font.BOLD, f.getSize()-2));
    	channelsArea = UIUtilities.createComponent(null);
    	
    	channelsPane = channelsArea;
    	IconManager icons = IconManager.getInstance();
		editName = new JButton(icons.getIcon(IconManager.EDIT_12));
		formatButton(editName, EDIT_NAME_TEXT, EDIT_NAME);
		descriptionButtonEdit = new JButton(icons.getIcon(IconManager.EDIT_12));
		formatButton(descriptionButtonEdit, EDIT_DESC_TEXT, EDIT_DESC);
		editChannel = new JButton(icons.getIcon(IconManager.EDIT_12));
		formatButton(editChannel, EDIT_CHANNEL_TEXT, EDIT_CHANNEL);
		editChannel.setEnabled(false);
		descriptionWiki.setEnabled(false);
    }
    
    /**
     * Creates the action for the inplace import icon tooltip
     * @return
     */
    private Action createInplaceIconAction() {
        Action inplaceIconAction = new AbstractAction(INPLACE_IMPORT_TOOLTIP_ACTION_TEXT) {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Just pass this on to the controller
                controller.actionPerformed(arg0);
            }
        };
        inplaceIconAction.putValue(Action.ACTION_COMMAND_KEY, ""+EditorControl.FILE_PATH_INPLACE_ICON);
        return inplaceIconAction;
    }
    
    /**
     * Formats the specified button.
     * 
     * @param button The button to handle.
     * @param text The tool tip text.
     * @param actionID The action command id.
     */
    private void formatButton(JButton button, String text, int actionID)
    {
    	button.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(button);
		button.setBackground(UIUtilities.BACKGROUND_COLOR);
		button.setToolTipText(text);
		button.addActionListener(this);
		button.setActionCommand(""+actionID);
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
    	String x = (String) details.get(EditorUtil.PIXEL_SIZE_X);
    	String y = (String) details.get(EditorUtil.PIXEL_SIZE_Y);
    	String z = (String) details.get(EditorUtil.PIXEL_SIZE_Z);
    	Double dx = null, dy = null, dz = null;
    	boolean number = true;
    	NumberFormat nf = NumberFormat.getInstance();
    	String units = null;
    	UnitsObject o;
    	try {
			dx = Double.parseDouble(x);
			o = EditorUtil.transformSize(dx);
			units = o.getUnits();
			dx = o.getValue();
		} catch (Exception e) {
			number = false;
		}
		try {
			dy = Double.parseDouble(y);
			o = EditorUtil.transformSize(dy);
			if (units == null) units = o.getUnits();
			dy = o.getValue();
		} catch (Exception e) {
			number = false;
		}
		try {
			dz = Double.parseDouble(z);
			o = EditorUtil.transformSize(dz);
			if (units == null) units = o.getUnits();
			dz = o.getValue();
		} catch (Exception e) {
			number = false;
		}
		
    	String label = "Pixels Size (";
    	String value = "";
    	String tooltip = "<html><body>";
    	if (dx != null && dx.doubleValue() > 0) {
    		value += nf.format(dx);
    		tooltip += "X: "+x+"<br>";
    		label += "X";
    	}
    	if (dy != null && dy.doubleValue() > 0) {
    		if (value.length() == 0) value += nf.format(dy);
    		else value +="x"+nf.format(dy);;
    		tooltip += "Y: "+y+"<br>";
    		label += "Y";
    	}
    	if (dz != null && dz.doubleValue() > 0) {
    		if (value.length() == 0) value += nf.format(dz);
    		else value +="x"+nf.format(dz);
    		tooltip += "Z: "+z+"<br>";
    		label += "Z";
    	}
    	label += ") ";
    	if (!number) {
    		component.setForeground(AnnotationUI.WARNING);
    		component.setToolTipText("Values stored in the file...");
    	} else {
    		component.setToolTipText(tooltip);
    	}
    	if (value.length() == 0) return null;
    	component.setText(value);
    	if (units == null) units = UnitsObject.MICRONS;
    	label += units;
    	return label;
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
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
    	JLabel l = new JLabel();
    	Font font = l.getFont();
    	int size = font.getSize()-2;
    	JLabel label = UIUtilities.setTextFont(EditorUtil.ARCHIVED,
    			Font.BOLD, size);
    	JCheckBox box = new JCheckBox();
    	box.setEnabled(false);
    	box.setBackground(UIUtilities.BACKGROUND);
    	box.setSelected(model.isArchived());
    	content.add(label, c);
    	c.gridx = c.gridx+2;
    	content.add(box, c);
    	c.gridy++;
    	c.gridx = 0;
    	label = UIUtilities.setTextFont(EditorUtil.ACQUISITION_DATE,
    			Font.BOLD, size);
    	JLabel value = UIUtilities.createComponent(null);
    	String v = model.formatDate(image);
    	value.setText(v);
    	content.add(label, c);
    	c.gridx = c.gridx+2;
    	content.add(value, c);
    	c.gridy++;
    	c.gridx = 0;
    	try { //just to be on the save side
    		label = UIUtilities.setTextFont(EditorUtil.IMPORTED_DATE,
        			Font.BOLD, size);
        	value = UIUtilities.createComponent(null);
        	v =  UIUtilities.formatShortDateTime(image.getInserted());
        	value.setText(v);
        	content.add(label, c);
        	c.gridx = c.gridx+2;
        	content.add(value, c);
        	c.gridy++; 
		} catch (Exception e) {
			
		}
    	label = UIUtilities.setTextFont(EditorUtil.XY_DIMENSION, Font.BOLD,
    			size);
    	value = UIUtilities.createComponent(null);
    	v = (String) details.get(EditorUtil.SIZE_X);
    	v += " x ";
    	v += (String) details.get(EditorUtil.SIZE_Y);
    	value.setText(v);
    	c.gridx = 0;
    	content.add(label, c);
    	c.gridx = c.gridx+2;
    	content.add(value, c);
    	c.gridy++;
    	label = UIUtilities.setTextFont(EditorUtil.PIXEL_TYPE, Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	value.setText((String) details.get(EditorUtil.PIXEL_TYPE));
    	c.gridx = 0;
    	content.add(label, c);
    	c.gridx = c.gridx+2;
    	content.add(value, c);
    	
    	value = UIUtilities.createComponent(null);
    	String s = formatPixelsSize(details, value);
    	if (s != null) {
    		c.gridy++;
        	label = UIUtilities.setTextFont(s, Font.BOLD, size);
        	c.gridx = 0;
        	content.add(label, c);
        	c.gridx = c.gridx+2;
        	content.add(value, c);
    	}
    	//parse modulo T.
    	Map<Integer, ModuloInfo> modulo = model.getModulo();
    	ModuloInfo moduloT = modulo.get(ModuloInfo.T);
    	c.gridy++;
    	label = UIUtilities.setTextFont(EditorUtil.Z_T_FIELDS, Font.BOLD,
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
    	c.gridx = c.gridx+2;
    	content.add(value, c);
    	c.gridy++;
    	if (moduloT != null) {
    	    label = UIUtilities.setTextFont(EditorUtil.SMALL_T_VARIABLE,
    	            Font.BOLD, size);
            value = UIUtilities.createComponent(null);
            value.setText(""+moduloT.getSize());
            c.gridx = 0;
            content.add(label, c);
            c.gridx = c.gridx+2;
            content.add(value, c);
            c.gridy++;
    	}
    	if (!model.isNumerousChannel() && model.getRefObjectID() > 0) {
    		label = UIUtilities.setTextFont(EditorUtil.CHANNELS,
    				Font.BOLD, size);
    		c.gridx = 0;
    		c.anchor = GridBagConstraints.NORTHEAST;
        	content.add(label, c);
        	c.anchor = GridBagConstraints.CENTER;
        	c.gridx++;
        	content.add(editChannel, c);
        	c.gridx++;
        	content.add(channelsPane, c);
        	c.gridy++;
    	}
    	
    	label = new JLabel("...");
    	label = UIUtilities.setTextFont(EditorUtil.ROI_COUNT, Font.BOLD, size);
        c.gridx = 0;
        content.add(label, c);
        c.gridx = c.gridx+2;
        roiCountLabel = UIUtilities.createComponent(null);
        roiCountLabel.setText("...");
        content.add(roiCountLabel, c);
        loadROICount(image);
        
    	JPanel p = UIUtilities.buildComponentPanel(content);
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
        return p;
    }
    
    /** 
     * Initializes a <code>TextPane</code>.
     * 
     * @return See above.
     */
    private JTextArea createTextPane()
    {
    	JTextArea pane = new JTextArea();
    	pane.setWrapStyleWord(true);
    	pane.setOpaque(false);
    	pane.setBackground(UIUtilities.BACKGROUND_COLOR);
    	return pane;
    }

    /**
     * Lays out the components using a <code>FlowLayout</code>.
     * 
     * @param button    The component to lay out.
     * @param component	The component to lay out.
     * @return See above.
     */
    private JPanel layoutEditablefield(Component button, JComponent component)
    {
    	return layoutEditablefield(button, component, -1);
    }
    
    /**
     * Lays out the components using a <code>FlowLayout</code>.
     * 
     * @param button    The component to lay out.
     * @param component	The component to lay out.
     * @param sizeRow   The size of the row.
     * @return See above.
     */
    private JPanel layoutEditablefield(Component button, JComponent component,
    		int sizeRow)
    {
    	JPanel p = new JPanel();
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		if (button != null) {
    		JToolBar bar = new JToolBar();
        	bar.setBorder(null);
        	bar.setFloatable(false);
        	bar.setBackground(UIUtilities.BACKGROUND_COLOR);
        	bar.add(button);
        	p.add(bar, c);
        	c.gridx++;
    	}
		if (sizeRow > 0) {
			c.ipady = sizeRow;
			c.gridheight = 2;
		}
		p.add(component, c);
    	JPanel content = UIUtilities.buildComponentPanel(p, 0, 0);
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	return content;
    }
    
    /**
     * Creates or recycles the menu.
     * 
     * @param invoker The component invoking the menu.
     * @param loc The location of the mouse clicked.
     */
    private void showViewMenu(JComponent invoker, Point loc)
    {
    	if (viewMenu == null) {
    		viewMenu = new JPopupMenu();
    		IconManager icons = IconManager.getInstance();
        	JMenuItem button = new JMenuItem(icons.getIcon(IconManager.VIEW));
        	button.setText(ViewAction.NAME);
        	button.setToolTipText(ViewAction.DESCRIPTION);
        	button.setActionCommand(""+EditorControl.VIEW_IMAGE);
        	button.addActionListener(controller);
        	viewMenu.add(button);
        	button = new JMenuItem(icons.getIcon(IconManager.VIEWER_IJ));
        	button.setText(ViewAction.NAME_IJ);
        	button.setToolTipText(ViewAction.DESCRIPTION_IJ);
        	button.setActionCommand(""+EditorControl.VIEW_IMAGE_IN_IJ);
        	button.addActionListener(controller);
        	viewMenu.add(button);
    	}
    	viewMenu.show(invoker, loc.x, loc.y);
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

        JPanel idPanel = new JPanel();
        idPanel.setLayout(new BoxLayout(idPanel, BoxLayout.X_AXIS));
        idPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        JPanel l = UIUtilities.buildComponentPanel(idLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        idPanel.add(l);
        idPanel.add(Box.createHorizontalGlue());
        idPanel.add(inplaceIcon);
        p.add(idPanel);
        
        l = UIUtilities.buildComponentPanel(ownerLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(l);
        int w = editName.getIcon().getIconWidth()+4;
        l = UIUtilities.buildComponentPanel(gpLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));
        l = UIUtilities.buildComponentPanel(parentLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));
        l = UIUtilities.buildComponentPanel(wellLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));

         namePanel = layoutEditablefield(editName, namePane);
         p.add(namePanel);
         p.add(Box.createVerticalStrut(5));
         
         if (refObject instanceof ImageData ||
            refObject instanceof DatasetData ||
            refObject instanceof ProjectData ||
        	refObject instanceof TagAnnotationData ||
        	refObject instanceof WellSampleData ||
        	refObject instanceof PlateData ||
        	refObject instanceof ScreenData ||
        	refObject instanceof PlateAcquisitionData) {
        	
        	descriptionScrollPane = new JScrollPane(descriptionWiki);
        	descriptionScrollPane.setHorizontalScrollBarPolicy(
        			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        	descriptionScrollPane.setBorder(AnnotationUI.EDIT_BORDER);
        	Dimension viewportSize = new Dimension(WIDTH, HEIGHT); 
        	descriptionScrollPane.getViewport().setPreferredSize(viewportSize);
        	
        	double[][] design = new double[][]
        			{
        				{TableLayout.PREFERRED, TableLayout.FILL},
        				{TableLayout.PREFERRED}
        			};
        	
        	TableLayout table = new TableLayout(design);
        	descriptionPanel = new JPanel(table);
        	descriptionPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        	descriptionPanel.add(descriptionButtonEdit, "0, 0, c, t");
        	descriptionPanel.add(descriptionScrollPane, "1, 0");
        	
        	p.add(descriptionPanel);
            p.add(Box.createVerticalStrut(5));
         }
         return p;
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
        } else if (refObject instanceof WellSampleData) {
        	img = ((WellSampleData) refObject).getImage();
        	if (img != null && img.getId() > 0)
        		data = img.getDefaultPixels();
        	Object parent = model.getParentRootObject();
        	if (parent instanceof WellData) {
        		add(Box.createVerticalStrut(5));
            	add(layoutWellContent((WellData) parent));
        	}
        } else if (refObject instanceof FileAnnotationData) {
        	FileAnnotationData fa = (FileAnnotationData) refObject;
        	String ns = fa.getNameSpace();
        	if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(ns) ||
        			FileAnnotationData.EDITOR_PROTOCOL_NS.equals(ns)) {
        		String description = fa.getDescription();
        		if (description != null && description.length() > 0) {
        			PreviewPanel panel = new PreviewPanel(description,
        					fa.getId());
        			panel.addPropertyChangeListener(controller);
        			add(Box.createVerticalStrut(5));
        	    	add(panel);
        		}
        	}
        } else if (refObject instanceof PlateData) {
        	add(Box.createVerticalStrut(5));
        	add(layoutPlateContent((PlateData) refObject));
        } else if (refObject instanceof ScreenData) {
        	add(Box.createVerticalStrut(5));
        	add(layoutScreenContent((ScreenData) refObject));
        }
        if (data == null) return;
        add(Box.createVerticalStrut(5));
    	add(buildContentPanel(EditorUtil.transformPixelsData(data), img));
    }

	/**
	 * Modifies the passed components depending on the value of the
	 * passed flag.
	 * 
	 * @param panel     The panel to handle.
	 * @param field		The field to handle.
	 * @param button	The button to handle.
	 * @param editable	Pass <code>true</code> if  to <code>edit</code>,
	 * 					<code>false</code> otherwise.
	 */
	private void editField(JPanel panel, JComponent field, JButton button,
			boolean editable)
	{
		if (field == namePane) {
			//namePane.setEnabled(editable);
			editableName = editable;
			namePane.setEditable(editable);
			if (editable) {
				panel.setBorder(EDIT_BORDER_BLACK);
				field.requestFocus();
			} else {
				panel.setBorder(defaultBorder);
			}
			namePane.getDocument().removeDocumentListener(this);
			String text = namePane.getText();
			if (text != null) text = text.trim();
			if (editable) namePane.setText(modifiedName);
			else namePane.setText(UIUtilities.formatPartialName(text));
			namePane.getDocument().addDocumentListener(this);
			namePane.select(0, 0);
			namePane.setCaretPosition(0);
		} else if (field == descriptionWiki) {
			descriptionWiki.setEnabled(editable); //was editable
			if (editable) {
				descriptionScrollPane.setBorder(EDIT_BORDER_BLACK);
				field.requestFocus();
			} else {
				descriptionScrollPane.setBorder(EDIT_BORDER);
			}
		}
	}
	
	/**
	 * Sets the new name of the edited object.
	 * 
	 * @param document The document to handle.
	 */
	private void handleNameChanged(Document document)
	{
		Document d = namePane.getDocument();
		if (d == document) modifiedName = namePane.getText();
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
			columnText = UIUtilities.LETTERS.get(k+1);
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
        if (refObject instanceof FileData ||
                refObject instanceof MultiImageData) {
                b = false;
        }
        namePane.setEnabled(b);
        editName.setEnabled(b);
        descriptionButtonEdit.setEnabled(b);
        editChannel.setEnabled(b);
    }

    /**
     * Overridden to lay out the UI.
     * @see AnnotationUI#buildUI()
     */
    protected void buildUI()
    {
        removeAll();
        editableName = false;
        namePane.setEditable(false);
        Object refObject = model.getRefObject();
        text = model.getObjectTypeAsString(refObject);
        if (model.isMultiSelection()) return;
        namePane.getDocument().removeDocumentListener(this);
        descriptionWiki.removeDocumentListener(this);
        originalName = model.getRefObjectName();
        modifiedName = model.getRefObjectName();
        originalDisplayedName = UIUtilities.formatPartialName(originalName);
        namePane.setText(originalDisplayedName);
        namePane.setToolTipText(originalName);

        boolean b = model.canEdit();
        String t = text;
        if (model.getRefObjectID() > 0)
            t += " "+ID_TEXT+model.getRefObjectID();
        if (refObject instanceof WellSampleData) {
            WellSampleData wsd = (WellSampleData) refObject;
            t += " (Image ID: "+wsd.getImage().getId()+")";
        }
        idLabel.setText(t);
        inplaceIcon.setVisible(model.isInplaceImport());
        String ownerName = model.getOwnerName();
        ownerLabel.setText("");
        if (ownerName != null && ownerName.length() > 0)
            ownerLabel.setText(OWNER_TEXT+ownerName);
        originalDescription = model.getRefObjectDescription();
        if (StringUtils.isEmpty(originalDescription))
            originalDescription = DEFAULT_DESCRIPTION_TEXT;
        descriptionWiki.setText(originalDescription);
        //wrap();
        descriptionWiki.setCaretPosition(0);
        descriptionWiki.setBackground(UIUtilities.BACKGROUND_COLOR);
        descriptionWiki.setForeground(UIUtilities.DEFAULT_FONT_COLOR);

        editNames();
        if (b) {
            namePane.getDocument().addDocumentListener(this);
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
	void setFocusOnName() { namePane.requestFocus(); }
	
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
        boolean b = model.canEdit();
        descriptionButtonEdit.setEnabled(b);
        if (b) {
        	descriptionWiki.addDocumentListener(this);
        }
	}

	/** Updates the data object. */
	void updateDataObject() 
	{
		if (!hasDataToSave()) return;
		Object object =  model.getRefObject();
		String name = modifiedName;
		String desc = descriptionWiki.getText().trim();
		if (name != null) {
			if (name.equals(originalName) || name.equals(originalDisplayedName))
				name = "";
		}
		String value = desc;
		if (desc != null) {
			String v = OMEWikiComponent.prepare(originalDescription.trim(),
					true);
			String v2 = OMEWikiComponent.prepare(desc.trim(), true);
			if (v2.equals(v)) value = "";
		}
		if (value == null) value = "";
		if (object instanceof ProjectData) {
			ProjectData p = (ProjectData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(value);
		} else if (object instanceof DatasetData) {
			DatasetData p = (DatasetData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(value);
		} else if (object instanceof ImageData) {
			ImageData p = (ImageData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(value);
		} else if (object instanceof TagAnnotationData) {
			TagAnnotationData p = (TagAnnotationData) object;
			if (name.length() > 0) p.setTagValue(name);
			p.setTagDescription(value);
		} else if (object instanceof ScreenData) {
			ScreenData p = (ScreenData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(value);
		} else if (object instanceof PlateData) {
			PlateData p = (PlateData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(value);
		} else if (object instanceof WellSampleData) {
			WellSampleData well = (WellSampleData) object;
			ImageData img = well.getImage();
			if (name.length() > 0) img.setName(name);
			img.setDescription(value);
		} else if (object instanceof FileData) {
			FileData f = (FileData) object;
			if (f.getId() > 0) return;
		} else if (object instanceof PlateAcquisitionData) {
			PlateAcquisitionData pa = (PlateAcquisitionData) object;
			if (name.length() > 0) pa.setName(name);
			pa.setDescription(value);
		}
	}
	
	/**
	 * Returns <code>true</code> if the name is valid,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isNameValid()
	{ 
		String name = namePane.getText();
		if (name == null) return false;
		return name.trim().length() != 0;
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
			if (j != n) buffer.append(", ");
			j++;
		}
		channelsArea.setText(buffer.toString());
		channelsArea.revalidate();
		channelsArea.repaint();
	}

	/** 
	 * Sets the extent size
	 * 
	 * @param width The value to set.
	 */
	void setExtentWidth(int width)
	{
		/*
		int diff = 10;
		int newWidth = width-diff;
		
		if (this.width != 0 && 
				(this.width-diff <= newWidth && newWidth <= this.width+diff)) return;
		
		this.width = newWidth;
		
		if (descriptionPanel != null) {
			Dimension viewportSize = new Dimension(width, HEIGHT);
			pane.getViewport().setPreferredSize(viewportSize);
			
			Dimension paneSize = pane.getSize();
			int h = paneSize.height;
			if (h < HEIGHT) h = HEIGHT;
			
			String newLineStr = null;
			if (pane.getVerticalScrollBar().isVisible())
				newLineStr = "\n";
		
			Dimension descriptionSize = new Dimension(this.width, HEIGHT);
			
			descriptionPane.setSize(descriptionSize);
			descriptionPane.setPreferredSize(descriptionSize);
		    descriptionPane.wrapText(this.width, newLineStr);
		    
			descriptionPanel.setSize(descriptionSize);
			descriptionPanel.setPreferredSize(descriptionSize);
		}
		*/
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
		if (model.isMultiSelection()) return false;
		String name = originalName;
		String value = namePane.getText();
		value = value.trim();
		if (name == null) return false;
		if (!name.equals(value) && !originalDisplayedName.equals(value))
			return true;
		
		name = originalDescription;
		value = descriptionWiki.getText();
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
	    editableName = false;
	    originalName = model.getRefObjectName();
	    originalDisplayedName = originalName;
	    originalDescription = model.getRefObjectDescription();
	    namePane.getDocument().removeDocumentListener(this);
	    descriptionWiki.removeDocumentListener(this);
	    namePane.setText(originalName);
	    if (StringUtils.isEmpty(originalDescription))
	        originalDescription = DEFAULT_DESCRIPTION_TEXT;
	    descriptionWiki.setText(originalDescription);
	    namePane.getDocument().addDocumentListener(this);
	    descriptionWiki.addDocumentListener(this);
	    channelEditPane = null;
	    editChannel.setEnabled(false);
	    descriptionWiki.setEnabled(false);
	    editNames();
	    if (oldObject == null) return;
	    if (!model.isSameObject(oldObject)) {
	        channelsArea.setText("");
	        idLabel.setText("");
	        inplaceIcon.setVisible(false);
	        ownerLabel.setText("");
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
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		handleNameChanged(e.getDocument());
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		handleNameChanged(e.getDocument());
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}

	/** 
	 * Edits the components displaying the name and description
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
	    int index = Integer.parseInt(e.getActionCommand());
	    switch (index) {
	    case EDIT_NAME:
	        editField(namePanel, namePane, editName, !editableName);
	        break;
	    case EDIT_DESC:
	        editField(descriptionPanel, descriptionWiki,
	                descriptionButtonEdit, !descriptionWiki.isEnabled());
	        break;
	    case EDIT_CHANNEL:
	        editChannels();
	    }
	}
	
	/**
	 * Resets the default text of the text fields if <code>null</code> or
	 * length <code>0</code>.
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		Object src = e.getSource();
		if (src == namePane) {
			String text = namePane.getText();
			editNames();
			if (StringUtils.isBlank(text)) {
				namePane.getDocument().removeDocumentListener(this);
				namePane.setText(modifiedName);
				namePane.getDocument().addDocumentListener(this);
				firePropertyChange(EditorControl.SAVE_PROPERTY,
						Boolean.valueOf(false), Boolean.valueOf(true));
			}
		} else if (src == descriptionWiki) {
			String text = descriptionWiki.getText();
			editNames();
			if (StringUtils.isBlank(text)) {
				descriptionWiki.removeDocumentListener(this);
				descriptionWiki.setText(DEFAULT_DESCRIPTION_TEXT);
				descriptionWiki.addDocumentListener(this);
				firePropertyChange(EditorControl.SAVE_PROPERTY,
						Boolean.valueOf(false), Boolean.valueOf(true));
			}
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
					break;
				case WikiDataObject.PROTOCOL:
					bus.post(new EditFileEvent(model.getSecurityContext(), id));
					break;
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
					break;
				case WikiDataObject.PROTOCOL:
					bus.post(new DataObjectSelectionEvent(
							FileData.class, id));
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
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

	/** Displays the file set associated to the image. */
        void displayFileset() {
            Point location = inplaceIcon.getLocation();
            SwingUtilities.convertPointToScreen(location, inplaceIcon.getParent());
            // as the inplaceIcon already is on the right edge of the window
            // move it a bit more to the left
            location.translate(
                    -FilesetInfoDialog.DEFAULT_WIDTH + inplaceIcon.getWidth(),
                    inplaceIcon.getHeight());
            FilesetInfoDialog d = new FilesetInfoDialog();
            d.setData(model.getFileset(), model.isInplaceImport());
            d.open(location);
        }
        
        /** 
         * Starts an asyc. call to load the number of ROIs
         */
        void loadROICount(ImageData image) {
            ExperimenterData exp = MetadataViewerAgent.getUserDetails();
            ROICountLoader l = new ROICountLoader(new SecurityContext(exp.getGroupId()), this, image.getId(), exp.getId());
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
