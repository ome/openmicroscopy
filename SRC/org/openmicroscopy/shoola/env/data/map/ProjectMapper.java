/*
 * org.openmicroscopy.shoola.env.data.map.ProjectMapper
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

package org.openmicroscopy.shoola.env.data.map;



//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.Group;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

/** 
 * 
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
public class ProjectMapper
{
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for updateProject.
	 * 
	 * @param projectID	specified project to retrieve.
	 */
	public static Criteria buildUpdateCriteria(int projectID)
	{
		Criteria c = new Criteria();
		c.addWantedField("id");
		c.addWantedField("name");
		c.addWantedField("description");
		c.addFilter("id", new Integer(projectID));
		return c;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveUserProjects.
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildUserProjectsCriteria(int userID)
	{
		Criteria criteria = new Criteria();
		
		//Specify which fields we want for the project.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("datasets");
		
		//Specify which fields we want for the datasets.
		criteria.addWantedField("datasets", "id");
		criteria.addWantedField("datasets", "name");
		
		//Retrieve the user's projects.
		criteria.addFilter("owner_id", new Integer(userID));
		
		criteria.addOrderBy("name");
		
		return criteria;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveProject.
	 * 
	 */
	public static Criteria buildProjectCriteria(int id)
	{
		Criteria criteria = new Criteria();

		//Specify which fields we want for the project.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("description");
		criteria.addWantedField("owner");
		criteria.addWantedField("datasets"); 

		//Specify which fields we want for the owner.
		criteria.addWantedField("owner", "id");
		criteria.addWantedField("owner", "FirstName");
		criteria.addWantedField("owner", "LastName");
		criteria.addWantedField("owner", "Email");
		criteria.addWantedField("owner", "Institution");
		criteria.addWantedField("owner", "Group");

		//Specify which fields we want for the owner's group.
		criteria.addWantedField("owner.Group", "id");
		criteria.addWantedField("owner.Group", "Name");

		//Specify which fields we want for the datasets.
		criteria.addWantedField("datasets", "id");
		criteria.addWantedField("datasets", "name");
		
		criteria.addFilter("id", new Integer(id));
		
		return criteria;
	}
	
	/** 
	 * Fill in the project data object. 
	 * 
	 * @param project	project graph.
	 * @param empty		project data to fill in.
	 * 
	 */
	public static void fillProject(Project project, ProjectData empty)
	{
		
		//Fill in the data coming from Project.
		empty.setID(project.getID());
		empty.setName(project.getName());
		empty.setDescription(project.getDescription());
				
		//Fill in the data coming from Experimenter.
		Experimenter owner = project.getOwner();
		empty.setOwnerID(owner.getID());
		empty.setOwnerFirstName(owner.getFirstName());
		empty.setOwnerLastName(owner.getLastName());
		empty.setOwnerEmail(owner.getEmail());
		empty.setOwnerInstitution(owner.getInstitution());
		
		//Fill in the data coming from Group.
		Group group = owner.getGroup();
		empty.setOwnerGroupID(group.getID());
		empty.setOwnerGroupName(group.getName());
		
		//Create the dataset summary list.
		List datasets = new ArrayList();
		Iterator i = project.getDatasets().iterator();
		Dataset d;
		while (i.hasNext()) {
			d = (Dataset) i.next();
			datasets.add(new DatasetSummary(d.getID(), d.getName()));
		}
		empty.setDatasets(datasets);			
	}

	
	/**
	 * Create list of project summary objects.
	 * 
	 * @param projects	OMEDS.
	 * @param pProto	
	 * @param dProto
	 * @return 
	 */
	public static List fillUserProjects(List projects, ProjectSummary pProto, 
										DatasetSummary dProto)
	{
		Map	datasetsMap = new HashMap();
		List projectsList = new ArrayList();  //The returned summary list.
		Iterator i = projects.iterator();
		ProjectSummary ps;
		Project p;
		DatasetSummary ds;
		Dataset d;
		Iterator j;
		List datasets;
		//For each p in projects...
		while (i.hasNext()) {
			p = (Project) i.next();
			
			//Make a new DataObject and fill it up.
			ps = (ProjectSummary) pProto.makeNew();
			ps.setID(p.getID());
			ps.setName(p.getName());

			j = p.getDatasets().iterator();
			datasets = new ArrayList();
			while (j.hasNext()) {
				d = (Dataset) j.next();
				int id = d.getID();
				ds = (DatasetSummary) datasetsMap.get(new Integer(id));
				if (ds == null) {
					//Make a new DataObject and fill it up.
					ds = (DatasetSummary) dProto.makeNew();		
					ds.setID(id);
					ds.setName(d.getName());
					datasetsMap.put(new Integer(id), ds);
				}  //else we have already created this object.
				
				//Add the dataset to this project's list.
				datasets.add(ds);	
			}
			
			//Link the datasets to this project.
			ps.setDatasets(datasets);
			
			//Add the project to the list of returned projects.
			projectsList.add(ps);
		}
		
		return projectsList;
	}

	/**
	 * Create list of project summary objects.
	 * 
	 * @param projects	OMEDS.
	 * @param pProto	
	 * @param dProto
	 * @return 
	 */
	public static List fillUserProjectsWithDatasetData(List projects, 
			ProjectSummary pProto, DatasetData dProto)
	{
		Map	datasetsMap = new HashMap();
		List projectsList = new ArrayList();  //The returned summary list.
		Iterator i = projects.iterator();
		ProjectSummary ps;
		Project p;
		DatasetData ds;
		Dataset d;
		Iterator j;
		List datasets;
		//For each p in projects...
		while (i.hasNext()) {
			p = (Project) i.next();
			
			//Make a new DataObject and fill it up.
			ps = (ProjectSummary) pProto.makeNew();
			ps.setID(p.getID());
			ps.setName(p.getName());

			j = p.getDatasets().iterator();
			datasets = new ArrayList();
			while (j.hasNext()) {
				d = (Dataset) j.next();
				int id = d.getID();
				ds = (DatasetData) datasetsMap.get(new Integer(id));
				if (ds == null) {
					//Make a new DataObject and fill it up.
					ds = (DatasetData) dProto.makeNew();		
					ds.setID(id);
					ds.setName(d.getName());
					datasetsMap.put(new Integer(id), ds);
				}  //else we have already created this object.
				
				//Add the dataset to this project's list.
				datasets.add(ds);	
			}
			
			//Link the datasets to this project.
			ps.setDatasets(datasets);
			
			//Add the project to the list of returned projects.
			projectsList.add(ps);
		}
		
		return projectsList;
	}
	/** Fill in a project data object.*/
	public static List fillNewProject(Project p, List datasets, 
										ProjectSummary pProto)
	{
		List ids = new ArrayList();
		if (datasets != null) {	//To be on the save side
			Iterator i = datasets.iterator();
			while (i.hasNext())
				ids.add(new Integer(((DatasetSummary) i.next()).getID()));
		}
		pProto.setID(p.getID());
		pProto.setName(p.getName());
		pProto.setDatasets(datasets);
		return ids;
	}
	
}
