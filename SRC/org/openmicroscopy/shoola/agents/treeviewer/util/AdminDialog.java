/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.AdminDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Dialog used to create <code>Group</code> or <code>Experimenter</code>.
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
public class AdminDialog 
	extends JDialog
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating to create the object. */
	public static final String CREATE_PROPERTY = "create";
	
	/** Bound property indicating to enable of not the save property. */
	static final String ENABLE_SAVE_PROPERTY = "enableSave";
	
	/** The title of the dialog if the object to create is a group. */
	private static final String TITLE_GROUP = "Create Group";
	
	/** The title of the dialog if the object to create is an group.. */
	private static final String TITLE_EXPERIMENTER = "Create Experimenter";
	
	/** The text displayed if the object to create is a group. */
	private static final String TEXT_GROUP = "Create a new group";
	
	/** The text displayed if the object to create is a group. */
	private static final String TEXT_EXPERIMENTER = 
		"Create a new Experimenter. Add him/her to ";
	
	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** Action ID to close the dialog. */
	private static final int CANCEL = 0;
	
	/** Action ID to save the data. */
	private static final int SAVE = 1;
	
	/** Button to close the dialog. */
	private JButton cancel;
	
	/** Button to close the dialog. */
	private JButton save;
	
	/** The type of object to create. */
	private Class	type;
	
	/** The parent of the data object. */
	private Object parent;
	
	/** 
	 * The main component displaying parameters required to 
	 * create a group or an experimenter. 
	 */
	private DataPane body;
	
	/** Initializes the components. */
	private void initComponents()
	{
		cancel = new JButton("Cancel");
		cancel.setActionCommand(""+CANCEL);
		cancel.addActionListener(this);
		save = new JButton("Create");
		save.setActionCommand(""+SAVE);
		save.addActionListener(this);
		save.setEnabled(false);
	}

	/** Closes and disposes of the dialog. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** Saves the data. */
	private void save()
	{
	}
	
    
	/**
	 * Sets the property of the dialog.
	 * 
	 * @param type The type to handle.
	 */
	private void setProperties(Class type)
	{
		setModal(true);
		if (GroupData.class.equals(type)) setTitle(TITLE_GROUP);
		else if (ExperimenterData.class.equals(type)) 
			setTitle(TITLE_EXPERIMENTER);
		else new IllegalArgumentException("Type not supported/");
	}
	
	/** 
	 * Creates the header.
	 * 
	 * @return See above.
	 */
	private TitlePanel createHeader()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = null;
		if (GroupData.class.equals(type)) {
			tp = new TitlePanel(getTitle(), TEXT_GROUP, 
					icons.getIcon(IconManager.CREATE_48));
		} else if (ExperimenterData.class.equals(type)) {
			String text = TEXT_EXPERIMENTER;
			if (parent instanceof GroupData) {
				text += ((GroupData) parent).getName();
			}
			tp = new TitlePanel(getTitle(), text, 
					icons.getIcon(IconManager.CREATE_48));
		}
		return tp;
	}
	
	/** 
	 * Builds and lays out the buttons.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.add(save);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		bar.add(cancel);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(createHeader(), BorderLayout.NORTH);
		c.add(body, BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the frame.
	 * @param type  The type of object to create.
	 * @param parent The parent of the data object or <code>null</code>.
	 */
	public AdminDialog(JFrame owner, Class type, Object parent)
	{
		super(owner);
		setProperties(type);
		this.type = type;
		this.parent = parent;
		if (ExperimenterData.class.equals(type)) {
			body = new ExperimenterPane(false);
		} else if (GroupData.class.equals(type)) {
			body = new GroupPane();
		}
		body.addPropertyChangeListener(this);
			
		initComponents();
		buildGUI();
		pack();
	}
	
	/**
	 * Reacts to click on control.
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
	 * Reacts to property fired the component used to edit a group or 
	 * an experimenter.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ENABLE_SAVE_PROPERTY.equals(name)) {
			save.setEnabled((Boolean) evt.getNewValue());
		}
		
	}
	
	
}
