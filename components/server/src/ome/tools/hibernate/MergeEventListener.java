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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.event.MergeEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener;
import org.springframework.util.Assert;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.InternalException;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.SecuritySystem;
import ome.tools.lsid.LsidUtils;

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

	private static final long serialVersionUID = 240558701677298961L;

	private static Log log = LogFactory.getLog( MergeEventListener.class );
	
	private SecuritySystem secSys;
	
	/** main constructor. Requires a non-null security system */
	public MergeEventListener( SecuritySystem securitySystem )
	{
		Assert.notNull(securitySystem);
		this.secSys = securitySystem;
	}
    
	@Override
	protected void copyValues(EntityPersister persister, Object entity, 
			Object target, SessionImplementor source, Map copyCache) {
		
		if (entity instanceof IObject)
		{
			fixNulledOrFilteredCollections((IObject)entity,(IObject)target,persister,source);
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
			fixNulledOrFilteredCollections((IObject)entity,(IObject)target,persister,source);
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
    	if (isUnloaded( orig ))
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
    
	protected void propagateHiddenValues(IObject from, IObject to) {
		secSys.copyToken(from,to);
		if ( from.getDetails().filteredSize() > 0 )
		{
			to.getDetails().addFiltered(from.getDetails().filteredSet());
		}
	}
	
	/** loads collections which have been filtered or nulled by the user 
	 * 
	 * @param entity IObject to have its collections reloaded
	 * @param id persistent (db) id of this entity
	 * @param currentState the possibly changed field data for this entity
	 * @param previousState the field data as seen in the db
	 * @param propertyNames field names
	 * @param types Hibernate {@link Type} for each field
	 * @param detailsIndex the index of the {@link Details} instance (perf opt)
	 */
	protected void fixNulledOrFilteredCollections(IObject entity, 
			IObject target, EntityPersister persister, SessionImplementor source)
	{
		
		Object[] currentState  = persister.getPropertyValues( entity, source.getEntityMode() );
		Object[] previousState = persister.getPropertyValues( target, source.getEntityMode() );
		String[] propertyNames = persister.getPropertyNames();
		Type[] types   = persister.getPropertyTypes();

		int detailsIndex = OmeroInterceptor.detailsIndex(propertyNames);
		Details d = (Details) currentState[detailsIndex];
		if ( d != null )
		{
			Set<String> s = d.filteredSet();
			for (String string : s) {
				string = LsidUtils.parseField(string);
				int idx = OmeroInterceptor.index(string,propertyNames);
				Object previous = previousState[idx];
				if ( ! (previous instanceof PersistentCollection) ) // implies not null
				{
					throw new InternalException(String.format(
							"Invalid collection found for filtered " +
							"field %s in previous state for %s",
							string,entity));
				}
				log("Copying filtered collection ",string);
				Collection copy = copy(((PersistentCollection)previous));
				persister.setPropertyValue(entity,idx,copy,source.getEntityMode());
			}
		}
		
		for (int i = 0; i < types.length; i++) {
			Type t = types[i];
			if ( t.isCollectionType() && null == currentState[i] )
			{
				Object previous = previousState[i];
				if ( ! (previous instanceof Collection) ) // implies not null
				{
					throw new InternalException(String.format(
							"Invalid collection found for null " +
							"field %s in previous state for %s",
							propertyNames[i],entity));
				}
				log("Copying nulled collection ",propertyNames[i]);
				Collection copy = copy(((PersistentCollection)previous));
				persister.setPropertyValue(entity,i,copy,source.getEntityMode());
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Collection copy(PersistentCollection c)
	{
		if (c instanceof Set)
		{
			return new HashSet((Set)c);
		} 
		
		else if (c instanceof List)
		{
			return new ArrayList((List)c);
		}
		
		else 
			throw new InternalException("Unsupported collection type:"+
					c.getClass().getName());
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
