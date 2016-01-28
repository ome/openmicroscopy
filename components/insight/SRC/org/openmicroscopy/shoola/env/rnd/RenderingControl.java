/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import omero.model.Length;
import omero.romio.PlaneDef;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.RenderingServiceException;

import org.openmicroscopy.shoola.env.rnd.data.ResolutionLevel;

import omero.gateway.model.ChannelData;
import omero.gateway.model.PixelsData;


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
 * @since OME2.2
 */
public interface RenderingControl
{
	
	/** The default name. */
	public static final String	DEFAULT_NAME = "default";
	
	/** The maximum size before retrieving the plane asynchronously. */
	public static final int		MAX_SIZE = 1024;
	
	/** The maximum size before retrieving the plane asynchronously. */
	public static final int		MAX_SIZE_THREE = 3*MAX_SIZE;
	
	/** The maximum number of channels. */
	public static final int		MAX_CHANNELS = 100;
	
	/** Flag to indicate that the image is not compressed. */
	public static final int		UNCOMPRESSED = 0;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * medium Level of compression. 
	 */
	public static final int		MEDIUM = 1;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * low Level of compression. 
	 */
	public static final int		LOW = 2;
	
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
     * The size of a pixel along the X-axis.
     * 
     * @return See above.
     */
    public Length getPixelsPhysicalSizeX();
    
    /**
     * The size of a pixel along the Y-axis.
     * 
     * @return See above.
     */
    public Length getPixelsPhysicalSizeY();
    
    /**
     * The size of a pixel along the Z-axis.
     * 
     * @return See above.
     */
    public Length getPixelsPhysicalSizeZ();

    /**
     * Specifies the model that dictates how transformed raw data has to be 
     * mapped onto a color space.
     * 
     * @param model Identifies the color space model.
     * @throws RenderingServiceException	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void setModel(String model)
    	throws RenderingServiceException, DSOutOfServiceException;
    
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
     * Returns the default time-point. The default value is set to the
     * first time-point.
     * 
     * @return See above.
     */
    public int getDefaultT();
    
    /**
     * Sets the index of the default focal section.
     * This index is used to define a default plane.
     *  
     * @param z The stack index.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void setDefaultZ(int z)
    	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Sets the default time-point index.
     * This index is used to define a default plane.
     * 
     * @param t The time-point index.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken. 
     */
    public void setDefaultT(int t)
    	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Sets the mapping strategy used during the mapping process.
     * 
     * @param bitResolution The depth, in bits, of the rendered image.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value. 
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void setQuantumStrategy(int bitResolution)
    	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Sets the size of sub-interval of the device space.
     * The interval is a sub-interval of [0, 255].
     * 
     * @param start The lower bound of the interval.
     * @param end   The upper bound of the interval.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void setCodomainInterval(int start, int end)
    	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Returns the lower bound of the codomain.
     * 
     * @return See above.
     */
    public int getCodomainStart();
    
    /**
     * Returns the upper bound of the codomain.
     * 
     * @return See above.
     */
    public int getCodomainEnd();
    
    /**
     * Returns the bit resolution value.
     * 
     * @return See above.
     */
    public int getBitResolution();
    
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
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.       
     * @throws DSOutOfServiceException  	If the connection is broken.                   
     */ 
    public void setQuantizationMap(int w, String family, double coefficient, 
                                    boolean noiseReduction)
    	throws RenderingServiceException, DSOutOfServiceException;
    
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
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void setChannelWindow(int w, double start, double end)
    	throws RenderingServiceException, DSOutOfServiceException;
    
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
     * @param color The color to set.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value. 
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void setRGBA(int w, Color color)
    	throws RenderingServiceException, DSOutOfServiceException;
    
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
     * @param active    Pass <code>true</code> to map the channel, 
     *                  <code>false</code> otherwise.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.  
     * @throws DSOutOfServiceException  	If the connection is broken.               
     */
    public void setActive(int w, boolean active)
    	throws RenderingServiceException, DSOutOfServiceException;
    
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
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    //public void addCodomainMap(CodomainMapContext mapCtx)
    //	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Updates the specified <code>CodomainMapContext</code>.
     * The transformation associated should already be in the transformations.
     * 
     * @param mapCtx The context to update.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    //public void updateCodomainMap(CodomainMapContext mapCtx)
    //	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Removed the <code>CodomainMapContext</code> from the list of
     * transformations.
     * 
     * @param mapCtx    The context to remove.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
   // public void removeCodomainMap(CodomainMapContext mapCtx)
    //	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Returns a read-only list of <code>CodomainMapContext</code>s using during
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
    
    /** 
     * Saves the current rendering settings to the database.
     * Returns the copy of the saved rendering data.
     *  
     * @return See above.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public RndProxyDef saveCurrentSettings()
    	throws RenderingServiceException, DSOutOfServiceException;
    
    /** 
     * Resets the original default values. 
     * The default values aren't values previously saved.
     * 
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void resetDefaults()
    	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Returns the <code>ChannelMetadata</code> object specified
     * by the passed index.
     * 
     * @param w The channel index.
     * @return See above.
     */
    public ChannelData getChannelData(int w);
    
    /**
     * Returns an array of <code>ChannelMetadata</code> objects.
     * 
     * @return See above.
     */
    public ChannelData[] getChannelData();
    
    /**
     * Returns <code>true</code> if one of the active channel is mapped
     * to <code>RED</code>,  <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean hasActiveChannelRed();
    
    /**
     * Returns <code>true</code> if one of the active channel is mapped
     * to <code>GREEN</code>, <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean hasActiveChannelGreen();
    
    /**
     * Returns <code>true</code> if one of the active channel is mapped
     * to <code>BLUE</code>, <code>false</code> otherwise.
     * 
     * @return See above
     */
    public boolean hasActiveChannelBlue();
    
    /**
     * Returns <code>true</code> if the channel is mapped
     * to <code>RED</code>, <code>false</code> otherwise.
     * 
     * @param index The index of the channel.
     * @return See above.
     */
    public boolean isChannelRed(int index);
    
    /**
     * Returns <code>true</code> if the channel is mapped
     * to <code>GREEN</code>, <code>false</code> otherwise.
     * 
     * @param index The index of the channel.
     * @return See above.
     */
    public boolean isChannelGreen(int index);
    
    /**
     * Returns <code>true</code> if the channel is mapped
     * to <code>BLUE</code>, <code>false</code> otherwise.
     * 
     * @param index The index of the channel.
     * @return See above.
     */
    public boolean isChannelBlue(int index);
    
    /**
     * Returns a copy of the current rendering settings.
     * 
     * @return See above.
     */
    public RndProxyDef getRndSettingsCopy();

    /**
     * Resets the rendering settings.
     * (Does not reset Z and T settings)
     * @param settings The settings to set.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void resetSettings(RndProxyDef settings)
    	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Resets the rendering settings.
     * 
     * @param rndDef
     *            The settings to set.
     * @param includeZT Pass <code>true</code> to also reset Z and T setting,
     *         <code>false</code> to ignore Z and T
     * @throws RenderingServiceException
     *             If an error occurred while setting the value.
     * @throws DSOutOfServiceException
     *             If the connection is broken.
     */
    public void resetSettings(RndProxyDef rndDef, boolean includeZT)
            throws RenderingServiceException, DSOutOfServiceException;
    
    /**
     * Returns <code>true</code> if the pixels type is signed, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
	public boolean isPixelsTypeSigned();
	
	/**
     * Returns the minimum value for that channels depending on the pixels
     * type and the original range.
     * 
     * @param w The channel's index.
     * @return See above.
     */
	public double getPixelsTypeLowerBound(int w);

	/**
     * Returns the maximum value for that channels depending on the pixels
     * type and the original range.
     * 
     * @param w The channel's index.
     * @return See above.
     */
	public double getPixelsTypeUpperBound(int w);
	
	/**
	 * Controls if the passed set of pixels is compatible
	 * to the set the rendering engine is for. 
	 * This method should be invoked before copying the rendering settings.
	 * 
	 * @param pixels The pixels to controls.
	 * @return See above.
	 */
	public boolean validatePixels(PixelsData pixels);
	
	/**
	 * Sets the compression level.
	 * 
	 * @param compression One of constants defined by this class.
	 */
	public void setCompression(int compression);
	
	/**
	 * Returns <code>true</code> if the compression is turned on,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isCompressed();
	
	/**
	 * Renders the specified {@link PlaneDef 2D-plane}.
	 * 
	 * @param pDef   Information about the plane to render.
	 * @return See above.
	 * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public BufferedImage render(PlaneDef pDef)
		throws RenderingServiceException, DSOutOfServiceException;

	/**
	 * Renders the specified {@link PlaneDef 2D-plane}.
	 * 
	 * @param pDef 	 Information about the plane to render.
	 * @param compression The compression level.
	 * @return See above.
	 * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public BufferedImage render(PlaneDef pDef, int compression)
		throws RenderingServiceException, DSOutOfServiceException;

	/**
	 * Returns one of the compression level defined by this class.
	 * 
	 * @return See above.
	 */
	public int getCompressionLevel();

	/** 
     * Sets the original rendering settings. 
     * 
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void setOriginalRndSettings()
    	throws RenderingServiceException, DSOutOfServiceException;
	
    /**
     * Projects the selected optical sections for the currently selected 
     * time-point and the active channels and returned a projected image.
     * 
     * @param startZ   The first optical section.
     * @param endZ     The last optical section.
     * @param stepping Stepping value to use while calculating the projection.
     * @param type 	   One of the projection type defined by this class.
     * @param channels The collection of channels to project.
     * @return See above.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public BufferedImage renderProjected(int startZ, int endZ, int stepping, 
    									int type, List<Integer> channels)
    	throws RenderingServiceException, DSOutOfServiceException;

    /**
     * Copies rendering settings from the original image to the projected
     * one.
     * 
     * @param rndToCopy The rendering settings to copy.
     * @param indexes   Collection of channel's indexes. 
     * 					Mustn't be <code>null</code>.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
     */
    public void copyRenderingSettings(RndProxyDef rndToCopy, 
    									List<Integer> indexes)
    	throws RenderingServiceException, DSOutOfServiceException;
    
    /**
	 * Returns a list of the active channels.
	 * 
	 * @return See above.
	 */
    public List<Integer> getActiveChannels();
    
    /**
     * Returns <code>true</code> if the passed rendering settings are the same
     * than the current one <code>false</code> otherwise.
     * 
     * @param def            The original settings. Mustn't be <code>null</code>.
     * @param checkPlane Pass <code>true</code> to take into account 
     *                                       the z-section and time-point, <code>false</code> 
     *                                       otherwise.
     * 
     * @return See above.
     */
    public boolean isSameSettings(RndProxyDef def, boolean checkPlane);
    
    /**
	 * Returns <code>true</code> if the passed rendering settings are the same
	 * than the current one <code>false</code> otherwise.
	 * 
	 * @param def 		 The original settings. Mustn't be <code>null</code>.
	 * @param checkPlane Pass <code>true</code> to take into account 
	 * 					 the z-section and time-point, <code>false</code> 
	 * 					 otherwise.
	 * @param checkInactiveChannels <code>true</code>: all channels will be checked for changes; just
	 *                                     active channels otherwise
	 * 
	 * @return See above.
	 */
    public boolean isSameSettings(RndProxyDef def, boolean checkPlane, boolean checkInactiveChannels);

    /**
     * Returns the id of the pixels set.
     * 
     * @return See above.
     */
    public long getPixelsID();
    
    /**
     * Returns <code>true</code> if the passed channels compose an RGB image, 
     * <code>false</code> otherwise.
     * 
     * @param channels The channels to handle
     * @return See above.
     */
    public boolean isMappedImageRGB(List channels);
    
    /**
     * Sets the overlays.
     * 
     * @param tableID  The id of the table.
     * @param overlays The overlays to set, or <code>null</code> to turn 
     * the overlays off.
     */
    public void setOverlays(long tableID, Map<Long, Integer> overlays)
    	throws RenderingServiceException, DSOutOfServiceException;
	
	/**
	 * Returns the possible resolution levels. This method should only be used
	 * when dealing with large images.
	 * 
	 * @return See above.
	 */
	public int getResolutionLevels();
	
	/**
	 * Returns the currently selected resolution level. This method should only 
	 * be used when dealing with large images.
	 * 
	 * @return See above.
	 */
	public int getSelectedResolutionLevel();
	
	/**
	 * Sets resolution level. This method should only be used when dealing with
	 * large images.
	 * 
	 * @param level The value to set.
	 * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public void setSelectedResolutionLevel(int level)
		throws RenderingServiceException, DSOutOfServiceException;
	
	/**
	 * Returns the dimension of a tile.
	 * 
	 * @return See above.
	 * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public Dimension getTileSize()
		throws RenderingServiceException, DSOutOfServiceException;
	
	/**
	 * Returns <code>true</code> if it is a big image, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 * @throws RenderingServiceException
	 * @throws DSOutOfServiceException
	 */
	public boolean isBigImage();
	
	/**
	 * Returns the collection of rendering controls linked to the master.
	 * 
	 * @return See above.
	 */
	public List<RenderingControl> getSlaves();
	
	/**
	 * Returns <code>true</code> if no longer active, <code>false</code>
	 * otherwise. This is used when the reference is still kept but the user
	 * no longer interacts with the rendering proxy.
	 * 
	 * @return See above.
	 */
	boolean isShutDown();
	
	/**
	 * Returns the list of the levels.
	 * 
	 * @return See above.
	 */
	List<ResolutionLevel> getResolutionDescriptions()
		throws RenderingServiceException, DSOutOfServiceException;
}
