/*
 * org.openmicroscopy.omero.dao.utils.AnalysisChainLinkUtils
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
import org.openmicroscopy.omero.model.AnalysisChainLink;

/**
 *  
 * @author  GENERATED CODE
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */

public class AnalysisChainLinkUtils  extends BaseModelUtils {

  private static Log log = LogFactory.getLog(AnalysisChainLinkUtils.class);


  public void clean(Object o){
    clean(o,new HashSet());
  }

  public void clean(Object o, Set done){

    // Enter each object-indexed clean only once
    if (done.contains(o)){
        return;
    }
    done.add(o);
  
    AnalysisChainLink self = (AnalysisChainLink) o;
    // Cleaning org.openmicroscopy.omero.model.AnalysisChain::analysisChain field
    if (null==self.getAnalysisChain()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChain())){
      self.setAnalysisChain(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainLink.analysisChain to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.AnalysisChain()).getUtils().clean(self.getAnalysisChain(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.AnalysisChainNode::analysisChainNodeByToNode field
    if (null==self.getAnalysisChainNodeByToNode()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainNodeByToNode())){
      self.setAnalysisChainNodeByToNode(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainLink.analysisChainNodeByToNode to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.AnalysisChainNode()).getUtils().clean(self.getAnalysisChainNodeByToNode(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.AnalysisChainNode::analysisChainNodeByFromNode field
    if (null==self.getAnalysisChainNodeByFromNode()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getAnalysisChainNodeByFromNode())){
      self.setAnalysisChainNodeByFromNode(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainLink.analysisChainNodeByFromNode to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.AnalysisChainNode()).getUtils().clean(self.getAnalysisChainNodeByFromNode(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.FormalOutput::formalOutput field
    if (null==self.getFormalOutput()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFormalOutput())){
      self.setFormalOutput(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainLink.formalOutput to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.FormalOutput()).getUtils().clean(self.getFormalOutput(),done);
    }
    // Cleaning org.openmicroscopy.omero.model.FormalInput::formalInput field
    if (null==self.getFormalInput()){
      // Do nothing
    } else if (!Hibernate.isInitialized(self.getFormalInput())){
      self.setFormalInput(null);
         if (log.isDebugEnabled()){
             log.debug("Set AnalysisChainLink.formalInput to null");
         }
    } else {
      (new org.openmicroscopy.omero.model.FormalInput()).getUtils().clean(self.getFormalInput(),done);
    }
  }


}
