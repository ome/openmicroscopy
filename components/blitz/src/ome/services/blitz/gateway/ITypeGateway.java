/*
 * blitzgateway.service.gateway.ITypeServiceGateway 
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.model.IObject;

import omero.gateways.DSAccessException;
import omero.gateways.DSOutOfServiceException;

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
public interface ITypeGateway
{	
	/**
	 * Get a list of the enumerations for type klass.
	 * @param klass see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<IObject> allEnumerations(String klass) 
	throws DSOutOfServiceException, DSAccessException;
	
	/*
	 * 
	 * ITypeService java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the IType service. 
	 * As the are created in the ITypeGateway they will be marked as 
	 * done.
	 * 
	 * 
	 * 	 	IObject createEnumeration(IObject newEnum) throws  DSOutOfServiceException, DSAccessException;
     	IObject getEnumeration(string type, string value) throws  DSOutOfServiceException, DSAccessException;
DONE	List<IObject> allEnumerations(string type) throws  DSOutOfServiceException, DSAccessException;
     	IObject updateEnumeration(IObject oldEnum) throws  DSOutOfServiceException, DSAccessException;
     	void updateEnumerations(List<IObject> oldEnums) throws  DSOutOfServiceException, DSAccessException;
     	void deleteEnumeration(IObject oldEnum) throws  DSOutOfServiceException, DSAccessException;
     	List<String> getEnumerationTypes() throws  DSOutOfServiceException, DSAccessException; 
     	List<String> getAnnotationTypes() throws  DSOutOfServiceException, DSAccessException;
     	List<IObject>Map getEnumerationsWithEntries() throws  DSOutOfServiceException, DSAccessException;
     	List<IObject> getOriginalEnumerations() throws  DSOutOfServiceException, DSAccessException;
     	void resetEnumerations(string enumClass) throws  DSOutOfServiceException, DSAccessException;
     */
}


