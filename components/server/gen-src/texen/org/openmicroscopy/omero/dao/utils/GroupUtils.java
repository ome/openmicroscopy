/*
 * org.openmicroscopy.omero.dao.utils.GroupUtils
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
import org.openmicroscopy.omero.model.Group;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class GroupUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(GroupUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    Group self = (Group) o;
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Group.datasets)
    // Cleaning java.util.Set::datasets set (Role: org.openmicroscopy.omero.model.Group.datasets)
    if (null==self.getDatasets()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDatasets())){
      self.setDatasets(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.datasets to null");
         }
    } else {
      for (Iterator it = self.getDatasets().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Dataset)
      	 //org.openmicroscopy.omero.model.Dataset
         (new org.openmicroscopy.omero.model.Dataset()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Group.images)
    // Cleaning java.util.Set::images set (Role: org.openmicroscopy.omero.model.Group.images)
    if (null==self.getImages()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImages())){
      self.setImages(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.images to null");
         }
    } else {
      for (Iterator it = self.getImages().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Image)
      	 //org.openmicroscopy.omero.model.Image
         (new org.openmicroscopy.omero.model.Image()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Group.projects)
    // Cleaning java.util.Set::projects set (Role: org.openmicroscopy.omero.model.Group.projects)
    if (null==self.getProjects()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getProjects())){
      self.setProjects(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.projects to null");
         }
    } else {
      for (Iterator it = self.getProjects().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Project)
      	 //org.openmicroscopy.omero.model.Project
         (new org.openmicroscopy.omero.model.Project()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Group.experimenters)
    // Cleaning java.util.Set::experimenters set (Role: org.openmicroscopy.omero.model.Group.experimenters)
    if (null==self.getExperimenters()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getExperimenters())){
      self.setExperimenters(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.experimenters to null");
         }
    } else {
      for (Iterator it = self.getExperimenters().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Experimenter)
      	 //org.openmicroscopy.omero.model.Experimenter
         (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Group.moduleExecutions)
    // Cleaning java.util.Set::moduleExecutions set (Role: org.openmicroscopy.omero.model.Group.moduleExecutions)
    if (null==self.getModuleExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecutions())){
      self.setModuleExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.moduleExecutions to null");
         }
    } else {
      for (Iterator it = self.getModuleExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ModuleExecution)
      	 //org.openmicroscopy.omero.model.ModuleExecution
         (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Group.imageInfos)
    // Cleaning java.util.Set::imageInfos set (Role: org.openmicroscopy.omero.model.Group.imageInfos)
    if (null==self.getImageInfos()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImageInfos())){
      self.setImageInfos(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.imageInfos to null");
         }
    } else {
      for (Iterator it = self.getImageInfos().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImageInfo)
      	 //org.openmicroscopy.omero.model.ImageInfo
         (new org.openmicroscopy.omero.model.ImageInfo()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.Experimenter::leader field
    if (null==self.getLeader()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getLeader())){
      self.setLeader(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.leader to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(self.getLeader(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Experimenter::contact field
    if (null==self.getContact()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getContact())){
      self.setContact(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.contact to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(self.getContact(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.ModuleExecution::moduleExecution field
    if (null==self.getModuleExecution()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecution())){
      self.setModuleExecution(null);
         if (log.isDebugEnabled()){
             log.debug("Set Group.moduleExecution to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(self.getModuleExecution(),done);
    }
  }


}
