/*
 * omeis.providers.re.Renderer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package omeis.providers.re;


//Java imports
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.io.nio.PixelBuffer;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.PixelsType;

import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.metadata.ChannelBindings;
import omeis.providers.re.metadata.PixMetadataException;
import omeis.providers.re.metadata.PixTStatsEntry;
import omeis.providers.re.metadata.PixelsStats;
import omeis.providers.re.metadata.StatsFactory;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;

import tmp.RenderingDefConstants;

/** 
 * Transforms raw image data into an <i>RGB</i> image that can be displayed on
 * screen. 
 * <p>Every instance of this class works against a given pixels set within an
 * <i>OME</i> Image (recall that an Image can have more than one pixels set)
 * and holds the rendering environment for that pixels set.  Said environment
 * is composed of:</p>
 * <ul>
 *  <li>Resources to access pixels raw data and metadata.</li>
 *  <li>Cached pixels metadata (statistic measurements).</li>
 *  <li>Settings that define the transformation context &#151; that is, a 
 *   specification of how raw data is to be transformed into an image that
 *   can be displayed on screen.</li>
 *  <li>Resources to apply the transformations defined by the transformation
 *   context to raw pixels.</li>
 * </ul>
 * <p>This class delegates the actual rendering to a {@link RenderingStrategy},
 * which is selected depending on how transformed data is to be mapped into
 * a color space.</p>
 *
 * @see PixelsMetadata
 * @see PixelsData
 * @see RenderingDef
 * @see QuantumManager
 * @see CodomainChain
 * @see RenderingStrategy
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.8 $ $Date: 2005/07/05 16:13:52 $)
 * </small>
 * @since OME2.2
 */
class Renderer
{
    private static Log log = LogFactory.getLog(Renderer.class);//j.m
    
    /**
     * The {@link Pixels} object to access the metadata of the pixels
     * set bound to this <code>Renderer</code>. 
     */
    private Pixels      metadata;
    private ome.model.display.RenderingDef rndDef;
    private PixelBuffer buffer;
  
    /* necessary? */
    private StatsFactory sf;
    private PixelsStats pixelStats;
    
    /**
     * Manages and allows to retrieve the objects that are used to quantize
     * wavelength data.
     */
	private QuantumManager		quantumManager;
    
    /**
     * Defines the sequence of spatial transformations to apply to quantized
     * data.
     */
    private CodomainChain       codomainChain;
    
	/**
     * Takes care of the actual rendering, using this <code>Renderer</code> as
     * a rendering context.
	 */
	private RenderingStrategy	renderingStrategy;
    
    /**
     * Collects performance measurements during each invocation of the
     * {@link #render(PlaneDef) render} method.
     */
    private RenderingStats      stats;
     
    /**
     * Creates a new instance to render the specified pixels set.
     * The {@link #initialize() initialize} method has to be called straight
     * after in order to get this new instance ready for rendering.
     * 
     * @param metadata The service to access the metadata of the pixels
     *                 set bound to this <code>Renderer</code>.
     */
    public Renderer(
            Pixels pixelsObj, 
            ome.model.display.RenderingDef renderingDefObj,
            PixelBuffer bufferObj,
            PixelsStats pixel_stats
            ) throws Exception // FIXME what exceptions?
    { 

        if (metadata == null || rndDef == null || buffer == null)
            throw new IllegalArgumentException("No null values please.");
        
        metadata = pixelsObj;
        rndDef = renderingDefObj;
        buffer = bufferObj;
        pixelStats = pixel_stats;
        
        //Grab pixels metadata -- create default display options if none 
        //available.
        PixelsDimensions pixelsDims = metadata.getPixelsDimensions();
        
        //Create and configure the quantum strategies.
        QuantumDef qd = rndDef.getQuantization();
        quantumManager = new QuantumManager(metadata.getSizeC().intValue());
        ChannelBinding[] cBindings= getChannelBindings();
        quantumManager.initStrategies(qd, metadata.getPixelsType(), pixelStats, cBindings);
        
        //Compute the location stats.
        computeLocationStats(getDefaultPlaneDef());
        
        //Create and configure the codomain chain.
        codomainChain = new CodomainChain(qd.getCdStart().intValue(), qd.getCdStop().intValue(),
                                          rndDef.getSpatialDomainEnhancement());
        
        
        //Create an appropriate rendering strategy.
        int m = RenderingDefConstants.convertType(rndDef.getModel());
        renderingStrategy = RenderingStrategy.makeNew(m);
    
    }
    
    /**
     * Specifies the model that dictates how transformed raw data has to be 
     * mapped onto a color space.
     * This class delegates the actual rendering to a {@link RenderingStrategy},
     * which is selected depending on that model.  So setting the model also
     * results in changing the rendering strategy.
     * 
     * @param model Identifies the color space model.  One of the constants
     *              defined by {@link RenderingDef}.
     */
	void setModel(int model)
	{
        rndDef.setModel(RenderingDefConstants.convertToType(model));
		renderingStrategy = RenderingStrategy.makeNew(model);
	}
	
    /**
     * Sets the index of the default focal section.
     * This index is used to define a default plane.
     *  
     * @param z The stack index.
     * @see #setDefaultT(int)
     * @see #getDefaultPlaneDef()
     */
    void setDefaultZ(int z) 
    {
        rndDef.setDefaultZ(Integer.valueOf(z));
    }
    
    /**
     * Sets the default timepoint index.
     * This index is used to define a default plane.
     * 
     * @param t The timepoint index.
     * @see #setDefaultZ(int)
     * @see #getDefaultPlaneDef()
     */
    void setDefaultT(int t) 
    {
        rndDef.setDefaultT(Integer.valueOf(t));
    }
    
    /**
     * Creates the default plane definition to use for the generation of the
     * very first image displayed by <i>2D</i> viewers.
     * 
     * @return Said plane definition.
     */
    PlaneDef getDefaultPlaneDef()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.XY, rndDef.getDefaultT().intValue());
        pd.setZ(rndDef.getDefaultZ().intValue());
        return pd;
    }
    
	/**
	 * Updates the {@link QuantumManager} and configures it according to the
	 * current quantum definition.
	 */
	void updateQuantumManager()
	{
        QuantumDef qd = rndDef.getQuantization();
		ChannelBinding[] cb = getChannelBindings();
		quantumManager.initStrategies(qd, metadata.getPixelsType(),getPixelsStats(), cb);
	}
	
	/**
	 * Renders the data selected by <code>pd</code> according to the current
	 * rendering settings.
	 * The passed argument selects a plane orthogonal to one of the <i>X</i>, 
	 * <i>Y</i>, or <i>Z</i> axes.  How many wavelengths are rendered and
	 * what color model is used depends on the current rendering settings.
	 * 
	 * @param pd Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
	 *           or <i>Z</i> axes.
	 * @return An <i>RGB</i> image ready to be displayed on screen.
	 * @throws IOException If an error occured while trying to pull out
	 * 					   data from the pixels data repository.
     * @throws QuantizationException If an error occurred while quantizing the
     *                               pixels raw data.
     * @throws NullPointerException If <code>pd</code> is <code>null</code>.
	 */
	RGBBuffer render(PlaneDef pd)
		throws IOException, QuantizationException
	{
		if (pd == null)
			throw new NullPointerException("No plane definition.");
        stats = new RenderingStats(this, pd);
        RGBBuffer img = renderingStrategy.render(this, pd);
        stats.stop();
        //j.m Logger log = Env.getSvcRegistry().getLogger();
        log.info(stats.getStats());
        //TODO: is this the right place to log??? We want to have as little
        //impact on performance as possible.
        return img;
	}

    /**
     * Returns the size, in bytes, of the {@link RGBBuffer} that would be
     * rendered from the plane selected by <code>pd</code>.
     * Note that the returned value also depends on the current rendering
     * strategy which is selected by the {@link #setModel(int) setModel} method.
     * So a subsequent invocation of this method may return a different value
     * if the {@link #setModel(int) setModel} method has been called since the
     * first call to this method.
     * 
     * @param pd Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *           or <i>Z</i> axes.
     * @return  See above.
     * @throws NullPointerException If <code>pd</code> is <code>null</code>.
     */
    int getImageSize(PlaneDef pd)
    {
        if (pd == null)
            throw new NullPointerException("No plane definition.");
        return renderingStrategy.getImageSize(pd, metadata.getPixelsDimensions());
    }
    
    /**
     * Returns a string with the dimensions of the specified plane.
     * The returned string has the format <code>AxB</code>, where <code>A</code>
     * is the number of pixels on the <i>X1</i>-axis and <code>B</code> the
     * the number of pixels on the the <i>X2</i>-axis.
     * The <i>X1</i>-axis is the <i>X</i>-axis in the case of an <i>XY</i> or 
     * <i>XZ</i> plane.  Otherwise it is the <i>Z</i>-axis &#151; <i>ZY</i>
     * plane.
     * The <i>X2</i>-axis is the <i>Y</i>-axis in the case of an <i>XY</i> or 
     * <i>ZY</i> plane.  Otherwise it is the <i>Z</i>-axis &#151; <i>XZ</i>
     * plane. 
     * 
     * @param pd    Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *              or <i>Z</i> axes.
     * @return See above.
     * @throws NullPointerException If <code>pd</code> is <code>null</code>.
     */
    String getPlaneDimsAsString(PlaneDef pd)
    {
        if (pd == null)
            throw new NullPointerException("No plane definition.");
        return renderingStrategy.getPlaneDimsAsString(pd, 
                metadata.getPixelsDimensions());
    }
        
    ChannelBinding[] getChannelBindings()
    {
        Set bindings = rndDef.getWaveRendering();
        return (ChannelBinding[]) bindings.toArray(new ChannelBinding[bindings.size()]);
    }
    
    /**
     * Returns the dimensions of the <i>5D</i> array containing the pixels set
     * this object renders.
     * 
     * @return  See above.
     */
	PixelsDimensions getPixelsDims() { return metadata.getPixelsDimensions(); }

    /**
     * Returns statistic measurements on the the pixels set this object 
     * renders.
     * 
     * @return  See above.
     */
	PixelsStats getPixelsStats() 
    { 
        return pixelStats;
    }

    /**
     * Returns the settings that define the transformation context.
     * That is, a specification of how raw data is to be transformed into an 
     * image that can be displayed on screen.
     * 
     * @return  See above.
     */
	RenderingDef getRenderingDef() { return rndDef; }

    /**
     * Returns the object that manages and allows to retrieve the objects 
     * that are used to quantize wavelength data.
     * 
     * @return  See above.
     */
	QuantumManager getQuantumManager() { return quantumManager; }

    /**
     * Returns the object that allows to access the pixels raw data.
     * 
     * @return  See above.
     */
	PixelBuffer getPixels() { return buffer; }
    Pixels getMetadata() { return metadata; }
    
    /**
     * Returns the string identifier of the pixels type.
     * 
     * @return One of the string identifiers: "UINT8", "INT8", etc. 
     */
    String getPixelsType()
    {
        return metadata.getPixelsType().getValue();
    }

    /**
     * Returns the object that defines the sequence of spatial transformations 
     * to be applied to quantized data.
     * 
     * @return  See above.
     */
	CodomainChain getCodomainChain() { return codomainChain; }   
    
    /**
     * Returns a {@link RenderingStats} object that the rendering strategy
     * can use to track performance.
     * A new stats object is created upon each invocation of the 
     * {@link #render(PlaneDef) render} method.
     * 
     * @return The stats object.
     */
    RenderingStats getStats() { return stats; }

    /*
     * 
     * TEMPORARILY COPIED HERE FROM PixelsMetadataImpl
     * 
     */
    
    /**
     * Implemented as specified by the {@link PixelsMetadata} interface.
     * @see PixelsMetadata#computeLocationStats(PlaneDef)
     */
    public void computeLocationStats(PlaneDef pd)
        throws PixMetadataException
    {
        if (pd == null) throw new NullPointerException("No plane definition.");
        if (rndDef == null) return; // TODO can this be true? -josh
        try {
            ChannelBinding[] cb = getChannelBindings();
            double start, end;
            PixTStatsEntry pixTStats;
            for (int i = 0; i < cb.length; i++) {
                pixTStats = pixelStats.getGlobalEntry(cb[i].getIndex());
                sf.computeLocationStats(metadata, buffer, pixelStats, 
                                        pd, cb[i].getIndex());
                // FIXME cb[i].setStats(sf.getLocationStats());
                cb[i].setNoiseReduction(sf.isNoiseReduction());
                start = cb[i].getInputStart();
                end = cb[i].getInputEnd();
                //TODO: find a better way.
                if (pixTStats.globalMax == end && pixTStats.globalMin == start)
                    cb[i].setInputStart(new Float(sf.getInputStart()));
                	   cb[i].setInputEnd(new Float(sf.getInputEnd())); // TODO double / Float? 
            }
        } catch (Exception ioe) {
            throw new PixMetadataException("Cannot retrieve the file header "+
                                        "to compute the location stats.", ioe);
        }
    }
    
}
