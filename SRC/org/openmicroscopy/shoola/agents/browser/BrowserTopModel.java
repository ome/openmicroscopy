/*
 * org.openmicroscopy.shoola.agents.browser.BrowserTopModel
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
package org.openmicroscopy.shoola.agents.browser;

import java.util.*;

import org.openmicroscopy.shoola.agents.browser.ui.BPalette;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserTopModel
{
    private Map modalSet;
    private Map orientationMap;
    private Map paletteMap;
    
    private Set modelListeners;

    public BrowserTopModel()
    {
        modalSet = new HashMap();
        orientationMap = new HashMap();
        paletteMap = new HashMap();
        modelListeners = new HashSet();
    }
    
    public void addModelListener(BrowserTopModelListener listener)
    {
        if(listener != null)
        {
            modelListeners.add(listener);
        }
    }
    
    public void removeModelListener(BrowserTopModelListener listener)
    {
        if(listener != null)
        {
            modelListeners.remove(listener);
        }
    }

    /**
     * Binds a particular palette to a specific name, and adds it to the ones
     * available to be hidden/shown/minimized in the browser window.
     * 
     * @param paletteName The name of the palette to overlay.
     * @param palette The palette itself.
     */
    public void addPalette(String paletteName, BPalette palette)
    {
        if(paletteName == null || palette == null)
        {
            return;
        }
        paletteMap.put(paletteName,palette);
        
    }
    
    /**
     * Shows the palette with the specified name.
     * @param name The name of the palette to show.
     */
    public void showPalette(String name)
    {
        BPalette palette = (BPalette)paletteMap.get(name);
        showPalette(palette);
    }
    
    /**
     * Makes the specified palette visible.  In actuality,
     * trigger those listening to make the palette visible.
     * 
     * @param palette The palette to show.
     */
    public void showPalette(BPalette palette)
    {
        if(palette != null && paletteMap.containsValue(palette))
        {
            for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
            {
                BrowserTopModelListener listener =
                    (BrowserTopModelListener)iter.next();
                listener.showPalette(palette);
            }
        }
    }
    
    /**
     * Hides the palette with the specified name.
     * @param name The name of the palette to hide.
     */
    public void hidePalette(String name)
    {
        BPalette palette = (BPalette)paletteMap.get(name);
        hidePalette(palette);
    }

    /**
     * Makes the specified palette invisible.  In actuality,
     * trigger those listening to make the palette visible.
     * 
     * @param palette The palette to hide.
     */
    public void hidePalette(BPalette palette)
    {
        if(palette != null && paletteMap.containsValue(palette))
        {
            for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
            {
                BrowserTopModelListener listener =
                    (BrowserTopModelListener)iter.next();
                listener.hidePalette(palette);
            }
        }
    }
    
    /**
     * Minimizes the palette with the specified name.
     * @param name The name of the palette to hide.
     */
    public void iconifyPalette(String name)
    {
        BPalette palette = (BPalette)paletteMap.get(name);
        iconifyPalette(palette);
    }

    /**
     * Minimizes the specified palette.  In actuality,
     * trigger those listening to make the palette visible.
     * 
     * @param palette The palette to hide.
     */
    public void iconifyPalette(BPalette palette)
    {
        if(palette != null && paletteMap.containsValue(palette))
        {
            for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
            {
                BrowserTopModelListener listener =
                    (BrowserTopModelListener)iter.next();
                listener.iconifyPalette(palette);
            }
        }
    }
    
    /**
     * Deiconifies the palette with the specified name.
     * @param name The name of the palette to deiconify.
     */
    public void deiconifyPalette(String name)
    {
        BPalette palette = (BPalette)paletteMap.get(name);
        deiconifyPalette(palette);
    }

    /**
     * Deiconifies the specified palette.  In actuality,
     * trigger those listening to make the palette deiconified.
     * 
     * @param palette The palette to hide.
     */
    public void deiconifyPalette(BPalette palette)
    {
        if(palette != null && paletteMap.containsValue(palette))
        {
            for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
            {
                BrowserTopModelListener listener =
                    (BrowserTopModelListener)iter.next();
                listener.deiconifyPalette(palette);
            }
        }
    }

}
