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
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.local.LocalUpdate;
import ome.model.IObject;
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
    
    public void saveObject(IObject arg0)
    {
        beforeSave();
        arg0 = internalSave(arg0);
        afterSave();
    }
    
    public IObject saveAndReturnObject(IObject arg0)
    {
        beforeSave();
        arg0 = internalSave(arg0);
        afterSave();
        return arg0;
    }

    public void saveCollection(Collection graph)
    {
        beforeSave();
        for (Object _object : graph)
        {
            IObject obj = (IObject) _object;
            obj = internalSave(obj);
        }
        afterSave();
    }
    
    public Collection saveAndReturnCollection(Collection graph)
    {
        throw new RuntimeException("Not implemented yet.");
    }
    
    public void saveMap(Map graph)
    {
        throw new RuntimeException("Not implemented yet.");
    }

    public IObject[] saveAndReturnArray(IObject[] graph)
    {
        beforeSave();
        for (int i = 0; i < graph.length; i++)
        {
            
            graph[i] = internalSave(graph[i]);
        }
        afterSave();
        return graph;
    }
    
    public void saveArray(IObject[] graph)
    {
        beforeSave();
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
    private void beforeSave()
    {
        // Cache filter for referential integrity.
        filter = new UpdateFilter(getHibernateTemplate());
        
        Event currentEvent = CurrentDetails.getCreationEvent(); 
        getHibernateTemplate().saveOrUpdate(currentEvent);
        currentSession().setFlushMode(FlushMode.COMMIT);
    }

    private IObject internalSave(IObject obj)
    {
        //obj = (IObject) getHibernateTemplate().merge(obj);
        //getHibernateTemplate().saveOrUpdate(obj);
        //getHibernateTemplate().flush(); // FIXME uh oh.

        IObject result = (IObject) filter.filter("in UpdateImpl",obj); 
        return (IObject) getHibernateTemplate().merge(result);
    }

    private void afterSave()
    {
        Set<EventLog> logs = CurrentDetails.getCreationEvent().getLogs();
        CurrentDetails.getCreationEvent().setLogs(new HashSet());
        
        for (EventLog log : logs)
        {
            getHibernateTemplate().saveOrUpdate(log);
        }
        
        logs = CurrentDetails.getCreationEvent().getLogs();
        if (logs.size() > 0)
            log.error("New logs created on update.afterSave:\n"+logs);
        // FIXME we shouldn't be updating experimenter etc. here.
     
        getHibernateTemplate().flush();
        currentSession().setFlushMode(FlushMode.AUTO);
        
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
