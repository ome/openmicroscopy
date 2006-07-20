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
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.event.MergeEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener;
import org.springframework.util.Assert;

// Application-internal dependencies
import ome.conditions.InternalException;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.SecuritySystem;

/**
 * responsible for responding to all Hibernate Events. Delegates tasks to
 * various components. It is assumed that graphs coming to the Hibernate methods
 * which produces these events have already been processed by the UpdateFilter.
 */
public class MergeEventListener extends IdTransferringMergeEventListener
{

	private static final long serialVersionUID = 240558701677298961L;

	private static Log log = LogFactory.getLog( MergeEventListener.class );
	
	private SecuritySystem secSys;
	
	private SaveEventSupport support;
	
	/** main constructor. Requires a non-null security system */
	public MergeEventListener( SecuritySystem securitySystem )
	{
		Assert.notNull(securitySystem);
		this.secSys = securitySystem;
		this.support = new SaveEventSupport(securitySystem);
	}
    
    @Override
    @SuppressWarnings({"cast","unchecked"})
    protected void entityIsTransient( MergeEvent event, Map copyCache )
    {
    	Class cls = event.getOriginal().getClass();
    	IEnum extant = null;
    	if ( IEnum.class.isAssignableFrom( cls ))
    	{
    		String value = ((IEnum) event.getOriginal()).getValue();
    		Class  type  = ((IEnum) event.getOriginal()).getClass();
    		Criteria c =
    		event.getSession().createCriteria(type)
    			.add(Restrictions.eq("value",value));
    		extant = (IEnum) c.uniqueResult();
    		if (null != extant)
    		{
	    		log.warn("Using existing Enum("
	    				+event.getEntityName()
	    				+") with value:"
	    				+value);
	    		copyCache.put(event.getEntity(), extant);
	    		event.setResult(extant);
    		}
    	}
    	
    	// the above didn't succeed. process normally.
    	if ( extant == null )
    	{
    		if ( ! secSys.allowCreation( (IObject) event.getOriginal() ) )
    		{
    			secSys.throwCreationViolation( (IObject) event.getOriginal() );
    		}
    	
    		else 
    		{
    			super.entityIsTransient( event, copyCache );
    		}
    	}
        fillReplacement( event );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void entityIsDetached(MergeEvent event, Map copyCache)
    {
    	IObject orig = (IObject) event.getOriginal();
    	if (isUnloaded( orig ))
    	{
           	final EventSource source = event.getSession();
    		log.warn("Reloading unloaded entity in MergeEventListener.\n" +
    				 "Not caught by UpdateFilter: "+
    				 event.getEntityName()+":"+event.getRequestedId());
    		throw new InternalException("Filter didn't catch unloaded:"+orig);
//    		Object obj = source.load( 
//    						orig.getClass(), 
//    						orig.getId());	
//    		event.setResult(obj);
//    		copyCache.put(event.getEntity(), obj);
//    		fillReplacement( event );
//    		return; //EARLY EXIT! 
    		// TODO this was maybe a bug. check if findDirty is superfluous.
    	}
    	
    	else if ( ! secSys.allowUpdate( (IObject) event.getOriginal() ))
    	{
    		secSys.throwUpdateViolation( (IObject) event.getOriginal() );
    	}
    	
    	else 
    	{
    		super.entityIsDetached( event, copyCache );
    	}
        fillReplacement( event );
    }
    
    // ~ Helpers
    // =========================================================================

    protected boolean isUnloaded( Object original )
    {
		if ( original != null 
				&& original instanceof IObject 
				&& ! ((IObject) original).isLoaded()) {
			return true;
		}
		return false;
    }
    
    protected void fillReplacement( MergeEvent event )
    {
        if ( event.getOriginal() instanceof IObject)
        {
            IObject obj = (IObject)  event.getOriginal();
            obj.getGraphHolder().setReplacement( (IObject) event.getResult() );
        }
    }

}
