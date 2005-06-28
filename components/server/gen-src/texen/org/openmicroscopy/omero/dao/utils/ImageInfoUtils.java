/*
 * org.openmicroscopy.omero.dao.utils.ImageInfoUtils
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
import org.openmicroscopy.omero.model.ImageInfo;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class ImageInfoUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(ImageInfoUtils.class);


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
  
    ImageInfo self = (ImageInfo) o;
    // Cleaning org.openmicroscopy.omero.model.Image::image field
    if (null==self.getImage()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getImage())){
      self.setImage(null);
         if (log.isDebugEnabled()){
             log.debug("Set ImageInfo.image to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Image()).getUtils().clean(self.getImage(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Group::group field
    if (null==self.getGroup()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getGroup())){
      self.setGroup(null);
         if (log.isDebugEnabled()){
             log.debug("Set ImageInfo.group to null");
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
             log.debug("Set ImageInfo.moduleExecution to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(self.getModuleExecution(),done);
    }
  }


}
