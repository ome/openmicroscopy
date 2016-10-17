/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.conditions.ResourceError;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;

import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * Transforms a plane within a given pixels set into a greyscale image.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/22 17:09:48 $) </small>
 * @since OME2.2
 */
class GreyScaleStrategy extends RenderingStrategy {

    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(GreyScaleStrategy.class);
    
    /** The channel we're operating on */
    private int channel;
    
    /** The channel binding we're using */
    private ChannelBinding channelBinding;

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
    @Override
    RGBBuffer render(Renderer ctx, PlaneDef planeDef) throws IOException,
            QuantizationException {
        // Set the context and retrieve objects we're gonna use.
        renderer = ctx;
        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        Pixels metadata = renderer.getMetadata();
        initAxesSize(planeDef, metadata);
        if (!findFirstActiveChannelBinding())
        {
            return getRgbBuffer();
        }
        PixelBuffer pixels = renderer.getPixels();
        RenderingStats performanceStats = renderer.getStats();
        QuantumStrategy qs = 
        	renderer.getQuantumManager().getStrategyFor(channel);
        CodomainChain cc = renderer.getCodomainChain(channel);
        
        // Retrieve the planar data to render
        performanceStats.startIO(channel);
        Plane2D plane =
        	PlaneFactory.createPlane(planeDef, channel, metadata, pixels);
        performanceStats.endIO(channel);

        RGBBuffer buf = getRgbBuffer();
        
        byte value;
        float alpha = channelBinding.getAlpha().floatValue() / 255;

        int x1, x2, discreteValue, pixelIndex;
        byte[] r = buf.getRedBand();
        byte[] g = buf.getBlueBand();
        byte[] b = buf.getGreenBand();
        boolean hasMapContext = cc.hasMapContext();
        if (plane.isXYPlanar())
        {
        	int planeSize = sizeX1 * sizeX2;
            for (int i = 0; i < planeSize; i++)
            {
                for (x1 = 0; x1 < sizeX1; ++x1)
                {
                    discreteValue = qs.quantize(plane.getPixelValue(i));
                    if (hasMapContext) {
                        discreteValue = cc.transform(discreteValue);
                    }
                    value = (byte) (discreteValue * alpha);
                    r[i] = value;
                    g[i] = value;
                    b[i] = value;
                }
            }
        }
        else
        {
        	for (x2 = 0; x2 < sizeX2; ++x2) {
        		for (x1 = 0; x1 < sizeX1; ++x1) {
        			pixelIndex = sizeX1 * x2 + x1;
        			discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
        			if (hasMapContext) {
                        discreteValue = cc.transform(discreteValue);
                    }
        			value = (byte) (discreteValue * alpha);
        			r[pixelIndex] = value;
        			g[pixelIndex] = value;
        			b[pixelIndex] = value;
        		}
        	}
        }
        return buf;
    }
    
    /**
	 * Implemented as specified by the superclass.
	 * 
	 * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
	 */
	@Override
	RGBIntBuffer renderAsPackedInt(Renderer ctx, PlaneDef planeDef)
	        throws IOException, QuantizationException {
        // Set the context and retrieve objects we're gonna use.
        renderer = ctx;
        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        Pixels metadata = renderer.getMetadata();
        initAxesSize(planeDef, metadata);
        if (!findFirstActiveChannelBinding())
        {
            return getIntBuffer();
        }
        PixelBuffer pixels = renderer.getPixels();
        RenderingStats performanceStats = renderer.getStats();
        QuantumStrategy qs = 
        	renderer.getQuantumManager().getStrategyFor(channel);
        CodomainChain cc = renderer.getCodomainChain(channel);
        
        // Retrieve the planar data to render
        
        Plane2D plane;
        try {
        	performanceStats.startIO(channel);
        	plane = PlaneFactory.createPlane(planeDef, channel, metadata, pixels);
        	performanceStats.endIO(channel);
		} finally
		{
			try
            {
                pixels.close();
            } 
            catch (IOException e)
            {
                log.error("Pixels could not be closed successfully.", e);
    			throw new ResourceError(
    					e.getMessage() + " Please check server log.");
            }
		}
       
	    RGBIntBuffer dataBuf = getIntBuffer();
	    
        int alpha = channelBinding.getAlpha();
        int[] buf = ((RGBIntBuffer) dataBuf).getDataBuffer();
        int x1, x2, discreteValue, pixelIndex;
        boolean hasMapContext = cc.hasMapContext();
        if (plane.isXYPlanar())
        {
        	int planeSize = sizeX1 * sizeX2;
        	for (int i = 0; i < planeSize; i++)
        	{
                discreteValue = qs.quantize(plane.getPixelValue(i));
                if (hasMapContext) {
                    discreteValue = cc.transform(discreteValue);
                }
                buf[i] = alpha << 24 | discreteValue << 16
                        | discreteValue << 8 | discreteValue;
        	}
        }
        else
        {
        	for (x2 = 0; x2 < sizeX2; ++x2) {
        		pixelIndex = sizeX1 * x2;
        		for (x1 = 0; x1 < sizeX1; ++x1) {
        			discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
        			if (hasMapContext) {
                        discreteValue = cc.transform(discreteValue);
                    }
        			buf[pixelIndex + x1] = alpha << 24 | discreteValue << 16
        			| discreteValue << 8 | discreteValue;
        		}
        	}
        }
	    return dataBuf;
	}

	/**
	 * Implemented as specified by the superclass.
	 * 
	 * @see RenderingStrategy#renderAsPackedIntAsRGBA(Renderer ctx, PlaneDef planeDef)
	 */
	@Override
	RGBAIntBuffer renderAsPackedIntAsRGBA(Renderer ctx, PlaneDef planeDef)
	        throws IOException, QuantizationException {
        // Set the context and retrieve objects we're gonna use.
        renderer = ctx;
        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        Pixels metadata = renderer.getMetadata();
        initAxesSize(planeDef, metadata);
        if (!findFirstActiveChannelBinding())
        {
            return getRGBAIntBuffer();
        }
        PixelBuffer pixels = renderer.getPixels();
        RenderingStats performanceStats = renderer.getStats();
        QuantumStrategy qs = 
        	renderer.getQuantumManager().getStrategyFor(channel);
        CodomainChain cc = renderer.getCodomainChain(channel);
        
        // Retrieve the planar data to render
        performanceStats.startIO(channel);
        Plane2D plane =
        	PlaneFactory.createPlane(planeDef, channel, metadata, pixels);
        performanceStats.endIO(channel);
	
	    RGBAIntBuffer dataBuf = getRGBAIntBuffer();
	    
        int alpha = channelBinding.getAlpha();
        int[] buf = ((RGBAIntBuffer) dataBuf).getDataBuffer();
        int x1, x2, discreteValue, pixelIndex;
        boolean hasMapContext = cc.hasMapContext();
        if (plane.isXYPlanar())
        {
        	int planeSize = sizeX1 * sizeX2;
        	for (int i = 0; i < planeSize; i++)
        	{
                discreteValue = qs.quantize(plane.getPixelValue(i));
                if (hasMapContext) {
                    discreteValue = cc.transform(discreteValue);
                }
                buf[i] = alpha | discreteValue << 24
                        | discreteValue << 16 | discreteValue << 8;
        	}
        }
        else
        {
        	for (x2 = 0; x2 < sizeX2; ++x2) {
        		pixelIndex = sizeX1 * x2;
        		for (x1 = 0; x1 < sizeX1; ++x1) {
        			discreteValue = qs.quantize(plane.getPixelValue(x1, x2));
        			discreteValue = cc.transform(discreteValue);
        			buf[pixelIndex + x1] = alpha | discreteValue << 24
        			| discreteValue << 16 | discreteValue << 8;
        		}
        	}
        }
	    return dataBuf;
	}


	/**
	 * Initializes the first active channel binding for the current rendering
	 * context.
	 *
	 * @return <code>true</code> when an active channel binding can be
	 * located and <code>false</code> otherwise.
	 */
	private boolean findFirstActiveChannelBinding()
	{
		ChannelBinding[] channelBindings = renderer.getChannelBindings();
		for (int i = 0; i < channelBindings.length; i++)
		{
			if (channelBindings[i].getActive())
			{
				channel = i;
				channelBinding = channelBindings[i];
				return true;
			}
		}
		return false;
	}

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#getImageSize(PlaneDef, Pixels)
     */
    @Override
    int getImageSize(PlaneDef pd, Pixels pixels) {
        initAxesSize(pd, pixels);
        return sizeX1 * sizeX2 * 3;
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#getPlaneDimsAsString(PlaneDef, Pixels)
     */
    @Override
    String getPlaneDimsAsString(PlaneDef pd, Pixels pixels) {
        initAxesSize(pd, pixels);
        return sizeX1 + "x" + sizeX2;
    }

}
