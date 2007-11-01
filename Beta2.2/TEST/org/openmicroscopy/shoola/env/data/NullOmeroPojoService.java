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
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;
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
     * @see OmeroDataService#findCGCPaths(Set, int, long)
     */
    public Set findCGCPaths(Set imgIDs, int algorithm, long levelID) 
    	throws DSOutOfServiceException, DSAccessException 
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#getCollectionCount(Class, String, Set)
     */
    public Map getCollectionCount(Class rootNodeType, String property,
            						Set rootNodeIDs)
    	throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#loadContainerHierarchy(Class, Set, boolean, long)
     */
    public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs,
            boolean withLeaves, long rootLevelID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#findContainerHierarchy(Class, Set, long)
     */
    public Set findContainerHierarchy(Class rootNodeType, Set leavesIDs,
                						long rootLevelID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#findAnnotations(Class, Set, Set)
     */
    public Map findAnnotations(Class nodeType, Set nodeIDs, Set annotatorIDs, 
    							boolean forUser)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#getImages(Class, Set, long)
     */
    public Set getImages(Class nodeType, Set nodeIDs, long rootLevelID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#createDataObject(DataObject, DataObject)
     */
    public DataObject createDataObject(DataObject newObject, DataObject parent)
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
     * @see OmeroDataService#removeAnnotationFrom(DataObject, AnnotationData)
     */
    public DataObject removeAnnotationFrom(DataObject annotatedObject,
                                            AnnotationData data)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#updateAnnotationFor(DataObject, AnnotationData)
     */
    public DataObject updateAnnotationFor(DataObject annotatedObject,
                                            AnnotationData data)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#loadExistingObjects(Class, Set, long)
     */
    public Set loadExistingObjects(Class nodeType, Set nodeIDs, long rootID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroDataService#addExistingObjects(DataObject, Set)
     */
    public void addExistingObjects(DataObject parent, Set children)
            throws DSOutOfServiceException, DSAccessException {}

    /**
     * No-op implementation
     * @see OmeroDataService#cutAndPaste(Map, Map)
     */
    public void cutAndPaste(Map toPaste, Map toCut)
            throws DSOutOfServiceException, DSAccessException {}

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
     * @see OmeroDataService#getChannelsMetadata(long)
     */
    public List getChannelsMetadata(long pixelsID)
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
     * @see OmeroDataService#updateAnnotationFor(Map)
     */
	public List updateAnnotationFor(Map toUpdate) 
		throws DSOutOfServiceException, DSAccessException
    {
		return null;
	}

    /**
     * No-op implementation
     * @see OmeroDataService#getAvailableGroups()
     */
	public Map<GroupData, Set> getAvailableGroups() 
		throws DSOutOfServiceException, DSAccessException 
	{
		return null;
	}

    /**
     * No-op implementation
     * @see OmeroDataService#getOrphanContainers(Class, boolean, long)
     */
	public Set getOrphanContainers(Class nodeType, boolean b, long rootLevelID) 
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
     * @see OmeroDataService#getArchivedFiles(String, long)
     */
	public Map getArchivedFiles(String location, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

    /**
     * No-op implementation
     * @see OmeroDataService#annotateChildren(Set, AnnotationData)
     */
	public List annotateChildren(Set objects, AnnotationData data) 
		throws DSOutOfServiceException, DSAccessException 
	{
		return null;
	}

    /**
     * No-op implementation
     * @see OmeroDataService#classifyChildren(Set, Set)
     */
	public Set classifyChildren(Set containers, Set categories) 
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
		return null;
	}

    /**
     * No-op implementation
     * @see OmeroDataService#classify(Set, Set)
     */
	public Set classify(Set<ImageData> images, Set<CategoryData> categories) 
		throws DSOutOfServiceException, DSAccessException 
	{
		return null;
	}

    /**
     * No-op implementation
     * @see OmeroDataService#declassify(Set, Set)
     */
	public Set declassify(Set<ImageData> images, Set<CategoryData> categories) 
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
     * @see OmeroDataService#getServerName()
     */
	public String getServerName() { return null; }

    /**
     * No-op implementation
     * @see OmeroDataService#removeAnnotationFrom(DataObject, List)
     */
	public DataObject removeAnnotationFrom(DataObject annotatedObject, 
					List data) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
     * No-op implementation
     * @see OmeroDataService#getSpace(int)
     */
	public long getSpace(int index) 
		throws DSOutOfServiceException, DSAccessException
	{
		return 0;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#getImagesAfter(Timestamp, long)
	 */
	public Set getImagesAfter(Timestamp time, long rootID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#getImagesBefore(Timestamp, long)
	 */
	public Set getImagesBefore(Timestamp time, long rootID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#getImagesPeriod(Timestamp, Timestamp, long)
	 */
	public Set getImagesPeriod(Timestamp lowerTime, Timestamp time, long userID)
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
	 * @see OmeroDataService#loadTopContainerHierarchy(Class, long)
	 */
	public Set loadTopContainerHierarchy(Class rootNodeType, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#findCategoryPaths(long, boolean, long)
	 */
	public Set findCategoryPaths(long imageID, boolean leaves, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#findCategoryPaths(Set, boolean, long)
	 */
	public Set findCategoryPaths(Set<Long> imagesID, boolean leaves, 
								long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#getImagesAfterIObject(Timestamp, long)
	 */
	public List getImagesAfterIObject(Timestamp time, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#getImagesBeforeIObject(Timestamp, long)
	 */
	public List getImagesBeforeIObject(Timestamp time, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#getImagesPeriodIObject(Timestamp, Timestamp, long)
	 */
	public List getImagesPeriodIObject(Timestamp lowerTime, Timestamp time, 
								long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return null;
	}

	/**
	 * No-op implementation
	 * @see OmeroDataService#getLoggingName()
	 */
	public String getLoggingName()
	{
		return null;
	}

}
