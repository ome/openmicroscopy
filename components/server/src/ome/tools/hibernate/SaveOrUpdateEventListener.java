/* ome.tools.hibernate.SaveOrUpdateEventListener
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

package ome.tools.hibernate;

// Java imports

// Third-party imports
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;

// Application-internal dependencies
import ome.model.IObject;
import ome.security.SecuritySystem;

/**
 */
public class SaveOrUpdateEventListener
        extends DefaultSaveOrUpdateEventListener
{

	private static final long serialVersionUID = -8107597969267192406L;

	private static Log log = LogFactory.getLog(SaveOrUpdateEventListener.class);

	protected SecuritySystem secSys;
	
	protected SaveEventSupport support;
	
    /**
     * main constructor. Replaces the default Hibernate merge listener with the
     * Spring IdTransferringMergeEventListener.
     */
    public SaveOrUpdateEventListener(SecuritySystem securitySystem)
    {
    	this.secSys = securitySystem;
    	this.support = new SaveEventSupport(securitySystem);
    }

    // 
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Catching server-side saves and updates.
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //

    @Override
    protected Serializable entityIsTransient(SaveOrUpdateEvent event) 
    throws HibernateException {
    	if ( ! secSys.allowCreation( (IObject) event.getObject() ))
    	{
    		secSys.throwCreationViolation( (IObject) event.getObject());
    	}
    	return super.entityIsTransient(event);
    }
    
    @Override
    protected void entityIsDetached(SaveOrUpdateEvent event)
    throws HibernateException {
    	if ( ! secSys.allowCreation( (IObject) event.getObject() ))
    	{
    		secSys.throwCreationViolation( (IObject) event.getObject());
    	}
    	super.entityIsDetached(event);
    }
    
}
