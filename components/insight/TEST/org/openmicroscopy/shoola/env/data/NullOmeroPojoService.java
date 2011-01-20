/*
 * org.openmicroscopy.shoola.env.data.NullOmeroPojoService
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data;



//Java imports
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class NullOmeroPojoService
    implements OmeroDataService
{

	/**
     * No-op implementation
     * @see OmeroDataService#addExistingObjects(DataObject, Collection)
     */
	public void addExistingObjects(DataObject parent, Collection children) 
		throws DSOutOfServiceException, DSAccessException
	{
	}

	/**
     * No-op implementation
     * @see OmeroDataService#advancedSearchFor(SearchDataContext)
     */
	public Object advancedSearchFor(SearchDataContext context) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#annotateChildren(Set, AnnotationData)
     */
	public List annotateChildren(Set folders, AnnotationData data) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#changePassword(String, String)
     */
	public Boolean changePassword(String oldPassword, String newPassword)
		throws DSOutOfServiceException, DSAccessException
	{
		return Boolean.valueOf(false);
	}

	/**
     * No-op implementation
     * @see OmeroDataService#createAnnotationFor(DataObject, AnnotationData)
     */
	public DataObject createAnnotationFor(DataObject annotatedObject, 
			AnnotationData data)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#createAnnotationFor(Set, AnnotationData)
     */
	public List createAnnotationFor(Set toAnnotate, AnnotationData data) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#createDataObject(DataObject, DataObject, Collection)
     */
	public DataObject createDataObject(DataObject newObject, DataObject parent, 
			Collection children) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#cutAndPaste(Map, Map)
     */
	public void cutAndPaste(Map toPaste, Map toCut)
		throws DSOutOfServiceException, DSAccessException
	{
	}

	/**
     * No-op implementation
     * @see OmeroDataService#deleteContainer(DataObject, boolean)
     */
	public void deleteContainer(DataObject object, boolean content) 
		throws DSOutOfServiceException, DSAccessException
	{	
	}

	/**
     * No-op implementation
     * @see OmeroDataService#findAnnotations(Class, List, List, boolean)
     */
	public Map findAnnotations(Class nodeType, List nodeIDs, List annotatorIDs,
			boolean forUser) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#findContainerHierarchy(Class, List, long)
     */
	public Set findContainerHierarchy(Class rootNodeType, List leavesIDs, 
				long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#findContainerPaths(Class, long, long)
     */
	public Collection findContainerPaths(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getArchivedFiles(String, long)
     */
	public Map getArchivedFiles(String location, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getChannelsMetadata(long)
     */
	public List getChannelsMetadata(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getCollectionCount(Class, String, List)
     */
	public Map getCollectionCount(Class rootNodeType, String property, 
			List rootNodeIDs) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getExperimenterImages(long)
     */
	public Set getExperimenterImages(long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getImages(Class, List, long)
     */
	public Set getImages(Class nodeType, List nodeIDs, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getImagesAllPeriodCount(Timestamp, Timestamp, long)
     */
	public List getImagesAllPeriodCount(Timestamp lowerTime, Timestamp time, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getImagesPeriod(Timestamp, Timestamp, long, boolean)
     */
	public Collection getImagesPeriod(Timestamp lowerTime, Timestamp time, long userID, 
			boolean asDataObject)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getLoggingName()
     */
	public String getLoggingName() { return null; }

	/**
     * No-op implementation
     * @see OmeroDataService#getOriginalFiles(long)
     */
	public Collection getOriginalFiles(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getServerName()
     */
	public String getServerName() { return null; }

	/**
     * No-op implementation
     * @see OmeroDataService#getSpace(int, long)
     */
	public long getSpace(int index, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return 0;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#loadContainerHierarchy(Class, List, boolean, long)
     */
	public Set loadContainerHierarchy(Class rootNodeType, List rootNodeIDs, 
			boolean withLeaves, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#loadExistingObjects(Class, List, long)
     */
	public Set loadExistingObjects(Class nodeType, List nodeIDs, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#loadPlateWells(long, long)
     */
	public Collection loadPlateWells(long plateID, long acquisitionID, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#loadScreenPlates(Class, List, long)
     */
	public Set loadScreenPlates(Class rootNodeType, List rootNodeIDs, 
			long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#loadTopContainerHierarchy(Class, long)
     */
	public Set loadTopContainerHierarchy(Class rootNodeType, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#removeDataObjects(Set, DataObject)
     */
	public Set removeDataObjects(Set children, DataObject parent)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#updateAnnotationFor(Map)
     */
	public List updateAnnotationFor(Map toUpdate) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#updateDataObject(DataObject)
     */
	public DataObject updateDataObject(DataObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#updateExperimenter(ExperimenterData)
     */
	public ExperimenterData updateExperimenter(ExperimenterData exp) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#delete(Collection)
     */
	public Collection<DeletableObject> delete(
			Collection<DeletableObject> objects) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getImage(long, long)
     */
	public ImageData getImage(long imageID, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getServerVersion()
     */
	public String getServerVersion()
	{
		return null;
	}

}
