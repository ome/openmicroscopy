package tmp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;

import omeis.providers.re.ColorsFactory;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.metadata.ChannelBindings;
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
    public static RenderingDef createDefaultRenderingDef(Pixels metadata)
    {
        
        int c_size = metadata.getSizeC().intValue();
        int z_size = metadata.getSizeZ().intValue();
        
        QuantumDef qDef = new QuantumDef();
        qDef.setBitResolution(new Integer(QuantumFactory.DEPTH_8BIT));
        qDef.setCdStart(new Integer(0));
        qDef.setCdEnd(new Integer(QuantumFactory.DEPTH_8BIT));
        
        ChannelBindings[] waves = new ChannelBindings[c_size];
        boolean active = false;
        int model = RenderingDefConstants.GS;
        
        List channels = metadata.getChannels();
        int w = 0;
        for (Iterator i = channels.iterator(); i.hasNext(); )
        {
        	Channel channel = (Channel) i.next();
        	double gMin = channel.getStatsInfo().getGlobalMin().doubleValue();
        	double gMax = channel.getStatsInfo().getGlobalMax().doubleValue();
            int rgb[] = ColorsFactory.getColor(w, channel);
            if (metadata.getAcquisitionContext().getPhotometricInterpretation().getValue() == "RGB") { 
            	// FIXME this should be linked to the ModelType of RenderingDefConstant somehow
                active = true;
                model = RenderingDefConstants.RGB;
            } 
            waves[w] = new ChannelBindings(w, gMin, gMax, rgb, active, 
            		                       QuantumFactory.LINEAR, 1);
        }
        waves[0].setActive(true);  //NOTE: dims object enforces 1 < sizeC.
        RenderingDef newRD = new RenderingDef();
        int defaultZ = z_size/2+z_size%2-1;
        newRD.setDefaultZ(Integer.valueOf(defaultZ));
        newRD.setDefaultT(Integer.valueOf(0));
        newRD.setModel(RenderingDefConstants.convertToType(model));
        newRD.setQuantization(qDef);
        newRD.setWaveRendering(Arrays.asList(waves));
        return newRD;
    }
}
