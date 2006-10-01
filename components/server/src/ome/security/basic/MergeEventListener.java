/* ome.security.basic.MergeEventListener
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
import java.util.Map;

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.event.MergeEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.ForeignKeyDirection;
import org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener;
import org.springframework.util.Assert;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.SecurityViolation;
import ome.model.IEnum;
import ome.model.IObject;
import ome.tools.hibernate.HibernateUtils;

/**
 * responsible for responding to merge events. in particular in load/re-loading
 * certain types to make use by clients easier.
 * 
 * In general, enforces the detached-graph re-attachment "Commandments" as 
 * outlined in TODO. Objects that are transient (no ID) are unchanged; 
 * objects that are managed (with ID) are checked for validity (i.e. must have 
 * a version); and unloaded/filtered objects & collections are re-filled.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class MergeEventListener extends IdTransferringMergeEventListener
{

	public final static String MERGE_EVENT = "MergeEvent";
	
	private static final long serialVersionUID = 240558701677298961L;

	private static Log log = LogFactory.getLog( MergeEventListener.class );
	
	private BasicSecuritySystem secSys;
	
	/** main constructor. Requires a non-null security system */
	public MergeEventListener( BasicSecuritySystem securitySystem )
	{
		Assert.notNull(securitySystem);
		this.secSys = securitySystem;
	}
    
	@Override
	public void onMerge(MergeEvent event) throws HibernateException {
		if (secSys.isDisabled(MERGE_EVENT)) throw new SecurityViolation(
			"The MergeEventListener has been disabled.");
		super.onMerge(event);
	}
	
	@Override
	public void onMerge(MergeEvent event, Map copyCache) throws HibernateException {
		if (secSys.isDisabled(MERGE_EVENT)) throw new SecurityViolation(
			"The MergeEventListener has been disabled.");
		super.onMerge(event, copyCache);
	}
	
	@Override
	protected void copyValues(EntityPersister persister, Object entity, 
			Object target, SessionImplementor source, Map copyCache) {
		
		if (entity instanceof IObject)
		{
			HibernateUtils.fixNulledOrFilteredCollections(
					(IObject)entity,(IObject)target,persister,source);
			propagateHiddenValues((IObject)entity,(IObject)target);
		}
		super.copyValues(persister, entity, target, source, copyCache);
	}

	@Override
	protected void copyValues(EntityPersister persister, Object entity, 
			Object target, SessionImplementor source, Map copyCache, 
			ForeignKeyDirection foreignKeyDirection) {
		
		if (entity instanceof IObject)
		{
			HibernateUtils.fixNulledOrFilteredCollections(
					(IObject)entity,(IObject)target,persister,source);
			propagateHiddenValues((IObject)entity, (IObject)target);
		}
		super.copyValues(persister, entity, target, source, copyCache,
				foreignKeyDirection);
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
	    		log("Using existing Enum(",event.getEntityName()
	    				,") with value:",value);
	    		copyCache.put(event.getEntity(), extant);
	    		event.setResult(extant);
    		}
    	}
    	
    	// the above didn't succeed. process normally.
    	if ( extant == null )
    	{
			super.entityIsTransient( event, copyCache );
    	}
        fillReplacement( event );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void entityIsDetached(MergeEvent event, Map copyCache)
    {
    	IObject orig = (IObject) event.getOriginal();
    	if (HibernateUtils.isUnloaded( orig ))
    	{
           	final EventSource source = event.getSession();
    		log("Reloading unloaded entity:",event.getEntityName(),":", orig.getId());
    		Object obj = source.load( 
    						orig.getClass(), 
    						orig.getId());	
    		event.setResult(obj);
    		copyCache.put(event.getEntity(), obj);
    		fillReplacement( event );
    		return; //EARLY EXIT! 
    		// TODO this was maybe a bug. check if findDirty is superfluous.
    	}
    	    	
    	else 
    	{
    		super.entityIsDetached( event, copyCache );
    	}
        fillReplacement( event );
    }
    
    // ~ Helpers
    // =========================================================================
    
    protected void fillReplacement( MergeEvent event )
    {
        if ( event.getOriginal() instanceof IObject)
        {
            IObject obj = (IObject)  event.getOriginal();
            obj.getGraphHolder().setReplacement( (IObject) event.getResult() );
        }
    }
    
	protected void propagateHiddenValues(IObject from, IObject to) {
		secSys.copyToken(from,to);
		if ( from.getDetails() != null && from.getDetails().filteredSize() > 0 )
		{
			to.getDetails().addFiltered(from.getDetails().filteredSet());
		}
	}

	private void log(Object...objects)
	{
		if ( log.isDebugEnabled() && objects != null && objects.length > 0)
		{
			StringBuilder sb = new StringBuilder(objects.length*16);
			for (Object obj : objects) {
				sb.append(obj.toString());
			}
			log.debug(sb.toString());
		}
	}
}
