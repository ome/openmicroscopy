/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.RollOverWin
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

package org.openmicroscopy.shoola.agents.hiviewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies



/** 
 * When the user mouses over an image, displays a zoomed version of the 
 * thumbnail.
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class RollOverWin
    extends JDialog
{

    /** The image to display. */
    private BufferedImage   image;
    
    /** The canvas hosting the image. */
    private RollOverCanvas  canvas;
    
    /** Sets the property of the dialog window. */ 
    private void setProperties()
    {
        setModal(false);
        setResizable(false);
        setUndecorated(true);
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
        int width = w+i.right+i.left;
        int height = h+i.top+i.bottom;
        Dimension d = new Dimension(width, height);
        canvas.setPreferredSize(d);
        canvas.setSize(d);
    }
    
    /** Builds and lays out the UI. */ 
    private void buildUI()
    {
        Container container = getContentPane();
        container.add(canvas, BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parent The parent of the window.
     */
    public RollOverWin(JFrame parent)
    {
        super(parent);
        canvas = new RollOverCanvas(this);
        setProperties();
        buildUI();
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
     * @param image The image to display.
     */
    public void setBufferedImage(BufferedImage image)
    {
        if (image == null)
            throw new IllegalArgumentException("No thumbnail.");
        this.image = image;
        makeComponentsSize(image.getWidth(), image.getHeight());
        canvas.repaint();
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
