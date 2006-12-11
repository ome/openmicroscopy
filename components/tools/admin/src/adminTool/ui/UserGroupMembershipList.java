/*
 * adminTool.GroupList 
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
public class UserGroupMembershipList
	extends JPanel
{
	
		private		JList				groups;
		private 	DefaultListModel	listModel;
		private 	Model				model;

		
		
		public void clear()
		{
			listModel.clear();
		}
				
		public String getSelectedUserGroup()
		{
			if(listModel.size()==0)
				return null;
			if(groups.getLeadSelectionIndex()<0)
				return null;
			if(groups.getLeadSelectionIndex()<listModel.size())
				return  (String)listModel.get(groups.getLeadSelectionIndex());
			else
				return null;
		}
		
		public UserGroupMembershipList(Model model)
		{
			listModel = new DefaultListModel();
			this.model = model;
			createGroupList();
			buildUI();
		}
		
		void createGroupList()
		{
			groups = new JList(listModel);
			groups.setCellRenderer(new UserGroupMembershipListRenderer(model));
			groups.setPreferredSize(new Dimension(200,7*22));
			groups.setMinimumSize(new Dimension(200,7*22));
			groups.setMaximumSize(new Dimension(200,7*22));
			groups.setPreferredSize(new Dimension(200,7*22));
		}
		
		public void setUser(String userName)
		{
			listModel.clear();
			if(model.findUserByName(userName))
			{
				List data = model.getUserGroupMembership(model.getCurrentUserID());
				listModel.clear();
				for( int i = 0 ; i < data.size() ; i++)
					listModel.add(i,data.get(i));
			}
		}
		
		void buildUI()
		{
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(groups, BorderLayout.CENTER);
			panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			this.setLayout(new BorderLayout());
			this.add(panel);
		}

}


