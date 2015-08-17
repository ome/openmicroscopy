/*
 * omeis.providers.re.RenderingEngine
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

// Java imports
import java.util.List;
import java.util.Map;

// Third-party libraries

// Application-internal dependencies
import ome.api.StatefulServiceInterface;
import ome.conditions.ValidationException;
import ome.model.core.Pixels;
import ome.model.display.QuantumDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;

/**
 * Defines a service to render a given pixels set.
 * <p>
 * A pixels set is a <i>5D</i> array that stores the pixels data of an image,
 * that is the pixels intensity values. Every instance of this service is paired
 * up to a pixels set. Use this service to transform planes within the pixels
 * set onto an <i>RGB</i> image.
 * </p>
 * <p>
 * The <code>RenderingEngine</code> allows to fine-tune the settings that
 * define the transformation context &#151; that is, a specification of how raw
 * pixels data is to be transformed into an image that can be displayed on
 * screen. Those settings are referred to as rendering settings or display
 * options. After tuning those settings it is possible to save them to the
 * metadata repository so that they can be used the next time the pixels set is
 * accessed for rendering; for example by another <code>RenderingEngine
 * </code>
 * instance. Note that the display options are specific to the given pixels set
 * and are experimenter scoped &#151; that is, two different users can specify
 * different display options for the <i>same</i> pixels set. (A
 * <code>RenderingEngine</code> instance takes this into account automatically
 * as it is always bound to a given experimenter.)
 * </p>
 * <p>
 * This service is <b>thread-safe</b>.
 * </p>
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public interface RenderingEngine extends StatefulServiceInterface {

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
     * @throws ValidationException
     *             If <code>pd</code> is <code>null</code>.
     */
    public RGBBuffer render(PlaneDef pd);

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
     * @throws ValidationException
     *             If <code>pd</code> is <code>null</code>.
     * @see #render(PlaneDef)
     */
    public int[] renderAsPackedInt(PlaneDef pd);

    /**
     * Renders the data selected by <code>pd</code> according to the current
     * rendering settings and compresses the resulting RGBA composite image.
     * 
     * @param pd
     *            Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *            or <i>Z</i> axes.
     * @return A compressed RGBA JPEG for display.
     * @throws ValidationException
     *             If <code>pd</code> is <code>null</code>.
     * @see #render(PlaneDef)
     * @see #renderAsPackedInt(PlaneDef)
     */
    public byte[] renderCompressed(PlaneDef pd);
    
    
    /**
     * Performs a projection through selected optical sections of a particular 
     * timepoint with the currently active channels and renders the data for
     * display.
     * @param algorithm {@link ome.api.IProjection#MAXIMUM_INTENSITY},
     * {@link ome.api.IProjection#MEAN_INTENSITY} or
     * {@link ome.api.IProjection#SUM_INTENSITY}.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @return A packed-integer <i>RGBA</i> rendered image of the projected
     * pixels.
     * @throws ValidationException Where:
     * <ul>
     *   <li><code>algorithm</code> is unknown</li>
     *   <li><code>timepoint</code> is out of range</li>
     *   <li><code>start</code> is out of range</li>
     *   <li><code>end</code> is out of range</li>
     *   <li><code>start > end</code></li>
     * </ul>
     * @see ome.api.IProjection#projectPixels(long, PixelsType, int, int, int, List, int, int, int, String)
     */
    public int[] renderProjectedAsPackedInt(int algorithm, int timepoint,
                                            int stepping, int start, int end);
    
    /**
     * Performs a projection through selected optical sections of a particular 
     * timepoint with the currently active channels, renders the data for
     * display and compresses the resulting RGBA composite image.
     * @param algorithm {@link ome.api.IProjection#MAXIMUM_INTENSITY},
     * {@link ome.api.IProjection#MEAN_INTENSITY} or
     * {@link ome.api.IProjection#SUM_INTENSITY}.
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @return A compressed <i>RGBA</i> rendered JPEG image of the projected
     * pixels.
     * @throws ValidationException Where:
     * <ul>
     *   <li><code>algorithm</code> is unknown</li>
     *   <li><code>timepoint</code> is out of range</li>
     *   <li><code>start</code> is out of range</li>
     *   <li><code>end</code> is out of range</li>
     *   <li><code>start > end</code></li>
     * </ul>
     * @see ome.api.IProjection#projectPixels(long, PixelsType, int, int, int, List, int, int, int, String)
     */
    public byte[] renderProjectedCompressed(int algorithm, int timepoint,
                                            int stepping, int start, int end);

    /**
     * Loads the <code>Pixels</code> set this Rendering Engine is for.
     * 
     * @param pixelsId
     *            The pixels set ID.
     */
    public void lookupPixels(long pixelsId);

    /**
     * Returns the id of the {@link ome.model.display.RenderingDef} loaded by
     * either {@link #lookupRenderingDef(long)} or
     * {@link #loadRenderingDef(long)}.
     */
    public long getRenderingDefId();

    /**
     * Loads the rendering settings associated to the specified pixels set.
     * 
     * @param pixelsId
     *            The pixels set ID.
     * @return <code>true</code> if a <code>RenderingDef</code> exists for the
     * <code>Pixels</code> set, otherwise <code>false</code>.
     */
    public boolean lookupRenderingDef(long pixelsId);
    
    /**
     * Loads a specific set of rendering settings that does not necessarily
     * have to be linked to the given Pixels set. However, the rendering
     * settings <b>must</b> be linked to a compatible Pixels set as defined
     * by {@link ome.api.IRenderingSettings#sanityCheckPixels(Pixels, Pixels)}.
     * @param renderingDefId The rendering definition ID.
     * @throws ValidationException If a <code>RenderingDef</code> does not
     * exist with the ID <code>renderingDefId</code> or if the
     * <code>RenderingDef</code> is incompatible due to differing pixels sets.
     */
    public void loadRenderingDef(long renderingDefId);
    
    /**
     * Informs the rendering engine that it should render a set of overlays on
     * each rendered frame. These are expected to be binary masks.
     * @param overlays Binary mask to color map.
     */
    public void setOverlays(Map<byte[], Integer> overlays);

    /** Creates a instance of the rendering engine. */
    public void load();

    /**
     * Specifies the model that dictates how transformed raw data has to be
     * mapped onto a color space.
     * 
     * @param model
     *            Identifies the color space model.
     */
    public void setModel(RenderingModel model);

    /**
     * Returns the model that dictates how transformed raw data has to be mapped
     * onto a color space.
     * 
     * @return See above.
     */
    public RenderingModel getModel();

    /**
     * Returns the index of the default focal section.
     * 
     * @return See above.
     */
    public int getDefaultZ();

    /**
     * Returns the default timepoint index.
     * 
     * @return See above.
     */
    public int getDefaultT();

    /**
     * Sets the index of the default focal section. This index is used to define
     * a default plane.
     * 
     * @param z
     *            The value to set.
     */
    public void setDefaultZ(int z);

    /**
     * Sets the default timepoint index. This index is used to define a default
     * plane.
     * 
     * @param t
     *            The value to set.
     */
    public void setDefaultT(int t);

    /**
     * Returns the {@link Pixels} set the Rendering engine is for.
     * 
     * @return See above.
     */
    public Pixels getPixels();

    /**
     * Returns the list of color models supported by the Rendering engine.
     * 
     * @return See above.
     */
    public List getAvailableModels();

    /**
     * Returns the list of mapping families supported by the Rendering engine.
     * 
     * @return See above.
     */
    public List getAvailableFamilies();

    /**
     * Sets the quantization strategy. The strategy is common to all channels.
     * 
     * @param bitResolution
     *            The bit resolution defining associated to the strategy.
     */
    public void setQuantumStrategy(int bitResolution);

    /**
     * Sets the sub-interval of the device space i.e. a discrete sub-interval of
     * [0, 255]
     * 
     * @param start
     *            The lower bound of the interval.
     * @param end
     *            The upper bound of the interval.
     */
    public void setCodomainInterval(int start, int end);

    /**
     * Returns the quantization object.
     * 
     * @return See above.
     */
    public QuantumDef getQuantumDef();

    /**
     * Sets the quantization map, one per channel.
     * 
     * @param w
     *            The channel index.
     * @param family
     *            The mapping family.
     * @param coefficient
     *            The coefficient identifying a curve in the family.
     * @param noiseReduction
     *            Pass <code>true</code> to turn the noise reduction algorithm
     *            on, <code>false</code> otherwise.
     * @see #getAvailableFamilies()
     * @see #getChannelCurveCoefficient(int)
     * @see #getChannelFamily(int)
     * @see #getChannelNoiseReduction(int)
     */
    public void setQuantizationMap(int w, Family family, double coefficient,
            boolean noiseReduction);

    /**
     * Returns the family associated to the specified channel.
     * 
     * @param w
     *            The channel index.
     * @return See above.
     * @see #getAvailableFamilies()
     */
    public Family getChannelFamily(int w);

    /**
     * Returns <code>true</code> if the noise reduction algorithm used to map
     * the pixels intensity values is turned on, <code>false</code> if the
     * algorithm is turned off. Each channel has an algorithm associated to it.
     * 
     * @param w
     *            The channel index.
     * @return See above.
     */
    public boolean getChannelNoiseReduction(int w);

    // TODO: not sure we need it
    public double[] getChannelStats(int w);

    /**
     * Returns the coefficient identifying a map in the family. Each channel has
     * a map associated to it.
     * 
     * @param w
     *            The channel index.
     * @return See above.
     * @see #getChannelFamily(int)
     */
    public double getChannelCurveCoefficient(int w);

    /**
     * Returns the pixels intensity interval. Each channel has a pixels
     * intensity interval associated to it.
     * 
     * @param w
     *            The channel index.
     * @param start
     *            The lower bound of the interval.
     * @param end
     *            The upper bound of the interval.
     */
    public void setChannelWindow(int w, double start, double end);

    /**
     * Returns the lower bound of the pixels intensity interval. Each channel
     * has a pixels intensity interval associated to it.
     * 
     * @param w
     *            The channel index.
     * @return See above.
     */
    public double getChannelWindowStart(int w);

    /**
     * Returns the upper bound of the pixels intensity interval. Each channel
     * has a pixels intensity interval associated to it.
     * 
     * @param w
     *            The channel index.
     * @return See above.
     */
    public double getChannelWindowEnd(int w);

    /**
     * Sets the four components composing the color associated to the specified
     * channel.
     * 
     * @param w
     *            The channel index.
     * @param red
     *            The red component. A value between 0 and 255.
     * @param green
     *            The green component. A value between 0 and 255.
     * @param blue
     *            The blue component. A value between 0 and 255.
     * @param alpha
     *            The alpha component. A value between 0 and 255.
     */
    public void setRGBA(int w, int red, int green, int blue, int alpha);

    /**
     * Returns a 4D-array representing the color associated to the specified
     * channel. The first element corresponds to the red component (value
     * between 0 and 255). The second corresponds to the green component (value
     * between 0 and 255). The third corresponds to the blue component (value
     * between 0 and 255). The fourth corresponds to the alpha component (value
     * between 0 and 255).
     * 
     * @param w
     *            The channel index.
     * @return See above
     */
    public int[] getRGBA(int w);

    /**
     * Maps the specified channel if <code>true</code>, unmaps the channel
     * otherwise.
     * 
     * @param w
     *            The channel index.
     * @param active
     *            Pass <code>true</code> to map the channel,
     *            <code>false</code> otherwise.
     */
    public void setActive(int w, boolean active);

    /**
     * Returns <code>true</code> if the channel is mapped, <code>false</code>
     * otherwise.
     * 
     * @param w
     *            The channel index.
     * @return See above.
     */
    public boolean isActive(int w);

    /**
     * Adds the context to the mapping chain. Only one context of the same type
     * can be added to the chain. The codomain transformations are functions
     * from the device space to device space. Each time a new context is added,
     * the second LUT is rebuilt.
     * 
     * @param mapCtx
     *            The context to add.
     * @see #updateCodomainMap(CodomainMapContext)
     * @see #removeCodomainMap(CodomainMapContext)
     */
    public void addCodomainMap(CodomainMapContext mapCtx);

    /**
     * Updates the specified context. The codomain chain already contains the
     * specified context. Each time a new context is updated, the second LUT is
     * rebuilt.
     * 
     * @param mapCtx
     *            The context to update.
     * @see #addCodomainMap(CodomainMapContext)
     * @see #removeCodomainMap(CodomainMapContext)
     */
    public void updateCodomainMap(CodomainMapContext mapCtx);

    /**
     * Removes the specified context from the chain. Each time a new context is
     * removed, the second LUT is rebuilt.
     * 
     * @param mapCtx
     *            The context to remove.
     * @see #addCodomainMap(CodomainMapContext)
     * @see #updateCodomainMap(CodomainMapContext)
     */
    public void removeCodomainMap(CodomainMapContext mapCtx);

    /** Saves the current rendering settings in the database. */
    public void saveCurrentSettings();

    /**
     * Saves the current rendering settings in the database
     * as a new {@link ome.model.display.RenderingDef} and loads the object
     * into the current {@link RenderingEngine}.
     */
    public long saveAsNewSettings();

    /**
     * Resets the default settings i.e. the default values internal to the
     * Rendering engine. The settings will be saved.
     *
     * @param save Pass <code>true</code> to save the settings,
     *             <code>false</code> otherwise.
     */
    public long resetDefaultSettings(boolean save);

	/**
	 * Sets the current compression level for the service. (The default is 85%)
	 * 
	 * @param percentage A percentage compression level from 1.00 (100%) to 
	 * 0.01 (1%).
	 * @throws ValidationException if the <code>percentage</code> is out of
	 * range.
	 */
	public void setCompressionLevel(float percentage);
	
	/**
	 * Returns the current compression level for the service.
	 * 
	 * @return See above.
	 */
	public float getCompressionLevel();
	
	/**
     * Returns <code>true</code> if the pixels type is signed, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
	public boolean isPixelsTypeSigned();
	
	/**
     * Returns the minimum value for that channels depending on the pixels
     * type and the original range (globalmax, globalmin)
     * 
     * @param w The channel index.
     * @return See above.
     */
	public double getPixelsTypeLowerBound(int w);

	/**
     * Returns the maximum value for that channels depending on the pixels
     * type and the original range (globalmax, globalmin)
     * 
     * @param w The channel index.
     * @return See above.
     */
	public double getPixelsTypeUpperBound(int w);

    public boolean requiresPixelsPyramid();

    public Object getResolutionDescriptions();

    public int getResolutionLevels();

    public int getResolutionLevel();

    public void setResolutionLevel(int resolutionLevel);

    public int[] getTileSize();

    public void setChannelLookupTable(int w, String lookup);

    public String getChannelLookupTable(int w);

}

