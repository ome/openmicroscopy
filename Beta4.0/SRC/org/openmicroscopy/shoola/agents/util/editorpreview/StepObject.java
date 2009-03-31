 /*
 * org.openmicroscopy.shoola.agents.util.editorpreview.StepObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.editorpreview;

//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * The object stores the data for a single 'step' of a protocol
 * This includes a name, and name-value pairs for each parameter. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class StepObject 
{

	/** The name of the step */
	private String 						name;
	
	/** The level of the step in the hierarchy. 
	 * Can be used to indent in display */
	private int 						level;
	
	/** A list of the parameters in this step  */
	private List<Param>					params;
	
	/**
	 * Creates an instance of this class, with a name and level. 
	 * 
	 * @param name		Name of Step
	 * @param level		Level in hierarchy. 
	 */
	StepObject(String name, int level) 
	{
		this.name = name;
		this.level = level;
		
		params = new ArrayList<Param>();
	}
	
	/**
	 * Adds a parameter to the step. 
	 * 
	 * @param name	The parameter name
	 * @param value	The parameter value
	 */
	void addParam(String name, String value) 
	{
		params.add(new Param(name, value));
	}
	
	/**
	 * Returns the name of the Step.
	 * 
	 * @return See above.
	 */
	String getName() {	return name; }
	
	/**
	 * Returns the level in the hierarchy of the Step.
	 * 
	 * @return See above.
	 */
	int getLevel()	{ return level; }
	 
	/**
	 * Returns the list of parameters in this step.
	 * 
	 * @return See above.
	 */
	List<Param> getParams() { return params; }
	
	/**
	 * A simple name-value class to represent the parameter in the list. 
	 */
	class Param {
		
		/** The name to store. */
		private String name;
		
		/** The associated value. */
		private String value;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param name  The name.
		 * @param value The associated value.
		 */
		Param(String name, String value)
		{
			this.name = name;
			this.value = value;
		}
		
		/**
		 * Returns the name.
		 * 
		 * @return See above.
		 */
		String getName() { 	return name; }
		
		/**
		 * Returns the value.
		 * 
		 * @return See above.
		 */
		String getValue() {	  return value; }
	}
	
}
