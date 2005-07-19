/*
 * org.openmicroscopy.omero.dao.utils.DatasetUtils
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
import org.openmicroscopy.omero.model.Dataset;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class DatasetUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(DatasetUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    Dataset self = (Dataset) o;
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Dataset.analysisChainExecutions)
    // Cleaning java.util.Set::analysisChainExecutions set (Role: org.openmicroscopy.omero.model.Dataset.analysisChainExecutions)
    if (null==self.getAnalysisChainExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainExecutions())){
      self.setAnalysisChainExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Dataset.analysisChainExecutions to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainExecution)
      	 //org.openmicroscopy.omero.model.AnalysisChainExecution
         (new org.openmicroscopy.omero.model.AnalysisChainExecution()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Dataset.omeSessions)
    // Cleaning java.util.Set::omeSessions set (Role: org.openmicroscopy.omero.model.Dataset.omeSessions)
    if (null==self.getOmeSessions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getOmeSessions())){
      self.setOmeSessions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Dataset.omeSessions to null");
         }
    } else {
      for (Iterator it = self.getOmeSessions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.OmeSession)
      	 //org.openmicroscopy.omero.model.OmeSession
         (new org.openmicroscopy.omero.model.OmeSession()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Dataset.moduleExecutions)
    // Cleaning java.util.Set::moduleExecutions set (Role: org.openmicroscopy.omero.model.Dataset.moduleExecutions)
    if (null==self.getModuleExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecutions())){
      self.setModuleExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Dataset.moduleExecutions to null");
         }
    } else {
      for (Iterator it = self.getModuleExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ModuleExecution)
      	 //org.openmicroscopy.omero.model.ModuleExecution
         (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Dataset.datasetAnnotations)
    // Cleaning java.util.Set::datasetAnnotations set (Role: org.openmicroscopy.omero.model.Dataset.datasetAnnotations)
    if (null==self.getDatasetAnnotations()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDatasetAnnotations())){
      self.setDatasetAnnotations(null);
         if (log.isDebugEnabled()){
             log.debug("Set Dataset.datasetAnnotations to null");
         }
    } else {
      for (Iterator it = self.getDatasetAnnotations().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.DatasetAnnotation)
      	 //org.openmicroscopy.omero.model.DatasetAnnotation
         (new org.openmicroscopy.omero.model.DatasetAnnotation()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.Experimenter::experimenter field
    if (null==self.getExperimenter()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getExperimenter())){
      self.setExperimenter(null);
         if (log.isDebugEnabled()){
             log.debug("Set Dataset.experimenter to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(self.getExperimenter(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Group::group field
    if (null==self.getGroup()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroup())){
      self.setGroup(null);
         if (log.isDebugEnabled()){
             log.debug("Set Dataset.group to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Group()).getUtils().clean(self.getGroup(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Dataset.projects)
    // Cleaning java.util.Set::projects set (Role: org.openmicroscopy.omero.model.Dataset.projects)
    if (null==self.getProjects()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getProjects())){
      self.setProjects(null);
         if (log.isDebugEnabled()){
             log.debug("Set Dataset.projects to null");
         }
    } else {
      for (Iterator it = self.getProjects().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Project)
      	 //org.openmicroscopy.omero.model.Project
         (new org.openmicroscopy.omero.model.Project()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Dataset.images)
    // Cleaning java.util.Set::images set (Role: org.openmicroscopy.omero.model.Dataset.images)
    if (null==self.getImages()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImages())){
      self.setImages(null);
         if (log.isDebugEnabled()){
             log.debug("Set Dataset.images to null");
         }
    } else {
      for (Iterator it = self.getImages().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Image)
      	 //org.openmicroscopy.omero.model.Image
         (new org.openmicroscopy.omero.model.Image()).getUtils().clean(it.next(),done);
      }
    }
  }


}
