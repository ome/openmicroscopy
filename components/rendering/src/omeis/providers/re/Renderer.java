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
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.api.IPixels;
import ome.io.nio.PixelBuffer;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.PixelsType;
import ome.model.enums.RenderingModel;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.metadata.StatsFactory;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;
import omeis.providers.re.quantum.QuantumStrategy;


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
public class Renderer
{
    
    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(Renderer.class);
    
	/** Identifies the type used to store model values. */
	public static final String MODEL_GREYSCALE = "greyscale";

	/** Identifies the type used to store model values. */
	public static final String MODEL_HSB = "hsb";
	
	/** Identifies the type used to store model values. */
	public static final String MODEL_RGB = "rgb";
	
	/** Identifies the type used to store photometric interpretation values. */
	public static final String PHOTOMETRIC_RGB = "RGB";
	
    static final String RGB_COLOR_DOMAIN = "RGB";
    
    /**
     * The {@link Pixels} object to access the metadata of the pixels
     * set bound to this <code>Renderer</code>. 
     */
    private Pixels          metadata;
    
    /** The settings that define the transformation context. */
    private RenderingDef    rndDef;
    
    /** The object that allows to access the pixels raw data. */
    private PixelBuffer     buffer;
  
    /**
     * Manages and allows to retrieve the objects that are used to quantize
     * wavelength data.
     */
	private QuantumManager  quantumManager;
    
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
    
    /** Omero Pixels service */
    private IPixels             iPixels;
     
    /**
     * Creates a new instance to render the specified pixels set.
     * The {@link #initialize() initialize} method has to be called straight
     * after in order to get this new instance ready for rendering.
     * 
     * @param iPixels An IPixels service.
     * @param pixelsObj Pixels object.
     * @param renderingDefObj Rendering definition object.
     * @param bufferObj PixelBuffer object.
     * @param metadata The service to access the metadata of the pixels
     *                 set bound to this <code>Renderer</code>.
     * @throws NullPointerException If <code>null</code> parameters are passed.
     */
    public Renderer(IPixels iPixels, Pixels pixelsObj,
    		        RenderingDef renderingDefObj, PixelBuffer bufferObj)
    { 
        metadata = pixelsObj;
        rndDef = renderingDefObj;
        buffer = bufferObj;
        this.iPixels = iPixels;
        
        if (metadata == null)
        	throw new NullPointerException("Expecting not null metadata");
        else if (rndDef == null)
        	throw new NullPointerException("Expecting not null rndDef");
        else if (buffer == null)
        	throw new NullPointerException("Expecting not null buffer");
                
        //Create and configure the quantum strategies.
        QuantumDef qd = rndDef.getQuantization();
        quantumManager = new QuantumManager(metadata);
        ChannelBinding[] cBindings= getChannelBindings();
        quantumManager.initStrategies(qd, metadata.getPixelsType(), cBindings);
        
        //Compute the location stats.
        computeLocationStats(getDefaultPlaneDef());
        
        //Create and configure the codomain chain.
        codomainChain = new CodomainChain(qd.getCdStart().intValue(), 
                                          qd.getCdEnd().intValue(),
                                          rndDef.getSpatialDomainEnhancement());
        
        
        //Create an appropriate rendering strategy.
        renderingStrategy = RenderingStrategy.makeNew(rndDef.getModel());
    }
    
    /**
     * Specifies the model that dictates how transformed raw data has to be 
     * mapped onto a color space.
     * This class delegates the actual rendering to a {@link RenderingStrategy},
     * which is selected depending on that model. So setting the model also
     * results in changing the rendering strategy.
     * 
     * @param model Identifies the color space model.
     */
	public void setModel(RenderingModel model)
	{
        rndDef.setModel(model);
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
    public void setDefaultZ(int z) { rndDef.setDefaultZ(Integer.valueOf(z)); }
    
    /**
     * Sets the default timepoint index.
     * This index is used to define a default plane.
     * 
     * @param t The timepoint index.
     * @see #setDefaultZ(int)
     * @see #getDefaultPlaneDef()
     */
    public void setDefaultT(int t) { rndDef.setDefaultT(Integer.valueOf(t)); }
    
    /**
     * Creates the default plane definition to use for the generation of the
     * very first image displayed by <i>2D</i> viewers.
     * 
     * @return The default <i>XY</i>-plane.
     */
    public PlaneDef getDefaultPlaneDef()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.XY,
                                    rndDef.getDefaultT().intValue());
        pd.setZ(rndDef.getDefaultZ().intValue());
        return pd;
    }
    
	/**
	 * Updates the {@link QuantumManager} and configures it according to the
	 * current quantum definition.
	 */
    public void updateQuantumManager()
	{
        QuantumDef qd = rndDef.getQuantization();
		ChannelBinding[] cb = getChannelBindings();
		quantumManager.initStrategies(qd, metadata.getPixelsType(), cb);
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
    public RGBBuffer render(PlaneDef pd)
		throws IOException, QuantizationException
	{
		if (pd == null)
			throw new NullPointerException("No plane definition.");
        stats = new RenderingStats(this, pd);
        log.info("Using: '" + renderingStrategy.getClass().getName()
                + "' rendering strategy.");
        RGBBuffer img = renderingStrategy.render(this, pd);
        stats.stop();
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
    public int getImageSize(PlaneDef pd)
    {
        if (pd == null)
            throw new NullPointerException("No plane definition.");
        return renderingStrategy.getImageSize(pd, metadata);
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
    public String getPlaneDimsAsString(PlaneDef pd)
    {
        if (pd == null)
            throw new NullPointerException("No plane definition.");
        return renderingStrategy.getPlaneDimsAsString(pd, metadata);
    }
        
    public ChannelBinding[] getChannelBindings()
    {
        List bindings = rndDef.getWaveRendering();
        return (ChannelBinding[])
            bindings.toArray(new ChannelBinding[bindings.size()]);
    }
    
    /**
     * Returns the settings that define the transformation context.
     * That is, a specification of how raw data is to be transformed into an 
     * image that can be displayed on screen.
     * 
     * @return  See above.
     */
    public RenderingDef getRenderingDef() { return rndDef; }

    /**
     * Returns the object that manages and allows to retrieve the objects 
     * that are used to quantize wavelength data.
     * 
     * @return  See above.
     */
    public QuantumManager getQuantumManager() { return quantumManager; }

    /**
     * Returns the object that allows to access the pixels raw data.
     * 
     * @return  See above.
     */
    public PixelBuffer getPixels() { return buffer; }
    
    public Pixels getMetadata() { return metadata; }
    
    /**
     * Returns the pixels type.
     * 
     * @return A pixels type enumeration object. 
     */
    public PixelsType getPixelsType()
    {
        return metadata.getPixelsType();
    }

    /**
     * Returns the object that defines the sequence of spatial transformations 
     * to be applied to quantized data.
     * 
     * @return  See above.
     */
    public CodomainChain getCodomainChain() { return codomainChain; }   
    
    /**
     * Returns a {@link RenderingStats} object that the rendering strategy
     * can use to track performance.
     * A new stats object is created upon each invocation of the 
     * {@link #render(PlaneDef) render} method.
     * 
     * @return The stats object.
     */
    public RenderingStats getStats() { return stats; }

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
    {
        if (pd == null)
        	throw new NullPointerException("No plane definition.");
        ChannelBinding[] cb = getChannelBindings();
        StatsFactory sf = new StatsFactory();
        int w = 0;
        for (Iterator i = getMetadata().getChannels().iterator(); i.hasNext(); )
        {
        	// FIXME: This is where we need to have the ChannelBinding -->
        	// Channel linkage. Without it, we have to assume that the order in
        	// which the channel bindings was created matches up with the order
        	// of the channels linked to the pixels set.
        	Channel channel = (Channel) i.next();
        	double gMin = channel.getStatsInfo().getGlobalMin().doubleValue();
        	double gMax = channel.getStatsInfo().getGlobalMax().doubleValue();
            //TEst
        	sf.computeLocationStats(metadata, buffer, pd, w);
        	//cb[w].setNoiseReduction(new Boolean(sf.isNoiseReduction()));
            cb[w].setNoiseReduction(Boolean.TRUE);
        	float start = cb[w].getInputStart().floatValue();
        	float end = cb[w].getInputEnd().floatValue();
        	//TODO: find a better way.
        	if (gMax == end && gMin == start)
        		cb[w].setInputStart(new Float(sf.getInputStart()));
        	cb[w].setInputEnd(new Float(sf.getInputEnd()));
        }
    }
    
    //
    // Methods pushed down from RenderingBean
    //
    public void setQuantumStrategy(int bitResolution)
    {
        RenderingDef rd = getRenderingDef();
        QuantumDef qd = rd.getQuantization(), newQd;
        newQd = new QuantumDef();
        newQd.setBitResolution(Integer.valueOf(bitResolution));
        newQd.setCdStart(qd.getCdStart());
        newQd.setCdEnd(qd.getCdEnd());
        rd.setQuantization(newQd);
        updateQuantumManager();
    }
    
    public void setCodomainInterval(int start, int end)
    {
        CodomainChain chain = getCodomainChain();
        chain.setInterval(start, end);
        RenderingDef rd = getRenderingDef();
        QuantumDef qd = rd.getQuantization(), newQd;
        newQd = new QuantumDef();
        newQd.setBitResolution(qd.getBitResolution());
        newQd.setCdStart(Integer.valueOf(start));
        newQd.setCdEnd(Integer.valueOf(end));
        rd.setQuantization(newQd);
        CodomainMapContext mapCtx;
        Iterator i = rd.getSpatialDomainEnhancement().iterator();
        while (i.hasNext())
        {
            mapCtx = (CodomainMapContext) i.next();
            mapCtx.setCodomain(start, end);
        }

    }
    
    public void setChannelWindow(int w, double start, double end)
    {
        QuantumStrategy qs = getQuantumManager().getStrategyFor(w);
        qs.setWindow(start, end);
        ChannelBinding[] cb = getChannelBindings();
        cb[w].setInputStart(new Float(start));
        cb[w].setInputEnd(new Float(end)); // TODO double / Float
    }
    
    public void setQuantizationMap(int w, Family family,
            double coefficient, boolean noiseReduction)
    {
        QuantumStrategy qs = getQuantumManager().getStrategyFor(w);
        qs.setQuantizationMap(family, coefficient, noiseReduction);
        ChannelBinding[] cb = getChannelBindings();
        cb[w].setFamily(family);
        cb[w].setCoefficient(coefficient);
        cb[w].setNoiseReduction(noiseReduction);
    }

    public void setRGBA(int w, int red, int green, int blue, int alpha)
    {
        ChannelBinding[] cb = getChannelBindings();
        Color c = cb[w].getColor();
        c.setRed(Integer.valueOf(red));
        c.setGreen(Integer.valueOf(green));
        c.setBlue(Integer.valueOf(blue));
        c.setAlpha(Integer.valueOf(alpha));
    }
    
    public void resetDefaults()
    {
        // Reset the bit resolution.
        setQuantumStrategy(QuantumFactory.DEPTH_8BIT); // NB: Java locks are
        setCodomainInterval(0, QuantumFactory.DEPTH_8BIT); // re-entrant.

        // Set the each channel's window to the channel's [min, max].
        // Make active only the first channel.
        ChannelBinding[] cb = getChannelBindings();
        boolean active = false;
        RenderingModel model =
        	PlaneFactory.getRenderingModel(iPixels, MODEL_GREYSCALE);
        
        List channels = getMetadata().getChannels();
        int w = 0;
        for (Iterator i = channels.iterator(); i.hasNext();)
        {
        	// The channel we're operating on
        	Channel channel = (Channel) i.next();
            if (channel.getPixels().getAcquisitionContext().
            	getPhotometricInterpretation().getValue() == PHOTOMETRIC_RGB)
            {
                active = true;
                model = PlaneFactory.getRenderingModel(iPixels, MODEL_RGB);
            }
            cb[w].setActive(Boolean.valueOf(active));
            double start = channel.getStatsInfo().getGlobalMin().doubleValue();
            double end   = channel.getStatsInfo().getGlobalMax().doubleValue();
            setChannelWindow(w, start, end);
            Color c = ColorsFactory.getColor(w, channel);
            setRGBA(w, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
            w++;
        }
        cb[0].setActive(Boolean.valueOf(active));
        // Remove all the codomainMapCtx except the identity.
        getCodomainChain().remove();

        // Fall back to the default strategy.
        setModel(model);
    }
    
}
