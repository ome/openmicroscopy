/* ome.security.basic.EventLogListener
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
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;

// Application-internal dependencies
import ome.model.IObject;


/**
 * responsible for responding to all Hibernate Events. Delegates tasks to
 * various components. It is assumed that graphs coming to the Hibernate methods
 * which produces these events have already been processed by the 
 * {@link ome.tools.hibernate.UpdateFilter}
 */
public class EventLogListener
implements PostUpdateEventListener, PostDeleteEventListener, PostInsertEventListener
{
        

	private static final long serialVersionUID = 3245068515908082533L;

	private static Log                   log       = LogFactory
                                                           .getLog(EventLogListener.class);
    
    protected BasicSecuritySystem			secSys;
    
    /**
     * main constructor. 
     */
    public EventLogListener(BasicSecuritySystem securitySystem)
    {
    	this.secSys = securitySystem;
    }

    // 
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Acting as all hibernate triggers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //

    public void onPostDelete(PostDeleteEvent event)
    {
    	add("DELETE",event.getEntity());
    }

    public void onPostInsert(PostInsertEvent event)
    {
    	add("INSERT",event.getEntity());
    }

    public void onPostUpdate(PostUpdateEvent event)
    {
    	add("UPDATE",event.getEntity());
    }

    // ~ Helpers
	// =========================================================================
    
    void add(String action, Object entity)
    {
    	if (entity instanceof IObject)
    	{
    		Class klass = entity.getClass();
    		Long id = ((IObject) entity).getId();
    		secSys.addLog(action, klass, id);
    	}
    }
    
}
