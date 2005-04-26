/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.TWinManager
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.hiviewer.twindow.TinyWindow;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * Brings {@link TinyWindow}s on screen to display full-scale thumbnails.
 * Windows that are already on screen for a thumbnail of a given image are
 * recycled, even if the window was originally brought up for a node in
 * another {@link HiViewerWin}.
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
class TWinManager
{
    
    /** Maps image ids onto on-screen windows. */
    private static Map  windows = new HashMap();
    
    
    /**
     * Returns a window to display the thumbnail in the given <code>node</code>.
     * If a window for the image represented by <code>node</code> is on screen,
     * then it will be recycled.  Otherwise a new one is created.
     * 
     * @param node The image node for which a window is needed.
     * @return A window for <code>node</code>.
     */
    private static TinyWindow getWindowFor(ImageNode node)
    {
        ImageSummary ho = (ImageSummary) node.getHierarchyObject();
        final Integer id = new Integer(ho.getID());
        TinyWindow w = (TinyWindow) windows.get(id);
        if (w == null) {
            Thumbnail prv = node.getThumbnail();
            BufferedImage full = prv.getFullScaleThumb();
            if (full != null) {
                //NOTE: Right now we pre-fetch all images so full != null 
                //unless they click on node at init time, when the thumbs
                //are being loaded.
                w = new TinyWindow((JFrame) node.getTopLevelAncestor(), full);
            //TODO: We assume getFullScaleThumb returns a *pre-fetched* image.
            //If this is not the case and we load async, then we need a
            //callback handler.
            
                w.addPropertyChangeListener(TinyWindow.CLOSED_PROPERTY,
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent pce)
                        {
                            windows.remove(id);
                        }
                    });
                windows.put(id, w);
                w.setTitle(node.getTitle());
            }
        }
        return w;
    }
    
    /**
     * Calculates the top left corner, in screen coordinates, for a window.
     * 
     * @param node    The node for which the window will be displayed.
     * @param winW    The width of the window.
     * @param winH    The height of the window.
     * @return The window's top left corner, in screen coordinates.
     */
    private static Point getWindowLocation(ImageNode node, int winW, int winH)
    {
        Rectangle r = node.getBounds();
        int offsetX = Math.abs(winW-r.width)/2,
            offsetY = Math.abs(winH-r.height)/2;
        Point p = node.getLocationOnScreen();
        p.x -= offsetX;
        p.y -= offsetY;
        return p;
    }
    
    /**
     * Brings a window on screen to display <code>node</code>'s thumbnail.
     * The window will be centered on top of the <code>node</code>.
     * 
     * @param node  The image node for which a window is needed.
     *              Mustn't be <code>null</code>.
     */
    static void display(ImageNode node)
    {
        if (node == null) throw new NullPointerException("No node.");
        TinyWindow w = getWindowFor(node);
        if (w != null) {  //Could be null, see notes in getWindowFor().
            w.pack();  //Now we have the right width and height.
            Point p = getWindowLocation(node, w.getWidth(), w.getHeight());
            w.setCollapsed(false);
            w.moveToFront(p);
        }
    }
    
}
