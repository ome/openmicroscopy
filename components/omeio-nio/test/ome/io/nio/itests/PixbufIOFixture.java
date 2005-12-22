/*
 * ome.io.nio.itests.PixbufIOFixture
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
package ome.io.nio.itests;

import java.io.IOException;
import ome.model.core.Pixels;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;


/**
 * @author callan
 *
 */
public class PixbufIOFixture
{
    private ApplicationContext ctx;

    private HibernateTemplate  ht;
    
    private Pixels pixels;
    private Experimenter experimenter;
    private Event event;

    private void initHibernate()
    {
        String[] paths = new String[] { "config.xml", "data.xml",
                "hibernate.xml" };
        ctx = new ClassPathXmlApplicationContext(paths);

        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");
    }
    
    private void createExperimenter()
    {
        experimenter = (Experimenter) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                Experimenter e = (Experimenter) session.createQuery(" from Experimenter e where e.omeName = 'test' ").uniqueResult();
                if (null == e)
                {
                    e = new Experimenter();
                    e.setOmeName("test");
                    session.save(e);
                }
                
                return e;
            }
        });
    }
    
    private void createEvent()
    {
        event = (Event) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                Event e = (Event) session.createQuery(" from Event e where e.name = 'test' ").uniqueResult();
                if (null == e)
                {
                    e = new Event();
                    e.setName("test");
                    session.save(e);
                }
                return e;
            }
        });
    }
    
    private void createPixels()
    {
        pixels = (Pixels) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                Pixels p = new Pixels();
                p.setSizeX(new Integer(64));
                p.setSizeY(new Integer(64));
                p.setSizeZ(new Integer(16));
                p.setSizeC(new Integer(2));
                p.setSizeT(new Integer(10));
                // FIXME: Bit of a hack until the model is updated, the follwing
                // is a SHA1 of "pixels"
                p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356");
                p.setBigEndian(Boolean.TRUE);

                p.setCreationEvent(event);
                p.setOwner(experimenter);

                session.save(p);
                return p;
            }
        });
    }
    
    public Pixels setUp() throws IOException
    {
        initHibernate();
        createExperimenter();
        createEvent();
        createPixels();
        
        return pixels;
    }
    
    protected void tearDown()
    {
        ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                if (pixels != null)
                {
                    Pixels p = (Pixels)
                        session.get(Pixels.class, pixels.getId());
                    session.delete(p);
                }
                
                if (experimenter != null)
                {
                    Experimenter ex = (Experimenter)
                        session.get(Experimenter.class, experimenter.getId());
                    session.delete(ex);
                }
                
                if (event != null)
                {
                    Event ev = (Event) session.get(Event.class, event.getId());
                    session.delete(ev);
                }
                
                return null;
            }
        });
    }
}
