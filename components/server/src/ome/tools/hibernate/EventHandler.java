/*
 * ome.tools.hibernate.EventHandler
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

// Third-party libraries
import java.sql.SQLException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import ome.api.IQuery;
import ome.model.enums.EventType;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;

// Application-internal dependencies

/**
 * method interceptor to properly create our Events.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
public class EventHandler implements MethodInterceptor
{

    private static Log       log = LogFactory.getLog(EventHandler.class);

    protected HibernateTemplate ht;

    public EventHandler(HibernateTemplate template)
    {
        this.ht = template;
    }

    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable
    {
            Experimenter o = 
                (Experimenter) ht.load(Experimenter.class,0);
            ExperimenterGroup g = 
                (ExperimenterGroup) ht.load(ExperimenterGroup.class,0);
            CurrentDetails.setOwner(o);
            CurrentDetails.setGroup(g);
            
            EventType test = (EventType) ht.execute(new HibernateCallback(){
                public Object doInHibernate(Session session) throws HibernateException, SQLException
                {
                    Query q = session.createQuery(
                            "from EventType where value = 'Test'");
                    return q.uniqueResult();
                }
            });
            CurrentDetails.newEvent(test);
            
            return arg0.proceed();
    
    }

}
