/*
 * org.openmicroscopy.omero.dao.utils.CategoryUtils
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package org.openmicroscopy.omero.dao.utils;

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;

//Application-internal dependencies
import org.openmicroscopy.omero.BaseModelUtils;
import org.openmicroscopy.omero.model.Category;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class CategoryUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(CategoryUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  //TODO Logging
  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    Category self = (Category) o;
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Category.classifications)
    // Cleaning java.util.Set::classifications set (Role: org.openmicroscopy.omero.model.Category.classifications)
    if (null==self.getClassifications()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getClassifications())){
      self.setClassifications(null);
         if (log.isDebugEnabled()){
             log.debug("Set Category.classifications to null");
         }
    } else {
      for (Iterator it = self.getClassifications().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Classification)
      	 //org.openmicroscopy.omero.model.Classification
         (new org.openmicroscopy.omero.model.Classification()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.CategoryGroup::categoryGroup field
    if (null==self.getCategoryGroup()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getCategoryGroup())){
      self.setCategoryGroup(null);
         if (log.isDebugEnabled()){
             log.debug("Set Category.categoryGroup to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.CategoryGroup()).getUtils().clean(self.getCategoryGroup(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.ModuleExecution::moduleExecution field
    if (null==self.getModuleExecution()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecution())){
      self.setModuleExecution(null);
         if (log.isDebugEnabled()){
             log.debug("Set Category.moduleExecution to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(self.getModuleExecution(),done);
    }
  }


}
