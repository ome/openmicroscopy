/*
 * trunk.components.common.src.ome.api.IScript
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.api;

// Java imports
import java.util.Map;
// Third-party libraries

// Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;

/*
 * Developer notes: The two annotations below are activated by setting
 * subversion properties on this class file. These values can then be accessed
 * via ome.system.Version
 */
//@RevisionDate("$Date: 2008-02-12 $")
//@RevisionNumber("$Revision: 1 $")
public interface IScript 
	extends ServiceInterface 
{
	/**
	 * This method returns the scripts on the server as by id and name. 
	 * 
	 * @return see above.
	 * @throws ApiUsageException
	 * @throws SecurityViolation
	 */
	Map getScripts() throws ApiUsageException, SecurityViolation;
	
	/**
	 * Delete the script on the server with id.
	 * @param id Id of the script to delete.
	 * @throws ApiUsageException
	 * @throws SecurityViolation
	 */
	void deleteScript(long id) throws ApiUsageException, SecurityViolation;
	
	/**
	 * Get the id of the script with name, scriptName, the script service
	 * ensures that all script names are unique. 
	 * @param scriptName The name of the script.
	 * @return see above.
	 * @throws ApiUsageException
	 * @throws SecurityViolation
	 */
	long getScriptID(String scriptName) throws ApiUsageException, SecurityViolation;
	
	/**
	 * Upload the script to the server and get the id. This method checks that
	 * a script with that names does not exist and that the script has parameters.
	 * @param script see above.
	 * @return The new id of the script.
	 * @throws ApiUsageException
	 * @throws SecurityViolation
	 */
	long uploadScript(String script) throws ApiUsageException, SecurityViolation;
	
	/**
	 * Get the script from the server with id.
	 * @param id see above.
	 * @return see above.
	 * @throws ApiUsageException
	 */
	String getScript(long id) throws ApiUsageException;

    /**
     * Get the script from the server with details from OriginalFile
     * @param id see above
     * @return see above
     * @throws ApiUsageException
     */
    Map getScriptWithDetails(long id) throws ApiUsageException;
	
	/**
	 * Get the parameters that the script takes. This is a key-value pair map,
	 * the key being the variable name, and the value the type of the variable.
	 * @param id see above.
	 * @return see above.
	 * @throws ApiUsageException
	 */
	Map getParams(long id) throws ApiUsageException;
	
	/**
	 * Run the script on the server with id, and using the parameters, paramMap.
	 * The server checks that all the parameters expected by the script are 
	 * supplied in the paramMap and that their types match. Once executed the 
	 * script then returns a resultMap which is a key-value pair map, the 
	 * key being the result variable name and the value being the value of the
	 * variable.
	 * @param id see above.
	 * @param paramMap see above.
	 * @return see above.
	 * @throws ApiUsageException
	 * @throws SecurityViolation
	 */
	Map runScript(long id, Map paramMap) throws ApiUsageException, SecurityViolation;
}
