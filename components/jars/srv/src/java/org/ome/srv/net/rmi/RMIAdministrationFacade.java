/*
 * Created on Feb 10, 2005
 */
package org.ome.srv.net.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.List;

import org.ome.LSID;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.logic.ServiceFactoryImpl;

/**
 * @author josh
 */
public class RMIAdministrationFacade extends UnicastRemoteObject implements
		AdministrationService {

	public RMIAdministrationFacade() throws RemoteException {
		super();
System.out.println("RMIAdministrationFacade.RMIAdministrationFacade()");		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ome.srv.RemoteAdministrationService#retrieveProjectsByExperimenter(int)
	 */
	public List retrieveProjectsByExperimenter(LSID experimenterId)
			throws RemoteException {
System.err
				.println("RMIAdministrationFacade.retrieveProjectsByExperimenter()");
System.err.flush();
		ServiceFactory factory = new ServiceFactoryImpl();
		List results = factory.getAdministrationService()
				.retrieveProjectsByExperimenter(experimenterId);
for (Iterator iter = results.iterator(); iter.hasNext();) {
	Object element = (Object) iter.next();
	System.err.println(element.getClass());
	System.err.flush();
}		
		return results;
	}

}