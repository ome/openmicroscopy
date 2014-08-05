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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pojos.DataObject;

/**
 * Holds the results of a search; a collection of {@link AdvancedSearchResult}s
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 * @since 5.0
 * 
 */
public class AdvancedSearchResultCollection extends
        ArrayList<AdvancedSearchResult> {

    /** No error */
    public static final int NO_ERROR = 0;

    /** Some other error */
    public static final int GENERAL_ERROR = 1;

    /** The search has to many results */
    public static final int TOO_MANY_RESULTS_ERROR = 2;
    
    /** The search has to many clauses, e.g. a 'a*' search term 
     *  would expand to too many single clauses */
    public static final int TOO_MANY_CLAUSES = 3;

    /** Error code if there was an error with the search */
    private int error = NO_ERROR;

    /**
     * @return <code>true</code> if there was an error with the search,
     *         <code>false</code> otherwise
     */
    public boolean isError() {
        return error != NO_ERROR;
    }

    /**
     * Set the error state
     * 
     * @param error
     */
    public void setError(int error) {
        this.error = error;
    }

    /**
     * Get the error code
     * 
     * @return
     */
    public int getError() {
        return this.error;
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

    @Override
    public void clear() {
        super.clear();
        error = NO_ERROR;
    }

    /**
     * Get all results of a certain scope and/or type
     * 
     * @param scopeId
     *            The scope to filter for, see {@link SearchDataContext}
     * @param type
     *            The type to filter for, see {@link DataObject}
     * @return
     */
    public List<AdvancedSearchResult> getResults(int scopeId,
            Class<? extends DataObject> type) {
        List<AdvancedSearchResult> result = new ArrayList<AdvancedSearchResult>();
        for (AdvancedSearchResult r : this) {
            if (scopeId < 0 && r.getScopeId() != scopeId) {
                continue;
            }
            if (type != null && !r.getType().equals(type)) {
                continue;
            }
            result.add(r);
        }
        return result;
    }

    /**
     * Get DataObjects filtered by scope and type
     * 
     * @param scopeId
     * @param type
     * @return
     */
    public List<DataObject> getDataObjects(int scopeId,
            Class<? extends DataObject> type) {
        List<DataObject> result = new ArrayList<DataObject>();
        for (AdvancedSearchResult r : this) {
            if (scopeId >= 0 && r.getScopeId() != scopeId) {
                continue;
            }
            if (type != null && !r.getType().equals(type)) {
                continue;
            }
            result.add(r.getObject());
        }
        return result;
    }

    /**
     * Get a perGroup map representation of the results
     * 
     * @return
     */
    public Map<Long, List<AdvancedSearchResult>> getByGroup() {
        return getByGroup(null);
    }

    /**
     * Get a perGroup map representation of the results, filtered by type
     * 
     * @param type
     * @return
     */
    public Map<Long, List<AdvancedSearchResult>> getByGroup(
            Class<? extends DataObject> type) {
        Map<Long, List<AdvancedSearchResult>> result = new HashMap<Long, List<AdvancedSearchResult>>();
        for (AdvancedSearchResult r : this) {
            List<AdvancedSearchResult> list = result.get(r.getGroupId());
            if (list == null) {
                list = new ArrayList<AdvancedSearchResult>();
                result.put(r.getGroupId(), list);
            }
            if (type == null || (r.getType().equals(type)))
                list.add(r);
        }
        return result;
    }

    /**
     * Removes results which DataObjects are not set
     */
    public void consolidate() {
        Iterator<AdvancedSearchResult> it = this.iterator();
        while (it.hasNext()) {
            AdvancedSearchResult r = it.next();
            if (r.getObject() == null)
                it.remove();
        }
    }

    @Override
    public String toString() {
        String s = "AdvancedSearchResultCollection [error=" + error + "]:\n";
        for (AdvancedSearchResult r : this) {
            s += r.toString() + "\n";
        }
        return s;
    }

}
