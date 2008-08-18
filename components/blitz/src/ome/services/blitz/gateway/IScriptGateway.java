/*
 * blitzgateway.service.gateway.IScriptGateway 
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
package ome.services.blitz.gateway;


//Java imports
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.gateways.DSAccessException;
import omero.gateways.DSOutOfServiceException;

import omero.RType;

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
public interface IScriptGateway
{	
	/**
	 * Get the scripts from the iScript Service.
	 * @return all the available scripts, mapped by id, name
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<Long, String> getScripts() throws   DSOutOfServiceException, 
											DSAccessException;
	
	/**
	 * Get the id of the script with name 
	 * @param name name of the script.
	 * @return the id of the script.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	long getScriptID(String name) throws DSOutOfServiceException, 
										 DSAccessException;
	
	/**
	 * Upload the script to the server.
	 * @param script script to upload
	 * @return id of the new script.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	long uploadScript(String script) throws DSOutOfServiceException, 
											DSAccessException;
	
	/**
	 * Get the script with id, this returns the actual script as a string.
	 * @param id id of the script to retrieve.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	String getScript(long id) throws DSOutOfServiceException, 
									 DSAccessException;
	
	/**
	 * Get the params the script takes, this is the name and type. 
	 * @param id id of the script.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<String, RType> getParams(long id) throws DSOutOfServiceException, 
												 DSAccessException;
	
	/**
	 * Run the script and get the results returned as a name , value map.
	 * @param id id of the script to run.
	 * @param map the map of params, values for inputs.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<String, RType> runScript(long id, Map<String, RType> map) 
						throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Delete the script from the server.
	 * @param id id of the script to delete.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void deleteScript(long id) throws 	DSOutOfServiceException, 
										DSAccessException;
	
	/*
	 * 
	 * IScriptService java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the IScript service. 
	 * As the are created in the IScriptGateway they will be marked as 
	 * done.
	 * 
	 *
	
DONE	Map<Long, String> getScripts() throws  DSOutOfServiceException, DSAccessException;
DONE	long getScriptID(string name) throws   DSOutOfServiceException, DSAccessException;
DONE	long uploadScript(string script) throws  DSOutOfServiceException, DSAccessException;
DONE	string getScript(long id) throws  DSOutOfServiceException, DSAccessException;
DONE	Map<RType, RType> getParams(long id) throws  DSOutOfServiceException, DSAccessException;
DONE	Map<RType, RType> runScript(long id, Map<RType, RType> map) throws  DSOutOfServiceException, DSAccessException;
DONE	void deleteScript(long id) throws  DSOutOfServiceException, DSAccessException;
     };
    */
}


