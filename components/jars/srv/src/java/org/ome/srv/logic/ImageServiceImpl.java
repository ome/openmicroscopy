/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.logic;

import java.rmi.RemoteException;

import java.util.List;

import org.ome.interfaces.ImageService;
import org.ome.model.IImage;
import org.ome.model.Image;
import org.ome.model.ImageWrapper;
import org.ome.model.LSID;
import org.ome.srv.db.NamedQuery;
import org.ome.srv.db.queries.ImagesByDatasetQuery;
import org.ome.srv.db.queries.ImagesByExperimenterQuery;
import org.ome.srv.db.queries.ImagesByProjectQuery;


/**
 * @author josh
 */
public class ImageServiceImpl extends AbstractService implements ImageService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImagesByExperimenter(org.ome.model.LSID)
	 */
	public List retrieveImagesByExperimenter(LSID experimenterId) throws RemoteException {
		NamedQuery nq = new ImagesByExperimenterQuery(experimenterId);
		List lsObjects = db.evaluateNamedQuery(nq);
		List domainObjects = ImageWrapper.wrap(lsObjects);
				
		return domainObjects;

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImage(org.ome.model.LSID)
	 */
	public IImage retrieveImage(LSID lsid) throws RemoteException {
		return new Image(db.getLSObject(lsid));
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImagesByProject(org.ome.model.LSID)
	 */
	public List retrieveImagesByProject(LSID projId) throws RemoteException {
		NamedQuery nq = new ImagesByProjectQuery(projId);
		List lsObjects = db.evaluateNamedQuery(nq);
		List domainObjects = ImageWrapper.wrap(lsObjects);
				
		return domainObjects;

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImagesByDataset(org.ome.model.LSID)
	 */
	public List retrieveImagesByDataset(LSID dsId) throws RemoteException {
		NamedQuery nq = new ImagesByDatasetQuery(dsId);
		List lsObjects = db.evaluateNamedQuery(nq);
		List domainObjects = ImageWrapper.wrap(lsObjects);
				
		return domainObjects;

	}
	
	
	//TODO ========================================
	
	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImagesByExperimenter(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public List retrieveImagesByExperimenter(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImage(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public IImage retrieveImage(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#updateImage(org.ome.model.IImage)
	 */
	public void updateImage(IImage arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/*  */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#setImage(org.ome.model.IImage)
	 */
	public void setImage(IImage arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/*  */
		throw new RuntimeException("implement me");
	}

	}
