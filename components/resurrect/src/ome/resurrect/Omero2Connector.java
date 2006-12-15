/*
 * ome.resurrect.Omero2Connector
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
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
public class Omero2Connector {
    private HibernateTemplate ht;

    private ApplicationContext ctx;

    private static Omero2Connector soleInstance;

    public static Omero2Connector getInstance() {
        if (soleInstance == null)
            soleInstance = new Omero2Connector();
        return soleInstance;
    }

    private Omero2Connector() {
        String[] paths = new String[] { "omero2/config.xml", "omero2/data.xml",
                "omero2/hibernate.xml" };

        ctx = new ClassPathXmlApplicationContext(paths);
        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");
    }

    @SuppressWarnings("unchecked")
    public List<Experimenter> getExperimenters() {
        List l = (List) ht.execute(new HibernateCallback() {
            public Object doInHibernate(org.hibernate.Session session)
                    throws org.hibernate.HibernateException,
                    java.sql.SQLException {
                return session.createQuery("from ome.model.Experimenter")
                        .list();
            }
        });

        return (List<Experimenter>) l;
    }

    @SuppressWarnings("unchecked")
    public List<Image> getImages() {
        List l = (List) ht.execute(new HibernateCallback() {
            public Object doInHibernate(org.hibernate.Session session)
                    throws org.hibernate.HibernateException,
                    java.sql.SQLException {
                return session.createQuery("from ome.model.Image").list();
            }
        });

        return (List<Image>) l;
    }

    public List transmutePixels(final ome.model.meta.Experimenter owner,
            final ome.model.meta.Event creationEvent, final int id) {
        List toSave = (List) ht.execute(new HibernateCallback() {
            public Object doInHibernate(org.hibernate.Session session)
                    throws org.hibernate.HibernateException,
                    java.sql.SQLException {
                ome.model.ImagePixel pixels = (ImagePixel) session.get(
                        ome.model.ImagePixel.class, id);
                PixelsTrans transform = new PixelsTrans(pixels, session, owner,
                        creationEvent, null);

                return transform.transmute();
            }
        });

        return toSave;
    }
}
