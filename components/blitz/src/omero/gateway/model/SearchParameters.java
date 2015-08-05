/*
 * org.openmicroscopy.shoola.env.data.util.SearchDataContext 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package omero.gateway.model;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import omero.gateway.facility.SearchFacility;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;


/**
 * Holds all parameters needed to perform a search operation;
 * See {@link SearchFacility#search(omero.gateway.SecurityContext, SearchParameters)}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.0
 */
public class SearchParameters
{
    /** The ID for searching across all groups*/
    public static final int ALL_GROUPS_ID = -1;

    /** Indicates that the date has to be interpreted as import date*/
    public static final int DATE_IMPORT = 0;

    /** Indicates that the date has to be interpreted as acquisition date*/
    public static final int DATE_ACQUISITION = 1;

    public static final Set<SearchScope> ALL_SCOPE;
    public static final List<Class<? extends DataObject>> ALL_TYPES;

    static {
        ALL_SCOPE = new HashSet<SearchScope>();
        ALL_SCOPE.add(SearchScope.NAME);
        ALL_SCOPE.add(SearchScope.DESCRIPTION);
        ALL_SCOPE.add(SearchScope.ANNOTATION);

        ALL_TYPES = new ArrayList<Class<? extends DataObject>>();
        ALL_TYPES.add(ImageData.class);
        ALL_TYPES.add(DatasetData.class);
        ALL_TYPES.add(ProjectData.class);
        ALL_TYPES.add(PlateData.class);
        ALL_TYPES.add(ScreenData.class);
    }

    /** The lower bound of the time interval. */
    private Timestamp start;

    /** The upper bound of the time interval. */
    private Timestamp end;

    private int dateType = -1;

    /** The scope of the search. Mustn't not be <code>null</code>. */
    private Set<SearchScope> scope;

    /** The types to search on. */
    private List<Class<? extends DataObject>> types;

    /** The query terms to search for */
    private String query;

    private long groupId = ALL_GROUPS_ID;

    /** The userId the search is restricted to.*/
    private long userId = -1;

    /**
     * Creates a new instance.
     * 
     * @param scope Scope of the search
     * @param types The types to search on, i.e. project, dataset, image.
     * @param some Some (at least one) of these terms must be present in 
     *             the document. May be <code>null</code>.
     * @param must All of these terms must be present in the document.
     *             May be <code>null</code>.
     * @param none None of these terms may be present in the document.
     *             May be <code>null</code>.
     */
    public SearchParameters(Set<SearchScope> scope,
            List<Class<? extends DataObject>> types, String query)
    {
        this.query = query;
        this.scope = scope;
        this.types = types;
    }

    /**
     * Sets the time interval.
     *
     * @param start The lower bound of the time interval.
     * @param end The upper bound of the time interval.
     */
    public void setTimeInterval(Timestamp start, Timestamp end, int type)
    {
        this.start = start;
        this.end = end;
        this.dateType = type;
    }

    /**
     * Returns the lower bound of the time interval.
     *
     * @return See above.
     */
    public Timestamp getStart() { return start; }

    /**
     * Returns the upper bound of the time interval.
     *
     * @return See above.
     */
    public Timestamp getEnd() { return end; }

    /**
     * Returns the scope of the search.
     *
     * @return See above.
     */
    public Set<SearchScope> getScope() { return scope; }

    /** 
     * Returns the types to search on.
     *
     * @return See above.
     */
    public List<Class<? extends DataObject>> getTypes() { return types; }

    /**
     * Returns the query terms to search for
     *
     * @return See above.
     */
    public String getQuery() { return query; }

    /**
     * Returns <code>true</code> if the context of the search is valid i.e.
     * parameters correctly set, <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isValid()
    {
        return !(StringUtils.isBlank(query) && start == null
                && end == null);
    }

    /**
     * Returns <code>true</code> if text to search for,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean hasTextToSearch()
    {
        return StringUtils.isNotBlank(query);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getDateType() {
        return dateType;
    }

    public void setDateType(int dateType) {
        this.dateType = dateType;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

}
