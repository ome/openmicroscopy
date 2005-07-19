/*
 * org.openmicroscopy.omero.dao.utils.AnalysisChainNodeUtils
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
import org.openmicroscopy.omero.model.AnalysisChainNode;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class AnalysisChainNodeUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(AnalysisChainNodeUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    AnalysisChainNode self = (AnalysisChainNode) o;
    // Cleaning org.openmicroscopy.omero.model.AnalysisChain::analysisChain field
    if (null==self.getAnalysisChain()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChain())){
      self.setAnalysisChain(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainNode.analysisChain to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.AnalysisChain()).getUtils().clean(self.getAnalysisChain(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Module::module field
    if (null==self.getModule()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getModule())){
      self.setModule(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainNode.module to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Module()).getUtils().clean(self.getModule(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.AnalysisChainNode.analysisNodeExecutions)
    // Cleaning java.util.Set::analysisNodeExecutions set (Role: org.openmicroscopy.omero.model.AnalysisChainNode.analysisNodeExecutions)
    if (null==self.getAnalysisNodeExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisNodeExecutions())){
      self.setAnalysisNodeExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainNode.analysisNodeExecutions to null");
         }
    } else {
      for (Iterator it = self.getAnalysisNodeExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisNodeExecution)
      	 //org.openmicroscopy.omero.model.AnalysisNodeExecution
         (new org.openmicroscopy.omero.model.AnalysisNodeExecution()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.AnalysisChainNode.analysisChainLinksByToNode)
    // Cleaning java.util.Set::analysisChainLinksByToNode set (Role: org.openmicroscopy.omero.model.AnalysisChainNode.analysisChainLinksByToNode)
    if (null==self.getAnalysisChainLinksByToNode()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainLinksByToNode())){
      self.setAnalysisChainLinksByToNode(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainNode.analysisChainLinksByToNode to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainLinksByToNode().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainLink)
      	 //org.openmicroscopy.omero.model.AnalysisChainLink
         (new org.openmicroscopy.omero.model.AnalysisChainLink()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.AnalysisChainNode.analysisChainLinksByFromNode)
    // Cleaning java.util.Set::analysisChainLinksByFromNode set (Role: org.openmicroscopy.omero.model.AnalysisChainNode.analysisChainLinksByFromNode)
    if (null==self.getAnalysisChainLinksByFromNode()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainLinksByFromNode())){
      self.setAnalysisChainLinksByFromNode(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainNode.analysisChainLinksByFromNode to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainLinksByFromNode().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainLink)
      	 //org.openmicroscopy.omero.model.AnalysisChainLink
         (new org.openmicroscopy.omero.model.AnalysisChainLink()).getUtils().clean(it.next(),done);
      }
    }
  }


}
