/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import org.ome.model.*;

/**
 * responsible for all user and group adminisistration as well as security
 * privielges and ownership.
 * 
 * @author josh
 */
public interface AdministrationService {

	public String getSessionKey() ;

	public IExperimenter createExperimenter() ; // password
																	  // TODO
	public int createExperimenter(IExperimenter data);

	public IExperimenter getExperimenter(LSID experimenterId);

	public IExperimenter getExperimenter(LSID experimenterId,
			LSID predicateGroupId); // TODO use enums

}