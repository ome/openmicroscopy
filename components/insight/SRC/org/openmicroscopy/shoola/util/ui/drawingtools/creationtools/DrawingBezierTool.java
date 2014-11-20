/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.creationtools.DrawingBezierTool 
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
package org.openmicroscopy.shoola.util.ui.drawingtools.creationtools;

//Java imports
import java.util.Map;

//Third-party libraries


import org.jhotdraw.draw.AttributeKey;
//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.BezierTextFigure;

/** 
 * A Bezier figure with text.
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
public class DrawingBezierTool 
	extends BezierTool 
	implements DrawingCreationTool
{
	
	/** Reset the tool to the selection tool after figure creation. */
	private boolean	resetToSelect;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param prototype 	The prototype. Mustn't be <code>null</code>.
	 */
	public DrawingBezierTool(BezierTextFigure prototype)
	{
		this(prototype, null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param prototype 	The prototype. Mustn't be <code>null</code>.
	 * @param attributes	The attributes for the new figures.
	 */
	public DrawingBezierTool(BezierTextFigure prototype, Map attributes)
	{
		super(prototype, attributes);
		
	}

	/**
     * Sets the attributes.
     *
     * @param attributes The CreationTool applies these attributes to the
     * prototype after having applied the default attributes from the DrawingEditor.
     */
    public void setAttributes(Map<AttributeKey, Object> attributes)
    {
        this.attributes = attributes;
    }

	/** 
	 * Overridden to only fired event if the {@link #resetToSelect} is
	 * <code>true</code>.
	 * @see BezierTool#fireToolDone()
	 */
	protected void fireToolDone()
	{ 
		if (!resetToSelect) return;
	}

	/**
	 * Implemented as specified by the {@link DrawingCreationTool} I/F.
	 * @see DrawingCreationTool#isResetToSelect()
	 */
	public boolean isResetToSelect() { return resetToSelect; }
	
	/**
	 * Implemented as specified by the {@link DrawingCreationTool} I/F.
	 * @see DrawingCreationTool#setResetToSelect(boolean)
	 */
	public void setResetToSelect(boolean create)
	{
		resetToSelect = create;
	}
	
}
