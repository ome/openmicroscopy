/*
 * omeds.tests.DatasetCriteriaFactory
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

package omeds.tests;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;

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
public class DatasetCriteriaFactory
{


	/**
	 * Create the criteria by which the object graph is pulled out.
	 * 
	 * @return 
	 */
	public static Criteria buildImagesCriteria()
	{
		Criteria criteria = new Criteria();
		
		//Specify which fields we want for the images.
		criteria.addWantedField("images");
		criteria.addWantedField("images", "id");
		criteria.addWantedField("images", "name");
		
		return criteria;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * 
	 */
	public static Criteria buildDatasetCriteria(int id)
	{
		Criteria criteria = new Criteria();

		//Specify which fields we want for the dataset.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("description");
		criteria.addWantedField("owner");
		criteria.addWantedField("images"); 		
		
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

		//Specify which fields we want for the images.
		criteria.addWantedField("images", "id");
		criteria.addWantedField("images", "name");
		
		criteria.addFilter("id", new Integer(id));
		
		return criteria;
	}

	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * 
	 */
	public static Criteria buildDatasetToProjectCriteria()
	{
		Criteria criteria = new Criteria();

		//Specify which fields we want for the dataset.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("description");
		criteria.addWantedField("owner");		
		criteria.addWantedField("projects"); 
		
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
	
		//Specify which fields we want for the projects.
		criteria.addWantedField("projects", "id");
		criteria.addWantedField("projects", "name");
		criteria.addWantedField("projects", "description");
		criteria.addWantedField("projects", "owner");
		criteria.addWantedField("projects.owner", "id");
		criteria.addWantedField("projects.owner", "FirstName");
		criteria.addWantedField("projects.owner", "LastName");
		criteria.addWantedField("projects.owner", "Group");
		
		//Specify which fields we want for the owner's group.
		criteria.addWantedField("projects.owner.Group", "id");
		criteria.addWantedField("projects.owner.Group", "Name");
		
		return criteria;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * 
	 */
	public static Criteria buildDatasetImageCriteria()
	{
		Criteria criteria = new Criteria();

		//Specify which fields we want for the dataset.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("description");
		criteria.addWantedField("owner");		
		criteria.addWantedField("images"); 
	
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

		//Specify which fields we want for the images.
		criteria.addWantedField("images", "id");
		criteria.addWantedField("images", "name");
		criteria.addWantedField("images", "description");
		criteria.addWantedField("images", "owner");
		criteria.addWantedField("images.owner", "id");
		criteria.addWantedField("images.owner", "FirstName");
		criteria.addWantedField("images.owner", "LastName");
		criteria.addWantedField("images.owner", "Group");
	
		//Specify which fields we want for the owner's group.
		criteria.addWantedField("images.owner.Group", "id");
		criteria.addWantedField("images.owner.Group", "Name");
	
		return criteria;
	}
	
}
