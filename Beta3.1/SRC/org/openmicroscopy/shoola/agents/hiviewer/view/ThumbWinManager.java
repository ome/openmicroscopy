/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.ThumbWinManager
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.hiviewer.util.RollOverWin;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;
import pojos.ImageData;

/** 
 * Brings {@link TinyDialog}s on screen to display full-scale thumbnails.
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
class ThumbWinManager
{
    
    /** The sole instance. */
    private static final ThumbWinManager singleton = new ThumbWinManager();
    
    /**
     * Updates the nodes displayed in a floating window.
     * 
     * @param nodes Collection of nodes to updates.
     */
    static void updateDisplayNodes(List nodes)
    {
    	if (nodes == null || nodes.size() == 0) return;
    	if (windows.size() == 0) return;
    	Iterator i = nodes.iterator();
    	Object node;
    	ImageData ho;
    	ThumbWin w;
    	while (i.hasNext()) {
			node = i.next();
			if (node instanceof ImageData) {
				ho = (ImageData) node;
				w = (ThumbWin) windows.get(new Long(ho.getId()));
				if (w != null) w.setDataObject(ho);
			}
		}
    }
    
    /**
     * Brings a window on screen to display <code>node</code>'s thumbnail.
     * The window will be centered on top of the <code>node</code>.
     * 
     * @param node  The image node for which a window is needed.
     *              Mustn't be <code>null</code>.
     * @param model A reference to the model. Mustn't be <code>null</code>.
     */
    static void display(ImageNode node, HiViewer model)
    {
        singleton.displayNode(node, model);
    }
    
    /**
     * Brings a window on screen to display <code>node</code>'s thumbnail.
     * The window will be centered on top of the <code>node</code>. The same
     * window is recycled.
     * 
     * @param node      The image node for which a window is needed.
     * @param browser   Reference to the <code>Browser</code>. 
     */
    static void rollOverDisplay(ImageNode node, Browser browser)
    {
        if (node == null) {
            if (rollOverDialog != null) {
                rollOverDialog.close();
                rollOverDialog = null;
            }
        } else {
            if (rollOverDialog == null) 
                rollOverDialog = new RollOverWin(
                                        (JFrame) node.getTopLevelAncestor(),
                                        browser);
            rollOverDialog.setImageNode(node);
            rollOverDialog.pack();  //Now we have the right width and height.
            Point p = singleton.getWindowLocation(node, 
                                        rollOverDialog.getWidth(), 
                                        rollOverDialog.getHeight());
            rollOverDialog.moveToFront(p);
            rollOverDialog.setVisible(true);
        }
    }
    
    /** Maps image ids onto on-screen windows. */
    private static Map<Long, TinyDialog>  		windows;
    
    /** The dialog displaying the magnified thumbnail. */
    private static RollOverWin 	rollOverDialog;
    
    /** Creates a new instance. */
    private ThumbWinManager()
    {
        windows = new HashMap<Long, TinyDialog>();
        rollOverDialog = null;
    }
    
    /**
     * Returns a window to display the thumbnail in the given <code>node</code>.
     * If a window for the image represented by <code>node</code> is on screen,
     * then it will be recycled.  Otherwise a new one is created.
     * 
     * @param node  The image node for which a window is needed.
     * @param model A reference to the model.
     * @return A window for <code>node</code>.
     */
    private TinyDialog getWindowFor(ImageNode node, HiViewer model)
    {
        ImageData ho = (ImageData) node.getHierarchyObject();
        final Long id = new Long(ho.getId());
        TinyDialog w = windows.get(id);
        if (w == null) {
            Thumbnail prv = node.getThumbnail();
            BufferedImage full = prv.getFullScaleThumb();
            if (prv.getScalingFactor() == Thumbnail.MAX_SCALING_FACTOR)
            	full = prv.getZoomedFullScaleThumb();
            if (full != null) {
                //NOTE: Right now we pre-fetch all images so full != null 
                //unless they click on node at init time, when the thumbs
                //are being loaded.
                w = new ThumbWin((JFrame) node.getTopLevelAncestor(), full, ho,
                                        model, node);
                w.moveToFront();
            //TODO: We assume getFullScaleThumb returns a *pre-fetched* image.
            //If this is not the case and we load async, then we need a
            //callback handler.
            
                w.addPropertyChangeListener(TinyDialog.CLOSED_PROPERTY,
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
     * @param node The node for which the window will be displayed.
     * @param winW The width of the window.
     * @param winH The height of the window.
     * @return The window's top left corner, in screen coordinates.
     */
    private Point getWindowLocation(ImageNode node, int winW, int winH)
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
     * @param model A reference to the model. Mustn't be <code>null</code>.
     */
    private void displayNode(ImageNode node, HiViewer model)
    {
        if (node == null) throw new IllegalArgumentException("No node.");
        if (model == null) throw new IllegalArgumentException("No model.");
        TinyDialog w = getWindowFor(node, model);
        if (w != null) {  //Could be null, see notes in getWindowFor().
            w.pack();  //Now we have the right width and height.
            Point p = getWindowLocation(node, w.getWidth(), w.getHeight());
            w.setCollapsed(false);
            w.moveToFront(p);
        }
    }
    
}
