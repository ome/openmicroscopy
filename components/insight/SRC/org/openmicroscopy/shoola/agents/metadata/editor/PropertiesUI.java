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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.ChannelData;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenAcquisitionData;
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
	implements ActionListener, DocumentListener, FocusListener
{
    
	/** The title associated to this component. */
	static final String			TITLE = "Properties";

	/** The default description. */
    private static final String	DEFAULT_DESCRIPTION_TEXT = "Description";
    
    /** The text for the external identifier. */
    private static final String	EXTERNAL_IDENTIFIER = "External Identifier:";
    
    /** The text for the id. */
    private static final String ID_TEXT = "ID: ";
    
    /** Action ID indicating to edit the name. */
    private static final int	EDIT_NAME = 0;
    
    /** Action ID indicating to edit the description. */
    private static final int	EDIT_DESC = 1;
    
    /** Button to edit the name. */
	private JButton				editName;
	
	/** Button to add documents. */
	private JButton				editDescription;
	
    /** The name before possible modification. */
    private String				originalName;
    
    /** The name before possible modification. */
    private String				originalDisplayedName;
    
    /** The description before possible modification. */
    private String				originalDescription;
    
    /** The component hosting the name of the <code>DataObject</code>. */
    private JTextArea			namePane;
    
    /** The component hosting the description of the <code>DataObject</code>. */
    private JTextArea			descriptionPane;
    
    /** The component hosting the {@link #namePane}. */
    private JPanel				namePanel;
    
    /** The component hosting the {@link #descriptionPane}. */
    private JPanel				descriptionPanel;
    
    /** The component hosting the id of the <code>DataObject</code>. */
    private JLabel				idLabel;
    
    /** The label displaying the parent of the node. */
    private JLabel				parentLabel;
    
    /** Indicates if the <code>DataObject</code> has group visibility. */
    private JRadioButton 		publicBox;
    
    /** Indicates if the <code>DataObject</code> is only visible by owner. */
    private JRadioButton 		privateBox;
    
    /** 
     * Indicates if the <code>DataObject</code> is only visible by members
     * of the group the user belongs to. 
     */
    private JRadioButton 		groupBox;
    
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
	
	/** Initializes the components composing this display. */
    private void initComponents()
    {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIUtilities.BACKGROUND_COLOR);
        Font f;
        groupBox = new JRadioButton(EditorUtil.GROUP_VISIBLE);
        groupBox.setBackground(UIUtilities.BACKGROUND_COLOR);
        groupBox.setToolTipText(EditorUtil.GROUP_DESCRIPTION);
        groupBox.setEnabled(false);
    	publicBox = new JRadioButton(EditorUtil.PUBLIC);
    	publicBox.setBackground(UIUtilities.BACKGROUND_COLOR);
    	publicBox.setEnabled(false);
    	f = publicBox.getFont();
        privateBox =  new JRadioButton(EditorUtil.PRIVATE);
        privateBox.setBackground(UIUtilities.BACKGROUND_COLOR);
        publicBox.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
        privateBox.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
        privateBox.setSelected(true);
        privateBox.setEnabled(false);
    	ButtonGroup group = new ButtonGroup();
       	group.add(privateBox);
       	group.add(groupBox);
       	group.add(publicBox);
       	
       	parentLabel = new JLabel();
       	parentLabel.setOpaque(false);
       	parentLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
       	idLabel = UIUtilities.setTextFont("");
    	namePane = createTextPane();
    	/*
    	namePane.addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent e) {
    			if (e.getClickCount() == 2)
    				editField(namePanel, namePane, editName, true);
    		}
		});
		*/
    	namePane.setEditable(false);
    	namePane.addFocusListener(this);
    	descriptionPane = createTextPane();
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
    	descriptionPane.addPropertyChangeListener(controller);
    	descriptionPane.setText(DEFAULT_TEXT);
    	descriptionPane.addFocusListener(this);
    	defaultBorder = namePane.getBorder();
    	
    	f = namePane.getFont();
    	namePane.setFont(f.deriveFont(Font.BOLD));
    	f = descriptionPane.getFont();
    	descriptionPane.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
    	descriptionPane.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	
    	
    	f = parentLabel.getFont();
    	parentLabel.setFont(f.deriveFont(Font.BOLD));
    	parentLabel.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	
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
    	
    	l = UIUtilities.setTextFont(EXTERNAL_IDENTIFIER, Font.BOLD, size);
    	JLabel value = UIUtilities.createComponent(null);
    	value.setFont(font.deriveFont(font.getStyle(), size));
    	value.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	String v = plate.getExternalIdentifier();
    	if (v == null || v.length() == 0) v = NO_SET_TEXT;
    	value.setText(v);
    	components.put(l, value);
    	l = UIUtilities.setTextFont("Status:", Font.BOLD, size);
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
     * @return See above.
     */
    private String isValidPixelsSize(Map details)
    {
    	String x = (String) details.get(EditorUtil.PIXEL_SIZE_X);
    	String y = (String) details.get(EditorUtil.PIXEL_SIZE_Y);
    	String z = (String) details.get(EditorUtil.PIXEL_SIZE_Z);
    	String label = "Pixels Size (";
    	
    	String value = "";
    	String zero = "0";
    	if (!zero.equals(x)) {
    		value += x;
    		label += "X";
    	}
    	if (!zero.equals(y)) {
    		if (value.length() == 0) value += y;
    		else value +="x"+y;
    		label += "Y";
    	}
    	if (!zero.equals(z)) {
    		if (value.length() == 0) value += z;
    		else value +="x"+z;
    		label += "Z";
    	}
    	label += ") ";
    	if (value.length() == 0) return null;
    	return label+"="+value;
    }

    private boolean isPixelsSizeNumber(Map details)
    {
    	String x = (String) details.get(EditorUtil.PIXEL_SIZE_X);
    	String y = (String) details.get(EditorUtil.PIXEL_SIZE_Y);
    	String z = (String) details.get(EditorUtil.PIXEL_SIZE_Z);
    	try {
			Double.parseDouble(x);
			Double.parseDouble(y);
			Double.parseDouble(z);
			return true;
		} catch (Exception e) {
		}
		return false;
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
    	double[] columns = {TableLayout.PREFERRED, 2, TableLayout.FILL};
    	TableLayout layout = new TableLayout();
    	content.setLayout(layout);
    	layout.setColumn(columns);
    	int index = 0;
    	JLabel l = new JLabel();
    	Font font = l.getFont();
    	int size = font.getSize()-2;
    	layout.insertRow(index, TableLayout.PREFERRED);
    	JLabel label = UIUtilities.setTextFont("Image Date", Font.BOLD, size);
    	JLabel value = UIUtilities.createComponent(null);
    	String v = model.formatDate(image);
    	value.setText(v);
    	content.add(label, "0, "+index);
    	content.add(value, "2, "+index);
    	
    	index++;
    	layout.insertRow(index, TableLayout.PREFERRED);
    	label = UIUtilities.setTextFont("Dimensions (XY)", Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	v = (String) details.get(EditorUtil.SIZE_X);
    	v += " x ";
    	v += (String) details.get(EditorUtil.SIZE_Y);
    	value.setText(v);
    	content.add(label, "0, "+index);
    	content.add(value, "2, "+index);
    	
    	String s = isValidPixelsSize(details);
    	if (s != null) {
    		boolean isANumber = isPixelsSizeNumber(details);
    		String[] split = s.split("=");
    		index++;
        	layout.insertRow(index, TableLayout.PREFERRED);
        	label = UIUtilities.setTextFont(split[0]+EditorUtil.MICRONS, 
        			Font.BOLD, size);
        	value = UIUtilities.createComponent(null);
        	value.setText(split[1]);
        	content.add(label, "0, "+index);
        	content.add(value, "2, "+index);
        	if (!isANumber) {
        		value.setForeground(AnnotationUI.WARNING);
        		value.setToolTipText("Values stored in the file...");
        	}
    	}
    	index++;
    	layout.insertRow(index, TableLayout.PREFERRED);
    	label = UIUtilities.setTextFont("z-sections/timepoints", Font.BOLD, 
    			size);
    	value = UIUtilities.createComponent(null);
    	v = (String) details.get(EditorUtil.SECTIONS);
    	v += " x ";
    	v += (String) details.get(EditorUtil.TIMEPOINTS);
    	value.setText(v);
    	content.add(label, "0, "+index);
    	content.add(value, "2, "+index);
    	
    	index++;
    	layout.insertRow(index, TableLayout.PREFERRED);
    	
    	if (!model.isNumerousChannel()) {
    		label = UIUtilities.setTextFont("Channels", Font.BOLD, size);
        	content.add(label, "0, "+index);
        	content.add(channelsArea, "2, "+index);
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
     * @param c			The component to lay out.
     * @return See above.
     */
    private JPanel layoutEditablefield(Component button, JComponent c)
    {
    	JPanel p = new JPanel();
    	double[][] size = {{TableLayout.PREFERRED, TableLayout.FILL}, 
    			{TableLayout.PREFERRED, TableLayout.FILL}};
    	p.setLayout(new TableLayout(size));
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	JToolBar bar = new JToolBar();
    	bar.setBorder(null);
    	bar.setFloatable(false);
    	bar.setBackground(UIUtilities.BACKGROUND_COLOR);
    	bar.add(button);
    	p.add(bar, "0, 0, l, t");
    	p.add(c, "1, 0, 1, 1");
    	
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
    	 JPanel p = new JPanel();
    	 p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    	 p.setBackground(UIUtilities.BACKGROUND_COLOR);
         p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
         JPanel l = UIUtilities.buildComponentPanel(idLabel, 0, 0);
         l.setBackground(UIUtilities.BACKGROUND_COLOR);
         int w = editName.getIcon().getIconWidth()+4;
         p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));
         l = UIUtilities.buildComponentPanel(parentLabel, 0, 0);
         l.setBackground(UIUtilities.BACKGROUND_COLOR);
         p.add(layoutEditablefield(Box.createHorizontalStrut(w), l));
         
         namePanel = layoutEditablefield(editName, namePane);
         p.add(namePanel);
         p.add(Box.createVerticalStrut(5));
         Object refObject = model.getRefObject();
         if ((refObject instanceof ImageData) || 
            (refObject instanceof DatasetData) ||
        	(refObject instanceof ProjectData) || 
        	(refObject instanceof TagAnnotationData) ||
        	(refObject instanceof WellSampleData) |
        	(refObject instanceof PlateData) ||
        	(refObject instanceof ScreenData)) {
        	//|| (refObject instanceof FolderData)) {
        	 p.add(Box.createVerticalStrut(5));
        	 descriptionPanel = layoutEditablefield(editDescription, 
        			 			descriptionPane);
        	 p.add(descriptionPanel);
         }
         p.add(Box.createVerticalStrut(5));
         //p.add(buildPermissions(null));
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
	 * <code>editable</code> flag.
	 * 
	 * @param panel     The panel to handle.
	 * @param field		The field to handle.
	 * @param button	The editable button.
	 * @param editable	Pass <code>true</code> if <code>editable</code>,
	 * 					<code>false</code> otherwise.
	 */
	private void editField(JPanel panel, JTextArea field, JButton button, 
			boolean editable)
	{
		//if ((model.getRefObject() instanceof FolderData) && 
		//		(button == editName)) editable = false;
		
		field.setEditable(editable);
		button.setEnabled(editable);
		if (editable) {
			panel.setBorder(EDIT_BORDER);
			field.requestFocus();
		} else {
			panel.setBorder(defaultBorder);
		}
		if (field == namePane) {
			namePane.getDocument().removeDocumentListener(this);
			String text = namePane.getText();
			if (text != null) text = text.trim();
			if (editable) {
				namePane.setText(modifiedName);
			} else {
				namePane.setText(EditorUtil.getPartialName(text));
			}
			
			namePane.getDocument().addDocumentListener(this);
			namePane.select(0, 0);
			namePane.setCaretPosition(0);
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
	
	/** Sets the text of the parent label. */
	private void setParentLabel()
	{
		Object parent = model.getParentRootObject();
		String text = "";
		if (parent instanceof WellData) {
			PlateData plate = ((WellData) parent).getPlate();
			text = "Plate: "; 
			text += plate.getName();
		}
		parentLabel.setText(text);
		parentLabel.repaint();
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
       //buildGUI();
       init = false;
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
		descriptionPane.getDocument().removeDocumentListener(this);
		originalName = model.getRefObjectName();
		modifiedName = model.getRefObjectName();
		originalDisplayedName = EditorUtil.getPartialName(originalName);
		namePane.setText(originalDisplayedName);
		namePane.setToolTipText(originalName);
		Object refObject = model.getRefObject();
		text = "";
		editName.setEnabled(true);
        if (refObject instanceof ImageData) text = "Image ";
        else if (refObject instanceof DatasetData) text = "Dataset";
        else if (refObject instanceof ProjectData) text = "Project";
        else if (refObject instanceof ScreenData) text = "Screen";
        else if (refObject instanceof PlateData) text = "Plate";
        else if (refObject instanceof ScreenAcquisitionData)
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
        } 
        /*
        if (refObject instanceof FolderData) {
        	editName.setEnabled(false);
        	text = "Folder";
        }
        */
        String t = text;
        if (model.getRefObjectID() > 0)
        	t += " "+ID_TEXT+model.getRefObjectID();
		idLabel.setText(t);
		originalDescription = model.getRefObjectDescription();
		if (originalDescription == null || originalDescription.length() == 0)
			originalDescription = DEFAULT_DESCRIPTION_TEXT;
		descriptionPane.setText(originalDescription);
        boolean b = model.isCurrentUserOwner(model.getRefObject());
        if ((refObject instanceof WellSampleData) ||
        		(refObject instanceof ScreenAcquisitionData)) b = false;
        namePane.setEnabled(b);
        descriptionPane.setEnabled(b);
        //if (refObject instanceof FolderData)
    	//	namePane.setEnabled(false);
        if (b) {
        	//if (!(refObject instanceof FolderData))
        	namePane.getDocument().addDocumentListener(this);
        	descriptionPane.getDocument().addDocumentListener(this);
        }
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
        boolean b = model.isCurrentUserOwner(model.getRefObject());
        descriptionPane.setEnabled(b);
        if (b) {
        	descriptionPane.getDocument().addDocumentListener(this);
        }
        //buildGUI();
	}
	
	/** Updates the data object. */
	void updateDataObject() 
	{
		if (!hasDataToSave()) return;
		Object object =  model.getRefObject();
		String name = namePane.getText().trim();
		String desc = descriptionPane.getText().trim();
		if (name != null) {
			if (name.equals(originalName) || name.equals(originalDisplayedName))
				name = "";
		}
		
		if (object instanceof ProjectData) {
			ProjectData p = (ProjectData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof DatasetData) {
			DatasetData p = (DatasetData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof ImageData) {
			ImageData p = (ImageData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof TagAnnotationData) {
			TagAnnotationData p = (TagAnnotationData) object;
			if (name.length() > 0) 
				p.setTagValue(name);
			if (desc.length() > 0)
				p.setTagDescription(desc);
		} else if (object instanceof ScreenData) {
			ScreenData p = (ScreenData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof PlateData) {
			PlateData p = (PlateData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof WellSampleData) {
			WellSampleData well = (WellSampleData) object;
			ImageData img = well.getImage();
			if (name.length() > 0) img.setName(name);
			img.setDescription(desc);
		} 
		/*
		else if (object instanceof FolderData) {
			FolderData folder = (FolderData) object;
			folder.setDescription(desc);
		}
		*/
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
		String s = "";
		int n = channels.size()-1;
		Iterator k = channels.keySet().iterator();
		int j = 0;
		while (k.hasNext()) {
			s += ((ChannelData) k.next()).getChannelLabeling();
			if (j != n) s +=", ";
			j++;
		}
		channelsArea.setText(s);
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
		name = name.trim();
		if (value.equals(name)) return false;
		return true;
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
		descriptionPane.getDocument().removeDocumentListener(this);
		idLabel.setText("");
		namePane.setText(originalName);
		descriptionPane.setText(originalDescription);
		namePane.getDocument().addDocumentListener(this);
		descriptionPane.getDocument().addDocumentListener(this);
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
				editField(namePanel, namePane, editName, true);
				break;
			case EDIT_DESC:
				editField(descriptionPanel, descriptionPane, editDescription,
						true);
		}
	}
	
	/**
	 * Resets the default text of the text fields if <code>null</code> or
	 * length <code>0</code>.
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		try {
			Object src = e.getSource();
			if (src == namePane) {
				editField(namePanel, namePane, editName, false);
				String text = namePane.getText();
				editName.setEnabled(true);
				if (text == null || text.trim().length() == 0) {
					namePane.getDocument().removeDocumentListener(this);
					namePane.setText(modifiedName);
					namePane.getDocument().addDocumentListener(this);
				}
			} else if (src == descriptionPane) {
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
			}
		} catch (Exception ex) {
			Logger log = MetadataViewerAgent.getRegistry().getLogger();
			LogMessage msg = new LogMessage();
			msg.print("Focus Lost");
			msg.print(ex);
			log.error(this, msg);
		}
	}
	
	/**
	 * Sets the position of the caret and selects the text depending on the
	 * source.
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e)
	{
		try {
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
			} else if (src == descriptionPane) {
				String text = descriptionPane.getText();
				if (text != null) {
					if (DEFAULT_DESCRIPTION_TEXT.equals(text.trim())) {
						descriptionPane.selectAll();
					} else {
						int n = text.length()-1;
						if (n >= 0) descriptionPane.setCaretPosition(n);
					}
					//descriptionPane.selectAll();
				}
			}
		} catch (Exception ex) {
			Logger log = MetadataViewerAgent.getRegistry().getLogger();
			LogMessage msg = new LogMessage();
			msg.print("Focus Gained");
			msg.print(ex);
			log.error(this, msg);
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
