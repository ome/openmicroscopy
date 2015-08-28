/*
 * org.openmicroscopy.shoola.util.ui.search.SearchContext 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *      This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Helper class storing the relevant information to search for.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SearchContext
{

    /** Retrieves the data for the logged in user. */
    public static final int                 CURRENT_USER = 0;

    /** 
     * Retrieves the data for the selected users excluding the logged in users.
     */
    public static final int                 OTHERS = 1;

    /** Identifying the <code>Image</code> context. */
    public static final int                 IMAGES = 0;

    /** Identifying the <code>Dataset</code> context. */
    public static final int                 DATASETS = 1;

    /** Identifying the <code>Project</code> context. */
    public static final int                 PROJECTS = 2;

    /** Identifying the <code>Screen</code> context. */
    public static final int                 SCREENS = 3;

    /** Identifying the <code>Plate</code> context. */
    public static final int                 PLATES = 4;

    /** Identifying the <code>Well</code> context. */
    public static final int                 WELLS = 5;

    /** Identifying the <code>Annotation</code> context. */
    public static final int                 TEXT_ANNOTATION = 6;

    /** Identifying the <code>Tag</code> context. */
    public static final int                 TAGS = 7;

    /** Identifying the <code>Tag set</code> context. */
    public static final int                 TAG_SETS = 8;

    /** Identifying the <code>Name</code> context. */
    public static final int                 NAME = 9;

    /** Identifying the <code>File annotation</code> context. */
    public static final int                 FILE_ANNOTATION = 10;

    /** Identifying the <code>URL annotation</code> context. */
    public static final int                 URL_ANNOTATION = 11;

    /** Identifying the <code>Description</code> context. */
    public static final int                 DESCRIPTION = 12;

    /** Identifying the <code>Customized</code> context. */
    public static final int                 CUSTOMIZED = 13;

    /** Identifying the <code>Annotation</code> context. */
    public static final int                 ANNOTATION = 14;

    /** Indicates not to take into account the time criteria. */
    public static final int                         ANY_DATE = 0;

    /** Indicates to search for objects imported in the last 2 weeks. */
    public static final int                                 LAST_TWO_WEEKS = 1; 

    /** Indicates to search for objects imported in the last month. */
    public static final int                         LAST_MONTH = 2; 

    /** Indicates to search for objects imported in the last 2 months. */
    public static final int                         LAST_TWO_MONTHS = 3; 

    /** Indicates to search for objects imported in the last year. */
    public static final int                         ONE_YEAR = 4; 

    /** 
     * Indicates to search for objects imported during a given period of 
     * time. 
     */
    public static final int                         RANGE = 5; 

    /** Maximum number of time options. */
    public static final int                                 MAX = 5;

    /** Identifies the number of results {@link #LEVEL_ONE_VALUE}. */
    public static final int                         LEVEL_ONE = 0;

    /** Identifies the number of results {@link #LEVEL_TWO_VALUE}. */
    public static final int                         LEVEL_TWO = 1;

    /** Identifies the number of results {@link #LEVEL_THREE_VALUE}. */
    public static final int                         LEVEL_THREE = 2;

    /** Identifies the number of results {@link #LEVEL_FOUR_VALUE}. */
    public static final int                         LEVEL_FOUR = 3;

    /** The number of options for possible returned results. */
    public static final int                         MAX_RESULTS = 3;

    /** The number of results returned. */
    public static final int                         LEVEL_ONE_VALUE = 50;

    /** The number of results returned. */
    public static final int                         LEVEL_TWO_VALUE = 100;

    /** The number of results returned. */
    public static final int                         LEVEL_THREE_VALUE = 250;

    /** The number of results returned. */
    public static final int                         LEVEL_FOUR_VALUE = 500;

    /** Identifies all the formats to search for. */
    public static final int                         ALL_FORMATS = 0;

    /** Indicates to search for <code>HTML</code> files only. */
    public static final int                         HTML = 1;

    /** Indicates to search for <code>HTML</code> files only. */
    public static final int                         PDF = 2;

    /** Indicates to search for <code>HTML</code> files only. */
    public static final int                         EXCEL = 3;

    /** Indicates to search for <code>HTML</code> files only. */
    public static final int                         POWER_POINT = 4;

    /** Indicates to search for <code>HTML</code> files only. */
    public static final int                         WORD = 5;

    /** Indicates to search for <code>HTML</code> files only. */
    public static final int                         XML = 6;

    /** Indicates to search for <code>HTML</code> files only. */
    public static final int                         TEXT = 7;

    /** The max number of supported formats. */
    public static final int                         MAX_FORMAT = 8;

    /** The query to search for. */
    private String          query;

    /** Collection of context. */
    private List<Integer>   context;

    /** Collection of context. */
    private List<Integer>   type;

    /** Collection of selected users. */
    private long            selectedOwner = -1;

    /** Collection of users to exclude. */
    private List<String>    excludedOwners;

    /** Collection of users to exclude. */
    private List<String>    excludedAnnotators;

    /** The start time of the interval. */
    private Timestamp               startTime;

    /** The end time of the interval. */
    private Timestamp               endTime;

    /** 
     * One out of the following indexes: {@link #CURRENT_USER}, {@link #OTHERS}.
     */
    private List<Integer>   ownerSearchContext;

    /** 
     * One out of the following indexes: {@link #CURRENT_USER}, {@link #OTHERS}.
     */
    private List<Integer>   annotatorSearchContext;

    /** Flag indicating if the search is case sensitive or not. */
    private boolean                 caseSensitive;

    /** The number of results. */
    private int                             numberOfResults;

    /** 
     * One of following constants: {@link SearchParameters#DATE_IMPORT}, 
     * {@link SearchParameters#DATE_AQUISITION}.
     */
    private int                             timeType;

    /** 
     * One of following constants: {@link #ANY_DATE}, {@link #LAST_TWO_WEEKS},
     * {@link #LAST_MONTH}, {@link #LAST_TWO_MONTHS}, {@link #ONE_YEAR} or
     * {@link #RANGE}.
     */
    private int                             dateIndex;

    /** The type of attachments to retrieve. */
    private int                             attachmentType;

    /** The group to search for */
    private long selectedGroup = GroupContext.ALL_GROUPS_ID;

    /**
     * Creates a new instance.
     * 
     * @param query The terms to search for.
     * @param context Collection of context.
     */
    public SearchContext(String query, List<Integer> context)
    {
        this.query = query;
        this.context = context;
        timeType = -1;
        attachmentType = ALL_FORMATS;
    }

    /**
     * Sets the case sensitivity flag.
     * 
     * @param caseSensitive The value to set.
     */
    void setCaseSensitive(boolean caseSensitive)
    { 
        this.caseSensitive = caseSensitive;
    }

    /**
     * Sets the context of the search for users.
     * 
     * @param context The value to set.
     */
    void setOwnerSearchContext(List<Integer> context)
    {
        ownerSearchContext = context;
    }

    /**
     * Sets the context of the search for users.
     * 
     * @param context The value to set.
     */
    void setAnnotatorSearchContext(List<Integer> context)
    {
        annotatorSearchContext = context;
    }

    /**
     * Sets the start and end times.
     * 
     * @param startTime     The start time.
     * @param endTime       The end time.
     */
    public void setTime(Timestamp startTime, Timestamp endTime)
    {
        this.startTime = startTime;
        this.endTime = endTime;
        if (startTime != null && endTime != null) dateIndex = RANGE;
    }

    /**
     * Sets the user context for the search
     * 
     * @param user The value to set.
     */
    public void setSelectedOwner(long user) { this.selectedOwner = user; }

    /**
     * Sets the {@link #startTime} and {@link #endTime} depending on the
     * passed index. 
     * 
     * @param index One of the constants defined by this class.
     */
    void setTime(int index)
    {
        GregorianCalendar gc = new GregorianCalendar();
        endTime = UIUtilities.getDefaultTimestamp();
        switch (index) {
            case ANY_DATE:
                setTime(null, null);
                break;
            case LAST_TWO_WEEKS:
                gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
                        gc.get(Calendar.MONTH), 
                        gc.get(Calendar.DAY_OF_MONTH), 23, 59, 0);
                startTime = new Timestamp(
                        gc.getTime().getTime()-14*UIUtilities.DAY);
                break;
            case LAST_MONTH:
                gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
                        gc.get(Calendar.MONTH), 
                        gc.get(Calendar.DAY_OF_MONTH), 23, 59, 0);

                startTime = new Timestamp(
                        gc.getTime().getTime()-30*UIUtilities.DAY);
                break;
            case LAST_TWO_MONTHS:
                gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
                        gc.get(Calendar.MONTH), 
                        gc.get(Calendar.DAY_OF_MONTH), 23, 59, 0);
                startTime = new Timestamp(
                        gc.getTime().getTime()-60*UIUtilities.DAY);
                break;
            case ONE_YEAR:
                gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
                        0, 1, 0, 0, 0);
                startTime = new Timestamp(gc.getTime().getTime());
        }
        dateIndex = index;
    }


    public void setTimeType(int timeType) {
        this.timeType = timeType;
    }

    /** 
     * Sets the collection of users to exclude.
     * 
     * @param users The value to set.
     */
    void setExcludedOwners(List<String> users) { excludedOwners = users; }

    /** 
     * Sets the collection of users to exclude.
     * 
     * @param users The value to set.
     */
    void setExcludedAnnotators(List<String> users)
    { 
        excludedAnnotators = users;
    }

    /**
     * Sets the attachments to search for.
     * 
     * @param type The value to set.
     */
    void setAttachmentType(int type)
    {
        switch (type) {
            case ALL_FORMATS:
            case HTML:
            case PDF:
            case EXCEL:
            case POWER_POINT:
            case WORD:
            case XML:
            case TEXT:
                attachmentType = type;
                break;
            default:
                attachmentType = ALL_FORMATS;
        }
    }

    /**
     * Returns the attachments to search for.
     * 
     * @return See above.
     */
    int getAttachmentType() { return attachmentType; }

    /** 
     * Returns one of the time constants defined by this class.
     * 
     * @return See above.
     */
    public int getDateIndex() { return dateIndex; }

    /**
     * Returns the time index.
     * 
     * @return See above.
     */
    public int getTimeType() { return timeType; }

    /**
     * Returns the collection of terms to search for. 
     * 
     * @return See above.
     */
    public String getQuery() { return query; }

    /**
     * Returns the collection of context.
     * 
     * @return See above.
     */
    public List<Integer> getContext() { return context; }

    /** 
     * Returns the collection of selected users.
     * 
     * @return See above.
     */
    public long getSelectedOwner() { return selectedOwner; }

    /** 
     * Returns the collection of users to exclude.
     * 
     * @return See above.
     */
    public List<String> getExcludedOwners() { return excludedOwners; }

    /** 
     * Returns the collection of users to exclude.
     * 
     * @return See above.
     */
    public List<String> getExcludedAnnotators() { return excludedAnnotators; }

    /**
     * Returns the start of time interval.
     * 
     * @return See above.
     */
    public Timestamp getStartTime() { return startTime; }

    /**
     * Returns the end of time interval.
     * 
     * @return See above.
     */
    public Timestamp getEndTime() { return endTime; }

    /**
     * Returns the context of the search for users.
     * 
     * @return See above.
     */
    public List<Integer> getOwnerSearchContext() { return ownerSearchContext; }

    /**
     * Returns the context of the search for users.
     * 
     * @return See above.
     */
    public List<Integer> getAnnotatorSearchContext()
    { 
        return annotatorSearchContext; 
    }

    /**
     * Returns <code>true</code> if the search is case sensitive, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isCaseSensitive() { return caseSensitive; }

    /**
     * Sets the type.
     * 
     * @param type The value to set.
     */
    public void setType(List<Integer> type) { this.type = type; }

    /**
     * Returns the type.
     * 
     * @return See above.
     */
    public List<Integer> getType() { return type; }

    /** 
     * Returns the numbers of results.
     * 
     * @return See above.
     */
    public int getNumberOfResults() { return numberOfResults; }

    /**
     * Sets the number of results.
     * 
     * @param results The value to set.
     */
    public void setNumberOfResults(int results) { numberOfResults = results; }

    /** Get the group to search for */
    public long getSelectedGroup() {
        return selectedGroup;
    }

    /** Set the group to search for */
    public void setSelectedGroup(long selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

}
