/*
 * Created on Feb 10, 2005
 */
package org.ome.srv.net.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.List;

import org.ome.model.IExperimenter;
import org.ome.model.LSID;
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