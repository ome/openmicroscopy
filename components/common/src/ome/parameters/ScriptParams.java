/*
 * ome.parameters.ScriptParams 
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
package ome.parameters;

//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
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
public class ScriptParams {
	
	/** The name of the script.*/
	private String name;
	
	/** The description of the function of the script. */
	private String description; 
	
	/** The map of the scripts params against their type. */
	private Map<String, String> paramMap;

	/** The map of the scripts return types against their type. */
	private Map<String, String> ReturnMap;

	/**
	 * Instantiate the scriptParams object.
	 *
	 */
	public ScriptParams()
	{
		name = null;
		description=null;
		paramMap = null;
		ReturnMap= null;
	}
	
	/**
	 * Set the name of the script.
	 * @param str see above.
	 */
	public void setScriptName(String str)
	{
		name = str;
	}
	
	/**
	 * Get the name of the script.
	 * @return see above.
	 */
	public String getScriptName()
	{
		return name;
	}

	/** 
	 * Set the description of the script--commonly the details of the scripts 
	 * function.
	 * @param str see above.
	 */
	public void setScriptDescription(String str)
	{
		description = str;
	}

	/** 
	 * Get the description of the script--commonly the details of the scripts 
	 * function.
	 * @return see above.
	 */
	public String getScriptDescription()
	{
		return description;
	}
	
	/**
	 * Set the param map of the script, the map of names to type.
	 * @param map see above.
	 */
	public void setParamMap(Map<String, String> map)
	{
		paramMap = map; 
	}
	
	/**
	 * Get the param map of the script, the map of names to type.
	 * return see above.
	 */
	public Map<String, String> getParamMap()
	{
		return paramMap; 
	}
	
	/**
	 * Set the return map of the script, the map of names to type.
	 * @param map see above.
	 */
	public void setReturnMap(Map<String, String> map)
	{
		ReturnMap = map; 
	}
	
	/**
	 * Get the param map of the script, the map of names to type.
	 * @return see above.
	 */
	public Map<String, String> getReturnMap()
	{
		return ReturnMap; 
	}
	
}

