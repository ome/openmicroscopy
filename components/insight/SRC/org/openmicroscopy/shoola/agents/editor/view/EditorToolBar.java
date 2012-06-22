/*
 * org.openmicroscopy.shoola.agents.editor.view.EditorToolBar 
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
package org.openmicroscopy.shoola.agents.editor.view;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.actions.PersonalManagementAction;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageObject;
import org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageRenderer;

import pojos.GroupData;

/** 
 * The tool bar of {@link Editor}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class EditorToolBar 
	extends JPanel
{
	
	/** Reference to the <code>Group Private</code> icon. */
	private static final Icon GROUP_PRIVATE_ICON;
	
	/** Reference to the <code>Group RWR---</code> icon. */
	private static final Icon GROUP_READ_ONLY_ICON;
	
	/** Reference to the <code>Group RWRA--</code> icon. */
	private static final Icon GROUP_READ_LINK_ICON;
	
	/** Reference to the <code>Group RWRW--</code> icon. */
	private static final Icon GROUP_READ_WRITE_ICON;
	
	/** Reference to the <code>Group</code> icon. */
	private static final Icon GROUP_PUBLIC_READ_ICON;
	
	/** Reference to the <code>Group</code> icon. */
	private static final Icon GROUP_PUBLIC_READ_WRITE_ICON;
	
	static { 
		IconManager icons = IconManager.getInstance();
		GROUP_PRIVATE_ICON = icons.getIcon(IconManager.PRIVATE_GROUP);
		GROUP_READ_ONLY_ICON = icons.getIcon(IconManager.READ_GROUP);
		GROUP_READ_LINK_ICON = icons.getIcon(IconManager.READ_LINK_GROUP);
		GROUP_READ_WRITE_ICON = icons.getIcon(IconManager.READ_WRITE_GROUP);
		GROUP_PUBLIC_READ_ICON = icons.getIcon(IconManager.PUBLIC_GROUP);
		GROUP_PUBLIC_READ_WRITE_ICON = icons.getIcon(
				IconManager.PUBLIC_GROUP);
	}
	
	/** The Actions that are displayed in the File toolbar */
	static Integer[] FILE_ACTIONS = {
						EditorControl.NEW_BLANK_FILE,
						EditorControl.OPEN_LOCAL_FILE, 
						EditorControl.OPEN_WWW_FILE,
						EditorControl.SAVE_FILE
						};

	/** The text indicating that the file is saved in the specified group.*/
	static final String SAVED = "Saved in";
	
	/** The text indicating to select the group where to select the group.*/
	static final String SAVE = "Save to";
	
	/** Reference to the Control. */
	private EditorControl controller;
	
	/** Reference to the View. */
	private EditorUI view;
	
	/** 
	 * The label indicating where to save the file or where the file is saved.
	 */
	private JLabel groupLabel;
	
	/** The component used to display the name of the group.*/
	private JComboBox groupButton;
	
	/** 
	 * Creates the bar.
	 * 
	 * @return See above.
	 */
	private JToolBar createBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setRollover(true);
		bar.setBorder(null);
		
		for (int i = 0; i < FILE_ACTIONS.length; i++) {
			addAction(FILE_ACTIONS[i], bar);
		}
		return bar;
	}
	
	/** 
	 * Creates the  management bar.
	 * 
	 * @return See above.
	 */
	private JPanel createManagementBar()
	{
		groupLabel = new JLabel();
		/*
		PersonalManagementAction a = (PersonalManagementAction)
		controller.getAction(EditorControl.PERSONAL);
		groupButton = new JButton(a);
        BorderFactory.createCompoundBorder(new EmptyBorder(2, 2, 2, 2), 
        		BorderFactory.createLineBorder(Color.GRAY));
        Collection l = EditorAgent.getAvailableUserGroups();
        if (l.size() > 1)
        	groupButton.addMouseListener(a);
        */
		Collection set = EditorAgent.getAvailableUserGroups();
		JComboBoxImageObject[] objects = new JComboBoxImageObject[set.size()];
        Iterator i = set.iterator();
        int index = 0;
        GroupData g;
        while (i.hasNext()) {
        	g = (GroupData) i.next();
        	objects[index] = new JComboBoxImageObject(g, getGroupIcon(g));
			index++;
		}
        groupButton = new JComboBox(objects);
        JComboBoxImageRenderer rnd = new JComboBoxImageRenderer();
        groupButton.setRenderer(rnd);
        rnd.setPreferredSize(new Dimension(200, 130));
        groupButton.setActionCommand(""+EditorControl.PERSONAL);
        groupButton.addActionListener(controller);
        
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.LEFT));
		bar.add(groupLabel);
		bar.add(groupButton);
		setGroupInformation();
		return bar;
	}

	/**
	 * Returns the icon associated to the group.
	 * 
	 * @param group The group to handle.
	 * @return See above.
	 */
	private Icon getGroupIcon(GroupData group)
	{
		switch (group.getPermissions().getPermissionsLevel()) {
	    	case GroupData.PERMISSIONS_PRIVATE:
	    		return GROUP_PRIVATE_ICON;
	    	case GroupData.PERMISSIONS_GROUP_READ:
	    		return GROUP_READ_ONLY_ICON;
	    	case GroupData.PERMISSIONS_GROUP_READ_LINK:
	    		return GROUP_READ_LINK_ICON;
	    	case GroupData.PERMISSIONS_GROUP_READ_WRITE:
	    		return GROUP_READ_WRITE_ICON;
	    	case GroupData.PERMISSIONS_PUBLIC_READ:
	    		return GROUP_PUBLIC_READ_ICON;
	    	case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
	    		return GROUP_PUBLIC_READ_WRITE_ICON;
		}
		return null;
	}
	
	/**
	 * Convenience method for getting an {@link Action} from the 
	 * {@link #controller}, creating a {@link CustomButton} and adding
	 * it to the component;
	 * 
	 * @param actionId Action ID, e.g. {@link EditorControl#CLOSE_EDITOR}
	 * @param comp The component to add the button. 
	 */
	private void addAction(int actionId, JComponent comp)
	{
		JButton b = new CustomButton(controller.getAction(actionId));
		b.setText("");
		comp.add(b);
	}

	/** Builds and lays out the UI. */
    private void buildGUI()
    {
    	JPanel toolBars = new JPanel();
    	toolBars.setBorder(null);
        toolBars.setLayout(new BoxLayout(toolBars, BoxLayout.X_AXIS));
        toolBars.add(createBar());
        Collection l = EditorAgent.getAvailableUserGroups();
        if (!view.isStandalone() && l.size() > 1)
        	toolBars.add(createManagementBar());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(toolBars);
    	/*
        JPanel bars = new JPanel(), outerPanel = new JPanel();
        bars.setBorder(null);
        bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
        bars.add(createManagementBar());
        //bars.add(createEditBar());
        bars.add(createSearchBar());
        outerPanel.setBorder(null);
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
        outerPanel.add(bars);
        outerPanel.add(Box.createRigidArea(HBOX));
        outerPanel.add(Box.createRigidArea(HBOX));
        outerPanel.add(Box.createHorizontalGlue());  
       
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
       
        add(UIUtilities.buildComponentPanel(outerPanel));
         */
    }

    /**
     * Creates a new instance.
     * 
     * @param controller Reference to the control. Mustn't be <code>null</code>.
     * @param view Reference to the view. Mustn't be <code>null</code>.
     */
	EditorToolBar(EditorControl controller, EditorUI view)
	{
		if (controller == null) 
			throw new NullPointerException("No controller.");
		if (view == null) 
			throw new NullPointerException("No view.");
		this.controller = controller;
		this.view = view;
		buildGUI();
	}
	
	/** 
	 * Sets the information about the group depending on the context and if the
	 * file has been saved or not.
	 */
	void setGroupInformation()
	{
		if (view.isStandalone()) return;
		if (groupLabel == null || groupButton == null) return;
		long id = view.getFileID();
		PersonalManagementAction a = (PersonalManagementAction) 
		controller.getAction(EditorControl.PERSONAL);
		if (id <= 0) {
			groupLabel.setText(SAVE);
			a.setPermissions();
		} else {
			groupLabel.setText(SAVED);
			groupButton.removeActionListener(controller);
			//groupButton.removeMouseListener(a);
		}
	}
	
}
