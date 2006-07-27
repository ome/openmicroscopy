/*
 * ome.api.local.LocalAdmin
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.api.local;

// Java imports
import java.util.List;

import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

// Third-party libraries

// Application-internal dependencies


/**
 * Provides local (internal) extensions for administration
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OMERO3.0
 */
public interface LocalAdmin extends ome.api.IAdmin {
    
	/** 
	 * returns a possibly uninitialized proxy for the given 
	 * {@link Experimenter#getOmeName() user name}. Use of the 
	 * {@link Experimenter} instance will initial its values.
	 */
	Experimenter userProxy( String omeName );
	
	/** 
	 * returns a possibly uninitialized proxy for the given 
	 * {@link Experimenter#getId() user id}. Use of the 
	 * {@link Experimenter} instance will initial its values.
	 */
	Experimenter userProxy( Long userId );
	
	/** 
	 * returns a possibly uninitialized proxy for the given 
	 * {@link ExperimenterGroup#getId() group id}. Use of the 
	 * {@link Experimenter} instance will initial its values.
	 */
	ExperimenterGroup groupProxy( Long groupId );
	
	
	/** 
	 * returns a possibly uninitialized proxy for the given 
	 * {@link ExperimenterGroup#getName() group name}. Use of the 
	 * {@link Experimenter} instance will initial its values.
	 */
	ExperimenterGroup groupProxy( String groupName );
	
	/** 
     * Finds the ids for all groups for which the given {@link Experimenter}
     * is owner/leader.
     * 
     * @param e Non-null, managed (i.e. with id) {@link Experimenter}
     * @see ExperimenterGroup#getDetails()
     * @see Details#getOwner()
     */
    List<Long> getLeaderOfGroupIds( Experimenter e );

}
