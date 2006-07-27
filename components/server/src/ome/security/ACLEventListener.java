/* ome.security.ACLEventListener
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

package ome.security;

// Java imports

// Third-party imports
import java.util.Iterator;

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Details;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreDeleteEvent;
import org.hibernate.event.PreDeleteEventListener;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreLoadEvent;
import org.hibernate.event.PreLoadEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.def.DefaultPostLoadEventListener;
import org.hibernate.event.def.DefaultPreLoadEventListener;

// Application-internal dependencies


/**
 * responsible for responding to all Hibernate Events. Delegates tasks to
 * various components. It is assumed that graphs coming to the Hibernate methods
 * which produces these events have already been processed by the 
 * {@link ome.tools.hibernate.UpdateFilter}
 */
public class ACLEventListener
        implements 
        /* BEFORE... */ PreDeleteEventListener, PreInsertEventListener,
        /* and...... */ PreLoadEventListener, PreUpdateEventListener,
        /* AFTER.... */ PostDeleteEventListener, PostInsertEventListener,
        /* TRIGGERS. */ PostLoadEventListener, PostUpdateEventListener 
{

	private static final long serialVersionUID = 3603644089117965153L;

	private static Log log = LogFactory.getLog(ACLEventListener.class);

	private SecuritySystem secSys;
	
    /**
     * main constructor. controls access to individual db rows..
     */
    public ACLEventListener(SecuritySystem securitySystem)
    {
        this.secSys = securitySystem;
    }

    // 
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Acting as all hibernate triggers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //

    // Unused
    public void onPostDelete(PostDeleteEvent event) {}
    public void onPostInsert(PostInsertEvent event) {}
    public void onPostUpdate(PostUpdateEvent event) {}
    public void onPreLoad(PreLoadEvent event) {}

    // have to use post load because preload doesn't contain db information.
    public void onPostLoad(PostLoadEvent event)
    {
    	Object entity = event.getEntity();
    	if ( entity instanceof IObject )
    	{
    		IObject obj = (IObject) entity;
            if ( ! secSys.allowLoad(obj.getClass(),obj.getDetails()))
            {
            	secSys.throwLoadViolation(obj);
            }
    	}
    }
    
    public boolean onPreInsert(PreInsertEvent event)
    {
    	Object entity = event.getEntity();
		if ( entity instanceof IObject )
		{
			IObject obj = (IObject) entity;
	        if ( ! secSys.allowCreation(obj) )
	        {
	        	secSys.throwCreationViolation(obj);
	        }
		}
        return false;
    }

    public boolean onPreUpdate(PreUpdateEvent event)
    {
    	Object entity = event.getEntity();
    	Object[] state = event.getOldState(); 
    	String[] names = event.getPersister().getPropertyNames();
		if ( entity instanceof IObject )
		{
			IObject obj = (IObject) entity;
	        if ( ! secSys.allowUpdate(obj, getDetails(state, names)) )
	        {
	        	secSys.throwUpdateViolation(obj);
	        }
		}
        return false;
    }
    
    public boolean onPreDelete(PreDeleteEvent event)
    {
    	Object entity = event.getEntity();
    	Object[] state = event.getDeletedState();
    	String[] names = event.getPersister().getPropertyNames();
		if ( entity instanceof IObject )
		{
			IObject obj = (IObject) entity;
	        if ( ! secSys.allowDelete(obj,getDetails(state,names)) )
	        {
	        	secSys.throwDeleteViolation(obj);
	        }
		}
        return false;
    }
    
    // ~ Helpers
	// =========================================================================
    
    private Details getDetails( Object[] state, String[] names)
    {
    	for (int i = 0; i < names.length; i++) {
			if ("details".equals( names[i] ))
			{
				return (Details) state[i];
			}		
		}
    	throw new InternalException("No details found in state argument.");
    }
    
}
