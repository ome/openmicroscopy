/*
 * org.openmicroscopy.shoola.env.data.model.DatasetData
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

package org.openmicroscopy.shoola.env.data.model;

import java.util.List;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
public class DatasetData {

	private int 		id;
	private String 		name;
	private String 		description;
	private int 		ownerID;
	private String		ownerFirstName;
	private String		ownerLastName;
	private String		ownerEmail;
	private String		ownerInstitution;
	private int 		ownerGroupID;
	private String		ownerGroupName;
	private List		images;
	
	
	public DatasetData() {}
	
	public DatasetData(int id, String name, String description, int ownerID,
					String ownerFirstName, String ownerLastName, 
					String ownerEmail, String ownerInstitution, 
					int ownerGroupID, String ownerGroupName, List images)
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.ownerID = ownerID;
		//this.ownerOmeName = ownerOmeName;
		this.ownerFirstName = ownerFirstName;
		this.ownerLastName = ownerLastName;
		this.ownerEmail = ownerEmail;
		this.ownerInstitution = ownerInstitution;
		this.ownerGroupID = ownerGroupID;
		this.ownerGroupName = ownerGroupName;	
		this.images = images;
	}

	public List getImages()
	{
		return images;
	}

	public String getDescription()
	{
		return description;
	}

	public int getID()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getOwnerEmail()
	{
		return ownerEmail;
	}

	public int getOwnerGroupID()
	{
		return ownerGroupID;
	}

	public String getOwnerGroupName()
	{
		return ownerGroupName;
	}

	public int getOwnerID()
	{
		return ownerID;
	}

	public String getOwnerInstitution()
	{
		return ownerInstitution;
	}

	public void setDatasets(List images)
	{
		this.images = images;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setOwnerEmail(String ownerEmail)
	{
		this.ownerEmail = ownerEmail;
	}

	public void setOwnerGroupID(int ownerGroupID)
	{
		this.ownerGroupID = ownerGroupID;
	}

	public void setOwnerGroupName(String ownerGroupName)
	{
		this.ownerGroupName = ownerGroupName;
	}

	public void setOwnerID(int ownerID)
	{
		this.ownerID = ownerID;
	}

	public void setOwnerInstitution(String ownerInstitution)
	{
		this.ownerInstitution = ownerInstitution;
	}

	public String getOwnerFirstName()
	{
		return ownerFirstName;
	}

	public String getOwnerLastName()
	{
		return ownerLastName;
	}


	public void setOwnerFirstName(String ownerFirstName)
	{
		this.ownerFirstName = ownerFirstName;
	}

	public void setOwnerLastName(String ownerLastName)
	{
		this.ownerLastName = ownerLastName;
	}

}
