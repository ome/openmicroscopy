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

package org.openmicroscopy.shoola.agents.imviewer.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionRef;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.rnd.data.Tile;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageAcquisitionData;
import omero.gateway.model.ImageData;

/** 
 * Defines the interface provided by the viewer component. 
 * The Viewer provides a top-level window hosting the rendered image.
 *
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public interface ImViewer
	extends ObservableComponent
{
	
	/** Flag to indicate that the image is not compressed. */
	public static final int 	UNCOMPRESSED = RenderingControl.UNCOMPRESSED;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * medium Level of compression. 
	 */
	public static final int 	MEDIUM = RenderingControl.MEDIUM;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * low Level of compression. 
	 */
	public static final int 	LOW = RenderingControl.LOW;
	
	/** The title of the <code>Image</code> view. */
	public static final String 	TITLE_VIEW_INDEX = "Image";
	
	/** The title of the <code>Grid</code> view. */
	public static final String 	TITLE_GRID_INDEX = "Split";
	
	/** The title of the <code>Projection</code> view. */
	public static final String 	TITLE_PROJECTION_INDEX = "Projection";
	
	/** 
	 * The maximum number of overlays before displaying them in a 
	 * Scroll Pane. 
	 */
    public static final int		MAX_OVERLAYS = 8;
    
    /** 
     * The maximum number of channels before displaying the channels 
     * buttons in a scroll pane.
     */
    public static final int		MAX_CHANNELS = Renderer.MAX_CHANNELS;
    
	/** The minimum size of an original image. */
	public static final int		MINIMUM_SIZE = 96;
	
	/** The maximum size of an original image. */
	public static final int		MAXIMUM_SIZE = 256;
	
	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Loading Rendering Settings</i> state. */
	//public static final int     LOADING_RENDERING_CONTROL = 2;

	/** Flag to denote the <i>Loading Image</i> state. */
	public static final int     LOADING_IMAGE = 3;

	/** Flag to denote the <i>Loading Image</i> state. */
	public static final int     LOADING_IMAGE_CANCELLED = 16;
	
	/** Flag to denote the <i>Loading Metadata</i> state. */
	public static final int     LOADING_METADATA = 4;

	/** Flag to denote the <i>Loading Plane Info</i> state. */
	public static final int     LOADING_PLANE_INFO = 5;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 6;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 7;

	/** Flag to denote the <i>Channel Movie</i> state. */
	public static final int     CHANNEL_MOVIE = 8;

	/** Flag to denote the <i>Rendering control loaded</i> state. */
	//public static final int     RENDERING_CONTROL_LOADED = 9;
	
	/** Flag to denote the <i>Loading Image Data</i> state. */
	public static final int     LOADING_IMAGE_DATA = 10;

	/** Flag to denote the <i>Loading Projection data</i> state. */
	public static final int     LOADING_PROJECTION_DATA = 11;

	/** Flag to denote the <i>Projection preview</i> state. */
	public static final int     PROJECTION_PREVIEW = 12;
	
	/** Flag to denote the <i>projecting</i> state. */
	public static final int     PROJECTING = 13;
	
	/** Flag to denote the <i>Pasting settings</i> state. */
	public static final int     PASTING = 14;
	
	/** Flag to denote the <i>Loading The tiles</i> state. */
	public static final int     LOADING_TILES = 15;
	
	/** Flag to denote the <i>Loading the RE</i> state. */
	public static final int     LOADING_RND = 17;
	
	/** Flag to denote the <i>Loading the Bird eye view</i> state. */
	public static final int     LOADING_BIRD_EYE_VIEW = 18;
	
	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     CANCELLED = 19;
	
	/** Bound property name indicating that a new z-section is selected. */
	public final static String  Z_SELECTED_PROPERTY = "zSelected";

	/** Bound property name indicating that a new timepoint is selected. */
	public final static String  T_SELECTED_PROPERTY = "tSelected";

	/** Bound property name indicating that a new bin is selected. */
    public final static String  BIN_SELECTED_PROPERTY = "binSelected";
    
	/** Bound property name indicating that a channel is activated. */
	public final static String  CHANNEL_ACTIVE_PROPERTY = "channelActive";

	/** Bound property indicating that the window state has changed. */
	public final static String  ICONIFIED_PROPERTY = "iconified";

	/** Identifies the grey scale color model. */
	public static final String  GREY_SCALE_MODEL = RenderingControl.GREY_SCALE;

	/** Identifies the RGB color model. */
	public static final String  RGB_MODEL = RenderingControl.RGB;

	/** Bound Property name indicating that a channel colour has changed. */
	public static final String	CHANNEL_COLOR_CHANGED_PROPERTY = 
									"channelColorChanged";

	/** Bound Property name indicating that the colour model has changed. */
	public static final String  COLOR_MODEL_CHANGED_PROPERTY = 
										"colorModelChanged";

	/** Bound Property name indicating rendering settings are set. */
	public static final String  RND_SETTINGS_PROPERTY = "rndSettings";
	
	/** Bound Property name indicating that the renderer is shown or hidden. */
	public static final String  HISTORY_VISIBLE_PROPERTY = "historyVisible";
	
	/** 
	 * Bound Property name indicating to keep track of an image recently
	 * viewed.
	 */
	public static final String  RECENT_VIEWER_PROPERTY = "reventViewer";
	
	/** 
	 * Bound Property name indicating that a new tabbed pane has been
	 * selected.
	 */
	public static final String  TAB_SELECTION_PROPERTY = "tabSelection";
	
	/** Bound Property name indicating to register the component.	 */
	public static final String  REGISTER_PROPERTY = "register";
	
	/** Identifies the <code>Color Picker</code> menu. */
	public static final int 	COLOR_PICKER_MENU = 0;

	/** Identifies the <code>Activity</code> menu. */
	public static final int 	ACTIVITY_MENU = 1;

	/** Identifies the index of the image viewer panel. */
	public static final int		VIEW_INDEX = 0;

	/** Identifies the index of the grid viewer panel. */
	public static final int		GRID_INDEX = 1;
	
	/** Identifies the index of the projection viewer panel. */
	public static final int		PROJECTION_INDEX = 2;
	
	/** Identifies the index of the renderer panel. */
	public static final int		RENDERER_INDEX = 3;
	
	/** Identifies the index of the metadata panel. */
	public static final int		METADATA_INDEX = 4;

	/** Shows or hides the lens. */
	public void showLens();

	/**
	 * Returns the zoomed image from the lens component. 
	 * 
	 * @return See above..
	 */
	public BufferedImage getZoomedLensImage();

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
	 * @param settings The settings set by another user.
	 * @param userID   The id of the user who set the settings.
	 * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
	 */
	public void activate(RndProxyDef settings, long userID, int displayMode);

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
	 * Call-back used by data loaders to provide the viewer with feedback about
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
	 * @param factor 	The value ot set.
	 * @param zoomIndex The index of the factor.
	 */
	public void setZoomFactor(double factor, int zoomIndex);

	/**
	 * Returns <code>true</code> if the zoom factor is set so that
	 * the image fit to the window size, <code>false</code> otherwise.
	 *  
	 * @return see above.
	 */
	public boolean isZoomFitToWindow();

	/**
	 * Sets the color model. 
	 * 
	 * @param m The index corresponding to the color model.
	 */
	public void setColorModel(int m);

	/**
	 * Sets the selected XY-plane. A new plane is then rendered.
	 * 
	 * @param z The selected z-section.
	 * @param t The selected timepoint.
	 * @param roi The ROI to display.
	 */
	public void setSelectedRegion(int z, int t, Rectangle roi);
	
	/**
	 * Sets the selected XY-plane. A new plane is then rendered.
	 * 
	 * @param z The selected z-section.
	 * @param t The selected timepoint.
	 */
	public void setSelectedXYPlane(int z, int t);

	/**
	 * Sets the selected XY-plane. A new plane is then rendered.
	 * 
	 * @param z The selected z-section.
	 * @param t The selected timepoint.
	 * @param bin The selected bin, only used for lifetime.
	 */
	public void setSelectedXYPlane(int z, int t, int bin);
	
	/**
	 * Sets the image to display.
	 * 
	 * @param image The image to display.
	 */
	public void setImage(Object image);

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
	 * @param preview Pass <code>true</code> to indicate that it is a color
	 * 					preview, <code>false</code> otherwise.
	 */
	public void setChannelColor(int index, Color c, boolean preview);

	/**
	 * Selects or deselects the specified channel.
	 * The selection process depends on the currently selected color model.
	 * 
	 * @param index    The index of the channel.
	 * @param selected Pass <code>true</code> to select the channel,
	 *                 <code>false</code> otherwise.
	 */
	public void setChannelSelection(int index, boolean selected);

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
	public int getRealT();

	/**
	 * Returns the number of z-sections.
	 * 
	 * @return See above.
	 */
	public int getMaxZ();

	/** Renders the current XY-plane. */
	public void renderXYPlane();

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
	 * @param colorModel The index of the color model either 
	 * 					 {@link #GREY_SCALE_MODEL} or {@link #RGB_MODEL}.
	 * @param includeROI Passed <code>true</code> to add ROI, 
	 * 					 <code>false</code> otherwise.
	 * @return See above.
	 */
	public List getImageComponents(String colorModel, boolean includeROI);

	/**
	 * Returns the image currently displayed.
	 * 
	 * @param includeROI Passed <code>true</code> to add ROI, 
	 * 					 <code>false</code> otherwise.
	 * @return See above.
	 */
	public BufferedImage getDisplayedImage(boolean includeROI);

	/**
	 * Returns the default timepoint.
	 * 
	 * @return See above.
	 */
	public int getDefaultT();

	/**
	 * Returns the size in microns of a pixel along the X-axis.
	 * 
	 * @return See above.
	 */
	public double getPixelsSizeX();

	/**
	 * Returns the size in microns of a pixel along the Y-axis.
	 * 
	 * @return See above.
	 */
	public double getPixelsSizeY();

	/**
	 * Returns the size in microns of a pixel along the X-axis.
	 * 
	 * @return See above.
	 */
	public double getPixelsSizeZ();

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
	ChannelData getChannelMetadata(int index);

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

	/**
	 * Returns the previous state of the component
	 * 
	 * @return See above.
	 * @see #getState()
	 */
	public int getHistoryState();

	/**
	 * Returns the color of the channel.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	public Color getChannelColor(int index);

	/**
	 * Sets the size of the unit bar in microns.
	 * 
	 * @param size The size of the unit bar in microns.
	 */
	public void setUnitBarSize(double size);

	/** Brings up on screen the unit bar selection widget. */
	public void showUnitBarSelection();

	/** Resets the defaults settings. */
	public void resetDefaults();

	/**
	 * Returns the value (with two decimals) of the unit bar or 
	 * <code>null</code> if the actual value is <i>negative</i>.
	 * 
	 * @return See above.
	 */
	public String getUnitBarValue();

	/**
	 * Returns the size of the unit bar.
	 * 
	 * @return See above.
	 */
	public double getUnitBarSize();

	/**
	 * Returns the color of the unit bar.
	 * 
	 * @return See above.
	 */
	public Color getUnitBarColor();

	/**
	 * Returns an iconified version of the viewed image.
	 * 
	 * @return See above.
	 */
	public ImageIcon getImageIcon();

	/**
	 * Brings up the menu on top of the specified component at 
	 * the specified location.
	 * 
	 * @param menuID    The id of the menu. One out of the following constants:
	 *                  {@link #COLOR_PICKER_MENU}.
	 * @param source	The component that requested the popup menu.
	 * @param location	The point at which to display the menu, relative to the
	 *                  <code>component</code>'s coordinates.
	 */
	public void showMenu(int menuID, Component source, Point location);

	/**
	 * Returns the number of pixels along the X-axis.
	 * 
	 * @return See above.
	 */
	public int getMaxX();

	/**
	 * Returns the number of pixels along the X-axis.
	 * 
	 * @return See above.
	 */
	public int getMaxY();

	/**
	 * Returns the index of the selected tabbed pane.
	 * One of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getSelectedIndex();

	/**
	 * Plays or stops playing the movie. The movie player may not be visible
	 * depending on the specified parameter.
	 * Indicates that the movie player is visible if the passed value is
	 * <code>true</code>, is hidden if the passed value is <code>false</code>.
	 * 
	 * @param play 		Pass <code>true</code> to play the movie, 
	 * 					<code>false</code> to stop.
	 * @param visible 	Pass <code>true</code> to display the movie player,
	 * 					<code>false</code> to hide it. If the movie player
	 * 					was visible, the movie stops regardless of the 
	 * 					first specified parameter.
	 * @param index		
	 */
	public void playMovie(boolean play, boolean visible, int index);

	/**
	 * Returns the collection of images composing the grid.
	 * 
	 * @return See above.
	 */
	public List getGridImages();
	
	/**
	 * Returns the image in color when the channels are in grey scale.
	 * This method should only be invoked when the color model 
	 * is <code>GreyScale</code>.
	 * 
	 * @return See above.
	 */
	public BufferedImage getCombinedGridImage();

	/**
	 * Returns the image displayed in the Grid Panel.
	 * 
	 * @return See above.
	 */
	public BufferedImage getGridImage();
	
	/**
	 * Returns a projected version of the image (preview).
	 * 
	 * @return See above.
	 */
	public BufferedImage getDisplayedProjectedImage();

	/** 
	 * Returns a list of {@link BufferedImage}s composing the lens' image.
	 * Returns <code>null</code> if the the color model is
	 * {@link #GREY_SCALE_MODEL} or if the lens' image isn't the combination of 
	 * at least two channels.
	 * 
	 * @param colorModel 	The index of the color model either 
	 * 						{@link #GREY_SCALE_MODEL} or {@link #RGB_MODEL}.
	 * @return See above.
	 */
	public List getLensImageComponents(String colorModel);

	/**
	 * Returns <code>true</code> if the textual information is painted on 
	 * top of the grid image, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isTextVisible();

	/**
	 * Returns to <code>true</code> if the textual information is painted on 
	 * top of the grid image, to <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	public void setTextVisible(boolean b);

	/** 
	 * Brings up on screen the Measurement Tool.
	 * 
	 * 
	 * @param loc The point at which to display the dialog.
	 */
	public void showMeasurementTool(Point loc);
	
	/**
	 * Adds the passed component to the viewer.
	 * 
	 * @param view The component to add.
	 */
	public void addToView(JComponent view);

	/**
	 * Removes the passed component from the viewer.
	 * 
	 * @param view The component to remove.
	 */
	public void removeFromView(JComponent view);

	/**
	 * Returns <code>true</code> if the user used the lens for this,
	 * <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	public boolean hasLens();

	/**
	 * Returns the zoom factor.
	 * 
	 * @return See above.
	 */
	public double getZoomFactor();

	/**
	 * Returns <code>true</code> if the playing a movie, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isPlayingMovie();

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
	 * Returns <code>true</code> if the specified channel is active,
	 * <code>false</code> otherwise.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	public boolean isChannelActive(int index);

	/**
	 * Returns the image displaying only the passed channel
	 * for the grid view when the channel is not 
	 * mapped to <code>RED</code>, <code>GREEN</code> or <code>BLUE</code>.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	//public BufferedImage getImageForGrid(int index);

	/** Copies the rendering settings. */
	public void copyRenderingSettings();

	/** Pastes the rendering settings. */
	public void pasteRenderingSettings();

	/**
	 * Returns <code>true</code> if there is some rendering settings to save,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasSettingsToPaste();

	/**
	 * Returns <code>true</code> if the history is displayed, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isHistoryShown();
	
	/** 
	 * Shows or hides the local history.
	 * 
	 * @param b Pass <code>true</code> to display the history,
	 * 			<code>false</code> otherwise.
	 */
	public void showHistory(boolean b); 
	
	/** Resets the default settings. */
    public void resetDefaultRndSettings();
    
    /** Saves the rendering settings. 
     * 
     * @param post Pass <code>true</code> to post an event,
     *             <code>false</code> otherwise.
     */
    public void saveRndSettings(boolean post);
    
    /** Moves the window to the front. */
    public void toFront();
    
    /**
     * Returns the index if the movie is playing.
     * 
     * @return See above.
     */
    public int getMovieIndex();
    
    /**
     * Returns the list of channels turned on in the <code>GridView</code>.
     * 
     * @return See above.
     */
    public List getActiveChannelsInGrid();

    /** Brings up the preferences widget. */
	public void showPreferences();

	/**
	 * Sets the rendering settings set by other users.
	 * 
	 * @param map The map with the value to set.
	 * @param userID The identifier of the user or <code>-1</code>.
	 */
	public void setRenderingSettings(Map map, long userID);

	/** 
	 * Retrieves the rendering settings set by other users. 
	 * 
	 * @param source	The component that requested the pop-up menu.
	 * @param location	The point at which to display the menu, relative to the
	 *                  <code>component</code>'s coordinates.
	 */
	public void retrieveRelatedSettings(Component source, Point location);
	
	/**
	 * Sets the magnification factor of the grid image.
	 * 
	 * @param factor The value to set.
	 */
	public void setGridMagnificationFactor(double factor);
    
	/**
	 * Convenience method returning details about the currently logged in
	 * user.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getUserDetails();

	/**
	 * Sets the rendering settings set by the passed user.
	 * 
	 * @param exp The user to handle.
	 */
	public void setUserRndSettings(ExperimenterData exp);

	/**
	 * Adds the view identified by the index.
	 * 
	 * @param index The index identifying the view.
	 */
	public void showView(int index);

	/** Sets the original rendering settings. */
	public void setOriginalRndSettings();

	/**
	 * Projects the whole image according the projection parameters.
	 * 
	 * @param ref Object containing the projection parameters.
	 */
	public void projectImage(ProjectionRef ref);

	/**
	 * Sets the containers where the projected image could be saved.
	 * 
	 * @param containers The collection to set.
	 */
	public void setContainers(Collection containers);

	/** Loads the containers containing the image. */
	public void loadContainers();
	
	/**
	 * Sets the projected preview image.
	 * 
	 * @param image The value to display.
	 */
	public void setProjectionPreview(Object image);
	
	/**
	 * Sets the newly created projected image.
	 * 
	 * @param image 		The projected image.
	 * @param indexes 		The channel's indexes projected.
	 * @param containers    The containers where the projected image has been 
	 * 						added.
	 * @param applySettings Pass <code>true</code> to set the rendering settings
	 * 						of the original image to the new pixels set,
	 * 						<code>false</code> otherwise.
	 */
	public void setProjectedImage(ImageData image, List<Integer> indexes, 
			List<DataObject> containers, boolean applySettings);

	/**
	 * Sets the settings created for the projected image.
	 * 
	 * @param result	The value to set.
	 * @param image 	The projected image.
	 */
	public void setProjectedRenderingSettings(Boolean result, ImageData image);
	
	/**
     * Sets the context of the node.
     * 
     * @param parent		The parent of the image or <code>null</code> 
     * 						if no context specified.
     * @param grandParent   The grandparent of the image or <code>null</code> 
     * 						if no context specified.
     */
    public void setContext(DataObject parent, DataObject grandParent);

    /**
     * Sets the plane information related to the image.
     * 
     * @param collection The collection of plane info objects.
     */
	public void setPlaneInfo(Collection collection);

	/**
	 * Sets the image data.
	 * 
	 * @param data The image to set.
	 */
	public void setImageData(ImageData data);

	/**
	 * Sets the index of the selected tabbed pane.
	 * 
	 * @param index The selected index.
	 */
	public void setSelectedPane(int index);

	/** Loads the metadata. */
	public void loadMetadata();

	/** Indicates that the compression level has been modified. */
	public void setCompressionLevel();

	/** Clears the history. */
	public void clearHistory();

	/**
	 * Returns <code>true</code> if the rendering settings used to render 
	 * the image are the original ones, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isOriginalSettings();

	/**
	 * Sets the rendering settings to paste.
	 * 
	 * @param rndProxyDef The settings to paste.
	 */
	public void setSettingsToPaste(RndProxyDef rndProxyDef);

	/**
	 * Returns the collection of <code>ChannelData</code> sorted by emission 
	 * wavelength.
	 * 
	 * @return See above.
	 */
	public List<ChannelData> getSortedChannelData();
	
	/**
	 * Brings up the color picker.
	 * 
	 * @param index The index of the channel.
	 */
	public void showColorPicker(int index);

	/** Loads all the datasets available. */
	public void loadAllContainers();
	
	/**
	 * Returns the unit in microns.
	 * 
	 * @return See above.
	 */
	public double getUnitInRefUnits();

	/** Makes a movie. */
	public void makeMovie();

	/**
	 * Notifies the component that the rendering control is loaded.
	 * 
	 * @param reload Pass <code>true</code> if the rendering control has been
	 * 				 reloaded following an exception, <code>false</code> if 
	 * 				 it is an initial load.
	 */
	public void onRndLoaded(boolean reload);

	/**
	 * Invokes when a channel is selected.
	 * 
	 * @param index The index of the channel.
	 */
	void onChannelSelection(int index);

	/**
	 * Returns <code>true</code> if the split view is allowed,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean allowSplitView();

	/**
	 * Turns all channels on or off depending on the passed value.
	 * 
	 * @param selection Pass <code>true</code> to select, <code>false</code>
	 * 					otherwise.
	 */
	void selectAllChannels(boolean selection);

	/**
	 * Sets the measurements associated to either the image or the plate.
	 * 
	 * @param result The collection to set.
	 */
	void setMeasurements(Collection result);

	/**
	 * Scrolls to viewport.
	 * 
	 * @param bounds The rectangle to display if possible.
	 */
	public void scrollToViewport(Rectangle bounds);

    /**
     * Returns <code>true</code> if the passed channels compose an RGB image, 
     * <code>false</code> otherwise.
     * 
     * @param channels The collection of channels to handle.
     * @return See above.
     */
	public boolean isMappedImageRGB(List channels);
	
	/**
	 * Renders the overlays. 
	 * 
	 * @param index    The index of the selected channel or <code>-1</code>.
	 * @param selected Pass <code>true</code> if the channel is selected,
	 * 				   <code>false</code> otherwise.
	 */
	public void renderOverlays(int index, boolean selected);
	
	/**
	 * Indicates that the color of the channel has changed.
	 * 
	 * @param index The index of the channel.
	 */
	public void onChannelColorChanged(int index);
	
	/**
	 * Returns <code>true</code> if the image is a large image,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isBigImage();

	/** Refreshes the view. */
	public void refresh();

	/** 
	 * Closes the viewer. 
	 * 
	 * @param notifyUser Pass <code>true</code> to notify the user, 
	 * 					<code>false</code> otherwise.
	 */
	public void close(boolean notifyUser);

	/** Detaches the viewer. */
	public void detach();
	
	/**
	 * Returns <code>true</code> if the permissions of the group only
	 * allow to view the image but not save the rendering settings, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean canAnnotate();
	
	/**
	 * Returns <code>true</code> if the user currently logged in is the
	 * owner of the image, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isUserOwner();
	
	/** 
	 * Sets the maximum range for channels.
	 * 
	 *  @param absolute Pass <code>true</code> to set it to the absolute value,
	 *  				<code>false</code> to the minimum and maximum.
	 */
	void setRangeAllChannels(boolean absolute);
	
	/** Uses the rendering settings of the owner. */
	void setOwnerSettings();

	/**
	 * Returns <code>true</code> to include the ROI in the saving option,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean includeROI();

	/**
	 * Sets the image displayed in the bird eye view.
	 * 
	 * @param result The value to set.
	 * @param scaled Indicates if the result is a scaled image
	 */
	void setBirdEyeView(BufferedImage result, boolean scaled);
	
    /**
     * Returns the number of rows, default is <code>1</code>.
     * 
     * @return See above.
     */
    int getRows();
    
    /**
     * Returns the number of columns, default is <code>1</code>.
     * 
     * @return See above.
     */
    int getColumns();
    
    /**
     * Returns the tiles to display.
     * 
     * @return See above.
     */
    Map<Integer, Tile> getTiles();

    /**
     * Indicates that the number of tiles loaded.
     * 
     * @param count The number of tiles loaded.
     */
	public void setTileCount(int count);
    
	/**
	 * Loads the tiles corresponding to the specified region.
	 * 
	 * @param region The selected region.
	 */
	public void loadTiles(Rectangle region);
	
	/**
	 * Returns the size of the tiled image along the X-axis i.e.
	 * the size of a tile along the X-axis multiplied by the number of columns.
	 * 
	 * @return See above.
	 */
	int getTiledImageSizeX();
	
	/**
	 * Returns the size of the tiled image along the Y-axis i.e.
	 * the size of a tile along the Y-axis multiplied by the number of rows.
	 * 
	 * @return See above.
	 */
	int getTiledImageSizeY();
	
	/** Cancels the rendering of the image.*/
	void cancelInit();
	
	/**
	 * Returns <code>true</code> if the image is compressed,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isCompressed();

	/**
         * Checks if the {@link Renderer} is loaded
         * @return <code>true</code> if the Renderer is loaded, <code>false</code> otherwise
         */
	boolean isRendererLoaded();
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	SecurityContext getSecurityContext();

	/**
	 * Invokes when the channels have been modified. Updates the values
	 * displayed in the measurement tool.
	 * 
	 * @param channels The channels to handle.
	 */
	void onUpdatedChannels(List<ChannelData> channels);

	/** 
	 * Returns the display mode.
	 * 
	 * @return See above.
	 */
	int getDisplayMode();
	
	/**
	 * Returns the id of the pixels set this viewer is for.
	 * 
	 * @return See above.
	 */
	long getPixelsID();
	
	/**
	 * Returns the selected resolution level.
	 * 
	 * @return See above.
	 */
	int getSelectedResolutionLevel();
	
	/**
	 * Returns the possible resolution levels. This method should only be used
	 * when dealing with large images.
	 * 
	 * @return See above.
	 */
	int getResolutionLevels();

	/** 
	 * Returns the selected bin.
	 * 
	 * @return See above.
	 */
    int getSelectedBin();

    /**
     * Returns the number of bins per time interval
     * 
     * @return See above
     */

    int getMaxLifetimeBin();

    /**
     * Returns the currently selected time-point.
     * 
     * @return See above.
     */
    int getRealSelectedT();
    
    /**
     * Reloads the 'saved by' thumbnails of the the rendering panel
     */
    void reloadRenderingThumbs();
    
    /**
     * Returns if interpolation is enabled or not
     * @return See above.
     */
    boolean isInterpolation();

    /**
     * En-/Disables interpolation
     * 
     * @param interpolation
     */
    void setInterpolation(boolean interpolation);
    
    /**
     * Set the {@link ImageAcquisitionData}
     * @param data The {@link ImageAcquisitionData}
     */
    void setImageAcquisitionData(ImageAcquisitionData data);
    
    /**
     * Get the {@link ImageAcquisitionData}
     * @return See above
     */
    ImageAcquisitionData getImageAcquisitionData();
}
