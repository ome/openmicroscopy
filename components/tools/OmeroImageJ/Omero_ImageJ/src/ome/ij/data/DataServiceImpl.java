/*
 * ome.ij.data.DataServiceImpl 
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
package ome.ij.data;

//Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import omero.model.Pixels;
import omero.sys.ParametersI;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ProjectData;

/** 
 * Implementation of the {@link DataService} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DataServiceImpl 
	implements DataService
{

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private Gateway            gateway;

	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the OMERO entry point.
	 *                  Mustn't be <code>null</code>.
	 */
	DataServiceImpl(Gateway gateway)
	{
		if (gateway == null)
			throw new IllegalArgumentException("No gateway.");
		this.gateway = gateway;
	}

	/**
	 * Implemented as specified by the {@link DataService}.
	 * @see DataService#loadImages(long)
	 */
	public Collection loadImages(long datasetId) 
		throws DSAccessException, DSOutOfServiceException 
	{
		ParametersI p = new ParametersI();
		p.leaves();
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(datasetId);
		return gateway.loadContainerHierarchy(DatasetData.class, ids, p);        
	}

	/**
	 * Implemented as specified by the {@link DataService}.
	 * @see DataService#loadProjects()
	 */
	public Collection loadProjects() 
		throws DSAccessException, DSOutOfServiceException
	{
		ParametersI p = new ParametersI();
		ExperimenterData exp = gateway.getUserDetails();
		p.exp(omero.rtypes.rlong(exp.getId()));
		p.noLeaves();
		p.orphan();
		return gateway.loadContainerHierarchy(ProjectData.class, null, p); 
	}
	
	/**
	 * Implemented as specified by the {@link DataService}.
	 * @see DataService#getCurrentUser()
	 */
	public ExperimenterData getCurrentUser()
	{
		return gateway.getUserDetails();
	}
	
	/**
	 * Implemented as specified by the {@link DataService}.
	 * @see DataService#getImage(long)
	 */
	public ImageObject getImage(long pixelsID)
		throws DSAccessException, DSOutOfServiceException
	{
		Pixels pixels = gateway.getPixels(pixelsID);
		return new ImageObject(pixels);
	}

	/**
	 * Implemented as specified by the {@link DataService}.
	 * @see DataService#getPlane(long, int, int, int)
	 */
	public byte[] getPlane(long pixelsID, int z, int c, int t)
			throws DSAccessException, DSOutOfServiceException 
	{
		return gateway.getPlane(pixelsID, z, c, t);
	}

	/**
	 * Implemented as specified by the {@link DataService}.
	 * @see DataService#exportImageAsOMETiff(File, long)
	 */
	public File exportImageAsOMETiff(File file, long imageID)
			throws DSAccessException, DSOutOfServiceException
	{
		return gateway.exportImageAsOMETiff(file, imageID);
	}
	
}
