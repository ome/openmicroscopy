/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.logic;

import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.FollowGroupService;
import org.ome.interfaces.ServiceFactory;

/**
 * @author josh
 */
public class ServiceFactoryImpl implements ServiceFactory {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getAdministrationService()
	 */
	public AdministrationService getAdministrationService() {
		return new AdministrationServiceImpl();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getGenericService()
	 */
	public GenericService getGenericService() {
		return new GenericServiceImpl();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getPredicateGroupService()
	 */
	public FollowGroupService getFollowGroupService() {
		return new FollowGroupServiceImpl();
	}

	
	
}
