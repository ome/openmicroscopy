/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorBoxLabel
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.colormap;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Generates a color box in addition to the text.  Generates an ImageIcon
 * on the fly (hopefully, this will be able to support all color models.)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorBoxLabel extends JLabel
{
    protected Color boxColor;
    protected static final int BOX_WIDTH = 16;
    protected static final int BOX_HEIGHT = 16;
    
    /**
     * Package-private to discourage use. If this class is used independently of
     * a renderer or some other class which fills in the fields of the color box
     * label immediately, then you should use the other constructor.)
     *
     */
    ColorBoxLabel()
    {
        // does nothing
    }
    
    /**
     * Creates a ColorBoxLabel with the specified color and associated text.
     * @param text The text to display.
     * @param color The color box in the color box.
     */
    public ColorBoxLabel(String text, Color color)
    {
        super(text);
        this.boxColor = color;
        setIcon(new ImageIcon(createImage(color)));
    }
    
    /**
     * Returns the box color of this label.
     * @return
     */
    public Color getBoxColor()
    {
        return boxColor;
    }
    
    /**
     * Assigns the box color of this label.
     * @param color
     */
    public void setBoxColor(Color color)
    {
        if(color == null)
        {
            return;
        }
        this.boxColor = color;
        setIcon(new ImageIcon(createImage(color)));
        repaint();
    }
    
    // returns an image based on color (for the image icon)
    private Image createImage(Color color)
    {
        int[] pixels = new int[BOX_WIDTH*BOX_HEIGHT];
        int blackColor = Color.black.getRGB();
        int rgbColor = color.getRGB();
        
        // first, draw the upper border
        int index = 0;
        for(int i=0;i<BOX_WIDTH;i++)
        {
            pixels[index++] = blackColor;
        }
        for(int i=0;i<BOX_HEIGHT-2;i++)
        {
            pixels[index++] = blackColor; // left border
            for(int j=0;j<BOX_WIDTH-2;j++)
            {
                pixels[index++] = rgbColor;
            }
            pixels[index++] = blackColor; // right border
        }
        for(int i=0;i<BOX_WIDTH;i++)
        {
            pixels[index++] = blackColor;
        }
        
        Toolkit t = Toolkit.getDefaultToolkit();
        MemoryImageSource src = new MemoryImageSource(BOX_WIDTH,BOX_HEIGHT,
                                                      pixels,0,BOX_WIDTH);
        Image image = t.createImage(src);
        return image;
    }
}
