/*
 * org.openmicroscopy.shoola.env.data.util.SearchDataContext 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries


import pojos.DataObject;
//Application-internal dependencies

public class SearchParameters
{

	/** Identifying the <code>Annotation</code> context. */
	public static final int			TEXT_ANNOTATION = 0;
	
	/** Identifying the <code>Tag</code> context. */
	public static final int			TAGS = 1;
	
	/** Identifying the <code>Name</code> context. */
	public static final int			NAME = 2;
	
	/** Identifying the <code>File annotation</code> context. */
	public static final int			FILE_ANNOTATION = 3;
	
	/** Identifying the <code>URL annotation</code> context. */
	public static final int			URL_ANNOTATION = 4;
	
	/** Identifying the <code>Description</code> context. */
	public static final int			DESCRIPTION = 5;
	
	/** Identifying the <code>Time</code> context. */
	public static final int			TIME = 6;
	
	/** Identifying the <code>Name</code> context. */
	public static final int			CUSTOMIZED = 7;
	
	/** Identifying the <code>Annotation</code> context. */
        public static final int                 ANNOTATION = 8;
	
	/** The lower bound of the time interval. */
	private Timestamp 				start;
	
	/** The upper bound of the time interval. */
	private Timestamp 				end;
	
	/** The scope of the search. Mustn't not be <code>null</code>. */
	private List<Integer>			scope;
	
	/** The types to search on. */
	private List<Class<? extends DataObject>>	types;
	
	/** The query terms to search for */
	private String[] terms = new String[0];

//	/** The groupIds the search is restricted to.*/
//	private List<Long> groupIds = new ArrayList<Long>();
	
	/** The userId the search is restricted to.*/
	private long userId = -1;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param scope	Scope of the search
	 * @param types The types to search on, i.e. project, dataset, image.
	 * @param some	Some (at least one) of these terms must be present in 
	 * 				the document. May be <code>null</code>.
	 * @param must	All of these terms must be present in the document.
	 * 				May be <code>null</code>.
	 * @param none	None of these terms may be present in the document. 
	 * 				May be <code>null</code>.
	 */
	public SearchParameters(List<Integer> scope, List<Class<? extends DataObject>> types, String[] terms)
	{
		this.terms = terms;
		this.scope = scope;
		this.types = types;
	}
	
	/**
	 * Sets the time interval.
	 * 
	 * @param start The lower bound of the time interval.
	 * @param end	The upper bound of the time interval.
	 */
	public void setTimeInterval(Timestamp start, Timestamp end)
	{
		this.start = start;
		this.end = end;
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
	public List<Integer> getScope() { return scope; }
	
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
	public String[] getTerms() { return terms; }
	
	/**
	 * Returns <code>true</code> if the context of the search is valid i.e.
	 * parameters correctly set, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isValid()
	{
		return !(terms.length==0 && start == null
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
		return terms.length>0;
	}
    
//        public List<Long> getGroupIds() {
//            return groupIds;
//        }
//    
//        public void setGroupIds(List<Long> groupIds) {
//            this.groupIds = groupIds;
//        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }
        
}
