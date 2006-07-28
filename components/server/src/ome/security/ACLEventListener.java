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

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.tools.hibernate.EventListenersFactoryBean;



/**
 * responsible for intercepting all pre-INSERT, pre-UPDATE, pre-DELETE, and 
 * post-LOAD events to apply access control. For each event, a call is made
 * to the {@link SecuritySystem} to see if the event is allowed, and if not,
 * another call is made to the {@link  SecuritySystem} to throw a 
 * {@link SecurityViolation}.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see     SecuritySystem
 * @see		SecurityViolation
 * @see     EventListenersFactoryBean
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
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

    /** unused */ public void onPostDelete(PostDeleteEvent event) {}
    /** unused */ public void onPostInsert(PostInsertEvent event) {}
    /** unused */ public void onPostUpdate(PostUpdateEvent event) {}
    /** unused */ public void onPreLoad(PreLoadEvent event) {}

    /** catches all load events after the fact, and tests the current users
     * permissions to read that object. We have to catch the load after the fact
     * because the permissions information is stored in the db.
     */
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
