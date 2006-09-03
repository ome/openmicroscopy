/*
 * ome.logic.AbstractLevel2Service
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;

/**
 * service level 2
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel2Service extends AbstractBean 
{

    protected transient LocalUpdate iUpdate;
    
    protected transient LocalQuery iQuery;
    
    // ~ Selfconfiguration (injection) for Non-JavaEE
	// =========================================================================
    
    public final void setUpdateService(LocalUpdate update)
    {
    	throwIfAlreadySet(this.iUpdate, update);
        this.iUpdate = update;
    }
    
    public final void setQueryService(LocalQuery query)
    {
    	throwIfAlreadySet(this.iQuery, query);
        this.iQuery = query;
    }

}

