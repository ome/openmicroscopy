/*
 * adminTool.UserList 
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
package src.adminTool.ui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import src.adminTool.model.Model;


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
public class UserList 
	extends JPanel
{
	private		JList				users;
	private 	DefaultListModel	listModel;
	
	private		Model				model;
	private 	UserListController  controller;
	
	private 	boolean 			filterByGroup;
	private		String				groupName;
	
	public void clear()
	{
		listModel.clear();
	}
	
	public void filterOff()
	{
		filterByGroup = false;
	}
	
	public void filterUsersByGroup(String name)
	{
		filterByGroup = true;
		groupName = name;
		refresh();
	}
	
	public void refresh()
	{
		List data;
		if(filterByGroup)
			data = model.getUsersNotInGroup(groupName);
		else
			data = model.getUserList();
		listModel.clear();
		for( int i = 0 ; i < data.size() ; i++)
			listModel.add(i, data.get(i));
		users.setModel(listModel);
	}
	
	public String getSelectedUser()
	{
		if(listModel.size() == 0 )
			return null;
		if(users.getLeadSelectionIndex()<0)
			return null;
		if(users.getLeadSelectionIndex()<listModel.size())
			return  (String)listModel.get(users.getLeadSelectionIndex());
		else
			return null;
	}
	
	public UserList(Model model)
	{
		listModel = new DefaultListModel();
		filterByGroup = false;
		this.model = model;
		createUserList();
		buildUI();
	}
	
	public void setController(UserListController control)
	{
		this.controller = control;
		users.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting())
				{
					JList list = (JList)e.getSource();
					if(list.getLeadSelectionIndex() >= 0 && 
							list.getLeadSelectionIndex() < listModel.size() )
						controller.userSelected((String)listModel.get(list.getLeadSelectionIndex()));
				}
			}
		});
	}
	
	void createUserList()
	{
		users = new JList(listModel);
		users.setCellRenderer(new UserListRenderer(model));
		
		refresh();
		
		users.setPreferredSize(new Dimension(200,7*22));
		users.setMinimumSize(new Dimension(200,7*22));
		users.setMaximumSize(new Dimension(200,7*22));
		users.setPreferredSize(new Dimension(200,7*22));
	}
		
	void buildUI()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		panel.add(users, BorderLayout.CENTER);
		this.add(panel);
	}
	
}


