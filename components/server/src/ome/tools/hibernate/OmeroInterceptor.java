/*
 * ome.tools.hibernate.OmeroSessionFactoryBean
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

//Java imports
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.springframework.util.Assert;

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.SecuritySystem;
import ome.tools.lsid.LsidUtils;



/** 
 * implements {@link org.hibernate.Interceptor} for controlling various
 * aspects of the Hibernate runtime. Where no special requirements exist, 
 * methods delegate to {@link EmptyInterceptor}
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see 	EmptyInterceptor
 * @see 	Interceptor
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class OmeroInterceptor implements Interceptor
{

	static volatile String last = null;
	
	static volatile int count = 1;
	
	private static Log log = LogFactory.getLog(OmeroInterceptor.class);
	
	private Interceptor EMPTY = EmptyInterceptor.INSTANCE;
	
	protected SecuritySystem secSys;

	/** only public ctor, requires a non-null {@link SecuritySystem} */
	public OmeroInterceptor( SecuritySystem securitySystem )
	{
		Assert.notNull(securitySystem);
		this.secSys = securitySystem;
	}

	/** default logic, but we may want to use them eventually for 
	 * dependency-injection.
	 */
	public Object instantiate(String entityName, EntityMode entityMode, 
			Serializable id) throws CallbackException {
		
		debug("Intercepted instantiate.");
		return EMPTY.instantiate(entityName, entityMode, id);
	
	}
	
	/** default logic, but this will need to be implemented for security TODO */
	public boolean onLoad(Object entity, Serializable id, Object[] state, 
			String[] propertyNames, Type[] types) throws CallbackException {
		
		debug("Intercepted load.");
		return EMPTY.onLoad(entity, id, state, propertyNames, types);
		
	}
	
	/** default logic */
    public int[] findDirty(Object entity, Serializable id, 
    		Object[] currentState, Object[] previousState, 
    		String[] propertyNames, Type[] types)
    {
    	debug("Intercepted dirty check.");
       	return EMPTY.findDirty(
    			entity, id, currentState, previousState, 
    			propertyNames, types);
    }
    
    /** callsback to {@link SecuritySystem#transientDetails(IObject)} for 
     * properly setting {@link IObject#getDetails() Details}
     */
    public boolean onSave(Object entity, Serializable id, 
    		Object[] state, 
    		String[] propertyNames, Type[] types)
    {
    	debug("Intercepted save.");
    	
    	if ( entity instanceof IObject )
    	{
    		int idx = detailsIndex(propertyNames);
    		IObject iobj = (IObject) entity;
    		Details d = secSys.transientDetails( iobj );
    		state[idx] = d;
    	}
    	
        return true; // transferDetails ALWAYS edits the new entity.
    }

    /** callsback to {@link SecuritySystem#managedDetails(IObject, Details)} for 
     * properly setting {@link IObject#getDetails() Details}. Also checks
     * if any collections have been left null or 
     * {@link Details#filteredSet() filtered} and throws an exception if 
     * necessary.
     */
    public boolean onFlushDirty(Object entity, Serializable id, 
    		Object[] currentState, Object[] previousState, 
    		String[] propertyNames, Type[] types)
    {
    	debug("Intercepted update.");
    	
    	boolean altered = false;
    	if ( entity instanceof IObject)
    	{
    		IObject obj = (IObject) entity;
    		int idx = detailsIndex(propertyNames);
        	checkCollections(obj,(Long)id,
        			currentState, previousState, 
        			propertyNames, types, idx);
    		altered |= resetDetails(obj,currentState,previousState,idx);
    	}
        return altered;
    }

    /** default logic, will be needed for security */
	public void onDelete(Object entity, Serializable id, 
			Object[] state, String[] propertyNames, Type[] types) 
	throws CallbackException 
	{ 
		debug("Intercepted delete."); 
	}
    
    // ~ Collections (of interest)
	// =========================================================================
    public void onCollectionRecreate(Object collection, Serializable key) 
    throws CallbackException { debug("Intercepted collection recreate."); }
    
	public void onCollectionRemove(Object collection, Serializable key) 
	throws CallbackException { debug("Intercepted collection remove."); }
	
	public void onCollectionUpdate(Object collection, Serializable key) 
	throws CallbackException { debug("Intercepted collection update."); }
	
    // ~ Flush (currently unclear semantics)
	// =========================================================================
    public void preFlush(Iterator entities) throws CallbackException 
    {
    	debug("Intercepted preFlush.");
		EMPTY.preFlush(entities);
	}
    
    public void postFlush(Iterator entities) throws CallbackException 
	{
    	debug("Intercepted postFlush.");
		EMPTY.postFlush(entities);	
	}
    
    // ~ Helpers
	// =========================================================================

    protected int detailsIndex( String[] propertyNames )
    {
    	return index( "details", propertyNames );
    }
    
    protected int index( String str, String[] propertyNames )
    {
        for (int i = 0; i < propertyNames.length; i++)
        {
            if ( propertyNames[i].equals( str ))
                return i;
        }
        throw new InternalException( "No \""+str+"\" property found." );
    }
    
    private void debug(String msg)
    {
    	if (log.isInfoEnabled())
    	{
    		log(msg);
    	}
    }
    
    // ~ Serialization
    // =========================================================================
    
    private static final long serialVersionUID = 7616611615023614920L;
    
    private void readObject(ObjectInputStream s) 
    throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
    }
    
    // ~ Unused interface methods
	// =========================================================================

	public void afterTransactionBegin(Transaction tx) {}
	public void afterTransactionCompletion(Transaction tx) {}
	public void beforeTransactionCompletion(Transaction tx) {}

	public Object getEntity(String entityName, Serializable id) throws CallbackException {
		return EMPTY.getEntity(entityName, id);
	}

	public String getEntityName(Object object) throws CallbackException {
		return EMPTY.getEntityName(object);
	}

	public Boolean isTransient(Object entity) {
		return EMPTY.isTransient(entity);
	}
	
	public String onPrepareStatement(String sql) {
		// start
		StringBuilder sb = new StringBuilder();

		String[] first  = sql.split("\\sfrom\\s");
		sb.append(first[0]);
		if (first.length == 1)
		{
			return sb.toString();
		}
		else if (first.length == 2)
		{
			// from
			sb.append("\n from ");			
			
			String[] second = first[1].split("\\swhere\\s");
			sb.append(second[0]);
			if (second.length == 1)
			{
				return sb.toString();
			}
			else if (second.length == 2)
			{
				// where
				sb.append("\n where ");
				sb.append(second[1]);
				return sb.toString();
			}
		}
		
		throw new InternalException("Assumptions about the number of " +
					"\"froms\" and \"wheres\" in sql query failed. ");
	}
	
	// ~ Helpers
	// =========================================================================
	
	/** asks {@link SecuritySystem} to create a new managed {@link Details}
	 * based on the previous state of this entity.
	 * 
	 * @param entity IObject to be updated
	 * @param currentState the possibly changed field data for this entity
	 * @param previousState the field data as seen in the db
	 * @param idx the index of Details in the state arrays.
	 */
	protected boolean resetDetails(IObject entity, Object[] currentState,
			Object[] previousState, int idx)
	{
		Details previous = (Details) previousState[idx];
		Details result = secSys.managedDetails( entity, previous ); 

		if ( previous != result )
		{
			currentState[idx] = result;
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
	@SuppressWarnings("unchecked")
	protected void checkCollections(IObject entity,Long id,
			Object[] currentState, Object[] previousState, 
			String[] propertyNames, Type[] types, int detailsIndex)
	{
		boolean unloaded = false;
		
		Details d = (Details) previousState[detailsIndex];
		if ( d != null )
		{
			Set<String> s = d.filteredSet();
			for (String string : s) {
				string = LsidUtils.parseField(string);
				int idx = index(string,propertyNames);
				// currentState[idx] = reloadCollection(entity, id, types[idx], string);
				unloaded = true;
				break;
			}
		}
		
		if (!unloaded)
		for (int i = 0; i < types.length; i++) {
			Type t = types[i];
			if ( t.isCollectionType() && null == currentState[i] )
			{
				//currentState[i] = reloadCollection(entity,id,t,propertyNames[i]);
				unloaded = true;
				break;
			}
		}
		
		if (unloaded)
		{
			throw new InternalException(
					"Filter didn't catch unloaded/filtered collection.");
		}
	}

		
	protected void log(String msg)
	{
		if ( msg.equals(last))
		{
			count++;
		}
	
		else if ( log.isDebugEnabled() )
		{
			String times = " ( "+count+" times )";
			log.debug(msg+times);
			last = msg;
			count = 1;
		}
	}
     
}
