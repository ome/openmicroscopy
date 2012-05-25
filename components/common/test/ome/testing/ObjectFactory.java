/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.testing;

import java.sql.Timestamp;
import java.util.Iterator;

import ome.model.core.Channel;
import ome.model.core.Color;
import ome.model.core.Image;
import ome.model.meta.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.Plane;
import ome.model.meta.ChannelBinding;
import ome.model.meta.PlaneSlicingContext;
import ome.model.meta.QuantumDef;
import ome.model.meta.RenderingDef;
import ome.model.meta.Thumbnail;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.DimensionOrder;
import ome.model.enums.Family;
import ome.model.enums.Format;
import ome.model.enums.PixelType;
import ome.model.enums.RenderingModel;
import ome.model.meta.StatsInfo;

/**
 * these method serve as a both client and test data store. An object that has
 * no id is "new"; an object with an id is detached and can represent something
 * serialized from IQuery.
 * 
 * NOTE: this is a bit dangerous, causing model builds to fail sometimes. where
 * else could it live?
 */
public class ObjectFactory {

    public static OriginalFile createFile() {
        OriginalFile ofile = new OriginalFile();
        ofile.setName("testing");
        ofile.setPath("/dev/null");
        ofile.setSha1("abc");
        ofile.setSize(1L);
        ofile.setMimeType("text/plain");

        return ofile;
    }

    public static Thumbnail createThumbnails(Pixels p) {
        Thumbnail t = new Thumbnail();
        t.setMimeType("txt");
        t.setSizeX(1);
        t.setSizeY(1);
        p.addThumbnail(t);
        return t;
    }

    public static Pixels createPixelGraph(Pixels example) {
        return createPixelGraphWithChannels(example, 1);
    }

    public static Channel createChannel(Channel example) {
        Channel ch = new Channel(null);
        StatsInfo si = new StatsInfo();
        si.setGlobalMax(0.0);
        si.setGlobalMin(0.0);
        ch.setStatsInfo(si);
        return ch;
    }

    public static Pixels createPixelGraphWithChannels(Pixels example, int channelCount) {

        Pixels p = new Pixels();
        AcquisitionMode mode = new AcquisitionMode();
        PixelType pt = new PixelType();
        DimensionOrder dO = new DimensionOrder();
        Image i = new Image();
        Channel[] c = new Channel[channelCount];
        StatsInfo[] si = new StatsInfo[channelCount];
        Plane[] pl = new Plane[channelCount];
        for (int w = 0; w < channelCount; w++) {
            c[w] = new Channel(null);
            si[w] = new StatsInfo();
            pl[w] = new Plane();
        }

        if (example != null) {
            p.setId(example.getId());
            p.setVersion(example.getVersion());

            // everything else unloaded.
            pt.setId(example.getType().getId());
            pt.unload();
            dO.setId(example.getDimensionOrder().getId());
            dO.unload();
            i.setId(example.getImage().getId());
            i.unload();
            Iterator<Channel> j = example.iterateChannels();
            int w = 0;
            while (j.hasNext()) {
            	c[w].setId(j.next().getId());
                c[w].unload();
				w++;
			}
            // Not needed but useful
            p.addPlane(example.iteratePlanes().next());
            (p.iteratePlanes().next()).unload();
        }

        else {

            mode.setValue("Wide-field");
           
            pt.setValue("int8");

            dO.setValue("XYZTC");

            for (int w = 0; w < channelCount; w++) {
                c[w].setPixels(p);
                
                // Not required but useful
                si[w].setGlobalMax(new Double(0.0));
                si[w].setGlobalMin(new Double(0.0));
                c[w].setStatsInfo(si[w]);
                pl[w].setTheC(new Integer(w));
                pl[w].setTheZ(new Integer(0));
                pl[w].setTheT(new Integer(0));
                pl[w].setDeltaT(new Double(0.0));
                p.addPlane(pl[w]);

            }

            i.setName("test");
            i.setAcquisitionDate(new Timestamp(System.currentTimeMillis()));
            i.setPixels(p);

        }
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setPhysicalSizeX(1.0);
        p.setPhysicalSizeY(1.0);
        p.setPhysicalSizeZ(1.0);
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // "pixels"
        p.setType(pt);
        p.setDimensionOrder(dO);
        p.setPhysicalSizeX(new Double(1.0));
        p.setPhysicalSizeY(new Double(1.0));
        p.setPhysicalSizeZ(new Double(1.0));
        p.setImage(i);

        for (int w = 0; w < channelCount; w++) {
            p.addChannel(c[w]);
        }

        return p;
    }

    public static ChannelBinding createChannelBinding() {
        // Prereqs for binding

        Family family = new Family();
        family.setValue("linear");

        ChannelBinding binding = new ChannelBinding();
        binding.setActive(Boolean.valueOf(false));
        binding.setCoefficient(new Double(1));
        binding.setColor(new Color(100));
        binding.setFamily(family);
        binding.setInputEnd(new Double(1.0));
        binding.setInputStart(new Double(1.0));
        binding.setNoiseReduction(Boolean.valueOf(false));

        return binding;
    }

    public static RenderingDef createRenderingDef() {
        // Prereqs for RenderingDef
        RenderingModel model = new RenderingModel();
        model.setValue("rgb");

        QuantumDef qdef = new QuantumDef();
        qdef.setBitResolution(new Integer(1));
        qdef.setCdEnd(new Integer(1));
        qdef.setCdStart(new Integer(1));

        RenderingDef def = new RenderingDef();
        def.setDefaultT(new Integer(1));
        def.setDefaultZ(new Integer(1));
        def.setModel(model);
        def.setPixels(ObjectFactory.createPixelGraph(null));
        def.setQuantumDef(qdef);

        return def;
    }

    public static PlaneSlicingContext createPlaneSlicingContext() {
        PlaneSlicingContext enhancement = new PlaneSlicingContext();
        enhancement.setConstant(Boolean.FALSE);
        enhancement.setLowerLimit(new Integer(1));
        enhancement.setPlanePrevious(new Integer(1));
        enhancement.setPlaneSelected(new Integer(1));
        enhancement.setUpperLimit(new Integer(1));

        return enhancement;

    }
}
