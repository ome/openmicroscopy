/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.Renderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface provided by the renderer component. 
 * The Renderer provides a top-level component hosting the rendering controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public interface Renderer
	extends ObservableComponent
{

    /** The value after which no ticks are displayed. */
	public static final int		MAX_NO_TICKS = 10;
    
	/** Identifies the grey scale color model. */
	public static final String  GREY_SCALE_MODEL = RenderingControl.GREY_SCALE;

	/** Identifies the RGB color model. */
	public static final String  RGB_MODEL = RenderingControl.RGB;
	
    /** 
     * The maximum number of channels before displaying the channels 
     * buttons in a scrollpane.
     */
    public static final int		MAX_CHANNELS = 10;
    
    /** 
     * Bound property name indicating to render the plane with the 
     * new rendering settings. 
     */
    public final static String  RENDER_PLANE_PROPERTY = "render_plane";
    
    /** Bound property name indicating that a new channel is selected. */
    public final static String  SELECTED_CHANNEL_PROPERTY = "selectedChannel";
    
    /** 
     * Bound property indicating that the pixels intensiy interval is 
     * modified.
     */
    public final static String  INPUT_INTERVAL_PROPERTY = "inputInterval";
    
    /** Bound property indicating that the color model has changed. */
    public final static String  COLOR_MODEL_PROPERTY = "colorModel";
    
	/** Bound property name indicating that a new z-section is selected. */
	public final static String  Z_SELECTED_PROPERTY = "zSelected";

	/** Bound property name indicating that a new timepoint is selected. */
	public final static String  T_SELECTED_PROPERTY = "tSelected";
	
	/** 
	 * Bound property name indicating to apply the rendering settings
	 * to all selected or displayed images. 
	 */
	public final static String  APPLY_TO_ALL_PROPERTY = "applyToAll";
	
    /** 
     * Sets the pixels intensity interval for the
     * currently selected channel.
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     */
    void setInputInterval(double s, double e);
    
    /** 
     * Sets the sub-interval of the device space. 
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     */
    void setCodomainInterval(int s, int e);
    
    /**
     * Sets the bit resolution and updates the image.
     * 
     * @param v The new bit resolution.
     */
    void setBitResolution(int v);
    
    /**
     * Sets the selected channel. This method is invoked when 
     * the channel has been selected from the viewer.
     * 
     * @param index The index of the selected channel.
     */
    void setSelectedChannel(int index);
    
    /**
     * Sets the family and updates the image.
     * 
     * @param family The new family value.
     */
    void setFamily(String family);
    
    /**
     * Sets the coefficient identifying a curve in the family
     * and updates the image.
     * 
     * @param k The new curve scoefficient.
     */
    void setCurveCoefficient(double k);
    
    /**
     * Sets the noise reduction flag to select the mapping algorithm
     * and updates the image.
     * 
     * @param b The noise reduction flag.
     */
    void setNoiseReduction(boolean b);
    
    /**
     * Returns the <code>Codomain map context</code> corresponding to
     * the specifed class.
     * 
     * @param mapType       The class identifying the context.
     * @return See above.
     */
    //CodomainMapContext getCodomainMapContext(Class mapType);

    /**
     * Sets the colour of the channel button in the renderer.
     * 
     * @param index The index of the channel
     */
    void setChannelColor(int index);
    
    /**
     * Fired if the colour model has been changed from RGB -> Greyscale or 
     * vise versa.
     */
    void setColorModelChanged();
    
    /**
     * Returns the current state.
     * 
     * @return See above
     */
    public int getState();
    
    /** Closes and disposes. */
    public void discard();

    /**
     * Returns the lower bound of the pixels intensity interval for the
     * currently selected channel.
     * 
     * @return See above.
     */
    public double getWindowStart();
    
    /**
     * Returns the upper bound of the pixels intensity interval for the
     * currently selected channel.
     * 
     * @return See above.
     */
    public double getWindowEnd();
    
    /**
     * Returns the global minimum for the currently selected channel.
     * 
     * @return See above.
     */
    public double getGlobalMin();
    
    /**
     * Returns the global maximum for the currently selected channel.
     * 
     * @return See above.
     */
    public double getGlobalMax();
    
    /**
     * Returns the global minimum for the currently selected channel.
     * 
     * @return See above.
     */
    public double getLowestValue();
    
    /**
     * Returns the global maximum for the currently selected channel.
     * 
     * @return See above.
     */
    public double getHighestValue();

    /**
     * Returns the {@link RendererUI View}.
     * 
     * @return See above.
     */
    public JComponent getUI();

    /**
     * Sets the specified rendering control.
     * 
     * @param rndControl The value to set.
     */
	public void setRenderingControl(RenderingControl rndControl);

	/** 
	 * Partially resets the rendering settings. Invoked when 
	 * selecting an image from the history.
	 */ 
	public void resetRndSettings();
	
	/**
	 * Invokes when the state of the viewer has changed.
	 * 
	 * @param b Pass <code>true</code> to enable the UI components, 
	 *          <code>false</code> otherwise.
	 */
	public void onStateChange(boolean b);

	/**
	 * Indicates that a channel has been selected using the channel button.
	 * 
	 * @param index	
	 * @param booleanValue
	 */
	void setChannelSelection(int index, boolean booleanValue);

	/**
	 * Sets the color of the specified channel depending on the current color
	 * model.
	 * 
	 * @param index The index of the channel.
	 * @param color The color to set.
	 */
	void setChannelColor(int index, Color color);

	/**
	 * Sets the color model.
	 * 
	 * @param index One of the constants defined by this class.
	 */
	void setColorModel(String index);

	/**
	 * Returns the color model.
	 * 
	 * @return See above.
	 */
	String getColorModel();
	
	/**
	 * Sets the selected XY-plane. A new plane is then rendered.
	 * 
	 * @param z The selected z-section.
	 * @param t The selected timepoint.
	 */
	void setSelectedXYPlane(int z, int t);

	/** Applies the rendering settings to the selected or displayed images. */
	void applyToAll();
	
	/** Notifies that the rendering settings have been applied. */
	void onSettingsApplied();
}
