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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import pojos.DataObject;

public class AdvancedSearchResultCollection extends
        ArrayList<AdvancedSearchResult> {

    private boolean error = false;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof AdvancedSearchResult))
            return false;

        AdvancedSearchResult a = (AdvancedSearchResult) o;

        for (AdvancedSearchResult b : this) {
            if (b.getType().equals(a.getType())
                    && b.getObjectId() == a.getObjectId())
                return true;
        }

        return false;
    }

    public List<AdvancedSearchResult> getResultsByScopeId(int scopeId) {
        List<AdvancedSearchResult> result = new ArrayList<AdvancedSearchResult>();
        for (AdvancedSearchResult r : this) {
            if (r.getScopeId() == scopeId) {
                result.add(r);
            }
        }
        return result;
    }

    public List<AdvancedSearchResult> getResultsByType(Class<?> type) {
        List<AdvancedSearchResult> result = new ArrayList<AdvancedSearchResult>();
        for (AdvancedSearchResult r : this) {
            if (r.getType().equals(type)) {
                result.add(r);
            }
        }
        return result;
    }
    
    public Collection<DataObject> getDataObjects() {
        Collection<DataObject> result = new ArrayList<DataObject>(size());
        for(AdvancedSearchResult r : this)
            result.add(r.getObject());
        return result;
    }

}
