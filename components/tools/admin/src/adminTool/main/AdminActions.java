/*
 * adminTool.adminActions 
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package src.adminTool.main;


//Java imports
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import src.adminTool.groupPanel.GroupsTab;
import src.adminTool.groupPanel.GroupsTabController;
import src.adminTool.model.IAdminException;
import src.adminTool.model.Model;
import src.adminTool.model.PermissionsException;
import src.adminTool.model.UnknownException;
import src.adminTool.omero.OMEROMetadataStore;
import src.adminTool.usersPanel.UsersTab;
import src.adminTool.usersPanel.UsersTabController;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AdminActions
	extends JPanel
	implements ChangeListener
{
	private JTabbedPane 			tabbedPane;
	private UsersTab 				usersTab;
	private GroupsTab 				groupsTab;
	private ServerTab 				serverTab;
	
	private Model					model;
	private UsersTabController 		userscontroller;
	private GroupsTabController 	groupscontroller;
	
	public AdminActions(OMEROMetadataStore store, String username) 	
	throws IAdminException, UnknownException, PermissionsException
	{
		model = new Model(store, username);
		createUIElements();
		setPermissions();
		buildUI();
	}
	
	
	void createUIElements()
	{
		createUsersTab();
		createGroupsTab();
		createServerTab();
		createTabbedPane();
	}
	
	void setPermissions()
	{
		if(!model.isSystemUser())
		{
			groupsTab.setEnabled(false);
			serverTab.setEnabled(false);
		}
		else
		{
			groupsTab.setEnabled(true);
			serverTab.setEnabled(true);			
		}
	}
	
	void createUsersTab()
	{
		usersTab = new UsersTab(model);
		userscontroller = new UsersTabController(model, usersTab);
		usersTab.setController(userscontroller);
	}
	
	void createGroupsTab()
	{
		groupsTab = new GroupsTab(model);
		groupscontroller = new GroupsTabController(model, groupsTab);
		groupsTab.setController(groupscontroller);
	}
	
	void createServerTab()
	{
		serverTab = new ServerTab();
	}
	
	void createTabbedPane()
	{
		tabbedPane = new JTabbedPane();
		tabbedPane.add("Users", usersTab);
		tabbedPane.add("Groups", groupsTab);
		tabbedPane.addChangeListener(this);
	//	tabbedPane.add("Server", serverTab);
	}
	
	void buildUI()
	{
		this.setLayout(new BorderLayout());
		this.add(tabbedPane, BorderLayout.CENTER);
	}


	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		    JTabbedPane pane = (JTabbedPane)e.getSource();
    
            int sel = pane.getSelectedIndex();
            usersTab.refresh();
            groupsTab.refresh();
            
	}
}


