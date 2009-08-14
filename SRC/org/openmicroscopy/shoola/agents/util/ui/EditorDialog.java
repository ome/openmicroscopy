/*
 * org.openmicroscopy.shoola.agents.util.ui.EditorDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.util.ui;



//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Basic modal dialog brought up to create a new container.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class EditorDialog
    extends JDialog
    implements ActionListener, DocumentListener
{
	
	/** Indicates that the dialog is to create object. */
	public static final int			CREATE_TYPE = 0;
	
	/** Indicates that the dialog is to edit the object.*/
	public static final int			EDIT_TYPE = 1;
	
	/** Bound property indicating to create an object. */
	public static final String		CREATE_PROPERTY = "create";
	
	/** Bound property indicating to create an object. */
	public static final String		CREATE_NO_PARENT_PROPERTY = 
		"createNoParent";
	
	/** Bound property indicating to create an object. */
	public static final String		CLOSE_EDITOR_DIALOG_PROPERTY = 
		"closeEditorDialog";
	
    /** The default size of the dialog. */
    private static final Dimension 	WIN_DIM = new Dimension(600, 300);
   
    /** The default title of the window. */
    private static final String		TITLE = "Create";
    
    /** The default title of the window. */
    private static final String		TITLE_EDIT = "Edit";

    /** Action command ID to close the dialog. */
    private static final int		CANCEL = 0;
    
    /** Action command ID to create a new object. */
    private static final int		SAVE = 1;
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField          nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea          	descriptionArea;
    
    /** Button to close the dialog. */
    private JButton				cancelButton;
    
    /** Button to create a new item. */
    private JButton				saveButton;
    
    /** The objec to create. */
    private DataObject			data;
    
    /** Box used to indicate that the new object will have public visibility. */
    private JRadioButton		publicBox;
    
    /** Box used to indicate that the new object will have group visibility. */
    private JRadioButton		groupBox;
    
    /** Box used to indicate that the new object will be private. */
    private JRadioButton		privateBox;
    
    /** The type of object to create. */
    private String				typeName;
    
    /** The original text when editing. */
    private String				originalText;
    
    /** The original text when editing. */
    private String				originalDescription;
    
    /** 
     * Sets to <code>true</code> if the object will have a parent,
     * <code>false</code> otherwise. 
     */ 
    private boolean				withParent;
    
    /** The type of dialog, either create or edit. */
    private int					type;
    
    /**
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @return See above.
     */
    private JPanel buildPermissions()
    {
        JPanel content = new JPanel();
    	content.add(privateBox);
    	content.add(groupBox);
       	content.add(publicBox);
        return content;
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	publicBox = new JRadioButton(EditorUtil.PUBLIC);
    	publicBox.setEnabled(false);
    	groupBox = new JRadioButton(EditorUtil.GROUP_VISIBLE);
    	groupBox.setEnabled(false);
        privateBox =  new JRadioButton(EditorUtil.PRIVATE);
        privateBox.setSelected(true);
        privateBox.setEnabled(false);
        
        nameArea = new JTextField();
        descriptionArea = new MultilineLabel();
        descriptionArea.setEditable(true);
        originalText = "";
        originalDescription = "";
        if (type == EDIT_TYPE) {
        	originalText = getDataName();
        	originalDescription = getDataDescription();
        	nameArea.setText(originalText);
        	descriptionArea.setText(originalDescription);
        	descriptionArea.getDocument().addDocumentListener(this);
        }
        nameArea.getDocument().addDocumentListener(this);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Close the dialog.");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);
       
        if (type == EDIT_TYPE) {
        	 saveButton = new JButton("Save");
        	saveButton.setToolTipText("Edit the object.");
        } else {
        	 saveButton = new JButton("Create");
        	saveButton.setToolTipText("Create a new object.");
        }
        saveButton.addActionListener(this);
        saveButton.setActionCommand(""+SAVE);
        saveButton.setEnabled(false);
        getRootPane().setDefaultButton(saveButton);
        addWindowListener(new WindowAdapter()
        {
        	public void windowOpened(WindowEvent e) { nameArea.requestFocus(); } 
        });
    }   
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. If the <code>DataOject</code>
     * can be annotated and if we are in the 
     * {@link Editor#PROPERTIES_EDITOR} mode, we display the annotation pane. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
        int height = 80;
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, 5, height}};//, 5, 
        				//TableLayout.PREFERRED} }; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
       
        content.add(UIUtilities.setTextFont("Name"), "0, 0, l, t");
        content.add(nameArea, "1, 0");
        content.add(UIUtilities.setTextFont("Description"), "0, 2, l, t");
        content.add(new JScrollPane(descriptionArea), "1, 2");
        //content.add(buildPermissions(), "1, 4, l, t");
        return content;
    }
    
    /**
     * Builds and lays out the buttons.
     * 
     * @return See above.
     */
    public JPanel buildToolBar()
    {
    	JPanel bar = new JPanel();
    	bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
    	bar.add(cancelButton);
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(saveButton);
    	bar.add(Box.createHorizontalStrut(10));
    	JPanel p = UIUtilities.buildComponentPanelRight(bar);
    	return p;
    }
    
    /**
     * Builds the panel hosting the title according to the 
     * <code>DataObject</code>.
     * 
     * @return See above.
     */
    private TitlePanel buildTitlePanel()
    {
        IconManager im = IconManager.getInstance();
        TitlePanel tp = null;
        Icon icon = im.getIcon(IconManager.CREATE_48);
        if (data instanceof ProjectData) {
        	typeName = "Project";
        	icon = im.getIcon(IconManager.PROJECT_48);
        } else if (data instanceof DatasetData) {
        	typeName = "Dataset";
        	icon = im.getIcon(IconManager.DATASET_48);
        } else if (data instanceof ScreenData) {
        	typeName = "Screen";
        	icon = im.getIcon(IconManager.SCREEN_48);
        } else if (data instanceof TagAnnotationData) {
        	typeName = "Tag";
        	icon = im.getIcon(IconManager.TAG_48);
        	String ns = ((TagAnnotationData) data).getNameSpace();
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
        		typeName = "Tag Set";
        		icon = im.getIcon(IconManager.TAG_SET_48);
        	}
        }
        if (type == CREATE_TYPE)
        	tp = new TitlePanel("Create "+typeName, 
        			"Create a new "+typeName+".", icon);
        else if (type == EDIT_TYPE)
        	tp = new TitlePanel("Edit "+typeName, "Edit the "+typeName+".", 
        			icon);
        return tp;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
    	contentPanel.setLayout(new BoxLayout(contentPanel, 
    								BoxLayout.Y_AXIS));
    	contentPanel.add(buildContentPanel());
    	c.add(buildTitlePanel(), BorderLayout.NORTH);
        c.add(buildContentPanel(), BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
    }
    
    /** Closes and disposes. */
    private void close()
    {
    	firePropertyChange(CLOSE_EDITOR_DIALOG_PROPERTY, Boolean.FALSE, 
    			Boolean.TRUE);
        setVisible(false);
        dispose();
    }
    
    /** Creates a new item. */
    private void save()
    {
    	String name = nameArea.getText();
    	if (name == null) return;
    	name = name.trim();
    	int n = name.length();
    	if (n == 0 || n > 256) return;
    	if (data instanceof ProjectData) {
    		ProjectData p  = (ProjectData) data;
			p.setName(name);
			p.setDescription(descriptionArea.getText().trim());
			data = p;
    	} else if (data instanceof DatasetData) {
    		DatasetData d = (DatasetData) data;
			d.setName(name);
			d.setDescription(descriptionArea.getText().trim());
			data = d;
    	} else if (data instanceof ScreenData) {
    		ScreenData d = (ScreenData) data;
			d.setName(name);
			d.setDescription(descriptionArea.getText().trim());
			data = d;
    	} else if (data instanceof TagAnnotationData) {
    		TagAnnotationData d = (TagAnnotationData) data;
    		d.setContent(name);
    		String text = descriptionArea.getText().trim();
    		if (text.length() > 0) d.setTagDescription(text);
			data = d;
    	}
    	if (withParent) firePropertyChange(CREATE_PROPERTY, null, data);
    	else firePropertyChange(CREATE_NO_PARENT_PROPERTY, null, data);
    	close();
    }
    
    /**
     * Checks if the object is supported.
     * 
     * @param object The type of object to create.
     */
    private void checkData(DataObject object)
    {
    	if (object == null)
    		throw new IllegalArgumentException("No object to create.");
    	if (object instanceof ProjectData) return;
    	if (object instanceof DatasetData) return;
    	if (object instanceof ScreenData) return;
    	if (object instanceof TagAnnotationData) return;
    	throw new IllegalArgumentException("Object not supported.");
    }
    
    /**
     * Returns the name of the data object.
     * 
     * @return See above.
     */
    private String getDataName()
    {
    	if (data instanceof ProjectData) 
    		return ((ProjectData) data).getName();
    	if (data instanceof DatasetData) 
    		return ((DatasetData) data).getName();
    	if (data instanceof ScreenData) 
    		return ((ScreenData) data).getName();
    	if (data instanceof TagAnnotationData) 
    		return ((TagAnnotationData) data).getTagValue();
    	return "";
    }
    
    /**
     * Returns the description of the data object.
     * 
     * @return See above.
     */
    private String getDataDescription()
    {
    	if (data instanceof ProjectData) 
    		return ((ProjectData) data).getDescription();
    	if (data instanceof DatasetData) 
    		return ((DatasetData) data).getDescription();
    	if (data instanceof ScreenData) 
    		return ((ScreenData) data).getDescription();
    	if (data instanceof TagAnnotationData) return "";
    	return "";
    }
    
    /** Sets the enabled flag of the {@link #saveButton}. */
    private void enableSave()
    {
    	String name = nameArea.getText();
    	String desc = descriptionArea.getText();
    	if (type == CREATE_TYPE) {
    		if (name == null) saveButton.setEnabled(false);
    		else {
    			name = name.trim();
            	int l = name.length();
            	saveButton.setEnabled(l > 0 && l < 256);
    		}
    	} else {
    		if (!originalText.equals(name)) {
    			name = name.trim();
            	int l = name.length();
            	saveButton.setEnabled(l > 0 && l < 256);
    		} else {
    			desc = desc.trim();
    			saveButton.setEnabled(!originalDescription.equals(desc));
    		}
    	}
    }
    
    
    /**
     * Creates a new instance.
     * 
     * @param owner			The owner of the frame.
     * @param data 			The type of object to create.
     * @param withParent 	Sets to <code>true</code> if the object will 
     * 						have a parent, <code>false</code> otherwise.
     * @param type			The type of the dialog. 
     */
    public EditorDialog(JFrame owner, DataObject data, boolean withParent, 
    		int  type)
    {
        super(owner);
        switch (type) {
        	case EDIT_TYPE:
        	this.type = type;
        	setTitle(TITLE_EDIT);
			break;
	        case CREATE_TYPE:
			default:
				this.type = CREATE_TYPE;
			setTitle(TITLE);
		}
        checkData(data);
        this.data = data;
        this.withParent = withParent;
        initComponents();
        buildGUI();
        setSize(WIN_DIM);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner			The owner of the frame.
     * @param data 			The type of object to create.
     * @param withParent 	Sets to <code>true</code> if the object will 
     * 						have a parent, <code>false</code> otherwise. 
     */
    public EditorDialog(JFrame owner, DataObject data, boolean withParent)
    {
        this(owner, data, withParent, CREATE_TYPE);
    }
    
    /**
     * Sets the description of the dialog.
     * 
     * @param description The description of the dialog.
     */
    public void setOriginalDescription(String description)
    {
    	if (description == null) return;
    	originalDescription = description;
    	descriptionArea.getDocument().removeDocumentListener(this);
    	descriptionArea.setText(description);
    	descriptionArea.getDocument().addDocumentListener(this);
    }
    
    /**
     * Creates a new item or closes the dialog.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				break;
			case SAVE:
				save();
		}
	}

	/**
	 * Enables the save button depending on the value entered for the name.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { enableSave(); }

	/**
	 * Enables the save button depending on the value entered for the name.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { enableSave(); }
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

}
