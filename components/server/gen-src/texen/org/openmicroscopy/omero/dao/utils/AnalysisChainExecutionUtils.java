/*
 * org.openmicroscopy.omero.dao.utils.AnalysisChainExecutionUtils
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
import org.openmicroscopy.omero.model.AnalysisChainExecution;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class AnalysisChainExecutionUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(AnalysisChainExecutionUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    AnalysisChainExecution self = (AnalysisChainExecution) o;
    // Cleaning org.openmicroscopy.omero.model.AnalysisChain::analysisChain field
    if (null==self.getAnalysisChain()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChain())){
      self.setAnalysisChain(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainExecution.analysisChain to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.AnalysisChain()).getUtils().clean(self.getAnalysisChain(),done);
    }
	// Type: org.hibernate.type.SetType(org.openmicroscopy.omero.model.AnalysisChainExecution.analysisNodeExecutions)
    // Cleaning java.util.Set::analysisNodeExecutions set (Role: org.openmicroscopy.omero.model.AnalysisChainExecution.analysisNodeExecutions)
    if (null==self.getAnalysisNodeExecutions()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisNodeExecutions())){
      self.setAnalysisNodeExecutions(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainExecution.analysisNodeExecutions to null");
         }
    } else {
      for (Iterator it = self.getAnalysisNodeExecutions().iterator(); it.hasNext();){
      	 //org.hibernate.type.ManyToOneType(org.openmicroscopy.omero.model.AnalysisNodeExecution)
      	 //org.openmicroscopy.omero.model.AnalysisNodeExecution
         (new org.openmicroscopy.omero.model.AnalysisNodeExecution()).getUtils().clean(it.next(),done);
      }
    }
    // Cleaning org.openmicroscopy.omero.model.Dataset::dataset field
    if (null==self.getDataset()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getDataset())){
      self.setDataset(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainExecution.dataset to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Dataset()).getUtils().clean(self.getDataset(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.Experimenter::experimenter field
    if (null==self.getExperimenter()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getExperimenter())){
      self.setExperimenter(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainExecution.experimenter to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.Experimenter()).getUtils().clean(self.getExperimenter(),done);
    }
  }


}
