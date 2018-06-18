/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.conditions.ResourceError;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.enums.RenderingModel;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.codomain.ReverseIntensityContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.data.RegionDef;
import omeis.providers.re.lut.LutProvider;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * Transforms raw image data into an <i>RGB</i> image that can be displayed on
 * screen.
 * <p>
 * Every instance of this class works against a given pixels set within an
 * <i>OME</i> Image (recall that an Image can have more than one pixels set)
 * and holds the rendering environment for that pixels set. Said environment is
 * composed of:
 * </p>
 * <ul>
 * <li>Resources to access pixels raw data and metadata.</li>
 * <li>Cached pixels metadata (statistic measurements).</li>
 * <li>Settings that define the transformation context &#151; that is, a
 * specification of how raw data is to be transformed into an image that can be
 * displayed on screen.</li>
 * <li>Resources to apply the transformations defined by the transformation
 * context to raw pixels.</li>
 * </ul>
 * <p>
 * This class delegates the actual rendering to a {@link RenderingStrategy},
 * which is selected depending on how transformed data is to be mapped into a
 * color space.
 * </p>
 * 
 * @see RenderingDef
 * @see QuantumManager
 * @see CodomainChain
 * @see RenderingStrategy
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/07/05 16:13:52 $) </small>
 * @since OME2.2
 */
public class Renderer {

    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(Renderer.class);
    
    /** The maximum number of channels. */
    public static final int		MAX_CHANNELS = 8;
    
    /** Identifies the type used to store model values. */
    public static final String MODEL_GREYSCALE = RenderingModel.VALUE_GREYSCALE;

    /** Identifies the type used to store model values. */
    public static final String MODEL_RGB = RenderingModel.VALUE_RGB;

    /** Identifies the type used to store model values. */
    public static final String MODEL_HSB = MODEL_RGB;

    /** Identifies the type used to store photometric interpretation values. */
    @Deprecated
    public static final String PHOTOMETRIC_MONOCHROME = PhotometricInterpretation.VALUE_MONOCHROME;

    /**
     * The {@link Pixels} object to access the metadata of the pixels set bound
     * to this <code>Renderer</code>.
     */
    private Pixels metadata;

    /** The settings that define the transformation context. */
    private RenderingDef rndDef;

    /** The object that allows access the raw pixel data. */
    private PixelBuffer buffer;

    /**
     * Manages and allows to retrieve the objects that are used to quantize
     * wavelength data.
     */
    private QuantumManager quantumManager;

    /**
     * Defines the sequence of spatial transformations to apply to quantized
     * data.
     */
    private List<CodomainChain> codomainChains;

    /**
     * Takes care of the actual rendering, using this <code>Renderer</code> as
     * a rendering context.
     */
    private RenderingStrategy renderingStrategy;

    /**
     * Collects performance measurements during each invocation of the
     * {@link #render(PlaneDef) render} method.
     */
    private RenderingStats stats;

    /** Renderer optimizations. */
    private Optimizations optimizations = new Optimizations();
    
    /** Map of overlays we've currently been told to render. */
    private Map<byte[], Integer> overlays;

    /** Lookup table provider. */
    private LutProvider lutProvider;

    /**
     * Returns a copy of a list of channel bindings with one element removed;
     * the so called "other" channel bindings for the image.
     * 
     * @param bindings	The original bindings.
     * @param toRemove	The bindings to remove.
     * @throws IllegalArgumentException if the <code>toRemove</code> channel
     * binding is not present in the list.
     * @return See above.
     */
    private List<ChannelBinding> getOtherBindings(
    		List<ChannelBinding> bindings, ChannelBinding toRemove)
    {
    	if (!bindings.contains(toRemove))
    		throw new IllegalArgumentException(
    				"Channel binding not found in list.");
    	List<ChannelBinding> otherBindings =
    		new ArrayList<ChannelBinding>(bindings.size() - 1);
    	for (ChannelBinding binding : bindings)
    	{
    		if (binding != toRemove)
    			otherBindings.add(binding);
    	}
    	return otherBindings;
    }
    
	/**
	 * Returns <code>true</code> if the color is black or white, 
	 * <code>false</code> otherwise.
	 * 
	 * @param color The array representing an RGB color.
	 * @return See above.
	 */
	private boolean isEndColor(int[] color)
	{
		if (color[0] == 255 && color[1] == 255 && color[2] == 255)
			return true;
		if (color[0] == 0 && color[1] == 0 && color[2] == 0) return true;
		return false;
	}
	
	/**
     * Checks to see if we can enable specific optimizations for "primary" color
     * rendering and alphaless rendering.
     * 
     * Alphaless rendering is only enabled when each of the active channels has
     * no alpha blending (alpha of 0xFF [255]).
     * 
     * Primary color rendering optimizations are only enabled when the
     * number of active channels < 4, each of the active channels is mapped
     * to a primary color (0xFF0000 [Red], 0x00FF00 [Green], 0x0000FF [Blue])
     * and there are no duplicate mappings (two channels mapped to Green for 
     * example). It is also dependent on alphaless rendering being enabled.
     */
    private void checkOptimizations()
    {
    	List<ChannelBinding> channelBindings = getChannelBindingsAsList();
    	
    	for (ChannelBinding channelBinding : channelBindings)
    	{
    		boolean isActive = channelBinding.getActive();
    		if (isActive && channelBinding.getAlpha() != 255)
    		{
    			log.info("Disabling alphaless rendering and " +
    					"PriColor rendering.");
    			optimizations.setAlphalessRendering(false);
    			return;
    		}
    	}
    	log.info("Enabling alphaless rendering.");
    	optimizations.setAlphalessRendering(true);
    	
    	int channelsActive = 0;
    	for (ChannelBinding channelBinding : channelBindings)
    	{
    		// First lets check and see if we have more than 3 channels active.
    		if (channelBinding.getActive() == false)
    			continue;
    		channelsActive++;
    		
    		if (overlays != null && overlays.size() > 0)
    		{
    			log.info("Disabling PriColor rendering, have overlays.");
    			optimizations.setPrimaryColorEnabled(false);
    			return;
    		}
    		if (channelsActive > 3)
    		{
    			log.info("Disabling PriColor rendering, active channels > 3");
    			optimizations.setPrimaryColorEnabled(false);
    			return;
    		}
    		// Check if there is no lut
    		if (StringUtils.isNotBlank(channelBinding.getLookupTable())) {
    		    optimizations.setPrimaryColorEnabled(false);
                return;
    		}
			// Now we ensure the color is "primary" (Red, Green or Blue).
			boolean isPrimary = false;
			int[] colorArray = getColorArray(channelBinding);
			if (isEndColor(colorArray)) {
				optimizations.setPrimaryColorEnabled(false);
				return;
			}
			for (int value : colorArray)
			{
				if (value != 0 && value != 255)
				{
					log.info("Disabling PriColor rendering, " +
							"channel color not primary.");
					optimizations.setPrimaryColorEnabled(false);
					return;
				}
				if (value == 255)
				{
					if (isPrimary == true)
					{
						log.info("Disabling PriColor rendering, " +
								"duplicate channel color component.");
						optimizations.setPrimaryColorEnabled(false);
						return;
					}
					isPrimary = true;
				}
			}
			
    		// Finally we check to make sure that the color is different from
			// all other channels that are active.
			List<ChannelBinding> otherBindings =
				getOtherBindings(channelBindings, channelBinding);
    		for (ChannelBinding otherChannelBinding : otherBindings)
    		{
    			if (otherChannelBinding.getActive() == false)
    				continue;
    			
    			int[] otherColorArray =
    				getColorArray(otherChannelBinding);
    			for (int i = 0; i < colorArray.length; i++)
    			{
    				if (colorArray[i] == otherColorArray[i]
    				    && colorArray[i] != 0)
    				{
    					log.info("Disabling PriColor rendering, " +
    							"duplicate channel color.");
    					optimizations.setPrimaryColorEnabled(false);
    					return;
    				}
    			}
    		}
    	}
    	
    	// All checks have passed, enable "primary" color rendering.
    	log.info("Enabling primary color rendering.");
    	optimizations.setPrimaryColorEnabled(true);
    }

    /**
     * Checks the region definition to ensure that the requested tile width
     * and height are valid with respect to the current resolution level.
     * @param rd Requested region definition.
     */
    private void checkRegionDef(RegionDef rd)
    {
        if (rd == null)
        {
            return;
        }
        // We're using the buffer X and Y size because of the
        // possibility that we're on a resolution level where
        // Pixels.Size[X,Y] != PixelBuffer.Size[X,Y].
        int sizeX = buffer.getSizeX();
        int sizeY = buffer.getSizeY();
        int x = rd.getX();
        int y = rd.getY();
        if ((rd.getWidth() + x) > sizeX)
        {
            rd.setWidth(sizeX - x);
        }
        if ((rd.getHeight() + y) > sizeY)
        {
            rd.setHeight(sizeY - y);
        }
    }

    /**
     * Converts the context.
     *
     * @param ctx The value to convert.
     * @return See above.
     */
    private CodomainMapContext convert(ome.model.display.CodomainMapContext ctx)
    {
        if (ctx instanceof ome.model.display.ReverseIntensityContext) {
            return new ReverseIntensityContext();
        }
        return null;
    }

    /**
     * Creates a new instance to render the specified pixels set and get this
     * new instance ready for rendering.
     * 
     * @param quantumFactory a populated quantum factory.
     * @param renderingModels an enumerated list of all rendering models.
     * @param pixelsObj Pixels object.
     * @param renderingDefObj Rendering definition object.
     * @param bufferObj PixelBuffer object.
     * @param lutProvider provider of the available lookup tables.
     * @throws NullPointerException If <code>null</code> parameters are passed.
     */
    public Renderer(QuantumFactory quantumFactory,
    		List<RenderingModel> renderingModels, Pixels pixelsObj,
            RenderingDef renderingDefObj, PixelBuffer bufferObj,
            LutProvider lutProvider) {
        metadata = pixelsObj;
        rndDef = renderingDefObj;
        buffer = bufferObj;
        this.lutProvider = lutProvider;
        if (metadata == null) {
            throw new NullPointerException("Expecting not null metadata");
        } else if (rndDef == null) {
            throw new NullPointerException("Expecting not null rndDef");
        } else if (buffer == null) {
            throw new NullPointerException("Expecting not null buffer");
        }

   
        // Create and configure the quantum strategies.
        QuantumDef qd = rndDef.getQuantization();
        quantumManager = new QuantumManager(metadata, quantumFactory);
        ChannelBinding[] cBindings = getChannelBindings();
        quantumManager.initStrategies(qd, cBindings);

        // Create and configure the codomain chain.
        
        codomainChains = new ArrayList<CodomainChain>();
        ChannelBinding cb;
        for (int i = 0; i < cBindings.length; i++) {
            cb = cBindings[i];
            List<ome.model.display.CodomainMapContext> l = cb.<ome.model.display.CodomainMapContext>
            collectSpatialDomainEnhancement(null);
            List<CodomainMapContext> nl = new ArrayList<CodomainMapContext>();
            if (l != null && l.size() > 0) {
                Iterator<ome.model.display.CodomainMapContext> j = l.iterator();
                while (j.hasNext()) {
                    CodomainMapContext ctx = convert(j.next());
                    if (ctx != null) {
                        nl.add(ctx);
                    }
                }
            }
            codomainChains.add(new CodomainChain(qd.getCdStart().intValue(),
                    qd.getCdEnd().intValue(), nl));
        }

        // Create an appropriate rendering strategy.
        renderingStrategy = RenderingStrategy.makeNew(rndDef.getModel());
        
        // Examine the metadata we've been given and enable optimizations.
        checkOptimizations();
    }

    /**
     * Returns the current lookup table provider.
     *
     * @return See above.
     */
    LutProvider getLutProvider()
    {
        return lutProvider;
    }

    /**
     * Specifies the model that dictates how transformed raw data has to be
     * mapped onto a color space. This class delegates the actual rendering to a
     * {@link RenderingStrategy}, which is selected depending on that model. So
     * setting the model also results in changing the rendering strategy.
     * 
     * @param model
     *            Identifies the color space model.
     */
    public void setModel(RenderingModel model)
    {
        rndDef.setModel(model);
        renderingStrategy = RenderingStrategy.makeNew(model);
    }

    /**
     * Sets the index of the default focal section. This index is used to define
     * a default plane.
     * 
     * @param z
     *            The stack index.
     * @see #setDefaultT(int)
     */
    public void setDefaultZ(int z)
    {
        rndDef.setDefaultZ(Integer.valueOf(z));
    }

    /**
     * Sets the default timepoint index. This index is used to define a default
     * plane.
     * 
     * @param t
     *            The timepoint index.
     * @see #setDefaultZ(int)
     */
    public void setDefaultT(int t) {
        rndDef.setDefaultT(Integer.valueOf(t));
    }
    
    /**
     * Sets a map of overlays to be rendered.
     * @param overlays Overlay to color map.
     */
    public void setOverlays(Map<byte[], Integer> overlays) {
    	this.overlays = overlays;
    	checkOptimizations();
    }
    
    /**
     * Returns the current set of overlays to be rendered.
     * @return Overlay to color map.
     */
    public Map<byte[], Integer> getOverlays() {
    	return overlays;
    }

    /**
     * Updates the {@link QuantumManager} and configures it according to the
     * current quantum definition.
     */
    public void updateQuantumManager() {
        QuantumDef qd = rndDef.getQuantization();
        ChannelBinding[] cb = getChannelBindings();
        quantumManager.initStrategies(qd, cb);
    }

    /**
     * Renders the data selected by <code>pd</code> according to the current
     * rendering settings. The passed argument selects a plane orthogonal to one
     * of the <i>X</i>, <i>Y</i>, or <i>Z</i> axes. How many wavelengths are
     * rendered and what color model is used depends on the current rendering
     * settings.
     * 
     * @param pd
     *            Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *            or <i>Z</i> axes.
     * @return An <i>RGB</i> image ready to be displayed on screen.
     * @throws IOException
     *             If an error occurred while trying to pull out data from the
     *             pixels data repository.
     * @throws QuantizationException
     *             If an error occurred while quantizing the pixels raw data.
     * @throws NullPointerException
     *             If <code>pd</code> is <code>null</code>.
     */
    public RGBBuffer render(PlaneDef pd) throws IOException,
            QuantizationException {
        if (pd == null) {
            throw new NullPointerException("No plane definition.");
        }
        checkRegionDef(pd.getRegion());
        stats = new RenderingStats(this, pd);
        log.info("Using: '" + renderingStrategy.getClass().getName()
                + "' rendering strategy.");
        RGBBuffer img = renderingStrategy.render(this, pd);
        stats.stop();
        // TODO: Commenting this out for now. -- callan
        //log.info(stats.getStats());
        return img;
    }

    /**
     * Renders the data selected by <code>pd</code> according to the current
     * rendering settings. The passed argument selects a plane orthogonal to one
     * of the <i>X</i>, <i>Y</i>, or <i>Z</i> axes. How many wavelengths are
     * rendered and what color model is used depends on the current rendering
     * settings.
     * 
     * @param pd
     *            Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *            or <i>Z</i> axes.
     * @param newBuffer
     *             The pixel buffer to use in place of the one currently
     *             defined in the renderer. This will not change the state
     *             of the Renderer. If <code>null</code> is passed the existing
     *             pixel buffer will be used.
     * @return An <i>RGB</i> image ready to be displayed on screen.
     * @throws IOException
     *             If an error occurred while trying to pull out data from the
     *             pixels data repository.
     * @throws QuantizationException
     *             If an error occurred while quantizing the pixels raw data.
     * @throws NullPointerException
     *             If <code>pd</code> is <code>null</code>.
     */
    public int[] renderAsPackedInt(PlaneDef pd, PixelBuffer newBuffer)
        throws IOException, QuantizationException
    {
        if (pd == null) {
            throw new NullPointerException("No plane definition.");
        }
        checkRegionDef(pd.getRegion());
        stats = new RenderingStats(this, pd);
        log.info("Using: '" + renderingStrategy.getClass().getName()
                + "' rendering strategy.");
        PixelBuffer oldBuffer = buffer;
        try
        {
            if (newBuffer != null)
            {
                buffer = newBuffer;
            }
            RGBIntBuffer img = renderingStrategy.renderAsPackedInt(this, pd);
            stats.stop();
            // TODO: Commenting this out for now. -- callan
            //log.info(stats.getStats());
            return img.getDataBuffer();
        }
        finally
        {
            buffer = oldBuffer;
        }
    }

    /**
     * Returns the size, in bytes, of the {@link RGBBuffer} that would be
     * rendered from the plane selected by <code>pd</code>. Note that the
     * returned value also depends on the current rendering strategy which is
     * selected by the {@link #setModel(RenderingModel) setModel} method. So a
     * subsequent invocation of this method may return a different value if the
     * {@link #setModel(RenderingModel) setModel} method has been called since
     * the first call to this method.
     * 
     * @param pd
     *            Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *            or <i>Z</i> axes.
     * @return See above.
     * @throws NullPointerException
     *             If <code>pd</code> is <code>null</code>.
     */
    public int getImageSize(PlaneDef pd) {
        if (pd == null) {
            throw new NullPointerException("No plane definition.");
        }
        return renderingStrategy.getImageSize(pd, metadata);
    }

    /**
     * Returns a string with the dimensions of the specified plane. The returned
     * string has the format <code>AxB</code>, where <code>A</code> is the
     * number of pixels on the <i>X1</i>-axis and <code>B</code> the the
     * number of pixels on the the <i>X2</i>-axis. The <i>X1</i>-axis is the
     * <i>X</i>-axis in the case of an <i>XY</i> or <i>XZ</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>ZY</i> plane. The <i>X2</i>-axis
     * is the <i>Y</i>-axis in the case of an <i>XY</i> or <i>ZY</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>XZ</i> plane.
     * 
     * @param pd
     *            Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *            or <i>Z</i> axes.
     * @return See above.
     * @throws NullPointerException
     *             If <code>pd</code> is <code>null</code>.
     */
    public String getPlaneDimsAsString(PlaneDef pd) {
        if (pd == null) {
            throw new NullPointerException("No plane definition.");
        }
        return renderingStrategy.getPlaneDimsAsString(pd, metadata);
    }

    /**
     * Returns an array containing the channel bindings. The dimension of the
     * array equals the number of channels.
     * 
     * @return See above.
     */
    public ChannelBinding[] getChannelBindings() {
        List<ChannelBinding> bindings = rndDef.collectWaveRendering(null);
        return (ChannelBinding[]) bindings.toArray(new ChannelBinding[bindings
                .size()]);
    }

    /**
     * Returns the list of codomain map contexts. One per channel.
     * @return See above.
     */
    public List getCodomainMapContexts() {
        return null;//rndDef.collectSpatialDomainEnhancement(null);
    }

    /**
     * Returns a list containing the channel bindings. The dimension of the
     * array equals the number of channels.
     * 
     * @return See above.
     */
	public List<ChannelBinding> getChannelBindingsAsList() {
        return rndDef.collectWaveRendering(null);
    }

    /**
     * Returns the settings that define the transformation context. That is, a
     * specification of how raw data is to be transformed into an image that can
     * be displayed on screen.
     * 
     * @return See above.
     */
    public RenderingDef getRenderingDef() {
        return rndDef;
    }

    /**
     * Returns the object that manages and allows to retrieve the objects that
     * are used to quantize wavelength data.
     * 
     * @return See above.
     */
    public QuantumManager getQuantumManager() {
        return quantumManager;
    }

    /**
     * Returns the object that allows to access the pixels raw data.
     * 
     * @return See above.
     */
    public PixelBuffer getPixels() {
        return buffer;
    }

    /**
     * Returns the {@link Pixels} set the rendering engine is for.
     * 
     * @return See above.
     */
    public Pixels getMetadata() {
        return metadata;
    }

    /**
     * Returns the pixels type.
     * 
     * @return A pixels type enumeration object.
     */
    public PixelsType getPixelsType() {
        return metadata.getPixelsType();
    }

    /**
     * Returns the objects that defines the sequence of spatial transformations
     * to be applied to quantized data. One object per channel
     * 
     * @return See above.
     */
    List<CodomainChain> getCodomainChains() {
        return Collections.unmodifiableList(codomainChains);
    }

    /**
     * Returns the object that defines the sequence of spatial transformations
     * to be applied to quantized data.
     * 
     * @param channel
     * @return See above.
     */
    public CodomainChain getCodomainChain(int channel) {
        return codomainChains.get(channel);
    }

    /**
     * Returns a {@link RenderingStats} object that the rendering strategy can
     * use to track performance. A new stats object is created upon each
     * invocation of the {@link #render(PlaneDef) render} method.
     * 
     * @return The stats object.
     */
    public RenderingStats getStats() {
        return stats;
    }

   

    //
    // Methods pushed down from RenderingBean
    //

    /**
     * Sets the bit resolution.
     * 
     * @param bitResolution
     *            The value to set.
     */
    public void setQuantumStrategy(int bitResolution) {
        /*
         * RenderingDef rd = getRenderingDef();
         * 
         * QuantumDef qd = rd.getQuantization(), newQd; newQd = new
         * QuantumDef(); newQd.setBitResolution(Integer.valueOf(bitResolution));
         * newQd.setCdStart(qd.getCdStart()); newQd.setCdEnd(qd.getCdEnd());
         * rd.setQuantization(newQd); updateQuantumManager();
         */
        RenderingDef rd = getRenderingDef();
        QuantumDef qd = rd.getQuantization();
        qd.setBitResolution(Integer.valueOf(bitResolution));
        updateQuantumManager();
    }

    /**
     * Sets the codomain interval i.e. a sub-interval of [0, 255].
     * 
     * @param start
     *            The lower bound of the interval.
     * @param end
     *            The upper bound of the interval.
     */
    public void setCodomainInterval(int start, int end) {
        CodomainChain c;
        for (int i = 0; i < getPixels().getSizeC(); i++) {
            c = getCodomainChain(i);
            c.setInterval(start, end);
        }
        /*
         * RenderingDef rd = getRenderingDef(); QuantumDef qd =
         * rd.getQuantization(), newQd; newQd = new QuantumDef();
         * newQd.setBitResolution(qd.getBitResolution());
         * newQd.setCdStart(Integer.valueOf(start));
         * newQd.setCdEnd(Integer.valueOf(end)); rd.setQuantization(newQd);
         */
        RenderingDef rd = getRenderingDef();
        QuantumDef qd = rd.getQuantization();
        qd.setCdStart(Integer.valueOf(start));
        qd.setCdEnd(Integer.valueOf(end));
        //need to rebuild the look up table
        updateQuantumManager();
    }

    /**
     * Sets the pixels intensity interval for the specified channel.
     * 
     * @param w
     *            The channel index.
     * @param start
     *            The lower bound of the interval.
     * @param end
     *            The upper bound of the interval.
     */
    public void setChannelWindow(int w, double start, double end) {
        QuantumStrategy qs = getQuantumManager().getStrategyFor(w);
        qs.setWindow(start, end);
        ChannelBinding[] cb = getChannelBindings();
        cb[w].setInputStart(new Double(start));
        cb[w].setInputEnd(new Double(end));
    }

    /**
     * Sets the mapping strategy for the specified channel.
     * 
     * @param w
     *            The channel index.
     * @param family
     *            The mapping family.
     * @param coefficient
     *            The coefficient identifying a curve in the family.
     * @param noiseReduction
     *            Pass <code>true</code> to select the noiseReduction
     *            algorithm, <code>false</code> otherwise.
     */
    public void setQuantizationMap(int w, Family family, double coefficient,
            boolean noiseReduction) {
        QuantumStrategy qs = getQuantumManager().getStrategyFor(w);
        qs.setQuantizationMap(family, coefficient, noiseReduction);
        ChannelBinding[] cb = getChannelBindings();
        cb[w].setFamily(family);
        cb[w].setCoefficient(qs.getCurveCoefficient());
        cb[w].setNoiseReduction(noiseReduction);
    }

    /**
     * Sets the color associated to the specified channel.
     * 
     * @param w
     *            The channel index.
     * @param red
     *            The red component of the color.
     * @param green
     *            The green component of the color.
     * @param blue
     *            The blue component of the color.
     * @param alpha
     *            The alpha component of the color.
     */
    public void setRGBA(int w, int red, int green, int blue, int alpha) {
        ChannelBinding[] cb = getChannelBindings();
        cb[w].setRed(Integer.valueOf(red));
        cb[w].setGreen(Integer.valueOf(green));
        cb[w].setBlue(Integer.valueOf(blue));
        cb[w].setAlpha(Integer.valueOf(alpha));
        checkOptimizations();
    }

    /**
     * Sets the lookup table associated to the channel.
     *
     * @param w The selected channel.
     * @param lookupTable The lookup table.
     */
    public void setChannelLookupTable(int w, String lookupTable) {
        ChannelBinding[] cb = getChannelBindings();
        cb[w].setLookupTable(lookupTable);
        checkOptimizations();
    }

    /**
     * Makes a particular channel active or inactive.
     * @param w the wavelength index to toggle.
     * @param active <code>true</code> to set the channel active or 
     * <code>false</code> to set the channel inactive.
     */
    public void setActive(int w, boolean active) {
    	ChannelBinding[] cb = getChannelBindings();
    	cb[w].setActive(Boolean.valueOf(active));
    	checkOptimizations();
    }
    
    /**
     * Returns the optimizations that the renderer currently has enabled.
     * @return See above.
     */
    public Optimizations getOptimizations()
    {
    	return optimizations;
    }

	/**
     * Closes the buffer, cleaning up file state.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void close() {
		try
		{
			if (buffer != null)
				buffer.close();
		}
		catch (IOException e)
		{
            log.error("Buffer did not close successfully.", e);
			throw new ResourceError(
					e.getMessage() + " Please check server log.");
		}
    }

    /**
     * Returns an array  whose ascending indices represent the color
     * components Red, Green and Blue.
     * 
     * @param channel  the color to decompose into an array.
     * @return See above.
     */
    public static int[] getColorArray(ChannelBinding channel)
    {
    	int[] colors = new int[3];
    	colors[0] = channel.getRed();
    	colors[1] = channel.getGreen();
    	colors[2] = channel.getBlue();
    	return colors;
    }

    /**
     * Returns <code>true</code> if the pixels type is signed, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isPixelsTypeSigned()
    {
    	if (metadata == null) return false;
    	return PlaneFactory.isTypeSigned(metadata.getPixelsType());
    }
	
    /**
     * Returns the minimum value for that channels depending on the pixels
     * type and the original range (globalmax, globalmin)
     * 
     * @param w The channel index.
     * @return See above.
     */
	public double getPixelsTypeLowerBound(int w)
	{
		QuantumStrategy qs = getQuantumManager().getStrategyFor(w);
		return qs.getPixelsTypeMin();
	}

	/**
     * Returns the maximum value for that channels depending on the pixels
     * type and the original range (globalmax, globalmin)
     * 
     * @param w The channel index.
     * @return See above.
     */
	public double getPixelsTypeUpperBound(int w)
	{
		QuantumStrategy qs = getQuantumManager().getStrategyFor(w);
		return qs.getPixelsTypeMax();
	}

    /**
     * Sets the active resolution level.
     * @param resolutionLevel The resolution level to be used by the renderer.
     * @see ome.io.nio.PixelBuffer#setResolutionLevel(int)
     **/
    public void setResolutionLevel(int resolutionLevel)
    {
        buffer.setResolutionLevel(resolutionLevel);
    }

    /**
     * Retrieves the active resolution level.
     * @return The active resolution level.
     * @see ome.io.nio.PixelBuffer#getResolutionLevel()
     **/
    public int getResolutionLevel()
    {
        return buffer.getResolutionLevel();
    }

    /**
     * Retrieves the number of resolution levels that the backing
     * pixels pyramid contains.
     * @return The number of resolution levels. This value does not
     * necessarily indicate either the presence or absence of a
     * pixels pyramid.
     * @see ome.io.nio.PixelBuffer#getResolutionLevels()
     **/
    public int getResolutionLevels()
    {
        return buffer.getResolutionLevels();
    }

    /**
     * Returns the image's size information per resolution level.
     * 
     * @return See above.
     */
    public List<List<Integer>> getResolutionDescriptions()
    {
        return buffer.getResolutionDescriptions();
    }

    /**
     * Retrieves the tile size for the pixel store.
     * @return The dimension of the tile or <code>null</code> if the pixel
     * buffer is not tiled.
     **/
    public Dimension getTileSize()
    {
        return buffer.getTileSize();
    }
}
