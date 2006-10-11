/*
 * ome.api.StatefulServiceInterface
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

package ome.api;

import ome.conditions.ApiUsageException;
import ome.model.meta.Event;
import ome.system.EventContext;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * OMERO API Interface with stateful semantics. 
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME3.0
 */
public interface StatefulServiceInterface extends ServiceInterface { 
    
    /** signals the beginning of the service lifecycle. */ 
    void create();
    
    /** signals the end of the service lifecycle. Resources such as Sessions
     * can be released. All further calls will throw an ApiUsageException.
     * @throws ApiUsageException
     */
    void destroy() throws ApiUsageException;

	/** Returns the current {@link EventContext} for this instance. This is
	 * useful for later identifying changes made by this {@link Event}.
	 */
    EventContext getCurrentEventContext();
}
