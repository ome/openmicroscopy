/*
 * org.openmicroscopy.omero.dao.utils.AnalysisChainUtils
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
import org.openmicroscopy.omero.model.AnalysisChain;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class AnalysisChainUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(AnalysisChainUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    AnalysisChain self = (AnalysisChain) o;
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.AnalysisChain.analysisChainExecutions)
    // Cleaning java.util.Set::analysisChainExecutions set (Role: org.openmicroscopy.omero.model.AnalysisChain.analysisChainExecutions)
    if (null==self.getAnalysisChainExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainExecutions())){
      self.setAnalysisChainExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChain.analysisChainExecutions to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainExecution)
      	 //org.openmicroscopy.omero.model.AnalysisChainExecution
         (new org.openmicroscopy.omero.model.AnalysisChainExecution()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.AnalysisChain.analysisChainLinks)
    // Cleaning java.util.Set::analysisChainLinks set (Role: org.openmicroscopy.omero.model.AnalysisChain.analysisChainLinks)
    if (null==self.getAnalysisChainLinks()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainLinks())){
      self.setAnalysisChainLinks(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChain.analysisChainLinks to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainLinks().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainLink)
      	 //org.openmicroscopy.omero.model.AnalysisChainLink
         (new org.openmicroscopy.omero.model.AnalysisChainLink()).getUtils().clean(it.next(),done);
      }
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.AnalysisChain.analysisChainNodes)
    // Cleaning java.util.Set::analysisChainNodes set (Role: org.openmicroscopy.omero.model.AnalysisChain.analysisChainNodes)
    if (null==self.getAnalysisChainNodes()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainNodes())){
      self.setAnalysisChainNodes(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChain.analysisChainNodes to null");
         }
    } else {
      for (Iterator it = self.getAnalysisChainNodes().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisChainNode)
      	 //org.openmicroscopy.omero.model.AnalysisChainNode
         (new org.openmicroscopy.omero.model.AnalysisChainNode()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.Experimenter::experimenter field
    if (null==self.getExperimenter()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getExperimenter())){
      self.setExperimenter(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChain.experimenter to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(self.getExperimenter(),done);
    }
  }


}
