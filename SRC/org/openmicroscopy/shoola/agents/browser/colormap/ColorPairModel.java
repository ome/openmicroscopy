/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorPairMap
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.ds.st.Category;

/**
 * Quick convenience mapping between categories and colors, using the
 * assignments already made by the ColorPairFactory.  Also abstracts the
 * order of the category assignments, so it can be used as the model (or
 * basis) for a legend UI.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorPairModel
{
    private ColorPairFactory factory;
    private Map colorMap;
    private List colorPairList;
    
    /**
     * Constructs an empty ColorPairModel.
     */
    public ColorPairModel()
    {
        init();
    }
    
    /**
     * Constructs a ColorPairModel with the specified array of categories.
     * @param categories The array of categories to base the model on.
     */
    public ColorPairModel(Category[] categories)
    {
        init();
        for(int i=0;i<categories.length;i++)
        {
            ColorPair pair = factory.getColorPair(categories[i]);
            colorMap.put(pair.getCategory(),pair.getColor());
            colorPairList.add(pair);
        }
    }
    
    private void init()
    {
        factory = new ColorPairFactory();
        colorMap = new HashMap();
        colorPairList = new ArrayList();
    }
    
    /**
     * Returns the color that represents a category.
     * @param category The category to retrieve the color for.
     * @return
     */
    public Color getColor(Category category)
    {
        return (Color)colorMap.get(category);
    }
    
    /**
     * Gets the color pair at the specified index.
     * @param i The index of the pair to get.
     * @return See above.
     */
    public ColorPair get(int i)
    {
        return (ColorPair)colorPairList.get(i);
    }
    
    /**
     * Gets the number of color pairs in the model.
     * @return See above.
     */
    public int size()
    {
        return colorPairList.size();
    }
    
    /**
     * Adds the specified category.  The color is automatically chosen.
     * @param category The category to add to the model.
     */
    public void addCategory(Category category)
    {
        if(category == null) return;
        ColorPair pair = factory.getColorPair(category);
        colorMap.put(category,pair.getColor());
        colorPairList.add(pair);
    }
}
