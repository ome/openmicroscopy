/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.agents.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pojos.DataObject;
import pojos.FileAnnotationData;

/** 
 * Holds the result of a FileAnnotationCheck, i. e.
 * the {@link DataObject}s each {@link FileAnnotationData} is linked to
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class FileAnnotationCheckResult {

    /** Map holding the results; key: {@link FileAnnotationData}, value: List of {@link DataObject}s the {@link FileAnnotationData} is linked to */
    private Map<FileAnnotationData, List<DataObject>> linkMap = new HashMap<FileAnnotationData, List<DataObject>>();

    /**
     * Get all {@link DataObject}s the given {@link FileAnnotationData} is linked to.
     * This method will never return null, but rather will return an empty list.
     * @param fd The {@link FileAnnotationData}
     * @return See above.
     */
    public List<DataObject> getLinks(FileAnnotationData fd) {
        List<DataObject> existingLinks = this.linkMap.get(fd);
        if (existingLinks == null) {
            existingLinks = new ArrayList<DataObject>();
        }
        return existingLinks;
    }

    /**
     * Add {@link DataObject}s the given {@link FileAnnotationData} object is linked to.
     * @param fd The {@link FileAnnotationData}
     * @param links The {@link DataObject}s fd is linked to
     */
    public void addLinks(FileAnnotationData fd, List<DataObject> links) {
        List<DataObject> existingLinks = this.linkMap.get(fd);
        if (existingLinks == null) {
            existingLinks = new ArrayList<DataObject>();
            this.linkMap.put(fd, existingLinks);
        }
        existingLinks.addAll(links);
    }

    /**
     * Get a list of {@link FileAnnotationData} which are linked to only one {@link DataObject}.
     * This method will never return null, but rather will return an empty list.
     * @return See above.
     */
    public List<FileAnnotationData> getSingleParentAnnotations() {
        List<FileAnnotationData> result = new ArrayList<FileAnnotationData>();
        for(FileAnnotationData fd : linkMap.keySet()) {
            List<DataObject> parents = linkMap.get(fd);
            if(parents!=null && parents.size()==1) {
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
}
