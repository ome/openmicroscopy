/*
 * omeis.providers.re.RenderingEngine
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import ome.api.StatefulServiceInterface;
import ome.conditions.ValidationException;
import ome.model.core.Pixels;
import ome.model.display.QuantumDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.model.meta.Event;
import ome.system.EventContext;
import ome.system.SelfConfigurableService;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;

/** 
 * Defines a service to render a given pixels set. 
 * <p>A pixels set is a <i>5D</i> array that stores the pixels data of an image,
 * that is the pixels intensity values.  Every instance of this service is
 * paired up to a pixels set.  Use this service to transform planes within the
 * pixels set onto an <i>RGB</i> image.</p>  
 * <p>The <code>RenderingEngine</code> allows to fine-tune the settings that 
 * define the transformation context &#151; that is, a specification of how raw
 * pixels data is to be transformed into an image that can be displayed on 
 * screen.  Those settings are referred to as rendering settings or display
 * options.  After tuning those settings it is possible to save them to the
 * metadata repository so that they can be used the next time the pixels set
 * is accessed for rendering; for example by another <code>RenderingEngine
 * </code> instance.  Note that the display options are specific to the given
 * pixels set and are experimenter scoped &#151; that is, two different users
 * can specify different display options for the <i>same</i> pixels set.  (A
 * <code>RenderingEngine</code> instance takes this into account automatically
 * as it is always bound to a given experimenter.)</p>
 * <p>This service is <b>thread-safe</b>.</p>
 * 
 *  
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/12 23:28:59 $)
 * </small>
 * @since OME2.2
 */
public interface RenderingEngine 
    extends SelfConfigurableService, StatefulServiceInterface
{

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
     * @throws ValidationException If <code>pd</code> is <code>null</code>.
     */
    public RGBBuffer render(PlaneDef pd) 
        throws ValidationException;
    
    /**
     * Loads the <code>Pixels</code> set this Rendering Engine is for.
     * 
     * @param pixelsId  The pixels set ID.
     */
	public void lookupPixels(long pixelsId);
    
    /**
     * Loads the rendering settings associated to the specified pixels set.
     * 
     * @param pixelsId  The pixels set ID.
     */
	public void lookupRenderingDef(long pixelsId);
    
    /** Creates a instance of the rendering engine. */
	public void load();
    
	/** Returns the current {@link EventContext} for this instance. This is
	 * useful for later identifying changes made by this {@link Event}.
	 */
	public EventContext getCurrentEventContext();
	
	/**
     * Specifies the model that dictates how transformed raw data has to be 
     * mapped onto a color space.
     * 
     * @param model Identifies the color space model.
	 */
	public void setModel(RenderingModel model);
    
    /**
     * Returns the model that dictates how transformed raw data has to be 
     * mapped onto a color space.
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
     * Sets the index of the default focal section.
     * This index is used to define a default plane.  
     *   
     * @param z The value to set.
     */
    public void setDefaultZ(int z);
    
    /**
     * Sets the default timepoint index.
     * This index is used to define a default plane.
     * 
     * @param t The value to set.
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
     * @param bitResolution The bit resolution defining associated to the 
     *                      strategy.
	 */
	public void setQuantumStrategy(int bitResolution);
    
    /**
     * Sets the sub-interval of the device space i.e. a discrete sub-interval
     * of [0, 255]
     * 
     * @param start The lower bound of the interval.
     * @param end   The upper bound of the interval.
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
     * @param w                 The channel index.
     * @param family            The mapping family. 
     * @param coefficient       The coefficient identifying a curve in the 
     *                          family.
     * @param noiseReduction    Pass <code>true</code> to turn the noise 
     *                          reduction algorithm on, <code>false</code>
     *                          otherwise.
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
     * @param w The channel index.
     * @return See above.
     * @see #getAvailableFamilies()
     */
    public Family getChannelFamily(int w);
    
    /**
     * Returns <code>true</code> if the noise reduction algortihm used to 
     * map the pixels intensity values is turned on, <code>false</code>
     * if the algorithm is turned off. Each channel
     * has an algorithm associated to it.
     * 
     * @param w The channel index.
     * @return See above.
     */
    public boolean getChannelNoiseReduction(int w);
    
    //TODO: not sure we need it
    public double[] getChannelStats(int w);
    
    /**
     * Returns the coefficient identifying a map in the family. Each channel
     * has a map associated to it.
     * 
     * @param w The channel index.
     * @return See above.
     * @see #getChannelFamily(int)
     */
    public double getChannelCurveCoefficient(int w);
    
    /**
     * Returns the pixels intensity interval. Each channel has a pixels
     * intensity interval associated to it.
     * 
     * @param w     The channel index.
     * @param start The lower bound of the interval.
     * @param end   The upper bound of the interval.
     */
	public void setChannelWindow(int w, double start, double end);
    
    /**
     * Returns the lower bound of the pixels intensity interval. Each channel 
     * has a pixels intensity interval associated to it.
     * 
     * @param w The channel index.
     * @return See above.
     */
	public double getChannelWindowStart(int w);
    
    /**
     * Returns the upper bound of the pixels intensity interval. Each channel 
     * has a pixels intensity interval associated to it.
     * 
     * @param w The channel index.
     * @return See above.
     */
	public double getChannelWindowEnd(int w);
    
    /**
     * Sets the four components composing the color associated to the specified
     * channel.
     * 
     * @param w     The channel index.
     * @param red   The red component. A value between 0 and 255.
     * @param green The green component. A value between 0 and 255.
     * @param blue  The blue component. A value between 0 and 255.
     * @param alpha The alpha component. A value between 0 and 255.
     */
	public void setRGBA(int w, int red, int green, int blue, int alpha);
    
    /**
     * Returns a 4D-array representing the color associated to the specified
     * channel. 
     * The first element corresponds to the red component 
     * (value between 0 and 255).
     * The second corresponds to the green component (value between 0 and 255).
     * The third corresponds to the blue component (value between 0 and 255).
     * The fourth corresponds to the alpha component (value between 0 and 255).
     * 
     * @param w The channel index.
     * @return See above
     */
	public int[] getRGBA(int w);
    
    /**
     * Maps the specified channel if <code>true</code>, unmaps the channel
     * otherwise.
     * 
     * @param w         The channel index.
     * @param active    Pass <code>true</code> to map the channel, 
     *                  <code>false</code> otherwise.
     */
	public void setActive(int w, boolean active);
    
    /**
     * Returns <code>true</code> if the channel is mapped, <code>false</code>
     * otherwise.
     * 
     * @param w The channel index.
     * @return See above.
     */
	public boolean isActive(int w);
	
	/**
     * Adds the context to the mapping chain. Only one context of the same
     * type can be added to the chain. The codomain transformations 
     * are functions from the device space to device space. Each time a new 
     * context is added, the second LUT is rebuilt.
     * 
     * @param mapCtx The context to add.
     * @see #updateCodomainMap(CodomainMapContext)
     * @see #removeCodomainMap(CodomainMapContext)
	 */
	public void addCodomainMap(CodomainMapContext mapCtx);
    
    /**
     * Upadtes the specified context. The codomain chain already contains
     * the specified context. Each time a new context is updated, the second 
     * LUT is rebuilt.
     * 
     * @param mapCtx The context to update.
     * @see #addCodomainMap(CodomainMapContext)
     * @see #removeCodomainMap(CodomainMapContext)
     */
	public void updateCodomainMap(CodomainMapContext mapCtx);
    
    /**
     * Removes the specified context from the chain. Each time a new context is 
     * removed, the second LUT is rebuilt.
     * 
     * @param mapCtx The context to remove.
     * @see #addCodomainMap(CodomainMapContext)
     * @see #updateCodomainMap(CodomainMapContext)
     */
	public void removeCodomainMap(CodomainMapContext mapCtx);
	
	/** Saves the current rendering settings in the database. */
	public void saveCurrentSettings();
	
	/** 
     * Resets the default settings i.e. the default values internal to the 
     * Rendering engine.
	 */
	public void resetDefaults();
	
}
