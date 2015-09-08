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

package omero.gateway.model;


/**
 * Encapsulates a single object found by a search together with the search scope
 * (name, description, etc.) it was found with.
 *
 * Multiple search results are supposed to be held in an
 * {@link SearchResultCollection}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 * @since 5.0
 */
public class SearchResult {

    /** The scope (name, description, ...), see {@link SearchDataContext} */
    private int scopeId;

    /** Indicates the type (ImageData, DatasetData, ...) see {@link DataObject} */
    private Class<? extends DataObject> type;

    /** Id of the found object */
    private long objectId = -1;

    /** Id of the group the object belongs to */
    private long groupId = -1;

    /** The found object itself */
    private DataObject object;
    
    /** Indicates that this result is an ID match */
    private boolean idMatch = false;

    /**
     * Create a new instance
     */
    public SearchResult() {
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
    public SearchResult(int scopeId, Class<? extends DataObject> type,
            long objectId, long groupId) {
        this.scopeId = scopeId;
        this.type = type;
        this.objectId = objectId;
        this.groupId = groupId;
    }

    /**
     * The Id of the search scope, e.g. name, description, ... see
     * {@link SearchDataContext}
     *
     * @return See above.
     */
    public int getScopeId() {
        return scopeId;
    }

    /**
     * Set the id of the search scope, e.g. name, description, ... see
     * {@link SearchDataContext}
     *
     * @param scopeId The id of the search scope.
     */
    public void setScopeId(int scopeId) {
        this.scopeId = scopeId;
    }

    /**
     * Get the type (class) of objects to search for see {@link DataObject}
     *
     * @return See above.
     */
    public Class<? extends DataObject> getType() {
        return type;
    }

    /**
     * Set the type (class) of objects to search for see {@link DataObject}
     *
     * @param type The type of object to search for.
     */
    public void setType(Class<? extends DataObject> type) {
        this.type = type;
    }

    /**
     * Get the Id of the found object
     *
     * @return See above.
     */
    public long getObjectId() {
        return objectId;
    }

    /**
     * Set the Id of the found object
     *
     * @param objectId The id of the found object.
     */
    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    /**
     * Get the found object
     *
     * @return See above.
     */
    public DataObject getObject() {
        return object;
    }

    /**
     * Get the group id of the object
     *
     * @return See above.
     */
    public long getGroupId() {
        return groupId;
    }

    /**
     * Set the group id of the object
     *
     * @param groupId The value to set.
     */
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    /** 
     * Indicates if this result is an ID match 
     */
    public boolean isIdMatch() {
        return idMatch;
    }

    /**
     * Set to <code>true</code> if this result is an ID match.
     *
     * @param idMatch The value to set.
     */
    public void setIdMatch(boolean idMatch) {
        this.idMatch = idMatch;
    }

    /**
     * Set the found object
     *
     * @param object The value to set.
     */
    public void setObject(DataObject object) {
        if (objectId >= 0) {
            if (object.getId() != objectId)
                throw new IllegalArgumentException(
                        "objectId does not match the object!");
        } else {
            objectId = object.getId();
        }

        if (type != null) {
            if (!object.getClass().equals(type))
                throw new IllegalArgumentException("Cannot add a "
                        + object.getClass().getSimpleName()
                        + " to a SearchResult intended for "
                        + type.getSimpleName() + "!");
        } else {
            type = object.getClass();
        }

        if (groupId >= 0) {
            if (object.getGroupId() != groupId)
                throw new IllegalArgumentException("The object's groupId ("
                        + object.getGroupId()
                        + ") does not match the previous set groupId ("
                        + groupId + ") !");
        } else {
            groupId = object.getGroupId();
        }

        this.object = object;
    }

    @Override
    public String toString() {
        return "SearchResult [scopeId=" + scopeId + ", type="
                + (type != null ? type.getSimpleName() : "null")
                + ", objectId=" + objectId + ", groupId=" + groupId + "]";
    }

}
