/*
 * org.openmicroscopy.shoola.env.data.OMEDSGateway
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

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.net.URL;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.DataServer;
import org.openmicroscopy.ds.RemoteAuthenticationException;
import org.openmicroscopy.ds.RemoteCaller;
import org.openmicroscopy.ds.RemoteConnectionException;
import org.openmicroscopy.ds.RemoteServerErrorException;
import org.openmicroscopy.ds.RemoteServices;
import org.openmicroscopy.ds.dto.DataInterface;
import org.openmicroscopy.ds.dto.UserState;

/** 
 * Unified access point to the various <i>OMEDS</i> services.
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
class OMEDSGateway 
{

	/**
	 * The factory provided by the connection library to access the various
	 * <i>OMEDS</i> services.
	 */
	private RemoteServices	proxiesFactory;
	
	OMEDSGateway(URL omedsAddress) 
	{
		proxiesFactory = DataServer.getDefaultServices(omedsAddress);
	}
	
	/**
	 * Try to connect to <i>OMEDS</i>.
	 * 
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * 			retrieve data from OMEDS service.
	 */
	void login(String userName, String password)
		throws DSOutOfServiceException, DSAccessException
	{
		RemoteCaller proxy = proxiesFactory.getRemoteCaller();
		proxy.login(userName, password);
		
		//TODO: The proxy should throw a checked exception if login fails!
		//Catch that exception when the connection lib will be modified.
	}
	
	void logout()
	{
		RemoteCaller proxy = proxiesFactory.getRemoteCaller();
		proxy.logout();
		
		//TODO: The proxy should throw a checked exception on failure!
		//Catch that exception when the connection lib will be modified.
		
		//TODO: The proxy should have a dispose method to release resources
		//like sockets.  Add this call when connection lib will be modified.
		//proxy.dispose();	
		
	}
	
	DataFactory getDataFactory()
	{
		return (DataFactory) proxiesFactory.getService(DataFactory.class);
	}

	/** Retrieve the user State. */
	UserState getUserState(Criteria c)
		throws DSOutOfServiceException, DSAccessException
	{ 
		UserState us;
		try {
		us = (UserState) getDataFactory().getUserState(c);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't retrieve the user id", rsee);
		} 
		return us;
	}
	
	/**
	 * Create a new Data Interface object
	 * Wrap the call to the {@link DataFactory#createNew(Class) create}
	 * method.
	 * @param dto 	targetClass, the core data type to count.
	 * @return DataInterface.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * create a DataInterface from OMEDS service. 
	 */
	DataInterface createNewData(Class dto)
			throws DSOutOfServiceException, DSAccessException 
	{
		DataInterface retVal;
		try {
			retVal = getDataFactory().createNew(dto);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		} 
		return retVal; 
	}

	/**
	 * Retrieve the graph defined by the criteria.
	 * Wrap the call to the 
	 * {@link DataFactory#retrieveList(Class, int, Criteria) retrieve}
	 * method.
	 *  
	 * @param dto		targetClass, the core data type to count.
	 * @param c			criteria by which the object graph is pulled out.
	 * @return
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service. 
	 */
	Object retrieveListData(Class dto, Criteria c) 
		throws DSOutOfServiceException, DSAccessException
	{
		Object retVal;
		try {
			retVal = getDataFactory().retrieveList(dto, c);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't retrieve data", rsee);
		} 
		return retVal;
	}
	
	/**
	* Load the graph defined by the criteria.
	* Wrap the call to the 
	* {@link DataFactory#retrieve(Class, Criteria) retrieve} method.
	* 
	* @param dto		targetClass, the core data type to count.
	* @param c			criteria by which the object graph is pulled out. 
	* @throws DSOutOfServiceException If the connection is broken, or logged in
	* @throws DSAccessException If an error occured while trying to 
	* retrieve data from OMEDS service. 
	*/
   Object retrieveData(Class dto, Criteria c) 
	   throws DSOutOfServiceException, DSAccessException 
   {
	   Object retVal;
	   try {
		   retVal = getDataFactory().retrieve(dto, c);
	   } catch (RemoteConnectionException rce) {
		   throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
	   } catch (RemoteAuthenticationException rae) {
		   throw new DSOutOfServiceException("Not logged in", rae);
	   } catch (RemoteServerErrorException rsee) {
		   throw new DSAccessException("Can't load data", rsee);
	   } 
	   return retVal;
   }
   
}
