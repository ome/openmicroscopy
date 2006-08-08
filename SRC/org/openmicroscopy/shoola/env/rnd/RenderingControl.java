/*
 * org.openmicroscopy.shoola.env.rnd.RenderingControl
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

package org.openmicroscopy.shoola.env.rnd;

//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import org.openmicroscopy.shoola.env.rnd.metadata.ChannelMetadata;

//Third-party libraries

//Application-internal dependencies
import ome.model.display.CodomainMapContext;
import ome.model.display.QuantumDef;
import omeis.providers.re.data.PlaneDef;

/** 
 * A facade to a given rendering environment for the UI benefit.
 * Allows to tune and read the rendering settings (set/get quantum, set/get 
 * channel, and so on).  Also allows to save the current settings.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface RenderingControl
{

    /** Flag to select a 1-bit depth (<i>=2^1-1</i>) output interval. */
    public static final int     DEPTH_1BIT = 1;

    /** Flag to select a 2-bit depth (<i>=2^2-1</i>) output interval. */
    public static final int     DEPTH_2BIT = 3;
    
    /** Flag to select a 3-bit depth (<i>=2^3-1</i>) output interval. */
    public static final int     DEPTH_3BIT = 7;
    
    /** Flag to select a 4-bit depth (<i>=2^4-1</i>) output interval. */
    public static final int     DEPTH_4BIT = 15;
    
    /** Flag to select a 5-bit depth (<i>=2^5-1</i>) output interval. */
    public static final int     DEPTH_5BIT = 31;
    
    /** Flag to select a 6-bit depth (<i>=2^6-1</i>) output interval. */
    public static final int     DEPTH_6BIT = 63;
    
    /** Flag to select a 7-bit depth (<i>=2^7-1</i>) output interval. */
    public static final int     DEPTH_7BIT = 127;
    
    /** Flag to select a 8-bit depth (<i>=2^8-1</i>) output interval. */
    public static final int     DEPTH_8BIT = 255;
    
    /** Identifies the <code>RGB</code> color model. */
    public static final String  RGB = "rgb";
    
    /** Identifies the <code>HSB</code> color model. */
    public static final String  HSB = "hsb";
    
    /** Identifies the <code>Grey scale</code> color model. */
    public static final String  GREY_SCALE = "greyscale";
    
    /** Identifies the <code>Linear</code> family. */
    public static final String  LINEAR = "linear";
    
    /** Identifies the <code>Polynomial</code> family. */
    public static final String  POLYNOMIAL = "polynomial";
    
    /** Identifies the <code>Exponential</code> family. */
    public static final String  EXPONENTIAL = "exponential";
    
    /** Identifies the <code>Exponential</code> family. */
    public static final String  LOGARITHMIC = "logarithmic";
    
    /**
     * Returns the number of pixels along the X-axis.
     * 
     * @return See above.
     */
    public int getPixelsDimensionsX();
    
    /**
     * Returns the number of pixels along the Y-axis.
     * 
     * @return See above.
     */
    public int getPixelsDimensionsY();
    
    /**
     * Returns the number of z-sections.
     * 
     * @return See above.
     */
    public int getPixelsDimensionsZ();
    
    /**
     * Returns the number of timepoints
     * 
     * @return See above.
     */
    public int getPixelsDimensionsT();
    
    /**
     * Returns the number of channels
     * 
     * @return See above.
     */
    public int getPixelsDimensionsC();
    
    /**
     * The size in microns of a pixel along the X-axis.
     * 
     * @return See above.
     */
    public float getPixelsSizeX();
    
    /**
     * The size in microns of a pixel along the Y-axis.
     * 
     * @return See above.
     */
    public float getPixelsSizeY();
    
    /**
     * The size in microns of a pixel along the Z-axis.
     * 
     * @return See above.
     */
    public float getPixelsSizeZ();
    
    //public PixelsStats getPixelsStats();
    
    
    //public double[] getChannelStats(int w);
    
    /**
     * Specifies the model that dictates how transformed raw data has to be 
     * mapped onto a color space.
     * 
     * @param model Identifies the color space model.
     */
    public void setModel(String model);
    
    /**
     * Returns the color space model that dictated how transformed raw data
     * has to be mapped onto a color space. 
     * 
     * @return See above.
     */
    public String getModel();
    
    /**
     * Returns the default focal plane. The default value is set to the
     * middle of the stack.
     * 
     * @return See above.
     */
    public int getDefaultZ();
    
    /**
     * Returns the default timepoint. The default value is set to the
     * first timepoint.
     * 
     * @return See above.
     */
    public int getDefaultT();
    
    /**
     * Sets the index of the default focal section.
     * This index is used to define a default plane.
     *  
     * @param z The stack index.
     */
    public void setDefaultZ(int z);
    
    /**
     * Sets the default timepoint index.
     * This index is used to define a default plane.
     * 
     * @param t The timepoint index.
     */
    public void setDefaultT(int t);
    
    /**
     * Sets the mapping strategy used during the mapping process.
     * 
     * @param bitResolution The depth, in bits, of the rendered image.
     */
    public void setQuantumStrategy(int bitResolution);
    
    /**
     * Sets the size of sub-interval of the device space.
     * The interval is a sub-interval of [0, 255].
     * 
     * @param start The lower bound of the interval.
     * @param end   The upper bound of the interval.
     */
    public void setCodomainInterval(int start, int end);
    
    /**
     * Returns the mapping context used during the mapping process. 
     * 
     * @return See above.
     */
    public QuantumDef getQuantumDef();
    
    /**
     * Sets the values used during the mapping of the specified wavelength.
     * 
     * @param w                 The index of the channel.
     * @param family            The mapping family.
     * @param coefficient       The coefficient identifying a curve in the
     *                          family.
     * @param noiseReduction    Pass <code>true</code> to select the 
     *                          mapping <code>NoiseReduction</code> algorithm,
     *                          <code>false</code> otherwise.
     */ 
    public void setQuantizationMap(int w, String family, double coefficient, 
                                    boolean noiseReduction);
    
    /**
     * Returns the family used to map the specified channel onto the device
     * space.
     * 
     * @param w The index of the channel.
     * @return See above.
     */
    public String getChannelFamily(int w);
    
    /**
     * Returns <code>true</code> is the <code>NoiseReduction</code> algorithm
     * is used when mapping the specified channel.
     * 
     * @param w The index of the channel.
     * @return See above.
     */
    public boolean getChannelNoiseReduction(int w);

    /**
     * Returns the coefficient that identifies a curve in the family of curves.
     *      
     * @param w The index of the channel.
     * @return  See above.
     * @see #getChannelFamily(int)
     */
    public double getChannelCurveCoefficient(int w);
    
    /**
     * Sets the size of the pixel intensity interval for the specified channel.
     * 
     * @param w     The index of the channel.
     * @param start The lower bound of the interval.
     * @param end   The upper bound of the interval.
     */
    public void setChannelWindow(int w, double start, double end);
    
    /**
     * Returns the lower bound of the pixel intensity interval.
     * 
     * @param w The index of the channel.
     * @return See above.
     * @see #setChannelWindow(int, double, double)
     */
    public double getChannelWindowStart(int w);
    
    /**
     * Returns the upper bound of the pixel intensity interval.
     * 
     * @param w The index of the channel.
     * @return See above.
     * @see #setChannelWindow(int, double, double)
     */
    public double getChannelWindowEnd(int w);
    
    /**
     * Sets the color on which the specified channel is mapped onto.
     * 
     * @param w     The index of the channel.
     * @param color The color selected.
     */
    public void setRGBA(int w, Color color);
    
    /**
     * Returns the color the channel is mapped onto.
     * 
     * @param w The index of the channel.
     * @return  See above.
     */
    public Color getRGBA(int w);
    
    /**
     * Sets the flag to map the channel, passed <code>true</code> to map the
     * channel, <code>false</code> otherwise.
     * 
     * @param w         The index of the channel.
     * @param active    <code>true</code> if the channel is mapped, 
     *                  <code>false</code> otherwise.
     */
    public void setActive(int w, boolean active);
    
    /**
     * Returns <code>true</code> if the channel is mapped, <code>false</code>
     * otherwise.
     * 
     * @param w The index of the channel.
     * @return  See above.
     */
    public boolean isActive(int w);
    
    /**
     * Adds the <code>CodomainMapContext</code> to the list of transformations.
     * Only one codomain map can be added to the transformations list.
     * When a new element is added to the list, the look-up table
     * managing the codomain transformations is rebuilt. 
     * 
     * @param mapCtx The context to add.
     */
    public void addCodomainMap(CodomainMapContext mapCtx);
    
    /**
     * Updates the specified <code>CodomainMapContext</code>.
     * The transformation associated should already be in the transformations.
     * 
     * @param mapCtx The context to update.
     */
    public void updateCodomainMap(CodomainMapContext mapCtx);
    
    /**
     * Removed the <code>CodomainMapContext</code> from the list of
     * transformations.
     * 
     * @param mapCtx    The context to remove.
     */
    public void removeCodomainMap(CodomainMapContext mapCtx);
    
    /**
     * Returns a read-only list of {@link CodomainMapContext}s using during
     * the mapping process in the device space.
     * 
     * @return See above.
     */
    public List getCodomainMaps();

    /**
     * Returns a list of string representing the mapping families supported by
     * the rendering engine.
     * 
     * @return See above.
     */
    public List getFamilies();
    
    
    /** Saves the current rendering settings to the database. */
    public void saveCurrentSettings();
    
    /** 
     * Resets the original default values. 
     * The default values aren't values previously saved.
     */
    public void resetDefaults();
   
    /**
     * Renders the specified {@link PlaneDef plane}.
     * 
     * @param pDef
     * @return The rendered image.
     */
    public BufferedImage render(PlaneDef pDef);
	
    public ChannelMetadata getChannelData(int w);
    
    public ChannelMetadata[] getChannelData();
    
    /** Shuts down the service. */
    public void shutDown();
    
}
