/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.MoveGroupSelectionDialog 
 *
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Selects the targets of the move group action.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MoveGroupSelectionDialog 
	extends JDialog
	implements ActionListener
{

	/** Action id to close and dispose.*/
	private static final int CANCEL = 0;
	
	/** Action id to move the data.*/
	private static final int MOVE = 1;
	
	/** Text displayed in the header.*/
	private static final String TEXT = "Select where to move the data into ";
	
	/** The default size of the busy image.*/
	private static final int SIZE = 48;
	
	/** The group to move the data to.*/
	private GroupData group;
	
	private List<DataObject> toMove;
	
	/** The list of possible targets.*/
	private List<DataObject> targets;
	
	/** The button to close and dispose.*/
	private JButton cancelButton;
	
	/** The button to move the data.*/
	private JButton moveButton;
	
	/** The component displayed in center of dialog.*/
	private JComponent body;
	
	/** The id of the user.*/
	private long userID;
	
	/** Closes and disposes.*/
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** Moves the data.*/
	private void move()
	{
		
		cancel();
	}
	
	/** Initializes the components.*/
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		moveButton = new JButton("Move");
		moveButton.addActionListener(this);
		moveButton.setActionCommand(""+MOVE);
	}
	
	/** 
	 * Builds and lays out the buttons.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.add(moveButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		bar.add(cancelButton);
		bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/** Builds and lays out the UI.*/
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(getTitle(), TEXT+group.getName(), 
				icons.getIcon(IconManager.MOVE_48));
		Container c = getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(tp, BorderLayout.NORTH);
		body = buildContent();
		c.add(body, BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** 
	 * Builds the main component of this dialog.
	 * 
	 * @param group The selected group if any.
	 * @return See above.
	 */
	private JPanel buildContent()
	{
		double[][] tl = {{TableLayout.FILL}, //columns
				{TableLayout.FILL}}; //rows
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		content.setLayout(new TableLayout(tl));
		JXBusyLabel label = new JXBusyLabel();
		label.setSize(WIDTH, WIDTH);
		content.add(label, "0, 0");
		return content;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param userID The identifier of the user.
	 * @param group The group where to move the data to.
	 * @param toMove The objects to move.
	 */
	public MoveGroupSelectionDialog(JFrame owner, long userID, GroupData group, 
			List<DataObject> toMove)
	{
		super(owner);
		if (group == null)
			throw new IllegalArgumentException("No group.");
		if (toMove == null || toMove.size() == 0)
			throw new IllegalArgumentException("No data to move.");
		this.group = group;
		this.toMove = toMove;
		this.userID = userID;
		initComponents();
		buildGUI();
	}

	/**
	 * Sets the values where to import the data.
	 * 
	 * @param targets The values to display.
	 */
	public void setTargets(List<DataObject> targets)
	{
		if (targets == null || targets.size() == 0) {
			body = new JLabel("No target to select.");
			repaint();
			return;
		}
		Set nodes = TreeViewerTranslator.transformHierarchy(targets, userID,
				-1);
	}
	
	/**
	 * Closes or moves the data.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				cancel();
				break;
			case MOVE:
				move();
		}
	}

}
