/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.RendererModel 
 *
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
package org.openmicroscopy.shoola.agents.metadata.rnd;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries
import org.apache.commons.collections.CollectionUtils;

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlShutDown;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.rnd.data.ResolutionLevel;
import org.openmicroscopy.shoola.util.file.modulo.ModuloInfo;
import org.openmicroscopy.shoola.util.file.modulo.ModuloParser;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ChannelData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.XMLAnnotationData;

/** 
 * The Model component in the <code>Renderer</code> MVC triad.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class RendererModel
{

	/** The maximum width of the preview image. */
	static final int PREVIEW_WIDTH = 256;

	/** The maximum height of the preview image. */
	static final int PREVIEW_HEIGHT = 256;

	/** Identifies the minimum value of the device space. */
	static final int CD_START = 0;

	/** Identifies the maximum value of the device space. */
	static final int CD_END = 255;

	/** Flag to select a 1-bit depth (<i>=2^1-1</i>) output interval. */
	static final int DEPTH_1BIT = RenderingControl.DEPTH_1BIT;

	/** Flag to select a 2-bit depth (<i>=2^2-1</i>) output interval. */
	static final int DEPTH_2BIT = RenderingControl.DEPTH_2BIT;

	/** Flag to select a 3-bit depth (<i>=2^3-1</i>) output interval. */
	static final int DEPTH_3BIT = RenderingControl.DEPTH_3BIT;

	/** Flag to select a 4-bit depth (<i>=2^4-1</i>) output interval. */
	static final int DEPTH_4BIT = RenderingControl.DEPTH_4BIT;

	/** Flag to select a 5-bit depth (<i>=2^5-1</i>) output interval. */
	static final int DEPTH_5BIT = RenderingControl.DEPTH_5BIT;

	/** Flag to select a 6-bit depth (<i>=2^6-1</i>) output interval. */
	static final int DEPTH_6BIT = RenderingControl.DEPTH_6BIT;

	/** Flag to select a 7-bit depth (<i>=2^7-1</i>) output interval. */
	static final int DEPTH_7BIT = RenderingControl.DEPTH_7BIT;

	/** Flag to select a 8-bit depth (<i>=2^8-1</i>) output interval. */
	static final int DEPTH_8BIT = RenderingControl.DEPTH_8BIT;

	/** Identifies the <code>Linear</code> family. */
	static final String LINEAR = RenderingControl.LINEAR;

	/** Identifies the <code>Exponential</code> family. */
	static final String LOGARITHMIC = RenderingControl.LOGARITHMIC;

	/** Identifies the <code>Exponential</code> family. */
	static final String EXPONENTIAL = RenderingControl.EXPONENTIAL;

	/** Identifies the <code>Exponential</code> family. */
	static final String POLYNOMIAL = RenderingControl.POLYNOMIAL;

	/** Reference to the component that embeds this model. */
	private Renderer component;

	/** Reference to the rendering control. */
	private RenderingControl rndControl;

	/** The current state of the component. */
	private int state;

	/** The index of the selected channel. */
	private int selectedChannelIndex;

	/** Flag to denote if the widget is visible or not. */
	private boolean visible;

    /** The index of the rendering. */
    private int rndIndex;

    /** The collection of sorted channels. */
    private List<ChannelData> sortedChannel;

    /**
     * The global minimum of all channels if the number of channels is
     * greater than {@code Renderer#MAX_CHANNELS}.
     */
    private Double globalMinChannels;

    /**
     * The global maximum of all channels if the number of channels is
     * greater than {@code Renderer#MAX_CHANNELS}.
     */
    private Double globalMaxChannels;

    /** The plane object to render. */
    private PlaneDef plane;

    /** The dimension of the preview image. */
    private Dimension previewSize;

    /** The rendering settings. */
    private RndProxyDef rndDef;

    /** Keeps track of the changes to the rendering settings */
    private RenderingDefinitionHistory history = new RenderingDefinitionHistory();
    
    /** Reference to the image. */
    private ImageData image;

    /** The security context.*/
    private SecurityContext ctx;

    /** Map hosting the extra dimension if available.*/
    private Map<Integer, ModuloInfo> modulo;

	/**
	 * Creates a new instance.
	 *
	 * @param ctx The security context.
	 * @param rndControl Reference to the component that controls the
	 *                   rendering settings. Mustn't be <code>null</code>.
	 * @param rndIndex The index associated to the renderer.
	 */
	RendererModel(SecurityContext ctx, RenderingControl rndControl,
			int rndIndex)
	{
		if (rndControl == null)
			throw new NullPointerException("No rendering control.");
		setRenderingControl(rndControl);
		this.ctx = ctx;
		this.rndIndex = rndIndex;
		visible = false;
		globalMaxChannels = null;
		globalMinChannels = null;
		plane = new PlaneDef();
		plane.slice = omero.romio.XY.value;
	}

	/**
	 * Returns the security context.
	 *
	 * @return See above.
	 */
	SecurityContext getSecurityContext() { return ctx; }

	/**
	 * Sets the image the component is for.
	 * 
	 * @param image The value to set.
	 */
	void setImage(ImageData image) { this.image = image; }

	/**
	 * Sets the rendering control.
	 * 
	 * @param rndControl Reference to the component that controls the
	 *                   rendering settings. Mustn't be <code>null</code>.
	 */
	void setRenderingControl(RenderingControl rndControl)
	{
		this.rndControl = rndControl;
		if (rndControl != null) {
		    rndDef = rndControl.getRndSettingsCopy();
		    history.reset();
		}
	}

	/**
	 * Returns the image the renderer is for.
	 *
	 * @return See above.
	 */
	ImageData getRefImage() { return image; }

	/**
	 * Returns <code>true</code> if one channel is selected,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasSelectedChannel() { return selectedChannelIndex >= 0; }

	/**
	 * Returns the index of a channel or <code>-1</code>.
	 *
	 * @return See above.
	 */
	int createSelectedChannel()
	{
		//Set the selected channel
		List<Integer> active = getActiveChannels();
		List<ChannelData> list = getChannelData();
		Iterator<ChannelData> i = list.iterator();
		ChannelData channel;
		int index;
		int setIndex = -1;
		while (i.hasNext()) {
			channel = i.next();
			index = channel.getIndex();
			if (active.contains(index) && setIndex < 0) {
				setIndex = index;
				break;
			}
		}
		return setIndex;
	}

	/**
	 * Returns the color associated to the channel.
	 *
	 * @param index The index of the channel.
	 * @return See above.
	 */
	Color getChannelColor(int index)
	{
		if (rndControl == null) return Color.white;
		return rndControl.getRGBA(index);
	}

	/**
	 * Returns the status of the window.
	 *
	 * @return See above.
	 */
	boolean isVisible() { return visible; }

	/**
	 * Called by the <code>Renderer</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 *
	 * @param component The embedding component.
	 */
	void initialize(Renderer component) { this.component = component; }

	/** Discards component. */
	void discard() 
	{
		if (rndControl == null) return;
		RenderingControlShutDown loader =
			new RenderingControlShutDown(ctx, rndControl.getPixelsID());
		loader.load();
		rndControl = null;
	}

	/**
	 * Returns the state of the component.
	 *
	 * @return See above.
	 */
	int getState() { return state; }

	/**
	 * Sets the pixels intensity interval for the specified channel.
	 * or for all channels if the number of channels is greater than 
	 * {@link Renderer#MAX_CHANNELS}
	 *
	 * @param index The index of the channel.
	 * @param start The lower bound of the interval.
	 * @param end The upper bound of the interval.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setInputInterval(int index, double start, double end)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		if (isLifetimeImage() && getModuloT() == null) {
		    for (int i = 0; i < getMaxC(); i++) {
                rndControl.setChannelWindow(i, start, end);
            }
		} else rndControl.setChannelWindow(index, start, end);
	}

	/**
	 * Returns the upper bound of the sub-interval of the device space.
	 *
	 * @return See above.
	 */
	int getCodomainEnd()
	{
		if (rndControl == null) return -1;
		return rndControl.getCodomainEnd();
	}

	/**
	 * Returns the lower bound of the sub-interval of the device space.
	 *
	 * @return See above.
	 */
	int getCodomainStart()
	{
		if (rndControl == null) return -1;
		return rndControl.getCodomainStart();
	}

	/**
	 * Sets the sub-interval of the device space.
	 * 
	 * @param s The lower bound of the interval.
	 * @param e The upper bound of the interval.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setCodomainInterval(int s, int e)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setCodomainInterval(s, e);
	}

	/**
	 * Sets the quantum strategy.
	 * 
	 * @param v The bit resolution defining the strategy.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setBitResolution(int v)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setQuantumStrategy(v);
	}

	/**
	 * Sets the selected channel.
	 * 
	 * @param index The index of the selected channel.
	 */
	void setSelectedChannel(int index) { selectedChannelIndex = index; }

	/**
	 * Sets the family used during the mapping process for the specified channel.
	 * 
	 * @param channel The selected channel.
	 * @param family The family to set.
	 * @throws RenderingServiceException If an error occurred while setting
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setFamily(int channel, String family)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		if (channel < 0 || channel > getMaxC()) channel = selectedChannelIndex;
		boolean b = rndControl.getChannelNoiseReduction(selectedChannelIndex);
		double k = rndControl.getChannelCurveCoefficient(selectedChannelIndex);
		rndControl.setQuantizationMap(channel, family, k, b);
	}

	/**
	 * Selects one curve in the family.
	 * 
	 * @param k The coefficient identifying a curve within a family.
	 * @throws RenderingServiceException If an error occurred while setting
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setCurveCoefficient(double k)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		boolean b = rndControl.getChannelNoiseReduction(selectedChannelIndex);
		String family = rndControl.getChannelFamily(selectedChannelIndex);
		rndControl.setQuantizationMap(selectedChannelIndex, family, k, b);
	}

	/**
	 * Turns on and off the noise reduction algorithm mapping.
	 * 
	 * @param b Pass <code>true</code>  to turn it on,
	 *          <code>false</code> otherwise.
	 * @throws RenderingServiceException If an error occurred while setting
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setNoiseReduction(boolean b)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		String family = rndControl.getChannelFamily(selectedChannelIndex);
		double k = rndControl.getChannelCurveCoefficient(selectedChannelIndex);
		rndControl.setQuantizationMap(selectedChannelIndex, family, k, b);
	}

	/**
	 * Updates the specified {@link CodomainMapContext context}.
	 * 
	 * @param ctx The context to update.
	 * @throws RenderingServiceException 	If an error occurred while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	/*
	void updateCodomainMap(CodomainMapContext ctx)
	throws RenderingServiceException, DSOutOfServiceException
	{
		rndControl.updateCodomainMap(ctx);
	}
	*/

	/**
	 * Returns the codomain map context corresponding to the specified 
	 * <code>codomain</code> class. Returns <code>null</code> if there is no
	 * context matching the class.
	 * 
	 * @param mapType The class corresponding to the context to retrieve.
	 * @return See above.
	 */
	/*
	CodomainMapContext getCodomainMap(Class mapType)
	{
		List maps = getCodomainMaps();
		Iterator i = maps.iterator();
		CodomainMapContext ctx;
		while (i.hasNext()) {
			ctx = (CodomainMapContext) i.next();
			if (ctx.getClass().equals(mapType)) return ctx;
		}
		return null;
	}
	*/

	/**
	 * Returns a read-only list of {@link CodomainMapContext}s using during
	 * the mapping process in the device space.
	 *
	 * @return See above.
	 */
	List getCodomainMaps()
	{ 
		if (rndControl == null) return new ArrayList();
		return rndControl.getCodomainMaps();
	}

	/**
	 * Removes the codomain map identified by the class from the chain of 
	 * codomain transformations.
	 * 
	 * @param mapType The type to identify the codomain map.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	/*
	void removeCodomainMap(Class mapType)
	throws RenderingServiceException, DSOutOfServiceException
	{
		CodomainMapContext ctx = getCodomainMap(mapType);
		if (ctx != null) rndControl.removeCodomainMap(ctx);
	}
	*/

	/**
	 * Adds the codomain map identified by the class to the chain of 
	 * codomain transformations.
	 *
	 * @param mapType The type to identify the codomain map.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	/*
	void addCodomainMap(Class mapType)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (mapType.equals(ReverseIntensityContext.class)) {
			ReverseIntensityContext riCtx = new ReverseIntensityContext();
			riCtx.setReverse(Boolean.TRUE);
			rndControl.addCodomainMap(riCtx);
		} else if (mapType.equals(PlaneSlicingContext.class)) {

		} else if (mapType.equals(ContrastStretchingContext.class)) {

		}
	}
	*/

	/** 
	 * Returns the index of the currently selected channel.
	 * 
	 * @return See above.
	 */
	int getSelectedChannel() { return selectedChannelIndex; }

	/**
	 * Returns a list of available mapping families.
	 * 
	 * @return See above.
	 */
	List getFamilies()
	{ 
		if (rndControl == null) return new ArrayList();
		return rndControl.getFamilies();
	}

	/**
	 * Returns the mapping family used for to map the selected channel.
	 *
	 * @return See above.
	 */
	String getFamily() { return getFamily(selectedChannelIndex); }

	/**
	 * Returns the mapping family used for to map the selected channel.
	 *
	 * @param channel The selected channel.
	 * @return See above.
	 */
	String getFamily(int channel)
	{
	    if (rndControl == null) return "";
	    return rndControl.getChannelFamily(channel);
	}

	/**
	 * Returns the map selected in the family for the selected channel.
	 *
	 * @return See above.
	 */
	double getCurveCoefficient()
	{
		return getCurveCoefficient(selectedChannelIndex);
	}

	/**
	 * Returns the map selected in the family for the selected channel.
	 *
	 * @param channel The selected channel.
	 * @return See above.
	 */
	double getCurveCoefficient(int channel)
	{
	    if (rndControl == null) return -1;
	    return rndControl.getChannelCurveCoefficient(channel);
	}
    
	/**
	 * Returns the bit resolution value.
	 * 
	 * @return See above.
	 */
	int getBitResolution()
	{
		if (rndControl == null) return -1;
		return rndControl.getBitResolution();
	}

	/**
	 * Returns <code>true</code> if the noise reduction flag is turned on
	 * for the selected channel, <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	boolean isNoiseReduction()
	{
		if (rndControl == null) return false;
		return rndControl.getChannelNoiseReduction(selectedChannelIndex);
	}

	/**
	 * Returns a list of <code>Channel Data</code> objects.
	 *
	 * @return See above.
	 */
	List<ChannelData> getChannelData()
	{
		if (rndControl == null) return new ArrayList<ChannelData>();
		if (sortedChannel == null) {
			ChannelData[] data = rndControl.getChannelData();
			ViewerSorter sorter = new ViewerSorter();
			sortedChannel = Collections.unmodifiableList(sorter.sort(data));
		}
		return sortedChannel;
	}

	/**
	 * Returns the global minimum of the currently selected channel
	 * or of all channels if the number of channels is greater
	 * {@link Renderer#MAX_CHANNELS}.
	 *
	 * @return See above.
	 */
	double getGlobalMin()
	{
		if (getMaxC() > Renderer.MAX_CHANNELS) {
			if (globalMinChannels == null) {
				double min = Double.MAX_VALUE;
				double value;
				for (int i = 0; i < getMaxC(); i++) {
					value = getGlobalMin(i); 
					if (value < min) min = value;
				}
				globalMinChannels = min;
			}
			return globalMinChannels.doubleValue();
		}
		return getGlobalMin(selectedChannelIndex);
	}

	/**
	 * Returns the global maximum of the currently selected channel
	 * or of all channels if the number of channels is greater
	 * {@link Renderer#MAX_CHANNELS}.
	 *
	 * @return See above.
	 */
	double getGlobalMax()
	{
		if (getMaxC() > Renderer.MAX_CHANNELS) {
			if (globalMaxChannels == null) {
				double max = Double.MIN_VALUE;
				double value;
				for (int i = 0; i < getMaxC(); i++) {
					value = getGlobalMax(i);
					if (value > max) max = value;
				}
				globalMaxChannels = max;
			}
			return globalMaxChannels.doubleValue();
		}
		return getGlobalMax(selectedChannelIndex);
	}

	/**
	 * Returns the global maximum of the passed channel.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	double getGlobalMax(int index)
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelData(index).getGlobalMax();
	}

	/**
	 * Returns the global minimum of the passed channel.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	double getGlobalMin(int index)
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelData(index).getGlobalMin();
	}

	/**
	 * Returns the lowest possible value.
	 *
	 * @return See above.
	 */
	double getLowestValue()
	{
		return getLowestValue(selectedChannelIndex);
	}

	/**
	 * Returns the lowest possible value for the passed channel.
	 *
	 * @param channel The channel to handle.
	 * @return See above.
	 */
	double getLowestValue(int channel)
	{
		if (rndControl == null) return -1;
		return rndControl.getPixelsTypeLowerBound(channel);
	}

	/**
	 * Returns the highest possible value.
	 * 
	 * @return See above.
	 */
	double getHighestValue()
	{
		return getHighestValue(selectedChannelIndex);
	}

	/**
	 * Returns the highest possible value.
	 * 
	 * @param channel The channel to handle.
	 * @return See above.
	 */
	double getHighestValue(int channel)
	{
		if (rndControl == null) return -1;
		return rndControl.getPixelsTypeUpperBound(channel);
	}

	/**
	 * Returns the lower bound of the pixels intensity interval of the 
	 * specified channel.
	 * 
	 * @param channel The index of the channel.
	 * @return See above.
	 */
	double getWindowStart(int channel)
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelWindowStart(channel);
	}

	/**
	 * Returns the upper bound of the pixels intensity interval of the 
	 * specified channel.
	 * 
	 * @param channel The index of the channel.
	 * @return See above.
	 */
	double getWindowEnd(int channel)
	{
		if (rndControl == null) return -1;
		return rndControl.getChannelWindowEnd(channel);
	}

	/**
	 * Returns the lower bound of the pixels intensity interval of the 
	 * currently selected channel.
	 * 
	 * @return See above.
	 */
	double getWindowStart()
	{
		return getWindowStart(selectedChannelIndex);
	}

	/**
	 * Returns the upper bound of the pixels intensity interval of the 
	 * currently selected channel.
	 * 
	 * @return See above.
	 */
	double getWindowEnd()
	{
		return getWindowEnd(selectedChannelIndex);
	}

	/**
	 * Returns <code>true</code> if the grey scale is selected,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isGreyScale()
	{
		if (rndControl == null) return false;
		return rndControl.getModel().equals(RenderingControl.GREY_SCALE);
	}

	/**
	 * Saves the rendering settings.
	 *
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void saveRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndDef = rndControl.saveCurrentSettings();
	}

	/**
	 * Returns <code>true</code> if the channel is mapped, <code>false</code>
	 * otherwise.
	 * 
	 * @param w The channel's index.
	 * @return See above.
	 */
	boolean isChannelActive(int w)
	{
		if (rndControl == null) return false;
		return rndControl.isActive(w);
	}

	/**
         * Returns the reference to the history
         */
	RenderingDefinitionHistory getRndDefHistory() {
	    return history;
	}
	
	/**
	 * Returns a list of active channels.
	 * 
	 * @return See above.
	 */
	List<Integer> getActiveChannels()
	{
		List<Integer> active = new ArrayList<Integer>();
		if (rndControl == null) return active;
		for (int i = 0; i < getMaxC(); i++) {
			if (rndControl.isActive(i)) active.add(Integer.valueOf(i));
		}
		return active;
	}

	/**
	 * Returns the number of channels.
	 *
	 * @return See above.
	 */
	int getMaxC()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsC();
	}

	/**
	 * Returns the index associated to the renderer.
	 * 
	 * @return See above.
	 */
	int getRndIndex() { return rndIndex; }

	/**
	 * Returns <code>true</code> if the renderer is for a general perspective
	 * or for a specific view.
	 * 
	 * @return See above.
	 */
	boolean isGeneralIndex()
	{ 
		return getRndIndex() == MetadataViewer.RND_GENERAL;
	}

	/**
	 * Sets the color for the specified channel.
	 *
	 * @param index The channel's index.
	 * @param color The color to set.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setChannelColor(int index, Color color)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setRGBA(index, color);
	}

	/**
	 * Returns the color model.
	 * 
	 * @return See above.
	 */
	String getColorModel()
	{
		if (rndControl == null) return null;
		return rndControl.getModel();
	}

	/**
	 * Sets the color model.
	 * 
	 * @param colorModel The color model to set.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setColorModel(String colorModel)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		makeHistorySnapshot();
		rndControl.setModel(colorModel);
	}

	/**
	 * Returns the number of pixels along the X-axis.
	 *
	 * @return See above.
	 */
	int getMaxX()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsX();
	}
	
	/**
	 * Returns the number of pixels along the Y-axis.
	 *
	 * @return See above.
	 */
	int getMaxY()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsY();
	}

	/**
	 * Returns the maximum number of z-sections.
	 *
	 * @return See above.
	 */
	int getMaxZ()
	{
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsZ();
	}

	/**
	 * Returns the maximum number of time-points.
	 * 
	 * @return See above.
	 */
	int getMaxT()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsDimensionsT();
	}

	/**
	 * Returns the number of time points if modulo available.
	 *
	 * @return See above.
	 */
	int getRealT()
	{
	    if (hasModuloT()) {
	        int sizeBin = modulo.get(ModuloInfo.T).getSize();
	        return getMaxT()/sizeBin;
	    }
	    return getMaxT();
	}

	/**
	 * Returns the currently selected z-section.
	 *
	 * @return See above.
	 */
	int getDefaultZ()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getDefaultZ();
	}

	/**
	 * Returns the currently selected time-point.
	 *
	 * @return See above.
	 */
	int getDefaultT()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getDefaultT();
	}

	/**
	 * Returns the currently selected time-point.
	 * 
	 * @return See above.
	 */
    int getRealSelectedT()
    { 
        if (rndControl == null) return -1;
        if (hasModuloT()) {
            return rndControl.getDefaultT() / getMaxLifetimeBin();
        }
        return rndControl.getDefaultT();
    }

	/**
	 * Sets the selected plane.
	 *
	 * @param z The z-section to set.
	 * @param t The time-point to set.
	 * @throws RenderingServiceException If an error occurred while setting
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setSelectedXYPlane(int z, int t)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		if (t >= 0 && t != getDefaultT()) rndControl.setDefaultT(t);
		if (z >= 0 && z != getDefaultZ()) rndControl.setDefaultZ(z);
	}

	/**
	 * Sets the selected z-section.
	 *
	 * @param z The z-section to set.
	 * @throws RenderingServiceException If an error occurred while setting
	 *                                  the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
    void setSelectedZ(int z)
        throws RenderingServiceException, DSOutOfServiceException
    {
        if (rndControl == null) return;
        if (z >= 0 && z != getDefaultZ()) rndControl.setDefaultZ(z);
    }

	/**
	 * Turns on or off the specified channel.
	 * 
	 * @param index The index of the channel.
	 * @param active Pass <code>true</code> to turn the channel on,
	 * <code>false</code> to turn in off.
	 * @throws RenderingServiceException If an error occurred while setting
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setChannelActive(int index, boolean active)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setActive(index, active);
	}

	/**
	 * Returns the compression level.
	 *
	 * @return See above.
	 */
	int getCompressionLevel()
	{
		if (rndControl == null) return -1;
		return rndControl.getCompressionLevel();
	}

	/**
	 * Returns the physical size of a pixels along the Y-axis.
	 *
	 * @return See above.
	 */
	double getPixelsSizeY()
	{ 
		if (rndControl == null) return -1;
		return rndControl.getPixelsPhysicalSizeY();
	}

	/**
	 * Returns the physical size of a pixels along the X-axis.
	 *
	 * @return See above.
	 */
	double getPixelsSizeX()
	{
		if (rndControl == null) return -1;
		return rndControl.getPixelsPhysicalSizeX();
	}
	
	/**
	 * Returns the physical size of a pixels along the Z-axis.
	 *
	 * @return See above.
	 */
	double getPixelsSizeZ()
	{
		if (rndControl == null) return -1;
		return rndControl.getPixelsPhysicalSizeZ();
	}

    /**
     * Returns a copy of the current rendering settings.
     * 
     * @return See above.
     */
	RndProxyDef getRndSettingsCopy()
	{ 
		if (rndControl == null) return null;
		return rndControl.getRndSettingsCopy();
	}

    /**
     * Returns the initial rendering settings.
     * 
     * @return See above.
     */
	RndProxyDef getInitialRndSettings() { return rndDef; }

	/**
	 * Returns <code>true</code> if an active channel
	 * is mapped to <code>Red</code> if the band is <code>0</code>,
	 * <code>Red</code> if the band is <code>0</code>,
	 * <code>Red</code> if the band is <code>0</code>,
	 * <code>false</code> otherwise.
	 *
	 * @param band Pass <code>0</code> for <code>Red</code>,
	 * 			   <code>1</code> for <code>Green</code>,
	 * 			   <code>2</code> for <code>Blue</code>.
	 * @return See above
	 */
	boolean hasActiveChannel(int band)
	{
		if (rndControl == null) return false;
		switch (band) {
			case 0: return rndControl.hasActiveChannelRed();
			case 1: return rndControl.hasActiveChannelGreen();
			case 2: return rndControl.hasActiveChannelBlue();
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the compression is turned on,
	 * <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	boolean isCompressed()
	{
		if (rndControl == null) return false;
		return rndControl.isCompressed();
	}

	/**
	 * Returns <code>true</code> if the specified channel 
	 * is mapped to <code>Red</code> if the band is <code>0</code>,
	 * <code>Red</code> if the band is <code>0</code>, 
	 * <code>Red</code> if the band is <code>0</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @param band Pass <code>0</code> for <code>Red</code>,
	 * 			   <code>1</code> for <code>Green</code>,
	 * 			   <code>2</code> for <code>Blue</code>.
	 * @param index The index of the channel.
	 * @return See above
	 */
	boolean isColorComponent(int band, int index)
	{
		if (rndControl == null) return false;
		switch (band) {
			case 0: return rndControl.isChannelRed(index);
			case 1: return rndControl.isChannelGreen(index);
			case 2: return rndControl.isChannelBlue(index);
		}
		return false;
	}

	/**
	 * Resets the default settings.
	 * 
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void resetDefaults()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.resetDefaults();
	}

	/**
	 * Resets the passed rendering settings.
	 * 
	 * @param settings The rendering settings to reset.
	 * @throws RenderingServiceException 	If an error occurred while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void resetSettings(RndProxyDef settings)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.resetSettings(settings);
	}

	/**
	 * Saves the current settings.
	 *
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	RndProxyDef saveCurrentSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return null;
		RndProxyDef def = rndControl.saveCurrentSettings();
		rndDef = def;
		return def;
	}
	
	/**
	 * Undoes the last change to the rendering settings
	 * @throws RenderingServiceException
	 * @throws DSOutOfServiceException
	 */
	void historyBack() throws RenderingServiceException, DSOutOfServiceException {
	        if (rndControl == null)
	            return;
	        
                RndProxyDef def;
                boolean canRedo = history.canRedo();
                boolean isSame = rndControl.isSameSettings(history.getCurrent(), true, true);
                if (!canRedo && !isSame)
                    def = history.backward(rndControl.getRndSettingsCopy());
                else
                    def = history.backward();
                
                resetSettings(def);
	}
	
	/**
	 * Redoes the previous change to the rendering settings
	 * @throws RenderingServiceException
	 * @throws DSOutOfServiceException
	 */
	void historyForward() throws RenderingServiceException, DSOutOfServiceException {
	        RndProxyDef def = history.forward();
                resetSettings(def);
        }

	/**
	 * Stores the current rendering settings in the history
	 */
	void makeHistorySnapshot() {
	        if (rndControl != null ) {
	            if (history.getCurrent()==null || !rndControl.isSameSettings(history.getCurrent(), false))
	                history.add(rndControl.getRndSettingsCopy());
	            else 
	                history.resetPrevAction();
	        }
	}
	
	/**
	 * Turns on or off the specified channel.
	 *
	 * @param index The index of the channel
	 * @param active Pass <code>true</code> to turn the channel on,
	 * 				 <code>false</code> to turn it off.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setActive(int index, boolean active)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setActive(index, active);
	}

	/**
	 * Sets the compression level.
	 * 
	 * @param compression  The compression level.
	 */
	void setCompression(int compression)
	{
		if (rndControl == null) return;
		rndControl.setCompression(compression);
	}

	/**
	 * Sets the original settings.
	 *
	 * @throws RenderingServiceException If an error occurred while setting
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	void setOriginalRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setOriginalRndSettings();
	}

	/**
	 * Checks if the passed set of pixels is compatible.
	 * Returns <code>true</code> if the pixels set is compatible,
	 * <code>false</code> otherwise.
	 * 
	 * @param pixels The pixels to check.
	 * @return See above.
	 */
	boolean validatePixels(PixelsData pixels)
	{
		if (rndControl == null) return false;
		return rndControl.validatePixels(pixels);
	}

	/**
	 * Renders the specified plane.
	 * 
	 * @param pDef The plane to render.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while setting
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	BufferedImage render(PlaneDef pDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return null;
		return rndControl.render(pDef);
	}

	/**
	 * Renders the specified plane.
	 * 
	 * @param pDef The plane to render.
	 * @param compression The compression level.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while setting
	 *                                  the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	BufferedImage render(PlaneDef pDef, int compression)
	        throws RenderingServiceException, DSOutOfServiceException
	{
	    if (rndControl == null) return null;
	    return rndControl.render(pDef, compression);
	}

	/**
	 * Returns <code>true</code> if the passed rendering settings are the same
	 * that the current one, <code>false</code> otherwise.
	 *
	 * @param def The settings to check.
	 * @param checkPlane Pass <code>true</code> to take into account the
	 * 					 z-section and time-point, <code>false</code> 
	 * 					 otherwise.
	 * @return See above.
	 */
	boolean isSameSettings(RndProxyDef def, boolean checkPlane)
	{
		if (rndControl == null) return false;
		return rndControl.isSameSettings(def, checkPlane);
	}
	
       /**
        * Returns <code>true</code> if the rendering settings 
        * have been modified
        *
        * @return See above.
        */
	boolean isModified() {
	    if(rndControl!=null) {
	        return !rndControl.isSameSettings(rndDef, true);
	    }
	    return false;
	}

    /**
     * Returns <code>true</code> if the image with the active channels
     * is an RGB image, <code>false</code> otherwise.
     * 
     * @return See above.
     */
	boolean isMappedImageRGB(List channels)
	{
		if (rndControl == null) return false;
		return rndControl.isMappedImageRGB(channels);
	}

    /**
     * Sets the overlays.
     * 
     * @param tableID  The id of the table.
     * @param overlays The overlays to set, or <code>null</code> to turn
     * the overlays off.
     */
    void setOverlays(long tableID, Map<Long, Integer> overlays)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	if (rndControl == null) return;
    	rndControl.setOverlays(tableID, overlays);
    }

    /** 
     * Renders the default plane.
     *
     * @return See above.
     */
    BufferedImage renderImage()
    {
    	plane.t = getDefaultT();
    	plane.z = getDefaultZ();
    	try {
    		if (rndControl == null) return null;
    		return rndControl.render(plane, RenderingControl.LOW);
		} catch (Exception e) {
		    LogMessage msg = new LogMessage();
            msg.append("Error while rendering the image.");
            msg.print(e);
            MetadataViewerAgent.getRegistry().getLogger().error(this, msg);
		}
    	return null;
    }

    /**
     * Returns the dimension of the preview image.
     * 
     * @return See above.
     */
    Dimension getPreviewDimension()
    {
    	if (previewSize != null) return previewSize;
    	previewSize = Factory.computeThumbnailSize(PREVIEW_WIDTH,
    			PREVIEW_HEIGHT, getMaxX(), getMaxY());
    	return previewSize;
    }
    
    /** 
     * Resets the rendering settings.
     *
     * @throws RenderingServiceException If an error occurred while setting 
	 * 									the value.
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
    void resetRenderingSettings()
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	rndControl.resetSettings(rndDef);
    }
    
	/**
	 * Returns <code>true</code> if it is a large image, 
	 * <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	boolean isBigImage()
	{
		if (rndControl == null) return false;
		return rndControl.isBigImage();
	}

	/**
	 * Returns the number of bins per time interval.
	 * 
	 * @return See above
	 */
	int getMaxLifetimeBin()
	{
	    if (hasModuloT()) {
	        ModuloInfo info = modulo.get(ModuloInfo.T);
	        return info.getSize();
	    }
		if (isLifetimeImage()) return getMaxC()-1;
		return 0;
	}

	/**
	 * Returns <code>true</code> if the image has extra dimension along T.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasModuloT()
	{
	    return (modulo != null && modulo.containsKey(ModuloInfo.T));
	}

	/**
	 * Returns the modulo info if it exists.
	 *
	 * @return See above.
	 */
	ModuloInfo getModuloT()
	{
	    if (!hasModuloT()) return null;
	    return modulo.get(ModuloInfo.T);
	}

	/**
	 * Returns <code>true</code> if the image is a lifetime image,
	 * <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	boolean isLifetimeImage()
	{
	    if (hasModuloT()) return true;
		if (getMaxC() >= Renderer.MAX_CHANNELS) return true;
		return false;
	}

	/**
	 * Returns the selected bin for lifetime image.
	 * 
	 * @return See above.
	 */
	int getSelectedBin()
	{
	    if (!isLifetimeImage()) return -1;
	    if (hasModuloT()) {
	        return getDefaultT()-getRealSelectedT()*getMaxLifetimeBin();
	    }
	    List<Integer> active = getActiveChannels();
	    if (active == null || active.size() != 1) return 0;
	    return active.get(0);
	}

	/**
	 * Sets the selected lifetime bin.
	 *
	 * @param bin The selected bin.
	 * @param t The selected t.
	 */
	void setSelectedBin(int bin, int t)
		throws RenderingServiceException, DSOutOfServiceException
	{
	    if (hasModuloT()) {
	        int binSize = getMaxLifetimeBin();
	        int v = bin + t * binSize;
	        setSelectedXYPlane(getDefaultZ(), v);
	        return;
	    }
		List<ChannelData> channels = getChannelData();
		ChannelData channel;
		Iterator<ChannelData> i = channels.iterator();
		int index;
		while (i.hasNext()) {
			channel = i.next();
			index = channel.getIndex();
			setActive(index, index == bin);
		}
	}

	/**
	 * Returns the dimension of a tile.
	 * 
	 * @return See above.
	 */
	Dimension getTileSize()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return new Dimension(0, 0);
		return rndControl.getTileSize();
	}

	/**
	 * Returns the possible resolution levels. This method should only be used
	 * when dealing with large images.
	 *
	 * @return See above.
	 */
	int getResolutionLevels()
	{
		if (rndControl == null) return 1;
		return rndControl.getResolutionLevels();
	}

	/**
	 * Returns the currently selected resolution level. This method should only 
	 * be used when dealing with large images.
	 *
	 * @return See above.
	 */
	int getSelectedResolutionLevel()
	{
		if (rndControl == null) return 0;
		return rndControl.getSelectedResolutionLevel();
	}

	/**
	 * Sets resolution level. This method should only be used when dealing with
	 * large images.
	 *
	 * @param level The value to set.
	 */
	void setSelectedResolutionLevel(int level)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return;
		rndControl.setSelectedResolutionLevel(level);
	}

	/**
	 * Sets the channels.
	 *
	 * @param channels The updated channels.
	 */
	void setChannels(List<ChannelData> channels)
	{
		ViewerSorter sorter = new ViewerSorter();
		sortedChannel = Collections.unmodifiableList(sorter.sort(channels));
	}

	/**
	 * Returns <code>true</code> if the object can be annotated,
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @return See above.
	 */
	boolean canAnnotate()
	{
		ImageData image = getRefImage();
		if (image == null) return false;
		return image.canAnnotate();
	}

	/**
	 * Returns the collection of rendering controls. This method should only 
	 * be invoked when loading tiles.
	 *
	 * @return See above.
	 */
	List<RenderingControl> getRenderingControls()
	{
		if (rndControl == null) return null;
		List<RenderingControl> list = new ArrayList<RenderingControl>();
		list.add(rndControl);
		List<RenderingControl> slaves = rndControl.getSlaves();
		if (slaves != null && slaves.size() > 0) list.addAll(slaves);
		return list;
	}

	/**
	 * Returns the list of the levels.
	 * 
	 * @return See above.
	 */
	List<ResolutionLevel> getResolutionDescriptions()
	throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndControl == null) return null;
		return rndControl.getResolutionDescriptions();
	}

	/**
	 * Sets the annotations and parses the content.
	 * 
	 * @param annotations The annotations to parse.
	 */
	void setXMLAnnotations(Collection<XMLAnnotationData> annotations)
    {
	    modulo = new HashMap<Integer, ModuloInfo>();
	    if (CollectionUtils.isEmpty(annotations)) return;
        ModuloParser parser;
        Iterator<XMLAnnotationData> i = annotations.iterator();
        XMLAnnotationData data;
        List<ModuloInfo> infos;
        Iterator<ModuloInfo> j;
        ModuloInfo info;
        while (i.hasNext()) {
            data = i.next();
            parser = new ModuloParser(data.getText());
            try {
                parser.parse();
                infos = parser.getModulos();
                j = infos.iterator();
                while (j.hasNext()) {
                   info = j.next();
                    modulo.put(info.getModuloIndex(), info);
                }
            } catch (Exception e) {
                LogMessage msg = new LogMessage();
                msg.append("Error while reading modulo annotation.");
                msg.print(e);
                MetadataViewerAgent.getRegistry().getLogger().error(this, msg);
            }
        }
    }
	
	/**
         * Set the color mode to greyscale or RGB
         *
         * @param b <code>true</code> for switching to greyscale, RGB otherwise 
         */
	void setGreyscale(boolean b) {
	    if(b)
	        component.setColorModel(Renderer.GREY_SCALE_MODEL, true);
	    else
	        component.setColorModel(Renderer.RGB_MODEL, true);
	}
	
	/**
	 * Checks if the image pixel type is integer
	 * @return See above
	 */
	boolean isIntegerPixelData() {
        String t = image.getDefaultPixels().getPixelType();
        return t.equals(OmeroImageService.INT_8)
                || t.equals(OmeroImageService.UINT_8)
                || t.equals(OmeroImageService.INT_16)
                || t.equals(OmeroImageService.UINT_16)
                || t.equals(OmeroImageService.INT_32)
                || t.equals(OmeroImageService.UINT_32);
	}
}
