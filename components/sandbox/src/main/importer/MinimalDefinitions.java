package main.importer;

import java.util.Arrays;
import java.util.HashSet;

import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.FamilyType;
import ome.model.enums.ModelType;
import ome.model.enums.PIType;


import utils.OmeroSupport;

public class MinimalDefinitions extends OmeroSupport
{

    public static void main(final String[] args) throws Exception
    {
        MinimalDefinitions defs = new MinimalDefinitions();
        Long pixel_id = defs.createPixels().getId();
        defs.createRenderingDef(pixel_id);
    }

    public ome.model.core.Pixels createPixels() throws Exception
    {

        // LOGICALCHANNELS
        ome.model.core.LogicalChannel lc = new ome.model.core.LogicalChannel();
        
        // CHANNELS
        ome.model.core.Channel c = new ome.model.core.Channel();
        // not-null
        c.setIndex(new Integer(0));
        // optional?
        c.setLogicalChannel(lc);

        // ACQContext
        ome.model.acquisition.AcquisitionContext ac = new ome.model.acquisition.AcquisitionContext();
        // Needed
        PIType pi = new PIType();
        pi.setValue("RGB");
        ac.setPhotometricInterpretation(pi);

        // TYPE
        ome.model.enums.PixelsType pt = new ome.model.enums.PixelsType();
        // not-null
        pt.setValue("Uint8");

        // DIMENSIONS
        ome.model.core.PixelsDimensions dims = new ome.model.core.PixelsDimensions();
        // not null
        dims.setSizeX(new Float(1.0));
        dims.setSizeY(new Float(1.0));
        dims.setSizeZ(new Float(1.0));

        // PIXELS
        ome.model.core.Pixels p = new ome.model.core.Pixels();
        // not null
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // "pixels"

        // needed
        p.setPixelsType(pt);
        p.setAcquisitionContext(ac);
        p.setPixelsDimensions(dims);

        return (Pixels) _u.saveAndReturnObject(p);

    }

    public RenderingDef createRenderingDef(final Long pixId) throws Exception
    {

        Pixels p = (Pixels) _q.getById(Pixels.class, pixId.longValue());

        QuantumDef qDef = new QuantumDef();
        // not-null
        qDef.setBitResolution(new Integer(255)); // TODO get QuantumFactory
                                                    // in common code
        qDef.setCdStart(new Integer(0));
        qDef.setCdStop(new Integer(255)); // QuantumFactory

        FamilyType ft = new FamilyType();
        // not-null
        ft.setValue("TODO");

        Color c = new Color();
        // not-null
        c.setRed(new Integer(1));
        c.setBlue(new Integer(1));
        c.setGreen(new Integer(1));
        c.setAlpha(new Integer(1));

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
        }

        ModelType mt = new ModelType();
        // not-null
        mt.setValue("TODO");

        int z_size = p.getSizeZ().intValue();
        RenderingDef newRD = new RenderingDef();
        int defaultZ = z_size / 2 + z_size % 2 - 1;
        // not-null
        newRD.setDefaultZ(Integer.valueOf(defaultZ));
        newRD.setDefaultT(Integer.valueOf(0));
        // needed
        newRD.setModel(mt);
        newRD.setQuantization(qDef);
        newRD.setWaveRendering(new HashSet(Arrays.asList(waves)));

        return (RenderingDef) _u.saveAndReturnObject(newRD);

    }

}
