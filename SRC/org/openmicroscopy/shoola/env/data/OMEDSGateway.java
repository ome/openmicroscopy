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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.DataServer;
import org.openmicroscopy.ds.DataServices;
import org.openmicroscopy.ds.RemoteAuthenticationException;
import org.openmicroscopy.ds.RemoteCaller;
import org.openmicroscopy.ds.RemoteConnectionException;
import org.openmicroscopy.ds.RemoteServerErrorException;
import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.DataInterface;
import org.openmicroscopy.ds.dto.UserState;
import org.openmicroscopy.ds.managers.AnnotationManager;
import org.openmicroscopy.ds.managers.DatasetManager;
import org.openmicroscopy.ds.managers.ProjectManager;
import org.openmicroscopy.ds.st.Experimenter;

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
	private DataServices	proxiesFactory;
	
	OMEDSGateway(URL omedsAddress) 
	{
		proxiesFactory = DataServer.getDefaultServices(omedsAddress);
	}
	
	/**
	 * Tries to connect to <i>OMEDS</i> and log in by using the supplied
	 * credentials.
	 * 
	 * @param userName	The user name to be used for login.
	 * @param password	The password to be used for login.
	 * @throws DSOutOfServiceException If the connection can't be established
	 * 									or the credentials are invalid.
	 */
	void login(String userName, String password)
		throws DSOutOfServiceException
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
	
	private ProjectManager getProjectManager()
	{
		return (ProjectManager) proxiesFactory.getService(ProjectManager.class);
	}
	
	private DatasetManager getDatasetManager()
	{
		return (DatasetManager) proxiesFactory.getService(DatasetManager.class);
	}
	
	private AnnotationManager getAnnotationManager()
	{
		return (AnnotationManager) proxiesFactory.getService(
													AnnotationManager.class);
	}
	
	/** Retrieve the current experimenter. */
	Experimenter getCurrentUser(Criteria c)
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
		return us.getExperimenter();
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
	 * Create a new Attribute object
	 * Wrap the call to the {@link DataFactory#createNew(String) create}
	 * method.
	 * @param semanticType 	the semantic type to create.
	 * @return Attribute.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * create a DataInterface from OMEDS service. 
	 */
	Attribute createNewData(String semanticTypeName) 
		throws DSOutOfServiceException, DSAccessException 
	{
		Attribute retVal;
		try {
			retVal = getDataFactory().createNew(semanticTypeName);
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
   	
   	/**
   	 * Load the graph defined by the criteria.
	 * Wrap the call to the 
	 * {@link DataFactory#retrieveList(String, Criteria) retrieve} method.
	 * 
   	 * @param semanticTypeName	specified semanticType.
   	 * @param c					criteria by which the object graph is pulled 
   	 * 							out.
   	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service. 
   	 */
   	Object retrieveListSTSData(String semanticTypeName, Criteria c)
		throws DSOutOfServiceException, DSAccessException 
	{
		Object retVal;
		try {
			retVal = getDataFactory().retrieveList(semanticTypeName, c);
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
   	 * Load the graph defined by the criteria.
	 * Wrap the call to the 
	 * {@link DataFactory#retrieve(String, Criteria) retrieve} method.
   	 * @param semanticTypeName	specified semanticType.
   	 * @param c					criteria by which the object graph is pulled 
   	 * 							out.
   	 * @return
   	 * @throws DSOutOfServiceException
   	 * @throws DSAccessException
   	 */
   	Object retrieveSTSData(String semanticTypeName, Criteria c)
		throws DSOutOfServiceException, DSAccessException
	{
		Object retVal;
		try {
			retVal = getDataFactory().retrieve(semanticTypeName, c);
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
	 * Wrap the call to the 
	 * {@link DataFactory#count(SemanticType, Criteria) count}
	 * method.
	 * @param type		type, the semantic type to count.
	 * @param c			criteria by which the object graph is pulled out.
	 * @return
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service. 
	 */
	int countData(String type, Criteria c)
		throws DSOutOfServiceException, DSAccessException
	{
		int val;
		try {
			val = getDataFactory().count(type, c);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't retrieve data", rsee);
		} 
		return val;
	}
	
	/** Mark the specified for update. */
	void markForUpdate(DataInterface object) 
	{
		getDataFactory().markForUpdate(object);
	}
   
   	/** Commit the marked objects. */
	void updateMarkedData()
		throws DSOutOfServiceException, DSAccessException 
	{
		try {
			getDataFactory().updateMarked();
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		} 
	}
	
	/** Add a list of {@link Dataset}s to a {@link Project}. */
	void addDatasetsToProject(int projectID, List datasetIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			getProjectManager().addDatasetsToProject(projectID, datasetIDs);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		} 
	}
	
	/** Add a {@link Dataset} to a list of {@link Project}s. */
	void addDatasetToProjects(int datasetID, List projectIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			getProjectManager().addDatasetToProjects(projectIDs, datasetID);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		} 
	}
	
	/** Remove a list of {@link Dataset}s from a {@link Project}.  */
	void removeDatasetsFromProject(int projectID, List datasetsIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
	  		getProjectManager().removeDatasetsFromProject(projectID, 
	  														datasetsIDs);
  		} catch (RemoteConnectionException rce) {
	  		throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
  		} catch (RemoteAuthenticationException rae) {
	  		throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
	  		throw new DSAccessException("Can't load data", rsee);
  		} 
	}
	
	/** Add a list of {@link Image}s to a {@link Dataset}. */
	void addImagesToDataset(int datasetID, List imageIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			getDatasetManager().addImagesToDataset(datasetID, imageIDs);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		} 
	}
	
	/** Remove a list of {@link Image}s from a {@link Dataset}.  */
	void removeImagesFromDataset(int datasetID, List imagesIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			getDatasetManager().removeImagesFromDataset(datasetID, imagesIDs);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		} 
	}
	
	/** Annotate. Each attribute in the list must be a newly-created
     * attribute; otherwise, call updateAttributes() with that attribute
     * as a member. */
	void annotateAttributesData(List attributes)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
            getAnnotationManager().annotateAttributes(attributes);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
            rsee.printStackTrace();
			throw new DSAccessException("Can't load data", rsee);
		} 
	}
    
    /** Update attributes. */
    void updateAttributes(List attributes)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            getDataFactory().updateList(attributes);
        } catch(RemoteConnectionException rce) {
            throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
        } catch (RemoteAuthenticationException rae) {
            throw new DSOutOfServiceException("Not logged in", rae);
        } catch (RemoteServerErrorException rsee) {
            rsee.printStackTrace();
            throw new DSAccessException("Can't load data", rsee);
        }
    }
	
}

