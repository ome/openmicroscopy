/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapDispatcher
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

import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.datamodel.CategoryTree;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Manages paint methods for the color map and dispatches instructions
 * to the various components of the color map and browser.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorMapDispatcher implements ColorMapCategoryListener,
                                           ColorMapGroupListener
{
    private ColorMapModel sourceModel;
    private ColorMapList colorList;
    private ColorPairModel currentModel;
    private Category selectedCategory;
    private PaintMethod overlayMethod = null;
    private PaintMethod highlightMethod = null;
    
    public ColorMapDispatcher(ColorMapModel model)
    {
        this.sourceModel = model;
    }
    
    public ColorMapDispatcher(ColorMapModel model, ColorMapList colorList)
    {
        this.sourceModel = model;
        this.colorList = colorList;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.colormap.ColorMapGroupListener#groupSelected(org.openmicroscopy.ds.st.CategoryGroup)
     */
    public void groupSelected(CategoryGroup group)
    {
        if(group != null)
        {
            CategoryTree tree = sourceModel.getTree();
            List categories = tree.getCategories(group);
            if(categories != null && categories.size() > 0)
            {
                Category[] cats = new Category[categories.size()];
                categories.toArray(cats);
                currentModel = new ColorPairModel(cats);
                colorList.setModel(currentModel);
                clearMethods();
                applyOverlayMethod();
            }
        }
        else
        {
            clearMethods();
        }
    }
    
    /**
     * Tells the dispatcher to disable heat map mode for the selected
     * browser.
     */
    public void fireModeCancel()
    {
        PaintMethod backupOverlayMethod = overlayMethod;
        clearMethods();
        overlayMethod = backupOverlayMethod;
    }
    
    /**
     * Tells the dispatcher to reenable heat map mode for the selected
     * browser.
     */
    public void fireModeReactivate()
    {
        applyOverlayMethod();
    }
    
    public void fireRedraw()
    {
        clearMethods();
        applyOverlayMethod();
        applyHighlightMethod(selectedCategory);
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.colormap.ColorMapGroupListener#groupsDeselected()
     */
    public void groupsDeselected()
    {
        clearMethods();
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.colormap.ColorMapCategoryListener#categorySelected(org.openmicroscopy.ds.st.Category)
     */
    public void categorySelected(Category category)
    {
        selectedCategory = category;
        clearHighlightMethod();
        applyHighlightMethod(category);
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.colormap.ColorMapCategoryListener#categoriesDeselected()
     */
    public void categoriesDeselected()
    {
        clearHighlightMethod();
        selectedCategory = null;
    }

    private void applyOverlayMethod()
    {
        if(currentModel == null) return;
        BrowserModel browserModel = sourceModel.getSource();
        List thumbnails = browserModel.getThumbnails();
        overlayMethod = ColorMapPMFactory.getOverlayMethod(currentModel);
        for(Iterator iter = thumbnails.iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            t.addMiddlePaintMethod(overlayMethod);
        }
        browserModel.fireModelUpdated();
    }
    
    private void applyHighlightMethod(Category whichCategory)
    {
        if(whichCategory == null) return;
        BrowserModel browserModel = sourceModel.getSource();
        List thumbnails = browserModel.getThumbnails();
        highlightMethod = ColorMapPMFactory.getHighlightMethod(whichCategory);
        for(Iterator iter = thumbnails.iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            t.addBackgroundPaintMethod(highlightMethod);
        }
        browserModel.fireModelUpdated();
    }
    
    private void clearHighlightMethod()
    {
        BrowserModel browserModel = sourceModel.getSource();
        List thumbnails = browserModel.getThumbnails();
        for(Iterator iter = thumbnails.iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            t.removeBackgroundPaintMethod(highlightMethod);
        }
        highlightMethod = null;
        browserModel.fireModelUpdated();
    }
    
    private void clearMethods()
    {
        BrowserModel browserModel = sourceModel.getSource();
        List thumbnails = browserModel.getThumbnails();
        for(Iterator iter = thumbnails.iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            t.removeMiddlePaintMethod(overlayMethod);
            t.removeBackgroundPaintMethod(highlightMethod);
        }
        overlayMethod = null;
        highlightMethod = null;
        browserModel.fireModelUpdated();
    }
}
