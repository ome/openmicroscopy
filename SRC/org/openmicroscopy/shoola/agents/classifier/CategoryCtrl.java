/*
 * org.openmicroscopy.shoola.agents.classifier.CategoryCtrl
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
package org.openmicroscopy.shoola.agents.classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.shoola.agents.classifier.events.LoadCategories;

/**
 * The controller for the category UI.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class CategoryCtrl
{
    private Classifier classifier;
    
    private static final Integer SAVED_ATTRIBUTE = new Integer(0);
    private static final Integer NEW_ATTRIBUTE = new Integer(1);
    private static final Integer CHANGED_ATTRIBUTE = new Integer(2);
    
    private boolean saved = true;
    
    private int datasetID;
    private String datasetName;
    private LoadCategories loadEvent;
    
    private List categoryGroupList;
    private Map categoryGroupMap;
    
    private Map categoryGroupChangeMap;
    private Map categoryChangeMap;
    
    public CategoryCtrl(Classifier classifier, LoadCategories triggerEvent)
    {
        if(classifier == null || triggerEvent == null)
        {
            throw new IllegalArgumentException("Cannot construct a " +
                "CategoryCtrl with null parameters.");
        }
        
        this.classifier = classifier;
        this.datasetID = triggerEvent.getID();
        this.datasetName = triggerEvent.getName();
        
        categoryGroupList = new ArrayList();
        categoryGroupMap = new HashMap();
        categoryGroupChangeMap = new HashMap();
        categoryChangeMap = new HashMap();
        
        List groupList = classifier.getCategoryGroups(datasetID);
        System.err.println("group list size ="+groupList.size());
        List categoryList = classifier.getCategories(datasetID);
        
        if(groupList == null || groupList.size() == 0)
        {
            return; // everything is brand new.
        }
        categoryGroupList = new ArrayList(groupList);
        Collections.sort(categoryGroupList,new AttributeComparator());
        // initialize hierarchy
        for(Iterator iter = categoryGroupList.iterator(); iter.hasNext();)
        {
            CategoryGroup group = (CategoryGroup)iter.next();
            categoryGroupMap.put(group,new ArrayList());
            categoryGroupChangeMap.put(group,SAVED_ATTRIBUTE);
        }
        
        if(categoryList == null || categoryList.size() == 0)
        {
            return;
        }
        
        for(Iterator iter = categoryList.iterator(); iter.hasNext();)
        {
            Category category = (Category)iter.next();
            CategoryGroup group = category.getCategoryGroup();
            List theList = (List)categoryGroupMap.get(group);
            theList.add(category);
            categoryChangeMap.put(category,SAVED_ATTRIBUTE);
        }
    }
    
    public int getDatasetID()
    {
        return datasetID;
    }
    
    public String getDatasetName()
    {
        return datasetName;
    }
    
    public List getCategoryGroups()
    {
        return categoryGroupList;
    }
    
    public List getCategories(CategoryGroup group)
    {
        List theList = (List)categoryGroupMap.get(group);
        Collections.sort(theList,new AttributeComparator());
        return theList;
    }
    
    public void newCategoryGroup(String groupName, String description)
    {
        CategoryGroup cg =
            classifier.createCategoryGroup(groupName,description,datasetID);
        categoryGroupList.add(cg);
        categoryGroupMap.put(cg,new ArrayList());
        categoryGroupChangeMap.put(cg,NEW_ATTRIBUTE);
        setSaved(false);
    }
    
    public void newCategory(CategoryGroup group, String categoryName,
                            String description)
    {
        Category c = classifier.createCategory(group,categoryName,
                                               description,datasetID);
        List theList = (List)categoryGroupMap.get(group);
        theList.add(c);
        categoryChangeMap.put(c,NEW_ATTRIBUTE);
        setSaved(false);
    }
    
    public boolean isCategoryGroupChanged(CategoryGroup group)
    {
        Integer value = (Integer)categoryGroupChangeMap.get(group);
        if(value == NEW_ATTRIBUTE || value == CHANGED_ATTRIBUTE)
        {
            return true;
        }
        else return false;
    }
    
    public boolean isCategoryChanged(Category category)
    {
        Integer value = (Integer)categoryChangeMap.get(category);
        if(value == NEW_ATTRIBUTE || value == CHANGED_ATTRIBUTE)
        {
            return true;
        }
        else return false;
    }
    
    public void markCategoryGroupAsChanged(CategoryGroup group)
    {
        if(categoryGroupChangeMap.containsKey(group))
        {
            categoryGroupChangeMap.put(group,CHANGED_ATTRIBUTE);
            setSaved(false);
        }
    }
    
    public void markCategoryAsChanged(Category category)
    {
        if(categoryChangeMap.containsKey(category))
        {
            categoryChangeMap.put(category,CHANGED_ATTRIBUTE);
            setSaved(false);
        }
    }
    
    public boolean isSaved()
    {
        return saved;
    }
    
    public void setSaved(boolean saved)
    {
        this.saved = saved;
    }
    
    public boolean save()
    {
        // first, check for any new or changed groups
        List changedGroupList = new ArrayList();
        List newGroupList = new ArrayList();
        for(Iterator iter = categoryGroupChangeMap.keySet().iterator();
            iter.hasNext();)
        {
            CategoryGroup cg = (CategoryGroup)iter.next();
            Integer status = (Integer)categoryGroupChangeMap.get(cg);
            if(status == NEW_ATTRIBUTE)
            {
                newGroupList.add(cg);
            }
            else if(status == CHANGED_ATTRIBUTE)
            {
                changedGroupList.add(cg);
            }
        }
        if(newGroupList.size() > 0)
        {
            classifier.commitNewAttributes(newGroupList);
        }
        if(changedGroupList.size() > 0)
        {
            classifier.updateAttributes(changedGroupList);
        }
        
        List changedCategoryList = new ArrayList();
        List newCategoryList = new ArrayList();
        
        for(Iterator iter = categoryChangeMap.keySet().iterator();
            iter.hasNext();)
        {
            Category c = (Category)iter.next();
            Integer status = (Integer)categoryChangeMap.get(c);
            if(status == NEW_ATTRIBUTE)
            {
                newCategoryList.add(c);
            }
            else if(status == CHANGED_ATTRIBUTE)
            {
                changedCategoryList.add(c);
            }
        }
        
        if(newCategoryList.size() > 0)
        {
            classifier.commitNewAttributes(newCategoryList);
        }
        if(changedCategoryList.size() > 0)
        {
            classifier.updateAttributes(changedCategoryList);
        }
        
        setSaved(true);
        return true;
    }
    
    /**
     * Prompt termination.  Expected return values: true if you can exit,
     * false otherwise.
     * @return
     */
    public boolean canExit()
    {
        if(!isSaved())
        {
            Object[] options = {"Save","Don't Save","Cancel"};
            int outcome = JOptionPane.showOptionDialog(null,
                                         "Would you like to save this categorization?",
                                         "Save Annotation",
                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null,
                                         options,
                                         options[0]);
            if(outcome == JOptionPane.YES_OPTION)
            {
                save();
                return true;
            }
            else if(outcome == JOptionPane.NO_OPTION)
            {
                return true;
            }
            else if(outcome == JOptionPane.CANCEL_OPTION)
            {
                return false;
            }
            else return true; // something's f'ed up; just bail?
        }
        else return true;
    }
    
    public void close()
    {
        classifier.close(this);
    }
    
}
