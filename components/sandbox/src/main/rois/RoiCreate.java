package main.rois;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ome.model.core.Pixels;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.roi.Roi5D;
import ome.model.roi.RoiExtent;
import ome.model.roi.RoiMap;
import ome.model.roi.RoiSet;

import utils.OmeroSupport;

public class RoiCreate extends OmeroSupport
{

    public static void main(final String[] args) throws Exception
    {
        new RoiCreate();
    }

    public RoiCreate() throws Exception
    {
        super();

        // Create lots of sets
        for (int i = 0; i < 1; i++)
        {
            RoiSet s = createSet();

            // for each set make lots of images with a single roi5d
            for (int j = 0; j < 1; j++)
            {
                Pixels p = createPixels();
                RoiMap m = createMap();
                Roi5D r = createRoi();
                RoiExtent re = createExtent();

                // Linking
                link(p, s, m, r, re);

            }

            _u.saveObject(s);
            
        }
        
        super.commit();

    }

    private RoiExtent createExtent()
    {
        RoiExtent re = new RoiExtent();
        re.setCindexMax(new Integer(5));
        re.setCindexMin(new Integer(1));
        re.setTindexMax(new Integer(10));
        re.setTindexMin(new Integer(1));
        re.setZindexMax(new Integer(100));
        re.setZindexMin(new Integer(1));
        return re;
    }

    private Pixels createPixels()
    {
        Pixels p = new Pixels();
        p.setSizeX(new Integer(64));
        p.setSizeY(new Integer(64));
        p.setSizeZ(new Integer(10));
        p.setSizeC(new Integer(3));
        p.setSizeT(new Integer(100));
        p.setSha1("This is a test");
        return p;
    }

    private RoiSet createSet()
    {
        RoiSet s = new RoiSet();
        return s;
    }

    private RoiMap createMap()
    {
        RoiMap m = new RoiMap();
        return m;
    }

    private Roi5D createRoi()
    {
        Roi5D r = new Roi5D();
        return r;
    }

    private void link(Pixels p, RoiSet s, RoiMap m, Roi5D r, RoiExtent re)
    {
        // ROI
        r.setPixels(p);
        if (null == r.getExtents()) r.setExtents(new HashSet());
        r.getExtents().add(re);
        re.setRoi5d(r);
        
        // MAP
        m.setRoi5d(r);
        m.setRoiset(s);

        // SET
        if (null == s.getRoiMaps()) s.setRoiMaps(new HashSet());
        s.getRoiMaps().add(m);

    }

}
