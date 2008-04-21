/*
 * util.data.ProjectData 
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
package util.data;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;

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
 * @since OME3.0
 */
public class ProjectData
	extends IObjectData
{	
	/** The Project object that the ProjectData object wraps. */
	private Project 	project;
	
	/**
	 * Create the project data object from the  
	 * @param project
	 */
	public ProjectData(Project project)
	{
		super(project);
		this.project = project;
	}
	
	/**
	 * Rturn the original wrapped project object.
	 * @return see above.
	 */
	public Project getProject()
	{
		return project;
	}
	
	/**
	 * Set the name of the project.
	 * @param name see above.
	 */
	public void setName(String name)
	{
		project.name = new RString(name);
	}
	
	/**
	 * Get the name of the project.
	 * @return see above.
	 */
	public String getName()
	{
		return project.name.val;
	}
	
	/**
	 * Get the project annotation links.
	 * @return see above.
	 */
	public List<ProjectAnnotationLink> getAnnotationLinks()
	{
		return project.annotationLinks;
	}
	
	/**
	 * Get the number of annotation links.
	 * @return see above.
	 */
	public int getAnnotationLinksCount()
	{
		return project.annotationLinks.size();
	}
	
	/**
	 * Get the number of annotationLinks for owner (id)
	 * @param owner the id of the user.
	 * @return number of links.
	 */
	public long getAnnotationLinksCount(long owner)
	{
		return project.annotationLinksCountPerOwner.get(new Long(owner));
	}
	
	/** 
	 * Are the annotation links loaded. 
	 * @return see above.
	 */
	public boolean annotationLinksLoaded()
	{
		return project.annotationLinksLoaded;
	}
	
}


