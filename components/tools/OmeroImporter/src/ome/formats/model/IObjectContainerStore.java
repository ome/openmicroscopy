/*
 * ome.formats.model.IObjectContainerStore
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package ome.formats.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ome.formats.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.IObject;

/**
 * An object that handles the storage and manipulation of IObject containers 
 * and their references. This interface is generally used in tandem with the 
 * Bio-Formats defined MetadataStore.  
 *  
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public interface IObjectContainerStore
{
    /**
     * Returns the current container cache.
     * @return See above.
     */
    Map<LSID, IObjectContainer> getContainerCache();
    
    /**
     * Returns the current reference cache.
     * @return See above.
     */
    Map<LSID, LSID> getReferenceCache();
    
    /**
     * Returns the current string based reference cache. This is usually 
     * populated by a ReferenceProcessor instance.
     * @return See above.
     */
    Map<String, String> getReferenceStringCache();
    
    /**
     * Sets the string based reference cache for this container store. This is
     * usually called by a ReferenceProcessor instance.
     * @param referenceStringCache String based reference cache to use.
     */
    void setReferenceStringCache(Map<String, String> referenceStringCache);
    
    /**
     * Retrieves an OMERO Blitz source object for a given LSID.
     * @param LSID LSID to retrieve a source object for.
     * @return See above.
     */
    IObject getSourceObject(LSID LSID);
    
    /**
     * Retrieves all OMERO Blitz source objects of a given class.
     * @param klass Class to retrieve source objects for.
     * @return See above.
     */
    <T extends IObject> List<T> getSourceObjects(Class<T> klass);
    
    /**
     * Retrieves an IObject container for a given class and location within the
     * OME-XML data model.
     * @param klass Class to retrieve a container for.
     * @param indexes Indexes into the OME-XML data model.
     * @return See above.
     */
    IObjectContainer getIObjectContainer(Class<? extends IObject> klass,
    		                             LinkedHashMap<String, Integer> indexes);
    
    /**
     * Counts the number of containers the MetadataStore has of a given class
     * and at a given index of the hierarchy if specified.
     * @param klass Class to count containers of.
     * @param indexes Indexes to use in the container count. For example, if
     * <code>klass</code> is <code>Image</code> and indexes is 
     * <code>int[] { 0 };</code> only containers that have an LSID of type
     * <code>Image</code> and a first index of <code>0</code> will be counted.
     * @return See above.
     */
    int countCachedContainers(Class<? extends IObject> klass, int... indexes);
    
    /**
     * Counts the number of references the MetadataStore has between objects
     * of two classes.
     * @param source Class of the source object. If <code>null</code> it is
     * treated as a wild card, all references whose target match
     * <code>target</code> will be counted. 
     * @param target Class of the target object. If <code>null</code> it is
     * treated as a wild card, all references whose source match
     * <code>source</code> will be counted. 
     * @return See above.
     */
    int countCachedReferences(Class<? extends IObject> source,
                              Class<? extends IObject> target);

    /**
     * Checks to see if there is currently an active reference for two LSIDs.
     * @param source LSID of the source object.
     * @param target LSID of the target object.
     * @return <code>true</code> if a reference exists, <code>false</code>
     * otherwise.
     */
    boolean hasReference(LSID source, LSID target);
}
