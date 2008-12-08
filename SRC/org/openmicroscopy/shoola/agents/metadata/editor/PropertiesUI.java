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
import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.ChannelData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

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
	implements DocumentListener
{
    
	/** The title associated to this component. */
	static final String			TITLE = "Properties";

	/** The default description. */
    private static final String	DEFAULT_DESCRIPTION_TEXT = "Description";
    
    /** The text for the id. */
    private static final String ID_TEXT = "ID:";
    
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
    
    /** The component hosting the id of the <code>DataObject</code>. */
    private JLabel				idLabel;
    
    /** Indicates if the <code>DataObject</code> has group visibility. */
    private JCheckBox 			publicBox;
    
    /** Indicates if the <code>DataObject</code> is only visible by owner. */
    private JCheckBox 			privateBox;
    
    /** The area displaying the channels information. */
	private JLabel				channelsArea;

	/**
     * Builds the panel hosting the information
     * 
     * @param details The information to display.
     * @return See above.
     */
    private JPanel buildContentPanel(Map details)
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
    	String v = model.formatDate((ImageData) model.getRefObject());
    	value.setText(v);
    	content.add(label, "0, "+index);
    	content.add(value, "2, "+index);
    	
    	index++;
    	layout.insertRow(index, TableLayout.PREFERRED);
    	label = UIUtilities.setTextFont("Dimensions", Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	v = (String) details.get(EditorUtil.SIZE_X);
    	v += " x ";
    	v += (String) details.get(EditorUtil.SIZE_Y);
    	value.setText(v);
    	content.add(label, "0, "+index);
    	content.add(value, "2, "+index);
    	
    	index++;
    	layout.insertRow(index, TableLayout.PREFERRED);
    	label = UIUtilities.setTextFont("Pixels Size "+EditorUtil.MICRONS, 
    			Font.BOLD, size);
    	value = UIUtilities.createComponent(null);
    	v = (String) details.get(EditorUtil.PIXEL_SIZE_X);
    	v += " x ";
    	v += (String) details.get(EditorUtil.PIXEL_SIZE_Y);
    	v += " x ";
    	v += (String) details.get(EditorUtil.PIXEL_SIZE_Z);
    	value.setText(v);
    	content.add(label, "0, "+index);
    	content.add(value, "2, "+index);
    	
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
    	
    	label = UIUtilities.setTextFont(EditorUtil.WAVELENGTHS, Font.BOLD, 
    			size);
    	content.add(label, "0, "+index);
    	content.add(channelsArea, "2, "+index);
    	
    	JPanel p = UIUtilities.buildComponentPanel(content);
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
        return p;
    }
    
    /**
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @param permissions   The permissions of the edited object.
     * @return See above.
     */
    private JPanel buildPermissions(PermissionData permissions)
    {
        JPanel content = new JPanel();
        content.setBackground(UIUtilities.BACKGROUND_COLOR);
       	if (permissions != null && 
       			permissions.isGroupRead()) publicBox.setSelected(true);
       	content.add(privateBox);
       	content.add(publicBox);
       	JPanel p = UIUtilities.buildComponentPanel(content, 0, 0);
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
    	//pane.setLineWrap(true);
    	pane.setWrapStyleWord(true);
    	pane.setOpaque(false);
    	pane.setBackground(UIUtilities.BACKGROUND_COLOR);
    	return pane;
    }

    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIUtilities.BACKGROUND_COLOR);
        Font f;
    	publicBox =  new JCheckBox(EditorUtil.PUBLIC);
    	publicBox.setBackground(UIUtilities.BACKGROUND_COLOR);
    	publicBox.setToolTipText(EditorUtil.PUBLIC_DESCRIPTION);
    	publicBox.setEnabled(false);
    	f = publicBox.getFont();
        privateBox =  new JCheckBox(EditorUtil.PRIVATE);
        privateBox.setBackground(UIUtilities.BACKGROUND_COLOR);
        publicBox.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
        privateBox.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
        privateBox.setSelected(true);
        
    	ButtonGroup group = new ButtonGroup();
       	group.add(privateBox);
       	group.add(publicBox);
       	
       	idLabel = new JLabel();
       	idLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
       	idLabel.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
       	f = idLabel.getFont();
       	idLabel.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
    	namePane = createTextPane();
    	descriptionPane = createTextPane();
    	descriptionPane.setLineWrap(true);
    	f = namePane.getFont();
    	namePane.setFont(f.deriveFont(Font.BOLD, f.getSize()+2));
    	f = descriptionPane.getFont();
    	descriptionPane.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
    	descriptionPane.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
    	channelsArea = UIUtilities.createComponent(null);
    }   
  
    /**
     * Builds the properties component.
     * 
     * @return See above.
     */
    private JPanel buildProperties()
    {
    	 JPanel p = new JPanel();
         p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
         p.add(namePane);
         p.add(Box.createVerticalStrut(5));
         JPanel l = UIUtilities.buildComponentPanel(idLabel, 0, 0);
         l.setBackground(UIUtilities.BACKGROUND_COLOR);
         p.add(l);
         p.add(Box.createVerticalStrut(5));
         p.add(descriptionPane);
         p.setBackground(UIUtilities.BACKGROUND_COLOR);
         p.add(Box.createVerticalStrut(5));
         p.add(buildPermissions(null));
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
    	if (!(refObject instanceof ImageData)) return;
    	PixelsData data = ((ImageData) refObject).getDefaultPixels();
    	add(Box.createVerticalStrut(5));
    	add(buildContentPanel(EditorUtil.transformPixelsData(data)));
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link EditorModel}.
     * 				Mustn't be <code>null</code>.                            
     */
    PropertiesUI(EditorModel model)
    {
       super(model);
       title = TITLE;
       initComponents();
       buildGUI();
    }   

    /**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		namePane.getDocument().removeDocumentListener(this);
		descriptionPane.getDocument().removeDocumentListener(this);
		originalName = model.getRefObjectName();
		namePane.setText(originalName);
		originalDisplayedName = EditorUtil.getPartialName(originalName);
		namePane.setText(originalDisplayedName);
		namePane.setToolTipText(originalName);
		idLabel.setText(ID_TEXT+model.getRefObjectID());
		originalDescription = model.getRefObjectDescription();
		if (originalDescription == null || originalDescription.length() == 0)
			originalDescription = DEFAULT_DESCRIPTION_TEXT;
		descriptionPane.setText(originalDescription);
        boolean b = model.isCurrentUserOwner(model.getRefObject());
        namePane.setEnabled(b);
        descriptionPane.setEnabled(b);
        if (b) {
        	namePane.getDocument().addDocumentListener(this);
        	descriptionPane.getDocument().addDocumentListener(this);
        }
        if (model.getRefObject() instanceof TagAnnotationData) {
        	namePane.getDocument().removeDocumentListener(this);
        	namePane.setEnabled(false);
        }
        buildGUI();
	}
	
	/** Sets the description of the object if the object is a tag annotation. */
	void setObjectDescription()
	{
		if (!(model.getRefObject() instanceof TagAnnotationData))  return;
		boolean b = model.isCurrentUserOwner(model.getRefObject());
		if (b)
			descriptionPane.getDocument().removeDocumentListener(this);
		Map<Long, List> annotations = model.getTextualAnnotationByOwner();
		long userID = MetadataViewerAgent.getUserDetails().getId();
		List l = annotations.get(userID);
		if (l != null && l.size() > 0) {
			TextualAnnotationData data = (TextualAnnotationData) l.get(0);
			descriptionPane.setText(data.getText());
			originalDescription = descriptionPane.getText();
		}
		if (b)
			descriptionPane.getDocument().addDocumentListener(this);
	}
	
    /** Sets the focus on the name area. */
	void setFocusOnName() { namePane.requestFocus(); }
   
	/** Updates the data object. */
	void updateDataObject() 
	{
		if (!hasDataToSave()) return;
		Object object =  model.getRefObject();
		String name = namePane.getText().trim();
		String desc = descriptionPane.getText().trim();
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
			p.setTagDescription(desc);
		} else if (object instanceof ScreenData) {
			ScreenData p = (ScreenData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(desc);
		} else if (object instanceof PlateData) {
			PlateData p = (PlateData) object;
			if (name.length() > 0) p.setName(name);
			p.setDescription(desc);
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
	 * @param waves The value to set.
	 */
	void setChannelData(List waves)
	{
		if (waves == null) return;
		String s = "";
		Iterator k = waves.iterator();
		int j = 0;
		while (k.hasNext()) {
			s += ((ChannelData) k.next()).getEmissionWavelength();
			if (j != waves.size()-1) s +=", ";
			j++;
		}
		channelsArea.setText(s);
		channelsArea.revalidate();
		channelsArea.repaint();
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return TITLE; }

	/**
	 * No-op implementation in this case.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove() { return null; }

	/**
	 * No-op implementation in this case.
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
		String name = originalName;//model.getRefObjectName().trim();
		String value = namePane.getText();
		value = value.trim();
		if (name == null) return false;
		if (!name.equals(value) && !originalDisplayedName.equals(value))
			return true;
		
		name = originalDescription;//model.getRefObjectDescription();
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
		//namePane.getDocument().removeDocumentListener(this);
		//descriptionPane.getDocument().removeDocumentListener(this);
		idLabel.setText("");
		namePane.setText(model.getRefObjectName());
		descriptionPane.setText(model.getRefObjectDescription());
		//namePane.getDocument().addDocumentListener(this);
		//descriptionPane.getDocument().addDocumentListener(this);
		originalName = namePane.getText();
		originalDescription = descriptionPane.getText();
		channelsArea.setText("");
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		
	}

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
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
