/* ome.tools.hibernate.HibernateUtils
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
import java.util.Set;

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EntityMode;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.tools.lsid.LsidUtils;

/**
 * contains methods for reloading {@link IObject#unload() unloaded} entities and 
 * nulled collections as well as determining the index of certain properties in
 * a dehydrated Hiberante array.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0-M3
 * @see <a href="http://cvs.openmicroscopy.org.uk/tiki/tiki-index.php?page=Omero+Object+Model">Object Model</a>
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public abstract class HibernateUtils 
{ 

	private static Log log = LogFactory.getLog(HibernateUtils.class);

	// using Image as an example. All details fields are named the same.
	private static String DETAILS = LsidUtils.parseField( Image.DETAILS ); 
	
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

		int detailsIndex = detailsIndex(propertyNames);
		Details d = (Details) currentState[detailsIndex];
		if ( d != null )
		{
			Set<String> s = d.filteredSet();
			for (String string : s) {
				string = LsidUtils.parseField(string);
				int idx = index(string,propertyNames);
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
    
    /** calculates if only the {@link Flag#LOCKED} marker has 
     * been changed. If not, the normal criteria apply.
     */
    public static boolean onlyLockChanged( SessionImplementor session,
    		EntityPersister persister, IObject entity, Object[] state, String[] names )
    {
    	
    	Object[] current = persister.getPropertyValues(entity, EntityMode.POJO);
    	int[] dirty = persister.findDirty( state, current, entity, session);

		if ( dirty != null ) 
		{
			if ( dirty.length > 1 ) return false;
			if ( ! DETAILS.equals( names[dirty[0]] )) return false;
			Details new_d = getDetails(current, names);
			Details old_d = getDetails(state, names);
			if ( new_d.getOwner() != old_d.getOwner() ||
					new_d.getGroup() != old_d.getGroup() ||
					new_d.getCreationEvent() != old_d.getCreationEvent() ||
					new_d.getUpdateEvent() != old_d.getUpdateEvent() )
				return false;
			Permissions new_p = new Permissions( new_d.getPermissions() );
			Permissions old_p = new Permissions( old_d.getPermissions() );
			old_p.set( Flag.LOCKED );
			return new_p.identical( old_p );
		}
		return false;
		
    }

    public static Details getDetails( Object[] state, String[] names)
    {
		return (Details) state[detailsIndex(names)];
    }
    
    public static int detailsIndex( String[] propertyNames )
    {
    	return index( DETAILS, propertyNames );
    }
    
    public static int index( String str, String[] propertyNames )
    {
        for (int i = 0; i < propertyNames.length; i++)
        {
            if ( propertyNames[i].equals( str ))
                return i;
        }
        throw new InternalException( "No \""+str+"\" property found." );
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
