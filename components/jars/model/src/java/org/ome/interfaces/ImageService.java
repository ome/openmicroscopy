/*
 * Created on Feb 10, 2005
 */
package org.ome.interfaces;

import java.rmi.RemoteException;
import java.util.List;

import org.ome.model.IImage;
import org.ome.model.LSID;

/**
 * @author josh
 */
public interface ImageService {

	public List retrieveImagesByExperimenter(LSID experimenterId)
			throws RemoteException;

	public List retrieveImagesByExperimenter(LSID experimenterId,
			LSID predicateGroupId) throws RemoteException;

	public IImage retrieveImage(LSID imageId) throws RemoteException;

	public IImage retrieveImage(LSID imageId, LSID predicateGroupId)
			throws RemoteException;

	// The next could be generated as (?container :contains ?image) //TODO
	// or just generalized "getByContainer()" since Transitive??

	public List retrieveImagesByDataset(LSID datasetId) throws RemoteException;

	public List retrieveImagesByProject(LSID projectId) throws RemoteException;

	public void updateImage(IImage data) throws RemoteException;

	public void setImage(IImage data) throws RemoteException;

}