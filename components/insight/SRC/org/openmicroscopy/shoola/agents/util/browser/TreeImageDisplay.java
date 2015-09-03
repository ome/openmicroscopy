/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.browser;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FileData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.MultiImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Represents a component in the composite structure used to visualize an
 * image hierarchy. 
 * <p>A concrete component can be either a {@link TreeImageNode}, to represent a
 * single image, or an {@link TreeImageSet}, to represent a collection of 
 * images. An {@link TreeImageSet} can also contain other image sets, thus 
 * leading to a composite structure. This is a tree whose leaf nodes are 
 * {@link TreeImageNode} objects and internal nodes are {@link TreeImageSet} 
 * objects.</p>
 * <p>So we have a general purpose, set-based structure we can use to visualize 
 * any image hierarchy: Project/Dataset/Image, Screen/Plate/Well/Image. 
 * The original data hierarchy translates into
 * a visualization tree as follows. Each image object corresponds to an
 * {@link TreeImageNode} and an image container, such as Dataset or Plate, 
 * corresponds to an {@link TreeImageSet}. All {@link TreeImageNode} objects
 * that are created for the images in a given image container are added to the 
 * {@link TreeImageSet} object created for that image container. Nested
 * containers translate into nested {@link TreeImageSet}s. For example, say you 
 * have a Project <code>p_1</code> and two datasets in it, <code>d_1</code> 
 * and <code>d_2</code>. The former contains image <code>i_1</code> and 
 * <code>i_2</code>, as the latter only has one image, <code>i_3</code>. 
 * This would translate into three {@link TreeImageNode}s 
 * <code>in_1, in_2, in_3</code>, respectively for <code>i_1, i_2, i_3</code>.
 * You would then create two {@link TreeImageSet}s <code>ds_1, ds_2</code>,
 * respectively for <code>d_1, d_2</code>, and add <code>in_1, in_2</code> to 
 * <code>ds_1</code> and <code>in_3</code> to <code> ds_2</code>. Finally you
 * would create a third {@link TreeImageSet} <code>ps_1</code> for the Project 
 * and add <code>ds_1, ds_2</code> to it.</p> 
 * <p>Operations on a visualization tree are performed through visitors.  The
 * {@link TreeImageDisplayVisitor} interface allows you to define arbitrary 
 * operations that can then be applied to the tree by calling the 
 * {@link #accept(TreeImageDisplayVisitor) accept} method, usually on the root 
 * node.
 * An example of this is layout management. In fact, an {@link TreeImageSet} can
 * contain other nodes &#151; this class inherits from 
 * {@link DefaultMutableTreeNode} and nodes are added to its internal desktop,
 * which has no layout manager. In order to position the contained nodes
 * properly, you can write a layout class that implements the 
 * {@link TreeImageDisplayVisitor} interface to lay out the contents of
 * every {@link TreeImageSet} node in a visualization tree.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public abstract class TreeImageDisplay
    extends DefaultMutableTreeNode
{
    
    /** Identifies the <code>plain</code> style constant. */
    public static final int     FONT_PLAIN = Font.PLAIN;
    
    /** Identifies the <code>bold</code> style constant. */
    public static final int     FONT_BOLD = Font.BOLD;
    
    /** The gap between the number of items and the name. */
    private static final String SPACE = " ";
    
    /** 
     * Back pointer to the parent node or <code>null</code> if this is the root.
     */
    private TreeImageDisplay    		parentDisplay;
    
    /** 
     * The set of nodes that have been added to this node.
     * Will always be empty for a leaf node. 
     */
    protected List<TreeImageDisplay>	childrenDisplay;
    
    /**
     * The text of the tool tip: annotation if the <code>DataObject</code>
     * can be annotated and the inserted date if any.
     */
    private String              		tooltip;
    
    /** 
     * Tells if the node has to be highlighted.
     * If <code>null</code>, the node will display the normal
     * background. If a color is specified, the node will be highlighted
     * using the specified color. 
     */
    private Color						highlight;
    
    /** The font style used for the node. */
    private int                 		fontStyle;
    
    /** Indicates if the node is expanded or not. */
    private boolean             		expanded;
    
    /** Indicates to display a truncated name. */
    private boolean						partialName;

    /** Flag indicating if the node can be selected. */
    protected boolean					selectable;
    
    /** The number of items. */
    protected long						numberItems;

    /** Flag indicating that the node has to be refreshed. */
    protected boolean					toRefresh;
    
    /** Indicates to display or not the number of items.*/
    private boolean displayItems;
    
    /** The annotation count.*/
    private int count;
    
    /**
     * Checks if the algorithm to visit the tree is one of the constants
     * defined by {@link TreeImageDisplayVisitor}.
     * 
     * @param type The algorithm type.
     * @return  Returns <code>true</code> if the type is supported,
     *          <code>false</code> otherwise.
     */
    private boolean checkAlgoType(int type)
    {
        switch (type) {
            case TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY:
            case TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY:
            case TreeImageDisplayVisitor.ALL_NODES:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Constructor used by subclasses.
     * 
     * @param hierarchyObject The original object in the image hierarchy which
     *                        is visualized by this node.
     *                        Never pass <code>null</code>. 
     */
    protected TreeImageDisplay(Object hierarchyObject)
    {
        super();
        if (hierarchyObject == null) 
            throw new NullPointerException("No hierarchy object.");
        setUserObject(hierarchyObject);
        selectable = true;
        childrenDisplay = new ArrayList<TreeImageDisplay>();
        numberItems = -1;
        partialName = true;
        fontStyle = FONT_PLAIN;
        displayItems = true;
        count = 0;
    }
    
    /**
     * Sets to <code>true</code> if the partial name of the node is shown, to
     * <code>false</code> otherwise.
     * 
     * @param b The value to set.
     */
    void setPartialName(boolean b) { partialName = b; }

    /**
     * Returns <code>true</code> if the partial name of the node is shown, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isPartialName() { return partialName; }
    
    /**
     * Sets to <code>true</code> if the node is expanded, 
     * <code>false</code> otherwise.
     * 
     * @param expanded The value to set.
     */
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
    
    /** 
     * Returns <code>true</code> is the node is expanded, 
     * <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean isExpanded() { return expanded; }
    
    /**
     * Returns the parent node to this node in the visualization tree.
     * 
     * @return 	The parent node or <code>null</code> if this node has no parent.
     *          This can happen if this node hasn't been linked yet or if it's
     *          the root node.
     */
    public TreeImageDisplay getParentDisplay() { return parentDisplay; }
    
    /**
     * Returns all the child nodes to this node in the visualization tree.
     * Note that, although never <code>null</code>, the returned set may be
     * empty.  In particular, this is always the case for a leaf node &#151;
     * that is an {@link TreeImageNode}.
     * 
     * @return A <i>read-only</i> set containing all the child nodes.
     */
    public List getChildrenDisplay() 
    { 
        return Collections.unmodifiableList(childrenDisplay);
    }
    
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
     * Adds a node to the visualization tree as a child of this node.
     * The <code>child</code>'s parent is set to be this node.  If <code>
     * child</code> is currently a child to another node <code>n</code>, 
     * then <code>child</code> is first 
     * {@link #removeChildDisplay(TreeImageDisplay) removed} from <code>n</code>
     * and then added to this node. 
     * 
     * @param child The node to add. Mustn't be <code>null</code>.
     * @see DefaultMutableTreeNode
     */
    public void addChildDisplay(TreeImageDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) return;
        if (child.parentDisplay != null)  //Was the child of another node.
            child.parentDisplay.removeChildDisplay(child);
        child.parentDisplay = this;
        childrenDisplay.add(child);
        numberItems = childrenDisplay.size();
    }
    
    /**
     * Removes the specified <code>child</code> node.
     * If <code>child</code> is not among the children of this node, no action
     * is taken. Otherwise, it is removed from the children set and orphaned.
     * That is, its parent (which is this node) is set to <code>null</code>.
     * 
     * @param child The node to remove. Mustn't be <code>null</code>.
     */
    public void removeChildDisplay(TreeImageDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) {
            //NOTE: parentDisplay != null b/c child has been added through
            //the add method.
            child.parentDisplay.childrenDisplay.remove(child);
            child.parentDisplay = null;
            numberItems = childrenDisplay.size();
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
            removeChildDisplay((TreeImageDisplay) i.next());
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
    		removeChildDisplay((TreeImageDisplay) i.next());
    }
    
    /**
     * Has the specified object visit this node and all nodes below this one
     * in the visualization tree.
     * For each node, the <code>visit</code> method is called passing in the
     * node being visited.
     * 
     * @param visitor The visitor. Mustn't be <code>null</code>.
     * @see TreeImageDisplayVisitor
     */
    public void accept(TreeImageDisplayVisitor visitor)
    {
        if (visitor == null) throw new NullPointerException("No visitor.");
        accept(visitor, TreeImageDisplayVisitor.ALL_NODES);
    }
    
    /**
     * Has the specified object visit this node and all nodes below this one
     * in the visualization tree.
     * According to the specified <code>algoType</code>,
     * the <code>visit</code> method is called passing in the
     * node being visited. 
     * 
     * @param visitor   The visitor. Mustn't be <code>null</code>.
     * @param algoType  The algorithm selected. Must be one of the constants
     *                  defined by {@link TreeImageDisplayVisitor}.
     * @see TreeImageDisplayVisitor
     */
    public void accept(TreeImageDisplayVisitor visitor, int algoType)
    {
        if (visitor == null) throw new NullPointerException("No visitor.");
        if (!checkAlgoType(algoType))
            throw new IllegalArgumentException("Algorithm not supported.");
        Iterator i = childrenDisplay.iterator();
        
        TreeImageDisplay child;
        switch (algoType) {
            case TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY:
                while (i.hasNext()) {
                    child = (TreeImageDisplay) i.next();
                    child.accept(visitor, algoType);
                }
                if (this instanceof TreeImageNode) doAccept(visitor);
                break;
            case TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY:
                while (i.hasNext()) {
                    child = (TreeImageDisplay) i.next();
                    if (child instanceof TreeImageSet)
                        child.accept(visitor, algoType);
                }
                if (this instanceof TreeImageSet) doAccept(visitor);
                break;
            case TreeImageDisplayVisitor.ALL_NODES:
                while (i.hasNext()) {
                    child = (TreeImageDisplay) i.next();
                    child.accept(visitor, algoType);
                }
                doAccept(visitor);
                break;
        }
    }
    
    /**
     * Sets the text displayed in a tool tip.
     * 
     * @param tooltip The text to set.
     */
    public void setToolTip(String tooltip) { this.tooltip = tooltip; }
    
    /**
     * Returns the text displayed in a tool tip.
     * 
     * @return See above.
     */
    public String getToolTip() { return tooltip; }
    
    /**
     * Returns <code>true</code> if the node can be selected,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isSelectable() { return selectable; }
    
    /**
     * Returns <code>true</code> if the node can be selected,
     * <code>false</code> otherwise.
     * 
     * @param selectable The value to set.
     */
    public void setSelectable(boolean selectable)
    { 
    	this.selectable = selectable; 
    }
    
    /**
     * Returns the name of the node.
     * 
     * @return See above.
     */
    public String getNodeName()
    { 
        Object obj = getUserObject();
        if (obj instanceof ProjectData) return ((ProjectData) obj).getName();
        else if (obj instanceof DatasetData) 
            return ((DatasetData) obj).getName();
        else if (obj instanceof ImageData) 
            return ((ImageData) obj).getName();
        else if (obj instanceof ExperimenterData) {
        	return EditorUtil.formatExperimenter((ExperimenterData) obj);
        } else if (obj instanceof GroupData) {
        	 return ((GroupData) obj).getName();
        } else if (obj instanceof TagAnnotationData)
        	return ((TagAnnotationData) obj).getTagValue();
        else if (obj instanceof ScreenData)
        	return ((ScreenData) obj).getName();
        else if (obj instanceof PlateData) {
        	PlateData plate = (PlateData) obj;
        	return plate.getName()+" ("+plate.getPlateType()+")";
        } else if (obj instanceof FileAnnotationData) {
        	String name = ((FileAnnotationData) obj).getFileName();
        	if (name.trim().length() == 0) 
        		return "ID: "+((FileAnnotationData) obj).getId();
        	return name;
        } else if (obj instanceof File)
        	return ((File) obj).getName();
        else if (obj instanceof FileData)
        	return ((FileData) obj).getName();
        else if (obj instanceof PlateAcquisitionData) 
        	return ((PlateAcquisitionData) obj).getLabel();
        else if (obj instanceof MultiImageData) 
        	return ((MultiImageData) obj).getName();
        else if (obj instanceof String) return (String) obj;
        return "";
    }
    
    /**
     * Returns the name of the node and the number of items
     * in the specified containers.
     * 
     * @return See above.
     */
    public String getNodeText()
    {
        String name = getNodeName();
        Object uo = getUserObject();
        if (uo instanceof ImageData) { return name;
        } else if (uo instanceof ExperimenterData) return name;
        else if (uo instanceof FileAnnotationData) return name;
        else if (uo instanceof File) return name; 
        else if (uo instanceof MultiImageData) 
        	return (name+SPACE+"["+numberItems+"]");
        else if (uo instanceof FileData) return name;
        else if (uo instanceof PlateAcquisitionData) return name;
        else if (uo instanceof String && numberItems < 0) 
        	return name;
        else if ((uo instanceof PlateData) && !hasChildrenDisplay())
        	return name;
        if (!displayItems) return name;
        if (numberItems < 0) return (name+SPACE+"[...]");
        return (name+SPACE+"["+numberItems+"]");
    }
    
    /**
     * Returns <code>true</code> if the object has been annotated, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isAnnotated()
    {
    	return EditorUtil.isAnnotated(getUserObject(), count);
    }
    
    /**
     * Returns <code>true</code> if the data object has children, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean hasChildren()
    {
    	Object uo = getUserObject();
    	if ((uo instanceof ProjectData) || (uo instanceof ScreenData) ||
    		(uo instanceof PlateData) || (uo instanceof DatasetData) ||
    		(uo instanceof TagAnnotationData) || (uo instanceof GroupData)) {
    		if (numberItems > 0) return true;
        	return hasChildrenDisplay();
    	}
    	return false;
    }
    
    /**
     * Sets the font style of the node.
     * 
     * @param style The font style to set. One out of the following constants
     *              {@link #FONT_BOLD} or {@link #FONT_PLAIN}.
     */
    public void setFontStyle(int style)
    {
        switch (style) {
            case FONT_BOLD:
            case FONT_PLAIN: 
                fontStyle = style;
                break;
            default:
                fontStyle = FONT_PLAIN;
        }
    }
    
    /**
     * Returns the font style for this node. One out of the following constants
     * {@link #FONT_BOLD} or {@link #FONT_PLAIN}.
     * 
     * @return See above.
     */
    public int getFontStyle() { return fontStyle; }
    
    /**
     * Sets the highlight color. 
     * 
     * @param highlight The color to set.
     */
    public void setHighLight(Color highlight) { this.highlight = highlight; }
    
    /**
     * Returns the highlight color.
     * 
     * @return See above.
     */
    public Color getHighLight() { return highlight; }
    
    /**
     * Returns the id of the node's user object if it is an instance of
     * <code>DataObject</code> otherwise returns <code>-1</code>.
     * 
     * @return See above.
     */
    public long getUserObjectId()
    {
        Object uo = getUserObject();
        if (uo instanceof DataObject) return ((DataObject) uo).getId();
        return -1;
    }

    /**
     * Returns the number of items.
     * 
     * @return See above.
     */
    public long getNumberOfItems() { return numberItems; }
    
    /**
     * Sets the flag indicating that the node needs to be refreshed.
     * 
     * @param toRefresh Pass <code>true</code> to indicate that the node needs
     * 					to be refreshed, <code>false</code> otherwise.
     */
    public void setToRefresh(boolean toRefresh) { this.toRefresh = toRefresh; }
    
    /** 
     * Sets the annotation count.
     * 
     * @param count The value to set.
     */
    public void setAnnotationCount(int count) { this.count = count; }
    
    /**
     * Returns <code>true</code> to indicate that the node needs
     * to be refreshed, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isToRefresh() { return toRefresh; }
    
    /**
     * Sets to <code>true</code> to display the number of items,
     * <code>false</code> otherwise.
     * 
     * @param displayItems The value to set.
     */
    public void setDisplayItems(boolean displayItems)
    {
    	this.displayItems = displayItems;
    }
    
    /**
     * Returns <code>true</code> if the specified user is the owner of the 
     * data object, <code>false</code> otherwise.
     * 
     * @param userId The id of the user.
     * @return See above.
     */
    public boolean isOwner(long userId)
    {
    	Object ho = getUserObject();
    	if (ho instanceof GroupData || ho instanceof ExperimenterData)
    		return false;
    	if (ho instanceof DataObject) {
    		DataObject data = (DataObject) ho;
    		if (data.getOwner() != null)
    			return data.getOwner().getId() == userId;
    	}
    	return false;
    }
    /** 
     * Overridden to return the name of the hierarchy object. 
     * @see Object#toString()
     */
    public String toString() { return getNodeName(); }
    
    /**
     * Overridden to make sure that the userObject is not <code>null</code>.
     * @see DefaultMutableTreeNode#setUserObject(Object)
     */
    public void setUserObject(Object userObject)
    {
        if (userObject == null)
            throw new NullPointerException("No userObject.");
        super.setUserObject(userObject);
    }
    
    /**
     * Made final to ensure objects are compared by reference so that the
     * {@link #addChildDisplay(TreeImageDisplay) addChildDisplay} and
     * {@link #removeChildDisplay(TreeImageDisplay) removeChildDisplay} methods
     * will work fine.
     * @see Object#equals(Object)
     */
    public final boolean equals(Object x) { return (this == x); }
    
    /**
     * Implemented by subclasses to call the right version of the <code>visit
     * </code> method on the specified <code>visitor</code>.
     * This method is called by {@link #accept(TreeImageDisplayVisitor)} during
     * the nodes iteration.  Subclasses will just call the <code>visit</code>
     * method passing a reference to <code>this</code>.
     * 
     * @param visitor The visitor.  Will never be <code>null</code>.
     */
    protected abstract void doAccept(TreeImageDisplayVisitor visitor);
    
    /**
     * Tells if the children of this node are {@link TreeImageNode}s.
     * 
     * @return 	Returns <code>true</code> if there's at least one 
     * 			{@link TreeImageNode} child, <code>false</code> otherwise.
     */
    public abstract boolean containsImages();
    
    /**
     * Tells if the children of this node were requested.
     * 
     * @return  <code>true</code> if the children were requested,
     *          <code>false</code> otherwise.
     */
    public abstract boolean isChildrenLoaded();
    
    /**
     * Indicates that the children were requested for this node or not.
     * 
     * @param childrenLoaded    The value to set.
     */
    public abstract void setChildrenLoaded(Boolean childrenLoaded);
    
    /**
     * Makes a copy of the current object.
     * 
     * @return See above.
     */
    public abstract TreeImageDisplay copy();
    
    /**
     * Tells if the passed node is already a child of the node.
     * 
     * @return 	Returns <code>true</code> if it is a child,
     *          <code>false</code> otherwise.
     */
    public abstract boolean contains(TreeImageDisplay node);

}
