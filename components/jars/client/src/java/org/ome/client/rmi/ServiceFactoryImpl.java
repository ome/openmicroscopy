/*
 * Created on Feb 11, 2005
 */
package org.ome.client.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.AnalysisService;
import org.ome.interfaces.AttributeService;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.FollowGroupService;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.ImageService;
import org.ome.interfaces.ServiceFactory;

/** provides client side implementations for all services 
 * based on configuration files
 * 
 * @author josh
 */
public class ServiceFactoryImpl implements ServiceFactory {

	ServiceFactory factory;

	public ServiceFactoryImpl(){
		try {
			factory = (ServiceFactory) Naming
					.lookup("//localhost/ServiceFactory"); 
			
		} catch (MalformedURLException mue) {
			throw new RuntimeException(mue);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		} catch (NotBoundException e) {
			throw new RuntimeException(e);
		}
 
	}
	
	/** TODO do we return null or throw?
	 * @return
	 */
	public AdministrationService getAdministrationService()  {
		try {
			return factory.getAdministrationService();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** 
	 * @return
	 */
	public GenericService getGenericService()  {
		try {
			return factory.getGenericService();
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getPredicateGroupService()
	 */
	public FollowGroupService getFollowGroupService() {
		try {
			return factory.getFollowGroupService();
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getContainerService()
	 */
	public ContainerService getContainerService() throws RemoteException {
		try {
			return factory.getContainerService();
		} catch (Exception e){
			throw new RuntimeException(e);
		}	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getImageService()
	 */
	public ImageService getImageService() throws RuntimeException {
		try {
			return factory.getImageService();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
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
