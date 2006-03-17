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
import java.io.IOException;

//Third-party libraries

//Application-internal dependencies
import ome.model.display.QuantumDef;
import ome.system.SelfConfigurableService;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;

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
    extends SelfConfigurableService
{

    /**
     * Renders the data selected by <code>pd</code> according to the current
     * rendering settings.
     * The passed argument selects a plane orthogonal to one of the <i>X</i>, 
     * <i>Y</i>, or <i>Z</i> axes. How many wavelengths are rendered and
     * what color model is used depends on the current rendering settings.
     * 
     * @param pd Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *           or <i>Z</i> axes.
     * @return An <i>RGB</i> image ready to be displayed on screen.
     * @throws IOException If an error occured while trying to pull out
     *                     data from the pixels data repository.
     * @throws QuantizationException If an error occurred while quantizing the
     *                               pixels raw data.
     * @throws NullPointerException If <code>pd</code> is <code>null</code>.
     * @throws IllegalStateException If the {@link Renderer renderer} is
     *                              <code>null</code>.
     */
    public RGBBuffer render(PlaneDef pd)
        throws IOException, QuantizationException;
    
    
    //TODO: javadoc the rest!
    
	//State management.
	public void lookupPixels(long pixelsId);
	public void lookupRenderingDef(long pixelsId);
	public void load();
    
    //FIXME: Hacks!
    public int getSizeX();
    public int getSizeY();
	
    /**
     * Sets the rendering model either <code>GreyScale</code>, 
     * <code>RGB</code> or <code>HSB</code>.
     * 
     * @param model The model to set.
     */
	public void setModel(int model);
    
    /**
     * Returns the model to render an <code>2D</code>-plane.
     * 
     * @return See above.
     */
	public int getModel();
    
    /**
     * Returns the default z-section in the stack. By default this value is set
     * to the middle of the stack of the first time-point.
     * 
     * @return See above.
     */
	public int getDefaultZ();
    
    /** 
     * Returns the default time-point. By default this value is set to the first
     * time-point.
     * 
     * @return See above.
     */
	public int getDefaultT();
	
    /**
     * Sets the default z-section.
     * 
     * @param z The z-section to set.
     */
    public void setDefaultZ(int z); //Is it the best way to do it?
    
    /**
     * Sets the default time-point.
     * 
     * @param t The value to set.
     */
    public void setDefaultT(int t); //Is it the best way to do it?
    
    /**
     * Sets the bit resolution. This method triggers a re-build of the first
     * look-up table.  
     * 
     * @param bitResolution The value to set.
     */
	public void setBitResolution(int bitResolution);
    
    /**
     * Sets the codomain interval i.e. a discrete sub-interval of 
     * <code>[0, 255]</code>. This method triggers a re-build of the second
     * look-up table.
     * 
     * @param start The lower bound of the codomain interval.
     * @param end   The upper bound of the codomain interval.
     */
	public void setCodomainInterval(int start, int end);
    
    /**
     * Returns the mapping context used during the quantization process.
     * 
     * @return See above.
     */
	public QuantumDef getQuantumDef();
	
    /**
     * Sets the parameters required to map the specified channel to the 
     * device space. This method triggers a re-build of the first look-up table. 
     * 
     * @param w                 The channel index.
     * @param family            Knows how to map the data.
     * @param coefficient       Identifies a curve in the family.
     * @param noiseReduction    Passed <code>true</code> to use the
     *                          <i>noise reduction</i> mapping algorithm, 
     *                          <code>false</code> otherwise.                       
     */
    public void setQuantizationMap(int w, int family, double coefficient, 
                                    boolean noiseReduction);
    
    /**
     * Returns the family used to map the specified channel to the device space.
     * 
     * @param w The channel index.
     * @return See above.
     */
    public int getChannelFamily(int w);
    
    /** 
     * Returns <code>true</code> if the <i>noise reduction</i> mapping algorithm
     * is used to map the specified channel to the device space.
     * 
     * @param w The channel index.
     * @return See above.
     */
    public boolean getChannelNoiseReduction(int w);
    
    /**
     * Returns a curve in the currently selected family for the specified
     * channel.
     * 
     * @param w The channel index.
     * @return See above.
     */
    public double getChannelCurveCoefficient(int w);
    
    /**
     * Sets the bounds of the pixel intensity interval for the specified 
     * channel. This method triggers a re-build of the first look-up table. 
     * 
     * @param w     The channel index.
     * @param start The lower bound of the pixel intensity interval.
     * @param end   The upper bound of the pixel intensity interval.
     */
	public void setChannelWindow(int w, double start, double end);
    
    /**
     * Returns the lower bound of the pixel intensity interval for the specified
     * channel. 
     * 
     * @param w The channel index.
     * @return See above.
     */
	public double getChannelWindowStart(int w);
    
    /**
     * Returns the upper bound of the pixel intensity interval for the specified
     * channel. This method triggers a re-build of the second look-up table. 
     * 
     * @param w The channel index.
     * @return See above.
     */
	public double getChannelWindowEnd(int w);
    
    /**
     * Sets the color in the <code>RGBA</code> color model, associated to the
     * specified channel.
     * 
     * @param w     The channel index.
     * @param red   The red component of the color.
     * @param green The green component of the color.
     * @param blue  The blue component of the color.
     * @param alpha The alpha component of the color.
     */
	public void setRGBA(int w, int red, int green, int blue, int alpha);
    
    /**
     * Returns an <code>RGBBA</code> array representing the color in the
     * <code>RGBA</code> color model, associated to the specified channel. 
     * The first element of the array stores the <code>red</code> component.
     * The second element of the array stores the <code>green</code> component.
     * The third element of the array stores the <code>blue</code> component.
     * The fourth element of the array stores the <code>alpha</code> component.
     * 
     * @param w The channel index.
     * @return See above.
     */
	public int[] getRGBA(int w);
    
    /**
     * Passes <code>true</code> to map the specified channel to the device
     * space, <code>false</code> otherwise.
     * 
     * @param w         The channel index.
     * @param active    Passed <code>true</code> to map the channel,
     *                  <code>false</code> otherwise.             
     */
	public void setActive(int w, boolean active);
    
    /**
     * Returns <code>true</code> if the channel is active i.e. mapped to the
     * device space, <code>false</code> otherwise.
     * 
     * @param w The channel index.
     * @return See above.
     */
	public boolean isActive(int w);
	
    /**
     * Adds a map context to the chain.
     * This means that the transformation associated to the passed context
     * will be applied after all the currently queued transformations.
     * An exception will be thrown if the chain already contains an object of
     * the same class as <code>mapCtx</code>. This is because we don't want
     * to compose the same transformation twice.
     * This method triggers a re-build of the second look-up table. 
     * 
     * @param mapCtx The context to add. Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the context is already defined.
     */
	public void addCodomainMap(CodomainMapContext mapCtx);
    
    /**
     * Updates the specified a map context in the chain.
     * This means that the context has previously been added to the chain.
     * This method triggers a re-build of the second look-up table. 
     * 
     * @param mapCtx    The context to add. Mustn't be <code>null</code> and
     *                  already contained in the chain.
     * @throws IllegalArgumentException If the specifed context doesn't exist. 
     */
	public void updateCodomainMap(CodomainMapContext mapCtx);
    
    /**
     * Removes a map context from the chain.
     * This method removes the object (if any) in the chain that is an instance
     * of the same class as <code>mapCtx</code>. This means that the
     * transformation associated to the passed context won't be applied.
     * This method triggers a re-build of the second look-up table.
     * 
     * @param mapCtx The context to remove.
     */
	public void removeCodomainMap(CodomainMapContext mapCtx);
	
    
    public double[] getChannelStats(int w); //Do we need that one?
    
	//Save display options to db.
	public void saveCurrentSettings();
	
	//ResetDefaults values.
	public void resetDefaults();
	
}
