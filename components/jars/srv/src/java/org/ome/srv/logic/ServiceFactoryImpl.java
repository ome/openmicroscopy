/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.logic;

import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.AnalysisService;
import org.ome.interfaces.AttributeService;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.FollowGroupService;
import org.ome.interfaces.ImageService;
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

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getContainerService()
	 */
	public ContainerService getContainerService() {
		return new ContainerServiceImpl();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getImageService()
	 */
	public ImageService getImageService() throws RuntimeException {
		return new ImageServiceImpl();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getAttributeService()
	 */
	public AttributeService getAttributeService() throws RuntimeException {
		return new AttributeServiceImpl();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getAnalysisService()
	 */
	public AnalysisService getAnalysisService() throws RuntimeException {
		return new AnalysisServiceImpl();
	}

	
	
}
