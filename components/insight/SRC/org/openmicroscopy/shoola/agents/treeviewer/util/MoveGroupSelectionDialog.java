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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.env.data.model.TransferableActivityParam;
import org.openmicroscopy.shoola.env.data.model.TransferableObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.GroupData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

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
	public static final int CANCEL = 1;
	
	/** Action id to move the data.*/
	private static final int MOVE = 2;
	
	/** Text displayed in the header.*/
	private static final String TEXT = "Select where to move the data into ";
	
	/** The default size of the busy image.*/
	private static final Dimension SIZE = new Dimension(32, 32);
	
	/** The group to move the data to.*/
	private GroupData group;
	
	/** The data to move.*/
	private Map<SecurityContext, List<DataObject>> toMove;
	
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
	
	/** Sort the data.*/
	private ViewerSorter sorter;
	
	/** Indicate the status of the dialog.*/
	private int status;
	
	/** Only displayed the top node.*/
	private boolean topOnly;
	
	/** Handle the nodes to display.*/
	private JTree treeDisplay;
	
	/**
     * Adds the nodes to the specified parent.
     * 
     * @param parent The parent node.
     * @param nodes The list of nodes to add.
     * @param tm The  tree model.
     */
    private void buildTreeNode(TreeImageDisplay parent, 
                                Collection nodes, DefaultTreeModel tm)
    {
        Iterator i = nodes.iterator();
        TreeImageDisplay display;
        List children;
        DataObject data;
        while (i.hasNext()) {
            display = (TreeImageDisplay) i.next();
            display.setDisplayItems(false);
            data = (DataObject) display.getUserObject();
            if (!(topOnly && (data instanceof DatasetData ||
            		data instanceof PlateData))) {
            	tm.insertNodeInto(display, parent, parent.getChildCount());
                if (display instanceof TreeImageSet) {
                    children = display.getChildrenDisplay();
                    if (children.size() > 0) {
                    	buildTreeNode(display, 
                    			prepareSortedList(sorter.sort(children)), tm);
                    }
                }
            }
        }
    }
	/** Closes and disposes.*/
	private void cancel()
	{
		status = CANCEL;
		setVisible(false);
		dispose();
	}
	
	/** Moves the data.*/
	private void move()
	{
		SecurityContext ctx = new SecurityContext(group.getId());
		TreePath path = treeDisplay.getSelectionPath();
		DataObject target = null;
		if (path != null) {
			Object object = path.getLastPathComponent();
			if (object != null && object instanceof TreeImageDisplay) {
				target = (DataObject) 
				((TreeImageDisplay) object).getUserObject();
			}
		}
		TransferableObject t = new TransferableObject(ctx, target, toMove);
		t.setGroupName(group.getName());
		IconManager icons = IconManager.getInstance();
		TransferableActivityParam param = new TransferableActivityParam(
				icons.getIcon(IconManager.MOVE_22), t);
		param.setFailureIcon(icons.getIcon(IconManager.MOVE_FAILED_22));
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		un.notifyActivity(null, param);
		cancel();
	}
	
	/** Initializes the components.*/
	private void initComponents()
	{
		sorter = new ViewerSorter();
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		moveButton = new JButton("Move");
		moveButton.setEnabled(false);
		moveButton.addActionListener(this);
		moveButton.setActionCommand(""+MOVE);
		Entry entry;
		Iterator i = toMove.entrySet().iterator();
		List<DataObject> list;
		DataObject data;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			list = (List<DataObject>) entry.getValue();
			if (list != null && list.size() > 0) {
				data = list.get(0);
				if (data instanceof PlateData || data instanceof DatasetData) {
					topOnly = true;
					break;
				}
			}
		}
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
		//c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(tp, BorderLayout.NORTH);
		JXBusyLabel label = new JXBusyLabel(SIZE);
		label.setBusy(true);
		body = buildContent(label);
		c.add(body, BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
		setSize(400, 500);
	}
	
	/** 
	 * Builds the main component of this dialog.
	 * 
	 * @param group The selected group if any.
	 * @param comp The component to add.
	 * @return See above.
	 */
	private JComponent buildContent(JComponent comp)
	{
		double[][] tl = {{TableLayout.FILL}, //columns
				{TableLayout.FILL}}; //rows
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		content.setLayout(new TableLayout(tl));
		content.setBackground(UIUtilities.BACKGROUND);
		content.add(comp, "0, 0, CENTER, CENTER");
		return content;
	}
	
	/**
     * Organizes the sorted list so that the Project/Screen/Tag Set 
     * are displayed first.
     * 
     * @param sorted The collection to organize.
     * @return See above.
     */
    private List prepareSortedList(List sorted)
    {
    	List<TreeImageDisplay> top = new ArrayList<TreeImageDisplay>();
		List<TreeImageDisplay> bottom = new ArrayList<TreeImageDisplay>();
		
		List<TreeImageDisplay> top2 = new ArrayList<TreeImageDisplay>();
		List<TreeImageDisplay> bottom2 = new ArrayList<TreeImageDisplay>();
		
		Iterator j = sorted.iterator();
		TreeImageDisplay object;
		Object uo;
		while (j.hasNext()) {
			object = (TreeImageDisplay) j.next();
			uo = object.getUserObject();
			if (uo instanceof ProjectData) top.add(object);
			else if (uo instanceof ScreenData) top2.add(object);
			else if (uo instanceof DatasetData) bottom.add(object);
			else if (uo instanceof PlateData) bottom2.add(object);
			else if (uo instanceof PlateAcquisitionData) bottom2.add(object);
		}
		List<TreeImageDisplay> all = new ArrayList<TreeImageDisplay>();
		if (top.size() > 0) all.addAll(top);
		if (bottom.size() > 0) all.addAll(bottom);
		if (top2.size() > 0) all.addAll(top2);
		if (bottom2.size() > 0) all.addAll(bottom2);
		return all;
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
			Map<SecurityContext, List<DataObject>> toMove)
	{
		super(owner);
		if (group == null)
			throw new IllegalArgumentException("No group.");
		if (toMove == null || toMove.size() == 0)
			throw new IllegalArgumentException("No data to move.");
		setTitle("Move to "+group.getName());
		this.group = group;
		this.toMove = toMove;
		this.userID = userID;
		initComponents();
		buildGUI();
	}

	/**
	 * Returns the status of the dialog.
	 * 
	 * @return See above.
	 */
	public int getStatus() { return status; }
	
	/**
	 * Sets the values where to import the data.
	 * 
	 * @param targets The values to display.
	 */
	public void setTargets(Collection targets)
	{
		moveButton.setEnabled(true);
		Container c = getContentPane();
		c.remove(body);
		c.remove(1);
		if (targets == null || targets.size() == 0) {
			c.add(buildContent(new JLabel("No target to select.")),
					BorderLayout.CENTER);
			c.add(buildToolBar(), BorderLayout.SOUTH);
			validate();
			repaint();
			return;
		}
		treeDisplay = new JTree();
		treeDisplay.setVisible(true);
        treeDisplay.setRootVisible(false);
        ToolTipManager.sharedInstance().registerComponent(treeDisplay);
        treeDisplay.setCellRenderer(new TreeCellRenderer());
        treeDisplay.setShowsRootHandles(true);
        TreeImageSet root = new TreeImageSet("");
        treeDisplay.setModel(new DefaultTreeModel(root));
		Set nodes = TreeViewerTranslator.transformHierarchy(targets, userID,
				-1);
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		buildTreeNode(root, prepareSortedList(sorter.sort(nodes)), dtm);
		dtm.reload();
		c.add(new JScrollPane(treeDisplay), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
		validate();
		repaint();
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
