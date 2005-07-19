/*
 * org.openmicroscopy.omero.dao.utils.SemanticTypeUtils
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
import org.openmicroscopy.omero.model.SemanticType;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class SemanticTypeUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(SemanticTypeUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    SemanticType self = (SemanticType) o;
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.SemanticType.semanticTypeOutputs)
    // Cleaning java.util.Set::semanticTypeOutputs set (Role: org.openmicroscopy.omero.model.SemanticType.semanticTypeOutputs)
    if (null==self.getSemanticTypeOutputs()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getSemanticTypeOutputs())){
      self.setSemanticTypeOutputs(null);
         if (log.isDebugEnabled()){
             log.debug("Set SemanticType.semanticTypeOutputs to null");
         }
    } else {
      for (Iterator it = self.getSemanticTypeOutputs().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.SemanticTypeOutput)
      	 //org.openmicroscopy.omero.model.SemanticTypeOutput
         (new org.openmicroscopy.omero.model.SemanticTypeOutput()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.SemanticType.formalOutputs)
    // Cleaning java.util.Set::formalOutputs set (Role: org.openmicroscopy.omero.model.SemanticType.formalOutputs)
    if (null==self.getFormalOutputs()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFormalOutputs())){
      self.setFormalOutputs(null);
         if (log.isDebugEnabled()){
             log.debug("Set SemanticType.formalOutputs to null");
         }
    } else {
      for (Iterator it = self.getFormalOutputs().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.FormalOutput)
      	 //org.openmicroscopy.omero.model.FormalOutput
         (new org.openmicroscopy.omero.model.FormalOutput()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.SemanticType.formalInputs)
    // Cleaning java.util.Set::formalInputs set (Role: org.openmicroscopy.omero.model.SemanticType.formalInputs)
    if (null==self.getFormalInputs()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFormalInputs())){
      self.setFormalInputs(null);
         if (log.isDebugEnabled()){
             log.debug("Set SemanticType.formalInputs to null");
         }
    } else {
      for (Iterator it = self.getFormalInputs().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.FormalInput)
      	 //org.openmicroscopy.omero.model.FormalInput
         (new org.openmicroscopy.omero.model.FormalInput()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.SemanticType.semanticElements)
    // Cleaning java.util.Set::semanticElements set (Role: org.openmicroscopy.omero.model.SemanticType.semanticElements)
    if (null==self.getSemanticElements()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getSemanticElements())){
      self.setSemanticElements(null);
         if (log.isDebugEnabled()){
             log.debug("Set SemanticType.semanticElements to null");
         }
    } else {
      for (Iterator it = self.getSemanticElements().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.SemanticElement)
      	 //org.openmicroscopy.omero.model.SemanticElement
         (new org.openmicroscopy.omero.model.SemanticElement()).getUtils().clean(it.next(),done);
      }
    }
  }


}
