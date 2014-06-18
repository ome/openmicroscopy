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

package org.openmicroscopy.shoola.env.data.util;

import omero.model.IObject;
import pojos.DataObject;

/**
 * Encapsulates a single object found by a search together with the search
 * scope (name, description, etc.) it was found with.
 * 
 * Multiple search results are supposed to be held in an {@link AdvancedSearchResultCollection}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @version 5.0
 */
public class AdvancedSearchResult {

    /** The scope (name, description, ...), see {@link SearchDataContext} */
    private int scopeId;

    /** Indicates the type (ImageData, DatasetData, ...) see {@link DataObject} */
    private Class<? extends DataObject> type;

    /** Id of the found object */
    private long objectId;

    /** The found object itself */
    private DataObject object;

    /**
     * Create a new instance
     */
    public AdvancedSearchResult() {
    }

    /**
     * Create a new instance
     * 
     * @param scopeId
     *            Id of the search scope, see {@link SearchDataContext}
     * @param type
     *            Type of the object to search
     * @param objectId
     *            Id of the found object
     */
    public AdvancedSearchResult(int scopeId, Class<? extends DataObject> type, long objectId) {
        this.scopeId = scopeId;
        this.type = type;
        this.objectId = objectId;
    }

    /**
     * The Id of the search scope, e. g. name, description, ... see
     * {@link SearchDataContext}
     * 
     * @return
     */
    public int getScopeId() {
        return scopeId;
    }

    /**
     * Set the id of the search scope, e. g. name, description, ... see
     * {@link SearchDataContext}
     * 
     * @param scopeId
     */
    public void setScopeId(int scopeId) {
        this.scopeId = scopeId;
    }

    /**
     * Get the type (class) of objects to search for see {@link DataObject}
     * 
     * @return
     */
    public Class<? extends DataObject> getType() {
        return type;
    }

    /**
     * Set the type (class) of objects to search for see {@link DataObject}
     * 
     * @param type
     */
    public void setType(Class<? extends DataObject> type) {
        this.type = type;
    }

    /**
     * Set the Id of the found object
     * 
     * @return
     */
    public long getObjectId() {
        return objectId;
    }

    /**
     * Get the Id of the found object
     * 
     * @param objectId
     */
    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    /**
     * Get the found object
     * 
     * @return
     */
    public DataObject getObject() {
        return object;
    }

    /**
     * Set the found object
     * 
     * @param object
     */
    public void setObject(DataObject object) {
        if (objectId >= 0) {
            if (object.getId() != objectId)
                throw new IllegalArgumentException(
                        "objectId does not match the object!");
            objectId = object.getId();
        }
        if (type != null) {
            if (!object.getClass().equals(type))
                throw new IllegalArgumentException("Cannot add a "
                        + object.getClass().getSimpleName()
                        + " to an AdvancedSearchResult intended for "
                        + type.getSimpleName() + "!");
            type = object.getClass();
        }
        this.object = object;
    }

    @Override
    public String toString() {
        return "AdvancedSearchResult [scopeId=" + scopeId + ", type=" + type.getSimpleName()
                + ", objectId=" + objectId + "]";
    }

    
}
