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

package org.openmicroscopy.shoola.agents.treeviewer.view;

import org.openmicroscopy.shoola.env.event.RequestEvent;

/**
 * SearchEvent to trigger a default search with the given query
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 *
 * @since 5.0
 */
public class SearchEvent extends RequestEvent {

    /** The search query, i.e. terms */
    private String query;

    /**
     * Creates a new instance
     * @param query The query.
     */
    public SearchEvent(String query) {
        this.query = query;
    }

    /**
     * Get the query to perform search with
     * @return See above.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Set the query for the search
     * @param query The query.
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
}
