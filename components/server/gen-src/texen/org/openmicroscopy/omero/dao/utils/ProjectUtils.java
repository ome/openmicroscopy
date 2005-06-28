/*
 * org.openmicroscopy.omero.dao.utils.ProjectUtils
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
import org.openmicroscopy.omero.model.Project;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class ProjectUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(ProjectUtils.class);


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
  
    Project self = (Project) o;
    // Cleaning org.openmicroscopy.omero.model.Group::group field
    if (null==self.getGroup()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroup())){
      self.setGroup(null);
         if (log.isDebugEnabled()){
             log.debug("Set Project.group to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Group()).getUtils().clean(self.getGroup(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Experimenter::experimenter field
    if (null==self.getExperimenter()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getExperimenter())){
      self.setExperimenter(null);
         if (log.isDebugEnabled()){
             log.debug("Set Project.experimenter to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(self.getExperimenter(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Project.datasets)
    // Cleaning java.util.Set::datasets set (Role: org.openmicroscopy.omero.model.Project.datasets)
    if (null==self.getDatasets()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDatasets())){
      self.setDatasets(null);
         if (log.isDebugEnabled()){
             log.debug("Set Project.datasets to null");
         }
    } else {
      for (Iterator it = self.getDatasets().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Dataset)
      	 //org.openmicroscopy.omero.model.Dataset
         (new org.openmicroscopy.omero.model.Dataset()).getUtils().clean(it.next(),done);
      }
    }
  }


}
