package main.importer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ome.model.enums.PIType;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

public class MinimalDefinitions
{

    private ApplicationContext ctx;

    private HibernateTemplate  ht;

    public static void main(final String[] args) throws Exception
    {
        MinimalDefinitions defs = new MinimalDefinitions();
        defs.createPixels();
    }

    public MinimalDefinitions()
    {

        String[] paths = new String[] { "config.xml", "data.xml",
                "hibernate.xml" };
        ctx = new ClassPathXmlApplicationContext(paths);

        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");

    }

    public void createPixels() throws Exception
    {

        ht.execute(new HibernateCallback()
        {

            public Object doInHibernate(org.hibernate.Session session)
                    throws org.hibernate.HibernateException,
                    java.sql.SQLException
            {

                Experimenter o = (Experimenter) session.get(Experimenter.class,
                        new Long(1));
                if (o == null)
                {
                    o = new Experimenter();
                    o.setOmeName("test");
                    session.save(o);
                }

                Event e = (Event) session.get(Event.class, new Long(1));
                if (e == null)
                {
                    e = new Event();
                    e.setName("test");
                    session.save(e);
                }

                // CHANNELS
                ome.model.core.Channel c = new ome.model.core.Channel();
                // not-null
                c.setIndex(new Integer(0));
                // admin
                c.setCreationEvent(e);
                c.setOwner(o);
                session.save(c);
                
                // LOGICALCHANNELS
                ome.model.core.LogicalChannel lc 
                    = new ome.model.core.LogicalChannel();
                // admin
                lc.setCreationEvent(e);
                lc.setOwner(o);
                session.save(lc);
                
                //ACQContext
                ome.model.acquisition.AcquisitionContext ac 
                    = new ome.model.acquisition.AcquisitionContext();
                // Needed
                PIType pi = new PIType();
                pi.setValue("RGB");
                pi.setCreationEvent(e);
                pi.setOwner(o);
                ac.setPhotometricInterpretation(pi);
                
                // admin
                ac.setCreationEvent(e);
                ac.setOwner(o);
                session.save(ac);
                session.save(pi);
                
                // TYPE
                ome.model.enums.PixelsType pt 
                    = new ome.model.enums.PixelsType();
                // not-null
                pt.setValue("Uint8");
                // admin
                pt.setCreationEvent(e);
                pt.setOwner(o);
                session.save(pt);
         
                // DIMENSIONS
                ome.model.core.PixelsDimensions dims 
                    = new ome.model.core.PixelsDimensions();
                // not null
                dims.setSizeX(new Float(1.0));
                dims.setSizeY(new Float(1.0));
                dims.setSizeZ(new Float(1.0));
                
                // admin
                dims.setCreationEvent(e);
                dims.setOwner(o);
                session.save(dims);
                
                // PIXELS
                ome.model.core.Pixels p = new ome.model.core.Pixels();
                // not null
                p.setSizeX(new Integer(1));
                p.setSizeY(new Integer(1));
                p.setSizeZ(new Integer(1));
                p.setSizeC(new Integer(1));
                p.setSizeT(new Integer(1));
                p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // "pixels"
                p.setBigEndian(Boolean.TRUE);
                // needed
                p.setPixelsType(pt);
                p.setAcquisitionContext(ac);
                p.setPixelsDimensions(dims);
                // admin
                p.setCreationEvent(e);
                p.setOwner(o);
                session.save(p);
                

                
                return null;
            };
        });

    }

}
