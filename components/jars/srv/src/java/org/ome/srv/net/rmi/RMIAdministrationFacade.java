/*
 * Created on Feb 10, 2005
 */
package org.ome.srv.net.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.ome.model.IExperimenter;
import org.ome.model.LSID;
import org.ome.interfaces.AdministrationService;

/**
 * @author josh
 */
public class RMIAdministrationFacade extends UnicastRemoteObject implements
		AdministrationService {

	public RMIAdministrationFacade() throws RemoteException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getSessionKey()
	 */
	public String getSessionKey() throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#createExperimenter()
	 */
	public IExperimenter createExperimenter() throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#createExperimenter(org.ome.model.IExperimenter)
	 */
	public int createExperimenter(IExperimenter arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return 0; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getExperimenter(org.ome.model.LSID)
	 */
	public IExperimenter getExperimenter(LSID arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getExperimenter(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public IExperimenter getExperimenter(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}


}