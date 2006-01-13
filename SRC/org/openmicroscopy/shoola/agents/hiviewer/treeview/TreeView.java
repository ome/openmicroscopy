/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeView
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.hiviewer.treeview;



//Java imports
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;

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
    public static final String 	SELECTED_DISPLAY_PROPERTY = 
        										"treeSelectedDisplay";
    
    /** 
     * Bound property name indicating an {@link ImageDisplay} object has been
     * selected in the visualization tree. 
     */
    public static final String 	TREE_POPUP_POINT_PROPERTY = "treePopupPoint";
    
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
    
    /**
     * Brings on screen the selected node. The nodes containing the child
     * are visited i.e. parent then grandparent all the way up to the root node.
     * 
     * @param childBounds 	The bounds of the selected node.
     * @param parent 		The node containing the child.
     * @param isRoot		<code>true</code> if its the root node, 
     * 						<code>false</code> otherwise.	
     */
    private void scrollToNode(Rectangle childBounds, ImageDisplay parent,
                                boolean isRoot)
    {
        JScrollPane dskDecorator = parent.getDeskDecorator();
        Rectangle viewRect = dskDecorator.getViewport().getViewRect();
        if (!viewRect.contains(childBounds)) {
            JScrollBar vBar = dskDecorator.getVerticalScrollBar();
            JScrollBar hBar = dskDecorator.getHorizontalScrollBar();
            vBar.setValue(childBounds.y);
            hBar.setValue(childBounds.x);
        }
        if (!isRoot) {
            ImageDisplay node = parent.getParentDisplay();
            scrollToNode(childBounds, node, (node.getParentDisplay() == null));       
        }      
    }
    
    /** Builds and lays out the UI. */
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
     * @param rootNode The root node of the tree. Mustn't be <code>null</code>;
     */
    public TreeView(JComponent rootNode)
    {
        if (rootNode == null) throw new IllegalArgumentException("No root");
        if (!(rootNode instanceof ImageDisplay))
            throw new IllegalArgumentException("Root must be an instance of " +
            		"ImageDisplay");
        uiDelegate = new TreeViewUI(this, (ImageDisplay) rootNode);
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
     * Sets the selected node. 
     * 
     * @param selectedDisplay The selected node.
     */
    void setSelectedDisplay(ImageDisplay selectedDisplay)
    {
        selectedNode = (TreeViewNode) 
        				uiDelegate.getTree().getLastSelectedPathComponent();
        //Bring the node on screen.
        ImageDisplay parent = selectedDisplay.getParentDisplay();
        if (parent != null)
            scrollToNode(selectedDisplay.getBounds(), parent,
                    (parent.getParentDisplay() == null));  
        firePropertyChange(SELECTED_DISPLAY_PROPERTY, null, selectedDisplay);
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
     * Returns <code>true</code> if the component is visible, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isDisplay() { return display; }

    /**
     * Returns the location of mouse click.
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
     * @param visitor The visitor.
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
     * @param visitor 	The visitor.
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

}
