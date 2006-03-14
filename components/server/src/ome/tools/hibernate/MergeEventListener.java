/* ome.tools.hibernate.MergeEventListener
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
import org.hibernate.event.EventSource;
import org.hibernate.event.MergeEvent;
import org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener;

// Application-internal dependencies
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.CurrentDetails;
import ome.util.Utils;

/**
 * responsible for responding to all Hibernate Events. Delegates tasks to
 * various components. It is assumed that graphs coming to the Hibernate methods
 * which produces these events have already been processed by the UpdateFilter.
 */
public class MergeEventListener extends IdTransferringMergeEventListener
{

    private static Log log = LogFactory.getLog( MergeEventListener.class );

    @Override
    protected void entityIsTransient( MergeEvent event, Map copyCache )
    {
        super.entityIsTransient( event, copyCache );
        fillReplacement( event );
    }

    @Override
    protected void entityIsDetached(MergeEvent event, Map copyCache)
    {
        super.entityIsDetached( event, copyCache );
        fillReplacement( event );
    }
    
    // ~ Helpers
    // =========================================================================
    
    protected void fillReplacement( MergeEvent event )
    {
        if ( event.getOriginal() instanceof IObject)
        {
            IObject obj = (IObject)  event.getOriginal();
            if ( obj.getDetails() == null )
                obj.setDetails( new Details() );
            
            obj.getDetails().setReplacement( (IObject) event.getResult() );
            
        }
    }

}
