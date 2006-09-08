/*
 * ome.tools.hibernate.ExtendedMetadata
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Third-party libraries
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Permissions;


/**
 * extension of the model metadata provided by {@link SessionFactory}. During
 * construction, the metadata is created and cached for later use.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see 	SessionFactory
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class ExtendedMetadata
{
	
	private final Map<String,Holder> classNameHolder = new HashMap<String,Holder>();

	// NOTES:
	// TODO we could just delegate to sf and implement the same interface.		
	// TOTEST
	// will need to get collection items out. // no filtering.
	// will also need to do the same for checkIfNeedLock once we have
	// a collection valued (non-association) type. (does that exist??)
	// will also need to handle components if we have any other than details.
	// Doesn't handle ComponentTypes
	/** constructor which takes a non-null Hibernate {@link SessionFactory}
	 * and eagerly parses the available metadata for later use.
	 * 
	 * @see SessionFactory#getAllClassMetadata()
	 */
	@SuppressWarnings("unchecked")
	public ExtendedMetadata( SessionFactory sessionFactory )
	{
		if ( sessionFactory == null ) 
			throw new ApiUsageException( "SessionFactory may not be null." );
		
		Map<String,ClassMetadata> m = sessionFactory.getAllClassMetadata();
		
		for (String  key : m.keySet()) 
		{
			ClassMetadata cm = m.get(key);	
			Type[] types = cm.getPropertyTypes();
			classNameHolder.put(key,
					new Holder( cm, locksFields(types), lockedByFields(key,m) ));
		}
	}

	/** walks the {@link IObject} argument <em>non-</em>recursively and gathers
	 * all attached {@link IObject} instances which may need to be locked by the
	 * creation or updating of the argument.
	 * 
	 * @param iObject A newly created or updated {@link IObject} instance which
	 *  	might possibly lock other {@link IObject IObjects}.
	 * 		A null argument will return an empty array to be checked.
	 * @return A non-null array of {@link IObject IObjects} which may need to be
	 * 		locked.
	 * @see Permissions.Flag#LOCKED
	 */
	public IObject[] getLockCandidates( IObject iObject )
	{
		if ( iObject == null ) return new IObject[]{};
		
		Holder h = classNameHolder.get( iObject.getClass().getName() );
		return h.getLockCandidates( iObject );
	}
	
	/** returns all class/field name pairs which may possible link to an object
	 * of type <code>klass</code>.
	 * 
	 * @param klass Non-null {@link Class subclass} of {@link IObject} 
	 * @return A non-null array of {@link String} queries which can be used to
	 * 		determine if an {@link IObject} instance can be unlocked.
	 * @see Permissions.Flag#LOCKED 
	 */
	public String[][] getLockChecks( Class<? extends IObject> klass )
	{
		if ( klass == null ) 
			throw new ApiUsageException( "Cannot proceed with null klass." );
		
		Holder h = classNameHolder.get( klass.getName() );
		
		if ( h == null )
			throw new ApiUsageException( "Metadata not found for: "
					+klass.getName());
		
		return h.getLockChecks( );		
	}

	// ~ Helpers
	// =========================================================================
	
	/** examines all {@link Type types} for this class and stores pointers
	 * to those fields which represent {@link IObject} instances. These 
	 * fields may need to be locked when an object of this type is created
	 * or updated.
	 */
	private int[] locksFields(Type[] type) {
		int idx = 0;
		int[] pointers = new int[type.length];
		
		Arrays.fill(pointers, -1);
		
		for (int i = 0; i < type.length; i++) {
			if ( IObject.class.isAssignableFrom(type[i].getReturnedClass()))
			{
				pointers[idx++] = i;
			}
		}

		int[] canLockFields = new int[idx];
		System.arraycopy(pointers, 0, canLockFields, 0, idx);
		return canLockFields;
	}

	/** examines all model objects to see which fields contain a {@link Type}
	 * which points to this class. Uses {@link #locksFields(Type[])} since this
	 * is the inverse process.
	 */
	private String[][] lockedByFields( String klass, Map<String,ClassMetadata> m ) 
	{
		if ( m == null ) 
			throw new InternalException( "ClassMetadata map cannot be null." );
		
		List<String[]> fields = new ArrayList<String[]>();
		
		for (String k : m.keySet()) {
			ClassMetadata cm = m.get( k );
			Type[] type = cm.getPropertyTypes();
			String[] names = cm.getPropertyNames();
			int[] inverse = locksFields(type);
			for (int i = 0; i < inverse.length; i++) 
			{
				if (klass.equals(type[inverse[i]].getReturnedClass().getName()))
				{
					fields.add(new String[]{k,names[inverse[i]]});
				}
			}
		}
		return fields.toArray(new String[fields.size()][2]);
	}
	
	/** inner class which wraps the {@link ClassMetadata} and pointer arrays for
	 * a single {@link IObject} type.
	 */
	private static class Holder 
	{
		private ClassMetadata cm;
		private int[] canLockFields;
		private String[][] lockedByFields;
		
		/** constructor which parses the {@link ClassMetadata} available from
		 * a {@link SessionFactory}
		 * 
		 * @param classMetadata Non-null metadata to be parsed.
		 */
		public Holder( ClassMetadata classMetadata, int[] locks, String[][] lockedBy )
		{
			if ( classMetadata == null )
				throw new InternalException("ClassMetadata should never be null.");
			
			cm = classMetadata;
			canLockFields = locks;
			lockedByFields = lockedBy;
		}
		
		public IObject[] getLockCandidates( IObject o )
		{
			int idx = 0;
			IObject[] retVal;
			IObject[] toCheck = new IObject[canLockFields.length];
			Object[] values = cm.getPropertyValues(o, EntityMode.POJO);
			for (int i = 0; i < canLockFields.length; i++) 
			{
				if ( values[canLockFields[i]] != null )
				{
					toCheck[idx++] = (IObject) values[canLockFields[i]];
				}
			}
			retVal = new IObject[idx];
			System.arraycopy(toCheck, 0, retVal, 0, idx);
			return retVal;
		}
		
		public String[][] getLockChecks( )
		{
			return lockedByFields;
		}
		
	}
	
}