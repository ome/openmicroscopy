/*
 * Created on Feb 11, 2005
 */
package org.ome.client.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.ome.client.Properties;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.PredicateGroupService;
import org.ome.interfaces.ServiceFactory;

/** provides client side implementations for all services 
 * based on configuration files
 * 
 * @author josh
 */
public class ServiceFactoryImpl implements ServiceFactory {

	/** TODO do we return null or throw?
	 * @return
	 */
	public AdministrationService getAdministrationService()  {
		try {
			AdministrationService as = (AdministrationService) Naming
					.lookup(Properties.getString("RMIServiceFactory.AdminService")); 
			return as;
			
		} catch (MalformedURLException mue) {
			// TODO: handle exception
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
		}
		return null;
	}
	
	/** 
	 * @return
	 */
	public GenericService getGenericService()  {
		try {
			GenericService gs = (GenericService) Naming
					.lookup(Properties.getString("RMIServiceFactory.GenericService")); 
			return gs;
			
		} catch (MalformedURLException mue) {
			// TODO: handle exception
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ServiceFactory#getPredicateGroupService()
	 */
	public PredicateGroupService getPredicateGroupService() {
		try {
			PredicateGroupService pgs = (PredicateGroupService) Naming
					.lookup(Properties.getString("RMIServiceFactory.GenericService")); 
			return pgs;
			
		} catch (MalformedURLException mue) {
			// TODO: handle exception
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
		}
		return null;	}
}
