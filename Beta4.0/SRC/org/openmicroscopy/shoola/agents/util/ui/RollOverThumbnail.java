/*
 * org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnail 
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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JDialog;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.image.geom.Factory;

/** 
 * Dialog displaying a magnified thumbnail. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class RollOverThumbnail 	
	extends JDialog
{

    /** The minimum magnification value. */
    private final static int		MINIMUM_ZOOM = 1;
    
    /** The maximum magnification value. */
    private final static int		MAXIMUM_ZOOM = 2;
     
    /** The image to display. */
    private BufferedImage   		image;
    
    /** The image to display. */
    private BufferedImage   		originalImage;
    
    /** The canvas hosting the image. */
    private RollOverThumbnailCanvas	canvas;
    
    /** The magnification factor. */
    private float					zoomFactor;

    /** Sets the property of the dialog window. */ 
    private void setProperties()
    {
        setModal(false);
        setResizable(false);
        setUndecorated(true);
        zoomFactor = MINIMUM_ZOOM;
    }
    
    /** 
     * Sets the size and preferred size of the canvas. 
     * 
     * @param w The width of the image.
     * @param h The height of the image.
     */
    private void makeComponentsSize(int w, int h)
    {
        if (canvas == null) return;
        Insets i = canvas.getInsets();
        Dimension d = new Dimension(w+i.right+i.left, h+i.top+i.bottom);
        canvas.setPreferredSize(d);
        canvas.setSize(d);
    }
    
    /** Builds and lays out the UI. */ 
    private void buildUI()
    {
        Container container = getContentPane();
        container.add(canvas, BorderLayout.CENTER);
    }
    
    /** Creates a new instance. */
    public RollOverThumbnail()
    {
        canvas = new RollOverThumbnailCanvas(this);
        setProperties();
        buildUI();
    }
    
    
    /**
     * Magnifies the displayed image.
     * 
     * @param tick The number of "clicks" the mouse wheel was rotated.
     */
    void magnifyImage(int tick)
    {
		zoomFactor -= 0.1f*tick;
		zoomFactor = Math.round(zoomFactor*10)/10.0f;
		if (zoomFactor < MINIMUM_ZOOM) zoomFactor = MINIMUM_ZOOM;
		if (zoomFactor > MAXIMUM_ZOOM) zoomFactor = MAXIMUM_ZOOM;
		image = Factory.magnifyImage(zoomFactor, originalImage);
		makeComponentsSize(image.getWidth(), image.getHeight());
		pack();
    }
    
    /**
     * Returns the image to display.
     * 
     * @return See above.
     */
    BufferedImage getImage() { return image; }
    
    /**
     * Sets the image and resizes the components. 
     * 
     * @param full		The thumbnail to display.
     * @param toolTip 	The tooltip.
     */
    public void setThumbnail(BufferedImage full, String toolTip)
    {
        canvas.setToolTipText(toolTip);
        if (full != null)  {
            image = full;
            originalImage = full;
            makeComponentsSize(image.getWidth(), image.getHeight());
            canvas.repaint();
        }
    }
    
    /** Hides and disposes. */
    public void close()
    {
        setVisible(true);
        dispose();
    }
    
    /**
     * Moves the window to the front and sets the location.
     * 
     * @param p The new location.
     */
    public void moveToFront(Point p) { moveToFront(p.x, p.y); }
    
    /** 
     * Moves the window to the front and sets the location.
     * 
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void moveToFront(int x, int y)
    {
        setLocation(x, y);
        setVisible(true);
    }
    
}
