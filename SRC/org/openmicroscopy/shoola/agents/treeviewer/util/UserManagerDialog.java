/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.UserManagerDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries
import layout.TableLayout;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.GroupData;

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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class UserManagerDialog 
	extends JDialog
{

	/** Bounds property indicating that a new user has been selected. */
	public static final String		USER_SWITCH_PROPERTY = "userSwitch";
	
	/** The window's title. */
	private static final String		TITLE = "Switch user";
	
	/** The window's description. */
	private static final String		TEXT = "Select a user and " +
											"view his/her data.";
	
	/** The description of the {@link #cancel} button. */
	private static final String		CANCEL_DESCRIPTION = "Close the window.";
	
	/** The description of the {@link #apply} button. */
	private static final String		APPLY_DESCRIPTION = "View selected " +
			"user's data.";
	
	/** The description of the {@link #mySelf} button. */
	private static final String		MYSELF_DESCRIPTION = "View my data.";
	
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** Button to close without applying the selection. */
	private JButton 					cancel;
	
	/** Button to apply the selection. */
	private JButton						apply;
	
	/** Button to select the logged in user. */
	private JButton						mySelf;
	
	/** The box hosting the groups. */
	private JComboBox					groups;
	
	/** The component hosting the users for a given group. */
	private JList						users;
	
	/** The current user. */
	private ExperimenterData			loggedUser;
	
	/** Helper class uses to sort elements. */
	private ViewerSorter				sorter;
	
	/** Map of ordered elements. */
	private Map<GroupData, Object[]>	orderedMap;
	
	/** Closes and disposes. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** Switches the user. */
	private void apply()
	{
		Map<Long, ExperimenterData> 
		r = new HashMap<Long, ExperimenterData>(1);
		GroupData g = (GroupData) groups.getSelectedItem();
		Object user = users.getSelectedValue();
		if (user == null) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo(TITLE, "Please select a user first.");
			return;
		}
		r.put(new Long(g.getId()), (ExperimenterData) user);
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
	 * Fills the users' list with the specified objects.
	 * 
	 * @param data The objects to add.
	 */
	private void fillList(Object[] data)
	{
		if (data == null) return;
		DefaultListModel model = (DefaultListModel) users.getModel();
		ExperimenterData d;
		int index = 0;
		for (int i = 0; i < data.length; i++) {
			d = (ExperimenterData) data[i];
			if (d.getId() != loggedUser.getId()) {
				model.add(index, d);
				index++;
			}	
        }
	}
	
	/** Adds listeners. */
	private void attachListeners()
	{
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { cancel(); }
		});
		apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { apply(); }
		});
		mySelf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{ 
				Map<Long, ExperimenterData> 
					r = new HashMap<Long, ExperimenterData>(1);
				r.put(new Long(loggedUser.getDefaultGroup().getId()), 
						loggedUser);
				firePropertyChange(USER_SWITCH_PROPERTY, null, r);
				cancel();
			}
		});
		groups.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e)
			{ 
				DefaultListModel model = (DefaultListModel) users.getModel();
				model.clear();
				fillList(orderedMap.get(groups.getSelectedItem()));
			}
		});
		users.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
		
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				apply.setEnabled(!lsm.isSelectionEmpty());
			}
		
		});
	}
	
	/** 
	 * Initializes the UI components. 
	 * 
	 * @param map		Map whose keys are the experimenter group and
	 * 					the values the collection of users in the
	 * 					corresponding experimenter groups.
	 * @param userID 	The id of the currently selected user.
	 */
	private void initComponents(Map map, long userID)
	{
		sorter = new ViewerSorter();
		orderedMap = new HashMap<GroupData, Object[]>();
		cancel = new JButton("Cancel");
		cancel.setToolTipText(
				UIUtilities.formatToolTipText(CANCEL_DESCRIPTION));
		apply = new JButton("Apply");
		apply.setEnabled(false);
		apply.setToolTipText(
				UIUtilities.formatToolTipText(APPLY_DESCRIPTION));
		getRootPane().setDefaultButton(apply);
		mySelf = new JButton("Myself");
		//IconManager im = IconManager.getInstance();
		//mySelf.setIcon(im.getIcon(IconManager.OWNER));
		mySelf.setToolTipText(
				UIUtilities.formatToolTipText(MYSELF_DESCRIPTION));
		mySelf.setEnabled(userID != loggedUser.getId());
		
		GroupData defaultGroup = loggedUser.getDefaultGroup();
		long groupID = defaultGroup.getId();
		//Build the array for box.
		Iterator i = map.keySet().iterator();
		//Remove not visible group
		GroupData g;
		GroupData[] objects = new GroupData[map.size()];
		int selectedIndex = 0;
		int index = 0;
		Object[] children;
		GroupData selectedGroup = defaultGroup;
		while (i.hasNext()) {
			g = (GroupData) i.next();
			objects[index] = g;
			if (g.getId() == groupID) {
				selectedIndex = index;
				selectedGroup = g;
			}
			children = sorter.sortArray((Set) map.get(g));
			orderedMap.put(g, children);
			index++;
		}
		//sort by name
		groups = new JComboBox(objects);
		groups.setRenderer(new GroupsRenderer());
		if (objects.length != 0)
			groups.setSelectedIndex(selectedIndex);
		
		DefaultListModel model = new DefaultListModel();
		users = new JList(model);
		fillList(orderedMap.get(selectedGroup));
		users.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		users.setLayoutOrientation(JList.VERTICAL);
		users.setCellRenderer(new UserListRenderer());		
	}
	
	/** 
	 * Builds the main component of this dialog.
	 * 
	 * @return See above.
	 */
	private JPanel buildContent()
	{
		double[][] tl = {{TableLayout.PREFERRED, 300}, //columns
 				{TableLayout.PREFERRED, 5,
			 	TableLayout.PREFERRED, 150}}; //rows
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        content.setLayout(new TableLayout(tl));
		//content.add(currentUser, "1, 0");
        JLabel label = UIUtilities.setTextFont("Groups");
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		content.add(label, "0, 0, l, c");
		content.add(groups, "1, 0, f, c");
		content.add(new JLabel(), "0, 1, 1, 1");
		label = UIUtilities.setTextFont("Users");
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		content.add(label, "0, 2, l, c");
		content.add(new JScrollPane(users), "1, 2, 1, 3");
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
		bar.add(mySelf);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		bar.add(cancel);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		bar.add(apply);
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager im = IconManager.getInstance();
		TitlePanel titlePanel = new TitlePanel(TITLE, TEXT, 
                im.getIcon(IconManager.OWNER_48));
		Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(titlePanel, BorderLayout.NORTH);
        c.add(buildContent(), BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent		The parent of this dialog.
	 * @param loggedUser	The user currently logged in.
	 * @param groups		Map whose keys are the experimenter group and
	 * 						the values the collection of users in the
	 * 						corresponding experimenter groups.
	 * @param userID		The id of the currently selected user.
	 */
	public UserManagerDialog(JFrame parent, ExperimenterData loggedUser, 
							Map groups, long userID)
	{
		super(parent);
		setProperties();
		this.loggedUser = loggedUser;
		initComponents(groups, userID);
		attachListeners();
		buildGUI();
	}
	
}
