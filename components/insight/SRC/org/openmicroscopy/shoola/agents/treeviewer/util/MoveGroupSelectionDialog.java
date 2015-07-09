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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

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
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries
import info.clearthought.layout.TableLayout;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.CreateFolderDialog;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
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
	implements ActionListener, PropertyChangeListener, TreeSelectionListener
{

	/** Bound property indicating to transfer the data.*/
	public static final String TRANSFER_PROPERTY = "transfer";
	
	/** Action id to close and dispose.*/
	public static final int CANCEL = 1;
	
	/** Action id to move the data.*/
	private static final int MOVE = 2;
	
	/** 
	 * Action id to create a new container e.g. Dataset. The supported types
	 * are Project, Dataset or Screen, depending on the objects to move.
	 */
	private static final int CREATE = 3;
	
	/** Text displayed in the header.*/
	private static final String TEXT = "Select where to move the data.";
	
	/** The default size of the busy image.*/
	private static final Dimension SIZE = new Dimension(32, 32);
	
	/** The object to handle.*/
	private ChgrpObject object;
	
	/** The button to close and dispose.*/
	private JButton cancelButton;
	
	/** The button to move the data.*/
	private JButton moveButton;
	
	/** The button to create a new container.*/
	private JButton createButton;
	
	/** The component displayed in center of dialog.*/
	private JComponent body;
	
	/** The id of the user.*/
	private long userID;
	
	/** Sort the data.*/
	private ViewerSorter sorter;
	
	/** Indicate the status of the dialog.*/
	private int status;

	/** Handle the nodes to display.*/
	private JTree treeDisplay;
	
	/** The type of container to create.*/
	private Class<?> containerType;
	
	/** The data object to create.*/
	private DataObject toCreate;
	
	/** Flag indicating that the message indicating that no node displayed.*/
	private boolean noDisplay;
	
	/**
	 * Creates a new container corresponding to {@link #containerType}.
	 * 
	 * @param name The name of the container.
	 */
	private void create(String name)
	{
		if (containerType == null) return;
		if (ProjectData.class.equals(containerType)) {
			toCreate = new ProjectData();
			((ProjectData) toCreate).setName(name);
		} else if (ScreenData.class.equals(containerType)) {
			toCreate = new ScreenData();
			((ScreenData) toCreate).setName(name);
		} else if (DatasetData.class.equals(containerType)) {
			toCreate = new DatasetData();
			((DatasetData) toCreate).setName(name);
		}
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
		TreeImageSet node = new TreeImageSet(toCreate);
		node.setDisplayItems(false);
		dtm.insertNodeInto(node, root, root.getChildCount());
		node = (TreeImageSet) root.getChildAt(root.getChildCount()-1);
		treeDisplay.setSelectionPath(new TreePath(node.getPath()));
		if (noDisplay) {
			noDisplay = false;
			Container c = getContentPane();
			c.remove(body);
			c.remove(1);
			c.add(new JScrollPane(treeDisplay), BorderLayout.CENTER);
			c.add(buildToolBar(), BorderLayout.SOUTH);
			validate();
			repaint();
			
		}
	}
	
	/** Brings up a dialog to create a new container.*/
	private void showNewFolder()
	{
		if (containerType == null) return;
		String title = "";
		String defaultName = "";
		if (ProjectData.class.equals(containerType)) {
			title = "New Project";
			defaultName = "untitled project";
		} else if (ScreenData.class.equals(containerType)) {
			title = "New Screen";
			defaultName = "untitled screen";
		} else if (DatasetData.class.equals(containerType)) {
			title = "New Dataset";
			defaultName = "untitled dataset";
		}
		CreateFolderDialog d = new CreateFolderDialog(this, title);
		d.setDefaultName(defaultName);
		d.addPropertyChangeListener(CreateFolderDialog.CREATE_FOLDER_PROPERTY,
				this);
		d.pack();
		UIUtilities.centerAndShow(this, d);
	}
	
	/**
     * Adds the nodes to the specified parent.
     * 
     * @param parent The parent node.
     * @param nodes The list of nodes to add.
     * @param tm The  tree model.
     */
    private void buildTreeNode(TreeImageDisplay parent, 
                       Collection<TreeImageDisplay> nodes, DefaultTreeModel tm)
    {
        Iterator<TreeImageDisplay> i = nodes.iterator();
        TreeImageDisplay display;
        List<TreeImageDisplay> children;
        while (i.hasNext()) {
            display = (TreeImageDisplay) i.next();
            display.setDisplayItems(false);
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
		DataObject target = null;
		if (toCreate != null) {
			target = toCreate;
		} else {
			TreePath path = treeDisplay.getSelectionPath();
			
			if (path != null) {
				Object object = path.getLastPathComponent();
				if (object != null && object instanceof TreeImageDisplay) {
					target = (DataObject) 
					((TreeImageDisplay) object).getUserObject();
				}
			}
		}
		ChgrpObject newObject = new ChgrpObject(object.getGroupData(),
				target, object.getTransferable());
		firePropertyChange(TRANSFER_PROPERTY, null, newObject);
		cancel();
	}
	
	/** Initializes the components.
	 * 
	 * @param same Pass <code>true</code> if the user moving the data and the 
	 * 			   owner of the data are the same person, <code>false</code>
	 */
	private void initComponents(boolean same)
	{
		containerType = null;
		sorter = new ViewerSorter();
		createButton = new JButton("New...");
		createButton.addActionListener(this);
		createButton.setActionCommand(""+CREATE);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		moveButton = new JButton("Move");
		moveButton.setEnabled(true);
		moveButton.addActionListener(this);
		moveButton.setActionCommand(""+MOVE);
		Entry<SecurityContext, List<DataObject>> entry;
		Iterator<Entry<SecurityContext, List<DataObject>>>
		i = object.getTransferable().entrySet().iterator();
		List<DataObject> list;
		DataObject data;
		while (i.hasNext()) {
			entry = i.next();
			list = entry.getValue();
			if (list != null && list.size() > 0) {
				data = list.get(0);
				if (data instanceof PlateData) {
					containerType = ScreenData.class;
				} else if (data instanceof DatasetData) {
					containerType = ProjectData.class;
				} else if (data instanceof ImageData) {
					containerType = DatasetData.class;
				}
			}
		}
		createButton.setEnabled(false);
		if (same) {
			createButton.setEnabled(containerType != null);
			createButton.setVisible(containerType != null);
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
		JPanel barLeft = new JPanel();
		barLeft.add(createButton);
		
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		
		row.add(UIUtilities.buildComponentPanel(barLeft));
		row.add(UIUtilities.buildComponentPanelRight(bar));
		return row;
	}
	
	/** Builds and lays out the UI.*/
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(getTitle(), TEXT,
				icons.getIcon(IconManager.MOVE_48));
		Container c = getContentPane();
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
    private List<TreeImageDisplay> 
    prepareSortedList(List<TreeImageDisplay> sorted)
    {
    	List<TreeImageDisplay> top = new ArrayList<TreeImageDisplay>();
		List<TreeImageDisplay> bottom = new ArrayList<TreeImageDisplay>();
		
		List<TreeImageDisplay> top2 = new ArrayList<TreeImageDisplay>();
		List<TreeImageDisplay> bottom2 = new ArrayList<TreeImageDisplay>();
		
		Iterator<TreeImageDisplay> j = sorted.iterator();
		TreeImageDisplay object;
		Object uo;
		while (j.hasNext()) {
			object = (TreeImageDisplay) j.next();
			uo = object.getUserObject();
			if (ProjectData.class.equals(containerType)) {
				if (uo instanceof ProjectData) {
					top.add(object);
					object.removeAllChildrenDisplay();
				}
			} else if (ScreenData.class.equals(containerType)) {
				if (uo instanceof ScreenData) {
					top.add(object);
					object.removeAllChildrenDisplay();
				}
			} else if (DatasetData.class.equals(containerType)) {
				if (uo instanceof ProjectData) top.add(object);
				else if (uo instanceof DatasetData) bottom.add(object);
			}
		}
		List<TreeImageDisplay> all = new ArrayList<TreeImageDisplay>();
		if (top.size() > 0) all.addAll(top);
		if (bottom.size() > 0) all.addAll(bottom);
		if (top2.size() > 0) all.addAll(top2);
		if (bottom2.size() > 0) all.addAll(bottom2);
		return all;
    }
    
	/** Builds the components displayed when no node to display.*/
	private void buildNoContentPane()
	{
		noDisplay = true;
		Container c = getContentPane();
		StringBuffer s = new StringBuffer();
		StringBuffer s1 = new StringBuffer();
		if (ProjectData.class.equals(containerType)) {
			s.append("There is no Project to move the Dataset(s) into.");
			s1.append("Please create a new Project if you wish.");
		} else if (DatasetData.class.equals(containerType)) {
			s.append("There is no Dataset to move the Image(s) into.");
			s1.append("Please create a new Dataset if you wish.");
		} else if (ScreenData.class.equals(containerType)) {
			s.append("There is no Screen to move the Plate into.");
			s1.append("Please create a new Screen if you wish.");
		}
		JPanel p = new JPanel();
		p.setBackground(UIUtilities.BACKGROUND);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(new JLabel(s.toString()));
		p.add(new JLabel(s1.toString()));
		body = buildContent(p);
		c.add(body, BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
		validate();
		repaint();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param userID The identifier of the user.
	 * @param object The object to move.
	 * @param same Pass <code>true</code> if the user moving the data and the 
	 * 			   owner of the data are the same person, <code>false</code>
	 */
	public MoveGroupSelectionDialog(JFrame owner, long userID,
			ChgrpObject object, boolean same)
	{
		super(owner);
		if (object == null)
			throw new IllegalArgumentException("No object to move.");
		StringBuffer buf = new StringBuffer();
		buf.append("Move to ");
		buf.append(object.getGroupData().getName());
		setTitle(buf.toString());
		this.object = object;
		this.userID = userID;
		initComponents(same);
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
	public void setTargets(Collection<DataObject> targets)
	{
		Container c = getContentPane();
		c.remove(body);
		c.remove(1);
		treeDisplay = new JTree();
		treeDisplay.setVisible(true);
        treeDisplay.setRootVisible(false);
        ToolTipManager.sharedInstance().registerComponent(treeDisplay);
        treeDisplay.setCellRenderer(new TreeCellRenderer(userID));
        treeDisplay.setShowsRootHandles(true);
        TreeImageSet root = new TreeImageSet("");
        treeDisplay.setModel(new DefaultTreeModel(root));
        treeDisplay.addTreeSelectionListener(this);
		if (CollectionUtils.isEmpty(targets)) {
			buildNoContentPane();
			return;
		}
		Set<TreeImageDisplay> 
		nodes = TreeViewerTranslator.transformHierarchy(
		        new ArrayList<Object>(targets));
		List<TreeImageDisplay> transformedNodes =
			prepareSortedList(sorter.sort(nodes));
		if (transformedNodes.size() == 0) {
			buildNoContentPane();
			return;
		}
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		buildTreeNode(root, transformedNodes, dtm);
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
				break;
			case CREATE:
				showNewFolder();
		}
	}

	/**
	 * Listens to property to create a new container.
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (CreateFolderDialog.CREATE_FOLDER_PROPERTY.equals(name)) {
			String folderName = (String) evt.getNewValue();
			if (folderName != null && folderName.trim().length() > 0) 
				create(folderName);
		}
	}

	/**
	 * Listens to node selection in the tree.
	 * @see TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e)
	{
		TreePath[] paths = treeDisplay.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			moveButton.setEnabled(false);
			return;
		}
		Object p = paths[0].getLastPathComponent();
		if (p instanceof TreeImageDisplay) {
			Object ho = ((TreeImageDisplay) p).getUserObject();
			moveButton.setEnabled(ho.getClass().equals(containerType));
		}
	}

}