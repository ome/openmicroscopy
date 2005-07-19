/*
 * org.openmicroscopy.omero.dao.utils.ExperimenterUtils
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
import org.openmicroscopy.omero.model.Experimenter;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class ExperimenterUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(ExperimenterUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    Experimenter self = (Experimenter) o;
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.renderingSettings)
    // Cleaning java.util.Set::renderingSettings set (Role: org.openmicroscopy.omero.model.Experimenter.renderingSettings)
    if (null==self.getRenderingSettings()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getRenderingSettings())){
      self.setRenderingSettings(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.renderingSettings to null");
         }
    } else {
      for (Iterator it = self.getRenderingSettings().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.RenderingSetting)
      	 //org.openmicroscopy.omero.model.RenderingSetting
         (new org.openmicroscopy.omero.model.RenderingSetting()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.datasets)
    // Cleaning java.util.Set::datasets set (Role: org.openmicroscopy.omero.model.Experimenter.datasets)
    if (null==self.getDatasets()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDatasets())){
      self.setDatasets(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.datasets to null");
         }
    } else {
      for (Iterator it = self.getDatasets().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Dataset)
      	 //org.openmicroscopy.omero.model.Dataset
         (new org.openmicroscopy.omero.model.Dataset()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.groupsByLeader)
    // Cleaning java.util.Set::groupsByLeader set (Role: org.openmicroscopy.omero.model.Experimenter.groupsByLeader)
    if (null==self.getGroupsByLeader()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroupsByLeader())){
      self.setGroupsByLeader(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.groupsByLeader to null");
         }
    } else {
      for (Iterator it = self.getGroupsByLeader().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Group)
      	 //org.openmicroscopy.omero.model.Group
         (new org.openmicroscopy.omero.model.Group()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.groupsByContact)
    // Cleaning java.util.Set::groupsByContact set (Role: org.openmicroscopy.omero.model.Experimenter.groupsByContact)
    if (null==self.getGroupsByContact()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroupsByContact())){
      self.setGroupsByContact(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.groupsByContact to null");
         }
    } else {
      for (Iterator it = self.getGroupsByContact().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Group)
      	 //org.openmicroscopy.omero.model.Group
         (new org.openmicroscopy.omero.model.Group()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.analysisChains)
    // Cleaning java.util.Set::analysisChains set (Role: org.openmicroscopy.omero.model.Experimenter.analysisChains)
    if (null==self.getAnalysisChains()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChains())){
      self.setAnalysisChains(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.analysisChains to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChains().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChain)
      	 //org.openmicroscopy.omero.model.AnalysisChain
         (new org.openmicroscopy.omero.model.AnalysisChain()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.images)
    // Cleaning java.util.Set::images set (Role: org.openmicroscopy.omero.model.Experimenter.images)
    if (null==self.getImages()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImages())){
      self.setImages(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.images to null");
         }
    } else {
      for (Iterator it = self.getImages().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Image)
      	 //org.openmicroscopy.omero.model.Image
         (new org.openmicroscopy.omero.model.Image()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.analysisChainExecutions)
    // Cleaning java.util.Set::analysisChainExecutions set (Role: org.openmicroscopy.omero.model.Experimenter.analysisChainExecutions)
    if (null==self.getAnalysisChainExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainExecutions())){
      self.setAnalysisChainExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.analysisChainExecutions to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainExecution)
      	 //org.openmicroscopy.omero.model.AnalysisChainExecution
         (new org.openmicroscopy.omero.model.AnalysisChainExecution()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.projects)
    // Cleaning java.util.Set::projects set (Role: org.openmicroscopy.omero.model.Experimenter.projects)
    if (null==self.getProjects()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getProjects())){
      self.setProjects(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.projects to null");
         }
    } else {
      for (Iterator it = self.getProjects().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Project)
      	 //org.openmicroscopy.omero.model.Project
         (new org.openmicroscopy.omero.model.Project()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.omeSessions)
    // Cleaning java.util.Set::omeSessions set (Role: org.openmicroscopy.omero.model.Experimenter.omeSessions)
    if (null==self.getOmeSessions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getOmeSessions())){
      self.setOmeSessions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.omeSessions to null");
         }
    } else {
      for (Iterator it = self.getOmeSessions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.OmeSession)
      	 //org.openmicroscopy.omero.model.OmeSession
         (new org.openmicroscopy.omero.model.OmeSession()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.moduleExecutions)
    // Cleaning java.util.Set::moduleExecutions set (Role: org.openmicroscopy.omero.model.Experimenter.moduleExecutions)
    if (null==self.getModuleExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecutions())){
      self.setModuleExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.moduleExecutions to null");
         }
    } else {
      for (Iterator it = self.getModuleExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ModuleExecution)
      	 //org.openmicroscopy.omero.model.ModuleExecution
         (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.Group::group field
    if (null==self.getGroup()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroup())){
      self.setGroup(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.group to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Group()).getUtils().clean(self.getGroup(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.ModuleExecution::moduleExecution field
    if (null==self.getModuleExecution()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecution())){
      self.setModuleExecution(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.moduleExecution to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(self.getModuleExecution(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Experimenter.groups)
    // Cleaning java.util.Set::groups set (Role: org.openmicroscopy.omero.model.Experimenter.groups)
    if (null==self.getGroups()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroups())){
      self.setGroups(null);
         if (log.isDebugEnabled()){
             log.debug("Set Experimenter.groups to null");
         }
    } else {
      for (Iterator it = self.getGroups().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Group)
      	 //org.openmicroscopy.omero.model.Group
         (new org.openmicroscopy.omero.model.Group()).getUtils().clean(it.next(),done);
      }
    }
  }


}
