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
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserCredentials;

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
		int userID = 0;
		Experimenter experimenter = null;
		Criteria crit = new Criteria();
		crit.addWantedField("id");
		UserCredentials uc = (UserCredentials)
							registry.lookup(LookupNames.USER_CREDENTIALS);
		crit.addFilter("ome_name", uc.getUserName());
		try {
			experimenter = (Experimenter) 
								proxy.retrieve(Experimenter.class, crit);
			userID = experimenter.getID();	
	 	} catch (Exception e) {
		 // TODO: handle exception by throwing either NotLoggedInException
		 //(broken connection, expired session) or ServiceUnavailableExc
		 //(temp server failure, temp middleware failure).
	 	}

		return userID;
	}
	/* (non-Javadoc)
	 * @see DataManagementService#retrieveProject(int)
	 */
	public Project retrieveProject(int id)
	{
		Project project = null;
		Criteria crit = new Criteria();

		//Specify which fields we want for the project.
		crit.addWantedField("id");
		crit.addWantedField("name");
		crit.addWantedField("description");
		crit.addWantedField("owner");
		crit.addWantedField("datasets"); 

		//Specify which fields we want for the owner.
		crit.addWantedField("owner", "id");
		crit.addWantedField("owner", "FirstName");
		crit.addWantedField("owner", "LastName");
		crit.addWantedField("owner", "Email");
		crit.addWantedField("owner", "Institution");
		crit.addWantedField("owner", "Group");

		//Specify which fields we want for the owner's group.
		crit.addWantedField("owner.Group", "id");
		crit.addWantedField("owner.Group", "Name");

		//Specify which fields we want for the datasets.
		crit.addWantedField("datasets", "id");
		crit.addWantedField("datasets", "name");
		
		//Load the graph defined by crit.
		try {
			project = (Project) proxy.load(Project.class, id, crit);
		} catch (Exception e) {
			// TODO: handle exception by throwing either NotLoggedInException
			//(broken connection, expired session) or ServiceUnavailableExc
			//(temp server failure, temp middleware failure).
		}
		
		return project;
	}

	/* (non-Javadoc)
	 * @see DataManagementService#retrieveDataset(int)
	 */
	public Dataset retrieveDataset(int id)
	{
		Dataset dataset = null;
		Criteria crit = new Criteria();

		//Specify which fields we want for the project.
		crit.addWantedField("id");
		crit.addWantedField("name");
		crit.addWantedField("description");
		crit.addWantedField("owner");
		crit.addWantedField("images"); 

		//Specify which fields we want for the owner.
		crit.addWantedField("owner", "id");
		crit.addWantedField("owner", "FirstName");
		crit.addWantedField("owner", "LastName");
		crit.addWantedField("owner", "Email");
		crit.addWantedField("owner", "Institution");
		crit.addWantedField("owner", "Group");

		//Specify which fields we want for the owner's group.
		crit.addWantedField("owner.Group", "id");
		crit.addWantedField("owner.Group", "Name");

		//Specify which fields we want for the images.
		crit.addWantedField("images", "id");
		crit.addWantedField("images", "name");
	
		//Load the graph defined by crit.
		try {
			dataset = (Dataset) proxy.load(Dataset.class, id, crit);
		} catch (Exception e) {
			// TODO: handle exception by throwing either NotLoggedInException
			//(broken connection, expired session) or ServiceUnavailableExc
			//(temp server failure, temp middleware failure).
		}
	
		return dataset;
	}
	
	/* (non-Javadoc)
	 * @see DataManagementService#retrieveDataset(int)
	 */
	public Image retrieveImage(int id)
	{
		Image image = null;
		Criteria crit = new Criteria();

		//Specify which fields we want for the project.
		crit.addWantedField("id");
		crit.addWantedField("name");
		crit.addWantedField("description");
		crit.addWantedField("owner");
		// not sure we need it
		crit.addWantedField("created"); 
		crit.addWantedField("inserted");
		 
		//Specify which fields we want for the owner.
		crit.addWantedField("owner", "id");
		crit.addWantedField("owner", "FirstName");
		crit.addWantedField("owner", "LastName");
		crit.addWantedField("owner", "Email");
		crit.addWantedField("owner", "Institution");
		crit.addWantedField("owner", "Group");

		//Specify which fields we want for the owner's group.
		crit.addWantedField("owner.Group", "id");
		crit.addWantedField("owner.Group", "Name");

		// grad info on the image.
		//Load the graph defined by crit.
		try {
			image = (Image) proxy.load(Image.class, id, crit);
		} catch (Exception e) {
			// TODO: handle exception by throwing either NotLoggedInException
			//(broken connection, expired session) or ServiceUnavailableExc
			//(temp server failure, temp middleware failure).
		}

		return image;
	}

}
