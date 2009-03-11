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
import static omero.rtypes.rbool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.services.blitz.gateway.services.DataService;
import ome.services.blitz.gateway.services.GatewayFactory;
import ome.services.blitz.gateway.services.util.ServiceUtilities;
import omero.RType;
import omero.ServerError;
import omero.api.ContainerClass;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.api.IScriptPrx;
import omero.api.ITypesPrx;
import omero.api.IUpdatePrx;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Project;
import omero.sys.Parameters;
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
	GatewayFactory 				gatewayFactory;
	
	/**
	 * Instantiate the imageService with the serviceFactory.
	 * @param serviceFactory see above.
	 */
	public DataServiceImpl(GatewayFactory gatewayFactory)
	{
		this.gatewayFactory = gatewayFactory;
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
		if(getLeaves)
			p.leaves();
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
		HashMap<String, RType> map = new HashMap<String, RType>();
		return 
			iContainerService.getImages(convertContainer(nodeType), nodeIds, new ParametersI(map));
	}

	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getPixelType(java.lang.String)
	 */
	public PixelsType getPixelType(String type) throws ServerError
	{
		IQueryPrx iQuery = gatewayFactory.getIQuery();
		return (PixelsType)iQuery.findByString("PixelsType", "value",
			type);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getPixelTypes()
	 */
	public List<PixelsType> getPixelTypes() throws ServerError
	{
		ITypesPrx iTypes = gatewayFactory.getITypes();
		List<IObject> list = iTypes.allEnumerations("PixelsType"); 
		return ServiceUtilities.collectionCast(PixelsType.class, list);
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
		if(getLeaves)
			p.leaves();
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

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#deleteScript(long)
	 */
	public void deleteScript(long id) throws ServerError 
	{
		IScriptPrx iScript = gatewayFactory.getIScript();
		iScript.deleteScript(id);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getParams(long)
	 */
	public Map<String, RType> getParams(long id) throws ServerError 
	{
		IScriptPrx iScript = gatewayFactory.getIScript();
		return iScript.getParams(id);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getScript(long)
	 */
	public String getScript(long id) throws ServerError 
	{
		IScriptPrx iScript = gatewayFactory.getIScript();
		return iScript.getScript(id);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getScriptID(String)
	 */
	public long getScriptID(String name) throws ServerError 
	{
		IScriptPrx iScript = gatewayFactory.getIScript();
		return iScript.getScriptID(name);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getScripts()
	 */
	public Map<Long, String> getScripts() throws ServerError 
	{
		IScriptPrx iScript = gatewayFactory.getIScript();
		return iScript.getScripts();
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#runScript(long, Map<String, RType>)
	 */
	public Map<String, RType> runScript(long id, Map<String, RType> map)
			throws ServerError 
	{
		IScriptPrx iScript = gatewayFactory.getIScript();
		return iScript.runScript(id, map);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#uploadScript(String)
	 */
	public long uploadScript(String script) throws ServerError 
	{
		IScriptPrx iScript = gatewayFactory.getIScript();
		return iScript.uploadScript(script);
	}
	
}


