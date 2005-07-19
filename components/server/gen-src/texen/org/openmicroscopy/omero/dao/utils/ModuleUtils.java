/*
 * org.openmicroscopy.omero.dao.utils.ModuleUtils
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
import org.openmicroscopy.omero.model.Module;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class ModuleUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(ModuleUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    Module self = (Module) o;
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Module.formalOutputs)
    // Cleaning java.util.Set::formalOutputs set (Role: org.openmicroscopy.omero.model.Module.formalOutputs)
    if (null==self.getFormalOutputs()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFormalOutputs())){
      self.setFormalOutputs(null);
         if (log.isDebugEnabled()){
             log.debug("Set Module.formalOutputs to null");
         }
    } else {
      for (Iterator it = self.getFormalOutputs().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.FormalOutput)
      	 //org.openmicroscopy.omero.model.FormalOutput
         (new org.openmicroscopy.omero.model.FormalOutput()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Module.formalInputs)
    // Cleaning java.util.Set::formalInputs set (Role: org.openmicroscopy.omero.model.Module.formalInputs)
    if (null==self.getFormalInputs()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFormalInputs())){
      self.setFormalInputs(null);
         if (log.isDebugEnabled()){
             log.debug("Set Module.formalInputs to null");
         }
    } else {
      for (Iterator it = self.getFormalInputs().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.FormalInput)
      	 //org.openmicroscopy.omero.model.FormalInput
         (new org.openmicroscopy.omero.model.FormalInput()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Module.moduleExecutions)
    // Cleaning java.util.Set::moduleExecutions set (Role: org.openmicroscopy.omero.model.Module.moduleExecutions)
    if (null==self.getModuleExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModuleExecutions())){
      self.setModuleExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set Module.moduleExecutions to null");
         }
    } else {
      for (Iterator it = self.getModuleExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.ModuleExecution)
      	 //org.openmicroscopy.omero.model.ModuleExecution
         (new org.openmicroscopy.omero.model.ModuleExecution()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.Module.analysisChainNodes)
    // Cleaning java.util.Set::analysisChainNodes set (Role: org.openmicroscopy.omero.model.Module.analysisChainNodes)
    if (null==self.getAnalysisChainNodes()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainNodes())){
      self.setAnalysisChainNodes(null);
         if (log.isDebugEnabled()){
             log.debug("Set Module.analysisChainNodes to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainNodes().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainNode)
      	 //org.openmicroscopy.omero.model.AnalysisChainNode
         (new org.openmicroscopy.omero.model.AnalysisChainNode()).getUtils().clean(it.next(),done);
      }
    }
  }


}
