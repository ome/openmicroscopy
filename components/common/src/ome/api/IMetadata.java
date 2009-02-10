/*
 * ome.api.IMetadata 
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
package ome.api;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.model.core.LogicalChannel;

/** 
 * Provides method to interact with acquisition metadata and 
 * annotations.
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
public interface IMetadata 
	extends ServiceInterface
{

	/**
	 * Loads the <code>logical channels</code> and the acquisition metadata 
	 * related to them.
	 * 
	 * @param ids The collection of logical channel's ids. 
	 * 		      Mustn't be <code>null</code>.
	 * @return The collection of loaded logical channels.
	 */
	public Set<LogicalChannel> loadChannelAcquisitionData(@NotNull 
			@Validate(Long.class) Set<Long> ids);
	
}
