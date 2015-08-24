/*
 * org.openmicroscopy.shoola.env.data.model.AnalysisActivityParam 
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

import javax.swing.Icon;

/** 
 * Parameters required to analyze images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AnalysisActivityParam
{

	 /** The icon associated to the parameters. */
    private Icon		icon;
    
    /** The parameters to use.*/
    private Object 		param;
    
    /**
     * Creates a new instance.
     * 
     * @param param The parameters used to create the movie.
     * @param icon The icon to use to identify action.
     */
    public AnalysisActivityParam(Object param, Icon icon)
    {
    	if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		this.icon = icon;
    }
    
	/**
	 * Returns the icon if set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/**
	 * Returns the parameters.
	 * 
	 * @return See above.
	 */
	public Object getParameters() { return param; }

}
