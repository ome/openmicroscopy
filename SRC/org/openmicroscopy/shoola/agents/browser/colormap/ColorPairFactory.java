/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorPairFactory
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

import org.openmicroscopy.ds.st.Category;

/**
 * A class that generates colors for different categories.  Right now, this
 * supports up to 32 different identifiable index colors; after that, it just
 * starts picking colors at random to use.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorPairFactory
{
    private int currentIndex;
    private final Color[] indexColors =
    {
        Color.red, Color.blue, Color.green, Color.yellow,
        Color.cyan,Color.magenta,Color.orange,Color.pink,
 
        new Color(0,128,255),new Color(255,0,128),
        new Color(128,0,255),new Color(0,255,128),
        new Color(128,255,255),new Color(255,255,128),
        new Color(128,128,255),new Color(255,128,128),
        
        new Color(128,0,0),new Color(0,0,128),
        new Color(0,128,0),new Color(128,128,0),
        new Color(0,128,128),new Color(128,0,128),
        new Color(128,64,0),new Color(128,64,128),
        
        new Color(0,64,128),new Color(128,0,64),
        new Color(64,0,128),new Color(0,128,64),
        new Color(64,128,128),new Color(128,128,64),
        new Color(128,64,64),new Color(64,64,128)
    };
    
    public ColorPairFactory()
    {
        currentIndex = 0;
    }
    
    public ColorPair getColorPair(Category category)
    {
        if(category == null) { return null;}
        if(currentIndex >= 31)
        {
            return new ColorPair(category,pickRandomColor());
        }
        else
        {
            return new ColorPair(category,indexColors[currentIndex++]);
        }
    }
    
    private Color pickRandomColor()
    {
        int red = (int)Math.round(Math.random()*255);
        int green = (int)Math.round(Math.random()*255);
        int blue = (int)Math.round(Math.random()*255);
        return new Color(red,green,blue);
    }
}
