/*
 * org.openmicroscopy.shoola.env.data.model.AnalysisParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;

import java.util.ArrayList;
import java.util.List;


/** 
 * Holds information about the objects to analyze.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AnalysisParam
{

	/** Identifies the <code>Split View Figure</code> script. */
	public static final String FLIM_SCRIPT = 
		ScriptObject.ANALYSIS_PATH+"FLIM.py";
	
	/** Identifies a <code>FLIM</code> analysis. */
	public static final int FLIM = 0;
	
	/** Identifies a <code>FRAP</code> analysis. */
	public static final int FRAP = 1;
	
	/** The objects to analyze. */
	private List<Long> 		ids;
	
	/** The type of objects to analyze. */
	private Class 			nodeType;
	
	/** One of the constants defined by this class. */
	private int 			index;
	
	/** The channels to analyze. */
	private List<Integer>	channels;
	
	/**
	 * Controls if the passed analysis index is supported.
	 * 
	 * @param value The value to handle.
	 */
	private void checkIndex(int value)
	{
		switch (value) {
			case FLIM:
			case FRAP:
				break;
			default:
				throw new IllegalArgumentException("Analysis not supported.");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ids The objects to analyze.
	 * @param nodeType The type of object to analyze.
	 * @param index One of the constants defined by this class.
	 */
	public AnalysisParam(List<Long> ids, Class nodeType, int index)
	{
		checkIndex(index);
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("No objects to analyse.");
		this.index = index;
		this.ids = ids;
		this.nodeType = nodeType;
		channels = new ArrayList<Integer>();
	}
	
	/**
	 * Returns the index identifying the analysis.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the type of node to analyze.
	 * 
	 * @return See above.
	 */
	public Class getNodeType() { return nodeType; }
	
	/**
	 * Returns the objects to analyze.
	 * 
	 * @return See above.
	 */ 
	public List<Long> getIds() { return ids; }
	
	/**
	 * Returns the channels to analyze.
	 * 
	 * @return See above.
	 */
	public List<Integer> getChannels() { return channels; }
	
	/**
	 * Returns the channels to analyze.
	 * 
	 * @param channels The channels to analyze.
	 */
	public void setChannels(List<Integer> channels)
	{
		if (channels == null) return;
		this.channels = channels;
	}
	
}
