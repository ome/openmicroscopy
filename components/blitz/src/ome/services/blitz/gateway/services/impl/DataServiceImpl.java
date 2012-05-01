/*
 * ome.services.blitz.omerogateway.services.impl.DataServiceImpl 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package ome.services.blitz.gateway.services.impl;


//Java imports
import java.util.List;

import ome.services.blitz.gateway.services.DataService;
import ome.services.blitz.gateway.services.GatewayFactory;
import ome.services.blitz.gateway.services.util.ServiceUtilities;
import omero.ServerError;
import omero.api.ContainerClass;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.api.ITypesPrx;
import omero.api.IUpdatePrx;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelType;
import omero.model.Project;
import omero.sys.ParametersI;

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
public class DataServiceImpl
	implements DataService
{
	
	long ownUserId;

	GatewayFactory 				gatewayFactory;
	
	/**
	 * Instantiate the imageService with the serviceFactory.
	 * @param serviceFactory see above.
	 */
	public DataServiceImpl(GatewayFactory gatewayFactory)
	{
		this.gatewayFactory = gatewayFactory;
                try
                {
                    ownUserId = gatewayFactory.getAdminService().getEventContext().userId;
                } catch (ServerError se) {
                    throw new RuntimeException(se);
                }
	}
	
	/**
	 * Converts the specified POJO into the corresponding model.
	 *  
	 * @param nodeType The POJO class.
	 * @return The corresponding class.
	 */
	private String convertContainer(ContainerClass nodeType)
	{
		return nodeType.name();
	}
	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#attachImageToDataset(omero.model.Dataset, omero.model.Image)
	 */
	public void attachImageToDataset(Dataset dataset, Image image)
			throws ServerError
	{
		IUpdatePrx iUpdate = gatewayFactory.getIUpdate();
		DatasetImageLink link = new DatasetImageLinkI();
		link.setParent( dataset );
		link.setChild( image );
		dataset.addDatasetImageLinkToBoth(link, true);
		iUpdate.saveObject(link);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#deleteObject(omero.model.IObject)
	 */
	public void deleteObject(IObject row) throws ServerError
	{
		IUpdatePrx iUpdate = gatewayFactory.getIUpdate();
		iUpdate.deleteObject(row);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#findAllByQuery(java.lang.String)
	 */
	public List<IObject> findAllByQuery(String myQuery) throws ServerError
	{
		IQueryPrx iQuery = gatewayFactory.getIQuery();
		return iQuery.findAllByQuery(myQuery, null);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#findByQuery(java.lang.String)
	 */
	public IObject findByQuery(String myQuery) throws ServerError
	{
		IQueryPrx iQuery = gatewayFactory.getIQuery();
		return iQuery.findByQuery(myQuery, null);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getDatasets(java.util.List, boolean)
	 */
	public List<Dataset> getDatasets(List<Long> ids, boolean getLeaves)
			throws ServerError
	{
		IContainerPrx iContainerService = gatewayFactory.getIContainer();
		ParametersI p = new ParametersI();
		if (getLeaves)
			p.leaves();
		p.exp(omero.rtypes.rlong(ownUserId));
		return ServiceUtilities.collectionCast(Dataset.class, 
			iContainerService.loadContainerHierarchy(
				convertContainer(ContainerClass.Dataset), ids, p));
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getImageByName(java.lang.String)
	 */
	public List<Image> getImageByName(String imageName) throws ServerError
	{
		String datasetQuery = "select i from Image i left outer join fetch i.datasetLinks" +  
        " dil left outer join fetch dil.parent d where i.name like '%"+ 
        imageName+ "%' order by i.name";	
		List imageList = findAllByQuery(datasetQuery);
		return imageList;
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getImageFromDatasetByName(java.lang.Long, java.lang.String)
	 */
	public List<Image> getImageFromDatasetByName(Long datasetId,
			String imageName) throws ServerError
	{
		String datasetQuery = "select i from Image i left outer join fetch i.datasetLinks" +  
        " dil left outer join fetch dil.parent d where d.id=" + datasetId +
        " and  i.name like '%"+ imageName+ "%' order by i.name";	
		List imageList = findAllByQuery(datasetQuery);
		return imageList;
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getImages(omero.api.ContainerClass, java.util.List)
	 */
	public List<Image> getImages(ContainerClass nodeType, List<Long> nodeIds)
			throws ServerError
	{
		IContainerPrx iContainerService = gatewayFactory.getIContainer();
		ParametersI p = new ParametersI();
	    p.exp(omero.rtypes.rlong(ownUserId));
		return iContainerService.getImages(convertContainer(nodeType), 
					nodeIds, p);
	}

	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getPixelType(java.lang.String)
	 */
	public PixelType getPixelType(String type) throws ServerError
	{
		IQueryPrx iQuery = gatewayFactory.getIQuery();
		return (PixelType)iQuery.findByString("PixelType", "value",
			type);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getPixelTypes()
	 */
	public List<PixelType> getPixelTypes() throws ServerError
	{
		ITypesPrx iTypes = gatewayFactory.getITypes();
		List<IObject> list = iTypes.allEnumerations("PixelType"); 
		return ServiceUtilities.collectionCast(PixelType.class, list);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getPixelsFromImage(long)
	 */
	public List<Pixels> getPixelsFromImage(long imageId) throws ServerError
	{
		IQueryPrx iQuery = gatewayFactory.getIQuery();
		String queryStr = new String("select p from Pixels as p left outer " +
			"join fetch p.pixelsType as pt " +
			"where p.image = " + imageId + " order by relatedto");
		return ServiceUtilities.collectionCast(Pixels.class, 
			iQuery.findAllByQuery(queryStr, null));
	}
	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getProjects(java.util.List, boolean)
	 */
	public List<Project> getProjects(List<Long> ids, boolean getLeaves)
			throws ServerError
	{
		IContainerPrx iContainerService = gatewayFactory.getIContainer();
		ParametersI p = new ParametersI();
		if (getLeaves) p.leaves();
		else p.noLeaves();
		p.exp(omero.rtypes.rlong(ownUserId));
		return ServiceUtilities.collectionCast(Project.class, 
			iContainerService.loadContainerHierarchy(
				convertContainer(ContainerClass.Project), ids, p));
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#saveAndReturnArray(java.util.List)
	 */
	public <T extends IObject> List<T> saveAndReturnArray(List<IObject> graph)
			throws ServerError
	{
		IUpdatePrx iUpdate = gatewayFactory.getIUpdate();
	    List rv = iUpdate.saveAndReturnArray(graph); 
		return rv;
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#saveAndReturnObject(omero.model.IObject)
	 */
	public IObject saveAndReturnObject(IObject obj) throws ServerError
	{
		IUpdatePrx iUpdate = gatewayFactory.getIUpdate();
		return iUpdate.saveAndReturnObject(obj);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#saveArray(java.util.List)
	 */
	public void saveArray(List<IObject> graph) throws ServerError
	{
		IUpdatePrx iUpdate = gatewayFactory.getIUpdate();
		iUpdate.saveArray(graph);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#saveObject(omero.model.IObject)
	 */
	public void saveObject(IObject obj) throws ServerError
	{
		IUpdatePrx iUpdate = gatewayFactory.getIUpdate();
		iUpdate.saveObject(obj);
	}	
	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#updatePixels(Pixels)
	 */
	public Pixels updatePixels(Pixels object) 
	throws omero.ServerError
	{
		IUpdatePrx iUpdate = gatewayFactory.getIUpdate();
		return (Pixels)iUpdate.saveAndReturnObject(object);
	}

}


