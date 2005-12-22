package main.rois;

import java.util.HashSet;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import ome.model.core.Pixels;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.roi.Roi5D;
import ome.model.roi.RoiExtent;
import ome.model.roi.RoiMap;
import ome.model.roi.RoiSet;

public class SimpleRoiCreate
{

    public static void main(final String[] args) throws Exception
    {
        new SimpleRoiCreate();
    }

    public SimpleRoiCreate() throws Exception
    {

        SessionFactory sf = new Configuration().configure()
                .buildSessionFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        
        Experimenter o = (Experimenter) session.get(Experimenter.class,
                new Integer(1));
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

        // Create lots of sets
        for (int i = 0; i < 10; i++)
        {
            RoiSet s = createSet(o, e);

            // for each set make lots of images with a single roi5d
            for (int j = 0; j < 10; j++)
            {
                Pixels p = createPixels(o, e);
                RoiMap m = createMap(o, e);
                Roi5D r = createRoi(o, e);
                RoiExtent re = createExtent(o, e);

                // Linking
                link(session, p, s, m, r, re);

            }

            session.flush();

        }

        tx.commit();
        
    }

    private RoiExtent createExtent(Experimenter o, Event e)
    {
        RoiExtent re = new RoiExtent();
        re.setCreationEvent(e);
        re.setOwner(o);
        re.setCindexMax(new Integer(5));
        re.setCindexMin(new Integer(1));
        re.setTindexMax(new Integer(10));
        re.setTindexMin(new Integer(1));
        re.setZindexMax(new Integer(100));
        re.setZindexMin(new Integer(1));
        return re;
    }

    private Pixels createPixels(Experimenter o, Event e)
    {
        Pixels p = new Pixels();
        p.setCreationEvent(e);
        p.setOwner(o);
        p.setBigEndian(Boolean.TRUE);
        p.setSha1("");
        p.setSizeX(new Integer(64));
        p.setSizeY(new Integer(64));
        p.setSizeZ(new Integer(10));
        p.setSizeC(new Integer(3));
        p.setSizeT(new Integer(100));
        return p;
    }

    private RoiSet createSet(Experimenter o, Event e)
    {
        RoiSet s = new RoiSet();
        s.setCreationEvent(e);
        s.setOwner(o);
        return s;
    }

    private RoiMap createMap(Experimenter o, Event e)
    {
        RoiMap m = new RoiMap();
        m.setCreationEvent(e);
        m.setOwner(o);
        return m;
    }

    private Roi5D createRoi(Experimenter o, Event e)
    {
        Roi5D r = new Roi5D();
        r.setCreationEvent(e);
        r.setOwner(o);
        return r;
    }

    private void link(org.hibernate.Session session, Pixels p, RoiSet s,
            RoiMap m, Roi5D r, RoiExtent re)
    {
        // ROI
        r.setPixels(p);
        if (null == r.getExtents()) r.setExtents(new HashSet());
        r.getExtents().add(re);

        // MAP
        m.setRoi5d(r);
        m.setRoiset(s);

        // SET
        if (null == s.getRoiMaps()) s.setRoiMaps(new HashSet());
        s.getRoiMaps().add(m);

        session.save(s);
        session.save(m);
        session.save(p);
        session.save(r);
        session.save(re);

    }

}
