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
import java.util.List;

import pojos.DataObject;

/**
 * Holds the results of a search; a collection of
 * {@link AdvancedSearchResult}s
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; 
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @version 5.0
 *
 */
public class AdvancedSearchResultCollection extends
        ArrayList<AdvancedSearchResult> {

    /** Indicates there was an error with the search */
    private boolean error = false;

    /**
     * @return <code>true</code> if there was an error with
     * the search, <code>false</code> otherwise
     */
    public boolean isError() {
        return error;
    }

    /**
     * Set the error flag
     * @param error Pass <code>true</code> if there was an error with 
     * the search, <code>false</code> otherwise
     */
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

   /**
    * Get all results of a certain scope and/or type
    * @param scopeId The scope to filter for, see {@link SearchDataContext}
    * @param type The type to filter for, see {@link DataObject}
    * @return
    */
    public List<AdvancedSearchResult> getResults(int scopeId, Class<? extends DataObject> type) {
        List<AdvancedSearchResult> result = new ArrayList<AdvancedSearchResult>();
        for (AdvancedSearchResult r : this) {
            if (scopeId<0 && r.getScopeId() != scopeId) {
                continue;
            }
            if (type!=null && !r.getType().equals(type)) {
                continue;
            }
            result.add(r);
        }
        return result;
    }

    @Override
    public String toString() {
        String s = "AdvancedSearchResultCollection [error=" + error + "]:\n";
        for(AdvancedSearchResult r : this) {
            s += r.toString()+"\n";
        }
        return s;
    }

    
}
