/*
 * org.openmicroscopy.omero.dao.utils.FormalInputUtils
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
import org.openmicroscopy.omero.model.FormalInput;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class FormalInputUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(FormalInputUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    FormalInput self = (FormalInput) o;
    // Cleaning org.openmicroscopy.omero.model.Module::module field
    if (null==self.getModule()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModule())){
      self.setModule(null);
         if (log.isDebugEnabled()){
             log.debug("Set FormalInput.module to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Module()).getUtils().clean(self.getModule(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.SemanticType::semanticType field
    if (null==self.getSemanticType()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getSemanticType())){
      self.setSemanticType(null);
         if (log.isDebugEnabled()){
             log.debug("Set FormalInput.semanticType to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.SemanticType()).getUtils().clean(self.getSemanticType(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.FormalInput.analysisChainLinks)
    // Cleaning java.util.Set::analysisChainLinks set (Role: org.openmicroscopy.omero.model.FormalInput.analysisChainLinks)
    if (null==self.getAnalysisChainLinks()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainLinks())){
      self.setAnalysisChainLinks(null);
         if (log.isDebugEnabled()){
             log.debug("Set FormalInput.analysisChainLinks to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainLinks().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainLink)
      	 //org.openmicroscopy.omero.model.AnalysisChainLink
         (new org.openmicroscopy.omero.model.AnalysisChainLink()).getUtils().clean(it.next(),done);
      }
    }
  }


}
