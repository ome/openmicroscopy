/*
 * org.openmicroscopy.shoola.env.data.NullOmeroPojoService
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.io.File;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries



//Application-internal dependencies
import omero.api.StatefulServiceInterfacePrx;

import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.data.util.SearchParameters;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;

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
     * No-operation implementation
     * @see OmeroDataService#addExistingObjects(DataObject, Collection)
     */
	public void addExistingObjects(SecurityContext ctx, DataObject parent, Collection children) 
		throws DSOutOfServiceException, DSAccessException
	{
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#advancedSearchFor(SearchDataContext)
     */
	public Object advancedSearchFor(SecurityContext ctx, SearchDataContext context) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#annotateChildren(Set, AnnotationData)
     */
	public List annotateChildren(SecurityContext ctx, Set folders, AnnotationData data) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#changePassword(String, String)
     */
	public Boolean changePassword(SecurityContext ctx, String oldPassword, String newPassword)
		throws DSOutOfServiceException, DSAccessException
	{
		return Boolean.valueOf(false);
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#createAnnotationFor(DataObject, AnnotationData)
     */
	public DataObject createAnnotationFor(SecurityContext ctx, DataObject annotatedObject,
			AnnotationData data)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#createAnnotationFor(Set, AnnotationData)
     */
	public List createAnnotationFor(SecurityContext ctx, Set toAnnotate, AnnotationData data)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#createDataObject(DataObject, DataObject, Collection)
     */
	public DataObject createDataObject(SecurityContext ctx, DataObject newObject,
			DataObject parent, Collection children) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#cutAndPaste(Map, Map)
     */
	public void cutAndPaste(SecurityContext ctx, Map toPaste, Map toCut)
		throws DSOutOfServiceException, DSAccessException
	{
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#deleteContainer(DataObject, boolean)
     */
	public void deleteContainer(SecurityContext ctx, DataObject object,
			boolean content) 
		throws DSOutOfServiceException, DSAccessException
	{	
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#findAnnotations(Class, List, List, boolean)
     */
	public Map findAnnotations(SecurityContext ctx, Class nodeType,
		List nodeIDs, List annotatorIDs, boolean forUser) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#findContainerHierarchy(Class, List, long)
     */
	public Set findContainerHierarchy(SecurityContext ctx, Class rootNodeType,
			List leavesIDs, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#findContainerPaths(Class, long, long)
     */
	public Collection findContainerPaths(SecurityContext ctx, Class type,
			long id, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getArchivedFiles(String, long)
     */
	public Map getArchivedImage(SecurityContext ctx, File location,
			long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getCollectionCount(Class, String, List)
     */
	public Map getCollectionCount(SecurityContext ctx, Class rootNodeType,
			String property, List rootNodeIDs) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getExperimenterImages(long)
     */
	public Set getExperimenterImages(SecurityContext ctx, long userID,
			boolean orphan)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getImages(Class, List, long)
     */
	public Set getImages(SecurityContext ctx, Class nodeType, List nodeIDs,
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getImagesAllPeriodCount(Timestamp, Timestamp, long)
     */
	public List getImagesAllPeriodCount(SecurityContext ctx,
			Timestamp lowerTime, Timestamp time, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getImagesPeriod(Timestamp, Timestamp, long, boolean)
     */
	public Collection getImagesPeriod(SecurityContext ctx, Timestamp lowerTime,
		Timestamp time, long userID, boolean asDataObject)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getLoggingName()
     */
	public String getLoggingName() { return null; }

	/**
     * No-operation implementation
     * @see OmeroDataService#getOriginalFiles(long)
     */
	public Collection getOriginalFiles(SecurityContext ctx, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getServerName()
     */
	public String getServerName() { return null; }

	/**
     * No-operation implementation
     * @see OmeroDataService#getSpace(int, long)
     */
	public long getSpace(SecurityContext ctx, int index, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return 0;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#loadContainerHierarchy(Class, List, boolean, long)
     */
	public Set loadContainerHierarchy(SecurityContext ctx, Class rootNodeType,
		List rootNodeIDs, boolean withLeaves, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#loadExistingObjects(Class, List, long)
     */
	public Set loadExistingObjects(SecurityContext ctx, Class nodeType,
			List nodeIDs, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#loadPlateWells(long, long, long)
     */
	public Collection loadPlateWells(SecurityContext ctx, long plateID,
			long acquisitionID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#loadScreenPlates(Class, List, long)
     */
	public Set loadScreenPlates(SecurityContext ctx, Class rootNodeType,
			List rootNodeIDs, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#loadTopContainerHierarchy(Class, long)
     */
	public Set loadTopContainerHierarchy(SecurityContext ctx,
			Class rootNodeType, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#removeDataObjects(Set, DataObject)
     */
	public Set removeDataObjects(SecurityContext ctx, Set children,
			DataObject parent)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#updateAnnotationFor(Map)
     */
	public List updateAnnotationFor(SecurityContext ctx, Map toUpdate) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#updateDataObject(DataObject)
     */
	public DataObject updateDataObject(SecurityContext ctx, DataObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#updateExperimenter(ExperimenterData, GroupData)
     */
	public ExperimenterData updateExperimenter(SecurityContext ctx,
			ExperimenterData exp, GroupData group) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#delete(Collection)
     */
	public RequestCallback delete(SecurityContext ctx,
			Collection<DeletableObject> objects) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getImage(long, long)
     */
	public ImageData getImage(SecurityContext ctx, long imageID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getServerVersion()
     */
	public String getServerVersion() {
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#getServerVersion()
     */
	public FSFileSystemView getFSRepositories(SecurityContext ctx, long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#transfer(SecurityContext, List, SecurityContext, List)
     */
	public RequestCallback transfer(SecurityContext ctx,
			SecurityContext target, List<DataObject> targetNode,
			List<DataObject> objects) throws DSOutOfServiceException,
			DSAccessException, ProcessException
	{
		return null;
	}

	/**
     * No-operation implementation
     * @see OmeroDataService#loadPlateFromImage(SecurityContext, Collection)
     */
	public Map<Long, PlateData> loadPlateFromImage(SecurityContext ctx,
			Collection<Long> ids) throws DSOutOfServiceException,
			DSAccessException {
		return null;
	}
	
	
	public void closeService(SecurityContext ctx,
			StatefulServiceInterfacePrx svc) {}

	/**
     * No-operation implementation
     * @see OmeroDataService#getImagesBySplitFilesets(SecurityContext, Class, List)
     */
	public Map<Long, Map<Boolean, List<ImageData>>> getImagesBySplitFilesets(
			SecurityContext ctx, Class<?> rootType, List<Long> rootIDs)
			throws DSOutOfServiceException, DSAccessException {
		return null;
	}

	/**
	     * No-operation implementation
	     * @see OmeroDataService#findDatasetsByImageId(SecurityContext, long)
	     */
        public Map<Long, List<DatasetData>> findDatasetsByImageId(SecurityContext ctx,
                List<Long> imgIds) throws DSOutOfServiceException, DSAccessException {
            return null;
        }

        /**
         * No-operation implementation
         * @see OmeroDataService#search(SecurityContext, SearchParameters)
         */
        public AdvancedSearchResultCollection search(SecurityContext ctx,
                SearchParameters context) throws DSOutOfServiceException,
                DSAccessException {
            return null;
        }

        /**
         * No-operation implementation
         * @see OmeroDataService#search(SecurityContext, SearchParameters, int)
         */
        public AdvancedSearchResultCollection search(SecurityContext ctx,
                SearchParameters context, int maxResults) throws DSOutOfServiceException,
                DSAccessException {
            return null;
        }
}
