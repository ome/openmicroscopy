/*
 * org.openmicroscopy.omero.dao.utils.ImagePixelUtils
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
import org.openmicroscopy.omero.model.ImagePixel;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class ImagePixelUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(ImagePixelUtils.class);


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
  
    ImagePixel self = (ImagePixel) o;
    // Cleaning org.openmicroscopy.omero.model.Image::image field
    if (null==self.getImage()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImage())){
      self.setImage(null);
         if (log.isDebugEnabled()){
             log.debug("Set ImagePixel.image to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Image()).getUtils().clean(self.getImage(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Repository::repository field
    if (null==self.getRepository()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getRepository())){
      self.setRepository(null);
         if (log.isDebugEnabled()){
             log.debug("Set ImagePixel.repository to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Repository()).getUtils().clean(self.getRepository(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ImagePixel.images)
    // Cleaning java.util.Set::images set (Role: org.openmicroscopy.omero.model.ImagePixel.images)
    if (null==self.getImages()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImages())){
      self.setImages(null);
         if (log.isDebugEnabled()){
             log.debug("Set ImagePixel.images to null");
         }
    } else {
      for (Iterator it = self.getImages().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Image)
      	 //org.openmicroscopy.omero.model.Image
         (new org.openmicroscopy.omero.model.Image()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ImagePixel.displayOptions)
    // Cleaning java.util.Set::displayOptions set (Role: org.openmicroscopy.omero.model.ImagePixel.displayOptions)
    if (null==self.getDisplayOptions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDisplayOptions())){
      self.setDisplayOptions(null);
         if (log.isDebugEnabled()){
             log.debug("Set ImagePixel.displayOptions to null");
         }
    } else {
      for (Iterator it = self.getDisplayOptions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.DisplayOption)
      	 //org.openmicroscopy.omero.model.DisplayOption
         (new org.openmicroscopy.omero.model.DisplayOption()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.ImagePixel.channelComponents)
    // Cleaning java.util.Set::channelComponents set (Role: org.openmicroscopy.omero.model.ImagePixel.channelComponents)
    if (null==self.getChannelComponents()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getChannelComponents())){
      self.setChannelComponents(null);
         if (log.isDebugEnabled()){
             log.debug("Set ImagePixel.channelComponents to null");
         }
    } else {
      for (Iterator it = self.getChannelComponents().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ChannelComponent)
      	 //org.openmicroscopy.omero.model.ChannelComponent
         (new org.openmicroscopy.omero.model.ChannelComponent()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.ModuleExecution::moduleExecution field
    if (null==self.getModuleExecution()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecution())){
      self.setModuleExecution(null);
         if (log.isDebugEnabled()){
             log.debug("Set ImagePixel.moduleExecution to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(self.getModuleExecution(),done);
    }
  }


}
