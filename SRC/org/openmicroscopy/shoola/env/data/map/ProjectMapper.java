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
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.Group;
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
	 * Criteria linked to retrieveUserProjects.
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
		return criteria;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria linked to retrieveProject.
	 * 
	 */
	public static Criteria buildProjectCriteria()
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
		
		return criteria;
	}
	
	/** Fill in the project data object. 
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
		// null pointer exception b/c
		//empty.setOwnerInstitution(owner.getInstitution());
		
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
	 * @param projects	list of project graph.
	 * @param pProto	
	 * @param dProto
	 * @return list of project summary objects.
	 */
	public static List fillUserProjects(List projects, ProjectSummary pProto, 
										DatasetSummary dProto)
	{
		//Create the project summary list
		List projectsList = new ArrayList();
		Iterator i = projects.iterator();
		ProjectSummary ps;
		Project p;
		DatasetSummary ds;
		Dataset d;
		
		while (i.hasNext()) {
			p = (Project) i.next();
			ps = (ProjectSummary) pProto.makeNew();
			//Fill in the data coming from Project
			ps.setId(p.getID());
			ps.setName(p.getName());
			Iterator j = p.getDatasets().iterator();
			List datasets = new ArrayList();
			while (j.hasNext()) {
				d = (Dataset) j.next();
				ds = (DatasetSummary) dProto.makeNew();	
				//Fill in the data coming from Dataset.			
				ds.setID(d.getID());
				ds.setName(d.getName());
				datasets.add(ds);
			}
			ps.setDatasets(datasets);
			projectsList.add(ps);
		}
		return projectsList;
	}
	
}
