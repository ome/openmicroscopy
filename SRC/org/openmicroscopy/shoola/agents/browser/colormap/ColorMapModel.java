/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapModel
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.datamodel.CategoryTree;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorMapModel
{
    private CategoryTree categoryTree;
    private BrowserModel source;
    private Set modelListeners;
    
    public ColorMapModel(BrowserModel source)
    {
        if(source == null)
        {
            throw new IllegalArgumentException("Null browser specified");
        }
        this.source = source;
        this.categoryTree = source.getCategoryTree();
        modelListeners = new HashSet();
    }
    
    public CategoryTree getTree()
    {
        return categoryTree;
    }
    
    public BrowserModel getSource()
    {
        return source;
    }
    
    public void setInfoSource(BrowserModel source)
    {
        if(source != null)
        {
            this.source = source;
            this.categoryTree = source.getCategoryTree();
            notifyListeners();
        }
    }
    
    public void addModelListener(ColorMapModelListener listener)
    {
        if(listener != null)
        {
            modelListeners.add(listener);
        }
    }
    
    public void removeListener(ColorMapModelListener listener)
    {
        if(listener != null)
        {
            modelListeners.remove(listener);
        }
    }
    
    private void notifyListeners()
    {
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            ColorMapModelListener listener =
                (ColorMapModelListener)iter.next();
            listener.modelChanged(this);
        }
    }
}
