 /*
 * org.openmicroscopy.shoola.agents.editor.model.IFieldContent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The interface for components that make up the content of a {@link Field};
 * By extending the {@link IAttributes} interface, the text can be edited
 * using a "Parameter-editing" UI component. 
 * 
 * Implemented by {@link TextContent}
 * Extended by {@link IParam} 
 * 
 * @see IParam
 * @see TextContent
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface IFieldContent 
	extends IAttributes {
	
	/**
	 * Allows String representation to be displayed by UI. 
	 * 
	 * @return
	 */
	public String toString();

}
