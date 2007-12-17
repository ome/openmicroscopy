/*
 * org.openmicroscopy.shoola.util.ui.search.SearchContext 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

	public static final int			JUST_CURRENT_USER = 0;
	
	
	public static final int			CURRENT_USER_AND_OTHERS = 1;
	
	public static final int			JUST_OTHERS = 2;
	
	/** Identifying the <code>Image</code> context. */
	public static final int			IMAGES = 0;
	
	/** Identifying the <code>Dataset</code> context. */
	public static final int			DATASETS = 1;
	
	/** Identifying the <code>Project</code> context. */
	public static final int			PROJECTS = 2;
	
	/** Identifying the <code>Annotation</code> context. */
	public static final int			ANNOTATIONS = 3;
	
	/** Identifying the <code>Tag</code> context. */
	public static final int			TAGS = 4;
	
	/** Identifying the <code>Tag set</code> context. */
	public static final int			TAG_SETS = 5;
	
	/** Indicates not to take into account the time criteria. */
	static final int				ANY_DATE = 0;
	
	/** Indicates to search for objects imported in the last 2 weeks. */
	static final int 				LAST_TWO_WEEKS = 1; 
	
	/** Indicates to search for objects imported in the last month. */
	static final int				LAST_MONTH = 2; 
	
	/** Indicates to search for objects imported in the last 2 months. */
	static final int				LAST_TWO_MONTHS = 3; 
	
	/** Indicates to search for objects imported in the last year. */
	static final int				ONE_YEAR = 4; 
	
	/** 
	 * Indicates to search for objects imported during a given period of 
	 * time. 
	 */
	static final int	RANGE = 5; 
	
	/** Maximum number of time options. */
	static final int 	MAX = 5;
	
	/** Collection of terms to search for. */
	private List<String>	terms;
	
	/** Collection of context. */
	private List<Integer>	context;
	
	/** Collection of context. */
	private List<String>	users;
	
	/** The start time of the interval. */
	private Timestamp		startTime;
	
	/** The end time of the interval. */
	private Timestamp		endTime;
	
	/** 
	 * One out of the following indexes: 
	 * {@link #JUST_CURRENT_USER}, {@link #JUST_OTHERS} or 
	 * {@link #CURRENT_USER_AND_OTHERS}.
	 */
	private int				userSearchContext;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param terms		Collection of terms to search. 
	 * @param context	Collection of context.
	 */
	SearchContext(List<String> terms, List<Integer> context)
	{
		this.terms = terms;
		this.context = context;
	}
	
	/**
	 * Sets the context of the search for users.
	 * 
	 * @param index The value to set.
	 */
	void setUserSearchContext(int index)
	{
		userSearchContext = index;
	}
	
	/**
	 * Sets the start and end times.
	 * 
	 * @param startTime	The start time.
	 * @param endTime	The end time.
	 */
	void setTime(Timestamp startTime, Timestamp endTime)
	{
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * Returns the collection of selected users if any.
	 * 
	 * @param users The value to set.
	 */
	void setUsers(List<String> users) { this.users = users; }
	
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
			case ONE_YEAR:
				gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
											0, 1, 0, 0, 0);
				startTime = new Timestamp(gc.getTime().getTime());
		}
	}
	
	/**
	 * Returns the collection of terms to search for. 
	 * 
	 * @return See above.
	 */
	public List<String> getTerms() { return terms; }
	
	/**
	 * Returns the collection of context.
	 * 
	 * @return See above.
	 */
	public List<Integer> getContext() { return context; }

	/** 
	 * Returns the collection of users' details.
	 * 
	 * @return See above.
	 */
	public List<String> getUsers() { return users; }
	
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
	public int getUserSearchContext() { return userSearchContext; }
	
	
}
