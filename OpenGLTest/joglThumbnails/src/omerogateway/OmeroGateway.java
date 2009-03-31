/*
* omerogateway.OmeroGateway
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
* 	This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
*------------------------------------------------------------------------------
*/
package omerogateway;


//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import omero.RInt;
import omero.RType;
import omero.ServerError;
import omero.client;
import omero.api.IContainerPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ThumbnailStorePrx;
import omero.api.ThumbnailStorePrxHelper;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsI;

//Application-internal dependencies

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class OmeroGateway 
{
	client 					client;
	ServiceFactoryPrx 		session;
	ThumbnailStorePrx		thumbnailService;
	IContainerPrx			containerService;
	
	public OmeroGateway(String host, String user, String password) 
	throws CannotCreateSessionException, PermissionDeniedException, ServerError
	{
		client = new client(host);
		session = client.createSession(user, password);
	}
	
	public Map<Long, byte[]> getThumbnailsForDataset(int width, List<Long> ids) 
		throws ServerError
	{
		if(thumbnailService==null)
			thumbnailService = session.createThumbnailStore();
		
		return thumbnailService.getThumbnailByLongestSideSet(omero.rtypes.rint(width), ids);
	}
	
	public Dataset getDataset(long id) throws ServerError
	{
		containerService = session.getContainerService();
		List<Long> idList = new ArrayList<Long>();
		idList.add(id);
		Map<String,RType> optionsMap = new HashMap<String, RType>();
		List<IObject>datasetList =  containerService.loadContainerHierarchy(Dataset.class.getName(), idList, optionsMap);
		if(datasetList.size()!=1)
			return null;
		return (Dataset)datasetList.get(0);
	}
	
		
	/**
	 * Get the pixels from the Dataset, the pixels must be loaded in the Dataset
	 * beforehand.
	 * This method makes no calls to the server.
	 * @param dataset see above.
	 * @return see above.
	 */
	public List<Long> pixelsIdList(DatasetI dataset)
			throws ServerError
	{
		List<Image> images = getImagesFromDataset(dataset);
		return getPixelsIdFromImageList(images);
	}
	
	/**
	 * Get the images from the Dataset, the Dataset must be loaded in the project
	 * beforehand.
	 * This method makes no calls to the server.
	 * @param dataset see above.
	 * @return see above.
	 */
	public List<Image> getImagesFromDataset(Dataset dataset) throws ServerError
	{
		List<Image> images = new ArrayList<Image>();
		Iterator<DatasetImageLink> iterator = ((DatasetI)dataset).iterateImageLinks(); 
		while(iterator.hasNext())
		    images.add(iterator.next().getChild());
		return images;
	}

	/**
	 * Get the pixels from the list of images, these images must have the pixels
	 * loaded beforehand.
	 * This method makes no calls to the server.
	 * @param images see above.
	 * @return see above.
	 */
	public List<Pixels> getPixelsFromImageList(List<Image> images)
	{
		List<Pixels> pixelsList = new ArrayList<Pixels>();
		for(Image image : images)
			for(Pixels pixels : image.copyPixels())
				pixelsList.add(pixels);
		return pixelsList;
	}
	
	/**
	 * Get the pixels if from the list of images, these images must have the pixels
	 * loaded beforehand.
	 * This method makes no calls to the server.
	 * @param images see above.
	 * @return see above.
	 */
	public List<Long> getPixelsIdFromImageList(List<Image> images)
	{
		List<Long> pixelsIdList = new ArrayList<Long>();
		for(Image image : images)
			for(Pixels pixels : image.copyPixels())
				pixelsIdList.add(pixels.getId().getValue());
		return pixelsIdList;
	}
	
	/** 
	 * Helper method for the conversion of base types in containers(normally 
	 * of type IObject) to a concrete type.  
	 * @param <T> new type.
	 * @param klass new type class.
	 * @param list container.
	 * @return see above.
	 */
	public <T extends IObject> List<T> 
    collectionCast(Class<T> klass, List<IObject> list)
    {
        List<T> newList = new ArrayList<T>(list.size());
        for (IObject o : list)
            newList.add((T) o);
        return newList;
    }
	
}

