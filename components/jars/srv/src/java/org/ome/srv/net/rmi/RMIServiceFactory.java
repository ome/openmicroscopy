/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.net.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.AnalysisService;
import org.ome.interfaces.AttributeService;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.FollowGroupService;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.ImageService;
import org.ome.interfaces.ServiceFactory;

/**
 * @author josh
 */
public class RMIServiceFactory extends UnicastRemoteObject 
	implements ServiceFactory {

	RMIAdministrationFacade raf = new RMIAdministrationFacade();
	RMIContainerFacade rcf = new RMIContainerFacade();
	
	public RMIServiceFactory() throws RemoteException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getAdministrationService()
	 */
	public AdministrationService getAdministrationService() {
		return raf;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getContainerService()
	 */
	public ContainerService getContainerService() {
		return rcf;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getGenericService()
	 */
	public GenericService getGenericService() {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getFollowGroupService()
	 */
	public FollowGroupService getFollowGroupService() {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getImageService()
	 */
	public ImageService getImageService() throws RuntimeException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getAttributeService()
	 */
	public AttributeService getAttributeService() throws RuntimeException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getAnalysisService()
	 */
	public AnalysisService getAnalysisService() throws RuntimeException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

}
