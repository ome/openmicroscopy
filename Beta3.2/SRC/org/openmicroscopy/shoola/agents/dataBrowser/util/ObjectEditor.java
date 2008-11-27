/*
 * org.openmicroscopy.shoola.agents.dataBrowser.util.ObjectEditor 
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
package org.openmicroscopy.shoola.agents.dataBrowser.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;

/** 
 * Modal dialog used to create an data object.
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
public class ObjectEditor 
	extends JDialog
	implements ActionListener, DocumentListener
{

	/** Bound property indicating to create the object. */
	public static final String		CREATE_DATAOBJECT_PROPERTY = 
														"createDataObject";
	
	/** Index indicating to create a new dataset. */
	public static final int			DATASET = 0;
	
	/** The title of the dialog. */
	private static final String 	TITLE = "New...";
	
    /** The default size of the dialog. */
    private static final Dimension 	WIN_DIM = new Dimension(400, 300);
   
	/** Action ID indicating to close the dialog. */
	private static final int		CANCEL = 10;

	/** Action ID indicating to close the dialog. */
	private static final int		SAVE = 11;
	
	/** One of the creation type defined by this class. */
	private int 			index;
	
	/** Button to close the dialog. */
	private JButton			cancelButton;
	
	/** Button to create a new data object. */
	private JButton			saveButton;
	
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField		nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea		descriptionArea;
    
    /** Button indicating to add the selected images to the dataset. */
    private JRadioButton	selectedImages;
    
    /** Button indicating to add the displayed images to the dataset. */
    private JRadioButton	displayedImages;
    
	/** Sets the properties. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
	}
	
	/**
	 * Controls if the passed index is supported.
	 * 
	 * @param value The value to check.
	 */
	private void checkIndex(int value)
	{
		switch (value) {
			case DATASET:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		ButtonGroup group = new ButtonGroup();
		selectedImages = new JRadioButton("Add Selected Images");
		group.add(selectedImages);
		displayedImages = new JRadioButton("Add Available Images");
		group.add(displayedImages);
		displayedImages.setSelected(true);
		nameArea = new JTextField();
		nameArea.getDocument().addDocumentListener(this);
        UIUtilities.setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Close.");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);
        saveButton = new JButton("Create");
        switch (index) {
			case DATASET:
				saveButton.setToolTipText("Create a new dataset.");
				break;
		}
        saveButton.addActionListener(this);
        saveButton.setActionCommand(""+SAVE);
        saveButton.setEnabled(false);
        getRootPane().setDefaultButton(saveButton);
	}
	
    /**
     * Builds the panel hosting the title according to the 
     * <code>DataObject</code>.
     * 
     * @return See above.
     */
    private TitlePanel buildTitlePanel()
    {
        TitlePanel tp = null;
        IconManager icons = IconManager.getInstance();
        switch (index) {
			case DATASET:
				tp = new TitlePanel("Create dataset", "Create a new dataset", 
	                    icons.getIcon(IconManager.CREATE_48));
				break;
	
			default:
				break;
		}
       return tp;
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
     * Builds the component with the various options.
     * 
     * @return See above.
     */
    private JPanel buildOptionsPanel()
    {
    	JPanel content = new JPanel();
    	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    	content.add(selectedImages);
    	content.add(displayedImages);
    	return content;
    }
    
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
        c.setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
    	contentPanel.setLayout(new BoxLayout(contentPanel, 
    								BoxLayout.Y_AXIS));
    	contentPanel.add(buildContentPanel());
    	contentPanel.add(buildOptionsPanel());
    	c.add(buildTitlePanel(), BorderLayout.NORTH);
        c.add(contentPanel, BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** Closes the dialog. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** Fires a property to create a new data object. */
	private void save()
	{
		DataObject object = null;
		switch (index) {
			case DATASET:
				DatasetData d = new DatasetData();
				d.setName(nameArea.getText().trim());
				d.setDescription(descriptionArea.getText().trim());
				object = d;
				break;
		}
		if (object != null) {
			List<Object> r = new ArrayList<Object>(2);
			boolean visible = true;
			if (selectedImages.isSelected()) visible = false;
			r.add(visible);
			r.add(object);
			firePropertyChange(CREATE_DATAOBJECT_PROPERTY, null, r);
		}
		cancel();
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
	 * @param owner The owner of the dialog.
	 * @param index	One of constants defined by this class.
	 */
	public ObjectEditor(JFrame owner, int index)
	{
		super(owner);
		setProperties();
		checkIndex(index);
		this.index = index;
		initComponents();
		buildGUI();
		setSize(WIN_DIM);
	}

	/**
	 * Closes or saves the data object.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				cancel();
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
