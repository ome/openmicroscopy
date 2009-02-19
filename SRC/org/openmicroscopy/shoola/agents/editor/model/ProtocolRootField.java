 /*
 * org.openmicroscopy.shoola.agents.editor.model.ProtocolRootField 
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
package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This extends {@link Field} to provide for the need to store additional info
 * in the root of the protocol. E.g. experimental info. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ProtocolRootField 
	extends Field {

	/** A reference to the experiment info, contained in the root field */
	private IAttributes 		experimentInfo;

	/** Sets the experiment info */
	public void setExpInfo(IAttributes expInfo) 
	{
		experimentInfo = expInfo;
	}
	
	/**
	 * Gets a reference to the experiment info. 
	 * 
	 * @return	see above. 
	 */
	public IAttributes getExpInfo() { return experimentInfo; }

}
