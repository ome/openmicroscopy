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
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

// Application-internal dependencies
import ome.model.enums.EventType;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;

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

    private static Log          log = LogFactory.getLog(EventHandler.class);

    // TODO Do we need to get rid of this?
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

        CurrentDetails.clear();
        setDetails();
        try {
            Object retVal = arg0.proceed();
            ht.flush(); 
            // TODO performance? but HInterceptor is flushing 
            // after finally { clear }
            return retVal;
        } finally {
            CurrentDetails.clear();
        }

    }

    protected void setDetails()
    {

        if (getName() == null)
            throw new RuntimeException(
                    "omero.username system property must be set.");

        Experimenter exp = (Experimenter) ht.execute(new HibernateCallback()
        {

            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException
            {
                Criteria c = session.createCriteria(Experimenter.class);
                c.add(Restrictions.eq("omeName", getName()));
                Object o = c.uniqueResult();

                if (o == null)
                    throw new RuntimeException("No such user: " + getName());

                return o;
            }
        });
        CurrentDetails.setOwner(exp);

        if (getGroup() == null)
            throw new RuntimeException(
                    "omero.groupname system property must be set.");

        ExperimenterGroup grp = (ExperimenterGroup) ht
                .execute(new HibernateCallback()
                {

                    public Object doInHibernate(Session session)
                            throws HibernateException, SQLException
                    {
                        Criteria c = session
                                .createCriteria(ExperimenterGroup.class);
                        c.add(Restrictions.eq("name", getGroup()));
                        Object o = c.uniqueResult();

                        if (o == null)
                            throw new RuntimeException("No such group: "
                                    + getGroup());

                        return o;
                    }
                });

        CurrentDetails.setGroup(grp);

        if (getType() == null)
            throw new RuntimeException("omero.eventtype must be set.");

        EventType type = (EventType) ht.execute(new HibernateCallback()
        {

            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException
            {
                Criteria c = session.createCriteria(EventType.class);
                c.add(Restrictions.eq("value", getType()));
                Object o = c.uniqueResult();

                if (o == null)
                    throw new RuntimeException("No such type: " + getType());

                return o;
            }
        });

        CurrentDetails.newEvent(type);

    }
    
    protected String getName()
    {
        return System.getProperties().getProperty("omero.username");
    }

    protected String getGroup()
    {
        return System.getProperties().getProperty("omero.groupname");
    }

    protected String getType()
    {
        return System.getProperties().getProperty("omero.eventtype");
    }
}
