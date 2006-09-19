/* ome.tools.hibernate.Reloader
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
import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.event.MergeEvent;
import org.hibernate.event.RefreshEvent;
import org.hibernate.event.RefreshEventListener;
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
 * responsible for reloading {@link IObject#unload() unloaded} entities and 
 * nulled collections.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0-M3
 * @see <a href="http://cvs.openmicroscopy.org.uk/tiki/tiki-index.php?page=Omero+Object+Model">Object Model</a>
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public abstract class Reloader 
{ 

	private static Log log = LogFactory.getLog(Reloader.class);
	
    // ~ Static methods
    // =========================================================================

    public static boolean isUnloaded( Object original )
    {
		if ( original != null 
				&& original instanceof IObject 
				&& ! ((IObject) original).isLoaded()) {
			return true;
		}
		return false;
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
	public static void fixNulledOrFilteredCollections(IObject entity, 
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
	protected static Collection copy(PersistentCollection c)
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
	
	private static void log(Object...objects)
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
