/*
 * org.openmicroscopy.shoola.agents.treemng.browser.TreeImageSet
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Represents a container in the composite structure used to visualize an
 * image hierarchy.
 * A <code>TreeImageSet</code> may contain either {@link TreeImageNode}s or 
 * other {@link TreeImageSet}s, but not both.
 *
 * @see TreeImageDisplay
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeImageSet
    extends TreeImageDisplay
{

    /**
     * Tells if the children of this node are {@link TreeImageNode}s.
     * This field will be <code>null</code> until the first call to
     * {@link #addChildDisplay(TreeImageDisplay)}. In fact, until then
     * we can't tell if this node is meant to contain {@link TreeImageNode}s
     * or other {@link TreeImageSet}s. 
     */
    private Boolean     containsImages;
    
    /** Flag to indicate if the children were loaded for that node. */
    private Boolean     childrenLoaded;

    /**
     * Implemented as specified by superclass.
     * @see TreeImageDisplay#doAccept(TreeImageDisplayVisitor)
     */
    protected void doAccept(TreeImageDisplayVisitor visitor)
    {
        visitor.visit(this);
    }
    
    /**
     * Creates a new container node.
     *  
     * @param hierarchyObject The original object in the image hierarchy which
     *                        is visualized by this node. It has to be an image
     *                        object in this case. 
     *                        Never pass <code>null</code>.
     */
    public TreeImageSet(Object hierarchyObject)
    {
        super(hierarchyObject);
        childrenLoaded = null;
    }


    /**
     * Adds a node to the visualization tree as a child of this node.
     * Note that an <code>TreeImageSet</code> may contain either
     * {@link TreeImageNode}s or other <code>TreeImageSet</code>s,
     * but not both. So if the first node you
     * add is an {@link TreeImageNode}, you're constrained to add
     * {@link TreeImageNode}s thereafter. 
     * Failure to comply will buy you a runtime exception.  
     * The same considerations apply to adding <code>TreeImageSet</code>s.  
     * 
     * @param child The node to add. Mustn't be <code>null</code>.
     * @see TreeImageDisplay#addChildDisplay(TreeImageDisplay)
     */
    public void addChildDisplay(TreeImageDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        Class childClass = child.getClass();
        if (containsImages == null) {  //First time add is invoked.
            containsImages = 
                new Boolean(childClass.equals(TreeImageNode.class));
        } else {  //Either ImageNodes or ImageSets have been added.
            if (containsImages.booleanValue()) {  //Children are ImageNodes.
                if (!childClass.equals(TreeImageNode.class))
                    throw new IllegalArgumentException(
                        "This node can only contain TreeImageNodes.");
            } else  { //Children are ImageSets.
                if (!childClass.equals(TreeImageSet.class))
                    throw new IllegalArgumentException(
                        "This node can only contain TreeImageSets.");
            }
        }
        super.addChildDisplay(child);
    }
    
    /**
     * Tells if the children of this node are {@link TreeImageNode}s.
     * Note that this method will return <code>false</code> if this is
     * an <code>TreeImageSet</code> meant to contain {@link TreeImageNode}s, but
     * no node has been added yet.
     * 
     * @return <code>true</code> if there's at least one {@link TreeImageNode} 
     *          child, <code>false</code> otherwise.
     * @see TreeImageDisplay#containsImages()
     */
    public boolean containsImages()
    {
        if (containsImages == null) return false;
        return containsImages.booleanValue();
    }

    /** 
     * Sets the number of items contained in the container hosted 
     * by this class.
     * 
     * @param value The number of items.
     * @see #getNumberItems()
     */
    public void setNumberItems(int value) { numberItems = value; }
    
    /**
     * Returns the number of items. Should only be called when children aren't
     * loaded.
     * 
     * @return See above.
     * @see #setNumberItems(int)
     */
    public int getNumberItems() { return numberItems; }
    
    /** 
     * Tells if the children of this node have been loaded. 
     * The node may not contain children but the children were requested.
     * Note that this method will return <code>false</code> if the value of the 
     * {@link #childrenLoaded} is <code>null</code>.
     * 
     * @return  <code>true</code> if the children have been loaded, 
     *          <code>false</code> otherwise.
     */
    public boolean isChildrenLoaded()
    { 
        if (childrenLoaded == null) return false;
        return childrenLoaded.booleanValue();
    }
    
    /**
     * Indicates if the children were requested for this node.
     * 
     * @param childrenLoaded    The value to set.
     */
    public void setChildrenLoaded(Boolean childrenLoaded)
    {
        this.childrenLoaded = childrenLoaded;
    }
    
}
