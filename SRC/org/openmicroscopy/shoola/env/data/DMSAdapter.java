/*
 * org.openmicroscopy.shoola.env.data.DMSProxy
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.shoola.env.data.map.DatasetDataMapper;
import org.openmicroscopy.shoola.env.data.map.ProjectDataMapper;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.config.Registry;

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
class DMSAdapter
	implements DataManagementService
{
	
	private DataFactory		proxy;
	private Registry		registry;
	/**
	 * 
	 */
	DMSAdapter(DataFactory proxy, Registry registry)
	{
		this.proxy = proxy;
		this.registry = registry;
	}

	public int getUserID()
	{
		int userID = 1;
		/*
		Attribute experimenter = null;
		Criteria crit = new Criteria();
		crit.addWantedField("id");
		UserCredentials uc = (UserCredentials)
							registry.lookup(LookupNames.USER_CREDENTIALS);
		crit.addFilter("ome_name", uc.getUserName());
		try {
			System.out.println("step 0: "+userID);
			experimenter = (Attribute) 
								proxy.retrieve(Experimenter.class, crit);
								
			userID = experimenter.getID();	
			System.out.println("step 1: "+userID);
	 	} catch (Exception e) {
		 // TODO: handle exception by throwing either NotLoggedInException
		 //(broken connection, expired session) or ServiceUnavailableExc
		 //(temp server failure, temp middleware failure).
		 //throw new RuntimeException(e);
		 System.out.println(e);
	 	}
	 	*/
	 	 return userID;
	}
    
    public ProjectData retrieveProject(int id, ProjectData retVal)
    {
		//Make a new retVal if none was provided.
		if (retVal == null) retVal = new ProjectData();
		
		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = ProjectDataMapper.buildCriteria();
		
		Project project = null;
		//Load the graph defined by criteria.
		criteria.addFilter("id", new Integer(id));
		try {
			//project = (Project) proxy.load(Project.class, id, criteria);
			project = (Project) proxy.retrieve(Project.class, criteria);
		} catch (Exception e) {
			boolean silly = true;
			
			  // TODO: handle exception by throwing either NotLoggedInException
			  //(broken connection, expired session) or ServiceUnavailableExc
			  //(temp server failure, temp middleware failure).
		}
		
		//Put the server data into the corresponding client object.
		ProjectDataMapper.fill(project, retVal);
		
    	return retVal;
    }
    
    public DatasetData retrieveDataset(int id, DatasetData retVal)
    {
		//Make a new retVal if none was provided.
		if (retVal == null) retVal = new DatasetData();
	
		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = DatasetDataMapper.buildCriteria();
	
		Dataset dataset = null;
	
		//Load the graph defined by criteria.
		try {
			dataset = (Dataset) proxy.load(Dataset.class, id, criteria);
		} catch (Exception e) {
			boolean silly = true;
		
			  // TODO: handle exception by throwing either NotLoggedInException
			  //(broken connection, expired session) or ServiceUnavailableExc
			  //(temp server failure, temp middleware failure).
		}
	
		//Put the server data into the corresponding client object.
		DatasetDataMapper.fill(dataset, retVal);
    	return retVal;
    }
    

}
