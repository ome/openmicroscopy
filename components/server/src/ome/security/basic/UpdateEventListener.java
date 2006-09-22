/* ome.security.basic.UpdateEventListener
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

package ome.security.basic;

// Java imports

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.basic.BasicSecuritySystem;

/**
 * responsible for setting the {@link Details#setUpdateEvent(ome.model.meta.Event) updat event}
 * on all events shortly before being saved.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see     BasicSecuritySystem
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class UpdateEventListener implements PreUpdateEventListener
{

	public final static String UPDATE_EVENT = "UpdateEvent";
	
	private static final long serialVersionUID = -7607753637653567889L;

	private static Log log = LogFactory.getLog(UpdateEventListener.class);

	private BasicSecuritySystem secSys;
	
    /**
     * main constructor. controls access to individual db rows..
     */
    public UpdateEventListener(BasicSecuritySystem bss)
    {
    	this.secSys = bss;
    }

    /** updates the update event field of an {@link IObject} instance.
     * 
     */
    public boolean onPreUpdate(PreUpdateEvent event)
    {
    	Object entity = event.getEntity();
		if ( entity instanceof IObject && ! secSys.isDisabled(UPDATE_EVENT))
		{
	    	int[] dirty = event.getPersister().findDirty(
	    				event.getState(),
	    				event.getOldState(),
	    				event.getEntity(),
	    				event.getSource());
	    	if (dirty == null||dirty.length==0) 
	    	{
	    		// return true; // veto.
	    	} 
	    	
	    	else 
	    	{
		    	// otherwise change update event (last modification)
				IObject obj = (IObject) entity;
		        obj.getDetails().setUpdateEvent( secSys.currentEvent() );
	    	}
		}
        return false;
    }

}
