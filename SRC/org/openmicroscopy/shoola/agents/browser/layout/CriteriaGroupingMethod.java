/*
 * org.openmicroscopy.shoola.agents.browser.layout.CriteriaGroupingMethod
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

import java.util.*;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.util.GrepOperator;

/**
 * Specifies a grouping by a particular set of criteria.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public class CriteriaGroupingMethod implements GroupingMethod
{
  private List criteria;
  private Map criteriaMap;
  
  /**
   * Creates a grouping method based on certain true/false criteria.
   */
  public CriteriaGroupingMethod()
  {
    criteria = new ArrayList();
    criteriaMap = new IdentityHashMap();
  }
  
  /**
   * Adds the condition/model binding to the method.  Criteria assigned first
   * will take preference in overlapping conditions.  To prevent this behavior,
   * use addCriteriaGroup(int,GrepOperator,GroupModel).
   * 
   * @param condition The condition to test for.
   * @param model The model to bind to the condition.
   */
  public void addCriteriaGroup(GrepOperator condition, GroupModel model)
  {
    if(condition != null && model != null)
    {
      criteria.add(condition);
      criteriaMap.put(condition,model);
    }
  }
  
  /**
   * Adds the condition/model binding to the method using the specified index
   * to indicate priority in case of overlapping true conditions.  Lower numbers
   * indicate higher priority (index must be between 0 and the size of criteria,
   * inclusive)
   * 
   * @param index The index you want to set the criteria to.
   * @param condition The condition to test for.
   * @param model The model to bind to the condition.
   */
  public void addCriteriaGroup(int index, GrepOperator condition,
                               GroupModel model)
  {
    if(index < 0 || index > criteria.size())
    {
      return;
    }
    if(condition != null && model != null)
    {
      criteria.add(index,condition);
      criteriaMap.put(condition,model);
    }
  }
  
  /**
   * Returns the number of groups.
   * @return The number of groups.
   */
  public int getGroupCount()
  {
    return criteria.size();
  }
  
  /**
   * Removes this criteria from the grouping method.
   * @param condition The condition to remove.
   */
  public void removeCriteriaGroup(GrepOperator condition)
  {
    if(condition != null)
    {
      criteria.remove(condition);
      criteriaMap.remove(condition);
    }
    // TODO: reallocate thumbnails to see if they are applicable to other groups?
  }
  
  /**
   * Assigns a thumbnail to a group based on the criteria already stored.  If
   * no such criteria applies to the thumbnail, this method will return null.
   * Otherwise, the selected criteria (the first in the order which applies to
   * the thumbnail) will determine which model the thumbnail belongs to.
   * 
   * @param t The thumbnail to assign.
   * @return The assigned group model.
   * @see org.openmicroscopy.shoola.agents.browser.layout.GroupingMethod#assignGroup(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
   */
  public GroupModel assignGroup(Thumbnail t)
  {
    if(t == null)
    {
      return null;
    }
    for(Iterator iter = criteria.iterator(); iter.hasNext();)
    {
      GrepOperator condition = (GrepOperator)iter.next();
      if(condition.eval(t))
      {
        GroupModel model = (GroupModel)criteriaMap.get(condition);
        model.addThumbnail(t);
        return model;
      }
    }
    return null;
  }
  
  /**
   * Gets the group model that a particular thumbnail belongs to.  If the
   * thumbnail is null or no such group exists, this method will return null.
   * 
   * @param t The thumbnail to query.
   * @return The group this thumbnail belongs to.
   * @see org.openmicroscopy.shoola.agents.browser.layout.GroupingMethod#getGroup(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
   */
  public GroupModel getGroup(Thumbnail t)
  {
    if(t == null)
    {
      return null;
    }
    for(Iterator iter = criteria.iterator(); iter.hasNext();)
    {
      GrepOperator condition = (GrepOperator)iter.next();
      if(condition.eval(t))
      {
        GroupModel model = (GroupModel)criteriaMap.get(condition);
        if(model.getThumbnails().contains(t))
        {
          return model;
        }
      }
    }
    return null;
  }

  // TODO: reweight based on order?
}
