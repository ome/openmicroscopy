/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.view;



//Java imports
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.model.PlaneInfo;
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.imviewer.ContainerLoader;
import org.openmicroscopy.shoola.agents.imviewer.DataLoader;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.ImageDataLoader;
import org.openmicroscopy.shoola.agents.imviewer.PlaneInfoLoader;
import org.openmicroscopy.shoola.agents.imviewer.ProjectionSaver;
import org.openmicroscopy.shoola.agents.imviewer.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.imviewer.RenderingSettingsCreator;
import org.openmicroscopy.shoola.agents.imviewer.RenderingSettingsLoader;
import org.openmicroscopy.shoola.agents.imviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.imviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer;
import org.openmicroscopy.shoola.agents.imviewer.rnd.RendererFactory;
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;
import org.openmicroscopy.shoola.agents.imviewer.util.player.ChannelPlayer;
import org.openmicroscopy.shoola.agents.imviewer.util.player.Player;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionRef;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;

/** 
* The Model component in the <code>ImViewer</code> MVC triad.
* This class tracks the <code>ImViewer</code>'s state and knows how to
* initiate data retrievals. It also knows how to store and manipulate
* the results. This class provides a suitable data loader.
* The {@link ImViewerComponent} intercepts the results of data loadings, feeds
* them back to this class and fires state transitions as appropriate.
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
class ImViewerModel
{

	/** Flag to indicate that the image is not compressed. */
	static final int 			UNCOMPRESSED = RenderingControl.UNCOMPRESSED;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * medium Level of compression. 
	 */
	static final int 			MEDIUM = RenderingControl.MEDIUM;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * low Level of compression. 
	 */
	static final int 			LOW = RenderingControl.LOW;
	
	/** The maximum number of items in the history. */
	private static final int	MAX_HISTORY = 10;
	
	/** The maximum width of the thumbnail. */
	private static final int    THUMB_MAX_WIDTH = 24; 

	/** The maximum height of the thumbnail. */
	private static final int    THUMB_MAX_HEIGHT = 24;
	
	/** The maximum width of the image. */
	private static final int    IMAGE_MAX_WIDTH = 512;

	/** The maximum height of the image. */
	private static final int    IMAGE_MAX_HEIGHT = 512;

	/** Index of the <code>RenderingSettings</code> loader. */
	private static final int	SETTINGS = 0;
	
	/** Index of the <code>RenderingControlLoader</code> loader. */
	private static final int	RND = 1;
	
	/** The image to view. */
	private ImageData 					image; 

	/** Holds one of the state flags defined by {@link ImViewer}. */
	private int                 		state;

	/** Reference to the component that embeds this model. */
	private ImViewer            		component;

	/** Map hosting the various loaders. */
	private Map<Integer, DataLoader>	loaders;
	
	/** The sub-component that hosts the display. */
	private Browser             		browser;
	
	/** Reference to the {@link Renderer}. */
	private Renderer            		renderer;

	/** Reference to the current player. */
	private ChannelPlayer       		player;

	/** The width of the thumbnail if the window is iconified. */
	private int                 		sizeX;

	/** The height of the thumbnail if the window is iconified. */
	private int                 		sizeY;

	/** The magnification factor for the thumbnail. */
	private double              		factor;

	/** The image icon. */
	private BufferedImage       		imageIcon;

	/** The bounds of the component requesting the viewer. */
	private Rectangle           		requesterBounds;

	/** Fit the image to the size of window, on resize. */
	private boolean						zoomFitToWindow; 

	/** The index of the selected tabbed. */
	private int							tabbedIndex;

	/** 
	 * Flag indicating to paint or not some textual information on top
	 * of the grid image.
	 */
	private boolean						textVisible;

	/** Flag indicating that a movie is played. */
	private boolean						playingMovie;
	
	/** Flag indicating that a movie is played. */
	private boolean						playingChannelMovie;

	/** Collection of history item. */
	private List<HistoryItem>			historyItems;

	/** 
	 * The index of the movie, not that we set to <code>-1</code>
	 * when the movie player is launched.
	 */
	private int 						movieIndex;
	
	/** The rendering setting related to a given set of pixels. */
	private Map							renderingSettings;
	
	/** The metadata viewer. */
	private MetadataViewer				metadataViewer;
	
	/** Flag indicating if the metadata are loaded. */
	private boolean						metadataLoaded;
	
	/** The ID of the last selected pixels set. */
	private long						currentPixelsID;
	
	/** Reference to the rendering control. */
	private RenderingControl    		currentRndControl;

	/** The rendering settings set by another user. */
	private RndProxyDef					alternativeSettings;
	
	/** The id of the selected user. */
	private long						selectedUserID;
	
	/** 
	 * Flag to compute the magnification factor when the image
	 * is set for the first time.
	 */
	private boolean						initMagnificationFactor;
	
    /** The parent of the image or <code>null</code> if no context specified. */
    private DataObject					parent;
    
    /** 
     * The grandparent of the image or <code>null</code> if no 
     * context specified. 
     */
    private DataObject					grandParent;
    
    /** The plane information. */
    private Map<Integer, PlaneInfo>		planeInfos;
    
    /** The id of the image. */
    private long						imageID;
    
    /** Copy of the original rendering settings.  */
    private RndProxyDef					originalDef;
    
    /** Copy of the last rendering settings for the main image.  */
    private RndProxyDef					lastMainDef;
    
    /** Copy of the last rendering settings of the projection preview.  */
    private RndProxyDef					lastProjDef;
    
    /** The projection's preview parameters. */
    private ProjectionParam				lastProjRef;
    
    /** The last projection timepoint. */
    private int							lastProjTime;
    
    /** The collection of containers hosting the image. */
    private Collection 					containers;
    
    /** The collection of sorted channel data, sorted by emission wavelength. */
    private List<ChannelData>			sortedChannels;

    /**
	 * Transforms 3D coords into linear coords.
	 * The returned value <code>L</code> is calculated as follows: 
	 * <nobr><code>L = sizeZ*sizeW*t + sizeZ*w + z</code></nobr>.
	 * 
	 * @param z The z coord.  Must be in the range <code>[0, sizeZ)</code>.
	 * @param c The w coord.  Must be in the range <code>[0, sizeW)</code>.
	 * @param t The t coord.  Must be in the range <code>[0, sizeT)</code>.
	 * @return See above.
	 */
    private Integer linearize(int z, int c, int t)
    {
    	int sizeZ = currentRndControl.getPixelsDimensionsZ();
		int sizeC = currentRndControl.getPixelsDimensionsC();
		int sizeT = currentRndControl.getPixelsDimensionsT();
		if (z < 0 || sizeZ <= z) 
			throw new IllegalArgumentException(
					"z out of range [0, "+sizeZ+"): "+z+".");
		if (c < 0 || sizeC <= c) 
			throw new IllegalArgumentException(
					"w out of range [0, "+sizeC+"): "+c+".");
		if (t < 0 || sizeT <= t) 
			throw new IllegalArgumentException(
					"t out of range [0, "+sizeT+"): "+t+".");
		return Integer.valueOf(sizeZ*sizeC*t+sizeZ*c+z);
    }
    
	/** Computes the values of the {@link #sizeX} and {@link #sizeY} fields. */
	private void computeSizes()
	{
		if (sizeX == -1 && sizeY == -1) {
			sizeX = THUMB_MAX_WIDTH;
			sizeY = THUMB_MAX_HEIGHT;
			double x = sizeX/(double) getMaxX();
			double y =  sizeY/(double) getMaxY();
			if (x > y) factor = x;
			else factor = y;
			double ratio =  (double) getMaxX()/getMaxY();
			if (ratio < 1) sizeX *= ratio;
			else if (ratio > 1 && ratio != 0) sizeY *= 1/ratio;
		}
	}

	/** 
	 * Computes the magnification factor.
	 * 
	 * @return See above
	 */
	private double initZoomFactor()
	{
		if (initMagnificationFactor) return -1;
		int maxX = getMaxX();
		int maxY = getMaxY();
		initMagnificationFactor = true;
		if (maxX > IMAGE_MAX_WIDTH || maxY >IMAGE_MAX_HEIGHT) {
			double ratioX = (double) IMAGE_MAX_WIDTH/maxX;
			double ratioY = (double) IMAGE_MAX_HEIGHT/maxY;
			if (ratioX < ratioY) return ratioX;
			return ratioY;
		}
		return -1;
	}
	
	/** 
	 * Initializes the model.
	 * 
	 * @param bounds The bounds of the component invoking the {@link ImViewer}.
	 */
	private void initialize(Rectangle bounds)
	{
		requesterBounds = bounds;
		state = ImViewer.NEW;
		initMagnificationFactor = false;
		sizeX = sizeY = -1;
		zoomFitToWindow = false; 
		tabbedIndex = ImViewer.VIEW_INDEX;
		textVisible = true;
		movieIndex = -1;
		loaders = new HashMap<Integer, DataLoader>();
		metadataViewer = null;
		metadataLoaded = false;
		currentPixelsID = -1;
		selectedUserID = -1;
		lastProjTime = -1;
		lastProjRef = null;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param imageID 	The id of the image.
	 * @param bounds	The bounds of the component invoking the 
	 *                  {@link ImViewer}.
	 */
	ImViewerModel(long imageID, Rectangle bounds)
	{
		this.imageID = imageID;
		initialize(bounds);
	}
	
	/**
	 * Creates a new object and sets its state to {@link ImViewer#NEW}.
	 * 
	 * @param image  	The image.
	 * @param bounds    The bounds of the component invoking the 
	 *                  {@link ImViewer}.
	 */
	ImViewerModel(ImageData image, Rectangle bounds)
	{
		this.image = image;
		initialize(bounds);
		metadataViewer = MetadataViewerFactory.getViewer(image, false);
		currentPixelsID = image.getDefaultPixels().getId();
	}

	/**
	 * Called by the <code>ImViewer</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(ImViewer component)
	{ 
		this.component = component;
		browser = BrowserFactory.createBrowser(component, getImageID(), 
										ImViewerFactory.getPreferences());
	}
	
	/**
	 * Returns the current user's details.
	 * 
	 * @return See above.
	 */
	ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) ImViewerAgent.getRegistry().lookup(
				LookupNames.CURRENT_USER_DETAILS);
	}

	/**
	 * Sets the settings set by another user.
	 * 
	 * @param alternativeSettings The value to set.
	 * @param userID              The id of the user who set the settings.
	 */
	void setAlternativeSettings(RndProxyDef alternativeSettings, long userID)
	{
		this.alternativeSettings = alternativeSettings;
		selectedUserID = userID;
	}
	
	/**
	 * Returns the id of the owner of the alternatives settings.
	 * 
	 * @return See above.
	 */
	long getAlternativeSettingsOwnerId() { return selectedUserID; }

	/**
	 * Compares another model to this one to tell if they would result in
	 * having the same display.
	 *  
	 * @param other The other model to compare.
	 * @return <code>true</code> if <code>other</code> would lead to a viewer
	 *          with the same display as the one in which this model belongs;
	 *          <code>false</code> otherwise.
	 */
	boolean isSameDisplay(ImViewerModel other)
	{
		if (other == null) return false;
		return true;//;((other.pixelsID == pixelsID) && (other.imageID == imageID));
	}

	/**
	 * Returns the name of the image.
	 * 
	 * @return See above.
	 */
	String getImageName()
	{ 
		if (image == null) return "";
		return image.getName(); 
	}

	/** 
	 * Returns the name of image and id.
	 * 
	 * @return See above.
	 */
	String getImageTitle()
	{
		return "[ID: "+getImageID()+"] "+
				EditorUtil.getPartialName(getImageName());
	}
	
	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link ImViewer} interface.  
	 */
	int getState() { return state; }

	/**
	 * Sets the object in the {@link ImViewer#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		state = ImViewer.DISCARDED;
		if (image == null) return;
		//Shut down the service
		OmeroImageService svr = ImViewerAgent.getRegistry().getImageService();
		long pixelsID = image.getDefaultPixels().getId();
		svr.shutDown(pixelsID);
		svr.shutDownDataSink(pixelsID);
		Iterator i = loaders.keySet().iterator();
		Integer index;
		while (i.hasNext()) {
			index =  (Integer) i.next();
			(loaders.get(index)).cancel();
		}
		
		if (renderer != null) renderer.discard();
		if (player == null) return;
		player.setPlayerState(Player.STOP);
		player = null;
	}

	/**
	 * Returns the sizeX.
	 * 
	 * @return See above.
	 */
	int getMaxX() { return currentRndControl.getPixelsDimensionsX(); }

	/**
	 * Returns the sizeY.
	 * 
	 * @return See above.
	 */
	int getMaxY() { return currentRndControl.getPixelsDimensionsY(); }

	/**
	 * Returns the maximum number of z-sections.
	 * 
	 * @return See above.
	 */
	int getMaxZ() { return currentRndControl.getPixelsDimensionsZ()-1; }

	/**
	 * Returns the maximum number of timepoints.
	 * 
	 * @return See above.
	 */
	int getMaxT() { return currentRndControl.getPixelsDimensionsT()-1; }

	/**
	 * Returns the currently selected z-section.
	 * 
	 * @return See above.
	 */
	int getDefaultZ() { return currentRndControl.getDefaultZ(); }

	/**
	 * Returns the currently selected timepoint.
	 * 
	 * @return See above.
	 */
	int getDefaultT() { return currentRndControl.getDefaultT(); }

	/**
	 * Returns the currently selected color model.
	 * 
	 * @return See above.
	 */
	String getColorModel() { return currentRndControl.getModel(); }
	
	/**
	 * Returns an array of <code>ChannelData</code> objects.
	 * 
	 * @return See above.
	 */
	List<ChannelData> getChannelData()
	{ 
		if (sortedChannels == null) {
			ChannelData[] data = currentRndControl.getChannelData();
			ViewerSorter sorter = new ViewerSorter();
			List l = sorter.sort(data);
			sortedChannels = Collections.unmodifiableList(l);
		}
		return sortedChannels;
	}

	/**
	 * Returns the <code>ChannelData</code> object corresponding to the
	 * given index.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	ChannelData getChannelData(int index)
	{ 
		return currentRndControl.getChannelData(index);
	}

	/**
	 * Returns the color associated to a channel.
	 * 
	 * @param w The OME index of the channel.
	 * @return See above.
	 */
	Color getChannelColor(int w) { return currentRndControl.getRGBA(w); }

	/**
	 * Returns <code>true</code> if the channel is mapped, <code>false</code>
	 * otherwise.
	 * 
	 * @param w	The channel's index.
	 * @return See above.
	 */
	boolean isChannelActive(int w) { return currentRndControl.isActive(w); }
	
	/** 
	 * Fires an asynchronous retrieval of the rendering control. 
	 * 
	 * @param pixelsID The id of the pixels set to load.
	 */
	void fireRenderingControlLoading(long pixelsID)
	{
		currentPixelsID = pixelsID;
		DataLoader loader = new RenderingControlLoader(component, pixelsID, 
												RenderingControlLoader.LOAD);
		loader.load();
		if (loaders.get(RND) != null)
			loaders.get(RND).cancel();
		loaders.put(RND, loader);
		state = ImViewer.LOADING_RENDERING_CONTROL;
	}

	/** Fires an asynchronous retrieval of the rendering control. */
	void fireRenderingControlReloading()
	{
		DataLoader loader = new RenderingControlLoader(component, 
									image.getDefaultPixels().getId(), 
										RenderingControlLoader.RELOAD);
		loader.load();
		if (loaders.get(RND) != null)
			loaders.get(RND).cancel();
		loaders.put(RND, loader);
		state = ImViewer.LOADING_RENDERING_CONTROL;
	}

	/** Fires an asynchronous retrieval of the rendering control. */
	void fireRenderingControlResetting()
	{
		DataLoader loader = new RenderingControlLoader(component, 
										image.getDefaultPixels().getId(), 
										RenderingControlLoader.RESET);
		loader.load();
		if (loaders.get(RND) != null)
			loaders.get(RND).cancel();
		loaders.put(RND, loader);
		state = ImViewer.LOADING_RENDERING_CONTROL;
	}
	
	/**
	 * Starts an asynchronous call to retrieve the plane info related
	 * to the image. This method should only be invoked for fast connection.
	 */
	void firePlaneInfoRetrieval()
	{
		PlaneInfoLoader loader = new PlaneInfoLoader(component, getPixelsID());
		loader.load();
	}
	
	/** Fires an asynchronous retrieval of the rendered image. */
	void fireImageRetrieval()
	{
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		state = ImViewer.LOADING_IMAGE;
		//OmeroImageService os = ImViewerAgent.getRegistry().getImageService();
		try {
			//component.setImage(os.renderImage(pixelsID, pDef));
			component.setImage(currentRndControl.renderPlane(pDef));
		} catch (Exception e) {
			component.reload(e);
		}
	}

	/**
	 * This method should only be invoked when we save the displayed image
	 * and split its components.
	 * 
	 * @return See above.
	 */
	BufferedImage getSplitComponentImage()
	{
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		//state = ImViewer.LOADING_IMAGE;
		try {
			return currentRndControl.renderPlane(pDef);
		} catch (Exception e) {
			component.reload(e);
		}
		return null;
	}

	/**
	 * Sets the rendering control.
	 * 
	 * @param rndControl 	The object to set.
	 */
	void setRenderingControl(RenderingControl rndControl)
	{
		loaders.remove(RND);
		currentRndControl = rndControl;
		originalDef = currentRndControl.getRndSettingsCopy();
		if (renderer == null) {
			renderer = RendererFactory.createRenderer(component, rndControl, 
					metadataViewer.getEditorUI());
			state = ImViewer.RENDERING_CONTROL_LOADED;
			double f = initZoomFactor();
			if (f > 0)
				browser.initializeMagnificationFactor(f);
			try {
				if (alternativeSettings != null)
					currentRndControl.resetSettings(alternativeSettings);
				alternativeSettings = null;
			} catch (Exception e) {}
		} else {
			renderer.setRenderingControl(rndControl);
		}
	} 

	/**
	 * Returns the {@link Browser}.
	 * 
	 * @return See above.
	 */
	Browser getBrowser() { return browser; }

	/**
	 * Sets the zoom factor.
	 * 
	 * @param factor The factor to set.
	 * @param reset	 Pass <code>true</code> to reset the magnification factor.
     * 				 <code>false</code> to set it.
	 */
	void setZoomFactor(double factor, boolean reset)
	{ 
		browser.setZoomFactor(factor, reset);
	}

	/**
	 * Returns the zoom factor.
	 * 
	 * @return The factor to set.
	 */
	double getZoomFactor() { return browser.getZoomFactor(); }

	/**
	 * This method determines if the browser image should be resized to fit 
	 * the window size if the window is resized. 
	 * 
	 * @param option see above.
	 */
	void setZoomFitToWindow(boolean option) { zoomFitToWindow = option; }

	/**
	 * This method determines if the browser image should be resized to fit 
	 * the window size if the window is resized.
	 *  
	 * @return <code>true</code> if image should resize on window resize. 
	 */ 
	boolean getZoomFitToWindow() { return zoomFitToWindow; }

	/**
	 * Sets the retrieved image, returns the a magnification or <code>-1</code>
	 * if no magnification factor computed. 
	 * 
	 * @param image The image to set.
	 * @return See above.
	 */
	double setImage(BufferedImage image)
	{
		state = ImViewer.READY; 
		browser.setRenderedImage(image);
		//update image icon
		computeSizes();
		imageIcon = Factory.magnifyImage(factor, image);
		return initZoomFactor();
	}

	/**
	 * Returns <code>true</code> if the magnification factor was set 
	 * when the image was first loaded, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isInitMagnificationFactor() { return initMagnificationFactor; }
	
	/**
	 * Sets the color model.
	 * 
	 * @param colorModel	The color model to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setColorModel(String colorModel)
		throws RenderingServiceException, DSOutOfServiceException
	{
		//oldColorModel = colorModel;
		if (ImViewer.GREY_SCALE_MODEL.equals(colorModel))
			currentRndControl.setModel(colorModel);
		else if (ImViewer.RGB_MODEL.equals(colorModel))
			currentRndControl.setModel(ImViewer.RGB_MODEL);
	}

	/**
	 * Sets the selected plane.
	 * 
	 * @param z The z-section to set.
	 * @param t The timepoint to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setSelectedXYPlane(int z, int t)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (t >= 0 && t != getDefaultT()) currentRndControl.setDefaultT(t);
		if (z >= 0 && z != getDefaultZ()) currentRndControl.setDefaultZ(z);
	}

	/**
	 * Sets the color for the specified channel.
	 * 
	 * @param index The channel's index.
	 * @param c     The color to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setChannelColor(int index, Color c)
		throws RenderingServiceException, DSOutOfServiceException
	{
		currentRndControl.setRGBA(index, c);
	}

	/**
	 * Sets the channel active.
	 * 
	 * @param index The channel's index.
	 * @param b     Pass <code>true</code> to select the channel,
	 *              <code>false</code> otherwise.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setChannelActive(int index, boolean b)
		throws RenderingServiceException, DSOutOfServiceException
	{
		currentRndControl.setActive(index, b);
	}  

	/**
	 * Returns the number of channels.
	 * 
	 * @return See above.
	 */
	int getMaxC() { return currentRndControl.getPixelsDimensionsC(); }

	/** 
	 * Returns the number of active channels.
	 * 
	 * @return See above.
	 */
	int getActiveChannelsCount() { return getActiveChannels().size(); }

	/**
	 * Returns a list of active channels.
	 * 
	 * @return See above.
	 */
	List<Integer> getActiveChannels()
	{
		return currentRndControl.getActiveChannels();
	}

	/** 
	 * Starts the channels movie player, invokes in the event-dispatcher 
	 * thread for safety reason.
	 * 
	 * @param play  Pass <code>true</code> to play the movie, <code>false</code>
	 *              to stop it.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void playMovie(boolean play)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (player != null && !play) {
			player.setPlayerState(Player.STOP);
			List l = player.getChannels();
			if (l != null) {
				Iterator i = l.iterator();
				while (i.hasNext()) 
					setChannelActive(((Integer) i.next()).intValue(), true);
			}
			player = null;
			state = ImViewer.READY;
			playingChannelMovie = false;
			return;
		}
		playingChannelMovie = true;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				player = new ChannelPlayer(component);
				player.setPlayerState(Player.START);
			}
		});
		state = ImViewer.CHANNEL_MOVIE;
	}

	/**
	 * Returns <code>true</code> if playing movie across channels,
	 * <code>false</code> oherwise.
	 * 
	 * @return See above.
	 */
	boolean isPlayingChannelMovie() { return playingChannelMovie; }
	
	/**
	 * Sets the state.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; };
	
	/**
	 * Returns the {@link Renderer}.
	 * 
	 * @return See above.
	 */
	Renderer getRenderer() { return renderer; }

	/**
	 * Returns the displayed image.
	 * 
	 * @return See above.
	 */
	BufferedImage getDisplayedImage() { return browser.getDisplayedImage(); }

	/**
	 * Returns the original image returned by the image service.
	 * 
	 * @return See above.
	 */
	BufferedImage getOriginalImage() { return browser.getRenderedImage(); }
	
	/**
	 * Returns the projected image returned by the image service.
	 * 
	 * @return See above.
	 */
	BufferedImage getProjectedImage() { return browser.getProjectedImage(); }

	/**
	 * Returns the image displayed in the grid view.
	 * 
	 * @return See above.
	 */
	BufferedImage getGridImage() { return browser.getGridImage(); }

	/**
	 * Returns the size in microns of a pixel along the X-axis.
	 * 
	 * @return See above.
	 */
	float getPixelsSizeX()
	{ 
		return currentRndControl.getPixelsPhysicalSizeX(); 
	}

	/**
	 * Returns the size in microns of a pixel along the Y-axis.
	 * 
	 * @return See above.
	 */
	float getPixelsSizeY()
	{ 
		return currentRndControl.getPixelsPhysicalSizeY(); 
	}

	/**
	 * Returns the size in microns of a pixel along the Y-axis.
	 * 
	 * @return See above.
	 */
	float getPixelsSizeZ(){ return currentRndControl.getPixelsPhysicalSizeZ(); }

	/**
	 * Returns <code>true</code> if the unit bar is painted on top of 
	 * the displayed image, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isUnitBar() { return browser.isUnitBar(); }

	/**
	 * Returns an iconified version of the displayed image.
	 * 
	 * @return See above.
	 */
	BufferedImage getImageIcon() { return imageIcon; }

	/**
	 * Returns the bounds of the component invoking the {@link ImViewer},
	 * or <code>null</code> if not available.
	 * 
	 * @return See above.
	 */
	Rectangle getRequesterBounds() { return requesterBounds; }

	/**
	 * Returns the ID of the pixels set.
	 * 
	 * @return See above.
	 */
	long getPixelsID() { return currentPixelsID; }

	/**
	 * Returns the index of the selected tabbed.
	 * 
	 * @return See above.
	 */
	int getTabbedIndex() { return tabbedIndex; }

	/**
	 * Sets the tabbed index.
	 * 
	 * @param index The value to set.
	 */
	void setTabbedIndex(int index) { tabbedIndex = index; }

	/**
	 * Returns a 3-dimensional array of boolean value, one per color band.
	 * The first (resp. second, third) element is set to <code>true</code> 
	 * if an active channel is mapped to <code>RED</code> (resp. 
	 * <code>GREEN</code>, <code>BLUE</code>), to <code>false</code> otherwise.
	 * 
	 * @return See above
	 */
	boolean[] hasRGB()
	{
		boolean[] rgb = new boolean[3];
		rgb[0] = currentRndControl.hasActiveChannelRed();
		rgb[1] = currentRndControl.hasActiveChannelGreen();
		rgb[2] = currentRndControl.hasActiveChannelBlue();
		return rgb;
	}

	/**
	 * Returns <code>true</code> if the textual information is painted on 
	 * top of the grid image, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isTextVisible() { return textVisible; }

	/**
	 * Sets to <code>true</code> if the textual information is painted on 
	 * top of the grid image, <code>false</code> otherwise.
	 * 
	 * @param textVisible The value to set.
	 */
	void setTextVisible(boolean textVisible) { this.textVisible = textVisible; }

	/**
	 * Returns <code>true</code> if the image has been loaded,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImageLoaded() { return (image != null); }
	
	/**
	 * Returns the ID of the viewed image.
	 * 
	 * @return See above.
	 */
	long getImageID()
	{ 
		if (image == null) return imageID;
		return image.getId(); 
	}

	/** 
	 * Saves the rendering settings. 
	 * 
	 * @param reset Pass <code>true</code> to reset the original settings,
	 * 				<code>false</code> otherwise.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void saveRndSettings(boolean reset)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (currentRndControl != null) {
			RndProxyDef def = currentRndControl.saveCurrentSettings();
			/*
			if (reset) originalDef = def;
			if (def != null) {
				if (renderingSettings != null) 
					renderingSettings.put(ImViewerAgent.getUserDetails(), def);
			}
			*/
			if (reset) {
				originalDef = def;
				if (def != null) {
					if (renderingSettings != null) 
						renderingSettings.put(ImViewerAgent.getUserDetails(), def);
				}
			}
			
		}
	}

	/**
	 * Sets to <code>true</code> when the movie is played, to <code>false</code>
	 * otherwise.
	 * 
	 * @param play  The value to set.
	 * @param index The movie index.
	 */
	void setPlayingMovie(boolean play, int index)
	{ 
		playingMovie = play; 
		movieIndex = index;
	}

	/**
	 * Returns <code>true</code> if a movie is played, to <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isPlayingMovie() { return playingMovie; }

	/**
	 * Returns <code>true</code> if the channel is mapped
	 * to <code>RED</code>, <code>false</code> otherwise.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	boolean isChannelRed(int index)
	{
		return currentRndControl.isChannelRed(index);
	}

	/**
	 * Returns <code>true</code> if the channel is mapped
	 * to <code>GREEN</code>, <code>false</code> otherwise.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	boolean isChannelGreen(int index)
	{
		return currentRndControl.isChannelGreen(index);
	}

	/**
	 * Returns <code>true</code> if the channel is mapped
	 * to <code>BLUE</code>, <code>false</code> otherwise.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	boolean isChannelBlue(int index)
	{
		return currentRndControl.isChannelBlue(index);
	}

	/**
	 * Returns a collection of pairs 
	 * (active channel's index, active channel's color).
	 * 
	 * @return See above.
	 */
	Map<Integer, Color> getActiveChannelsColorMap()
	{
		List l = getActiveChannels();
		Map<Integer, Color> m = new HashMap<Integer, Color>(l.size());
		Iterator i = l.iterator();
		Integer index;
		while (i.hasNext()) {
			index = (Integer) i.next();
			m.put(index, getChannelColor(index.intValue()));
		}
		return m;
	}

	/** Sets the settings before turning on/off channels in the grid view. */
	void setLastSettingsRef()
	{
		if (getTabbedIndex() != ImViewer.GRID_INDEX) return;
		lastMainDef = currentRndControl.getRndSettingsCopy();
	}
	
	/** 
	 * Creates a new history item and adds it to the list of elements.
	 * Returns the newly created item.
	 * 
	 *  @param title The title of the history item.
	 *  @return See above.
	 */
	HistoryItem createHistoryItem()
	{
		String title = null;
		BufferedImage img = null;
		Color c = null;
		//Make a smaller image
		RndProxyDef def = currentRndControl.getRndSettingsCopy();
		switch (getTabbedIndex()) {
			case ImViewer.PROJECTION_INDEX:
				title = ImViewer.TITLE_PROJECTION_INDEX;
				img = browser.getProjectedImage();
				lastProjDef = def;
				c = Color.GREEN.brighter();
				break;
			case ImViewer.VIEW_INDEX:
				title = ImViewer.TITLE_VIEW_INDEX;
				img = browser.getRenderedImage();
				lastMainDef = def;
		}
		if (img == null) return null;
		double ratio = 1;
		int w = img.getWidth();
		int h = img.getHeight();
		if (w < ImViewer.MINIMUM_SIZE || h < ImViewer.MINIMUM_SIZE) ratio = 1;
		else {
			if (w >= h) ratio = (double) ImViewer.MINIMUM_SIZE/w;
			else ratio = (double) ImViewer.MINIMUM_SIZE/h;
		}
		BufferedImage thumb = Factory.magnifyImage(ratio, img);
		HistoryItem i = new HistoryItem(def, thumb, title);
		i.setHighlight(c);
		i.allowClose(false);
		i.setIndex(getTabbedIndex());
		if (historyItems == null) historyItems = new ArrayList<HistoryItem>();
		if (historyItems.size() == MAX_HISTORY)
			historyItems.remove(1); //always keep the first one
		historyItems.add(i);
		return i;
	}

	/**
	 * Returns the original rendering settings for the main image.
	 * 
	 * @return See above.
	 */
	RndProxyDef getOriginalDef() { return originalDef; }
	
	/**
	 * Returns the last rendering settings for the main image.
	 * 
	 * @return See above.
	 */
	RndProxyDef getLastMainDef() { return lastMainDef; }
	
	/**
	 * Returns the last rendering settings for the projection preview image.
	 * 
	 * @return See above.
	 */
	RndProxyDef getLastProjDef() { return lastProjDef; }
	
	/**
	 * Removes the item from the list.
	 * 
	 * @param node The node to remove.
	 */
	void removeHistoryItem(HistoryItem node)
	{
		if (historyItems != null) historyItems.remove(node);
	}
	
	/** Clears the history. */
	void clearHistory()
	{
		if (historyItems == null || historyItems.size() == 0) return;
		HistoryItem node = historyItems.get(0);
		historyItems.clear();
		historyItems.add(node);
		lastMainDef = node.getRndSettings();
		lastProjDef = null;
		lastProjTime = -1;
	}

	/**
	 * Returns the collection of history items.
	 * 
	 * @return See above.
	 */
	List<HistoryItem> getHistory() { return historyItems; }

	/**
	 * Partially resets the rendering settings.
	 * 
	 * @param settings  The value to set.
	 * @param reset		Pass <code>true</code> to reset the controls, 
	 * 					<code>false</code> otherwise.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void resetMappingSettings(RndProxyDef settings, boolean reset) 
		throws RenderingServiceException, DSOutOfServiceException
	{
		currentRndControl.resetSettings(settings);
		if (reset) renderer.resetRndSettings();
	}

	/**
	 * Sets the last rendering settings.
	 * 
	 * @param settings The settings to set.
	 */
	void setLastRndDef(RndProxyDef settings)
	{
		switch (getTabbedIndex()) {
			case ImViewer.PROJECTION_INDEX:
				lastProjDef = settings;
				break;
			case ImViewer.VIEW_INDEX:
				lastMainDef = settings;
		}
	}
	
	/** 
	 * Starts an asynchronous call to retrieve the rendering settings to paste. 
	 */
	void fireLoadRndSettingsToPaste()
	{
		long id = ImViewerFactory.getRefImage().getDefaultPixels().getId();
		if (id < 0) return;
		RenderingSettingsLoader loader = new RenderingSettingsLoader(component,
				id, true);
		loader.load();
		state = ImViewer.PASTING;
	}

	/** 
	 * Resets the default settings. 
	 * 
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void resetDefaultRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{ 
		currentRndControl.resetDefaults(); 
	}
	
	/** 
	 * Sets the original default settings. 
	 * 
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setOriginalRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{ 
		currentRndControl.setOriginalRndSettings(); 
	}
	
	/**
	 * Returns <code>true</code> if we have rendering settings to paste,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasRndToPaste() 
	{ 
		ImageData image = ImViewerFactory.getRefImage();
		if (image == null) return false;
		PixelsData pixels = image.getDefaultPixels();
		if (pixels == null) return false;
		if (currentRndControl == null) return false;
		return currentRndControl.validatePixels(pixels);
	}

	/** Posts a {@link CopyRndSettings} event. */
	void copyRenderingSettings()
	{
		CopyRndSettings evt = new CopyRndSettings(image);
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		bus.post(evt);
	}

	/**
	 * Returns the movie index.
	 * 
	 * @return See above.
	 */
	int getMovieIndex() { return movieIndex; }
	
	/**
	 * Returns <code>true</code> if the image is compressed, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImageCompressed() { return currentRndControl.isCompressed(); }
	
	/**
	 * Sets the compressiong level.
	 * 
	 * @param compressionLevel 	One of the compression level defined by 
	 * 							{@link RenderingControl} I/F.
	 */
	void setCompressionLevel(int compressionLevel)
	{
		currentRndControl.setCompression(compressionLevel);
	}

	/**
	 * Returns the compression level.
	 * 
	 * @return See above.
	 */
	int getCompressionLevel()
	{
		return currentRndControl.getCompressionLevel();
	}
	
	/**
	 * Fires an asynchronous retrieval of the rendering settings 
	 * linked to the currently viewed set of pixels.
	 */
	void fireRenderingSettingsRetrieval()
	{
		DataLoader loader = new RenderingSettingsLoader(component, 
						image.getDefaultPixels().getId());
		loader.load();
		if (loaders.get(SETTINGS) != null)
			loaders.get(SETTINGS).cancel();
		loaders.put(SETTINGS, loader);
	}
	
	/**
	 * Returns the rendering settings linked to the currently viewed set
	 * of pixels.
	 * 
	 * @return See above.
	 */
	Map getRenderingSettings() { return renderingSettings; }
	
	/** 
	 * Sets the rendering settings linked to the currently viewed set
	 * of pixels.
	 * 
	 * @param map The map to set.
	 */
	void setRenderingSettings(Map map)
	{
		renderingSettings = map;
		loaders.remove(SETTINGS);
	}

	/**
	 * Applies the settings set by the selected user.
	 * 
	 * @param exp	The user to handle.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setUserSettings(ExperimenterData exp)
		throws RenderingServiceException, DSOutOfServiceException
	{
		RndProxyDef rndDef = (RndProxyDef) renderingSettings.get(exp);
		currentRndControl.resetSettings(rndDef);
	}
	
	/**
	 * Returns the id of the owner of the image.
	 * 
	 * @return See above.
	 */
	long getOwnerID() { return image.getOwner().getId(); }

	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasMetadataToSave()
	{ 
		if (metadataViewer == null) return false;
		return metadataViewer.hasDataToSave();
	}

	/** Saves the data. */
	void saveMetadata()
	{ 
		if (metadataViewer != null)
			metadataViewer.saveData();
	}

	/** Loads the data. */
	void loadMetadata()
	{ 
		if (!metadataLoaded) {
			metadataLoaded = true;
			List<ChannelData> l = new ArrayList<ChannelData>();
			List<ChannelData> sorted = getChannelData();
			Iterator<ChannelData> i = sorted.iterator();
			while (i.hasNext()) {
				l.add(i.next());
			}
			metadataViewer.activate(l); 
		}
	}

	 /**
     * Returns the collection of pixels sets linked to the image.
     * 
     * @return See above.
     */
    List<PixelsData> getPixelsSets() { return image.getAllPixels(); }

    /**
     * Returns the collection of pixels ID.
     * 
     * @return See above.
     */
    List<Long> getPixelsIDs()
    {
    	Iterator<PixelsData> i = getPixelsSets().iterator();
    	List<Long> ids = new ArrayList<Long>();
    	while (i.hasNext()) 
			ids.add(i.next().getId());
		return ids;
    }

    /** Resets the history when switching to a new rendering control.*/
	void resetHistory() { historyItems = null; }
	
	/**
	 * Starts an asynchronous call to render a preview of the projected image.
	 * 
	 * @param startZ	The lower bound of the z-section interval to project.
	 * @param endZ		The upper bound of the z-section interval to project.
	 * @param stepping	The stepping used, usually <code>1</code>.
	 * @param type		The type of projection.
	 */
	void fireRenderProjected(int startZ, int endZ, int stepping, int type)
	{
		state = ImViewer.PROJECTION_PREVIEW;
		ProjectionParam param = new ProjectionParam(getPixelsID(), 
				startZ, endZ, stepping, type);
		param.setChannels(getActiveChannels());
		lastProjRef = param;
		ProjectionSaver loader = new ProjectionSaver(component, param, 
				                  ProjectionSaver.PREVIEW);
		loader.load();
	}
	
	/**
	 * Starts an asynchronous call to project image.
	 * 
	 * @param startZ	The lower bound of the z-section interval to project.
	 * @param endZ		The upper bound of the z-section interval to project.
	 * @param stepping	The stepping used, usually <code>1</code>.
	 * @param type		The type of projection.
	 * @param typeName	A textual representation of the projection's type.
	 * @param ref Object with the projection's parameters.
	 */
	void fireImageProjection(int startZ, int endZ, int stepping, int type, 
							String typeName, ProjectionRef ref)
	{
		startZ = ref.getStartZ();
		endZ = ref.getEndZ();
		state = ImViewer.PROJECTING;
		StringBuffer buf = new StringBuffer();
		buf.append("Original Image: "+getImageName());
		buf.append("\n");
		//buf.append("Original [Image: id "+getImageID()+"]");
		//buf.append("\n");
		buf.append("Projection type: "+typeName);
		buf.append("\n");
		buf.append("z-sections: "+(startZ+1)+"-"+(endZ+1));
		buf.append("\n");
		buf.append("timepoints: "+(ref.getStartT()+1)+"-"+(ref.getEndT()+1));
		List<Integer> channels = ref.getChannels();
		ProjectionParam param = new ProjectionParam(getPixelsID(), 
				startZ, endZ, stepping, type, ref.getStartT(), ref.getEndT(), 
				channels, ref.getImageName());
		param.setDescription(buf.toString());
		param.setDatasets(ref.getDatasets());
		param.setChannels(getActiveChannels());
		ProjectionSaver loader = new ProjectionSaver(component, param, 
							ProjectionSaver.PROJECTION, ref.isApplySettings());
		loader.load();
	}
	
	/**
	 * Starts an asynchronous retrieval of the containers containing the 
	 * image.
	 */
	void fireContainersLoading()
	{
		state = ImViewer.LOADING_PROJECTION_DATA;
		ContainerLoader loader = new ContainerLoader(component, getImageID());
		loader.load();
	}
    
	/**
	 * Returns the type of pixels.
	 * 
	 * @return See above.
	 */
	String getPixelsType() { return image.getDefaultPixels().getPixelType(); }

	/**
	 * Starts an asynchronous creation of the rendering settings
	 * for the pixels set.
	 * 
	 * @param indexes	The indexes of the projected channels.
	 * @param image 	The projected image.
	 */
	void fireProjectedRndSettingsCreation(List<Integer> indexes, ImageData image)
	{
		RndProxyDef def = currentRndControl.getRndSettingsCopy();
		RenderingSettingsCreator l = new RenderingSettingsCreator(component, 
				image, def, indexes);
		l.load();
	}
	
	/**
	 * Sets the context of the node.
	 * 
	 * @param parent		The parent of the image or <code>null</code> 
	 * 						if no context specified.
	 * @param grandParent   The grandparent of the image or <code>null</code> 
	 * 						if no context specified.
	 */
	void setContext(DataObject parent, DataObject grandParent)
	{
		this.parent = parent;
		this.grandParent = grandParent;
	}
	
	/**
     * Returns the parent of the image or <code>null</code> 
     * if no context specified.
     * 
     * @return See above.
     */
    DataObject getParent() { return parent; }
    
    /**
     * Returns the grandparent of the image or <code>null</code> 
     * if no context specified.
     * 
     * @return See above.
     */
    DataObject getGrandParent() { return grandParent; }
    
    /**
     * Sets the plane information.
     * 
     * @param objects The collection of <code>Plane Info</code> objects.
     */
    void setPlaneInfo(Collection objects)
    {
    	if (planeInfos == null) planeInfos = new HashMap<Integer, PlaneInfo>();
    	else planeInfos.clear();
    	Iterator i = objects.iterator();
    	PlaneInfo object;
    	Integer index;
    	while (i.hasNext()) {
			object = (PlaneInfo) i.next();
			if (object != null) {
				index = linearize(object.getTheZ().getValue(), 
					object.getTheC().getValue(), object.getTheT().getValue());
				planeInfos.put(index, object);
			}
		}
    }
    
    /**
     * Returns the <code>Plane Info</code> for the specified XY-plane.
     * 
     * @param z The z coord.  Must be in the range <code>[0, sizeZ)</code>.
	 * @param c The w coord.  Must be in the range <code>[0, sizeW)</code>.
	 * @param t The t coord.  Must be in the range <code>[0, sizeT)</code>.
     * @return See above.
     */
    PlaneInfo getPlane(int z, int c, int t)
    {
    	Integer index = null;
    	try {
			index = linearize(z, c, t);
			return planeInfos.get(index);
		} catch (Exception e) {}
    	return null;
    }

    /** Loads the image before doing anything else. */
	void fireImageLoading()
	{
		ImageDataLoader loader = new ImageDataLoader(component, getImageID());
		loader.load();
		state = ImViewer.LOADING_IMAGE_DATA;
	}
	
	/** 
	 * Sets the image data.
	 * 
	 * @param image The value to set.
	 */
	void setImageData(ImageData image)
	{
		this.image = image;
		metadataViewer = MetadataViewerFactory.getViewer(image, false);
		currentPixelsID = image.getDefaultPixels().getId();
	}
	
	/**
	 * Returns <code>true</code> if the rendering settings are original, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isOriginalSettings()
	{
		return isSameSettings(originalDef);
	}

	/**
	 * Returns <code>true</code> if the passed rendering settings are the same
	 * that the current one, <code>false</code> otherwise.
	 * 
	 * @param def The settings to check.
	 * @return See above.
	 */
	boolean isSameSettings(RndProxyDef def)
	{
		if (currentRndControl == null) return true;
		return currentRndControl.isSameSettings(def);
	}
    /**
     * Sets the projected image for preview.
     * 
     * @param image The buffered image.
     */
	void setRenderProjected(BufferedImage image)
	{
		state = ImViewer.READY; 
		browser.setRenderProjected(image);
	}

	/**
	 * Sets the collections of containers hosting the image.
	 * 
	 * @param containers The collection of containers hosting the image.
	 */
	void setContainers(Collection containers)
	{ 
		this.containers = containers; 
		state = ImViewer.READY;
	}
	
	/**
	 * Returns the collections of containers hosting the image.
	 * @return See above.
	 */
	Collection getContainers() { return containers; }
	
	/**
	 * Returns the pixels data.
	 * 
	 * @return See above.
	 */
	PixelsData getPixelsData()
	{ 
		if (image == null) return null;
		return image.getDefaultPixels();
	}
	
	/**
	 * Returns the last projection ref.
	 * 
	 * @return See above.
	 */
	ProjectionParam getLastProjRef() { return lastProjRef; }
	
	/**
	 * Sets the last projection ref.
	 * 
	 * @param ref The value to set.
	 */
	void setLastProjectionRef(ProjectionParam ref) { lastProjRef = ref; }
	
	/**
	 * Returns the timepoint used for the projection's preview.
	 * 
	 * @return See above.
	 */
	int getLastProjectionTime() { return lastProjTime; }
	
	/**
	 * Sets the timepoint used for a projection's preview.
	 * 
	 * @param time The value to set.
	 */
	void setLastProjectionTime(int time) { lastProjTime = time; }
	
}
