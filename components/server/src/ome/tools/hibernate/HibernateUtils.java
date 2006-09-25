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
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
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
     * 
     * In the case of system types (the only types which can have null details)
     * a true will be returned even though {@link Flag#LOCKED} can't be set
     * to prevent spurious {@link SecurityViolation}. This is due to ticket:307
     * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/307">ticket:307</a>
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
			
			// have to handle nulls because of ticket:307
			if (old_d == null || new_d == null) 
			{
				if (new_d == null && old_d == null) 
				{
					throw new InternalException(
							"Both details null. Can't have changed!");
				}
				
				else if (new_d == null) 
				{
					// don't worry about it. 
					return true;
				} 
				
				else 
				{ // then new_d != null. What's up?
					if (new_d.getPermissions() != null && 
							new_d.getPermissions().isSet(Flag.SOFT)) 
						return true;	
					return false;
				}
			}
			else {
				if ( ! onlyPermissionsChanged(new_d, old_d)) return false;
				Permissions new_p = new Permissions( new_d.getPermissions() );
				Permissions old_p = new Permissions( old_d.getPermissions() );
				old_p.set( Flag.LOCKED );
				return new_p.identical( old_p );
			}
			
		}
		return false;
		
    }

    /** 
     * 
     * @param newD Not null.
     * @param oldD Not null.
     * @return
     */
    public static boolean onlyPermissionsChanged(Details new_d, Details old_d)
    {
		if ( idEqual(new_d.getOwner(), old_d.getOwner()) &&
				idEqual(new_d.getGroup(), old_d.getGroup()) &&
				idEqual(new_d.getCreationEvent(),old_d.getCreationEvent()) &&
				idEqual(new_d.getUpdateEvent(),old_d.getUpdateEvent()) &&
				idEqual(new_d.getExternalInfo(),old_d.getExternalInfo()))
			return true;
		return false;
    }
    
    /** returns true under the following circumstatnces:
     * <ul>
     *  <li>both arguments are null, or</li>
     *  <li>both arguments are identical (==), or</li>
     *  <li>both arguments have the same id value(equals)</li>  
     * </ul>
     */
	public static boolean idEqual(IObject arg1, IObject arg2) {

		// arg1 is null
		if (arg1 == null) {
			// both are null, therefore equal
			if (arg2 == null)
				return true;

			// just arg1 is null, can't be equal
			return false;
		}

		// just arg2 is null, also can't be equal
		else if (arg2 == null)
			return false;

		// neither argument is null,
		// so let's move a level down,
		// but first test reference equality
		// as a performance op.
		
		if ( arg1 == arg2 ) return true;             // OP
 
		Long arg1_id = arg1.getId();
		Long arg2_id = arg2.getId();

		// arg1_id is null
		if (arg1_id == null) {

			// both are null, and not identical (see OP above)
			// therefore different
			if (arg2_id == null)
				return false;

			// just arg2_id is null, can't be equal
			return false;
		}

		// just arg2_id null, and also can't be equal
		else if (arg2_id == null)
			return false;

		// neither null, then we can just test the ids.
		else
			return arg1_id.equals(arg2_id);
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
