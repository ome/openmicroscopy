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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.map.DatasetMapper;
import org.openmicroscopy.shoola.env.data.map.ProjectMapper;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.ui.UserCredentials;
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
	//TODO: write the method in STSAdapter b/c Experimenter is a semantic type
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
    
    /** Retrieve user projects. */
    public List retrieveUserProjects(ProjectSummary pProto, 
    								DatasetSummary dProto)
	{	
		//Make new protos if none was provided.
		if (pProto == null) pProto = new ProjectSummary();
		if (dProto == null) dProto = new DatasetSummary();
		
		//Retrieve the user ID.
		UserCredentials uc = (UserCredentials)
							registry.lookup(LookupNames.USER_CREDENTIALS);

		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = ProjectMapper.buildUserProjectsCriteria(
															uc.getUserID());

		//Load the graph defined by criteria.
		List projects = null;
	  	try {
			projects = (List) proxy.retrieveList(Project.class, criteria);
	  	} catch (Exception e) {
	  		
		// TODO: handle exception by throwing either NotLoggedInException
		//(broken connection, expired session) or ServiceUnavailableExc
		//(temp server failure, temp middleware failure).
	  	}
    	
    	return ProjectMapper.fillUserProjects(projects, pProto, dProto);
	}
	
    public List retrieveUserProjects()
    {
    	return retrieveUserProjects(null, null);
    }
    
    /** Retrieve a project. */
    public ProjectData retrieveProject(int id, ProjectData retVal)
    {
		//Make a new retVal if none was provided.
		if (retVal == null) retVal = new ProjectData();
		
		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = ProjectMapper.buildProjectCriteria();
		
		Project project = null;
		
		//Load the graph defined by criteria.
		try {
			project = (Project) proxy.load(Project.class, id, criteria);
		} catch (Exception e) {
		  // TODO: handle exception by throwing either NotLoggedInException
		  //(broken connection, expired session) or ServiceUnavailableExc
		  //(temp server failure, temp middleware failure).
		}
		//Put the server data into the corresponding client object.
		ProjectMapper.fillProject(project, retVal);
		
    	return retVal;
    }
    
	/** Retrieve a dataset. */
    public DatasetData retrieveDataset(int id, DatasetData retVal)
    {
		//Make a new retVal if none was provided.
		if (retVal == null) retVal = new DatasetData();
	
		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = DatasetMapper.buildDatasetCriteria();
	
		Dataset dataset = null;
	
		//Load the graph defined by criteria.
		try {
			dataset = (Dataset) proxy.load(Dataset.class, id, criteria);
		} catch (Exception e) {
		  // TODO: handle exception by throwing either NotLoggedInException
		  //(broken connection, expired session) or ServiceUnavailableExc
		  //(temp server failure, temp middleware failure).
		}
	
		//Put the server data into the corresponding client object.
		DatasetMapper.fillDataset(dataset, retVal);
    	return retVal;
    }
    

}
