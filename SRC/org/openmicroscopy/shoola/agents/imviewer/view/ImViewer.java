/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewer
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

package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface provided by the viewer component. 
 * The Viewer provides a top-level window hosting the rendered image.
 *
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public interface ImViewer
    extends ObservableComponent
{

    /** Flag to denote the <i>New</i> state. */
    public static final int     NEW = 1;
    
    /** Flag to denote the <i>Loading Rendering Settings</i> state. */
    public static final int     LOADING_RENDERING_CONTROL = 2;
    
    /** Flag to denote the <i>Loading Image</i> state. */
    public static final int     LOADING_IMAGE = 3;
    
    /** Flag to denote the <i>Loading Metadata</i> state. */
    //public static final int     LOADING_METADATA = 4;
    
    /** Flag to denote the <i>Loading Plane Info</i> state. */
    public static final int     LOADING_PLANE_INFO = 4;
    
    /** Flag to denote the <i>Ready Image</i> state. */
    public static final int     READY_IMAGE = 5;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     READY = 6;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     DISCARDED = 7;
    
    /** Flag to denote the <i>Channel Movie</i> state. */
    public static final int     CHANNEL_MOVIE = 8;
    
    /** Bound property name indicating that a new z-section is selected. */
    public final static String  Z_SELECTED_PROPERTY = "z_selected";
    
    /** Bound property name indicating that a new timepoint is selected. */
    public final static String  T_SELECTED_PROPERTY = "t_selected";
    
    /** Bound property name indicating that a channel is activated. */
    public final static String  CHANNEL_ACTIVE_PROPERTY = "channel_active";
    
    /** Bound property indicating that the window state has changed. */
    public final static String  ICONIFIED_PROPERTY = "iconified";
    
    /** Identifies the grey scale color model. */
    public static final String  GREY_SCALE_MODEL = RenderingControl.GREY_SCALE;
    
    /** Identifies the RGB color model. */
    public static final String  RGB_MODEL = RenderingControl.RGB;
    
    /** Identifies the HSB color model. */
    public static final String  HSB_MODEL = RenderingControl.HSB;
    
    /**
     * Iconified if the specified value is <code>true</code>, deiconified
     * otherwise.
     * 
     * @param b Pass <code>true</code> to iconify, <code>false</code> otherwise.
     */
    void iconified(boolean b);
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();

    /**
     * Callback used by data loaders to provide the viewer with feedback about
     * the data retrieval.
     * 
     * @param description   Textual description of the ongoing operation.
     * @param perc          Percentage of the total work done. If negative, 
     *                      it is interpreted as not available.
     */
    public void setStatus(String description, int perc);
    
    /**
     * Sets the zoom factor.
     * 
     * @param factor The value ot set.
     */
    public void setZoomFactor(double factor);
    
    /**
     * Sets the image rate.
     * 
     * @param level The value to set.
     */
    public void setRateImage(int level);
    
    /**
     * Sets the color model. The key of the map is one out of the following
     * constants {@link #GREY_SCALE_MODEL}, {@link #RGB_MODEL} or 
     * {@link #HSB_MODEL} and the value is the action firing the event.
     * 
     * @param m The value to set.
     */
    public void setColorModel(Map m);

    /**
     * Sets the selected XY-plane. A new plane is then rendered.
     * 
     * @param z The selected z-section.
     * @param t The selected timepoint.
     */
    public void setSelectedXYPlane(int z, int t);

    /**
     * Sets the image to display.
     * 
     * @param image The image to display.
     */
    public void setImage(BufferedImage image);

    /**
     * Plays a movie across channel i.e. one channel is selected at a time.
     * 
     * @param play  Pass <code>true</code> to play the movie, 
     *              <code>false</code> otherwise.
     */
    public void playChannelMovie(boolean play);
    
    /**
     * Sets the color of the specified channel depending on the current color
     * model.
     * 
     * @param index The OME index of the channel.
     * @param c     The color to set.
     */
    public void setChannelColor(int index, Color c);
    
    /**
     * Selects or deselects the specified channel.
     * The selection process depends on the currently selected color model.
     * 
     * @param index The OME index of the channel.
     * @param b     Pass <code>true</code> to select the channel,
     *              <code>false</code> otherwise.
     */
    public void setChannelSelection(int index, boolean b);
    
    /** 
     * Activates/desactivates the specified channel
     * 
     * @param index The index of the channel.
     * @param b     Pass <code>true</code> to activate the channel, 
     *              <code>false</code> otherwise.
     */
    public void setChannelActive(int index, boolean b);
    
    /** Plays a movie across channels. */
    public void displayChannelMovie();
    
    /**
     * Returns the number of channels.
     * 
     * @return See above.
     */
    public int getMaxC();
    
    /**
     * Returns the number of timepoints.
     * 
     * @return See above.
     */
    public int getMaxT();
    
    /**
     * Returns the number of z-sections.
     * 
     * @return See above.
     */
    public int getMaxZ();

    /**
     * Sets the {@link RenderingControl}.
     * 
     * @param result The {@link RenderingControl} to set.
     */
    public void setRenderingControl(RenderingControl result);
    
    /** Renders the current XY-plane. */
    public void renderXYPlane();
    
    /** 
     * Brings up on screen the widget controlling the rendering settings
     * If the widget is already visible, nothing happens. If iconified, the 
     * widget is de-iconified.
     */
    public void showRenderer();
    
    /**
     * Returned the name of the rendered image.
     * 
     * @return See above.
     */
    public String getImageName();
    
    /**
     * Returns the currently selected color model.
     * 
     * @return See above.
     */
    public String getColorModel();
   
    /**
     * Returns the {@link ImViewerUI View}.
     * 
     * @return See above.
     */
    public JFrame getUI();
    
    /**
     * Returns the default z-section.
     * 
     * @return See above.
     */
    public int getDefaultZ();
    
    /** 
     * Returns a list of {@link BufferedImage}s composing the displayed image.
     * Returns <code>null</code> if the the color model is
     * {@link #GREY_SCALE_MODEL} or if the image isn't the combination of at 
     * least two channels.
     * 
     * @return See above.
     */
    public List getImageComponents();
    
    /**
     * Returns the image currently displayed.
     * 
     * @return See above.
     */
    public BufferedImage getImage();
    
    /**
     * Returns the default timepoint.
     * 
     * @return See above.
     */
    public int getDefaultT();

    /**
     * The size in microns of a pixel along the X-axis.
     * 
     * @return See above.
     */
    public float getPixelsSizeX();

    /**
     * Returns the title of the viewer.
     * 
     * @return See above.
     */
    public String getViewTitle();

    /**
     * Returns a list with the index of the active channels. Returns
     * <code>null</code> if no active channel.
     * 
     * @return See above.
     */
    public List getActiveChannels();
    
    /**
     * Returns the channel metadata.
     * 
     * @param index The index of the channel.
     * @return See above.
     */
    ChannelMetadata getChannelMetadata(int index);

    /**
     * Returns <code>true</code> if the unit bar is painted on top of 
     * the displayed image, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isUnitBar();
    
    /**
     * Sets the value of the flag controlling if the unit bar is painted or not.
     * 
     * @param b Pass <code>true</code> to paint the unit bar, 
     *          <code>false</code> otherwise.
     */
    public void setUnitBar(boolean b);
    
    public int getHistoryState();
    
    public Color getChannelColor(int index);
    
    public void setChannelColor(Color c, int index);
    
}
