/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.db.jena;

import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.FollowGroupService;
import org.ome.interfaces.ServiceFactory;

/**
 * @author josh
 */
public class JenaStoreFactory implements ServiceFactory {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getAdministrationService()
	 */
	public AdministrationService getAdministrationService() {
		return new JenaAdministrationStore();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getGenericService()
	 */
	public GenericService getGenericService() {
		return new JenaGenericStore();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getPredicateGroupService()
	 */
	public FollowGroupService getFollowGroupService() {
		return new JenaFollowGroupStore();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getContainerService()
	 */
	public ContainerService getContainerService() {
		return new JenaContainerStore();
	}

	
	
}
