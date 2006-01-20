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

import ome.model.Experimenter;
import ome.model.Image;
import ome.model.ImagePixel;
import ome.resurrect.transform.PixelsTrans;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;


/**
 * @author callan
 *
 */
public class Omero2Connector
{
    private HibernateTemplate ht;
    
    private ApplicationContext ctx;
    
    private static Omero2Connector soleInstance;
    
    public static Omero2Connector getInstance()
    {
        if (soleInstance == null)
            soleInstance = new Omero2Connector();
        return soleInstance;
    }
    
    private Omero2Connector()
    {
        String[] paths = new String[] { "omero2/config.xml", "omero2/data.xml",
                                        "omero2/hibernate.xml" };

        ctx = new ClassPathXmlApplicationContext(paths);
        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");
    }

    @SuppressWarnings("unchecked")
    public List<Experimenter> getExperimenters()
    {
        List l = (List) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                return session.createQuery("from ome.model.Experimenter").list();
            }
        });
        
        return (List<Experimenter>) l;
    }
    
    @SuppressWarnings("unchecked")
    public List<Image> getImages()
    {
        List l = (List) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                return session.createQuery("from ome.model.Image").list();
            }
        });
        
        return (List<Image>) l;
    }
    
    public List transmutePixels(final ome.model.meta.Experimenter owner,
                                final ome.model.meta.Event creationEvent,
                                final int id)
    {
        List toSave = (List) ht.execute(new HibernateCallback()
        {
             public Object doInHibernate(org.hibernate.Session session)
                 throws org.hibernate.HibernateException, java.sql.SQLException
             {
                 ome.model.ImagePixel pixels =
                     (ImagePixel) session.get(ome.model.ImagePixel.class, id);
                 PixelsTrans transform = new PixelsTrans(pixels, session, owner,
                                                         creationEvent, null);
                 
                 return transform.transmute();
             }
        });
                
        return toSave;
   }
}
