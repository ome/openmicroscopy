/*
 * Created on Feb 10, 2005
 */
package org.ome.srv.logic;

import org.ome.model.Experimenter;
import org.ome.model.IExperimenter;
import org.ome.model.LSID;
import org.ome.interfaces.AdministrationService;

/**
 * @author josh
 */
public class AdministrationServiceImpl extends AbstractService implements AdministrationService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getExperimenter(org.ome.model.LSID)
	 */
	public IExperimenter getExperimenter(LSID lsid) {
		return new Experimenter(db.getLSObject(lsid));
	}

	//TODO===========================================
	
	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getSessionKey()
	 */
	public String getSessionKey() {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#createExperimenter()
	 */
	public IExperimenter createExperimenter() {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#createExperimenter(org.ome.model.IExperimenter)
	 */
	public int createExperimenter(IExperimenter arg0)  {
		// TODO Auto-generated method stub
		/* return 0; */
		throw new RuntimeException("implement me");
	}


	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getExperimenter(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public IExperimenter getExperimenter(LSID arg0, LSID arg1) {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	
}
