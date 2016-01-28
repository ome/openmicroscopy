/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.HashMultimap;

import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;

/** 
 * Holds the result of a FileAnnotationCheck, i.e.
 * the {@link DataObject}s each {@link FileAnnotationData} is linked to
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class FileAnnotationCheckResult {

    /** Map holding the results; */
    private HashMultimap<FileAnnotationData, DataObject> linkMap = HashMultimap.create();
    
    /** The DataObjects from which the FileAnnotations should be removed */
    private List<DataObject> referenceObjects;
    
    /**
     * Creates a new instance
     */
    public FileAnnotationCheckResult() {
    }
    
    /**
     * Creates a new instance
     * @param referenceObjects The DataObjects from which the FileAnnotations should be removed
     */
    public FileAnnotationCheckResult(List<DataObject> referenceObjects) {
        this.referenceObjects = referenceObjects;
    }

    /**
     * Get all {@link DataObject}s the given {@link FileAnnotationData} is linked to.
     * This method will never return null, but rather will return an empty list.
     * @param fd The {@link FileAnnotationData}
     * @return See above.
     */
    public Set<DataObject> getLinks(FileAnnotationData fd) {
        return linkMap.get(fd);
    }

    /**
     * Add {@link DataObject}s the given {@link FileAnnotationData} object is linked to.
     * @param fd The {@link FileAnnotationData}
     * @param links The {@link DataObject}s fd is linked to
     */
    public void addLinks(FileAnnotationData fd, List<DataObject> links) {
        linkMap.putAll(fd, links);
    }

    /**
     * Get the {@link FileAnnotationData} which can be deleted, e. g. are not
     * referenced anywhere else
     * 
     * @return See above.
     */
    public List<FileAnnotationData> getDeleteCandidates() {
        List<FileAnnotationData> result = new ArrayList<FileAnnotationData>();
        for (FileAnnotationData fd : linkMap.keySet()) {
            Set<DataObject> parents = linkMap.get(fd);
            if(CollectionUtils.isEmpty(referenceObjects) && parents.size()==1) {
                result.add(fd);
            }
            else if (containsAll(referenceObjects, parents)) {
                result.add(fd);
            }
        }
        return result;
    }
    
    /**
     * Get all {@link FileAnnotationData}.
     * @return See above.
     */
    public Set<FileAnnotationData> getAllAnnotations() {
        return linkMap.keySet();
    }
    
    /**
     * Checks if all DataObjects of collB are included in collA
     * 
     * @param collA
     * @param collB
     * @return See above.
     */
    boolean containsAll(Collection<DataObject> collA,
            Collection<DataObject> collB) {
        for (DataObject b : collB) {
            boolean contains = false;
            for (DataObject a : collA) {
                if (a.getClass().equals(b.getClass()) && a.getId() == b.getId()) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                return false;
            }
        }
        return true;
    }
}
