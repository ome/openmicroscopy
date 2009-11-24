/*
 * org.openmicroscopy.shoola.env.data.model.FigureActivityParam 
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


//Java imports
import java.util.List;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class storing information about the figure to create.
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
public class FigureActivityParam
{

	/** Identifies the split view figure. */
	public static final int	SPLIT_VIEW_FIGURE = 0;
	
    /** The icon associated to the parameters. */
    private Icon		icon;
    
    /** The parameters to use.*/
    private Object 		param;
    
    /** The selected objects. */
    private List<Long>	ids;
    
    /** The type of objects to handle. */
    private Class       objectType;
    
    /** The type of figure. */
    private int 		type;
    
    /**
     * Creates a new instance.
     * 
     * @param param The parameters used to create the movie.
     * @param ids	The selected objects.
     * @param objectType The type of objects to handle.
     * @param type	The type of figure.
     */
	public FigureActivityParam(Object param, List<Long>	ids, 
			Class objectType, int type)
	{
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("Objects not valid.");
		if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		this.type = type;
		this.ids = ids;
		this.objectType = objectType;
	}
	
    /**
	 * Sets the icon associated to the activity.
	 * 
	 * @param icon The value to set.
	 */
	public void setIcon(Icon icon) { this.icon = icon; }
	
	/**
	 * Returns the icon if set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/**
	 * Returns the selected objects.
	 * 
	 * @return See above.
	 */
	public List<Long> getIds() { return ids; }
	
	/**
	 * Returns the type of object to handle.
	 * 
	 * @return See above.
	 */
	public Class getObjectType() { return objectType; }
	
	/**
	 * Returns the parameters.
	 * 
	 * @return See above.
	 */
	public Object getParameters() { return param; }
	
	/**
	 * Returns the type of figure.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
}
