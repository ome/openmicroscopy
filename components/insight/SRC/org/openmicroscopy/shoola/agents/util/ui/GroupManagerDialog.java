/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.GroupData;
/** 
 * Selects the groups to add to the display. At least one group must be selected.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class GroupManagerDialog 
	extends JDialog
	implements ActionListener
{

	/** Bounds property indicating that groups have been selected. */
	public static final String GROUP_SWITCH_PROPERTY = "groupSwitch";
	
	/** The window's title. */
	public static final String TITLE = "Groups Selection";
	
	/** Action command ID indicating to close the window. */
	private static final int CANCEL = 0;
	
	/** Action command ID indicating to apply the selection. */
	private static final int APPLY = 1;
	
	/** The window's description. */
	private static final String TEXT = "Select at least one group.";
	
	/** The description of the {@link #cancel} button. */
	private static final String CANCEL_DESCRIPTION = "Close the window.";
	
	/** The description of the {@link #apply} button. */
	private static final String APPLY_DESCRIPTION = "Select the experimenters.";
	
	/** The component hosting the users for a given group. */
	private SelectionTable groupsTable;
	
	/** Button to close without applying the selection. */
	private JButton cancel;
	
	/** Button to apply the selection. */
	private JButton apply;
	
	/** The groups to display.*/
	private Collection groups;
	
	/** Closes and disposes. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Builds the tool bar hosting the {@link #cancel} and {@link #apply}
	 * buttons.
	 * 
	 * @return See above;
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.setBorder(null);
		bar.add(apply);
		bar.add(Box.createRigidArea(UserManagerDialog.H_SPACER_SIZE));
		bar.add(cancel);
		JPanel p =  UIUtilities.buildComponentPanelRight(bar);
		return p;
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param icon The icon displayed in the title panel.
	 */
	private void buildGUI(Icon icon)
	{
		TitlePanel titlePanel = new TitlePanel(TITLE, TEXT, icon);
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(titlePanel, BorderLayout.NORTH);
		c.add(new JScrollPane(groupsTable), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** 
	 * Initializes the UI components. 
	 * 
	 * @param selectedGroups The selected groups.
	 */
	private void initComponents(List<GroupData> selectedGroups)
	{
		cancel = new JButton("Cancel");
		cancel.setToolTipText(
				UIUtilities.formatToolTipText(CANCEL_DESCRIPTION));
		cancel.addActionListener(this);
		cancel.setActionCommand(""+CANCEL);
		apply = new JButton("Apply");
		apply.addActionListener(this);
		apply.setActionCommand(""+APPLY);
		//apply.setEnabled(false);
		apply.setToolTipText(
				UIUtilities.formatToolTipText(APPLY_DESCRIPTION));
		getRootPane().setDefaultButton(apply);
		ViewerSorter sorter = new ViewerSorter();
		List<GroupData> l = (List<GroupData>) sorter.sort(groups);
		groupsTable = new SelectionTable();
		groupsTable.setGroups(l);
		List<Long> ids = new ArrayList<Long>(selectedGroups.size());
		Iterator<GroupData> i = selectedGroups.iterator();
		while (i.hasNext()) {
			ids.add(i.next().getId());
		}
		DefaultTableModel model = (DefaultTableModel) groupsTable.getModel();

		i = l.iterator();
		GroupData g;
		int index = 0;
		while (i.hasNext()) {
			g = i.next();
			model.insertRow(index, new Object[]{g, 
					new Boolean(ids.contains(g.getId()))});
			index++;
		}
		ListSelectionListener listener = new ListSelectionListener() {
			
			/** 
			 * Listen to the selection of the nodes.
			 * @see ListSelectionListener#valueChanged(ListSelectionEvent)
			 */
			public void valueChanged(ListSelectionEvent evt)
			{
				int rows = groupsTable.getModel().getRowCount();
				int count =  0;
				for (int j = 0; j < rows; j++) {
					if ((Boolean) groupsTable.getValueAt(j, 1))
						count++;
				}
				apply.setEnabled(count != 0);
			}
		};
		groupsTable.getSelectionModel().addListSelectionListener(listener);
		groupsTable.getColumnModel().getSelectionModel().
		addListSelectionListener(listener);
	}
	
	/** Sets the groups. */
	private void apply()
	{
		List<GroupData> groups = new ArrayList<GroupData>();
		//Check the selected users
		Boolean b;
		for (int i = 0; i < groupsTable.getRowCount(); i++) {
			b = (Boolean) groupsTable.getValueAt(i, 1);
			if (b.booleanValue()) {
				groups.add((GroupData) groupsTable.getValueAt(i, 0));
			}
		}
		firePropertyChange(GROUP_SWITCH_PROPERTY, null, groups);
		cancel();
	}
	
	/** Sets the properties of the window. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of this dialog.
	 * @param selectedGroups The collection of group already displayed.
	 * @param icon The icon displayed in the title panel.
	 */
	public GroupManagerDialog(JFrame parent, 
			Collection groups, List<GroupData> selectedGroups,
			Icon icon)
	{
		super(parent);
		setProperties();
		this.groups = groups;
		initComponents(selectedGroups);
		buildGUI(icon);
	}
	
	/** Sets the default size of window. */
	public void setDefaultSize()
	{
		setSize(UserManagerDialog.DEFAULT_SIZE);
	}
	
	/**
	 * Performs the actions.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int id = Integer.parseInt(e.getActionCommand());
		switch (id) {
			case CANCEL:
				cancel();
				break;
			case APPLY:
				apply();
		}
	}
	
}
