/*
 * org.openmicroscopy.shoola.agents.browser.ui.CategoryMenuFactory
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
 
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.datamodel.CategoryTree;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Factory that generates menus (and submenus) based on the current list of
 * categories in a dataset.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class CategoryMenuFactory
{
    private CategoryGroup lastGroupSelected;
    private Category lastCategorySelected;
    
    /**
     * The model to base the factory off of, so it can always get a
     * particular current version of a CategoryTree.
     */
    private BrowserModel backingModel;
    
    public CategoryMenuFactory(BrowserModel backingModel)
    {
        if(backingModel == null)
            throw new IllegalArgumentException("Factory needs valid backing model");
        
        this.backingModel = backingModel;
    }
    
    public JMenu createMenu(Thumbnail selected)
    {
        CategoryTree tree = backingModel.getCategoryTree();
        JMenu menu = new JMenu("Categorize");
        
        List list = tree.getCategoryGroups();
        Collections.sort(list);
        for(Iterator iter = list.iterator(); iter.hasNext();)
        {
            CategoryGroup cg = (CategoryGroup)iter.next();
            menu.add(createCategoryMenu(tree,cg));
        }
        return menu;
    }
    
    private JMenu createCategoryMenu(CategoryTree tree, final CategoryGroup group)
    {
        JMenu menu = new JMenu(group.getName());
        
        List categoryList = tree.getCategories(group); 
        for(Iterator iter = categoryList.iterator(); iter.hasNext();)
        {
            final Category category = (Category)iter.next();
            JMenuItem item = new JMenuItem(category.getName());
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    Set selectedSet = backingModel.getSelectedImages();
                    if(selectedSet == null || selectedSet.size() == 0)
                        return;
                    
                    Thumbnail[] ts = new Thumbnail[selectedSet.size()];
                    selectedSet.toArray(ts);
                    if(ts.length == 1)
                    {
                        Thumbnail t = ts[0];
                        CategoryEventHandler.handle(t,group,category);
                    }
                    else
                    {
                        CategoryEventHandler.handle(ts,group,category);
                    }
                }
            });
            menu.add(item);
        }
        
        return menu;
    }
}
