/*
 * org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.browser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Represents a component in the composite structure used to visualize an
 * image hierarchy.
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
public abstract class TreeBrowserDisplay
	extends DefaultMutableTreeNode
{

	/** 
	 * Back pointer to the parent node or <code>null</code> if this is the root.
	 */
	private TreeBrowserDisplay 			parentDisplay;
	
	/** 
     * The set of nodes that have been added to this node.
     * Will always be empty for a leaf node. 
     */
    protected Set<TreeBrowserDisplay>	childrenDisplay;
   
    /** Indicates if the node is expanded or not. */
    private boolean             		expanded;
    
    /** The default icon associated to this node. */
    private Icon						defaultIcon;
    
    /** Flag  indicating that the node is for a menu. */
    private boolean						menuNode;
    
    /**
     * Returns the name of the node.
     * 
     * @return See above.
     */
    private String getNodeName()
    {
    	Object obj = getUserObject();
        if (obj instanceof ProjectData) return ((ProjectData) obj).getName();
        else if (obj instanceof DatasetData) 
            return ((DatasetData) obj).getName();
        else if (obj instanceof ImageData) 
            return ((ImageData) obj).getName();
        else if (obj instanceof ExperimenterData) {
        	ExperimenterData exp = (ExperimenterData) obj;
        	return EditorUtil.formatExperimenter(exp);
        } else if (obj instanceof ScreenData) 
        	return ((ScreenData) obj).getName();
        else if (obj instanceof PlateData) 
        	return ((PlateData) obj).getName();
        else if (obj instanceof TagAnnotationData)
        	return ((TagAnnotationData) obj).getTagValue();
        else if (obj instanceof String) return (String) obj;
        return "";
    }
    /**
     * Constructor used by subclasses.
     * 
     * @param hierarchyObject The original object in the image hierarchy which
     *                        is visualized by this node.  
     *                        Never pass <code>null</code>. 
     */
    protected TreeBrowserDisplay(Object hierarchyObject)
    {
    	this(hierarchyObject, null, false);
    }
    
    /**
     * Constructor used by subclasses.
     * 
     * @param hierarchyObject 	The original object in the image hierarchy which
     *                        	is visualized by this node.  
     *                        	Never pass <code>null</code>.
     * @param defaultIcon		The default icon associated to this node. 
     * @param menuNode			Pass <code>true</code> to indicate that the node
     * 							is a menu node, <code>false</code> otherwise.
     */
    protected TreeBrowserDisplay(Object hierarchyObject, Icon defaultIcon,
								boolean menuNode)
    {
        super();
        if (hierarchyObject == null) 
            throw new NullPointerException("No hierarchy object.");
        setUserObject(hierarchyObject);
        childrenDisplay = new HashSet<TreeBrowserDisplay>();
        this.defaultIcon = defaultIcon;
        this.menuNode = menuNode;
    }
    
    /**
     * Returns <code>true</code> to indicate that the node is part of a 
     * menu, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isMenuNode() { return menuNode; }
    
    /**
     * Returns the default icon associated to this node. 
     *  
     * @return See above.
     */
    public Icon getDefaultIcon() { return defaultIcon; }
    
    /**
     * Returns <code>true</code> if the nodes contain children,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean hasChildrenDisplay()
    { 
        return (childrenDisplay.size() != 0);
    }
    
    /**
     * Returns the parent node to this node in the visualization tree.
     * 
     * @return The parent node or <code>null</code> if this node has no parent.
     *          This can happen if this node hasn't been linked yet or if it's
     *          the root node.
     */
    public TreeBrowserDisplay getParentDisplay() { return parentDisplay; }
    
    /**
     * Returns all the child nodes to this node in the visualization tree.
     * Note that, although never <code>null</code>, the returned set may be
     * empty.  In particular, this is always the case for a leaf node &#151;
     * that is an {@link TreeBrowserNode}.
     * 
     * @return A <i>read-only</i> set containing all the child nodes.
     */
    public Set getChildrenDisplay() 
    { 
        return Collections.unmodifiableSet(childrenDisplay);
    }
    
    /**
     * Adds a node to the visualization tree as a child of this node.
     * The <code>child</code>'s parent is set to be this node.  If <code>
     * child</code> is currently a child to another node <code>n</code>, 
     * then <code>child</code> is first 
     * {@link #removeChildDisplay(TreeBrowserDisplay) removed} from <code>n</code>
     * and then added to this node. 
     * 
     * @param child The node to add. Mustn't be <code>null</code>.
     * @see DefaultMutableTreeNode
     */
    public void addChildDisplay(TreeBrowserDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) return;
        if (child.parentDisplay != null)  //Was the child of another node.
            child.parentDisplay.removeChildDisplay(child);
        child.parentDisplay = this;
        childrenDisplay.add(child);
    }
    
    /**
     * Removes the specified <code>child</code> node.
     * If <code>child</code> is not among the children of this node, no action
     * is taken. Otherwise, it is removed from the children set and orphaned.
     * That is, its parent (which is this node) is set to <code>null</code>.
     * 
     * @param child The node to remove. Mustn't be <code>null</code>.
     */
    public void removeChildDisplay(TreeBrowserDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) {
            //NOTE: parentDisplay != null b/c child has been added through
            //the add method.
            child.parentDisplay.childrenDisplay.remove(child);
            child.parentDisplay = null;
        }
    }
    
    /** Removes all <code>children</code> nodes from the children set. */
    public void removeAllChildrenDisplay()
    {
        Iterator i = childrenDisplay.iterator();
        Set<Object> toRemove = new HashSet<Object>(childrenDisplay.size());
        while (i.hasNext())
            toRemove.add(i.next());
        i = toRemove.iterator();
        while (i.hasNext())
            removeChildDisplay((TreeBrowserDisplay) i.next());
    }
    
    /**
     * Removes the children contained in the passed collection.
     * 
     * @param children The collection to handle.
     */
    public void removeChildrenDisplay(List children)
    {
    	if (children == null) return;
    	Iterator i = children.iterator();
    	while (i.hasNext()) 
    		removeChildDisplay((TreeBrowserDisplay) i.next());
    }
    
    /** 
     * Returns <code>true</code> is the node is expanded, 
     * <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean isExpanded() { return expanded; }
    
    /**
     * Sets to <code>true</code> if the node is expanded, 
     * <code>false</code> otherwise.
     * 
     * @param expanded The value to set.
     */
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
    
    /** 
     * Overridden to return the name of the hierarchy object. 
     * @see Object#toString()
     */
    public String toString() { return getNodeName(); }
    
    /**
     * Made final to ensure objects are compared by reference so that the
     * {@link #addChildDisplay(TreeBrowserDisplay) addChildDisplay} and
     * {@link #removeChildDisplay(TreeBrowserDisplay) removeChildDisplay} methods
     * will work fine.
     * @see Object#equals(Object)
     */
    public final boolean equals(Object x) { return (this == x); }
    
    /**
     * Implemented by subclasses to call the right version of the <code>visit
     * </code> method on the specified <code>visitor</code>.
     * This method is called during
     * the nodes iteration. Subclasses will just call the <code>visit</code>
     * method passing a reference to <code>this</code>.
     * 
     * @param visitor The visitor.  Will never be <code>null</code>.
     */
    protected abstract void doAccept(TreeBrowserVisitor visitor);
    
    /**
     * Tells if the children of this node were requested.
     * 
     * @return  <code>true</code> if the children were requested,
     *          <code>false</code> otherwise.
     */
    public abstract boolean isChildrenLoaded();
    
    /**
     * Indicates if the children were requested for this node.
     * 
     * @param childrenLoaded    The value to set.
     */
    public abstract void setChildrenLoaded(Boolean childrenLoaded);
    
}
