/*
 * Created on Feb 10, 2005
 */
package org.ome.interfaces;

import java.util.List;

import org.ome.model.IImage;
import org.ome.model.LSID;

/**
 * @author josh
 */
public interface ImageService {

	public List retrieveImagesByExperimenter(LSID experimenterId);

	public List retrieveImagesByExperimenter(LSID experimenterId,
			LSID predicateGroupId) ;

	public IImage retrieveImage(LSID imageId) ;

	public IImage retrieveImage(LSID imageId, LSID predicateGroupId);

	// The next could be generated as (?container :contains ?image) //TODO
	// or just generalized "getByContainer()" since Transitive??

	public List retrieveImagesByDataset(LSID datasetId) ;

	public List retrieveImagesByProject(LSID projectId) ;

	public void updateImage(IImage data) ;

	public void setImage(IImage data) ;

}