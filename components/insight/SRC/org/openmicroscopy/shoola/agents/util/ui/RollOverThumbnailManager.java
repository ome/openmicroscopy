/*
 * org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnailManager 
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
package org.openmicroscopy.shoola.agents.util.ui;



//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies

/** 
 * Manages the window.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RollOverThumbnailManager
{

	 /** The sole instance. */
    private static final RollOverThumbnailManager 
    				singleton = new RollOverThumbnailManager();
    
    
    /** The dialog displaying the magnified thumbnail. */
    private static RollOverThumbnail	rollOverDialog;

    /** Closes the dialog. */
    public static void stopOverDisplay()
    {
    	rollOverDisplay(null, null, null, null);
    }
    
    /**
     * Brings a window on screen to display <code>node</code>'s thumbnail.
     * The window will be centered on top of the <code>node</code>. The same
     * window is recycled.
     * 
     * @param image			The thumbnail to display.
     * @param bounds 		The bounds of the node.
     * @param locOnScreen 	The location of the node on screen.
     * @param toolTip 		The tooltip.
     */
    public static void rollOverDisplay(BufferedImage image, Rectangle bounds, 
    						Point locOnScreen, String toolTip)
    {
        if (image == null) {
            if (rollOverDialog != null) {
                rollOverDialog.close();
                rollOverDialog = null;
            }
        } else {
            if (rollOverDialog == null) 
            	 rollOverDialog = new RollOverThumbnail();
            rollOverDialog.setThumbnail(image, toolTip);
            rollOverDialog.pack();  //Now we have the right width and height.
            Point p = singleton.getWindowLocation(bounds, locOnScreen,
                                        rollOverDialog.getWidth(), 
                                        rollOverDialog.getHeight());
            rollOverDialog.moveToFront(p);
            rollOverDialog.setVisible(true);
        }
    }
    
    /**
     * Calculates the top left corner, in screen coordinates, for a window.
     * 
     * @param bounds 		The bounds of the node.
     * @param locOnScreen 	The location of the node on screen.
     * @param winW 			The width of the window.
     * @param winH 			The height of the window.
     * @return The window's top left corner, in screen coordinates.
     */
    private Point getWindowLocation(Rectangle bounds, Point locOnScreen,
    								int winW, int winH)
    {
        /*
        int offsetX = Math.abs(winW-r.width)/2,
            offsetY = Math.abs(winH-r.height)/2;
        Point p = node.getLocationOnScreen();
        p.x -= offsetX;
        p.y -= offsetY;
        */
        int offsetX = Math.abs(winW-bounds.width)/2;
        locOnScreen.y -= winH+5;
        locOnScreen.x -= offsetX;
        return locOnScreen;
    }
    
    /*
     *  Thumbnail prv = node.getThumbnail();
            BufferedImage full = prv.getFullScaleThumb();
            if (prv.getScalingFactor() == Thumbnail.MAX_SCALING_FACTOR)
            	full = prv.getZoomedFullScaleThumb();
            
            
            rollOverDialog.setThumbnail(full, node.toString());
            rollOverDialog.pack();  //Now we have the right width and height.
            Point p = singleton.getWindowLocation(node.getBounds(),
            							node.getLocationOnScreen(),
                                        rollOverDialog.getWidth(), 
                                        rollOverDialog.getHeight());
     */
}
