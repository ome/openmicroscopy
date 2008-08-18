/*
 * blitzgateway.service.FileService 
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

import java.util.List;
import java.util.Map;

import omero.RType;
import omero.model.Format;
import omero.model.OriginalFile;


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
interface FileService
{	
	/**
	 * Keep service alive.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	public void keepAlive() throws omero.ServerError;
	
	/**
	 * Does the file with id exist in the OMERO original file store.
	 * @param id id of the file.
	 * @return see above.
	 * @throws omero.ServerError
	 * @throws DSOutOfServiceException 
	 */
	public boolean fileExists(Long id) 
							throws omero.ServerError;
	
	/**
	 * find the file with fileName in the database and format and return the 
	 * file ids that match as a list.
	 * @param fileName name of the file.
	 * @param fmt The format of the file. 
	 * @return see above.
	 * @throws omero.ServerError
	 * @throws DSOutOfServiceException 
	 */
	public List<Long> findFile(String fileName, Format fmt)
						throws omero.ServerError;
	
	/**
	 * Get the rawfile from the database with the id
	 * @param id the id of the file to retrieve.
	 * @return see above.
	 * @throws omero.ServerError
	 * @throws DSOutOfServiceException
	 */
	public byte[] getRawFile(long id)
						throws omero.ServerError;
	
	/**
	 * Get the file with the id as a string.
	 * @param id The id of the file.
	 * @return see above.
	 * @throws omero.ServerError
	 * @throws DSOutOfServiceException
	 */
	public String getFileAsString(long id)
						throws omero.ServerError;
	
	/**
	 * Get the format of the file with id.
	 * @param id The file id.
	 * @return see above.
	 * @throws omero.ServerError
	 * @throws DSOutOfServiceException
	 */
	public Format getFileFormat(long id)
					throws omero.ServerError;

	/**
	 * Get the format object for string map.
	 * @param fmt String of the format.
	 * @return see above.
	 * @throws omero.ServerError
	 * @throws DSOutOfServiceException
	 */
	public Format getFormat(String fmt) 
			throws omero.ServerError;
	
	
	/**
	 * Get all the file formats in the system.
	 * @return see above.
	 * @throws omero.ServerError
	 * @throws DSOutOfServiceException
	 */
	public List<Format> getAllFormats()
				throws omero.ServerError;
	/**
	 * Get the original file with id.
	 * @param id see above.
	 * @return see above.
	 * @throws omero.ServerError
	 * @throws DSOutOfServiceException
	 */
	public OriginalFile getOriginalFile(long id) throws omero.ServerError;
	
	/**
	 * Get the scripts from the iScript Service.
	 * @return all the available scripts, mapped by id, name
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	Map<Long, String> getScripts() throws   
											omero.ServerError;
	
	/**
	 * Get the id of the script with name 
	 * @param name name of the script.
	 * @return the id of the script.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	long getScriptID(String name) throws 
										 omero.ServerError;
	
	/**
	 * Upload the script to the server.
	 * @param script script to upload
	 * @return id of the new script.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	long uploadScript(String script) throws 
											omero.ServerError;
	
	/**
	 * Get the script with id, this returns the actual script as a string.
	 * @param id id of the script to retrieve.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	String getScript(long id) throws 
									 omero.ServerError;
	
	/**
	 * Get the params the script takes, this is the name and type. 
	 * @param id id of the script.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	Map<String, RType> getParams(long id) throws omero.ServerError;
	
	/**
	 * Run the script and get the results returned as a name , value map.
	 * @param id id of the script to run.
	 * @param map the map of params, values for inputs.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	Map<String, RType> runScript(long id, Map<String, RType> map) 
						throws omero.ServerError;
	
	/**
	 * Delete the script from the server.
	 * @param id id of the script to delete.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	void deleteScript(long id) throws 	
										omero.ServerError;
	
}



