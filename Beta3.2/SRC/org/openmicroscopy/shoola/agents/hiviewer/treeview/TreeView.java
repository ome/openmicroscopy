/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeView
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.hiviewer.treeview;



//Java imports
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * A Component hosting a tree view of the data displayed in the 
 * {@link org.openmicroscopy.shoola.agents.hiviewer.browser.Browser}.
 * <p>
 * This component has a <code>Menu bar</code> and a <code>JTree</code>
 * hosting the display.
 * The menu bar is composed a label presenting the number of images 
 * currently on screen and a <code>Tool Bar</code>. 
 * The tool bar hosts the {@link CollapseButton} and {@link CloseButton}.
 * </p>
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class TreeView
	extends JPanel
{
    
    /** 
     * Bound property name indicating to remove the component from the display.
     */
    public static final String 	CLOSE_PROPERTY = "close";
    
    /** 
     * Bound property name indicating an {@link ImageDisplay} object has been
     * selected in the visualization tree. 
     */
    public static final String 	TREE_SELECTED_DISPLAY_PROPERTY = 
       										"treeSelectedDisplay";
    
    /** 
     * Bound property name indicating an {@link ImageDisplay} object has been
     * selected in the visualization tree. 
     */
    public static final String 	TREE_POPUP_POINT_PROPERTY = "treePopupPoint";
    
    /** 
     * Bound property name indicating to remove the magnified node 
     * from the display.
     */
    public static final String 	REMOVE_ROLL_OVER_PROPERTY = "removeRollOver";
    
    /** Indicates if the component is visible on screen. */
    private boolean 				display;

    /** The location of the mouse click. */
    private Point					popupPoint;
    
    /** The UI delegate. */
    private TreeViewUI				uiDelegate;
    
    /** The selected node in the tree. */
    private TreeViewNode			selectedNode;

    /** 
     * Controls if the specified algorithm is supported.
     * 
     * @param algo The algorithm to control.
     */
    private void checkAlgo(int algo)
    {
        switch (algo) {
	        case TreeViewNodeVisitor.IMAGE_NODE_ONLY:
	        case TreeViewNodeVisitor.IMAGE_SET_ONLY:  
	        case TreeViewNodeVisitor.ALL_NODES:
	            return;
	        default:
	            throw new IllegalArgumentException("Algo not supported.");
        }
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder());
        add(uiDelegate.getMenuBar(), BorderLayout.NORTH);
        add(new JScrollPane(uiDelegate.getTree()), BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param rootNode  The root node of the tree. Mustn't be <code>null</code>.
     */
    public TreeView(ImageDisplay rootNode)
    {
        if (rootNode == null) throw new IllegalArgumentException("No root");
        uiDelegate = new TreeViewUI(this, rootNode);
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { removeRollOver(); }
        });
        buildGUI();
    }
        
    /** Hides the component. */
    void close()
    {
        if (!display) return;
        firePropertyChange(CLOSE_PROPERTY, Boolean.FALSE, Boolean.TRUE);
    }
    
    /**
     * Collapses the specified node.
     * 
     * @param node The node to collapse.
     */
    void collapseNode(TreeViewNode node)
    {
        if (node == null) return;
        TreeViewNode root = uiDelegate.getRoot();
        if (root.equals(node)) return;
        uiDelegate.getTree().collapsePath(new TreePath(node.getPath()));
    }
    
    /**
     * Selects the specified node.
     * 
     * @param node The selected node.
     */
    void selectNode(TreeViewNode node)
    {
        uiDelegate.selectNodes(selectedNode, node);
        selectedNode = node;
    }
    
    /**
     * Sets the selected nodes.
     * 
     * @param nodes	Collection of selected nodes.
     * @param lastSelected
     */
    void setSelectedDisplays(List nodes, TreeViewNode lastSelected)
    {
    	if (nodes == null || nodes.size() == 0) return;
    	Iterator i = nodes.iterator();
    	ImageDisplay[] uos = new ImageDisplay[nodes.size()];
    	int index = 0;
    	while (i.hasNext()) 
    		uos[index] = (ImageDisplay) 
    		((TreeViewNode) i.next()).getUserObject();

    	selectedNode = lastSelected;
    	ImageDisplay uo = (ImageDisplay) lastSelected.getUserObject();
    	firePropertyChange(TREE_SELECTED_DISPLAY_PROPERTY, null, uos);
        firePropertyChange(HiViewer.SCROLL_TO_NODE_PROPERTY, null, uo);
    }
    
    /** 
     * Sets the selected node. 
     * 
     * @param node The selected node.
     */
    void setSelectedDisplay(TreeViewNode node)
    {
    	if (node == null) return;
        selectedNode = node;
        ImageDisplay uo = (ImageDisplay) node.getUserObject();
        firePropertyChange(TREE_SELECTED_DISPLAY_PROPERTY, null, uo);
        firePropertyChange(HiViewer.SCROLL_TO_NODE_PROPERTY, null, uo);
    }
    
    /**
     * Sets the location of the mouse click.
     * 
     * @param p The location of the mouse click.
     */
    void setPopupPoint(Point p)
    { 
        popupPoint = p; 
        firePropertyChange(TREE_POPUP_POINT_PROPERTY, null, 
                			uiDelegate.getTree());
    }
    
    /** 
     * Removes the thumbnail from the display when the magnification 
     * is on.
     */
	void removeRollOver()
	{
		firePropertyChange(REMOVE_ROLL_OVER_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
    /**
     * Returns <code>true</code> if the component is visible, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isDisplay() { return display; }

    /**
     * Returns the location of the mouse click.
     * 
     * @return See above.
     */
    public Point getPopupPoint() { return popupPoint; }
    
    /**
     * Sets to <code>true</code> to show the component,
     * <code>false</code> to hide.
     * 
     * @param d The flag to set.
     */
    public void setDisplay(boolean d) { display = d; }

    /**
     * Has the specified object visit all the visualization nodes hosted by
     * the tree.
     * 
     * @param visitor The visitor to accept.
     * @see TreeViewNodeVisitor
     */
    public void accept(TreeViewNodeVisitor visitor)
    {
        accept(visitor, TreeViewNodeVisitor.ALL_NODES);
    }
    
    /**
     * Has the specified object visit all the visualization nodes hosted by
     * the tree.
     * 
     * @param visitor 	The visitor to accept.
     * @param algo 		The algorithm selected to visit the visualization trees.
     *                  One of the constants defined by 
     * 					{@link TreeViewNodeVisitor}.
     * @see TreeViewNodeVisitor
     */
    public void accept(TreeViewNodeVisitor visitor, int algo)
    {
        checkAlgo(algo);
        DefaultTreeModel model = 
            	(DefaultTreeModel) uiDelegate.getTree().getModel();
        TreeViewImageSet root = (TreeViewImageSet) model.getRoot();
        root.accept(visitor, algo);
    }

    /**
     * Sorts the nodes by name or date depending on the specified index.
     * 
     * @param index     The index indicating to sort the nodes by name or date.
     * @param rootNode  The root node of the tree. Mustn't be <code>null</code>.
     */
    public void sortNodes(int index, ImageDisplay rootNode)
    {
        uiDelegate.sortNodes(index, rootNode);
    }
    
    /** Expands the tree. */
    public void expandTree() { uiDelegate.expandTree(); }
    
}
