/*
 * org.openmicroscopy.shoola.env.data.DataManagementService
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;


/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */


public interface DataManagementService
{
	/**
	 * Create, if none provided, two new protos and fill them up
	 * with data retrieved form OMEDS Project objects.
	 * Each project proto object is linked to a list of dataset proto 
	 * objects.
	 * 
	 * @param pProto	project proto.
	 * @param dProto	dataset proto.
	 * @return
	 */
    public List retrieveUserProjects(ProjectSummary pProto, 
    								DatasetSummary dProto);
    								
	/**
	 * Retrieve all user's projects.
	 * Create a list of project summary DataObjects filled up with 
	 * data retrieved from an OMEDS project objects.
	 * Each project summary object is linked to a list of dataset summary 
	 * objects.
	 * 
	 * @return See above.
	 */
    public List retrieveUserProjects();
    
	/**
	 * Create, if none provided, a DataObject and fill it up with
	 * data retrieved from an OMEDS Project object.
	 * 
	 * @param projectID		projectID
	 * @param retVal		DataObject to fill up.
	 * @return
	 */								
    public ProjectData retrieveProject(int projectID, ProjectData retVal);
    
	/**
	 * Create a DataObject and fill it up with
	 * data retrieved from an OMEDS Project object.
	 * 
	 * @param projectID		projectID
	 * @return
	 */
	public ProjectData retrieveProject(int projectID);
	
	/**
	 * Create, if none provided, a DataObject and fill it up with
	 * data retrieved from an OMEDS Dataset object.
	 * 
	 * @param datasetID		datasetID
	 * @param retVal	DataObject to fill up.
	 * @return
	 */
    public DatasetData retrieveDataset(int datasetID, DatasetData retVal);
    
	/**
	 * Create a DataObject and fill it up with
	 * data retrieved from an OMEDS Dataset object.
	 * 
	 * @param datasetID		datasetID
	 * @return
	 */
	public DatasetData retrieveDataset(int datasetID);
	
	/**
	 * Create, if none provided, a DataObject and fill it up with
	 * data retrieved from an OMEDS Image object.
	 * 
	 * @param id		imageID
	 * @param retVal	DataObject to fill up.
	 * @return
	 */
    public ImageData retrieveImage(int id, ImageData retVal);
    
	/**
	* Create a DataObject and fill it up with
	* data retrieved from an OMEDS Image object.
	* 
	* @param id		imageID
	* @return
	*/
	public ImageData retrieveImage(int id);
	
    /**
     * Retrieve all images linked to a given dataset.
     * Creates a list of image summary objects, object filled up with
     * data retrieved from an OMEDS Image object.
     * 
     * @param datasetID		
     * @return list of image summary objects.
     */
    public List retrieveImages(int datasetID);
    
}
