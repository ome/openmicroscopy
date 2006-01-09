package tmp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;

import omeis.providers.re.ColorsFactory;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.metadata.ChannelBindings;
import omeis.providers.re.metadata.PixTStatsEntry;
import omeis.providers.re.metadata.PixelsStats;
import omeis.providers.re.quantum.QuantumFactory;

public class Helper
{

    // FIXME where should this live?
    public static Plane2D createPlane(PlaneDef planeDef, int channel, Pixels pixels,
            PixelBuffer buffer) 
    {

        // TODO null checks.

        Integer z = Integer.valueOf(planeDef.getZ());
        Integer c = Integer.valueOf(channel);
        Integer t = Integer.valueOf(planeDef.getT());
        
        ByteBuffer bb;
        try
        {
            bb = buffer.getPlane(z, c, t);
        } catch (IOException e)
        {
            throw new RuntimeException("Error getting plane",e);
        } catch (DimensionsOutOfBoundsException e)
        {
            throw new RuntimeException("Error getting plane",e);
        }

        byte[] planeBuf = new byte[bb.limit()];
        for (int i = 0; bb.hasRemaining(); i++)
        {
            planeBuf[i] = bb.get();
        }

        String type = pixels.getPixelsType().getValue();
        boolean bigEndian = pixels.getBigEndian().booleanValue();

        // BytesConverter strategy = BytesConverter.makeNew(
        // pixFileHeader.pixelType,
        // bigEndian);
        //        
        // Plane2D plane = new XYPlane(
        // planeBuf, planeDef,
        // pixels.getSizeX().intValue(), pixels.getSizeY().intValue(),
        // pixels.getBytesPerPixel().intValue(), strategy);
        // FIXME

        throw new RuntimeException("not implemented yet");

    }
    
    /** 
     * Helper method to create the default settings if none is available.
     * In this case we use a grayscale model to map the first wavelength in
     * the pixels file.  The mapping is linear and the intervals are selected
     * according to a "best-guess" statistical approach.
     * 
     * @param stats For each wavelength, it contains the global minimum and
     *              maximum of the wavelength stack across time.
     * @return  The default rendering settings.
     */
    public static RenderingDef createDefaultRenderingDef(Pixels metadata, PixelsStats stats)
    {
        
        int c_size = metadata.getSizeC().intValue();
        int z_size = metadata.getSizeZ().intValue();
        
        QuantumDef qDef = new QuantumDef();
        qDef.setBitResolution(QuantumFactory.DEPTH_8BIT);
        qDef.setCdStart(0);
        qDef.setCdStop(QuantumFactory.DEPTH_8BIT);
        
        ChannelBindings[] waves = new ChannelBindings[c_size];
        PixTStatsEntry wGlobal;
        int[] rgb = ColorsFactory.getColor(0,0); // FIXME was empty
        Channel pixData;
        boolean active = false;
        int model = RenderingDefConstants.GS;
        Map map = getPixelsChannelData(metadata);
        
        for (int w = 0; w < c_size; ++w) {
            pixData = (Channel) map.get(new Integer(w));
            wGlobal = stats.getGlobalEntry(w);
            if (pixData == null) rgb = ColorsFactory.getColor(w, -1);
            else  rgb = ColorsFactory.getColor(w, pixData.getLogicalChannel().getEmissionWave());
            if (pixData != null && 
                    metadata.getAcquisitionContext().getPhotometricInterpretation().getValue() == "RGB") { 
            	// FIXME this should be linked to the ModelType of RenderingDefConstant somehow
                active = true;
                model = RenderingDefConstants.RGB;
            } 
            waves[w] = new ChannelBindings(w, wGlobal.globalMin, 
                                        wGlobal.globalMax, rgb, active, 
                                        QuantumFactory.LINEAR, 1);
        }
        waves[0].setActive(true);  //NOTE: dims object enforces 1 < sizeC.
        RenderingDef newRD = new RenderingDef();
        int defaultZ = z_size/2+z_size%2-1;
        newRD.setDefaultZ(Integer.valueOf(defaultZ));
        newRD.setDefaultT(Integer.valueOf(0));
        newRD.setModel(RenderingDefConstants.convertToType(model));
        newRD.setQuantization(qDef);
        newRD.setWaveRendering(new HashSet(Arrays.asList(waves)));
        return newRD;
    }
    
    /**
     * Returns the Logical data associated to the pixels set.
     * 
     * @return See above.
     */
    public static Map getPixelsChannelData(Pixels metadata) {
        Map result = new HashMap();
        Set channels = metadata.getChannels();
        for (Iterator it = channels.iterator(); it.hasNext();)
        {
            Channel channel = (Channel) it.next();
            Integer idx = channel.getIndex();
            result.put(idx,channel);
        }
        return result; 
    }
    
}
