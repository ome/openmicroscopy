/*
 * org.openmicroscopy.shoola.agents.util.editor.PropertiesUI 
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiDataObject;
import pojos.AnnotationData;
import pojos.ChannelData;
import pojos.DatasetData;
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
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class PropertiesUI   
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
    
    /** Button to edit the name. */
	private JButton				editName;
	
	/** Button to add documents. */
	private JButton				editDescription;
	
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
    private OMEWikiComponent	descriptionPane;
    
    /** The component hosting the {@link #namePane}. */
    private JPanel				namePanel;
    
    /** The component hosting the {@link #descriptionPane}. */
    private JPanel				descriptionPanel;
    
    /** The component hosting the id of the <code>DataObject</code>. */
    private JLabel				idLabel;
    
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
	
	/** Flag indicating to build the UI once. */
	private boolean 			init;
	
	/** Description pane.*/
	private JScrollPane			pane;

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
       	ownerLabel = new JLabel();
       	ownerLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
    	namePane = createTextPane();
    	/*
    	namePane.addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent e) {
    			if (e.getClickCount() == 2)
    				editField(namePanel, namePane, editName, true);
    		}
		});
		*/
    	typePane = createTextPane();
    	typePane.setEditable(false);
    	namePane.setEditable(false);
    	namePane.addFocusListener(this);
    	f = namePane.getFont(); 
    	newFont = f.deriveFont(f.getStyle(), f.getSize()-2);
    	descriptionPane = new OMEWikiComponent(false);
    	try {
    		descriptionPane.installObjectFormatters();
		} catch (Exception e) {
			//just to be on the save side.
		}
    	
    	descriptionPane.setFont(newFont);
    	//descriptionPane = new RegexTextPane(f.getFamily(), f.getSize()-2);
    	//descriptionPane.installDefaultRegEx();
    	//descriptionPane.addPropertyChangeListener(controller);
    	
    	//descriptionPane.setBackground(UIUtilities.BACKGROUND_COLOR);
    	//descriptionPane.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	
    	descriptionPane.addPropertyChangeListener(this);
    	//descriptionPane.setLineWrap(true);
    	//descriptionPane.setColumns(20);
    	/*
    	descriptionPane.addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent e) {
    			if (e.getClickCount() == 2)
    				editField(descriptionPanel, descriptionPane, 
    						editDescription, true);
    		}
		});
		*/
    	descriptionPane.setEnabled(false);
    	descriptionPane.setAllowOneClick(true);
    	descriptionPane.addFocusListener(this);
    	addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				wrap();
			}
		});
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
    	
    	IconManager icons = IconManager.getInstance();
		editName = new JButton(icons.getIcon(IconManager.EDIT_12));
		editName.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(editName);
		editName.setBackground(UIUtilities.BACKGROUND_COLOR);
		editName.setToolTipText("Edit the name.");
		editName.addActionListener(this);
		editName.setActionCommand(""+EDIT_NAME);
		editDescription = new JButton(icons.getIcon(IconManager.EDIT_12));
		editDescription.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(editDescription);
		editDescription.setBackground(UIUtilities.BACKGROUND_COLOR);
		editDescription.setToolTipText("Edit the description.");
		editDescription.addActionListener(this);
		editDescription.setActionCommand(""+EDIT_DESC);
		descriptionPane.setEnabled(false);
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
        Set set = components.entrySet();
        Entry entry;
        
		Iterator i = set.iterator();
		c.gridy = 0;
        while (i.hasNext()) {
            c.gridx = 0;
            entry = (Entry) i.next();
            ++c.gridy;
       	 	c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            pane.add((JLabel) entry.getKey(), c);
            c.gridx++;
            pane.add(Box.createHorizontalStrut(5), c); 
            c.gridx++;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            pane.add((JComponent) entry.getValue(), c);  
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
    	try {
			dx = Double.parseDouble(x);
		} catch (Exception e) {
			number = false;
		}
		try {
			dy = Double.parseDouble(y);
		} catch (Exception e) {
			number = false;
		}
		try {
			dz = Double.parseDouble(z);
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
    	JLabel label = UIUtilities.setTextFont(EditorUtil.ACQUISITION_DATE, 
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
        	label = UIUtilities.setTextFont(s+EditorUtil.MICRONS, 
        			Font.BOLD, size);
        	c.gridx = 0;
        	content.add(label, c);
        	c.gridx = c.gridx+2;
        	content.add(value, c);
    	}
    	c.gridy++;
    	label = UIUtilities.setTextFont(EditorUtil.Z_T_FIELDS, Font.BOLD, 
    			size);
    	value = UIUtilities.createComponent(null);
    	v = (String) details.get(EditorUtil.SECTIONS);
    	v += " x ";
    	v += (String) details.get(EditorUtil.TIMEPOINTS);
    	value.setText(v);
    	c.gridx = 0;
    	content.add(label, c);
    	c.gridx = c.gridx+2;
    	content.add(value, c);
    	c.gridy++;
    	if (!model.isNumerousChannel() && model.getRefObjectID() > 0) {
    		label = UIUtilities.setTextFont(EditorUtil.CHANNELS,
    				Font.BOLD, size);
    		c.gridx = 0;
        	content.add(label, c);
        	c.gridx = c.gridx+2;
        	content.add(channelsArea, c);
    	}
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
    	}
		c.gridx++;
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
     * Builds the properties component.
     * 
     * @return See above.
     */
    private JPanel buildProperties()
    {
    	Object refObject = model.getRefObject();
        if (refObject instanceof ImageData) {
        	ImageData img = (ImageData) refObject;
        	try {
        		img.getDefaultPixels();
    		} catch (Exception e) {}
        } else if (refObject instanceof WellSampleData) {
        	ImageData img = ((WellSampleData) refObject).getImage();
        	if (img != null && img.getId() > 0) {
        		img.getDefaultPixels();
        	}
        }
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel l = UIUtilities.buildComponentPanel(idLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        int w = editName.getIcon().getIconWidth()+4;
        p.add(layoutEditablefield(null, l));
        l = UIUtilities.buildComponentPanel(ownerLabel, 0, 0);
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));
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
        	refObject instanceof ScreenData) {
        	 p.add(Box.createVerticalStrut(5));
        	 descriptionPanel = layoutEditablefield(editDescription, 
        			 descriptionPane, 5);
        	 //descriptionPanel.setBorder(AnnotationUI.EDIT_BORDER);

        	 pane = new JScrollPane(descriptionPanel);
        	 pane.setBorder(AnnotationUI.EDIT_BORDER);
        	 Dimension d = pane.getPreferredSize();
        	 pane.getViewport().setPreferredSize(new Dimension(d.width, 60));
        	 p.add(pane);
         } else if (refObject instanceof FileData) {
        	 /*
        	 FileData f = (FileData) refObject;
        	 if (f.isImage()) {
        		 p.add(Box.createVerticalStrut(5));
            	 descriptionPanel = layoutEditablefield(null, 
            			 			descriptionPane, 80);
            	 p.add(descriptionPanel);
        	 }
        	 */
         }
         p.add(Box.createVerticalStrut(5));
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
			button.setEnabled(editable);
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
		} else if (field == descriptionPane) {
			descriptionPane.setEnabled(editable); //was editable
			if (editable) {
				pane.setBorder(EDIT_BORDER_BLACK);
				field.requestFocus();
			} else {
				pane.setBorder(EDIT_BORDER);
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
	private String getWellLabel(WellData well, int columnIndex, 
			int rowIndex)
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
		parentLabel.setText("");
		wellLabel.setText("");
		gpLabel.setText("");
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
     * @param model 		Reference to the {@link EditorModel}.
     * 						Mustn't be <code>null</code>.   
     * @param controller 	Reference to the {@link EditorControl}.
     * 						Mustn't be <code>null</code>.                             
     */
    PropertiesUI(EditorModel model, EditorControl controller)
    {
       super(model);
       if (controller == null)
    	   throw new IllegalArgumentException("No control.");
       this.controller = controller;
       title = TITLE;
       initComponents();
       init = false;
    }   

    /** Wraps the text.*/
    private void wrap()
    {
    	if (descriptionPanel != null && descriptionPanel.getSize() != null) {
    		String newLineStr = null;
    		if (pane.getVerticalScrollBar().isVisible())
    			newLineStr = "";
			descriptionPane.wrapText(descriptionPanel.getSize().width,
					newLineStr);
		}
    }
    
    /**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		if (!init) {
			buildGUI();
			init = true;
		}
		removeAll();
		if (model.isMultiSelection()) return;
		namePane.getDocument().removeDocumentListener(this);
		//descriptionPane.getDocument().removeDocumentListener(this);
		descriptionPane.removeDocumentListener(this);
		originalName = model.getRefObjectName();
		modifiedName = model.getRefObjectName();
		originalDisplayedName = UIUtilities.formatPartialName(originalName);
		namePane.setText(originalDisplayedName);
		namePane.setToolTipText(originalName);
		Object refObject = model.getRefObject();
		text = "";
		
		boolean b = model.isUserOwner(refObject);
        if (refObject instanceof ImageData) text = "Image";
        else if (refObject instanceof DatasetData) text = "Dataset";
        else if (refObject instanceof ProjectData) text = "Project";
        else if (refObject instanceof ScreenData) text = "Screen";
        else if (refObject instanceof PlateData) text = "Plate";
        else if (refObject instanceof PlateAcquisitionData)
        	text = "Plate Run";
        else if (refObject instanceof FileAnnotationData) {
        	FileAnnotationData fa = (FileAnnotationData) refObject;
        	String ns = fa.getNameSpace();
        	if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(ns))
        		text = "Experiment";
        	else if (FileAnnotationData.EDITOR_PROTOCOL_NS.equals(ns))
        		text = "Protocol";
        	else text = "File";
        } else if (refObject instanceof WellSampleData) text = "Field";
        else if (refObject instanceof TagAnnotationData) {
        	TagAnnotationData tag = (TagAnnotationData) refObject;
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
        		text = "Tag Set";
        	else text = "Tag";
        } else if (refObject instanceof FileData) {
        	editName.setEnabled(false);
        	FileData f = (FileData) refObject;
        	if (f.isDirectory()) text = "Folder";
        	else text = "File";
        } else if (refObject instanceof MultiImageData) {
        	editName.setEnabled(false);
        	text = "File";
        }
        String t = text;
        if (model.getRefObjectID() > 0)
        	t += " "+ID_TEXT+model.getRefObjectID();
        if (refObject instanceof WellSampleData) {
        	WellSampleData wsd = (WellSampleData) refObject;
        	t += " (Image ID: "+wsd.getImage().getId()+")";
        }
		idLabel.setText(t);
		String ownerName = model.getOwnerName();
		if (ownerName != null && ownerName.length() > 0)
			ownerLabel.setText(OWNER_TEXT+ownerName);
		originalDescription = model.getRefObjectDescription();
		if (originalDescription == null || originalDescription.length() == 0)
			originalDescription = DEFAULT_DESCRIPTION_TEXT;
		descriptionPane.setText(originalDescription);
		
		//wrap();
		descriptionPane.setCaretPosition(0);
		descriptionPane.setBackground(UIUtilities.BACKGROUND_COLOR);
    	descriptionPane.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	
		
        if ((refObject instanceof WellSampleData) ||
        		(refObject instanceof PlateAcquisitionData)) b = false;
        
        namePane.setEnabled(b);
        //descriptionPane.setEnabled(b);
        if (!(refObject instanceof FileData)) editName.setEnabled(b);
        
        if (b) {
        	namePane.getDocument().addDocumentListener(this);
        	descriptionPane.addDocumentListener(this);
        }
        editDescription.setEnabled(b);
        setParentLabel();
        buildGUI();
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
		descriptionPane.setText(originalDescription);
        boolean b = model.isUserOwner(model.getRefObject());
        editDescription.setEnabled(b);
        if (b) {
        	//descriptionPane.getDocument().addDocumentListener(this);
        	descriptionPane.addDocumentListener(this);
        }
        //buildGUI();
	}

	/** Updates the data object. */
	void updateDataObject() 
	{
		if (!hasDataToSave()) return;
		Object object =  model.getRefObject();
		String name = modifiedName;//namePane.getText().trim();
		String desc = descriptionPane.getText().trim();
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
		if (object instanceof ProjectData) {
			ProjectData p = (ProjectData) object;
			if (name.length() > 0) p.setName(name);
			if (value.length() > 0) p.setDescription(value);
		} else if (object instanceof DatasetData) {
			DatasetData p = (DatasetData) object;
			if (name.length() > 0) p.setName(name);
			if (value.length() > 0) p.setDescription(value);
		} else if (object instanceof ImageData) {
			ImageData p = (ImageData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof TagAnnotationData) {
			TagAnnotationData p = (TagAnnotationData) object;
			if (name.length() > 0) 
				p.setTagValue(name);
			if (value.length() > 0)
				p.setTagDescription(value);
		} else if (object instanceof ScreenData) {
			ScreenData p = (ScreenData) object;
			if (name.length() > 0) p.setName(name);
			if (value.length() > 0) p.setDescription(value);
		} else if (object instanceof PlateData) {
			PlateData p = (PlateData) object;
			if (name.length() > 0) p.setName(name);
			if (value.length() > 0) p.setDescription(value);
		} else if (object instanceof WellSampleData) {
			WellSampleData well = (WellSampleData) object;
			ImageData img = well.getImage();
			if (name.length() > 0) img.setName(name);
			if (value.length() > 0) img.setDescription(value);
		} else if (object instanceof FileData) {
			FileData f = (FileData) object;
			if (f.getId() > 0) return;
			//if (f.isImage()) f.setDescription(desc);
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
	protected List<AnnotationData> getAnnotationToRemove() { return null; }

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
		value = descriptionPane.getText();
		value = value.trim();
		if (name == null) 
			return value.length() != 0;
		name = OMEWikiComponent.prepare(name.trim(), true);
		value = OMEWikiComponent.prepare(value.trim(), true);
		if (DEFAULT_DESCRIPTION_TEXT.equals(name) && 
				DEFAULT_DESCRIPTION_TEXT.equals(value)) return false;
		
		return !(name.equals(value));
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		originalName = model.getRefObjectName();
		originalDisplayedName = originalName;
		originalDescription = model.getRefObjectDescription();
		namePane.getDocument().removeDocumentListener(this);
		//descriptionPane.getDocument().removeDocumentListener(this);
		descriptionPane.removeDocumentListener(this);
		idLabel.setText("");
		ownerLabel.setText("");
		namePane.setText(originalName);
		descriptionPane.setText(originalDescription);
		namePane.getDocument().addDocumentListener(this);
		//descriptionPane.getDocument().addDocumentListener(this);
		descriptionPane.addDocumentListener(this);
		channelsArea.setText("");
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
				editField(namePanel, namePane, editName, !namePane.isEditable());
				break;
			case EDIT_DESC:
				editField(descriptionPanel, descriptionPane, editDescription,
						!descriptionPane.isEnabled());
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
			/*
			editField(namePanel, namePane, editName, false);
			String text = namePane.getText();
			editName.setEnabled(true);
			if (text == null || text.trim().length() == 0) {
				namePane.getDocument().removeDocumentListener(this);
				namePane.setText(modifiedName);
				namePane.getDocument().addDocumentListener(this);
			}
			*/
			//namePane.setCaretPosition(0);
			String text = namePane.getText();
			editName.setEnabled(true);
			namePane.setEditable(false);
			if (text == null || text.trim().length() == 0) {
				namePane.getDocument().removeDocumentListener(this);
				namePane.setText(modifiedName);
				namePane.getDocument().addDocumentListener(this);
				firePropertyChange(EditorControl.SAVE_PROPERTY, 
						Boolean.valueOf(false), Boolean.valueOf(true));
			}
		} else if (src == descriptionPane) {
			/*
			editField(descriptionPanel, descriptionPane, editDescription, 
					false);
			editDescription.setEnabled(true);
			String text = descriptionPane.getText();
			if (text == null || text.trim().length() == 0) {
				descriptionPane.getDocument().removeDocumentListener(this);
				descriptionPane.setText(DEFAULT_DESCRIPTION_TEXT);
				descriptionPane.getDocument().addDocumentListener(this);
			}
			descriptionPane.select(0, 0);
			*/
			//editField(descriptionPanel, descriptionPane, editDescription, 
			//		false);
			String text = descriptionPane.getText();
			editDescription.setEnabled(true);
			if (text == null || text.trim().length() == 0) {
				//descriptionPane.getDocument().removeDocumentListener(this);
				descriptionPane.removeDocumentListener(this);
				descriptionPane.setText(DEFAULT_DESCRIPTION_TEXT);
				//descriptionPane.getDocument().addDocumentListener(this);
				descriptionPane.addDocumentListener(this);
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
	public void focusGained(FocusEvent e)
	{
		Object src = e.getSource();
		if (src == namePane) {
			String text = namePane.getText();
			if (text != null) {
				
				//namePane.selectAll();
				//int n = text.length()-1;
				//if (n >= 0) namePane.setCaretPosition(n);
			}
			//namePane.select(0, 0);
			//namePane.setCaretPosition(0);
		}
	}
	
	/** 
	 * Listens to property changes fired by the {@link #descriptionPane}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		/*
		if (RegexTextPane.REGEX_DBL_CLICKED_PROPERTY.equals(name)) {
			WikiDataObject object = (WikiDataObject) evt.getNewValue();
			long id = object.getId();
			switch (object.getIndex()) {
				case WikiDataObject.IMAGE:
					if (id > 0) {
						bus.post(new ViewImage(id, null));
					}
					break;
				case WikiDataObject.PROTOCOL:
					bus.post(new EditFileEvent(id));
					break;
			}
		} 
		*/
		
		if (OMEWikiComponent.WIKI_DATA_OBJECT_PROPERTY.equals(name)) {
			WikiDataObject object = (WikiDataObject) evt.getNewValue();
			long id = object.getId();
			switch (object.getIndex()) {
				case WikiDataObject.IMAGE:
					if (id > 0) 
						bus.post(new ViewImage(new ViewImageObject(id), null));
					break;
				case WikiDataObject.PROTOCOL:
					bus.post(new EditFileEvent(id));
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
					break;
			}
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
