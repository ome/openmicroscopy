/* ome.tools.hibernate.SaveEventSupport
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
import java.util.Map;

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.EventSource;
import org.hibernate.event.MergeEvent;
import org.hibernate.event.SaveOrUpdateEvent;
import org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener;
import org.springframework.util.Assert;

// Application-internal dependencies
import ome.model.IEnum;
import ome.model.IObject;
import ome.security.SecuritySystem;

/**
 */
class SaveEventSupport
{
    
	private SecuritySystem secSys;
	
	public SaveEventSupport(SecuritySystem securitySystem)
	{
		this.secSys = securitySystem;
	}
	

	boolean canReload( AbstractEvent event )
	{
		return true;
	}

    boolean isUnloaded( Object original )
    {
		if ( original != null 
				&& original instanceof IObject 
				&& ! ((IObject) original).isLoaded()) {
			return true;
		}
		return false;
    }
    
    void fillReplacement( MergeEvent event )
    {
        if ( event.getOriginal() instanceof IObject)
        {
            IObject obj = (IObject)  event.getOriginal();
            obj.getGraphHolder().setReplacement( (IObject) event.getResult() );
        }
    }

}
