/*
 * org.openmicroscopy.omero.dao.utils.ImageUtils
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
import org.openmicroscopy.omero.model.Image;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class ImageUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(ImageUtils.class);


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
  
    Image self = (Image) o;
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.thumbnails)
    // Cleaning java.util.Set::thumbnails set (Role: org.openmicroscopy.omero.model.Image.thumbnails)
    if (null==self.getThumbnails()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getThumbnails())){
      self.setThumbnails(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.thumbnails to null");
         }
    } else {
      for (Iterator it = self.getThumbnails().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Thumbnail)
      	 //org.openmicroscopy.omero.model.Thumbnail
         (new org.openmicroscopy.omero.model.Thumbnail()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.classifications)
    // Cleaning java.util.Set::classifications set (Role: org.openmicroscopy.omero.model.Image.classifications)
    if (null==self.getClassifications()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getClassifications())){
      self.setClassifications(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.classifications to null");
         }
    } else {
      for (Iterator it = self.getClassifications().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Classification)
      	 //org.openmicroscopy.omero.model.Classification
         (new org.openmicroscopy.omero.model.Classification()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.displayRois)
    // Cleaning java.util.Set::displayRois set (Role: org.openmicroscopy.omero.model.Image.displayRois)
    if (null==self.getDisplayRois()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDisplayRois())){
      self.setDisplayRois(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.displayRois to null");
         }
    } else {
      for (Iterator it = self.getDisplayRois().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.DisplayRoi)
      	 //org.openmicroscopy.omero.model.DisplayRoi
         (new org.openmicroscopy.omero.model.DisplayRoi()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.imageInfos)
    // Cleaning java.util.Set::imageInfos set (Role: org.openmicroscopy.omero.model.Image.imageInfos)
    if (null==self.getImageInfos()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImageInfos())){
      self.setImageInfos(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.imageInfos to null");
         }
    } else {
      for (Iterator it = self.getImageInfos().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImageInfo)
      	 //org.openmicroscopy.omero.model.ImageInfo
         (new org.openmicroscopy.omero.model.ImageInfo()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.imagePixels)
    // Cleaning java.util.Set::imagePixels set (Role: org.openmicroscopy.omero.model.Image.imagePixels)
    if (null==self.getImagePixels()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImagePixels())){
      self.setImagePixels(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.imagePixels to null");
         }
    } else {
      for (Iterator it = self.getImagePixels().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImagePixel)
      	 //org.openmicroscopy.omero.model.ImagePixel
         (new org.openmicroscopy.omero.model.ImagePixel()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.imagePlates)
    // Cleaning java.util.Set::imagePlates set (Role: org.openmicroscopy.omero.model.Image.imagePlates)
    if (null==self.getImagePlates()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImagePlates())){
      self.setImagePlates(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.imagePlates to null");
         }
    } else {
      for (Iterator it = self.getImagePlates().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImagePlate)
      	 //org.openmicroscopy.omero.model.ImagePlate
         (new org.openmicroscopy.omero.model.ImagePlate()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.features)
    // Cleaning java.util.Set::features set (Role: org.openmicroscopy.omero.model.Image.features)
    if (null==self.getFeatures()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFeatures())){
      self.setFeatures(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.features to null");
         }
    } else {
      for (Iterator it = self.getFeatures().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Feature)
      	 //org.openmicroscopy.omero.model.Feature
         (new org.openmicroscopy.omero.model.Feature()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.imageAnnotations)
    // Cleaning java.util.Set::imageAnnotations set (Role: org.openmicroscopy.omero.model.Image.imageAnnotations)
    if (null==self.getImageAnnotations()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImageAnnotations())){
      self.setImageAnnotations(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.imageAnnotations to null");
         }
    } else {
      for (Iterator it = self.getImageAnnotations().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImageAnnotation)
      	 //org.openmicroscopy.omero.model.ImageAnnotation
         (new org.openmicroscopy.omero.model.ImageAnnotation()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.moduleExecutions)
    // Cleaning java.util.Set::moduleExecutions set (Role: org.openmicroscopy.omero.model.Image.moduleExecutions)
    if (null==self.getModuleExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecutions())){
      self.setModuleExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.moduleExecutions to null");
         }
    } else {
      for (Iterator it = self.getModuleExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ModuleExecution)
      	 //org.openmicroscopy.omero.model.ModuleExecution
         (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.imageDimensions)
    // Cleaning java.util.Set::imageDimensions set (Role: org.openmicroscopy.omero.model.Image.imageDimensions)
    if (null==self.getImageDimensions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImageDimensions())){
      self.setImageDimensions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.imageDimensions to null");
         }
    } else {
      for (Iterator it = self.getImageDimensions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ImageDimension)
      	 //org.openmicroscopy.omero.model.ImageDimension
         (new org.openmicroscopy.omero.model.ImageDimension()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.channelComponents)
    // Cleaning java.util.Set::channelComponents set (Role: org.openmicroscopy.omero.model.Image.channelComponents)
    if (null==self.getChannelComponents()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getChannelComponents())){
      self.setChannelComponents(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.channelComponents to null");
         }
    } else {
      for (Iterator it = self.getChannelComponents().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ChannelComponent)
      	 //org.openmicroscopy.omero.model.ChannelComponent
         (new org.openmicroscopy.omero.model.ChannelComponent()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.displayOptions)
    // Cleaning java.util.Set::displayOptions set (Role: org.openmicroscopy.omero.model.Image.displayOptions)
    if (null==self.getDisplayOptions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDisplayOptions())){
      self.setDisplayOptions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.displayOptions to null");
         }
    } else {
      for (Iterator it = self.getDisplayOptions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.DisplayOption)
      	 //org.openmicroscopy.omero.model.DisplayOption
         (new org.openmicroscopy.omero.model.DisplayOption()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.ImagePixel::imagePixel field
    if (null==self.getImagePixel()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImagePixel())){
      self.setImagePixel(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.imagePixel to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.ImagePixel()).getUtils().clean(self.getImagePixel(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Group::group field
    if (null==self.getGroup()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroup())){
      self.setGroup(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.group to null");
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
             log.debug("Set Image.experimenter to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(self.getExperimenter(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Image.datasets)
    // Cleaning java.util.Set::datasets set (Role: org.openmicroscopy.omero.model.Image.datasets)
    if (null==self.getDatasets()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDatasets())){
      self.setDatasets(null);
         if (log.isDebugEnabled()){
             log.debug("Set Image.datasets to null");
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
