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

import loci.formats.IFormatReader;
import ome.formats.Index;
import ome.util.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Annotation;
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
	 * Returns the current Bio-Formats reader that has been used to populate
	 * the container store.
	 * @return See above.
	 */
	IFormatReader getReader();

	/**
	 * Sets the Bio-Formats reader that will be used to populate the container
	 * store.
	 * @param reader Bio-Formats reader.
	 */
	void setReader(IFormatReader reader);

	/**
	 * Returns the user specified annotations.
	 * @return See above.
	 */
	List<Annotation> getUserSpecifiedAnnotations();

    /**
     * Sets the user specified image annotations.
     * @param annotations user specified annotations
     */
    void setUserSpecifiedAnnotations(List<Annotation> annotations);

    /**
     * Returns the user specified image/plate name.
     * @return See above.
     */
    String getUserSpecifiedName();

    /**
     * Sets the user specified image/plate name.
     * @param name user specified image/plate name
     */
    void setUserSpecifiedName(String name);

    /**
     * Returns the user specified image/plate description.
     * @return See above.
     */
    String getUserSpecifiedDescription();

    /**
     * Sets the user specified image/plate description.
     * @param description user-specified image/plate description
     */
    void setUserSpecifiedDescription(String description);

    /**
     * Returns the user-specified linkage target (usually a Dataset for Images)
     * and a Screen for Plates).
     * @return See above.
     */
    IObject getUserSpecifiedTarget();

    /**
     * Sets the user-specified linkage target (usually a Dataset for Images)
     * and a Screen for Plates).
     * @param target user-specified linkage target
     */
    void setUserSpecifiedTarget(IObject target);

    /**
     * Returns the user specified physical pixel sizes.
     * @return An array of double[] { physicalSizeX, physicalSizeY,
     * physicalSizeZ } as specified by the user. A value of <code>null</code>
     * for any one index states the user has not made a choice for the size
     * of that particular dimension.
     */
    Double[] getUserSpecifiedPhysicalPixelSizes();

    /**
     * Sets the user specified physical pixel sizes. A value of
     * <code>null</code> states the original file physical size for that
     * dimension should be used.
     * @param physicalSizeX Physical pixel size width.
     * @param physicalSizeY Physical pixel height.
     * @param physicalSizeZ Physical pixel depth.
     */
    void setUserSpecifiedPhysicalPixelSizes(Double physicalSizeX,
		                                Double physicalSizeY,
		                                Double physicalSizeZ);

    /**
     * Returns the current authoritative LSID container cache. This container
     * cache records the explicitly set LSID to container references.
     * @return See above.
     */
    Map<Class<? extends IObject>, Map<String, IObjectContainer>>
	getAuthoritativeContainerCache();

    /**
     * Returns the current container cache.
     * @return See above.
     */
    Map<LSID, IObjectContainer> getContainerCache();

    /**
     * Returns the current reference cache.
     * @return See above.
     */
    Map<LSID, List<LSID>> getReferenceCache();

    /**
     * Adds a reference to the reference cache.
     * @param source Source LSID to add.
     * @param target Target LSID to add.
     */
    void addReference(LSID source, LSID target);

    /**
     * Returns the current string based reference cache. This is usually
     * populated by a ReferenceProcessor instance.
     * @return See above.
     */
    Map<String, String[]> getReferenceStringCache();

    /**
     * Sets the string based reference cache for this container store. This is
     * usually called by a ReferenceProcessor instance.
     * @param referenceStringCache String based reference cache to use.
     */
    void setReferenceStringCache(Map<String, String[]> referenceStringCache);

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
     * OME-XML data model. <b>NOTE:</b> The container will be created if it
     * does not already exist.
     * @param klass Class to retrieve a container for.
     * @param indexes Indexes into the OME-XML data model.
     * @return See above.
     */
    IObjectContainer getIObjectContainer(Class<? extends IObject> klass,
		                             LinkedHashMap<Index, Integer> indexes);

    /**
     * Removes an IObject container from within the OME-XML data model store.
     * @param lsid LSID of the container to remove.
     */
    void removeIObjectContainer(LSID lsid);

    /**
     * Retrieves all IObject containers of a given class. <b>NOTE:</b> this
     * will only return <b>existing</b> containers.
     * @param klass Class to retrieve containers for.
     * @return See above.
     */
    List<IObjectContainer> getIObjectContainers(Class<? extends IObject> klass);

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
