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

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

//Application-internal dependencies
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.SecuritySystem;



/** 
 * extends {@link org.hibernate.EmptyInterceptor} for controlling various
 * aspects of the Hibernate runtime.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 */
public class OmeroInterceptor implements Interceptor
{

	static volatile String last = null;
	
	static volatile int count = 1;
	
	private static Log log = LogFactory.getLog(OmeroInterceptor.class);
	
	private Interceptor EMPTY = EmptyInterceptor.INSTANCE;
	
	protected SecuritySystem secSys;
	
	public OmeroInterceptor( SecuritySystem securitySystem )
	{
		this.secSys = securitySystem;
	}

	// we may want to use them eventually for dependency-injection.
	public Object instantiate(String entityName, EntityMode entityMode, Serializable id) 
	throws CallbackException {
		debug("Intercepted instantiate.");
		return EMPTY.instantiate(entityName, entityMode, id);
	}
	
	// this will need to be implemented for security
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
		debug("Intercepted load.");
		return EMPTY.onLoad(entity, id, state, propertyNames, types);
	}
	
    public int[] findDirty(Object entity, Serializable id, 
    		Object[] currentState, Object[] previousState, 
    		String[] propertyNames, Type[] types)
    {
    	debug("Intercepted dirty check.");
    	// Checks for dirty happen even when we've done nothing, and therefore
    	// a strict exception here isn't appropriate. Testing to see if we can
    	// even evaluate objects.
    	if ( ! secSys.isReady())
    	{
    		return null; // EARLY EXIT
    	}
    	
    	if ( IObject.class.isAssignableFrom( entity.getClass() ) )
    	{
    		int idx = detailsIndex(propertyNames);
    		secSys.managedDetails( 
    				(IObject) entity, 
    				(Details) previousState[idx]);
    	}
    	
//        if ( entity instanceof Experimenter )
//        {
//            return new int[]{};
//        }
        
        // Use default logic.
        return null;
    }
    
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
    
    public boolean onFlushDirty(Object entity, Serializable id, 
    		Object[] currentState, Object[] previousState, 
    		String[] propertyNames, Type[] types)
    {
    	debug("Intercepted update.");
    	
    	boolean altered = false;
    	if ( entity instanceof IObject)
    	{
    		int idx = detailsIndex(propertyNames);
    		Details d = secSys.managedDetails( 
    				(IObject) entity, 
    				(Details) previousState[idx] );
    		if ( null != d )
    		{
    			currentState[idx] = d;
    			return true;
    		}
    	}
        return false;
    }

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

    private int detailsIndex( String[] propertyNames )
    {
        for (int i = 0; i < propertyNames.length; i++)
        {
            if ( propertyNames[i].equals( "details" ))
                return i;
        }
        throw new InternalException( "No \"details\" property found." );
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
		String[] first  = sql.split("\\sfrom\\s");
		if (first.length == 1)
		{
			return sql;
		}
		
		else if (first.length == 2)
		{
			String[] second = first[1].split("\\swhere\\s");
			StringBuilder sb = new StringBuilder();
			sb.append(first[0]);
			sb.append("\n from ");
			sb.append(second[0]);
			sb.append("\n where ");
			sb.append(second[1]);
			return sb.toString();
		}
		
		else 
		{
			throw new InternalException("Assumption about the number of " +
					"\"froms\" in sql query failed. ");
		}
	}
	
	// ~ Helpers
	// =========================================================================
	
	protected void log(String msg)
	{
		if ( msg.equals(last))
		{
			count++;
		}
	
		else if ( log.isInfoEnabled() )
		{
			String times = " ( "+count+" times )";
			log.info(msg+times);
			last = msg;
			count = 1;
		}
	}
     
}
