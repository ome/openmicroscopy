/*
 * ome.logic.UpdateImpl
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

// Java imports
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

// Third-party libraries
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.api.IUpdate;
import ome.api.ServiceInterface;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.IObject;
import ome.model.meta.Event;
import ome.tools.hibernate.UpdateFilter;
import ome.util.Utils;


/**
 * implementation of the IUpdate service interface
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
@Transactional(readOnly=false)
@Stateless
@Remote(IUpdate.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.IUpdate")
@Local(LocalUpdate.class)
@LocalBinding (jndiBinding="omero/local/ome.api.local.LocalUpdate")
@SecurityDomain("OmeroSecurity")
@Interceptors({SimpleLifecycle.class})
public class UpdateImpl extends AbstractLevel1Service implements LocalUpdate
{
    protected transient LocalQuery localQuery;
    
    public final void setQueryService( LocalQuery query )
    {
    	throwIfAlreadySet(this.localQuery, query);
    	this.localQuery = query;
    }
    
    @Override
    protected Class<? extends ServiceInterface> getServiceInterface()
    {
        return IUpdate.class;
    };
    
    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

    @RolesAllowed("user")
    public void rollback()
    {
        getHibernateTemplate().execute( new HibernateCallback() {
            public Object doInHibernate(Session session) 
            throws HibernateException ,SQLException {
                session.connection().rollback();
                return null;
            }; 
         });
    }

    @RolesAllowed("user")
    public void flush()
    {
        getHibernateTemplate().execute( new HibernateCallback() {
           public Object doInHibernate(Session session) 
           throws HibernateException ,SQLException {
               session.flush();
               return null;
           }; 
        });
    }

    @RolesAllowed("user")
    public void commit()
    {
        getHibernateTemplate().execute( new HibernateCallback() {
            public Object doInHibernate(Session session) 
            throws HibernateException ,SQLException {
                session.connection().commit();
                return null;
            }; 
         });
    }

    
    // ~ INTERFACE METHODS
    // =========================================================================
    
    @RolesAllowed("user")
    public void saveObject(IObject graph)
    {
    	doAction(graph,new UpdateAction<IObject>()
    	{
    		@Override
    		public IObject run(IObject value, UpdateFilter filter) {
    			return internalSave(value, filter); 
    		}
    	});
    }
    
    @RolesAllowed("user")
    public IObject saveAndReturnObject( IObject graph )
    {
    	return doAction( graph, new UpdateAction<IObject>()
    	{
    		@Override
    		public IObject run( IObject value, UpdateFilter filter) {
    			return internalSave(value, filter); 
    		}
    	});
    }

    @RolesAllowed("user")
    public void saveCollection(Collection graph)
    {
    	doAction( graph, new UpdateAction<Collection>()
    	{
    		@Override
    		public Collection run(Collection value, UpdateFilter filter) {
    	        for (Object o : value)
    	        {
    	            IObject obj = (IObject) o;
    	            obj = internalSave( obj, filter );
    	        }
    	        return null;
    		}
    	});
    }
    
    @RolesAllowed("user")
    public Collection saveAndReturnCollection( Collection graph)
    {
        throw new RuntimeException("Not implemented yet.");
    }
    
    @RolesAllowed("user")
    public void saveMap(Map graph)
    {
        throw new RuntimeException("Not implemented yet.");
    }

    @RolesAllowed("user")
    public IObject[] saveAndReturnArray(IObject[] graph)
    {
    	return doAction( graph, new UpdateAction<IObject[]>(){
    		@Override
    		public IObject[] run(IObject[] value, UpdateFilter filter) {
    	        IObject[] copy = new IObject[value.length];
    			for (int i = 0; i < value.length; i++)
    	        {
    	            copy[i] = internalSave( value[i], filter );
    	        }
    	        return value;
    		}
    	});
    }
    
    @RolesAllowed("user")
    public void saveArray(IObject[] graph)
    {
    	doAction( graph, new UpdateAction<IObject[]>(){
    		@Override
    		public IObject[] run(IObject[] value, UpdateFilter filter) {
    	        IObject[] copy = new IObject[value.length];
    			for (int i = 0; i < value.length; i++)
    	        {
    	            copy[i] = internalSave( value[i], filter );
    	        }
    	        return value;
    		}
    	});
    }

    @RolesAllowed("user")
    public Map saveAndReturnMap( Map map )
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    @RolesAllowed("user")
    public void deleteObject( IObject row )
    {
    	doAction( row, new UpdateAction<IObject>()
    	{
    		@Override
    		public IObject run(IObject value, UpdateFilter filter) {
    			internalDelete(value, filter); 
    			return null;
    		}
    	});
    }
    
    // ~ Internals
    // =========================================================
    private void beforeUpdate( Object argument, UpdateFilter filter )
    {

        if ( argument == null )
            throw new IllegalArgumentException( 
                    "Argument to save cannot be null.");

        if ( getLogger().isDebugEnabled() )
            getLogger().debug( " Saving event before merge. " );

    }

    /** 
     * Note if we use anything other than merge here, functionality
     * from {@link ome.tools.hibernate.MergeEventListener} needs to be 
     * moved to {@link UpdateFilter} or to another event listener.
     */
    protected IObject internalSave (IObject obj, UpdateFilter filter )
    {
        if ( getLogger().isDebugEnabled() )
            getLogger().debug( " Internal save. " );
        
        IObject result = (IObject) filter.filter(null,obj); 
        result = (IObject) getHibernateTemplate().merge(result);
        return result;
    }

    protected void internalDelete(IObject obj, UpdateFilter filter )
    {
        if ( getLogger().isDebugEnabled() )
            getLogger().debug( " Internal delete. " );
        
        getHibernateTemplate().delete(
                getHibernateTemplate().load(
                        Utils.trueClass( obj.getClass() ),
                        obj.getId() ));
    }
    
    
    private void afterUpdate( Event currentEvent, UpdateFilter filter)
    {
        
        if ( getLogger().isDebugEnabled() )
            getLogger().debug( " Post-save cleanup. " );
           
        // Clean up
        getHibernateTemplate().flush();
        filter.unloadReplacedObjects();
      
    }
    
    private <T> T doAction( T graph, UpdateAction<T> action )
    {
    	T retVal;
        UpdateFilter filter = new UpdateFilter( );
        Event currentEvent = getSecuritySystem().getCurrentEvent();
        try 
        {
        	beforeUpdate( graph, filter );
        	retVal = action.run( graph, filter );
        	afterUpdate( currentEvent, filter );
        } finally {
            // Return the previous event.
            getSecuritySystem().setCurrentEvent( currentEvent );
        }
        return retVal;
    }
    
    private abstract class UpdateAction<T>{
    	public abstract T run( T value, UpdateFilter filter );
    }

    
}
