/*
 * ome.io.nio.itests.PlaneIOUnitTest
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

import ome.model.meta.Event;
import ome.model.meta.Experimenter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import junit.framework.TestCase;


/**
 * @author callan
 *
 */
public class PlaneIOUnitTest extends TestCase
{

    private ApplicationContext ctx;

    private HibernateTemplate  ht;

    protected void setUp()
    {
        String[] paths = new String[] { "config.xml", "data.xml",
                "hibernate.xml" };
        ctx = new ClassPathXmlApplicationContext(paths);

        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");

        ht.execute(new HibernateCallback()
        {

            public Object doInHibernate(org.hibernate.Session session)
                    throws org.hibernate.HibernateException,
                    java.sql.SQLException
            {

                Experimenter o = (Experimenter) session.get(Experimenter.class, new Integer(1));
                if (o == null)
                {
                    o = new Experimenter();
                    o.setOmeName("test");
                    session.save(o);
                }   
                
                Event e = (Event) session.get(Event.class, new Integer(1));
                if (e == null)
                {
                    e = new Event();
                    e.setName("test");
                    session.save(e);
                }   
                
                ome.model.core.Pixels p = new ome.model.core.Pixels();
                p.setSizeX(new Integer(1));
                p.setSizeY(new Integer(1));
                p.setSizeZ(new Integer(1));
                p.setSizeC(new Integer(1));
                p.setSizeT(new Integer(1));
                p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356");  // "pixels"
                p.setBigEndian(Boolean.TRUE);
                
                p.setCreationEvent(e);
                p.setOwner(o);

                
                session.save(p);
                return null;
            };
        });
    }
    
    public void testInitialPlane()
    {
    }
}
