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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

// Application-internal dependencies
import ome.annotations.Validate;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.local.LocalUpdate;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.security.CurrentDetails;
import ome.tools.hibernate.UpdateFilter;


/**
 * implementation of the IUpdate service interface
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class UpdateImpl extends AbstractLevel1Service implements LocalUpdate
{

    private static Log log = LogFactory.getLog(UpdateImpl.class);

    @Override
    protected String getName() {
        return IUpdate.class.getName();
    };
    
    protected IQuery query;

    protected UpdateFilter filter;
    
    private UpdateImpl(){}; // We need the query
    public UpdateImpl(IQuery query)
    {
        this.query = query;
    }

    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

    public void rollback()
    {
        SessionFactory sf = 
            getHibernateTemplate().getSessionFactory();
        Session s = SessionFactoryUtils.getSession(sf,false);
        
        try {
            s.connection().rollback();
        } catch (SQLException sqle){
            getHibernateTemplate().getJdbcExceptionTranslator().
            translate("Attempting to rollback from SessionFactory",null,sqle);
        }
    }
    
    // ~ INTERFACE METHODS
    // =========================================================================
    
    public void saveObject(IObject graph)
    {
        beforeSave( graph );
        graph = internalSave( graph );
        afterSave();
    }
    
    public IObject saveAndReturnObject( IObject graph )
    {
        beforeSave( graph );
        graph = internalSave( graph );
        afterSave();
        return graph;
    }

    public void saveCollection(@Validate(IObject.class) Collection graph)
    {
        beforeSave( graph );
        for (Object _object : graph)
        {
            IObject obj = (IObject) _object;
            obj = internalSave(obj);
        }
        afterSave();
    }
    
    public Collection saveAndReturnCollection(
            @Validate(IObject.class) Collection graph)
    {
        throw new RuntimeException("Not implemented yet.");
    }
    
    public void saveMap(Map graph)
    {
        throw new RuntimeException("Not implemented yet.");
    }

    public IObject[] saveAndReturnArray(IObject[] graph)
    {
        beforeSave( graph );
        for (int i = 0; i < graph.length; i++)
        {
            
            graph[i] = internalSave(graph[i]);
        }
        afterSave();
        return graph;
    }
    
    public void saveArray(IObject[] graph)
    {
        beforeSave( graph );
        for (int i = 0; i < graph.length; i++)
        {
            graph[i] = internalSave(graph[i]);
        }
        afterSave();
    }

    public Map saveAndReturnMap(Map map)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void deleteObject(IObject row)
    {
        getHibernateTemplate().delete(row);
    }
    
    // ~ Internals
    // =========================================================
    private void beforeSave( Object argument)
    {

        if ( argument == null )
            throw new IllegalArgumentException( 
                    "Argument to save cannot be null.");
        
        // Cache filter for referential integrity.
        filter = new UpdateFilter(getHibernateTemplate());
        
        // Save event before we enter.
        Event currentEvent = CurrentDetails.getCreationEvent(); 
        getHibernateTemplate().saveOrUpdate(currentEvent);

        // Don't flush until we're done.
        currentSession().setFlushMode(FlushMode.COMMIT);
    }

    private IObject internalSave(IObject obj)
    {
        IObject result = (IObject) filter.filter("in UpdateImpl",obj); 
        return (IObject) getHibernateTemplate().merge(result);
    }

    private void afterSave()
    {
        // Save all that and go back to AUTO flush.
        getHibernateTemplate().flush(); // TODO performance?
        currentSession().setFlushMode(FlushMode.AUTO);
        
        // Let's save the event again using a temporary event.
        Event currentEvent = CurrentDetails.getCreationEvent();
        EventType afterSave = new EventType();
        afterSave.setValue( "afterSave" );
        CurrentDetails.newEvent( afterSave );
        getHibernateTemplate().saveOrUpdate( currentEvent );

        // Checks.
        List logs = CurrentDetails.getCreationEvent().collectLogs( null );
        if (logs.size() > 0)
            log.error("New logs created on update.afterSave:\n"+logs);
        // FIXME we shouldn't be updating experimenter etc. here.
        
        // Return the previous event.
        CurrentDetails.setCreationEvent( currentEvent );
        
        // Cleanup
        filter = null;
    }

    private Session currentSession()
    {
        Session s = SessionFactoryUtils.getSession(
                getHibernateTemplate().getSessionFactory(),false);
        return s;
    }

    
}
