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

import pojos.DataObject;

public class AdvancedSearchResult {

    /** The scope (name, description, ...) */
    private int scopeId;
    
    /** Indicates the type (imagei, dataseti, ...) */
    private Class<?> type;
    
    private long objectId;
    
    private DataObject object;
    
    public AdvancedSearchResult() {
    }

    
    public AdvancedSearchResult(int scopeId, Class<?> type, long objectId) {
        this.scopeId = scopeId;
        this.type = type;
        this.objectId = objectId;
    }

    public int getScopeId() {
        return scopeId;
    }

    public void setScopeId(int scopeId) {
        this.scopeId = scopeId;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public DataObject getObject() {
        return object;
    }

    public void setObject(DataObject object) {
        this.object = object;
    }
    
}
