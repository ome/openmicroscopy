/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;


//Java import
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserManageAction;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.PartialNameVisitor;
import org.openmicroscopy.shoola.agents.util.browser.SmartFolder;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.util.dnd.DnDTree;
import org.openmicroscopy.shoola.agents.util.dnd.ObjectToTransfer;
import org.openmicroscopy.shoola.env.data.FSFileSystemView;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FileData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.MultiImageData;
import pojos.PlateData;
import pojos.PlateAcquisitionData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * The Browser's View.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BrowserUI
    extends JPanel
    implements PropertyChangeListener
{   
    /** The <code>Attachments</code> smart folder. */
    private static final int[] VALUES = {TreeFileSet.MOVIE, TreeFileSet.OTHER};
    
    /** The tree hosting the display. */
    private DnDTree           treeDisplay;
    
    /** The tool bar hosting the controls. */
    private JToolBar				rightMenuBar;
    
    /** The tool bar hosting the controls. */
    private JToolBar				leftMenuBar;
    
    /** The Controller. */
    private BrowserControl  		controller;
    
    /** The model. */
    private BrowserModel    		model;
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter    		sorter;
    
    /** Reference to the listener. */
    private TreeExpansionListener	listener;
    
    /** Reference to the selection listener. */
    private TreeSelectionListener	selectionListener;

    /** Collections of nodes whose <code>enabled</code> flag has to be reset. */
    private Set<TreeImageDisplay>	nodesToReset;
    
    /** Button indicating if the partial name is displayed or not. */
    private JToggleButton			partialButton;
    
    /** Indicates if the <code>control</code> key is down. */
    private boolean 				ctrl;
    
    /** Flag indicating if the left mouse button is pressed. */
    private boolean					leftMouseButton;
    
    /** The component displayed at the bottom of the UI. */
    private JComponent				bottomComponent;

    /* The tree selection event. */
    private TreeSelectionEvent event;

    /* The time when the latest tree selection event was handled. */
    private long eventHandledTime = Long.MIN_VALUE;
    
    /* The time when the latest mouse press event occurred. */
    private long mousePressedTime;
    
    /* If a mouse event delayed handling tree selection, because
     * a corresponding tree selection event had not yet been seen. */
    private boolean delayedHandlingTreeSelection = false;
    
    /** Flag indicating if it is a right-click.*/
    private boolean rightClickButton;
    
    /** Flag indicating if it is a right-click.*/
    private boolean rightClickPad;
    
    /** Identifies the key event.*/
    private int keyEvent;
    
    /**
     * Builds the tool bar.
     * 
     * @return See above.
     */
    private JPanel buildToolBar()
    {
    	JPanel bar = new JPanel();
    	bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
    	bar.setBorder(null);
    	JPanel p = new JPanel();
    	p.setLayout(new FlowLayout(FlowLayout.LEFT));
    	p.setBorder(null);
    	p.add(leftMenuBar);
    	p.setPreferredSize(leftMenuBar.getPreferredSize());
    	bar.add(p);
    	p = new JPanel();
    	p.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    	p.setBorder(null);
    	p.add(rightMenuBar);
    	p.setPreferredSize(rightMenuBar.getPreferredSize());
    	bar.add(p);
    	return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setLayout(new BorderLayout(0, 0));
    	add(buildToolBar(), BorderLayout.NORTH);
    	add(new JScrollPane(treeDisplay), BorderLayout.CENTER);
    }
    
    /** Helper method to create the menu bar. */
    private void createMenuBars()
    {
        rightMenuBar = new JToolBar();
        rightMenuBar.setBorder(null);
        rightMenuBar.setRollover(true);
        rightMenuBar.setFloatable(false);
       
        JButton button;
        leftMenuBar = new JToolBar();
        leftMenuBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        leftMenuBar.setRollover(true);
        leftMenuBar.setFloatable(false);
        BrowserManageAction a;
        int type = model.getBrowserType();
        switch (type) {
			case Browser.PROJECTS_EXPLORER:
			case Browser.SCREENS_EXPLORER:
				a = (BrowserManageAction) 
					controller.getAction(BrowserControl.NEW_CONTAINER);
				button = new JButton(a);
				button.setName("new container button");
				button.setBorderPainted(false);
				button.addMouseListener(a);
				rightMenuBar.add(button);
				break;
			case Browser.ADMIN_EXPLORER:
				a = (BrowserManageAction) 
					controller.getAction(BrowserControl.NEW_ADMIN);
				button = new JButton(a);
				button.setName("new group or user button");
				button.setBorderPainted(false);
				button.addMouseListener(a);
				rightMenuBar.add(button);
				break;
			case Browser.TAGS_EXPLORER:
				a = (BrowserManageAction) 
					controller.getAction(BrowserControl.NEW_TAG);
				button = new JButton(a);
				button.setName("new tag button");
				button.setBorderPainted(false);
				button.addMouseListener(a);
				rightMenuBar.add(button);
		}
        button = new JButton(controller.getAction(BrowserControl.CUT));
        button.setName("cut button");
        button.setBorderPainted(false);
        rightMenuBar.add(button);
        button = new JButton(controller.getAction(BrowserControl.COPY));
        button.setName("copy button");
        button.setBorderPainted(false);
        rightMenuBar.add(button);
        button = new JButton(controller.getAction(BrowserControl.PASTE));
        button.setName("paste button");
        button.setBorderPainted(false);
        rightMenuBar.add(button);
        button = new JButton(controller.getAction(BrowserControl.DELETE));
        button.setName("delete button");
		button.setBorderPainted(false);
		rightMenuBar.add(button);
		button = new JButton(controller.getAction(BrowserControl.REFRESH));
		button.setName("refresh button");
		button.setBorderPainted(false);
		rightMenuBar.add(button);
		
		if (type == Browser.ADMIN_EXPLORER) {
			button = new JButton(controller.getAction(
					BrowserControl.RESET_PASSWORD));
			button.setBorderPainted(false);
			rightMenuBar.add(button);
		} else {
			button = new JButton(controller.getAction(BrowserControl.IMPORT));
			button.setBorderPainted(false);
			//rightMenuBar.add(button);
		}
		rightMenuBar.add(Box.createHorizontalStrut(6));
		rightMenuBar.add(new JSeparator());
		rightMenuBar.add(Box.createHorizontalStrut(6));
        ButtonGroup group = new ButtonGroup();
        JToggleButton b = new JToggleButton();
        group.add(b);
        b.setBorderPainted(true);
        b.setSelected(true);
        b.setAction(controller.getAction(BrowserControl.SORT));
        
        rightMenuBar.add(b);
        b = new JToggleButton(controller.getAction(BrowserControl.SORT_DATE));
        b.setBorderPainted(true);
        group.add(b);
        rightMenuBar.add(b);
       
        partialButton = new JToggleButton(
        				controller.getAction(BrowserControl.PARTIAL_NAME));
        partialButton.setBorderPainted(true);
        rightMenuBar.add(partialButton);
        rightMenuBar.add(new JSeparator(JSeparator.VERTICAL));
        button = new JButton(controller.getAction(BrowserControl.COLLAPSE));
        button.setBorderPainted(false);
        rightMenuBar.add(button);
    }

    /** 
     * Reacts to node expansion event.
     * 
     * @param node     The node to handle.
     * @param expanded Pass <code>true</code> is the node is expanded,
     *                 <code>false</code> otherwise.
     */
    private void onNodeNavigation(TreeImageDisplay node, boolean expanded)
    {
        node.setExpanded(expanded);
        controller.onNodeNavigation(node, expanded);
        treeDisplay.clearSelection();
        try {
        	treeDisplay.setSelectionPath(new TreePath(node.getPath()));
		} catch (Exception e) {}
        
		treeDisplay.repaint();
    }
    
    /**
     * Reacts to mouse pressed and mouse release event.
     * 
     * @param me        The event to handle.
     * @param released  Pass <code>true</code> if the method is invoked when
     *                  the mouse is released, <code>false</code> otherwise.
     */
    private void onClick(MouseEvent me, boolean released)
    {
        Point p = me.getPoint();
        int row = treeDisplay.getRowForLocation(p.x, p.y);
        if (row != -1) {
            if (me.getClickCount() == 1) {
                model.setClickPoint(p);
                if (mousePressedTime > eventHandledTime)
                	/* have not yet seen the tree selection event */
                	delayedHandlingTreeSelection = true;
                else
                	handleTreeSelection();
               //if (released) {
                if ((me.isPopupTrigger() && !released) || 
                		(me.isPopupTrigger() && released && 
                				!UIUtilities.isMacOS()) ||
                				(UIUtilities.isMacOS() && 
                						SwingUtilities.isLeftMouseButton(me)
                						&& me.isControlDown())) {
                	if (rightClickButton && !model.isMultiSelection()) { //(!(me.isShiftDown() || ctrl))
                		
                		TreePath path = treeDisplay.getPathForLocation(p.x, 
                				p.y);
                    	//treeDisplay.removeTreeSelectionListener(
                    	//		selectionListener);
                		if (path != null) 
                			treeDisplay.setSelectionPath(path);
                		//treeDisplay.addTreeSelectionListener(selectionListener);
                    	if (path != null && 
                    			path.getLastPathComponent()
                    			instanceof TreeImageDisplay)
                    		controller.onRightClick((TreeImageDisplay) 
                    				path.getLastPathComponent());
                	}
                	if (model.getBrowserType() == Browser.ADMIN_EXPLORER) 
                		controller.showPopupMenu(TreeViewer.ADMIN_MENU);
                	else 
                		controller.showPopupMenu(TreeViewer.FULL_POP_UP_MENU);
                }
            } else if (me.getClickCount() == 2 && !(me.isMetaDown()
            		|| me.isControlDown() || me.isShiftDown())) {
            	//controller.cancel();
                //model.viewDataObject();
            	TreeImageDisplay d  = model.getLastSelectedDisplay();
                if (d == null) return;
                Object o = d.getUserObject();
                if (o instanceof ImageData) {
                	model.browse(d);
                } else if (o instanceof PlateData) {
                	if (!d.hasChildrenDisplay() || 
                			d.getChildrenDisplay().size() == 1) 
                		model.browse(d);
                } else if (o instanceof PlateAcquisitionData) {
                	model.browse(d);
                }
            }
        }
    }
    
    /**
     * Handles the mouse moved event. Displays the properties of the
     * the nodes the mouse is over.
     * 
     * @param e	The mouse event to handle.
     */
    private void rollOver(MouseEvent e)
    {
    	if (!model.getParentModel().isRollOver()) return;
    	JTree tree = treeDisplay;
    	TreePath path = treeDisplay.getClosestPathForLocation(
    											e.getX(), e.getY());
        Rectangle bounds = tree.getPathBounds(path);
        if (!bounds.contains(e.getPoint())) return;
        TreeImageDisplay node = (TreeImageDisplay) path.getLastPathComponent();
    	Object uo = node.getUserObject();
    	if (!(uo instanceof DataObject)) return;
    }
    
    /**
     * Creates an experimenter node hosting the passed experimenter.
     * 
     * @param exp	The experimenter to add.
     * @param groupNode The node to attach the experimenter to.
     * @return See above.
     */
    private TreeImageSet createExperimenterNode(ExperimenterData exp,
    		TreeImageDisplay groupNode)
    {
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeImageSet node = new TreeImageSet(exp);
    	switch (model.getBrowserType()) {
			case Browser.IMAGES_EXPLORER:
				createTimeElements(node);
				break;
			case Browser.FILES_EXPLORER:
				createFileElements(node);
				break;
			case Browser.TAGS_EXPLORER:
				createTagsElements(node);
				break;
			default:
				buildEmptyNode(node);
		}
    	//TreeImageDisplay root = getTreeRoot();
    	//root.addChildDisplay(node);
    	//tm.insertNodeInto(node, root, root.getChildCount());
    	groupNode.addChildDisplay(node);
    	tm.insertNodeInto(node, groupNode, groupNode.getChildCount());
    	return node;
    }
    
    /**
     * Creates the elements to attach the specified group node.
     * 
     * @param node The node to attach the elements to.
     * @return See above.
     */
    private TreeImageSet createGroupNode(TreeImageSet node)
    {
    	switch (model.getBrowserType()) {
			case Browser.IMAGES_EXPLORER:
				createTimeElements(node);
				break;
			case Browser.FILES_EXPLORER:
				createFileElements(node);
				break;
			default:
				buildEmptyNode(node);
		}
    	return node;
    }
    
    /**
     * Creates the smart folders added to the passed node.
     * 
     * @param parent The parent of the smart folder.
     */
    private void createTimeElements(TreeImageSet parent)
    {
    	createTimeNode(TreeImageTimeSet.TODAY, parent, true);
    	createTimeNode(TreeImageTimeSet.WEEK, parent, true);
		createTimeNode(TreeImageTimeSet.TWO_WEEK, parent, true);
		TreeImageSet n = createTimeNode(TreeImageTimeSet.YEAR, parent, false);
    	int month = TreeImageTimeSet.getCurrentMonth()+1;
    	for (int i = 0; i < month; i++) 
    		createTimeNode(TreeImageTimeSet.YEAR, i, n);
    	n.setNumberItems(-1);
    	n = createTimeNode(TreeImageTimeSet.YEAR_BEFORE, parent, false);
    	for (int i = 0; i < 12; i++) 
    		createTimeNode(TreeImageTimeSet.YEAR_BEFORE, i, n);
    	n.setNumberItems(-1);
    	createTimeNode(TreeImageTimeSet.OTHER, parent, true);
    	//parent.setChildrenLoaded(true);
    }
    
    /**
     * Creates folders hosting <code>FileAnnotation</code>s.
     * 
     * @param parent The parent of the elements.
     */
    private void createFileElements(TreeImageSet parent)
    {
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeFileSet node;
    	for (int i = 0; i < VALUES.length; i++) {
    		node = new TreeFileSet(VALUES[i]);
    		buildEmptyNode(node);
    		node.setNumberItems(-1);
    		parent.addChildDisplay(node);
    		tm.insertNodeInto(node, parent, parent.getChildCount());
		}
    }
    
    /**
     * Creates the smart folders added to the passed node.
     * 
     * @param parent The parent of the smart folder.
     */
    private void createTagsElements(TreeImageDisplay parent)
    {
    	if (model.getDisplayMode() == TreeViewer.GROUP_DISPLAY) return;
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeFileSet node = new TreeFileSet(TreeFileSet.TAG);
    	buildEmptyNode(node);
		node.setNumberItems(-1);
		parent.addChildDisplay(node);
		tm.insertNodeInto(node, parent, parent.getChildCount());
    }
    
    /**
     * Creates and returns a {@link TreeImageTimeSet}.
     * 
     * @param index 	One of the following constants: 
     * 					{@link TreeImageTimeSet#YEAR} or 
     * 					{@link TreeImageTimeSet#WEEK}
     * @param parent	The parent of the new node.
     * @param empty		Pass <code>true</code> to add an empty node,
     * 					<code>false</code> otherwise.
     * @return See above.
     */
    private TreeImageTimeSet createTimeNode(int index, TreeImageSet parent, 
    										boolean empty)
    {
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeImageTimeSet date = new TreeImageTimeSet(index);
    	if (empty) buildEmptyNode(date);
    	parent.addChildDisplay(date);
    	tm.insertNodeInto(date, parent, parent.getChildCount());
    	return date;
    }
    
    /**
     * Creates and returns a {@link TreeImageTimeSet}.
     * 
     * @param index 	One of the following constants: 
     * 					{@link TreeImageTimeSet#YEAR} or 
     * 					{@link TreeImageTimeSet#YEAR_BEFORE}.
     * @param month		The index of the month.
     * @param parent	The parent of the new node.
     * @return See above.
     */
    private TreeImageTimeSet createTimeNode(int index, int month, 
    										TreeImageSet parent)
    {
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeImageTimeSet date = new TreeImageTimeSet(index, month);
    	buildEmptyNode(date);
    	parent.addChildDisplay(date);
    	tm.insertNodeInto(date, parent, parent.getChildCount());
    	parent.setChildrenLoaded(true);
    	return date;
    }
    
    /**
     * Transforms the file contained in the passed directory.
     * Returns the collection of <code>TreeImageNode</code> to check.
     * 
     * @param dirSet The directory to transform.
     * @return See above.
     */
    private List<TreeImageNode> transformDirectory(TreeImageSet dirSet)
    {
    	List<TreeImageNode> leaves = new ArrayList<TreeImageNode>();
    	FileData dir = (FileData) dirSet.getUserObject();
    	TreeImageDisplay expNode = BrowserFactory.getDataOwner(dirSet);
    	if (expNode == null) return leaves;
    	Object ho = expNode.getUserObject();
    	if (!(ho instanceof ExperimenterData)) return leaves;
    	long expID = ((ExperimenterData) ho).getId();
    	DataObject[] files = model.getFilesData(expID, dir);
    	if (files != null && files.length > 0) {
    		DataObject object;
    		FileData file;
    		TreeImageDisplay display;
    		DefaultTreeModel dtm =  (DefaultTreeModel) treeDisplay.getModel();
    		for (int i = 0; i < files.length; i++) {
    			object = files[i];
    			display = null;
    			if (object instanceof MultiImageData) {
					display = TreeViewerTranslator.transformMultiImage(
    						(MultiImageData) object);
    				if (display != null)
    					buildTreeNode(display, 
    						prepareSortedList(sorter.sort(
    								display.getChildrenDisplay())), dtm);
				} else if (object instanceof FileData) {
					file = (FileData) object;
    				if (file.isDirectory()) {
            			if (!file.isHidden()) {
            				display = new TreeImageSet(file);
            				buildEmptyNode(display);
            			}
            		} else {
            			if (!file.isHidden()) 
            				display = new TreeImageNode(file);
            		}
    			} else if (object instanceof ImageData) {
    				display = TreeViewerTranslator.transformImage(
    						(ImageData) object);
    			} 
    			if (display != null) dirSet.addChildDisplay(display);
			}
    	}
    	return leaves;
    }
    
    /**
     * Creates the file system view.
     * 
     * @param id The id of the user the directory is for.
     * @return See above.
     */
    private Set<TreeImageDisplay> createFileSystemExplorer(
    		TreeImageDisplay expNode)
    {
    	Set<TreeImageDisplay> results = new HashSet<TreeImageDisplay>();
    	ExperimenterData exp = (ExperimenterData) expNode.getUserObject();
    	FSFileSystemView fs = model.getRepositories(exp.getId());
    	if (fs == null) return results;
    	FileData file;
    	TreeImageSet display;
    	FileData[] files = fs.getRoots();
		for (int j = 0; j < files.length; j++) {
			file = files[j];
			if (file.isDirectory() && !file.isHidden()) {
				display = new TreeImageSet(file);
				//display.setChildrenLoaded(true);
				expNode.addChildDisplay(display);
				buildEmptyNode(display);
				//transformDirectory(display);
				results.add(display);
    		}
		}
    	return results;
    }
    
    /** Handles the selection of the nodes in the tree.*/
    private void handleTreeSelection()
    {
    	delayedHandlingTreeSelection = false;
    	TreeImageDisplay[] nodes = model.getSelectedDisplays();
    	if (((rightClickButton && !ctrl) || rightClickPad)
    		&& model.isMultiSelection()) {
    		setFoundNode(nodes);
    		return;
    	}
    	if (ctrl && leftMouseButton) {
    		TreePath[] paths = treeDisplay.getSelectionPaths();
    		List<TreePath> added = new ArrayList<TreePath>();
    		TreePath[] all = null;
    		if (paths != null) {
    			all = new TreePath[paths.length];
        		for (int i = 0; i < paths.length; i++) {
        			all[i] = new TreePath(paths[i].getPath());
				}
    		}
    		//treeDisplay.removeTreeSelectionListener(selectionListener);
    		if (all != null) treeDisplay.setSelectionPaths(all);
    		//treeDisplay.addTreeSelectionListener(selectionListener);
    		if (all != null) {
    			for (int i = 0; i < all.length; i++)
            		added.add(all[i]);
    		}
        	controller.onClick(added);
    		return;
    	}
    	if (event == null) return;
    	TreePath[] paths = event.getPaths();
    	List<TreePath> added = new ArrayList<TreePath>();
    	for (int i = 0; i < paths.length; i++) {
    		if (rightClickPad) {
    			if (!event.isAddedPath(paths[i])) {
        			added.add(paths[i]);
        		}
    		} else {
    			if (event.isAddedPath(paths[i])) {
        			added.add(paths[i]);
        		}
    		}
		}
    	//if (!ctrl) 
    	controller.onClick(added);
    }

    /** 
     * Handles multi-selection using <code>Ctrl-A</code> or <code>Meta-A</code>.
     */
    private void handleMultiSelection()
    {
    	TreeImageDisplay[] nodes = model.getSelectedDisplays();
		if (nodes == null || nodes.length == 0) return;
		TreeImageDisplay n = nodes[0];
		Object o = n.getUserObject();
		TreeImageDisplay p;
		if (o instanceof ImageData) {
			p = n.getParentDisplay();
			controller.selectNodes(p.getChildrenDisplay(), ImageData.class);
		} else if (o instanceof DatasetData) {
			if (n.isExpanded()) {
				controller.selectNodes(n.getChildrenDisplay(), ImageData.class);
			} else {
				p = n.getParentDisplay();
				controller.selectNodes(p.getChildrenDisplay(),
						DatasetData.class);
			}
		} else if (o instanceof ProjectData) {
			if (n.isExpanded()) {
				controller.selectNodes(n.getChildrenDisplay(),
						DatasetData.class);
			} else {
				p = n.getParentDisplay();
				controller.selectNodes(p.getChildrenDisplay(),
						ProjectData.class);
			}
		} else if (o instanceof ScreenData) {
			if (n.isExpanded()) {
				controller.selectNodes(
						n.getChildrenDisplay(),
						PlateData.class);
			} else {
				p = n.getParentDisplay();
				controller.selectNodes(p.getChildrenDisplay(),
						ScreenData.class);
			}
		} else if (o instanceof PlateData) {
			if (n.isExpanded()) {
				controller.selectNodes(
						n.getChildrenDisplay(),
						PlateAcquisitionData.class);
			} else {
				p = n.getParentDisplay();
				controller.selectNodes(p.getChildrenDisplay(),
						PlateData.class);
			}
		} else if (o instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) o;
			String ns = tag.getNameSpace();
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
				//dealing with a tag set.
				if (n.isExpanded()) {
					controller.selectNodes(n.getChildrenDisplay(),
							TagAnnotationData.class);
				} else {
					p = n.getParentDisplay();
					List l = p.getChildrenDisplay();
					Iterator i = l.iterator();
					List values = new ArrayList();
					TreeImageDisplay node;
					Object data;
					while (i.hasNext()) {
						node = (TreeImageDisplay) i.next();
						data = node.getUserObject();
						if (data instanceof TagAnnotationData) {
							tag = (TagAnnotationData) data;
							ns = tag.getNameSpace();
							if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
								values.add(node);
						}
					}
					controller.selectNodes(values, TagAnnotationData.class);
				}
			} else { //tag
				if (n.isExpanded()) {
					controller.selectNodes(n.getChildrenDisplay(),
							ImageData.class);
				} else {
					p = n.getParentDisplay();
					List l = p.getChildrenDisplay();
					Iterator i = l.iterator();
					List values = new ArrayList();
					TreeImageDisplay node;
					Object data;
					while (i.hasNext()) {
						node = (TreeImageDisplay) i.next();
						data = node.getUserObject();
						if (data instanceof TagAnnotationData) {
							tag = (TagAnnotationData) data;
							ns = tag.getNameSpace();
							if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
								values.add(node);
						}
					}
					controller.selectNodes(values, TagAnnotationData.class);
				}
			}
		} else if (o instanceof FileAnnotationData) {
			p = n.getParentDisplay();
			controller.selectNodes(p.getChildrenDisplay(),
					FileAnnotationData.class);
		} else if (n instanceof TreeImageTimeSet) {
			TreeImageTimeSet time = (TreeImageTimeSet) n;
			if (time.containsImages()) {
				controller.selectNodes(n.getChildrenDisplay(),
						ImageData.class);
			}
		} else if (n instanceof TreeFileSet) {
			controller.selectNodes(n.getChildrenDisplay(),
					FileAnnotationData.class);
		}
    }
    
    /**
	 * Builds the top nodes of the tree.
	 * 
	 * @param exp The experimenter to attach.
	 * @return See above.
	 */
	private TreeImageDisplay buildTreeNodes(ExperimenterData exp)
	{
		TreeImageDisplay root = getTreeRoot();
		TreeImageDisplay node = null;
		switch (model.getDisplayMode()) {
			case TreeViewer.GROUP_DISPLAY:
				if (TreeViewerAgent.isMultiGroups()) {
	    			GroupData group = model.getSelectedGroup();
	    			List<TreeImageSet> nodes = createGroups(group);
	    			Iterator<TreeImageSet> i = nodes.iterator();
	    			TreeImageSet n;
	    			GroupData g;
	    			while (i.hasNext()) {
						n = i.next();
						g = (GroupData) n.getUserObject();
						n = createGroupNode((TreeImageSet) n);
						if (g.getId() == group.getId())
							node = n;
					}
	    		} else {
	    			node = createGroup(model.getSelectedGroup());
	    			node = createGroupNode((TreeImageSet) node);
	    		}
				break;
			case TreeViewer.EXPERIMENTER_DISPLAY:
			default:
				if (model.isSingleGroup()) {
		    		node = root;
		    		node = createExperimenterNode(exp, node);
		    	} else {
		    		if (TreeViewerAgent.isMultiGroups()) {
		    			GroupData group = model.getSelectedGroup();
		    			List<TreeImageSet> nodes = createGroups(group);
		    			Iterator<TreeImageSet> i = nodes.iterator();
		    			TreeImageSet n;
		    			GroupData g;
		    			while (i.hasNext()) {
							n = i.next();
							g = (GroupData) n.getUserObject();
							n = createExperimenterNode(exp, n);
							if (g.getId() == group.getId())
								node = n;
						}
		    		} else {
		    			node = createGroup(model.getSelectedGroup());
		    			node = createExperimenterNode(exp, node);
		    		}
		    	}
				break;
		}
    	
    	return node;
	}

    /**
     * Creates the group nodes.
     * 
     * @param defaultGroup The default group
     * @return See above.
     */
    private List<TreeImageSet> createGroups(GroupData defaultGroup)
    {
    	TreeImageDisplay root = getTreeRoot();
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	//root.addChildDisplay(node);
    	//tm.insertNodeInto(node, root, root.getChildCount());
    	
    	List<TreeImageSet> l = new ArrayList<TreeImageSet>();
    	List groups = sorter.sort(TreeViewerAgent.getAvailableUserGroups());
    	//sort the group first.
    	Iterator i = groups.iterator();
    	GroupData group;
    	TreeImageSet n = new TreeImageSet(defaultGroup);
    	TreeViewerTranslator.formatToolTipFor(n);
    	l.add(n);
    	root.addChildDisplay(n);
    	tm.insertNodeInto(n, root, root.getChildCount());
    	while (i.hasNext()) {
			group = (GroupData) i.next();
			if (group.getId() != defaultGroup.getId()) {
				n = new TreeImageSet(group);
				TreeViewerTranslator.formatToolTipFor(n);
				l.add(n);
				root.addChildDisplay(n);
				tm.insertNodeInto(n, root, root.getChildCount());
			}
		}
    	return l;
    }
    
    /**
     * Creates the group node.
     * 
     * @param group The group to add.
     * @return See above.
     */
    private TreeImageSet createGroup(GroupData group)
    {
    	TreeImageDisplay root = getTreeRoot();
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	//root.addChildDisplay(node);
    	//tm.insertNodeInto(node, root, root.getChildCount());
    	TreeImageSet n = new TreeImageSet(group);
    	TreeViewerTranslator.formatToolTipFor(n);
    	root.addChildDisplay(n);
    	tm.insertNodeInto(n, root, root.getChildCount());
    	return n;
    }
    
    /** 
     * Helper method to create the trees hosting the display. 
     * 
     * @param exp The logged in experimenter.
     */
    private void createTrees(ExperimenterData exp)
    {
        treeDisplay = new DnDTree(model.getUserID(),
        		TreeViewerAgent.isAdministrator());
        treeDisplay.addPropertyChangeListener(this);
        String key = "meta pressed A";
        if (UIUtilities.isWindowsOS()) key = "ctrl pressed A";
        KeyStroke ks = KeyStroke.getKeyStroke(key);
        treeDisplay.getInputMap().put(ks, "none");
        treeDisplay.setVisible(true);
        treeDisplay.setRootVisible(false);
        ToolTipManager.sharedInstance().registerComponent(treeDisplay);
        treeDisplay.setCellRenderer(new TreeCellRenderer(model.getUserID()));
        treeDisplay.setShowsRootHandles(true);
        TreeImageSet root = new TreeImageSet("");
        treeDisplay.setModel(new DefaultTreeModel(root));
        if (model.getBrowserType() != Browser.ADMIN_EXPLORER) {
        	TreeImageDisplay node = buildTreeNodes(exp);
        	if (node != null)
            	treeDisplay.collapsePath(new TreePath(node.getPath()));
        }
        //Add Listeners
        //treeDisplay.requestFocus();
        treeDisplay.addMouseListener(new MouseAdapter() {
           public void mousePressed(MouseEvent e)
           {
        	   mousePressedTime = e.getWhen();
        	   rightClickPad = UIUtilities.isMacOS() &&
        	           SwingUtilities.isLeftMouseButton(e) && e.isControlDown();
        	   rightClickButton = SwingUtilities.isRightMouseButton(e);
        	   ctrl = e.isControlDown();
        	   if (UIUtilities.isMacOS()) ctrl = e.isMetaDown();
        	   leftMouseButton = SwingUtilities.isLeftMouseButton(e);
        	   if (UIUtilities.isMacOS() || UIUtilities.isLinuxOS())
        		   onClick(e, false); 
           }
           public void mouseReleased(MouseEvent e)
           { 
        	   leftMouseButton = SwingUtilities.isLeftMouseButton(e);
        	   if (UIUtilities.isWindowsOS()) onClick(e, true);
           }
           
          // public void mouseMoved(MouseEvent e) { rollOver(e); }
        });
        treeDisplay.addMouseMotionListener(new MouseMotionAdapter() {
           
            public void mouseMoved(MouseEvent e) { rollOver(e); }
         });
        treeDisplay.addTreeExpansionListener(listener);
        selectionListener = new TreeSelectionListener() {
        
            public void valueChanged(TreeSelectionEvent e)
            {
            	event = e;
            	eventHandledTime = System.currentTimeMillis();
            	
            	if (delayedHandlingTreeSelection)
            		/* mouse click delayed handling until this event occurred */
            		handleTreeSelection();
            	
            	switch (keyEvent) {
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_UP:
						TreePath[] paths = treeDisplay.getSelectionPaths();
						if (paths != null)
							controller.onClick(Arrays.asList(paths));
						else controller.onClick(new ArrayList<TreePath>());
						break; 
				}
            }
        };
        treeDisplay.addTreeSelectionListener(selectionListener);
        //remove standard behaviour
        treeDisplay.addKeyListener(new KeyAdapter() {
	
			public void keyPressed(KeyEvent e)
			{
				ctrl = false;
				switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						ViewCmd cmd = new ViewCmd(model.getParentModel(), true);
					    cmd.execute();
						break;
					case KeyEvent.VK_DELETE:
						switch (model.getState()) {
							case Browser.LOADING_DATA:
							case Browser.LOADING_LEAVES:
							//case Browser.COUNTING_ITEMS:  
								break;
							default:
								model.delete();
						}
						break;
					case KeyEvent.VK_CONTROL:
						if (!UIUtilities.isMacOS()) ctrl = true;
						break;
					case KeyEvent.VK_META:
						if (UIUtilities.isMacOS()) ctrl = true;
						break;
					case KeyEvent.VK_A:
						if (UIUtilities.isWindowsOS() && e.isControlDown() ||
							!UIUtilities.isWindowsOS() && e.isMetaDown()) {
							handleMultiSelection();
						}
						break;
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_UP:
					case KeyEvent.VK_RIGHT:
						keyEvent = e.getKeyCode();
						break;
					case KeyEvent.VK_LEFT:
						TreePath[] paths = treeDisplay.getSelectionPaths();
						TreeImageDisplay node;
						Object o;
						for (int i = 0; i < paths.length; i++) {
							o = paths[i].getLastPathComponent();
							if (o instanceof TreeImageDisplay) {
								node = (TreeImageDisplay) o;
								if (node.isExpanded())
									node.setExpanded(false);
							}
						}
				}
			}
			
			public void keyReleased(KeyEvent e)
			{
				ctrl = false;
				keyEvent = -1;
			}
			
		});
    }

    /**
     * Adds the nodes to the specified parent.
     * 
     * @param parent    The parent node.
     * @param nodes     The list of nodes to add.
     * @param tm        The  tree model.
     */
    private void buildTreeNode(TreeImageDisplay parent, 
                                Collection nodes, DefaultTreeModel tm)
    {
        if (nodes.size() == 0) {
            tm.insertNodeInto(new DefaultMutableTreeNode(Browser.EMPTY_MSG), 
                    parent, parent.getChildCount());
            return;
        }
        Iterator i = nodes.iterator();
        TreeImageDisplay display;
        List children;
        parent.removeAllChildren();
        int browserType = model.getBrowserType();
        Object uo;
        while (i.hasNext()) {
            display = (TreeImageDisplay) i.next();
        	tm.insertNodeInto(display, parent, parent.getChildCount());
        	
            if (display instanceof TreeImageSet) {
                children = display.getChildrenDisplay();
                if (children.size() > 0) {
                    if (display.containsImages()) {
                    	display.setExpanded(true);
                    	setExpandedParent(display, false);
                    	nodesToReset.add(display);
                    	buildTreeNode(display, 
                    			prepareSortedList(sorter.sort(children)), tm);
                        expandNode(display);
                        tm.reload(display);
                    } else {
                    	if (browserType == Browser.TAGS_EXPLORER) {
                    		if (display.isExpanded()) {
                        		setExpandedParent(display, false);
                            	nodesToReset.add(display);
                        	}
                    		buildTreeNode(display, 
                            		prepareSortedList(sorter.sort(children)),
                            		tm);
                    		if (display.isExpanded()) {
                    			expandNode(display);
                                tm.reload(display);
                    		}
                    	} else {
                    		if (display.isExpanded()) {
                        		setExpandedParent(display, true);
                            	nodesToReset.add(display);
                        	}
                        	buildTreeNode(display, 
                        		prepareSortedList(sorter.sort(children)), tm);
                    	}
                    }
                } else {
                	uo = display.getUserObject();
                	if (uo instanceof DatasetData) {
                		tm.insertNodeInto(new DefaultMutableTreeNode(Browser.EMPTY_MSG), 
                				display, display.getChildCount());
                	} else if (uo instanceof TagAnnotationData) {
                		TagAnnotationData tag = (TagAnnotationData) uo;
                		if (!(TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                				tag.getNameSpace()))) {
                			tm.insertNodeInto(
                					new DefaultMutableTreeNode(Browser.EMPTY_MSG), 
                    				display, display.getChildCount());
                		}
                	} else if (uo instanceof GroupData) {
                		tm.insertNodeInto(new DefaultMutableTreeNode(Browser.EMPTY_MSG), 
                				display, display.getChildCount());
                	} else if (uo instanceof FileAnnotationData) {
                		if (browserType == Browser.SCREENS_EXPLORER) {
                			TreeImageSet n = new TreeImageSet(uo);
                			tm.insertNodeInto(
                					new DefaultMutableTreeNode(Browser.EMPTY_MSG), 
                    				n, n.getChildCount());
                		}
                	}
                }  
            }
        } 
        if (parent.isExpanded()) {
            expandNode(parent);
            tm.reload(parent);
        }
    }
    
    /**
     * Sets the value of the <code>expanded</code> flag for the parent of 
     * the specified node.
     * 
     * @param n	The node to handle.
     * @param b	The value to set.
     */
    private void setExpandedParent(TreeImageDisplay n, boolean b)
    {
    	TreeImageDisplay p = n.getParentDisplay();
    	if (p != null) {
    		p.setExpanded(b);
    		setExpandedParent(p, b);
    	}
    }
    
    /**
     * Adds a dummy node to the specified node.
     * 
     * @param node The parent node.
     */
    private void buildEmptyNode(DefaultMutableTreeNode node)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
        tm.insertNodeInto(new DefaultMutableTreeNode(Browser.EMPTY_MSG), node,
                            node.getChildCount());
    }
    
    /**
     * Creates the smart folders added to the passed node.
     * 
     * @param parent The parent of the smart folder.
     */
    private void buildOrphanImagesNode(TreeImageDisplay parent)
    {
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeFileSet node = new TreeFileSet(TreeFileSet.ORPHANED_IMAGES);
    	buildEmptyNode(node);
		node.setNumberItems(-1);
		parent.addChildDisplay(node);
		tm.insertNodeInto(node, parent, parent.getChildCount());
    }

    /**
     * Sorts the children of the passed node.
     * 
     * @param node The node to handle.
     */
    private void sortNode(TreeImageSet node)
    {
    	DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
    	List children = node.getChildrenDisplay();
    	Iterator j;
    	if (node.containsImages()) {
    		node.removeAllChildren();
        	dtm.reload(node);
        	if (children.size() != 0) {
        		buildTreeNode(node, sorter.sort(children), dtm);
        	} else buildEmptyNode(node);
        	j = nodesToReset.iterator();
        	while (j.hasNext()) {
        		setExpandedParent((TreeImageDisplay) j.next(), true);
        	}
    	} else {
    		if (children.size() != 0) {
    			j = children.iterator();
    			while (j.hasNext())
					sortNode((TreeImageSet) j.next());
        	} else buildEmptyNode(node);
    	}
    }
    
    /**
     * Refreshes the passed folder node.
     * 
     * @param node		The node to refresh.
     * @param elements	The elements to add.
     */
    private void refreshFolderNode(TreeImageSet node, Set elements)
	{
		node.removeAllChildren();
		node.removeAllChildrenDisplay();
		Iterator k = elements.iterator();
		TreeImageDisplay child;
		while (k.hasNext()) {
			child = (TreeImageDisplay) k.next();
			node.addChildDisplay(child);
		}

		buildTreeNode(node, sorter.sort(elements), 
                (DefaultTreeModel) treeDisplay.getModel());
		node.setExpanded(true);
		expandNode(node);
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
			else if (uo instanceof GroupData) top.add(object);
			else if (uo instanceof ScreenData) top2.add(object);
			else if (uo instanceof DatasetData) bottom.add(object);
			else if (uo instanceof PlateData) bottom2.add(object);
			else if (uo instanceof PlateAcquisitionData) bottom2.add(object);
			else if (uo instanceof TagAnnotationData) {
				if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
					((TagAnnotationData) uo).getNameSpace()))
					top.add(object);
				else bottom.add(object);
			} else if (uo instanceof File) {
				File f = (File) uo;
				if (f.isDirectory()) {
					if (((TreeImageSet) object).isSystem())
						top.add(object);
					else bottom.add(object);
				} else top.add(object);
			} else if (uo instanceof FileData) {
				FileData f = (FileData) uo;
				if (f.isDirectory()) top.add(object);
				else bottom.add(object);
			} else if (uo instanceof ImageData)
				bottom.add(object);
			else if (uo instanceof MultiImageData)
				bottom.add(object);
			else if (uo instanceof ExperimenterData)
				bottom.add(object);
			else if (object instanceof SmartFolder ||
			        object instanceof TreeFileSet)
				bottom2.add(object);
		}
		List<TreeImageDisplay> all = new ArrayList<TreeImageDisplay>();
		if (top.size() > 0) all.addAll(top);
		if (bottom.size() > 0) all.addAll(bottom);
		if (top2.size() > 0) all.addAll(top2);
		if (bottom2.size() > 0) all.addAll(bottom2);
		return all;
    }
    

	/**
	 * Refreshes the folder hosting the file.
	 * 
	 * @param expNode	The experimenter node to refresh.
	 * @param r			The data to display.
	 */
	private void refreshFileFolder(TreeImageDisplay expNode, 
			Map<Integer, Set> r)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		expNode.setChildrenLoaded(Boolean.valueOf(true));
		expNode.setExpanded(Boolean.valueOf(true));
		if (r == null || r.size() == 0) return;
		Iterator i = r.keySet().iterator();
		int index;
		int n = expNode.getChildCount();
		TreeFileSet node;
		dtm.reload();
		while (i.hasNext()) {
			index = (Integer) i.next();
			for (int j = 0; j < n; j++) {
				node = (TreeFileSet) expNode.getChildAt(j);
				if (node.getType() == index) 
					refreshFolderNode(node, r.get(index));
			}
		}
		setExpandedParent(expNode, true);
	}
	
	/**
	 * Refreshes the folder hosting the time.
	 * 
	 * @param expNode	The experimenter node to refresh.
	 * @param r			The data to display.
	 */
	private void refreshTimeFolder(TreeImageDisplay expNode,
			Map<Integer, Set> r)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		expNode.setChildrenLoaded(Boolean.TRUE);
		expNode.setExpanded(true);
		if (r == null || r.size() == 0) return;
		Iterator i = r.keySet().iterator();
		int index;
		int n = expNode.getChildCount();
		TreeImageTimeSet node, child;
		dtm.reload();
		int nodeType;
		List children;
		Iterator s;
		while (i.hasNext()) {
			index = (Integer) i.next();
			for (int j = 0; j < n; j++) {
				node = (TreeImageTimeSet) expNode.getChildAt(j);
				nodeType = node.getType();
				switch (nodeType) {
					case TreeImageTimeSet.YEAR:
					case TreeImageTimeSet.YEAR_BEFORE:
						children = node.getChildrenDisplay();
						s = children.iterator();
						while (s.hasNext()) {
							child = (TreeImageTimeSet) s.next();
							if (child.getIndex() == index) 
								refreshFolderNode(child, r.get(index));
						}
					default:
						if (node.getIndex() == index) 
							refreshFolderNode(node, r.get(index));
						break;
				}
			}
		}
		setExpandedParent(expNode, true);
	}
    
    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserControl, BrowserModel) initialize} method
     * should be called straight after to link this View to the Controller.
     */
    BrowserUI()
    {
        sorter = new ViewerSorter();
        nodesToReset = new HashSet<TreeImageDisplay>();
        listener = new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                onNodeNavigation((TreeImageDisplay) 
						e.getPath().getLastPathComponent(), false);
            }
            public void treeExpanded(TreeExpansionEvent e) {
                onNodeNavigation((TreeImageDisplay) 
						e.getPath().getLastPathComponent(), true);  
            }   
        };
    }
    
    /**
     * Links this View to its Controller and its Model.
     * 
     * @param controller    The Controller.
     * @param model         The Model.
     * @param exp			The experimenter the tree view is for.
     */
    void initialize(BrowserControl controller, BrowserModel model, 
    						ExperimenterData exp)
    {
    	if (controller == null)
    		throw new IllegalArgumentException("Controller cannot be null");
    	if (model == null)
    		throw new IllegalArgumentException("Model cannot be null");
        this.controller = controller;
        this.model = model;
        createMenuBars();
        createTrees(exp);
        buildGUI();
    }

    /**
     * Creates a dummy loading node whose parent is the specified node.
     * 
     * @param parent The parent node.
     */
    void loadAction(TreeImageDisplay parent)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
        parent.removeAllChildren();
        tm.insertNodeInto(new DefaultMutableTreeNode(Browser.LOADING_MSG), parent,
                			parent.getChildCount());
        tm.reload(parent);
    }
    
    /**
     * Returns <code>true</code> if the first child of the passed node
     * is one of the added element.
     * 
     * @param parent The node to handle.
     * @return See above.
     */
    boolean isFirstChildMessage(TreeImageDisplay parent)
    {
    	int n = parent.getChildCount();
    	if (n == 0) return true;
    	DefaultMutableTreeNode node = 
			 (DefaultMutableTreeNode) parent.getChildAt(0);
    	Object uo = node.getUserObject();
    	if (Browser.LOADING_MSG.equals(uo) || Browser.EMPTY_MSG.equals(uo))
    		return true;
    	return false;
    }
    
    /**
     * Returns the tree hosting the display.
     * 
     * @return See above.
     */
    JTree getTreeDisplay() { return treeDisplay; }
    
    /**
     * Returns the root node of the tree.
     * 
     * @return See above.
     */
    TreeImageDisplay getTreeRoot()
    {
        if (treeDisplay == null) return null;
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        if (dtm == null) return null;
        return (TreeImageDisplay) dtm.getRoot();
    }
    
    /**
     * Returns the title of the Browser according to the type.
     * 
     * @return See above.
     */
    String getBrowserTitle()
    {
        switch (model.getBrowserType()) {
            case Browser.PROJECTS_EXPLORER:
                return Browser.HIERARCHY_TITLE;
            case Browser.IMAGES_EXPLORER:
                return Browser.IMAGES_TITLE;
            case Browser.TAGS_EXPLORER:
                return Browser.TAGS_TITLE;
            case Browser.SCREENS_EXPLORER:
            	return Browser.SCREENS_TITLE;
            case Browser.FILES_EXPLORER:
                return Browser.FILES_TITLE;
            case Browser.FILE_SYSTEM_EXPLORER:
                return Browser.FILE_SYSTEM_TITLE;
            case Browser.ADMIN_EXPLORER:
                return Browser.ADMIN_TITLE;
        }
        return "";
    }
    
    /**
     * Selects the specified node.
     * 
     * @param node The node to select.
     */
    void selectFoundNode(TreeImageDisplay node)
    {
        TreePath path = new TreePath(node.getPath());
        treeDisplay.setSelectionPath(path);
        TreeCellRenderer renderer = (TreeCellRenderer) 
        			treeDisplay.getCellRenderer();
        //treeDisplay.requestFocus();
        renderer.getTreeCellRendererComponent(treeDisplay, node, 
                					treeDisplay.isPathSelected(path),
                					false, true, 0, false);
    }
    
    /**
     * Collapses the specified node. To avoid loop, we first need to 
     * remove the <code>TreeExpansionListener</code>.
     * 
     * @param node The node to collapse.
     */
    void collapsePath(DefaultMutableTreeNode node)
    {
        //First remove listener otherwise an event is fired.
        treeDisplay.removeTreeExpansionListener(listener);
        treeDisplay.collapsePath(new TreePath(node.getPath()));
        treeDisplay.addTreeExpansionListener(listener);
    }
    
    /** 
     * Collapses the node when an on-going data loading is cancelled.
     * 
     * @param node The node to collapse.
     */
    void cancel(DefaultMutableTreeNode node)
    {
        if (node == null) return;
        if (node.getChildCount() <= 1) {
            if (node.getUserObject() instanceof String) {
                node.removeAllChildren(); 
                buildEmptyNode(node);
            }
        }
        //in this order otherwise the node is not collapsed.
        ((DefaultTreeModel) treeDisplay.getModel()).reload(node);
        collapsePath(node);
    }
    
    /**
     * Update the specified set of nodes.
     * 
     * @param nodes The collection of nodes to update.
     * @param object The <code>DataObject</code> to update.
     */
    void updateNodes(List nodes, DataObject object)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        Iterator i = nodes.iterator(); 
        TreeImageDisplay node;
        while (i.hasNext()) {
            node = (TreeImageDisplay) i.next();
            node.setUserObject(object);
            dtm.nodeChanged(node);
        }
    }
    
    /**
     * Removes the specified set of nodes from the tree.
     * 
     * @param nodes         The collection of nodes to remove.
     * @param parentDisplay The selected parent.
     */
    void removeNodes(List nodes, TreeImageDisplay parentDisplay)
    {
        if (parentDisplay == null) parentDisplay = getTreeRoot();
        Iterator i = nodes.iterator(); 
        TreeImageDisplay node;
        TreeImageDisplay parent;
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        while (i.hasNext()) {
            node = (TreeImageDisplay) i.next();
            parent = node.getParentDisplay();
            if (parent.isChildrenLoaded()) {
                parent.removeChildDisplay(node);
                parent.remove(node);
                dtm.reload(parent);
                if (parent.equals(parentDisplay))
                    treeDisplay.setSelectionPath(
                            new TreePath(parent.getPath()));
            }
        }
    }
    
    /**
     * Adds the newly created node to the tree.
     * 
     * @param nodes         The collection of the parent nodes.
     * @param newNode       The node to add to the parent.
     * @param parentDisplay The selected parent.
     */
    void createNodes(List nodes, TreeImageDisplay newNode,
                    TreeImageDisplay parentDisplay)
    {
        if (parentDisplay == null) parentDisplay = getTreeRoot();
        Iterator i = nodes.iterator();
        TreeImageDisplay parent;
        List list;
        Iterator j;
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        //buildEmptyNode(newNode);
        boolean toLoad = false;
        TreeImageDisplay n;
        while (i.hasNext()) {
            parent = (TreeImageDisplay) i.next();
            //problem will come when we have images
            if (parent.isChildrenLoaded()) {
                parent.addChildDisplay(newNode); 
                list = prepareSortedList(
                		sorter.sort(parent.getChildrenDisplay()));
                parent.removeAllChildren();
                j = list.iterator();
                while (j.hasNext()) {
                	n = (TreeImageDisplay) j.next();
                	if (!n.isChildrenLoaded()) {
                		n.removeAllChildren();
                		buildEmptyNode(n);
                	}
                	dtm.insertNodeInto(n, parent, parent.getChildCount());
                }
                dtm.reload(parent);
                expandNode(parent);
                if (parent.equals(parentDisplay))
                    treeDisplay.setSelectionPath(
                            new TreePath(newNode.getPath()));
            } else { //Only the currently selected one will be loaded.
                if (parent.equals(parentDisplay)) toLoad = true;
            }
        }
        //should be leaves. Need to review that code.
        if (toLoad) { //TO BE MODIFIED
            //if (parentDisplay.getParentDisplay() == null) //root
            //    controller.loadData();
            //else controller.loadLeaves();
        }
    }
    
    /**
     * Sorts the nodes in the tree view  according to the specified index.
     * 
     * @param type 	One out of the following constants: 
     * 				{@link  Browser#SORT_NODES_BY_DATE} or 
     * 				{@link  Browser#SORT_NODES_BY_NAME}.
     */
    void sortNodes(int type)
    {
    	boolean b = type == Browser.SORT_NODES_BY_DATE;
        sorter.setByDate(b);
        sorter.setAscending(!b);
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
    	int n = root.getChildCount();
    	TreeImageDisplay node;
    	List children;
    	Iterator j;
    	List all;
        switch (model.getBrowserType()) {
			case Browser.IMAGES_EXPLORER:
			case Browser.FILES_EXPLORER:
				for (int i = 0; i < n; i++) {
					node = (TreeImageDisplay) root.getChildAt(i);
					children = node.getChildrenDisplay();
					j = children.iterator();
					TreeImageDisplay child;
					while (j.hasNext()) {
						child = (TreeImageDisplay) j.next();
						if (child instanceof TreeImageTimeSet ||
						    child instanceof TreeFileSet)
							sortNode((TreeImageSet) child);
					}
				}
				break;
			case Browser.ADMIN_EXPLORER:
				for (int i = 0; i < n; i++) {
					node = (TreeImageDisplay) root.getChildAt(i);
					children = node.getChildrenDisplay();
					node.removeAllChildren();
					dtm.reload(node);
					if (!children.isEmpty()) {
						if (node.getUserObject() instanceof GroupData) {
							all = prepareSortedList(sorter.sort(children));
							buildTreeNode(node, all, dtm);
						} else {
							buildTreeNode(node, sorter.sort(children), dtm);
						}
					} else buildEmptyNode(node);
					j = nodesToReset.iterator();
					while (j.hasNext()) {
						setExpandedParent((TreeImageDisplay) j.next(), true);
					}
				}
				break;
			default:
				for (int i = 0; i < n; i++) {
					node = (TreeImageDisplay) root.getChildAt(i);
					children = node.getChildrenDisplay();
					node.removeAllChildren();
					dtm.reload(node);
					if (!children.isEmpty()) {
						if (node.getUserObject() instanceof ExperimenterData) {
						    List<Object> sets = new ArrayList<Object>();
						    List<Object> toSort = new ArrayList<Object>();
							Iterator k = children.iterator();
							while (k.hasNext()) {
                                Object object = (Object) k.next();
                                if (object instanceof TreeFileSet) {
                                    sets.add((TreeFileSet) object);
                                } else toSort.add(object);
                            }
							sets.addAll(sorter.sort(toSort));
							Collections.reverse(sets);
							all = prepareSortedList(sets);
							buildTreeNode(node, all, dtm);
						} else {
							buildTreeNode(node, sorter.sort(children), dtm);
						}
					} else buildEmptyNode(node);
					j = nodesToReset.iterator();
					while (j.hasNext()) {
						setExpandedParent((TreeImageDisplay) j.next(), true);
					}
				}
		}
    }

    /** 
     * Reacts to state change.
     * 
     * @param b Pass <code>true</code> to enable the trees, <code>false</code>
     *          otherwise.
     */
    void onStateChanged(boolean b)
    {
        model.getParentModel().onComponentStateChange(b);
    }

    /**
     * Enables the components composing the display depending on the specified
     * parameter.
     * 
     * @param b Pass <code>true</code> to enable the component, 
     *          <code>false</code> otherwise.
     */
    void onComponentStateChange(boolean b)
    {
        treeDisplay.setEnabled(b);
    }

    /** Resets the UI so that we have no node selected in trees. */
    void setNullSelectedNode()
    {
        if (getTreeRoot() != null) {
            treeDisplay.setSelectionRow(-1);
        }
    }
    
    /** 
     * Returns <code>true</code> if the partial name is displayed, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isPartialName() { return !partialButton.isSelected(); }
    
    /**
     * Removes the collection of <code>TreePath</code>s from the main tree.
     * We first need to remove the <code>TreeSelectionListener</code> to avoid 
     * loop.
     * 
     * @param paths Collection of paths to be removed.
     */
    void removeTreePaths(List paths)
    {
    	//treeDisplay.removeTreeSelectionListener(selectionListener);
    	Iterator j = paths.iterator();
        while (j.hasNext()) 
        	treeDisplay.removeSelectionPath((TreePath) j.next());

        //treeDisplay.addTreeSelectionListener(selectionListener);
    }

    /**
     * Adds the experimenter's data to the passed node.
     * 
     * @param nodes		The data to add.
     * @param expNode	The selected experimenter node.
     */
	void setExperimenterData(Set nodes, TreeImageDisplay expNode)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		expNode.removeAllChildren();
		expNode.removeAllChildrenDisplay();
		expNode.setChildrenLoaded(Boolean.valueOf(true));
        dtm.reload(expNode);
        Iterator i;
        if (nodes.size() > 0) {
        	boolean createFolder = true;
            i = nodes.iterator();
            TreeImageDisplay node;
            TreeFileSet n = null;
            Set<TreeImageDisplay> toKeep = new HashSet<TreeImageDisplay>();
            int type;
            while (i.hasNext()) {
            	node = (TreeImageDisplay) i.next();
            	if (node instanceof TreeFileSet) {
            		type = ((TreeFileSet) node).getType();
            		if (type == TreeFileSet.TAG || 
            			type == TreeFileSet.ORPHANED_IMAGES) {
            			List l = node.getChildrenDisplay();
                		if (l.size() > 0) {
                			createFolder = false;
                			n = (TreeFileSet) node.copy();
                			n.setChildrenLoaded(Boolean.valueOf(true));
                			expNode.addChildDisplay(n);
                			expandNode(n);
                		}
            		} else {
            			toKeep.add(node);
            			expNode.addChildDisplay(node);
            		}
            	} else {
            		toKeep.add(node);
            		expNode.addChildDisplay(node);
            	}
            }
            List sorted = prepareSortedList(sorter.sort(toKeep));
            if (n != null) sorted.add(n);
            buildTreeNode(expNode, sorted,
            		(DefaultTreeModel) treeDisplay.getModel());
            if (createFolder) {
            	switch (model.getBrowserType()) {
					case Browser.TAGS_EXPLORER:
						if (n == null)
							createTagsElements(expNode);
						break;
					case Browser.PROJECTS_EXPLORER:
						buildOrphanImagesNode(expNode);
            	}
            }
        } else {
        	switch (model.getBrowserType()) {
				case Browser.TAGS_EXPLORER:
					createTagsElements(expNode);
					break;
				case Browser.PROJECTS_EXPLORER:
					buildOrphanImagesNode(expNode);
					break;
				default:
				    expNode.setExpanded(false);
					buildEmptyNode(expNode);
        	}
		}
        //
        i = nodesToReset.iterator();
        while (i.hasNext()) 
			setExpandedParent((TreeImageDisplay) i.next(), true);
        TreeImageDisplay root = getTreeRoot();
		TreeImageDisplay element, child;
		Object ho;
		List children;
		for (int j = 0; j < root.getChildCount(); j++) {
			element = (TreeImageDisplay) root.getChildAt(j);
			ho = element.getUserObject();
			if (ho instanceof GroupData && element.isExpanded()) {
				expandNode(element);
				children = element.getChildrenDisplay();
				if (children != null) {
					i = children.iterator();
					while (i.hasNext()) {
						child = (TreeImageDisplay) i.next();
						if (child.isExpanded())
							expandNode(child);
					}
				}
			}
		}
	}

	/**
	 * Sets the nodes hosting the groups to manage.
	 * 
	 * @param nodes The nodes to display
	 * @param expanded The list of nodes previously expanded.
	 */
	void setGroups(Set nodes, List expanded)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		TreeImageDisplay root = getTreeRoot();
		root.removeAllChildren();
		root.removeAllChildrenDisplay();
		root.setChildrenLoaded(Boolean.valueOf(true));
		root.setExpanded(true);
		dtm.reload();
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext()) {
            	root.addChildDisplay((TreeImageDisplay) i.next());
            } 
            buildTreeNode(root, prepareSortedList(sorter.sort(nodes)), dtm);
            i = nodes.iterator();
            while (i.hasNext()) {
            	((TreeImageDisplay) i.next()).setExpanded(false);
            } 
            if (expanded != null && expanded.size() > 0) {
            	i = nodes.iterator();
                TreeImageDisplay display;
                GroupData group;
                Object ho;
                Set l;
                GroupData g;
                while (i.hasNext()) {
                	display = (TreeImageDisplay) i.next();
    				ho = display.getUserObject();
    				if (ho instanceof GroupData) {
    					g = (GroupData) ho;
    					if (expanded.contains(g.getId())) {
    						expandNode(display);
    					}
    				}
    			}
            }  
        } 
        if (TreeViewerAgent.isAdministrator()) {
        	 SmartFolder folder = new SmartFolder(GroupData.class, 
             "Experimenters w/o groups");
             buildEmptyNode(folder);
             //root.addChildDisplay(folder);
             //dtm.insertNodeInto(folder, root, root.getChildCount());
        }
	}
	
	/**
	 * Sets the number of items imported during a period of time.
	 * 
	 * @param expNode 	The node hosting the experimenter.
	 * @param index		The index of the time node.
	 * @param value		The value to set.
	 */
	void setCountValues(TreeImageDisplay expNode, int index, Object value)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		if (model.getBrowserType() != Browser.TAGS_EXPLORER)
			expNode.setChildrenLoaded(Boolean.valueOf(true));
		int n = expNode.getChildCount();
		TreeImageSet node;
		List l;
		Iterator i, k;
		TreeImageTimeSet child;
		//Test
		List<TreeImageSet> toRemove = new ArrayList<TreeImageSet>();
		List<TreeImageSet> toKeep = new ArrayList<TreeImageSet>();
		int number;
		int total;
		DefaultMutableTreeNode childNode;
		List<DefaultMutableTreeNode> remove =
			new ArrayList<DefaultMutableTreeNode>();
		for (int j = 0; j < n; j++) {
			childNode = (DefaultMutableTreeNode) expNode.getChildAt(j);
			Object o = childNode.getUserObject();
			if (o instanceof String) {
				String s = (String) o;
				if (Browser.EMPTY_MSG.equals(s)) {
					remove.add(childNode);
				}
			}
			if (childNode instanceof TreeImageTimeSet) {
				node = (TreeImageTimeSet) childNode;
				if (((TreeImageTimeSet) node).getType() == index) {
					if (value instanceof Integer) {
						//Check if the number is 0
						int vv = ((Integer) value).intValue();
						long v = node.getNumberItems();
						node.setNumberItems(vv);
						if (v == 0 && vv > 0) {
							buildEmptyNode(node);
							node.setChildrenLoaded(Boolean.valueOf(false));
						}
					} else if (value instanceof List) {
						l = (List) value;
						total = 0;
						i = node.getChildrenDisplay().iterator();
						while (i.hasNext()) {
							child = (TreeImageTimeSet) i.next();
							number = child.countTime(l);
							total += number;
							if (number > 0) {
								child.setNumberItems(number);
								toKeep.add(child);
							} else {
								toRemove.add(child);
							}
						}
						node.removeAllChildren();
						node.removeChildrenDisplay(toRemove);
						node.setNumberItems(total);
						k = toKeep.iterator();
						while (k.hasNext()) {
							dtm.insertNodeInto((TreeImageTimeSet) k.next(), node,
													node.getChildCount());
						}
					}
					dtm.reload(node);
				}
			} else if (childNode instanceof TreeFileSet) {
				node = (TreeFileSet) childNode;
				if (((TreeFileSet) node).getType() == index) {
					if (value instanceof Long) 
						node.setNumberItems((Long) value);
				}
			}
		}
		if (remove.size() > 0) {
			Iterator<DefaultMutableTreeNode> j = remove.iterator();
			while (j.hasNext()) {
				expNode.remove(j.next());
				dtm.reload(expNode);
			}
			
		}
	}
		
	/**
	 * Refreshes the folder hosting the files and the times.
	 * 
	 * @param expNode The experimenter node to refresh.
	 * @param r			The data to display.
	 */
	void refreshFolder(TreeImageDisplay expNode, Map<Integer, Set> r)
	{
		int browseType = model.getBrowserType();
		if (browseType == Browser.IMAGES_EXPLORER)
			refreshTimeFolder(expNode, r);
		else if (browseType == Browser.FILES_EXPLORER)
			refreshFileFolder(expNode, r);
	}
	
	/**
     * Adds the specifies nodes to the currently selected
     * {@link TreeImageDisplay}.
     * 
     * @param nodes     The collection of nodes to add.
     * @param parent    The parent of the nodes.
     */
    void setLeavesViews(Collection nodes, TreeImageSet parent)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        parent.removeAllChildren();
        parent.removeAllChildrenDisplay();
        parent.setChildrenLoaded(Boolean.TRUE);
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                parent.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(parent, sorter.sort(nodes), dtm);
        } else buildEmptyNode(parent);
        dtm.reload(parent);
        if (!isPartialName()) {
    		model.component.accept(new PartialNameVisitor(isPartialName()), 
    				TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
        }
    }
    
	/**
	 * Adds a new experimenter to the tree.
	 * 
	 * @param experimenter  The experimenter to add.
	 * @param groupNode The node to add the experimenter to.
	 */
	void addExperimenter(ExperimenterData experimenter,
			TreeImageDisplay groupNode)
	{
		TreeImageSet node = createExperimenterNode(experimenter, groupNode);
	}

	/**
	 * Removes the specified experimenter from the tree.
	 * 
	 * @param exp The experimenter data to remove.
	 * @param refNode The node to remove the experimenter from.
	 */
	void removeExperimenter(ExperimenterData exp, TreeImageDisplay refNode)
	{
		if (model.getBrowserType() == Browser.ADMIN_EXPLORER) return;
		if (refNode == null) refNode = getTreeRoot();

		List<TreeImageDisplay> nodesToKeep;
		List l = refNode.getChildrenDisplay();
		if (l == null || l.size() == 0) return;
		Iterator j = l.iterator();
		TreeImageDisplay element, node;
		Object ho;
		ExperimenterData expElement;
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		Iterator k;
		node = null;
		nodesToKeep = new ArrayList<TreeImageDisplay>();
		List<TreeImageDisplay> toExpand = new ArrayList<TreeImageDisplay>();
		while (j.hasNext()) {
			element = (TreeImageDisplay) j.next();
			ho = element.getUserObject();
			if (ho instanceof ExperimenterData) {
				expElement = (ExperimenterData) ho;
				if (expElement.getId() == exp.getId())
					node = element;
				else {
					nodesToKeep.add(element);
					if (element.isExpanded())
						toExpand.add(element);
				}
			}
		}
		if (node != null) refNode.removeChildDisplay(node);
		k = nodesToKeep.iterator();
		refNode.removeAllChildren();
		while (k.hasNext()) {
			tm.insertNodeInto((TreeImageSet) k.next(), refNode,
					refNode.getChildCount());
		}
		
		tm.reload(refNode);
		k = toExpand.iterator();
		while (k.hasNext()) {
			expandNode((TreeImageSet) k.next(), false);
		}
	}

	/**
	 * Removes the specified group from the tree.
	 * 
	 * @param group The data to remove.
	 */
	void removeGroup(GroupData group)
	{
		if (model.getBrowserType() == Browser.ADMIN_EXPLORER) return;
		TreeImageDisplay root = getTreeRoot();
		List<TreeImageDisplay> nodesToKeep;
		List l = root.getChildrenDisplay();
		if (l == null || l.size() == 0) return;
		Iterator j = l.iterator();
		TreeImageDisplay element, n, node;
		Object ho;
		ExperimenterData expElement;
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		Iterator k;
		GroupData g;
		nodesToKeep = new ArrayList<TreeImageDisplay>();
		node = null;
		while (j.hasNext()) {
			element = (TreeImageDisplay) j.next();
			if (element.getUserObject() instanceof GroupData) {
				g = (GroupData) element.getUserObject();
				if (g.getId() == group.getId()) node = element;
				else nodesToKeep.add(element);
			}
		}
		
		if (node != null) root.removeChildDisplay(node);
		k = nodesToKeep.iterator();
		root.removeAllChildren();
		while (k.hasNext()) {
			tm.insertNodeInto((TreeImageSet) k.next(), root,
							root.getChildCount());
		}
		tm.reload();
	}
	
	/** Reactivates the tree. */
	void reActivate()
	{
		clear();
		treeDisplay.reset(model.getUserID(),
	       		TreeViewerAgent.isAdministrator());
		TreeCellRenderer renderer = (TreeCellRenderer)
		treeDisplay.getCellRenderer();
		renderer.reset(model.getUserID());
		if (model.getBrowserType() != Browser.ADMIN_EXPLORER) {
			ExperimenterData exp = model.getUserDetails();
			TreeImageDisplay node = buildTreeNodes(exp);
			if (model.isSelected() && node != null) {
				expandNode(node, true);
			}
        }
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		tm.reload();
	}

	/** Clear the tree.*/
	void clear()
	{
		TreeImageDisplay root = getTreeRoot();
		root.removeAllChildren();
		root.removeAllChildrenDisplay();
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		tm.reload();
	}
	
	/**
	 * Sets the nodes selecting via other views.
	 * 
	 * @param newSelection	The collection of nodes to select.
	 */
	void setFoundNode(TreeImageDisplay[] newSelection)
	{
		if (newSelection == null) {
		    model.setSelectedDisplay(null, true);
		    treeDisplay.clearSelection();
		    controller.getAction(BrowserControl.DELETE).setEnabled(false);
		} else {
			TreePath[] paths = new TreePath[newSelection.length];
			for (int i = 0; i < newSelection.length; i++) {
				paths[i] = new TreePath(newSelection[i].getPath());
			}
			treeDisplay.setSelectionPaths(paths);
		}
		
		treeDisplay.repaint();
	}
    
	/**
	 * Loads the files contained in the passed folder.
	 * 
	 * @param display The directory.
	 */
	void loadFile(TreeImageDisplay display)
	{
		//if ((uo instanceof File) && (display instanceof TreeImageSet)) {
		display.removeAllChildren();
		display.removeAllChildrenDisplay();
		display.setChildrenLoaded(Boolean.TRUE);
		display.setExpanded(true);
		transformDirectory((TreeImageSet) display);
		buildTreeNode(display, prepareSortedList(sorter.sort(
				display.getChildrenDisplay())), 
				(DefaultTreeModel) treeDisplay.getModel());

		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		tm.reload(display);
		//model.fireFilesCheck(leaves);
	}
	
	/** 
	 * Loads the file system. 
	 * 
	 * @param refresh Pass <code>true</code> to rebuild the file system view.
	 * 				  <code>false</code> otherwise.
	 */
	void loadFileSystem(TreeImageDisplay expNode)
	{
		if (model.getBrowserType() != Browser.FILE_SYSTEM_EXPLORER) 
			return;
		setExperimenterData(createFileSystemExplorer(expNode), expNode);
	}

	/**
	 * Reloads the specified node.
	 * 
	 * @param node The node to reload.
	 */
	void reloadContainer(TreeImageDisplay node)
	{
		if (node == null) return;
		node.removeAllChildren();
		node.removeAllChildrenDisplay();
		node.setChildrenLoaded(Boolean.FALSE);
		buildEmptyNode(node);
		treeDisplay.collapsePath(new TreePath(node.getPath()));
        treeDisplay.expandPath(new TreePath(node.getPath()));
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		tm.reload(node);
	}
	
	/** 
	 * Reloads the specified node.
	 * 
	 * @param node The node to reload.
	 */
	void reloadNode(TreeImageDisplay node)
	{
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		if (node == null) tm.reload();
		else tm.reload(node);
	}
	
	/**
     * Expands the specified node. To avoid loop, we first need to 
     * remove the <code>TreeExpansionListener</code>.
     * 
     * @param node The node to expand.
     */
    void expandNode(TreeImageDisplay node, boolean withListener)
    {
    	 //First remove listener otherwise an event is fired.
    	if (node == null) return;
    	node.setExpanded(true);
    	if (withListener) {
    		treeDisplay.expandPath(new TreePath(node.getPath()));
    	} else {
    		treeDisplay.removeTreeExpansionListener(listener);
    		treeDisplay.expandPath(new TreePath(node.getPath()));
    		treeDisplay.addTreeExpansionListener(listener);
    	}
    }
	
    /**
     * Expands the specified node. To avoid loop, we first need to 
     * remove the <code>TreeExpansionListener</code>.
     * 
     * @param node The node to expand.
     */
    void expandNode(TreeImageDisplay node)
    {
    	expandNode(node, false);
    }
	
    /**
     * Adds the component under the tree.
     * 
     * @param component The component to add.
     */
    void addComponent(JComponent component)
    {
    	if (bottomComponent != null) remove(bottomComponent);
    	bottomComponent = component;
    	if (component != null) add(bottomComponent, BorderLayout.SOUTH);
    	revalidate();
    	repaint();
    }

    /**
	 * Adds the specified groups to the tree.
	 * 
	 * @param group The group to add.
	 */
	void setUserGroup(List<GroupData> groups)
	{
		if (groups == null || groups.size() == 0) return;
		ExperimenterData exp = model.getUserDetails();
		TreeImageSet node;
		Iterator<GroupData> i = groups.iterator();
		while (i.hasNext()) {
			node = createGroup(i.next());
			node = createExperimenterNode(exp, node);
			
		}
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		dtm.reload();
    	
	}
	
    /**
     * Reacts to the D&D properties.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (DnDTree.DRAGGED_PROPERTY.equals(name)) {
			ObjectToTransfer transfer = (ObjectToTransfer) evt.getNewValue();
			int action = TreeViewer.CUT_AND_PASTE;
			if (transfer.getDropAction() == DnDConstants.ACTION_COPY)
				action = TreeViewer.COPY_AND_PASTE;
			//check the node
			model.transfer(transfer.getTarget(), transfer.getNodes(),
					action);
		}
	}

}
