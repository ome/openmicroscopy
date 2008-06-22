/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.EditorDialog
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

package org.openmicroscopy.shoola.agents.treeviewer.view;



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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

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
class EditorDialog
    extends JDialog
    implements ActionListener, DocumentListener
{
	
	/** Bound property indicating to create an object. */
	static final String				CREATE_PROPERTY = "create";
	
	/** Bound property indicating to create an object. */
	static final String				CREATE_NO_PARENT_PROPERTY = "createNoParent";
	
    /** The default size of the dialog. */
    private static final Dimension 	WIN_DIM = new Dimension(600, 350);
   
    /** The default title of the window. */
    private static final String		TITLE = "Create";

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
    
    /** 
     * Sets to <code>true</code> if the object will have a parent,
     * <code>false</code> otherwise. 
     */ 
    private boolean				withParent;
    
    /**
     * Builds and lays out the panel displaying the permissions of the edited
     * file.
     * 
     * @return See above.
     */
    private JPanel buildPermissions()
    {
        JPanel content = new JPanel();
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED} }; //rows
        content.setLayout(new TableLayout(tl));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        //The owner is the only person allowed to modify the permissions.
        //boolean isOwner = model.isObjectOwner();
        //Owner
        JLabel label = UIUtilities.setTextFont(EditorUtil.OWNER);
        JPanel p = new JPanel();
        JCheckBox box =  new JCheckBox(EditorUtil.READ);
        box.setSelected(true);
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(EditorUtil.WRITE);
        box.setSelected(true);
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setUserWrite(source.isSelected());
               view.setEdit(true);
            }
        
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 0, l, c");
        content.add(p, "1, 0, l, c");  
        //Group
        label = UIUtilities.setTextFont(EditorUtil.GROUP);
        p = new JPanel();
        box =  new JCheckBox(EditorUtil.READ);
        box.setSelected(true);
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(EditorUtil.WRITE);
        box.setSelected(false);
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setGroupWrite(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 1, l, c");
        content.add(p, "1,1, l, c"); 
        //OTHER
        label = UIUtilities.setTextFont(EditorUtil.WORLD);
        p = new JPanel();
        box =  new JCheckBox(EditorUtil.READ);
        box.setSelected(true);
        /*
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldRead(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        box =  new JCheckBox(EditorUtil.WRITE);
        box.setSelected(false);
        /*
        box.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
               JCheckBox source = (JCheckBox) e.getSource();
               permissions.setWorldWrite(source.isSelected());
               view.setEdit(true);
            }
        });
        */
        //box.setEnabled(isOwner);
        box.setEnabled(false);
        p.add(box);
        content.add(label, "0, 2, l, c");
        content.add(p, "1, 2, l, c"); 
        return content;
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
        nameArea = new JTextField();
        UIUtilities.setTextAreaDefault(nameArea);
        nameArea.getDocument().addDocumentListener(this);
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Close the dialog.");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);
        saveButton = new JButton("Create");
        saveButton.setToolTipText("Create a new item.");
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
     * is annotable and if we are in the {@link Editor#PROPERTIES_EDITOR} mode,
     * we display the annotation pane. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
        int height = 80;
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, TableLayout.PREFERRED, 5, 
        				TableLayout.PREFERRED, height} }; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
       
        content.add(UIUtilities.setTextFont("Name"), "0, 1, l, c");
        content.add(nameArea, "1, 1, f, c");
        content.add(new JLabel(), "0, 2, 1, 2");
        content.add(UIUtilities.setTextFont("Description"), "0, 3, l, c");
        content.add(new JScrollPane(descriptionArea), "1, 3, 1, 4");
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
    	return UIUtilities.buildComponentPanelRight(bar);
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
        if (data instanceof ProjectData) {
        	 tp = new TitlePanel("Create project", "Create a new project", 
                     im.getIcon(IconManager.CREATE_BIG));
        } else if (data instanceof DatasetData) {
        	 tp = new TitlePanel("Create dataset", "Create a new dataset", 
                     im.getIcon(IconManager.CREATE_BIG));
        }
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
        c.add(contentPanel, BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
    }
    
    /** Closes and disposes. */
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
    /** Creates a new item. */
    private void save()
    {
    	if (data instanceof ProjectData) {
    		ProjectData p  = (ProjectData) data;
			p.setName(nameArea.getText().trim());
			p.setDescription(descriptionArea.getText().trim());
			data = p;
    	} else if (data instanceof DatasetData) {
    		DatasetData d = (DatasetData) data;
			d.setName(nameArea.getText().trim());
			d.setDescription(descriptionArea.getText().trim());
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
    	if ((object instanceof ProjectData) || (object instanceof DatasetData))
    		return;
    	throw new IllegalArgumentException("Object not supported.");
    }
    
    /** Sets the enabled flag of the {@link #saveButton}. */
    private void enableSave()
    {
    	String name = nameArea.getText();
    	if (name == null) saveButton.setEnabled(false);
    	name = name.trim();
    	int l = name.length();
    	saveButton.setEnabled(l > 0 && l < 256);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner			The owner of the frame.
     * @param data 			The type of object to create.
     * @param withParent 	Sets to <code>true</code> if the object will 
     * 						have a parent, <code>false</code> otherwise. 
     */
    EditorDialog(JFrame owner, DataObject data, boolean withParent)
    {
        super(owner);
        setTitle(TITLE);
        checkData(data);
        this.data = data;
        this.withParent = withParent;
        initComponents();
        buildGUI();
        setSize(WIN_DIM);
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
