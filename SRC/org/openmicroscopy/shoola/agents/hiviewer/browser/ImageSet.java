/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Represents a container in the composite structure used to visualize an
 * image hierarchy.
 * An <code>ImageSet</code> may contain either {@link ImageNode}s or other
 * <code>ImageSet</code>s, but not both.
 *
 * @see ImageDisplay
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImageSet
    extends ImageDisplay
{

    /**
     * Tells if the children of this node are {@link ImageNode}s.
     * This field will be <code>null</code> until the first call to
     * {@link #addChildDisplay(ImageDisplay)}.  In fact, until then
     * we can't tell if this node is meant to contain {@link ImageNode}s
     * or other <code>ImageSet</code>s. 
     */
    private Boolean     containsImages;
    
    
    /**
     * Implemented as specified by superclass.
     * @see ImageDisplay#doAccept(ImageDisplayVisitor)
     */
    protected void doAccept(ImageDisplayVisitor visitor) 
    {
        visitor.visit(this);
    }
    
    /**
     * Creates a new container node.
     * 
     * @param title The frame's title.
     * @param hierarchyObject The original object in the image hierarchy which
     *                        is visualized by this node.  
     *                        Never pass <code>null</code>.
     */
    public ImageSet(String title, Object hierarchyObject)
    {
        super(title, hierarchyObject);
        setResizable(true);
    }
    
    /**
     * Adds a node to the visualization tree as a child of this node.
     * Note that an <code>ImageSet</code> may contain either {@link ImageNode}s
     * or other <code>ImageSet</code>s, but not both.  So if the first node you
     * add is an {@link ImageNode}, you're constrained to add {@link ImageNode}s
     * thereafter.  Failure to comply will buy you a runtime exception.  
     * The same considerations apply to adding <code>ImageSet</code>s.  
     * 
     * @param child The node to add.  Mustn't be <code>null</code>.
     * @see ImageDisplay#addChildDisplay(ImageDisplay)
     */
    public void addChildDisplay(ImageDisplay child)
    {
        if (child == null) throw new NullPointerException("No child.");
        Class childClass = child.getClass();
        if (containsImages == null) {  //First time add is invoked.
            containsImages = new Boolean(childClass.equals(ImageNode.class));
        } else {  //Either ImageNodes or ImageSets have been added.
            if (containsImages.booleanValue()) {  //Children are ImageNodes.
                if (!childClass.equals(ImageNode.class))
                    throw new IllegalArgumentException(
                        "This node can only contain ImageNodes.");
            } else  { //Children are ImageSets.
                if (!childClass.equals(ImageSet.class))
                    throw new IllegalArgumentException(
                        "This node can only contain ImageSets.");
            }
        }
        super.addChildDisplay(child);
    }
    
    /**
     * Tells if the children of this node are {@link ImageNode}s.
     * Note that this method will return <code>false</code> if this is
     * an <code>ImageSet</code> meant to contain {@link ImageNode}s, but
     * no node has been added yet.
     * 
     * @return <code>true</code> if there's at least one {@link ImageNode} 
     *          child, <code>false</code> otherwise.
     * @see ImageDisplay#containsImages()
     */
    public boolean containsImages()
    {
        if (containsImages == null) return false;
        return containsImages.booleanValue();
    }
    
}
