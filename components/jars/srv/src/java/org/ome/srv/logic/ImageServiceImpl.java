/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.logic;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.ome.cache.Cache;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.ImageService;
import org.ome.model.IImage;
import org.ome.model.ImageWrapper;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.model.ProjectWrapper;

/**
 * @author josh
 */
public class ImageServiceImpl extends AbstractServiceImpl implements ImageService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImagesByExperimenter(org.ome.model.LSID)
	 */
	public List retrieveImagesByExperimenter(LSID experimenterId) throws RemoteException {
		ImageService imageStore = dbFactory.getImageService();
		Cache cache = cacheFactory.getCache();

		List lsObjects = imageStore
				.retrieveImagesByExperimenter(experimenterId);

		for (Iterator iter = lsObjects.iterator(); iter.hasNext();) {
			LSObject obj = (LSObject) iter.next();

		}

		List domainObjects = ImageWrapper.wrap(lsObjects);

		return domainObjects;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImagesByExperimenter(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public List retrieveImagesByExperimenter(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImage(org.ome.model.LSID)
	 */
	public IImage retrieveImage(LSID arg0) throws RemoteException {
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
	 * @see org.ome.interfaces.ImageService#retrieveImagesByDataset(org.ome.model.LSID)
	 */
	public IImage retrieveImagesByDataset(LSID arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ImageService#retrieveImagesByProject(org.ome.model.LSID)
	 */
	public IImage retrieveImagesByProject(LSID arg0) throws RemoteException {
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
