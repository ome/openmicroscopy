/*
 * org.openmicroscopy.shoola.agents.browser.layout.CategoryGroupingMethod
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
package org.openmicroscopy.shoola.agents.browser.layout;

import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.datamodel.CategoryTree;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;
import org.openmicroscopy.shoola.agents.browser.util.GrepOperator;

/**
 * Establishes the grouping method by category.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class CategoryGroupingMethod extends CriteriaGroupingMethod
{
    private CategoryGroup classificationGroup;
    private CategoryTree hierarchyTree;
    private GroupModel unclassifiedModel;
    
    private GrepOperator unclassifiedOperator = new GrepOperator()
    {
        public boolean eval(Object o)
        {
            if(!(o instanceof Thumbnail))
            {
                return false;
            }
            Thumbnail t = (Thumbnail)o;
            return unclassifiedModel.containsThumbnail(t);
        }
    };

    
    public CategoryGroupingMethod(CategoryGroup group,
                                  CategoryTree tree)
    {
        if(group == null || tree == null)
        {
            throw new IllegalArgumentException("Null parameters");
        }
        this.classificationGroup = group;
        this.hierarchyTree = tree;
        
        List categories = tree.getCategories(group);
        for(int i=0;i<categories.size();i++)
        {
            Category c = (Category)categories.get(i);
            GrepOperator operator = makeOperator(c);
            GroupModel model = new GroupModel(c.getName());
            addCriteriaGroup(operator,model);
        }
        unclassifiedModel = new GroupModel("Unclassified");
        addCriteriaGroup(unclassifiedOperator,unclassifiedModel);
    }
    
    public GroupModel assignGroup(Thumbnail t)
    {
        GroupModel model = super.assignGroup(t);
        if(model == null)
        {
            unclassifiedModel.addThumbnail(t);
            return unclassifiedModel;
        }
        else return model;
    }
    
    public void assignGroups(Thumbnail[] ts)
    {
        super.assignGroups(ts);
        for(int i=0;i<ts.length;i++)
        {
            GroupModel model = getGroup(ts[i]);
            if(model == null)
            {
                unclassifiedModel.addThumbnail(ts[i]);
            }
        }
    }
    
    private GrepOperator makeOperator(final Category c)
    {
        GrepOperator operator = new GrepOperator()
        {
            public boolean eval(Object o)
            {
                if(!(o instanceof Thumbnail))
                {
                    return false;
                }
                Thumbnail t = (Thumbnail)o;
                ThumbnailDataModel tdm = t.getModel();
                AttributeMap attrMap = tdm.getAttributeMap();
                List classifications = attrMap.getAttributes("Classification");
                if(classifications == null)
                {
                    return false;
                }
                for(Iterator iter = classifications.iterator(); iter.hasNext();)
                {
                    Classification cl = (Classification)iter.next();
                    if(cl.getCategory().equals(c))
                    {
                        return true;
                    }
                }
                return false;
            }

        };
        return operator;
    }
}
