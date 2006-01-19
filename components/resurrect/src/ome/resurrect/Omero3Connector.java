/*
 * ome.resurrect.Omero2Connector
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
package ome.resurrect;

import java.util.List;

import ome.model.enums.PIType;
import ome.model.enums.PixelsType;
import ome.model.meta.Experimenter;
import ome.model.meta.Event;

import org.hibernate.Query;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;


/**
 * @author callan
 *
 */
public class Omero3Connector
{
    private HibernateTemplate ht;
    
    private ApplicationContext ctx;
    
    private static Omero3Connector soleInstance;
    
    public static Omero3Connector getInstance()
    {
        if (soleInstance == null)
            soleInstance = new Omero3Connector();
        return soleInstance;
    }
    
    public Omero3Connector()
    {
        String[] paths = new String[] { "omero3/config.xml", "omero3/data.xml",
                                        "omero3/hibernate.xml" };

        ctx = new ClassPathXmlApplicationContext(paths);
        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");
    }
    
    public void save(final Event creationEvent, final Object[] objects)
    {
        ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                session.save(creationEvent);
                
                for (Object o : objects)
                    session.save(o);

                return null;
            }
        });
    }

    public void save(final Event creationEvent, final Object object)
    {
        ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                session.save(creationEvent);
                session.save(object);

                return null;
            }
        });
    }
    
    public PixelsType getPixelsType(final String asString)
    {
        PixelsType p = (PixelsType) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                Query query = session.createQuery(
                    "from ome.model.enums.PixelsType where value = :value");
    
                List l = query.setParameter("value", asString).list();
    
                if (l.size() != 1)
                    throw new RuntimeException("Request for PixelsType: '" + asString +
                            "' resulted in an abnormal number of results: '" +
                            l.size() + "'. Should be 1.");
                
                return (PixelsType) l.get(0);
            }
        });
        
        return p;
    }
    
    public PIType getPIType(final String asString)
    {
        PIType p = (PIType) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                Query query = session.createQuery(
                    "from PIType where value = :value");
    
                List l = query.setParameter("value", asString).list();
    
                if (l.size() != 1)
                    throw new RuntimeException("Request for PIType: '" + asString +
                            "' resulted in an abnormal number of results: '" +
                            l.size() + "'. Should be 1.");
                
                return (PIType) l.get(0);
            }
        });
        
        return p;
    }
    
    public Experimenter getExperimenter(final long id)
    {
        Experimenter e = (Experimenter) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                return session.get(Experimenter.class, id);
            }
        });
        
        return e;
    }
}
