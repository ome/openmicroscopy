/*
 * org.openmicroscopy.shoola.agents.classifier.CategoryTree
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
package org.openmicroscopy.shoola.agents.browser.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;

/**
 * A tree representation of the categories available to a particular browser
 * model.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class CategoryTree
{
    private List categoryGroupList;
    private Map categoryGroupMap;
    private Map categoryMap; // inverse mapping
    
    public CategoryTree()
    {
        categoryGroupList = new ArrayList();
        categoryGroupMap = new HashMap();
        categoryMap = new HashMap();
    }
    
    public void addCategoryGroup(CategoryGroup cg)
    {
        if(cg != null)
        {
            categoryGroupList.add(cg);
            List theList = new ArrayList();
            categoryGroupMap.put(cg,theList);
            Collections.sort(categoryGroupList,new CategoryComparator());
        }
    }
    
    public void addCategory(CategoryGroup parent, Category category)
    {
        if(parent == null || category == null) return;
        List categoryList = (List)categoryGroupMap.get(parent);
        if(categoryList == null) return;
        categoryList.add(category);
        Collections.sort(categoryList,new CategoryComparator());
        categoryMap.put(category,parent);
    }
    
    public CategoryGroup getCategoryGroup(int i)
    {
        return (CategoryGroup)categoryGroupList.get(i);
    }
    
    /**
     * Inverse lookup (workaround on getting higher-depth STs)
     * @param c
     * @return
     */
    public CategoryGroup getGroupForCategory(Category c)
    {
        return (CategoryGroup)categoryMap.get(c);
    }
    
    public List getCategoryGroups()
    {
        return Collections.unmodifiableList(categoryGroupList);
    }
    
    public List getCategories(int i)
    {
        try
        {
            CategoryGroup cg = getCategoryGroup(i);
            return Collections.unmodifiableList((List)categoryGroupMap.get(cg));
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }
    
    public List getCategories(CategoryGroup cg)
    {
        if(cg != null)
        {
            List theList = (List)categoryGroupMap.get(cg);
            if(theList != null) return Collections.unmodifiableList(theList);
            else return null;
        }
        else return null;
    }
    
    public Category getCategory(int groupNo, int i)
    {
        List theList = getCategories(groupNo);
        if(theList == null) return null;
        try
        {
            return (Category)theList.get(i);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }
    
    public Category getCategory(CategoryGroup cg, int i)
    {
        List theList = getCategories(cg);
        if(theList == null) return null;
        try
        {
            return (Category)theList.get(i);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }
}
