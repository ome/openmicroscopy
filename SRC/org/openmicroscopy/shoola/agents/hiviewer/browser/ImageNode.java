/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode
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
import java.awt.Dimension;

//Third-party libraries

//Application-internal dependencies

/** 
 * Represents a leaf in the composite structure used to visualize an
 * image hierarchy.
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
public class ImageNode
    extends ImageDisplay
{

    /** The thumbnail this node is going to display. */
    private Thumbnail   thumbnail;
    
    
    /**
     * Implemented as specified by superclass.
     * @see ImageDisplay#doAccept(ImageDisplayVisitor)
     */
    protected void doAccept(ImageDisplayVisitor visitor)
    {
        visitor.visit(this);
    }
    
    /**
     * Creates a new leaf node.
     * 
     * @param title The frame's title.
     * @param hierarchyObject The original object in the image hierarchy which
     *                        is visualized by this node.  It has to be an image
     *                        object in this case. 
     *                        Never pass <code>null</code>.
     * @param t The thumbnail this node is going to display.  This is obviously
     *          a thumbnail for the image object this node represents.
     */
    public ImageNode(String title, Object hierarchyObject, Thumbnail t)
    {
        super(title, hierarchyObject);
        if (t == null) throw new NullPointerException("No thumbnail.");
        thumbnail = t;
        ThumbnailCanvas img = new ThumbnailCanvas(this);
        desktopPane.add(img);
        int w = t.getWidth(), h = t.getHeight();
        img.setBounds(0, 0, w, h);
        desktopPane.setPreferredSize(new Dimension(w, h));
    }
    
    /**
     * Spits out a runtime exception because it's not possible to add a
     * child to a leaf node.
     */
    public void addChildDisplay(ImageDisplay child)
    {
        throw new IllegalArgumentException(
                "Can't add a child to an ImageNode.");
    }
    
    /**
     * Always returns <code>false</code> as this is not a container node.
     * @see ImageDisplay#containsImages()
     */
    public boolean containsImages() { return false; }

    public Thumbnail getThumbnail() { return thumbnail; }
    
}
