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
import java.util.HashSet;

import javax.swing.JComponent;

import pojos.ImageData;

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
    private Thumbnail       thumbnail;
    
    /** The component on which the thumbnail is painted. */
    private ThumbnailCanvas canvas;
    
    /**
     * Implemented as specified by superclass.
     * @see ImageDisplay#doAccept(ImageDisplayVisitor)
     */
    protected void doAccept(ImageDisplayVisitor visitor)
    {
        visitor.visit(this);
    }
    
    /**
     * Returns the component on which the thumbnail is painted.
     * 
     * @return See above.
     */
    JComponent getCanvas() { return canvas; }
    
    /**
     * Creates a new leaf node.
     * 
     * @param title             The frame's title.
     * @param hierarchyObject   The original object in the image hierarchy which
     *                          is visualized by this node. It has to be an 
     *                          image object in this case. 
     *                          Never pass <code>null</code>.
     * @param t                 The thumbnail this node is going to display. 
     *                          This is obviously a thumbnail for the image
     *                          object this node represents.
     */
    public ImageNode(String title, Object hierarchyObject, Thumbnail t)
    {
        super(title, "", hierarchyObject);
        //Probably cleaner to use a visitor but for performance reason better
        //that way.
        if (hierarchyObject instanceof ImageData) { 
            ImageData data = (ImageData) hierarchyObject;
            HashSet nodes = new HashSet();
            Long n = data.getAnnotationCount();
            if (n != null && n.longValue() > 0) 
                nodes.add(new AnnotatedButton());
            Long m = data.getClassificationCount();
            if (m != null && m.longValue() > 0) 
                nodes.add(new ClassifiedButton());
            if (nodes.size() > 0) setDecoration(nodes);
        }
        setTitleBarType(SMALL_BAR);
        if (t == null) throw new NullPointerException("No thumbnail.");
        thumbnail = t;
        canvas = new ThumbnailCanvas(this);
        getInternalDesktop().add(canvas);
        setCanvasSize(t.getWidth(), t.getHeight());
    }
    
    /**
     * Spits out a runtime exception because it's not possible to add a
     * child to a leaf node.
     * 
     * @param child The child to add. In this case, a runtime exception is 
     * 				thrown.
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

    /**
     * Returns the thumbnail hosted by this class.
     * 
     * @return See above.
     */
    public Thumbnail getThumbnail() { return thumbnail; }
    
    /** 
     * Overrides the #setSize(int, int) method, otherwise
     * after collapsing the node, we can resize the imageNode.
     * @see ImageDisplay#setSize(int, int)
     */
    public void setSize(int w, int h)
    {
        super.setSize(w, h);
        setResizable(false);
    }
    
    /** 
     * Sets the size of the {@link ThumbnailCanvas} and the preferred size of
     * the internal desktop.
     * 
     * @param w The width of the canvas.
     * @param h The height of the canvas.
     */
    public void setCanvasSize(int w, int h)
    {
        canvas.setBounds(0, 0, w, h);
        getInternalDesktop().setSize(w, h);
        getInternalDesktop().setPreferredSize(new Dimension(w, h));
    }
    
}
