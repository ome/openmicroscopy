/*
 * ome.logic.GenericImpl
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

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.Cascades.CascadingAction;
import org.hibernate.event.def.AbstractSaveEventListener;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

// Application-internal dependencies
import ome.api.local.LocalUpdate;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.security.CurrentDetails;
import ome.tools.hibernate.UpdateFilter;
import ome.util.ContextFilter;
import ome.util.Filterable;

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

    public void saveObject(IObject arg0)
    {
        saveAndReturnObject(arg0);
    }
    
    public IObject saveAndReturnObject(IObject arg0)
    {
        log.info("Saving ..."+arg0);
        arg0 = (IObject) new UpdateFilter().filter("in UpdateImpl",arg0); 
        // TODO optimize by passing around the update filter in side of a single call.
        //arg0 = (IObject) getHibernateTemplate().merge(arg0);
        getHibernateTemplate().save(arg0);
        getHibernateTemplate().flush(); // FIXME uh oh.
        
        if (arg0 instanceof Event || arg0 instanceof EventLog || arg0 instanceof EventDiff)
        {
        
        } else {
            saveCollection(CurrentDetails.getCreationEvent().getLogs()); // TODO in AOP please
        }
        
        return arg0;
    }

    public void saveCollection(Collection graph)
    {
        for (Object object : graph)
        {
            saveAndReturnObject((IObject)object);
        }
    }
    
    public Collection saveAndReturnCollection(Collection graph)
    {
        for (Object object : graph)
        {
            saveAndReturnObject((IObject)object);
        }
        return graph; // FIXME
    }
    
    public void saveMap(Map graph)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public IObject[] saveAndReturnArray(IObject[] graph)
    {
        for (int i = 0; i < graph.length; i++)
        {
            graph[i] = saveAndReturnObject(graph[i]);
        }
        return graph;
    }
    
    public void saveArray(IObject[] graph)
    {
        for (int i = 0; i < graph.length; i++)
        {
            saveObject(graph[i]);
        }
    }

    public Map saveAndReturnMap(Map map)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    
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
     
}
