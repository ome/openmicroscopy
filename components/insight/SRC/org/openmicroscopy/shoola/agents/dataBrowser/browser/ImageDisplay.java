/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.tpane.TinyPane;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Represents a component in the composite structure used to visualize an
 * image hierarchy.
 * <p>A concrete component can be either an {@link ImageNode}, to represent a
 * single image, or an {@link ImageSet}, to represent a collection of images.
 * An {@link ImageSet} can also contain other image sets, thus leading to a
 * composite structure.  This is a tree whose leaf nodes are {@link ImageNode}
 * objects and internal nodes are {@link ImageSet} objects.</p>
 * <p>So we have a general purpose, set-based structure we can use to visualize 
 * any image hierarchy: Project/Dataset/Image, Screen/Plate/Well/Image, etc.
 * The original data hierarchy translates into a visualization tree as follows.
 * Each image object corresponds to an {@link ImageNode} and an image container,
 * such as Dataset, corresponds to an {@link ImageSet}. 
 * All {@link ImageNode} objects that are
 * created for the images in a given image container are added to the 
 * {@link ImageSet} object created for that image container.  Nested containers
 * translate into nested {@link ImageSet}s. For example, say you have a 
 * Project <code>p_1</code> and two datasets in it, <code>d_1</code> and <code>
 * d_2</code>.  The former contains image <code>i_1</code> and <code>i_2</code>,
 * as the latter only has one image, <code>i_3</code>.  This would translate
 * into three {@link ImageNode}s <code>in_1, in_2, in_3</code>, respectively
 * for <code>i_1, i_2, i_3</code>.  You would then create two {@link ImageSet}s
 * <code>ds_1, ds_2</code>, respectively for <code>d_1, d_2</code>, and add
 * <code>in_1, in_2</code> to <code>ds_1</code> and <code>in_3</code> to <code>
 * ds_2</code>.  Finally you would create a third {@link ImageSet} <code>ps_1
 * </code> for the Project and add <code>ds_1, ds_2</code> to it.</p> 
 * <p>Operations on a visualization tree are performed through visitors.  The
 * {@link ImageDisplayVisitor} interface allows you to define arbitrary 
 * operations that can then be applied to the tree by calling the 
 * {@link #accept(ImageDisplayVisitor) accept} method, usually on the root node.
 * An example of this is layout management. In fact, an {@link ImageSet} can
 * contain other nodes &#151; this class inherits from {@link TinyPane} and
 * nodes are added to its internal desktop, which has no layout manager. In
 * order to position the contained nodes properly, you can write a layout class
 * that implements the {@link ImageDisplayVisitor} interface to lay out the
 * contents of every {@link ImageSet} node in a visualization tree.</p>
 * 
 * @see ImageNode
 * @see ImageSet
 * @see ImageDisplayVisitor
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public abstract class ImageDisplay
    extends TinyPane
{
    
    /** Bound property indicating an annotation visualization. */
    public final static String     ANNOTATE_NODE_PROPERTY = "annotateNode";
    
    /** The icon for data not owned.*/
    private final static Icon NOT_OWNED_ICON;
    
    /** The icon for annotation.*/
    private final static Icon ANNOTATION_ICON;
    
    static {
    	IconManager icons = IconManager.getInstance();
    	NOT_OWNED_ICON = icons.getIcon(IconManager.NOT_OWNER_8);
    	ANNOTATION_ICON = icons.getIcon(IconManager.ANNOTATION_8);
    }
    /** 
     * Back pointer to the parent node or <code>null</code> if this is the root.
     */
    protected ImageDisplay  	parentDisplay;
    
    /** 
     * The set of nodes that have been added to this node.
     * Will always be empty for a leaf node. 
     */
    private List<ImageDisplay>	childrenDisplay;
    
    /** 
     * The original object in the image hierarchy which is visualized by this 
     * node.
     */
    protected Object       	 	hierarchyObject;
    
    /** The annotation count.*/
    private int count;
    
    /**
     * Returns the owner of the data object or <code>null</code>.
     * 
     * @return See above.
     */
    protected ExperimenterData getNodeOwner()
    {
    	if (hierarchyObject instanceof ExperimenterData ||
    			hierarchyObject instanceof GroupData)
    		return null;
    	if (hierarchyObject instanceof DataObject)
    		return ((DataObject) hierarchyObject).getOwner();
    	return null;
    }
    
    /**
     * Checks if the algorithm to visit the tree is one of the constants
     * defined by {@link ImageDisplayVisitor}.
     * 
     * @param type The algorithm type.
     * @return  Returns <code>true</code> if the type is supported,
     *          <code>false</code> otherwise.
     */
    private boolean checkAlgoType(int type)
    {
        switch (type) {
            case ImageDisplayVisitor.IMAGE_NODE_ONLY:
            case ImageDisplayVisitor.IMAGE_SET_ONLY:
            case ImageDisplayVisitor.ALL_NODES:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Returns the partial name of the image's name.
     * 
     * @param originalName The original name.
     * @return See above.
     */
    protected String getPartialName(String originalName)
    {
    	return EditorUtil.getPartialName(originalName);
    }
    
    /**
     * Constructor used by subclasses.
     * 
     * @param title The frame's title. 
     * @param note	The note added to the frame's title.
     * @param hierarchyObject The original object in the image hierarchy which
     *                        is visualized by this node.
     *                        Never pass <code>null</code>.
     */
    protected ImageDisplay(String title, String note, Object hierarchyObject)
    {
        super(title, note);
        if (hierarchyObject == null) 
            throw new NullPointerException("No hierarchy object.");
        count = 0;
        this.hierarchyObject = hierarchyObject;
        childrenDisplay = new ArrayList<ImageDisplay>();
        setToolTipText(getNodeName());
    }
    
    /**
     * Returns the parent node to this node in the visualization tree.
     * 
     * @return The parent node or <code>null</code> if this node has no parent.
     *          This can happen if this node hasn't been linked yet or if it is
     *          the root node.
     */
    public ImageDisplay getParentDisplay() { return parentDisplay; }
    
    /**
     * Returns all the child nodes to this node in the visualization tree.
     * Note that, although never <code>null</code>, the returned set may be
     * empty.  In particular, this is always the case for a leaf node &#151;
     * that is an {@link ImageNode}.
     * 
     * @return A <i>read-only</i> set containing all the child nodes.
     */
    public Collection<ImageDisplay> getChildrenDisplay() 
    { 
        return Collections.unmodifiableList(childrenDisplay);
    }
    
    /**
     * Adds a node to the visualization tree as a child of this node.
     * The node is added to the internal desktop of this node, but you
     * will have to set its bounds for it to show up &#151; this is a
     * consequence of the fact that a desktop has no layout manager.
     * The <code>child</code>'s parent is set to be this node.  If <code>
     * child</code> is currently a child to another node <code>n</code>,
     * then <code>child</code> is first 
     * {@link #removeChildDisplay(ImageDisplay) removed} from <code>n</code>
     * and then added to this node. 
     * 
     * @param child The node to add.  Mustn't be <code>null</code>.
     * @see TinyPane
     */
    public void addChildDisplay(ImageDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) return;
        if (child.parentDisplay != null)  //Was the child of another node.
            child.parentDisplay.removeChildDisplay(child);
        child.parentDisplay = this;
        childrenDisplay.add(child);
        ((JLayeredPane) getInternalDesktop()).add(child, Integer.valueOf(0));
    }
    
    /**
     * Removes the specified <code>child</code> node.
     * If <code>child</code> is not among the children of this node, no action
     * is taken.  Otherwise, it is removed from the children set and orphaned.
     * That is, its parent (which is this node) is set to <code>null</code>.
     * The specified <code>child</code> component is also removed from the
     * internal desktop of this node.
     * 
     * @param child The node to remove.  Mustn't be <code>null</code>.
     */
    public void removeChildDisplay(ImageDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) {
            //NOTE: parentDisplay != null b/c child has been added through
            //the add method.
            child.parentDisplay.childrenDisplay.remove(child);
            child.parentDisplay.getInternalDesktop().remove(child);
            child.parentDisplay = null;
        }
    }
    
    /** Removes all <code>children</code> nodes from the children set. */
    public void removeAllChildrenDisplay()
    {
    	Iterator<ImageDisplay> i = childrenDisplay.iterator();
        Set<ImageDisplay> toRemove = 
        		new HashSet<ImageDisplay>(childrenDisplay.size());
        while (i.hasNext())
            toRemove.add(i.next());
        i = toRemove.iterator();
        while (i.hasNext())
            removeChildDisplay((ImageDisplay) i.next());
    }
    
    /**
     * Updates the hierarchy object.
     * 
     * @param ho    The hierarchy object to set. Mustn't be <code>null</code>.
     */
    public void setHierarchyObject(Object ho)
    {
        if (ho == null) 
            throw new NullPointerException("No hierarchy object.");
        hierarchyObject = ho;
        setToolTipText(toString());
    }
    
    /**
     * Returns the original object in the image hierarchy which is visualized 
     * by this node.
     * 
     * @return The image hierarchy object.  Never <code>null</code>.
     */
    public Object getHierarchyObject() { return hierarchyObject; }
    
    /**
     * Adds an <code>Annotated</code> icon if the object 
     * has annotation linked to it. Adds an <code>Owner</code> icon if
     * the owner is not the user currently logged in.
     * 
     * @param userID The id of the user currently logged in.
     */
    public void setNodeDecoration(long userID)
    {
    	List<JLabel> nodes = new ArrayList<JLabel>();
    	if (EditorUtil.isAnnotated(hierarchyObject, count))
    		nodes.add(new JLabel(ANNOTATION_ICON));
    	
    	if (nodes.size() > 0) setDecoration(nodes);
    	validate();
    	repaint();
    }
    
    /**
     * Adds an <code>Annotated</code> icon if the object 
     * has annotation linked to it. Adds an <code>Owner</code> icon if
     * the owner is not the user currently logged in.
     */
    public void setNodeDecoration() { setNodeDecoration(-1); }
    
    /** 
     * Sets the annotation count.
     * 
     * @param count The value to set.
     */
    public void setAnnotationCount(int count)
    { 
    	this.count = count;
    	noDecoration();
    	setNodeDecoration();
    	validate();
    	repaint();
    }
    
    /**
     * Has the specified object visit this node and all nodes below this one
     * in the visualization tree.
     * For each node, the <code>visit</code> method is called passing in the
     * node being visited.
     * 
     * @param visitor The visitor Mustn't be <code>null</code>.
     * @see ImageDisplayVisitor
     */
    public void accept(ImageDisplayVisitor visitor)
    {
        if (visitor == null) throw new NullPointerException("No visitor.");
        accept(visitor, ImageDisplayVisitor.ALL_NODES);
    }
    
    /**
     * Has the specified object visit this node and all nodes below this one
     * in the visualization tree.
     * According to the specified <code>algoType</code>,
     * the <code>visit</code> method is called passing in the
     * node being visited.
     * 
     * @param visitor   The visitor.  Mustn't be <code>null</code>.
     * @param algoType  The algorithm selected. Must be one of the constants
     *                  defined by {@link ImageDisplayVisitor}.
     * @see ImageDisplayVisitor
     */
    public void accept(ImageDisplayVisitor visitor, int algoType)
    {
        if (visitor == null) throw new NullPointerException("No visitor.");
        if (!checkAlgoType(algoType))
            throw new IllegalArgumentException("Algorithm not supported.");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Iterator<ImageDisplay> i = childrenDisplay.iterator();
        ImageDisplay child;
        switch (algoType) {
            case ImageDisplayVisitor.IMAGE_NODE_ONLY:
                while (i.hasNext()) {
                    i.next().accept(visitor, algoType);
                }
                if (this instanceof ImageNode) doAccept(visitor);
                break;
            case ImageDisplayVisitor.IMAGE_SET_ONLY:
                while (i.hasNext()) {
                    child = i.next();
                    if (child instanceof ImageSet)
                        child.accept(visitor, algoType);
                }
                if (this instanceof ImageSet) doAccept(visitor);
                break;
            case ImageDisplayVisitor.ALL_NODES:
                while (i.hasNext()) {
                   i.next().accept(visitor, algoType);
                }
                doAccept(visitor);
                break;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * Returns the name of the node.
     * 
     * @return See above.
     */
    public String getNodeName()
    { 
        if (hierarchyObject instanceof ProjectData)
        	return ((ProjectData) hierarchyObject).getName();
        else if (hierarchyObject instanceof DatasetData)
            return ((DatasetData) hierarchyObject).getName();
        else if (hierarchyObject instanceof ImageData)
            return ((ImageData) hierarchyObject).getName();
        else if (hierarchyObject instanceof ExperimenterData) {
        	ExperimenterData exp = (ExperimenterData) hierarchyObject;
        	return exp.getFirstName()+" "+exp.getLastName();
        } else if (hierarchyObject instanceof String)
        	return (String) hierarchyObject;
        return "";
    }
    
    /** 
     * Overridden to return the name of the hierarchy object.
     * @see Object#toString()
     */
    public String toString()
    {
        String s = "";
        if (hierarchyObject instanceof ProjectData)
            s = ((ProjectData) hierarchyObject).getName();
        else if (hierarchyObject instanceof DatasetData)
            s = ((DatasetData) hierarchyObject).getName();
        else if (hierarchyObject instanceof ImageData)
            s = getPartialName(((ImageData) hierarchyObject).getName());
        else if (hierarchyObject instanceof ExperimenterData) {
        	ExperimenterData exp = (ExperimenterData) hierarchyObject;
        	s = exp.getFirstName()+" "+exp.getLastName();
        }
        return s;
    }
    
    /**
     * Made final to ensure objects are compared by reference so that the
     * {@link #addChildDisplay(ImageDisplay) addChildDisplay} and
     * {@link #removeChildDisplay(ImageDisplay) removeChildDisplay} methods
     * will work fine.
     * @see Object#equals(Object)
     */
    public final boolean equals(Object x) { return (this == x); }
    
    /**
     * Implemented by subclasses to call the right version of the <code>visit
     * </code> method on the specified <code>visitor</code>.
     * This method is called by {@link #accept(ImageDisplayVisitor)} during
     * the nodes iteration.  Subclasses will just call the <code>visit</code>
     * method passing a reference to <code>this</code>.
     * 
     * @param visitor The visitor.  Will never be <code>null</code>.
     */
    protected abstract void doAccept(ImageDisplayVisitor visitor);
    
    /**
     * Tells if the children of this node are {@link ImageNode}s.
     * 
     * @return <code>true</code> if there's at least one {@link ImageNode}
     *          child, <code>false</code> otherwise.
     */
    public abstract boolean containsImages();
    
    /** 
     * Adds the specified listener to the passed components.
     * 
     * @param listener The listener to add.
     */
    public abstract void addListenerToComponents(Object listener);

}
