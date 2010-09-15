/*
 * org.openmicroscopy.shoola.env.data.model.AnalysisResultsHandlingParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.graphutils.ChartObject;

/** 
 * Hosts the parameters necessary to display the results.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AnalysisResultsHandlingParam
{

	/** Indicates to display the results in an histogram. */
	public static final int HISTOGRAM = 0;
	
	/** The name of the X-axis. */
	private String nameXaxis;
	
	/** The name of the Y-axis. */
	private String nameYaxis;
	
	/** One of the constants defined by this class. */
	private int index;
	
	/** 
	 * Checks if the passed index is supported.
	 * 
	 * @param value The 
	 */
	private void checkIndex(int value)
	{
		switch (value) {
			case HISTOGRAM:
				return;
			default:
				throw new IllegalArgumentException("Index not supported");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index One of the constants defined by this class.
	 * @param nameXaxis Name of the X-axis.
	 * @param nameYaxis Name of the Y-axis.
	 */
	public AnalysisResultsHandlingParam(int index, 
			String nameXaxis, String nameYaxis)
	{
		checkIndex(index);
		this.index = index;
		if (nameXaxis == null || nameXaxis.trim().length() == 0)
			nameXaxis = ChartObject.X_AXIS;
		if (nameYaxis == null || nameYaxis.trim().length() == 0)
			nameYaxis = ChartObject.Y_AXIS;
		this.nameYaxis = nameYaxis;
		this.nameXaxis = nameXaxis;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index One of the constants defined by this class.
	 */
	public AnalysisResultsHandlingParam(int index)
	{
		this(index, null, null);
	}
	
	/**
	 * Returns the name of the X-axis.
	 * 
	 * @return See above.
	 */
	public String getNameXaxis() { return nameXaxis; }
	
	/**
	 * Returns the name of the Y-axis.
	 * 
	 * @return See above.
	 */
	public String getNameYaxis() { return nameYaxis; }
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }

}
