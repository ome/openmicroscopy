/*
* org.openmicroscopy.shoola.agents.measurement.util.model.WorkFlow
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
*  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.measurement.util.model;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class Workflow
{
	/** The default workflow, i.e. nothing .*/
	public static String DEFAULTWORKFLOW = "Default Workflow";
	
	/** The namespace of the workflow. */
	private String nameSpace;
	
	/** The keywords of the workflow. */
	private List<String> keywords;
	
	/**
	 * Instantiate the class. 
	 * @param nameSpace The namespace of the workflow.
	 * @param keywords The keywords of the workflow.
	 */
	public Workflow(String nameSpace, List<String> keywords)
	{
		this.nameSpace = nameSpace;
		this.keywords = keywords;
	}
	
	/**
	 * Return the namespace of this workflow.
	 * @return See above.
	 */
	public String getNameSpace()
	{
		return this.nameSpace;
	}
	
	/**
	 * Return the keywords of this workflow.
	 * @return See above.
	 */
	public List<String> getKeywords()
	{
		return this.keywords;
	}
	
	/**
	 * Add a new keyword to the workflow. 
 	 * @param keyword See above.
	 */
	public void addKeyword(String keyword)
	{
		this.keywords.add(keyword);
	}
	
	/**
	 * Does the keyword exist in the workflow.
	 * @param value keyword to test for existance.
	 * @return See above.
	 */
	public boolean contains(String value)
	{
		for(String keyword : this.keywords)
		{
			if(value.equals(keyword))
				return true;
		}
		return false;
	}
}
