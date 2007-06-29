/*
 * org.openmicroscopy.shoola.agents.measurement.util.ObjectCreationTool 
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
package org.openmicroscopy.shoola.agents.measurement.util;


//Java imports

//Third-party libraries
import java.util.Map;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.CreationTool;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ObjectCreationTool
	extends CreationTool
	implements MeasureCreationTool
{	
	/** Reset the tool to the selection tool after figure creation. */
	private boolean resetToSelect = false;
	
	 /** Creates a new instance. */
    public ObjectCreationTool(String prototypeClassName) {
        super(prototypeClassName, null, null);
    }
    public ObjectCreationTool(String prototypeClassName, Map<AttributeKey, Object> attributes) {
        super(prototypeClassName, attributes, null);
    }
    public ObjectCreationTool(String prototypeClassName, Map<AttributeKey, Object> attributes, String name) {
       super(prototypeClassName, attributes, name);
    }
    public ObjectCreationTool(Figure prototype) {
        super(prototype, null, null);
    }
    /** Creates a new instance. */
    public ObjectCreationTool(Figure prototype, Map<AttributeKey, Object> attributes) {
        super(prototype, attributes, null);
    }
    /** Creates a new instance. */
    public ObjectCreationTool(Figure prototype, Map<AttributeKey, Object> attributes, String name) {
      super(prototype, attributes, name);
    }
    
    protected void creationFinished(Figure createdFigure) 
    {
        if(resetToSelect)
        	fireToolDone();
    }
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.MeasureCreationTool#isResetToSelect()
	 */
	public boolean isResetToSelect()
	{
		return resetToSelect;
	}
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.agents.measurement.util.MeasureCreationTool#setResetToSelect(boolean)
	 */
	public void setResetToSelect(boolean create)
	{
		resetToSelect = create;
	}
}


