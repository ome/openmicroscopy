/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;

import java.sql.Timestamp;
import java.util.List;

import omero.gateway.model.ExperimenterData;

/** 
 * Helper class hosting the context of a data search.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class SearchDataContext
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
	
	/** Identifying the <code>ID</code> context. */
	public static final int                 ID = 8;
	
	/** Indicates to set the creation time interval. */
	public static final int			CREATION_TIME = 0;
	
	/** Indicates to set the modification time interval. */
	public static final int			MODIFICATION_TIME = 1;
	
	/** Indicates to set the time of annotation. */
	public static final int			ANNOTATION_TIME = 2;

	/** 
	 * Indicates to exclude the collection of users who owned entities.
	 */
	public static final int			EXCLUDE_OWNER = 102;
	
	/** 
	 * Indicates to exclude the collection of users who annotated entities.
	 */
	public static final int			EXCLUDE_ANNOTATOR = 103;
	
	/** One the time constants defined by this class. */
	private int						timeIndex;
	
	/** 
	 * Set to <code>true</code> if it is an union of contexts, or
	 * <code>false</code> if it is an intersection of contexts.
	 */
	private boolean 				unionOfContexts;

	/** 
	 * Set to <code>true</code> if the case is taken into account, 
	 * <code>false</code> otherwise.
	 */
	private boolean 				caseSensitive;
	
	/** The lower bound of the time interval. */
	private Timestamp 				start;
	
	/** The upper bound of the time interval. */
	private Timestamp 				end;
	
	/** The scope of the search. Mustn't not be <code>null</code>. */
	private List<Integer>			scope;
	
	/** The types to search on. */
	private List<Class>				types;
	
	/** 
	 * Some (at least one) of these terms must be present in the document. 
	 * May be <code>null</code>.
	 */
	private String[]				some;
	
	/**
	 * All of these terms must be present in the document.
	 * May be <code>null</code>.
	 */
	private String[]				must;
	
	/** 
	 * None of these terms may be present in the document. 
	 * May be <code>null</code>.
	 */
	private String[]				none;
	
	/** Collection of experimenters to restrict the search on.*/ 
	private List<ExperimenterData>	owners;

	/** Collection of experimenters to restrict the search on.*/ 
	private List<ExperimenterData>	annotators;
	
	/** Collection of experimenters to restrict the search on.*/ 
	private List<ExperimenterData>	excludedOwners;

	/** Collection of experimenters to restrict the search on.*/ 
	private List<ExperimenterData>	excludedAnnotators;
	
	/** The number of results returned. */
	private int						numberOfResults;

	/** The collection of groups to perform the search on.*/
	private List<Long> groups;
	
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
	public SearchDataContext(List<Integer> scope, List<Class> types, 
							String[] some, String[] must, String[] none)
	{
		//if (some == null && must == null && none == null)
		//	throw new IllegalArgumentException("No terms to search for.");
		this.some = some;
		this.must = must;
		this.none = none;
		this.scope = scope;
		this.types = types;
		numberOfResults = -1;
		unionOfContexts = true;
	}
	
	/**
	 * Sets the groups to search into.
	 * 
	 * @param groups The groups.
	 */
	public void setGroups(List<Long> groups) { this.groups = groups; }
	
	/**
	 * Returns the groups to search into.
	 * 
	 * @return See above.
	 */
	public List<Long> getGroups() { return groups; }
	
	
	/** 
	 * Sets to <code>true</code> if it is an union of contexts, or
	 * <code>false</code> if it is an intersection of contexts.
	 * 
	 * @param unionOfContexts The value to set.
	 */
	public void setUnionOfContexts(boolean unionOfContexts)
	{
		this.unionOfContexts = unionOfContexts;
	}
	
	/** 
	 * Returns <code>true</code> if it is an union of contexts, or
	 * <code>false</code> if it is an intersection of contexts.
	 * 
	 * @return See above.
	 */
	public boolean isUnionOfContexts() { return unionOfContexts; }
	
	/**
	 * Returns <code>true</code> if the case is taken into account, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isCaseSensitive() { return caseSensitive; }

	/**
	 * Sets to <code>true</code> if the case is taken into account, 
	 * to <code>false</code> otherwise.
	 * 
	 * @param caseSensitive The value to set.
	 */
	public void setCaseSensitive(boolean caseSensitive)
	{
		this.caseSensitive = caseSensitive;
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
	public List<Class> getTypes() { return types; }
	
	/**
	 * Returns the terms that might be present in the document. 
	 * May be <code>null</code>.
	 * 
	 * @return See above.
	 */
	public String[] getSome() { return some; }
	
	/**
	 * Returns the terms that must present in the document. 
	 * May be <code>null</code>.
	 * 
	 * @return See above.
	 */
	public String[] getMust() { return must; }
	
	/**
	 * Returns the terms that cannot be present in the document. 
	 * May be <code>null</code>.
	 * 
	 * @return See above.
	 */
	public String[] getNone() { return none; }
	
	/**
	 * Returns <code>true</code> if the context of the search is valid i.e.
	 * parameters correctly set, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isValid()
	{
		return !(none == null && must == null && some == null && start == null
			&& end == null);
	}
	
	/**
	 * Sets the time index. One of the time constants defined by this class.
	 * 
	 * @param index The value to set.
	 */
	public void setTimeIndex(int index)
	{
		switch (index) {
			case CREATION_TIME:
			case MODIFICATION_TIME:
				timeIndex = index;
				break;
			default:
				timeIndex = -1;;
		}
	}
	
	/**
	 * Returns the time index.
	 * 
	 * @return See above.
	 */
	public int getTimeIndex() { return timeIndex; }
	
	/**
	 * Returns the collection of users or <code>null</code>
	 * if none specified.
	 * 
	 * @return See above.
	 */
	public List<ExperimenterData> getOwners() { return owners; }
	
	/**
	 * Returns the collection of users or <code>null</code>
	 * if none specified.
	 * 
	 * @return See above.
	 */
	public List<ExperimenterData> getAnnotators() { return annotators; }

	/**
	 * Returns the collection of users or <code>null</code>
	 * if none specified.
	 * 
	 * @return See above.
	 */
	public List<ExperimenterData> getExcludedOwners() { return excludedOwners; }
	
	/**
	 * Returns the collection of users or <code>null</code>
	 * if none specified.
	 * 
	 * @return See above.
	 */
	public List<ExperimenterData> getExcludedAnnotators()
	{ 
		return excludedAnnotators;
	}
	
	/**
	 * Sets the collection of users who owns the data.
	 * 
	 * @param users The collection to set.
	 */
	public void setOwners(List<ExperimenterData> users) { owners = users; }
	
	/**
	 * Sets the collection of users who owns the data.
	 * 
	 * @param users The collection to set.
	 */
	public void setExcludedOwners(List<ExperimenterData> users)
	{ 
		excludedOwners = users; 
	}
	
	/**
	 * Sets the collection of users who owns the data.
	 * 
	 * @param users The collection to set.
	 */
	public void setAnnotators(List<ExperimenterData> users)
	{ 
		annotators = users; 
	}
	
	/**
	 * Sets the collection of users who owns the data.
	 * 
	 * @param users The collection to set.
	 */
	public void setExcludedAnnotators(List<ExperimenterData> users)
	{ 
		excludedAnnotators = users; 
	}
	
	/**
	 * Sets the number of results returned.
	 * 
	 * @param results The value to set.
	 */
	public void setNumberOfResults(int results)
	{
		numberOfResults = results;
	}
	
	/**
	 * Returns the number of results.
	 * 
	 * @return See above.
	 */
	public int getNumberOfResults() { return numberOfResults; }
	
	/**
	 * Returns <code>true</code> if text to search for,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasTextToSearch()
	{
		return (some != null || none != null || must != null);
	}
	
}
