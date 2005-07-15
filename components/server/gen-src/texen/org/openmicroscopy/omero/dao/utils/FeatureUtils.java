/*
 * org.openmicroscopy.omero.dao.utils.FeatureUtils
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
import org.openmicroscopy.omero.model.Feature;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class FeatureUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(FeatureUtils.class);


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
  
    Feature self = (Feature) o;
    // Cleaning org.openmicroscopy.omero.model.Image::image field
    if (null==self.getImage()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImage())){
      self.setImage(null);
         if (log.isDebugEnabled()){
             log.debug("Set Feature.image to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Image()).getUtils().clean(self.getImage(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Feature.features)
    // Cleaning java.util.Set::features set (Role: org.openmicroscopy.omero.model.Feature.features)
    if (null==self.getFeatures()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFeatures())){
      self.setFeatures(null);
         if (log.isDebugEnabled()){
             log.debug("Set Feature.features to null");
         }
    } else {
      for (Iterator it = self.getFeatures().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.Feature)
      	 //org.openmicroscopy.omero.model.Feature
         (new org.openmicroscopy.omero.model.Feature()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.Feature::feature field
    if (null==self.getFeature()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFeature())){
      self.setFeature(null);
         if (log.isDebugEnabled()){
             log.debug("Set Feature.feature to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Feature()).getUtils().clean(self.getFeature(),done);
    }
  }


}
