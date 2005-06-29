/*
 * org.openmicroscopy.omero.dao.utils.ModuleExecutionUtils
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
import org.openmicroscopy.omero.model.ModuleExecution;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class ModuleExecutionUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(ModuleExecutionUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  //DONE Logging
  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    ModuleExecution self = (ModuleExecution) o;
    // Cleaning org.openmicroscopy.omero.model.Image::image field
    if (null==self.getImage()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImage())){
      self.setImage(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.image to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Image()).getUtils().clean(self.getImage(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Dataset::dataset field
    if (null==self.getDataset()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDataset())){
      self.setDataset(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.dataset to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Dataset()).getUtils().clean(self.getDataset(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Group::group field
    if (null==self.getGroup()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroup())){
      self.setGroup(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.group to null");
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
             log.debug("Set ModuleExecution.experimenter to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(self.getExperimenter(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.thumbnails)
    // Cleaning java.util.Set::thumbnails set (Role: org.openmicroscopy.omero.model.ModuleExecution.thumbnails)
    if (null==self.getThumbnails()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getThumbnails())){
      self.setThumbnails(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.thumbnails to null");
         }
    } else {
      for (Iterator it = self.getThumbnails().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Thumbnail)
      	 //org.openmicroscopy.omero.model.Thumbnail
         (new org.openmicroscopy.omero.model.Thumbnail()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.classifications)
    // Cleaning java.util.Set::classifications set (Role: org.openmicroscopy.omero.model.ModuleExecution.classifications)
    if (null==self.getClassifications()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getClassifications())){
      self.setClassifications(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.classifications to null");
         }
    } else {
      for (Iterator it = self.getClassifications().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Classification)
      	 //org.openmicroscopy.omero.model.Classification
         (new org.openmicroscopy.omero.model.Classification()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.repositories)
    // Cleaning java.util.Set::repositories set (Role: org.openmicroscopy.omero.model.ModuleExecution.repositories)
    if (null==self.getRepositories()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getRepositories())){
      self.setRepositories(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.repositories to null");
         }
    } else {
      for (Iterator it = self.getRepositories().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Repository)
      	 //org.openmicroscopy.omero.model.Repository
         (new org.openmicroscopy.omero.model.Repository()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.categories)
    // Cleaning java.util.Set::categories set (Role: org.openmicroscopy.omero.model.ModuleExecution.categories)
    if (null==self.getCategories()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getCategories())){
      self.setCategories(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.categories to null");
         }
    } else {
      for (Iterator it = self.getCategories().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Category)
      	 //org.openmicroscopy.omero.model.Category
         (new org.openmicroscopy.omero.model.Category()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.displayRois)
    // Cleaning java.util.Set::displayRois set (Role: org.openmicroscopy.omero.model.ModuleExecution.displayRois)
    if (null==self.getDisplayRois()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDisplayRois())){
      self.setDisplayRois(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.displayRois to null");
         }
    } else {
      for (Iterator it = self.getDisplayRois().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.DisplayRoi)
      	 //org.openmicroscopy.omero.model.DisplayRoi
         (new org.openmicroscopy.omero.model.DisplayRoi()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.imageInfos)
    // Cleaning java.util.Set::imageInfos set (Role: org.openmicroscopy.omero.model.ModuleExecution.imageInfos)
    if (null==self.getImageInfos()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImageInfos())){
      self.setImageInfos(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.imageInfos to null");
         }
    } else {
      for (Iterator it = self.getImageInfos().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImageInfo)
      	 //org.openmicroscopy.omero.model.ImageInfo
         (new org.openmicroscopy.omero.model.ImageInfo()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.imagePlates)
    // Cleaning java.util.Set::imagePlates set (Role: org.openmicroscopy.omero.model.ModuleExecution.imagePlates)
    if (null==self.getImagePlates()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImagePlates())){
      self.setImagePlates(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.imagePlates to null");
         }
    } else {
      for (Iterator it = self.getImagePlates().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImagePlate)
      	 //org.openmicroscopy.omero.model.ImagePlate
         (new org.openmicroscopy.omero.model.ImagePlate()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.imagePixels)
    // Cleaning java.util.Set::imagePixels set (Role: org.openmicroscopy.omero.model.ModuleExecution.imagePixels)
    if (null==self.getImagePixels()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImagePixels())){
      self.setImagePixels(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.imagePixels to null");
         }
    } else {
      for (Iterator it = self.getImagePixels().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImagePixel)
      	 //org.openmicroscopy.omero.model.ImagePixel
         (new org.openmicroscopy.omero.model.ImagePixel()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.groups)
    // Cleaning java.util.Set::groups set (Role: org.openmicroscopy.omero.model.ModuleExecution.groups)
    if (null==self.getGroups()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroups())){
      self.setGroups(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.groups to null");
         }
    } else {
      for (Iterator it = self.getGroups().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Group)
      	 //org.openmicroscopy.omero.model.Group
         (new org.openmicroscopy.omero.model.Group()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.experimenters)
    // Cleaning java.util.Set::experimenters set (Role: org.openmicroscopy.omero.model.ModuleExecution.experimenters)
    if (null==self.getExperimenters()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getExperimenters())){
      self.setExperimenters(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.experimenters to null");
         }
    } else {
      for (Iterator it = self.getExperimenters().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Experimenter)
      	 //org.openmicroscopy.omero.model.Experimenter
         (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.categoryGroups)
    // Cleaning java.util.Set::categoryGroups set (Role: org.openmicroscopy.omero.model.ModuleExecution.categoryGroups)
    if (null==self.getCategoryGroups()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getCategoryGroups())){
      self.setCategoryGroups(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.categoryGroups to null");
         }
    } else {
      for (Iterator it = self.getCategoryGroups().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.CategoryGroup)
      	 //org.openmicroscopy.omero.model.CategoryGroup
         (new org.openmicroscopy.omero.model.CategoryGroup()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.imageAnnotations)
    // Cleaning java.util.Set::imageAnnotations set (Role: org.openmicroscopy.omero.model.ModuleExecution.imageAnnotations)
    if (null==self.getImageAnnotations()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImageAnnotations())){
      self.setImageAnnotations(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.imageAnnotations to null");
         }
    } else {
      for (Iterator it = self.getImageAnnotations().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImageAnnotation)
      	 //org.openmicroscopy.omero.model.ImageAnnotation
         (new org.openmicroscopy.omero.model.ImageAnnotation()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.datasetAnnotations)
    // Cleaning java.util.Set::datasetAnnotations set (Role: org.openmicroscopy.omero.model.ModuleExecution.datasetAnnotations)
    if (null==self.getDatasetAnnotations()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDatasetAnnotations())){
      self.setDatasetAnnotations(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.datasetAnnotations to null");
         }
    } else {
      for (Iterator it = self.getDatasetAnnotations().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.DatasetAnnotation)
      	 //org.openmicroscopy.omero.model.DatasetAnnotation
         (new org.openmicroscopy.omero.model.DatasetAnnotation()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.imageDimensions)
    // Cleaning java.util.Set::imageDimensions set (Role: org.openmicroscopy.omero.model.ModuleExecution.imageDimensions)
    if (null==self.getImageDimensions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImageDimensions())){
      self.setImageDimensions(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.imageDimensions to null");
         }
    } else {
      for (Iterator it = self.getImageDimensions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImageDimension)
      	 //org.openmicroscopy.omero.model.ImageDimension
         (new org.openmicroscopy.omero.model.ImageDimension()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.channelComponents)
    // Cleaning java.util.Set::channelComponents set (Role: org.openmicroscopy.omero.model.ModuleExecution.channelComponents)
    if (null==self.getChannelComponents()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getChannelComponents())){
      self.setChannelComponents(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.channelComponents to null");
         }
    } else {
      for (Iterator it = self.getChannelComponents().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ChannelComponent)
      	 //org.openmicroscopy.omero.model.ChannelComponent
         (new org.openmicroscopy.omero.model.ChannelComponent()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ModuleExecution.displayOptions)
    // Cleaning java.util.Set::displayOptions set (Role: org.openmicroscopy.omero.model.ModuleExecution.displayOptions)
    if (null==self.getDisplayOptions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDisplayOptions())){
      self.setDisplayOptions(null);
         if (log.isDebugEnabled()){
             log.debug("Set ModuleExecution.displayOptions to null");
         }
    } else {
      for (Iterator it = self.getDisplayOptions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.DisplayOption)
      	 //org.openmicroscopy.omero.model.DisplayOption
         (new org.openmicroscopy.omero.model.DisplayOption()).getUtils().clean(it.next(),done);
      }
    }
  }


}
