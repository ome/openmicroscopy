/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import info.clearthought.layout.TableLayout; 

import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
* Modal dialog presenting the existing user groups and 
* and the experimenters in each group. The user can then select and 
* view other people data.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* @since OME3.0
*/
public class UserManagerDialog 
	extends JDialog
	implements ActionListener
{

	/** Bounds property indicating that a new user has been selected. */
	public static final String		USER_SWITCH_PROPERTY = "userSwitch";
	
	/** Bounds property indicating that no user selected. */
	public static final String		NO_USER_SWITCH_PROPERTY = "noUserSwitch";
	
	/** The default size of the window. */
	static final Dimension	DEFAULT_SIZE = new Dimension(400, 400);
	
	/** The window's title. */
	private static final String		TITLE = "Users Selection";
	
	/** The window's description. */
	private static final String		TEXT = "Select the users.";
	
	/** The description of the {@link #cancel} button. */
	private static final String		CANCEL_DESCRIPTION = "Close the window.";
	
	/** The description of the {@link #apply} button. */
	private static final String		APPLY_DESCRIPTION = "Select the " +
			"users.";
	
	/** Action command ID indicating to close the window. */
	private static final int		CANCEL = 0;
	
	/** Action command ID indicating to apply the selection. */
	private static final int		APPLY = 1;

	/** 
	 * The size of the invisible components used to separate buttons
	 * horizontally.
	 */
	static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
  
	/** Button to close without applying the selection. */
	private JButton cancel;
	
	/** Button to apply the selection. */
	private JButton apply;

	/** The component hosting the users for a given group. */
	private JTable members;
	
	/** The component hosting the users for a given group. */
	private JTable owners;
	
	/** The current user. */
	private ExperimenterData loggedUser;
	
	/** Helper class uses to sort elements. */
	private ViewerSorter sorter;
	
	/** The group currently selected.*/
	private GroupData group;
	
	/** The collection of selected users.*/
	private List<ExperimenterData> selectedUsers;
	
	/** Closes and disposes. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** Switches the user. */
	private void apply()
	{
		Map<Long, List<ExperimenterData>> 
		r = new HashMap<Long, List<ExperimenterData>>();
		//Check the selected users
		Boolean b;
		List<ExperimenterData> users = new ArrayList<ExperimenterData>();
		for (int i = 0; i < owners.getRowCount(); i++) {
			b = (Boolean) owners.getValueAt(i, 1);
			if (b.booleanValue()) {
				users.add((ExperimenterData) owners.getValueAt(i, 0));
			}
		}
		
		for (int i = 0; i < members.getRowCount(); i++) {
			b = (Boolean) members.getValueAt(i, 1);
			if (b.booleanValue()) {
				users.add((ExperimenterData) members.getValueAt(i, 0));
			}
		}
		r.put(group.getId(), users);
		firePropertyChange(USER_SWITCH_PROPERTY, null, r);
		cancel();
	}
	
	/** Sets the properties of the window. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
	}
	
	/**
	 * Returns <code>true</code> if the experimenter is already displayed.
	 * <code>false</code> otherwise.
	 * 
	 * @param experimenter The experimenter to handle.
	 * @return See above.
	 */
	private boolean isAlreadyDisplayed(ExperimenterData experimenter)
	{
		Iterator<ExperimenterData> i = selectedUsers.iterator();
		ExperimenterData exp;
		long id = experimenter.getId();
		while (i.hasNext()) {
			exp = i.next();
			if (exp.getId() == id) return true;
		}
		return false;
	}

	/**
	 * Fills the users' list with the specified objects.
	 * 
	 * @param group The group to handle.
	 */
	private void fillList(GroupData group)
	{
		if (group == null) return;
		DefaultTableModel model = (DefaultTableModel) owners.getModel();
		int index = 0;
		List l = sorter.sort(group.getLeaders());
		Iterator i = l.iterator();
		ExperimenterData exp;
		List<Long> ids = new ArrayList<Long>();
		while (i.hasNext()) {
			exp = (ExperimenterData) i.next();
			if (exp.getId() != loggedUser.getId()) {
				model.insertRow(index, new Object[]{exp, 
						new Boolean(isAlreadyDisplayed(exp))});
				ids.add(exp.getId());
				index++;
			}
		}
		if (group.getLeaders().size() == group.getExperimenters().size())
			return;
		//model.add(index, "Members");
		model = (DefaultTableModel) members.getModel();
		l = sorter.sort(group.getExperimenters());
		i = l.iterator();
		index = 0;
		while (i.hasNext()) {
			exp = (ExperimenterData) i.next();
			if (exp.getId() != loggedUser.getId() &&
				!ids.contains(exp.getId())) {
				model.insertRow(index, new Object[]{exp, 
						new Boolean(isAlreadyDisplayed(exp))});
				index++;
			}
		}
	}
	
	/** Adds listeners. */
	private void attachListeners()
	{
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
		
			/** 
			 * Cancels while closing the window.
			 * @see WindowAdapter#windowClosing(WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		/*
		users.addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				Object value = users.getSelectedValue();
				apply.setEnabled(value instanceof ExperimenterData);
			}
		});
		*/
		cancel.setActionCommand(""+CANCEL);
		cancel.addActionListener(this);
		apply.setActionCommand(""+APPLY);
		apply.addActionListener(this);
	}
	
	/** 
	 * Initializes the UI components. 
	 * 
	 * @param selectedGroup The group to display
	 * @param userIcon	The icon used to represent an user.
	 */
	private void initComponents(Icon userIcon)
	{
		sorter = new ViewerSorter();
		cancel = new JButton("Cancel");
		cancel.setToolTipText(
				UIUtilities.formatToolTipText(CANCEL_DESCRIPTION));
		apply = new JButton("Apply");
		//apply.setEnabled(false);
		apply.setToolTipText(
				UIUtilities.formatToolTipText(APPLY_DESCRIPTION));
		getRootPane().setDefaultButton(apply);
		members = new SelectionTable(userIcon);
		owners = new SelectionTable(userIcon);
		fillList(group);
		attachListeners();
	}
	
	/** 
	 * Builds the main component of this dialog.
	 * 
	 * @param group The selected group if any.
	 * @return See above.
	 */
	private JPanel buildContent(GroupData group)
	{
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, 5, TableLayout.FILL}}; //rows
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		content.setLayout(new TableLayout(tl));
		if (group != null) {
			content.add(UIUtilities.setTextFont(
				"Select from: "+group.getName()), "0, 0, 1, 0");
		}
		//content.add(UIUtilities.setTextFont("Experimenters "),
		//		"0, 2, LEFT, TOP");
		int rows = owners.getRowCount();
		JPanel p = new JPanel();
		
		p.setBackground(UIUtilities.BACKGROUND);
		double[][] size = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 5,
			TableLayout.PREFERRED, TableLayout.PREFERRED}};
		p.setLayout(new TableLayout(size));
		if (rows > 0) {
			p.add(UIUtilities.setTextFont("Group's owners"), "0, 0");
			p.add(owners, "0, 1, 1, 1");
		}
		rows = members.getRowCount();
		if (rows > 0) {
			p.add(UIUtilities.setTextFont("Members"), "0, 3");
			p.add(members, "0, 4, 1, 4");
		}
		content.add(new JScrollPane(p), "0, 2, 1, 2");
		return content;
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
		//bar.add(mySelf);
		//bar.add(Box.createRigidArea(H_SPACER_SIZE));
		bar.add(apply);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		bar.add(cancel);
		JPanel p =  UIUtilities.buildComponentPanelRight(bar);
		return p;
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param group The selected group.
	 * @param icon The icon displayed in the title panel.
	 */
	private void buildGUI(GroupData group, Icon icon)
	{
		TitlePanel titlePanel = new TitlePanel(TITLE, TEXT, icon);
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(titlePanel, BorderLayout.NORTH);
		c.add(buildContent(group), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of this dialog.
	 * @param loggedUser The user currently logged in.
	 * @param selected The selected group.
	 * @param selectedUsers The collection of users already displayed.
	 * @param userIcon The icon representing an user.
	 * @param icon The icon displayed in the title panel.
	 */
	public UserManagerDialog(JFrame parent, ExperimenterData loggedUser, 
		GroupData selected, List<ExperimenterData> selectedUsers,
		Icon userIcon, Icon icon)
	{
		super(parent);
		setProperties();
		this.loggedUser = loggedUser;
		group = selected;
		if (selectedUsers == null)
			selectedUsers = new ArrayList<ExperimenterData>();
		this.selectedUsers = selectedUsers;
		initComponents(userIcon);
		buildGUI(selected, icon);
	}

	/**
	 * Returns the users already selected.
	 * 
	 * @return See above.
	 */
	List<ExperimenterData> getSelectedUsers()
	{
		return selectedUsers;
	}
	
	/** Sets the default size of window. */
	public void setDefaultSize()
	{
		setSize(DEFAULT_SIZE);
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
