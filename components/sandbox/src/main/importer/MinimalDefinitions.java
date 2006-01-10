package main.importer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.FamilyType;
import ome.model.enums.ModelType;
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
        Long pixel_id = defs.createPixels().getId();
        defs.createRenderingDef(pixel_id);
    }

    public MinimalDefinitions()
    {

        String[] paths = new String[] { "config.xml", "data.xml",
                "hibernate.xml" };
        ctx = new ClassPathXmlApplicationContext(paths);

        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");

    }

    public ome.model.core.Pixels createPixels() throws Exception
    {

        return (ome.model.core.Pixels) ht.execute(new HibernateCallback()
        {

            public Object doInHibernate(org.hibernate.Session session)
                    throws org.hibernate.HibernateException,
                    java.sql.SQLException
            {

                Experimenter o = getOwner(session);

                Event e = getEvent(session);

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
                
                // ACQContext
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
                           
                return p;
            }
        });
    }

            public RenderingDef createRenderingDef(final Long pixId) throws Exception
            {

                return (RenderingDef) ht.execute(new HibernateCallback()
                {

                    public Object doInHibernate(org.hibernate.Session session)
                            throws org.hibernate.HibernateException,
                            java.sql.SQLException
                    {

                        Experimenter o = getOwner(session);

                        Event e = getEvent(session);

                        Pixels p = (Pixels) session.load(Pixels.class, pixId);
                                                
                        QuantumDef qDef = new QuantumDef();
                        // not-null
                        qDef.setBitResolution(new Integer(255)); // TODO get QuantumFactory in common code
                        qDef.setCdStart(new Integer(0));
                        qDef.setCdStop(new Integer(255)); // QuantumFactory
                        // admin
                        qDef.setCreationEvent(e);
                        qDef.setOwner(o);
                        session.save(qDef);
                        
                        FamilyType ft = new FamilyType();
                        //not-null
                        ft.setValue("TODO");
                        //admin
                        ft.setCreationEvent(e);
                        ft.setOwner(o);
                        session.save(ft);
                        
                        Color c = new Color();
                        // not-null
                        c.setRed(new Integer(1));
                        c.setBlue(new Integer(1));
                        c.setGreen(new Integer(1));
                        c.setAlpha(new Integer(1));
                        // admin
                        c.setOwner(o);
                        c.setCreationEvent(e);
                        session.save(c);
                        
                        ChannelBinding[] waves = new ChannelBinding[p.getSizeC().intValue()];
                        for (int i = 0; i < waves.length; i++)
                        {
                            waves[i] = new ChannelBinding();
                            // not-null
                            waves[i].setFamily(ft);
                            waves[i].setCoefficient(new Double(0));
                            waves[i].setIndex(new Integer(0));
                            waves[i].setInputStart(new Float(0));
                            waves[i].setInputEnd(new Float(255)); // FIXME
                            waves[i].setActive(Boolean.TRUE);
                            waves[i].setColor(c);
                            // admin
                            waves[i].setOwner(o);
                            waves[i].setCreationEvent(e);
                            session.save(waves[i]);
                        }
                        
                        ModelType mt = new ModelType();
                        // not-null
                        mt.setValue("TODO");
                        // admin
                        mt.setCreationEvent(e);
                        mt.setOwner(o);
                        session.save(mt);
                        
                        int z_size = p.getSizeZ().intValue();
                        RenderingDef newRD = new RenderingDef();
                        int defaultZ = z_size/2+z_size%2-1;
                        // not-null
                        newRD.setDefaultZ(Integer.valueOf(defaultZ));
                        newRD.setDefaultT(Integer.valueOf(0));
                        // needed
                        newRD.setModel(mt);
                        newRD.setQuantization(qDef);
                        newRD.setWaveRendering(new HashSet(Arrays.asList(waves)));
                        // admin
                        newRD.setOwner(o);
                        newRD.setCreationEvent(e);
                        session.save(newRD);
                        
                        return newRD;
                    }
                });
            }
                    
            private Event getEvent(org.hibernate.Session session)
            {
                Event e = (Event) session.get(Event.class, new Long(1));
                if (e == null)
                {
                    e = new Event();
                    e.setName("test");
                    session.save(e);
                }
                return e;
            }

            private Experimenter getOwner(org.hibernate.Session session)
            {
                Experimenter o = (Experimenter) session.get(Experimenter.class,
                        new Long(1));
                if (o == null)
                {
                    o = new Experimenter();
                    o.setOmeName("test");
                    session.save(o);
                }
                return o;
    }

}
