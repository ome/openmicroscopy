/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.db.jena;

import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.PredicateGroupService;
import org.ome.interfaces.ServiceFactory;

/**
 * @author josh
 */
public class JenaServiceFactoryImpl implements ServiceFactory {

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
		return new JenaGenericService();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getPredicateGroupService()
	 */
	public PredicateGroupService getPredicateGroupService() {
		return new JenaPredicateGroupService();
	}

	
	
}
