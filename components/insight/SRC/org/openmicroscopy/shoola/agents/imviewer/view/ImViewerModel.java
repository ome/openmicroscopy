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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Third-party libraries
import com.sun.opengl.util.texture.TextureData;

//Application-internal dependencies
import omero.model.PlaneInfo;
import omero.romio.PlaneDef;
import omero.romio.RegionDef;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.imviewer.BirdEyeLoader;
import org.openmicroscopy.shoola.agents.imviewer.ContainerLoader;
import org.openmicroscopy.shoola.agents.imviewer.DataLoader;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.ImageDataLoader;
import org.openmicroscopy.shoola.agents.imviewer.ImageLoader;
import org.openmicroscopy.shoola.agents.imviewer.MeasurementsLoader;
import org.openmicroscopy.shoola.agents.imviewer.OverlaysRenderer;
import org.openmicroscopy.shoola.agents.imviewer.PlaneInfoLoader;
import org.openmicroscopy.shoola.agents.imviewer.ProjectionSaver;
import org.openmicroscopy.shoola.agents.imviewer.RenderingSettingsCreator;
import org.openmicroscopy.shoola.agents.imviewer.RenderingSettingsLoader;
import org.openmicroscopy.shoola.agents.imviewer.TileLoader;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.imviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;
import org.openmicroscopy.shoola.agents.imviewer.util.player.ChannelPlayer;
import org.openmicroscopy.shoola.agents.imviewer.util.player.Player;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionRef;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.TableResult;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.rnd.data.Region;
import org.openmicroscopy.shoola.env.rnd.data.ResolutionLevel;
import org.openmicroscopy.shoola.env.rnd.data.Tile;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.WellData;
import pojos.WellSampleData;

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

	/** The maximum number of items in the history. */
	private static final int	MAX_HISTORY = 10;
	
	/** The maximum width of the thumbnail. */
	private static final int    THUMB_MAX_WIDTH = 24; 

	/** The maximum height of the thumbnail. */
	private static final int    THUMB_MAX_HEIGHT = 24;
	
	/** Index of the <code>RenderingSettings</code> loader. */
	private static final int	SETTINGS = 0;
	
	/** Index of the <code>RenderingControlLoader</code> loader. */
	private static final int	RND = 1;
	
	/** Index of the <code>ImageLoader</code> loader. */
	private static final int	IMAGE = 2;
	
	/** Index of the <code>ImageLoader</code> loader. */
	private static final int	BIRD_EYE_BVIEW = 3;
	
	/** The image to view. */
	private DataObject 					image; 

	/** Holds one of the state flags defined by {@link ImViewer}. */
	private int                 		state;

	/** Reference to the component that embeds this model. */
	private ImViewer            		component;

	/** Map hosting the various loaders. */
	private Map<Integer, DataLoader>	loaders;
	
	/** The sub-component that hosts the display. */
	private Browser             		browser;

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

	/** The index of the selected tab. */
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
    
    /** 
     * The collection of measurements linked to either the image or the plate.
     */
    private Collection 					measurements;
    
    /**  
     * Flag indicating if the viewer should be opened as a separate window
     * or not. The default value is <code>true</code>.
     */
    private boolean						separateWindow;
    
    /** The id of the table containing the overlay. */
    private long						overlayTableID;

    /** 
     * The value indicating the reduction factor used for big images. 
     * The default value is <code>1</code>.
     */
    private double						originalRatio;
    
    /** The size of the object if it is a big image. */
    private Dimension					computedSize;
    
    /** The tiles to display. */
    private Map<Integer, Tile>			tiles;
    
    /** The number of rows, default is <code>1</code>.*/
    private int numberOfRows;
    
    /** The number of columns, default is <code>1</code>.*/
    private int numberOfColumns;
    
	/** The size of the tile.*/
	private Dimension tileSize;
	
	/** The power of 2 used to determine the tile size.*/
	private Map<Integer, ResolutionLevel> resolutionMap;
	
	/** The size of the tiled image along the X-axis.*/
	private int tiledImageSizeX;
	
	/** The size of the tiled image along the Y-axis.*/
	private int tiledImageSizeY;
	
	/** Flag indicating that the image is loaded for the first time.*/
	private boolean firstTime;
	
	/**
	 * Creates the plane to retrieve.
	 * 
	 * @return See above.
	 */
	private PlaneDef createPlaneDef()
	{
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		return pDef;
	}
	
	/** Initializes the tiles objects.*/
	private void initializeTiles()
	{
		Dimension d = getTileSize();
		int w = d.width;
		int h = d.height;
		int edgeWidth = w;
		int edgeHeight = h;
		ResolutionLevel rl = resolutionMap.get(getSelectedResolutionLevel());
		int px = rl.getPowerAlongX();
		int py = rl.getPowerAlongY();
		rl = resolutionMap.get(getResolutionLevels()-1);
		int mx = rl.getPowerAlongX();
		int my = rl.getPowerAlongY();
		int size = (int) (getMaxX()/Math.pow(2, mx-px));
		edgeWidth = w;
		int n = size/w;
		tiledImageSizeX = n*w;
		if (n*w < size) {
			edgeWidth = size-n*w;
			tiledImageSizeX += edgeWidth;
			n++;
		}
		numberOfColumns = n;
		size = (int) (getMaxY()/Math.pow(2, my-py));
		edgeHeight = h;
		n = size/h;
		tiledImageSizeY = n*h;
		if (n*h < size) {
			edgeHeight = size-n*h;
			tiledImageSizeY += edgeHeight;
			n++;
		}
		numberOfRows = n;
		int index = 0;
		Tile tile;
		Region region;
		int x = 0;
		int y = 0;
		int ww;
		int hh;
		if (numberOfColumns <= 0) numberOfColumns = 1;
		if (numberOfColumns <= 0) numberOfColumns = 1;
		for (int i = 0; i < numberOfRows; i++) {
			if (i == (numberOfRows-1)) hh = edgeHeight;
			else hh = h;
			for (int j = 0; j < numberOfColumns; j++) {
				if (j == (numberOfColumns-1)) ww = edgeWidth;
				else ww = w;
				index = i*numberOfColumns+j;
				tile = new Tile(index, i, j);
				region = new Region(x, y, ww, hh);
				tile.setRegion(region);
				x += d.width;
				tiles.put(index, tile);
			}
			y += d.height;
			x = 0;
		}
	}

    /**
     * Sorts the tiles by index.
     * 
     * @param tiles The tiles to sort.
     */
    private void sortTilesByIndex(List<Tile> tiles)
    {
    	 if (tiles == null || tiles.size() == 0) return;
         Comparator c = new Comparator() {
             public int compare(Object o1, Object o2)
             {
                 int n1 = ((Tile) o1).getIndex(), n2 = ((Tile) o2).getIndex();
                 int v = 0;
                 if (n1 < n2) v = -1;
                 else if (n1 > n2) v = 1;
                 return v;
             }
         };
         Collections.sort(tiles, c);
    }
    
    /**
	 * Transforms 3D coordinates into linear coordinates.
	 * The returned value <code>L</code> is calculated as follows: 
	 * <code>L = sizeZ*sizeC*t + sizeZ*c + z</code>.
	 * 
	 * @param z The z coordinate.  Must be in the range <code>[0, sizeZ)</code>.
	 * @param c The c coordinate.  Must be in the range <code>[0, sizeC)</code>.
	 * @param t The t coordinate.  Must be in the range <code>[0, sizeT)</code>.
	 * @return See above.
	 */
    private Integer linearize(int z, int c, int t)
    {
    	Renderer rnd = metadataViewer.getRenderer();
    	if (rnd == null) return -1;
    	int sizeZ = rnd.getPixelsDimensionsZ();
		int sizeC = rnd.getPixelsDimensionsC();
		int sizeT = rnd.getPixelsDimensionsT();
		if (z < 0 || sizeZ <= z) return -1;
		if (c < 0 || sizeC <= c) return -1;
		if (t < 0 || sizeT <= t) return -1;
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
		/* 28/02 
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
		*/
		return -1;
	}
	
	/** 
	 * Initializes the model.
	 * 
	 * @param bounds The bounds of the component invoking the {@link ImViewer}.
	 * @param separateWindow Pass <code>true</code> to open the viewer in a 
	 * 						 separate window, <code>false</code> otherwise.  
	 */
	private void initialize(Rectangle bounds, boolean separateWindow)
	{
		firstTime = true;
		this.separateWindow = separateWindow;
		tiles = new HashMap<Integer, Tile>();
		originalRatio = 1;
		overlayTableID = -1;
		requesterBounds = bounds;
		state = ImViewer.NEW;
		initMagnificationFactor = false;
		sizeX = sizeY = -1;
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
	
	/** Initializes the {@link #metadataViewer}. */
	private void initializeMetadataViewer()
	{
		metadataViewer = MetadataViewerFactory.getViewer("", 
				MetadataViewer.RND_SPECIFIC);
		metadataViewer.setRootObject(image, metadataViewer.getUserID());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param imageID 	The id of the image.
	 * @param bounds	The bounds of the component invoking the 
	 *                  {@link ImViewer}.
	 * @param separateWindow Pass <code>true</code> to open the viewer in a 
	 * 						 separate window, <code>false</code> otherwise.  
	 */
	ImViewerModel(long imageID, Rectangle bounds, boolean separateWindow)
	{
		this.imageID = imageID;
		initialize(bounds, separateWindow);
	}
	
	/**
	 * Returns the image to view.
	 * 
	 * @return See above.
	 */
	ImageData getImage()
	{
		if (image instanceof WellSampleData)
			return ((WellSampleData) image).getImage();
		return (ImageData) image;
	}
	
	/**
	 * Creates a new object and sets its state to {@link ImViewer#NEW}.
	 * 
	 * @param image  	The image or well sample to view.
	 * @param bounds    The bounds of the component invoking the 
	 *                  {@link ImViewer}.
	 * @param separateWindow Pass <code>true</code> to open the viewer in a 
	 * 						 separate window, <code>false</code> otherwise.  
	 */
	ImViewerModel(DataObject image, Rectangle bounds, boolean separateWindow)
	{
		this.image = image;
		initialize(bounds, separateWindow);
		numberOfRows = 1;
		numberOfColumns = 1;
		//initializeMetadataViewer();
		if (getImage().getDefaultPixels() != null) {
			currentPixelsID = getImage().getDefaultPixels().getId();
		}
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
		browser = BrowserFactory.createBrowser(component, 
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
	 * Returns <code>true</code> if it is the same parent, <code>false</code>
	 * otherwise.
	 * 
	 * @param data The data to handle.
	 * @return See above
	 */
	boolean isSameParent(DataObject data)
	{
		if (data != null && parent != null) {
			if (parent instanceof WellData) {
				if (grandParent != null) {
					if (data.getClass().getName().equals(
							grandParent.getClass().getName()))
						return data.getId() == grandParent.getId();
				}
			} else {
				if (data.getClass().getName().equals(
						parent.getClass().getName()))
					return data.getId() == parent.getId();
			}
			
		}
		return false;
	}
	
	/**
	 * Returns the name of the image.
	 * 
	 * @return See above.
	 */
	String getImageName()
	{ 
		if (image == null) return "";
		return getImage().getName(); 
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

	/** Cancels the bird eye view loading.*/
	void cancelBirdEyeView()
	{
		state = ImViewer.CANCELLED;
		DataLoader loader = loaders.get(BIRD_EYE_BVIEW);
		if (loader != null) {
			loader.cancel();
			loaders.remove(BIRD_EYE_BVIEW);
		}
		discard();
	}
	
	/**
	 * Sets the object in the {@link ImViewer#DISCARDED} state.
	 * Any ongoing data loading will be canceled.
	 */
	void discard()
	{
		state = ImViewer.DISCARDED;
		if (imageIcon != null) imageIcon.flush();
		//
		Iterator i = loaders.keySet().iterator();
		Integer index;
		while (i.hasNext()) {
			index =  (Integer) i.next();
			(loaders.get(index)).cancel();
		}
		browser.discard();
		if (metadataViewer != null && metadataViewer.getRenderer() != null) {
			metadataViewer.getRenderer().discard();
		}
			
		//if (image == null) return;
		//Shut down the service
		//OmeroImageService svr = ImViewerAgent.getRegistry().getImageService();
		//long pixelsID = getImage().getDefaultPixels().getId();
		//svr.shutDown(pixelsID);
		
		if (player == null) return;
		player.setPlayerState(Player.STOP);
		player = null;
	}

	/**
	 * Returns the sizeX.
	 * 
	 * @return See above.
	 */
	int getMaxX()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return 0;
		return rnd.getPixelsDimensionsX();
	}

	/**
	 * Returns the sizeY.
	 * 
	 * @return See above.
	 */
	int getMaxY()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return 0;
		return rnd.getPixelsDimensionsY(); 
	}

	/**
	 * Returns the maximum number of z-sections.
	 * 
	 * @return See above.
	 */
	int getMaxZ()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return 0;
		return rnd.getPixelsDimensionsZ()-1; 
	}

	/**
	 * Returns the maximum number of timepoints.
	 * 
	 * @return See above.
	 */
	int getMaxT()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return 0;
		return rnd.getPixelsDimensionsT()-1;
	}

	/**
	 * Returns the currently selected z-section.
	 * 
	 * @return See above.
	 */
	int getDefaultZ()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return 0;
		return rnd.getDefaultZ();
	}

	/**
	 * Returns the currently selected timepoint.
	 * 
	 * @return See above.
	 */
	int getDefaultT()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return 0;
		return rnd.getDefaultT(); 
	}

	/**
	 * Returns the currently selected color model.
	 * 
	 * @return See above.
	 */
	String getColorModel()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return Renderer.GREY_SCALE_MODEL;
		return rnd.getColorModel();
	}
	
	/**
	 * Returns a sorted list of <code>ChannelData</code> objects.
	 * 
	 * @return See above.
	 */
	List<ChannelData> getChannelData()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return new ArrayList<ChannelData>();
		return rnd.getChannelData();
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
		List<ChannelData> list = getChannelData();
		Iterator<ChannelData> i = list.iterator();
		ChannelData channel;
		while (i.hasNext()) {
			channel = i.next();
			if (channel.getIndex() == index) return channel;
		}
		return null;
	}

	/**
	 * Returns the color associated to a channel.
	 * 
	 * @param w The index of the channel.
	 * @return See above.
	 */
	Color getChannelColor(int w)
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return null;
		return rnd.getChannelColor(w);
	}

	/**
	 * Returns <code>true</code> if the channel is mapped, <code>false</code>
	 * otherwise.
	 * 
	 * @param w	The channel's index.
	 * @return See above.
	 */
	boolean isChannelActive(int w)
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return false;
		return rnd.isChannelActive(w);
	}
	
	/**
	 * Starts an asynchronous call to retrieve the plane info related
	 * to the image. This method should only be invoked for fast connection.
	 */
	void firePlaneInfoRetrieval()
	{
		if (planeInfos != null && planeInfos.size() > 0) return;
		int size = getMaxT()*getMaxC()*getMaxZ();
		if (size >  OmeroImageService.MAX_PLANE_INFO) return;
		PlaneInfoLoader loader = new PlaneInfoLoader(component, getPixelsID());
		loader.load();
	}

	/** Fires an asynchronous retrieval of the rendered image. */
	void fireImageRetrieval()
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		PlaneDef pDef = createPlaneDef();
		state = ImViewer.LOADING_IMAGE;
		if (firstTime) {
			browser.setUnitBar(true);
			long pixelsID = getImage().getDefaultPixels().getId();
			ImageLoader loader = new ImageLoader(component, pixelsID, pDef, 
					false);
			loader.load();
			loaders.put(IMAGE, loader);
		} else {
			if (ImViewerAgent.hasOpenGLSupport()) 
				component.setImage(rnd.renderPlaneAsTexture(pDef));
			else component.setImage(rnd.renderPlane(pDef));
		}
	}

	/**
	 * Returns <code>true</code> if the image is rendered for the first time,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above
	 */
	boolean isFirstTime() { return firstTime; }
	
	/**
	 * This method should only be invoked when we save the displayed image
	 * and split its components.
	 * 
	 * @return See above.
	 */
	BufferedImage getSplitComponentImage()
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return null;
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		return rnd.renderPlane(pDef);
	}

	/**
	 * This method should only be invoked when we save the displayed image
	 * and split its components.
	 * 
	 * @return See above.
	 */
	TextureData getSplitComponentImageAsTexture()
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return null;
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		return rnd.renderPlaneAsTexture(pDef);
	}
	
	/** Notifies that the rendering control has been loaded. */
	void onRndLoaded()
	{
		state = ImViewer.READY;
		Renderer rnd = metadataViewer.getRenderer();
		resolutionMap = new HashMap<Integer, ResolutionLevel>();
		if (rnd != null) {
			tileSize = rnd.getTileSize();
			if (tileSize != null) {
				int levels = getResolutionLevels();
				int powerX = (int) (Math.log(tileSize.width)/Math.log(2));
				int powerY = (int) (Math.log(tileSize.height)/Math.log(2));
				int index = 0;
				int vx = 0, vy = 0;
				for (int i = levels-1; i >= 0; i--) {
					vx = powerX-index;
					vy = powerY-index;
					resolutionMap.put(i, new ResolutionLevel(i, vx, vy));
					index++;
				}
			}
		}
		if (isBigImage())
			initializeTiles();
		//
		double f = initZoomFactor();
		if (f > 0)
			browser.initializeMagnificationFactor(f);
		try {
			if (alternativeSettings != null && rnd != null)
				rnd.resetSettings(alternativeSettings, false);
			alternativeSettings = null;
			if (rnd != null) originalDef = rnd.getRndSettingsCopy();
		} catch (Exception e) {}
	}
	
	/**
	 * Returns the {@link Browser}.
	 * 
	 * @return See above.
	 */
	Browser getBrowser() { return browser; }

	/**
	 * Returns the {@link MetadataViewer}.
	 * 
	 * @return See above.
	 */
	MetadataViewer getMetadataViewer() { return metadataViewer; }
	
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
	 * @return <code>true</code> if image should resize on window resize. 
	 */ 
	boolean isZoomFitToWindow()
	{ 
		return getZoomFactor() == ZoomAction.ZOOM_FIT_FACTOR; 
	}

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
		loaders.remove(IMAGE);
		firstTime = false;
		//update image icon
		//28/02 added to speed up process, turn back on for 4.1
		/*
		if (imageIcon == null) {
			computeSizes();
			imageIcon = Factory.magnifyImage(factor, image);
		}
		*/
		return initZoomFactor();
	}

	/** Creates the image icon. */
	void createImageIcon()
	{
		BufferedImage img = browser.getRenderedImage();
		if (img != null) {
			computeSizes();
			imageIcon = Factory.magnifyImage(factor, img);
		}
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
	 * @param update		Flag indicating to fire a property change 
	 * 						indicating to update the image.
	 * @throws RenderingServiceException 	If an error occurred while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setColorModel(String colorModel, boolean update)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		if (ImViewer.GREY_SCALE_MODEL.equals(colorModel)) {
			rnd.setOverlays(overlayTableID, null);
			rnd.setColorModel(colorModel, update);
		} else if (ImViewer.RGB_MODEL.equals(colorModel))
			rnd.setColorModel(ImViewer.RGB_MODEL, update);
	}

	/**
	 * Sets the selected plane.
	 * 
	 * @param z The z-section to set.
	 * @param t The timepoint to set.
	 */
	void setSelectedXYPlane(int z, int t)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		rnd.setSelectedXYPlane(z, t, -1);
	}

	/**
	 * Sets the color for the specified channel.
	 * 
	 * @param index The channel's index.
	 * @param c     The color to set.
	 */
	void setChannelColor(int index, Color c)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		rnd.setChannelColor(index, c, false);
	}

	/**
	 * Sets the channel active.
	 * 
	 * @param index  The channel's index.
	 * @param active Pass <code>true</code> to select the channel,
	 *               <code>false</code> otherwise.
	 */
	void setChannelActive(int index, boolean active)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		rnd.setActive(index, active);
	}  

	/**
	 * Returns the number of bins per time interval
	 * 
	 * @return See above
	 */
	int getMaxLifetimeBin()
	{
		if (isNumerousChannel()) return getMaxC();
		return 0;
	}
	
	/**
	 * Returns <code>true</code> if the image is a lifetime image,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isNumerousChannel()
	{ 
		if (getMaxC() >= Renderer.MAX_CHANNELS) return true;
		return getImage().isLifetime(); 
	}
	
	/**
	 * Returns <code>true</code> if the split view is allowed i.e. if the 
	 * image is not too big, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean allowSplitView()
	{
		if (isBigImage()) return false;
		if (getMaxC() <= 1) return false;
		if (isNumerousChannel()) return false;
		//if (getMaxX() >= 2*IMAGE_MAX_WIDTH) return false;
		//if (getMaxY() >= 2*IMAGE_MAX_HEIGHT) return false;
		return true;
	}
	
	/**
	 * Returns <code>true</code> if it is a large image, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isBigImage()
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) {
			try {
				Boolean 
				b = ImViewerAgent.getRegistry().getImageService().isLargeImage(
						getImage().getDefaultPixels().getId());
				if (b != null) return b.booleanValue();
			} catch (Exception e) {} //ingore
			
			return false;
		}
		return rnd.isBigImage();
	}
	
	/**
	 * Returns the number of channels.
	 * 
	 * @return See above.
	 */
	int getMaxC()
	{ 
		return getImage().getDefaultPixels().getSizeC();
	}

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
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return new ArrayList<Integer>();
		return rnd.getActiveChannels();
	}

	/** 
	 * Starts the channels movie player, invokes in the event-dispatcher 
	 * thread for safety reason.
	 * 
	 * @param play  Pass <code>true</code> to play the movie, <code>false</code>
	 *              to stop it.
	 * @throws RenderingServiceException 	If an error occurred while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void playMovie(boolean play)
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
			if (state != ImViewer.LOADING_IMAGE) 
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
	 * <code>false</code> otherwise.
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
	 * Returns the main image as a texture data.
	 * 
	 * @return See above.
	 */
	TextureData getImageAsTexture() { return browser.getImageAsTexture(); }
	
	/**
	 * Returns the projected image as a texture data.
	 * 
	 * @return See above.
	 */
	TextureData getProjectedImageAsTexture()
	{ 
		return browser.getProjectedImageAsTexture(); 
	}
	
	/**
	 * Returns the size in microns of a pixel along the X-axis.
	 * 
	 * @return See above.
	 */
	double getPixelsSizeX()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return -1;
		return rnd.getPixelsSizeX(); 
	}

	/**
	 * Returns the size in microns of a pixel along the Y-axis.
	 * 
	 * @return See above.
	 */
	double getPixelsSizeY()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return -1;
		return rnd.getPixelsSizeY();  
	}

	/**
	 * Returns the size in microns of a pixel along the Y-axis.
	 * 
	 * @return See above.
	 */
	double getPixelsSizeZ()
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return -1;
		return rnd.getPixelsSizeZ(); 
	}

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
	 * Returns the index of the selected tab.
	 * 
	 * @return See above.
	 */
	int getTabbedIndex() { return tabbedIndex; }

	/**
	 * Sets the tab index.
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
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return null;
		return rnd.hasRGB();
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
		return getImage().getId(); 
	}

	/** 
	 * Saves the rendering settings. 
	 * 
	 * @param reset Pass <code>true</code> to reset the original settings,
	 * 				<code>false</code> otherwise.
	 * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken. 
	 */
	void saveRndSettings(boolean reset)
		throws RenderingServiceException, DSOutOfServiceException
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		RndProxyDef def = rnd.saveCurrentSettings();
		if (reset) {
			originalDef = def;
			if (def != null && renderingSettings != null) {
				renderingSettings.put(ImViewerAgent.getUserDetails(), def);
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
	 * to <code>Red</code> if the band is <code>0</code>, 
	 * to <code>Green</code> if the band is <code>1</code>,
	 * to <code>Blue</code> if the band is <code>2</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @param band  The color band.
	 * @param index The index of the channel.
	 * @return See above.
	 */
	boolean isColorComponent(int band, int index)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return false;
		return rnd.isColorComponent(band, index);
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

	/**
	 * Returns a collection of pairs (channel's index, channel's color).
	 * 
	 * @return See above.
	 */
	Map<Integer, Color> getChannelsColorMap()
	{
		Map<Integer, Color> m = new HashMap<Integer, Color>(getMaxC());
		for (int i = 0; i < getMaxC(); i++) 
			m.put(i, getChannelColor(i));
		return m;
	}
	
	/** 
	 * Sets the settings before turning on/off channels in the grid view. 
	 * 
	 * @param index The index specified.
	 */
	void setLastSettingsRef(int index)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		switch (index) {
			case ImViewer.GRID_INDEX:
			case ImViewer.PROJECTION_INDEX:
				lastMainDef = rnd.getRndSettingsCopy();
				break;
			case ImViewer.VIEW_INDEX:
				//lastProjDef = rnd.getRndSettingsCopy();	
		}
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
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return null;
		String title = null;
		BufferedImage img = null;
		Color c = null;
		//Make a smaller image
		RndProxyDef def = rnd.getRndSettingsCopy();
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
	 */
	void resetMappingSettings(RndProxyDef settings) 
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		rnd.resetSettings(settings, false);
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
				//lastProjDef = settings;
				break;
			case ImViewer.VIEW_INDEX:
				lastMainDef = settings;
		}
	}
	
	/**
	 * Sets the original settings, the method should only be invoked when
	 * keeping track of the value after a save from the preview.
	 * 
	 * @param settings The settings to set.
	 */
	void resetOriginalSettings(RndProxyDef settings)
	{
		originalDef = settings;
		if (settings != null && renderingSettings != null) {
			renderingSettings.put(ImViewerAgent.getUserDetails(), settings);
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

	/** Resets the default settings. */
	void resetDefaultRndSettings()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		rnd.resetSettings(); 
	}
	
	/** Sets the original default settings. */
	void setOriginalRndSettings()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		rnd.setOriginalRndSettings(); 
	}
	
	/**
	 * Returns <code>true</code> if we have rendering settings to paste,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasRndToPaste() 
	{ 
		if (metadataViewer == null) return false;
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return false;
		ImageData image = ImViewerFactory.getRefImage();
		if (image == null) return false;
		PixelsData pixels = image.getDefaultPixels();
		if (pixels == null) return false;
		return rnd.validatePixels(pixels);
	}

	/** Posts a {@link CopyRndSettings} event. */
	void copyRenderingSettings()
	{
		CopyRndSettings evt = new CopyRndSettings(getImage());
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
	boolean isImageCompressed()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return false;
		return rnd.isCompressed(); 
	}
	
	/**
	 * Sets the compression level.
	 * 
	 * @param compressionLevel 	One of the compression level defined by 
	 * 							{@link RenderingControl} I/F.
	 */
	void setCompressionLevel(int compressionLevel)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		rnd.setCompression(compressionLevel);
	}

	/**
	 * Returns the compression level.
	 * 
	 * @return See above.
	 */
	int getCompressionLevel()
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return RenderingControl.UNCOMPRESSED;
		return rnd.getCompressionLevel();
	}
	
	/**
	 * Fires an asynchronous retrieval of the rendering settings 
	 * linked to the currently viewed set of pixels.
	 */
	void fireRenderingSettingsRetrieval()
	{
		DataLoader loader = new RenderingSettingsLoader(component, 
						getImage().getDefaultPixels().getId());
		loader.load();
		if (loaders.get(SETTINGS) != null)
			loaders.get(SETTINGS).cancel();
		loaders.put(SETTINGS, loader);
	}
	
	/**
	 * Fires an asynchronous retrieval of the rendering settings 
	 * linked to the currently viewed set of pixels.
	 */
	void fireOwnerSettingsRetrieval()
	{
		RenderingSettingsLoader loader = new RenderingSettingsLoader(component, 
				getImage().getDefaultPixels().getId());
		loader.setOwner(getOwnerID());
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
	 */
	void setUserSettings(ExperimenterData exp)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		RndProxyDef rndDef = (RndProxyDef) renderingSettings.get(exp);
		rnd.resetSettings(rndDef, false);
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
			Map<ChannelData, Color> m = new LinkedHashMap<ChannelData, Color>();
			List<ChannelData> sorted = getChannelData();
			Iterator<ChannelData> i = sorted.iterator();
			ChannelData channel;
			while (i.hasNext()) {
				channel = i.next();
				m.put(channel, getChannelColor(channel.getIndex()));
			}
			metadataViewer.activate(m); 
		}
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
		lastProjDef = metadataViewer.getRenderer().getRndSettingsCopy();
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
		if (startZ < 0) startZ = ref.getStartZ();
		if (endZ < startZ) endZ = ref.getEndZ();
		state = ImViewer.PROJECTING;
		StringBuffer buf = new StringBuffer();
		buf.append("Original Image: "+getImageName());
		buf.append("\n");
		buf.append("Original Image ID: "+getImageID());
		buf.append("\n");
		buf.append("Projection type: "+typeName);
		buf.append("\n");
		buf.append("z-sections: "+(startZ+1)+"-"+(endZ+1));
		buf.append("\n");
		String range = "ZRange_"+(startZ+1)+"_"+(endZ+1)+"_";
		int startT = ref.getStartT();
		int endT = ref.getEndT();
		if (startT == endT) buf.append("timepoint: "+(startT+1));
		else buf.append("timepoints: "+(startT+1)+"-"+(endT+1));
		List<Integer> channels = ref.getChannels();
		ProjectionParam param = new ProjectionParam(getPixelsID(), 
				startZ, endZ, stepping, type, startT, endT, channels, 
				range+ref.getImageName());
		param.setDescription(buf.toString());
		param.setDatasets(ref.getDatasets());
		param.setDatasetParent(ref.getProject());
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
	String getPixelsType()
	{ 
		return getImage().getDefaultPixels().getPixelType();
	}

	/**
	 * Starts an asynchronous creation of the rendering settings
	 * for the pixels set.
	 * 
	 * @param indexes	The indexes of the projected channels.
	 * @param image 	The projected image.
	 */
	void fireProjectedRndSettingsCreation(List<Integer> indexes, 
			ImageData image)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		RndProxyDef def = rnd.getRndSettingsCopy();
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
		if (metadataViewer != null)
			metadataViewer.setParentRootObject(parent, grandParent);
		if (isHCSImage()) fireMeasurementsLoading();
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
				if (index >= 0) planeInfos.put(index, object);
			}
		}
    }
    
    /**
     * Returns the <code>Plane Info</code> for the specified XY-plane.
     * 
     * @param z The z coordinate.  Must be in the range <code>[0, sizeZ)</code>.
	 * @param c The w coordinate.  Must be in the range <code>[0, sizeW)</code>.
	 * @param t The t coordinate.  Must be in the range <code>[0, sizeT)</code>.
     * @return See above.
     */
    PlaneInfo getPlane(int z, int c, int t)
    {
    	Integer index = null;
    	try {
			index = linearize(z, c, t);
			if (index < 0) return null;
			return planeInfos.get(index);
		} catch (Exception e) {}
    	return null;
    }

    /** Loads the image before doing anything else. */
	void fireImageLoading()
	{
		state = ImViewer.LOADING_IMAGE_DATA;
		ImageDataLoader loader = new ImageDataLoader(component, getImageID());
		loader.load();
	}
	
	/** 
	 * Sets the image data.
	 * 
	 * @param image The value to set.
	 */
	void setImageData(ImageData image)
	{
		state = ImViewer.LOADING_RND;
		this.image = image;
		initializeMetadataViewer();
		currentPixelsID = image.getDefaultPixels().getId();
		if (metadataViewer != null)
			metadataViewer.setParentRootObject(parent, grandParent);
	}
	
	/**
	 * Returns <code>true</code> if the rendering settings are original, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isOriginalSettings() { return isSameSettings(originalDef, false); }

	/**
	 * Returns <code>true</code> if it is the original plane, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isOriginalPlane()
	{
		if (originalDef.getDefaultZ() != getDefaultZ()) return false;
		if (originalDef.getDefaultT() != getDefaultT()) return false;
		return true;
	}
	
	/**
	 * Returns <code>true</code> if the passed rendering settings are the same
	 * that the current one, <code>false</code> otherwise.
	 * 
	 * @param def The settings to check.
	 * @param checkPlane Pass <code>true</code> to check the plane, 
	 * 					 <code>false</code> otherwise.
	 * @return See above.
	 */
	boolean isSameSettings(RndProxyDef def, boolean checkPlane)
	{
		if (metadataViewer == null) return false;
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return false;
		return rnd.isSameSettings(def, checkPlane);
	}
	
    /**
     * Sets the projected image for preview.
     * 
     * @param image The buffered image.
     */
	void setRenderProjected(Object image)
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
		return getImage().getDefaultPixels();
	}
	
	/**
	 * Returns the last projection details.
	 * 
	 * @return See above.
	 */
	ProjectionParam getLastProjRef() { return lastProjRef; }
	
	/**
	 * Sets the last projection details.
	 * 
	 * @param ref The value to set.
	 */
	void setLastProjectionRef(ProjectionParam ref) { lastProjRef = ref; }
	
	/**
	 * Returns the time-point used for the projection's preview.
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

	/**
	 * Returns the unit in microns.
	 * 
	 * @return See above.
	 */
	double getUnitInMicrons() { return browser.getUnitInMicrons(); }
	
	/** Loads all the available datasets. */
	void loadAllContainers()
	{
		ContainerLoader loader = new ContainerLoader(component);
		loader.load();
	}

	/** Makes a movie. */
	void makeMovie()
	{
		if (metadataViewer == null) return;
		metadataViewer.makeMovie((int) getUnitInMicrons(), 
				getBrowser().getUnitBarColor());
	}
	
	
	/**
	 * Sets the selected lifetime bin.
	 * 
	 * @param bin The selected bin.
	 */
	void setSelectedBin(int bin)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		List<ChannelData> channels = getChannelData();
		ChannelData channel;
		Iterator<ChannelData> i = channels.iterator();
		int index;
		while (i.hasNext()) {
			channel = i.next();
			index = channel.getIndex();
			rnd.setActive(index, index == bin);
		}
	}
	
	/** Sets the rendering engine to handle lifetime image. */
	void setForLifetime()
	{
		if (!isNumerousChannel()) return;
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		setColorModel(ImViewer.GREY_SCALE_MODEL, true);
		List<ChannelData> channels = getChannelData();
		ChannelData c;
		Iterator<ChannelData> i = channels.iterator();
		int index;
		while (i.hasNext()) {
			c = i.next();
			index = c.getIndex();
			rnd.setChannelWindow(index, c.getGlobalMin(), c.getGlobalMax());
			rnd.setActive(index, index == 0);
		}
	}
	
	/**
	 * Returns the selected bin.
	 * 
	 * @return See above.
	 */
	int getSelectedBin()
	{
		List<Integer> active = getActiveChannels();
		if (active == null || active.size() != 1) return 0;
		return active.get(0);
	}

	/** 
	 * Sets the selected channel.
	 * 
	 * @param index The index of the channel.
	 */
	void setSelectedChannel(int index)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		rnd.setSelectedChannel(index);
	}
	
    /**
     * Returns <code>true</code> if the viewer should be opened in a 
     * separate window, <code>false</code> otherwise.
     * The default value is <code>true</code>.
     * 
     * @return See above.
     */
    boolean isSeparateWindow() { return separateWindow; }
   
    /**
     * Sets the {@link #separateWindow} flag. 
     * 
     * @param separateWindow Pass <code>true</code> to view the viewer
     *						 in a separate window, <code>false</code> otherwise.
     */
    void setSeparateWindow(boolean separateWindow)
    { 
    	this.separateWindow = separateWindow;
    }
    
    /** Loads the measurements associated to the plate if any specified. */
    void fireMeasurementsLoading()
    {
    	if (parent instanceof WellData) {
    		//PlateData p = ((WellData) parent).getPlate();
    		ImageData p = getImage();
    		MeasurementsLoader loader = new MeasurementsLoader(component, p);
    		loader.load();
    	}
    }

	/**
	 * Sets the measurements associated to either the image or the plate.
	 * 
	 * @param result The collection to set.
	 */
	void setMeasurements(Collection result)
	{
		measurements = result;
	}
    
	/**
	 * Returns the measurements if any.
	 * 
	 * @return See above.
	 */
	Collection getMeasurements() { return measurements; }
	
	/**
	 * Sets the retrieved image, returns the a magnification or <code>-1</code>
	 * if no magnification factor computed. 
	 * 
	 * @param image The image to set.
	 * @return See above.
	 */
	double setImageAsTexture(TextureData image)
	{
		state = ImViewer.READY; 
		browser.setRenderedImage(image);
		loaders.remove(IMAGE);
		firstTime = false;
		//update image icon
		//28/02 added to speed up process, turn back on for 4.1
		/*
		if (imageIcon == null) {
			computeSizes();
			imageIcon = Factory.magnifyImage(factor, image);
		}
		*/
		return initZoomFactor();
	}

	/**
	 * Brings up the activity options.
	 * 
	 * @param source   The source of the mouse pressed.
	 * @param location The location of the mouse pressed.
	 */
	void activityOptions(Component source, Point location)
	{
		if (metadataViewer == null) return;
		metadataViewer.activityOptions(source, location, 
				MetadataViewer.PUBLISHING_OPTION);
	}

    /**
     * Returns <code>true</code> if the passed channels compose an RGB image, 
     * <code>false</code> otherwise.
     * 
     * @param channels The collection of channels to handle.
     * @return See above.
     */
	boolean isMappedImageRGB(List channels)
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return false;
		return rnd.isMappedImageRGB(channels);
	}
	
	/**
	 * Returns the overlays associated to that image.
	 * 
	 * @return See above.
	 */
	Map<Integer, Integer> getOverLays()
	{
		Iterator i = measurements.iterator();
		Object object;
		TableResult table = null;
		
		while (i.hasNext()) {
			object = i.next();
			if (object instanceof TableResult) {
				table = (TableResult) object;
				overlayTableID = table.getTableID();
				break;
			}
		}
		if (table == null) return null;
		Object[][] data = table.getData();
		Map<Integer, Integer> overlays = new LinkedHashMap<Integer, Integer>();
		int index = 0;
		Color c = null;
		Long value = -1L;
		for (int j = 0; j < data.length; j++) {
			value = (Long) data[j][2];
			if (value != null) {
				overlays.put((Integer) data[j][0], value.intValue());
			} else {
				if (index == 0) c = Color.red;
				else if (index == 1) c = Color.green;
				else if (index == 2) c = Color.blue;
				overlays.put((Integer) data[j][0], UIUtilities.convertColor(c));
			}
			index++;
			if (index%3 == 0) index = 0;
		}
		return overlays;
	}
	
	/**
	 * Renders the overlays.
	 * 
	 * @param overlays
	 */
	void renderOverlays(Map<Long, Integer> overlays)
	{
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		state = ImViewer.LOADING_IMAGE;
		OverlaysRenderer loader = new OverlaysRenderer(component, getPixelsID(), 
				pDef, overlayTableID, overlays);
		loader.load();
	}
	
	/**
	 * Returns the original ratio.
	 * 
	 * @return See above.
	 */
	double getOriginalRatio() { return originalRatio; }
	
	/**
	 * Determines the size of the image if it is a big image.
	 * 
	 * @return See above.
	 */
	Dimension computeSize()
	{
		computedSize = new Dimension(getMaxX(), getMaxY());
		return computedSize;
		/*
		if (!isBigImage()) {
			computedSize = new Dimension(getMaxX(), getMaxY());
			return computedSize;
		}
		originalRatio = Math.min((double) RenderingControl.MAX_SIZE/getMaxX(), 
				(double) RenderingControl.MAX_SIZE/getMaxY());
		computedSize = Factory.computeThumbnailSize(
				RenderingControl.MAX_SIZE, RenderingControl.MAX_SIZE, 
				getMaxX(), getMaxY());
		if (computedSize == null) 
			computedSize = new Dimension(RenderingControl.MAX_SIZE,
					RenderingControl.MAX_SIZE);
		return computedSize;
		*/
	}
	
	/** Refreshes the renderer. */
	void refresh()
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd != null) rnd.refresh();
	}
	
	/**
	 * Returns <code>true</code> if the object is writable,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isWritable()
	{ 
		boolean b = isUserOwner();
		if (b) return b;
		int level = 
			ImViewerAgent.getRegistry().getAdminService().getPermissionLevel();
		switch (level) {
			case AdminObject.PERMISSIONS_GROUP_READ_LINK:
			case AdminObject.PERMISSIONS_PUBLIC_READ_WRITE:
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the user logged in is the owner of the 
	 * image, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isUserOwner()
	{
		long userID = ImViewerAgent.getUserDetails().getId();
		return EditorUtil.isUserOwner(getImage(), userID);
	}
	
	/** 
	 * Sets the maximum range for channels.
	 * 
	 *  @param absolute Pass <code>true</code> to set it to the absolute value,
	 *  				<code>false</code> to the minimum and maximum.
	 */
	void setRangeAllChannels(boolean absolute)
	{
		metadataViewer.getRenderer().setRangeAllChannels(absolute);
	}
	
	/**
	 * Returns <code>true</code> if the data object is a well sample, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isHCSImage()
	{
		return (image instanceof WellSampleData);
	}

	/**
	 * Loads the bird eye view image for big image.
	 */
	void fireBirdEyeViewRetrieval()
	{
		// use the lowest resolution
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		int level = getSelectedResolutionLevel();
		PlaneDef pDef = createPlaneDef();
		Dimension d = getTileSize();
		int w = d.width;
		int h = d.height;
		int edgeWidth = w;
		int edgeHeight = h;
		ResolutionLevel rl = resolutionMap.get(0);
		int px = rl.getPowerAlongX();
		int py = rl.getPowerAlongY();
		rl = resolutionMap.get(getResolutionLevels()-1);
		int mx = rl.getPowerAlongX();
		int my = rl.getPowerAlongY();
		int size = (int) (getMaxX()/Math.pow(2, mx-px));
		edgeWidth = w;
		int n = size/w;
		int tiledImageSizeX = n*w;
		if (n*w < size) {
			edgeWidth = size-n*w;
			tiledImageSizeX += edgeWidth;
			n++;
		}
		size = (int) (getMaxY()/Math.pow(2, my-py));
		edgeHeight = h;
		n = size/h;
		int tiledImageSizeY = n*h;
		if (n*h < size) {
			edgeHeight = size-n*h;
			tiledImageSizeY += edgeHeight;
			n++;
		}
		pDef.region = new RegionDef(0, 0, tiledImageSizeX, tiledImageSizeY);
		//rnd.setSelectedResolutionLevel(0);
		//BufferedImage image = rnd.renderPlane(pDef);
		double ratio = 1;
		w = tiledImageSizeX;
		h = tiledImageSizeY;
		if (w < BirdEyeLoader.BIRD_EYE_SIZE || h < BirdEyeLoader.BIRD_EYE_SIZE)
			ratio = 1;
		else {
			if (w >= h) ratio = (double) BirdEyeLoader.BIRD_EYE_SIZE/w;
			else ratio = (double) BirdEyeLoader.BIRD_EYE_SIZE/h;
		}
		if (ratio < BirdEyeLoader.MIN_RATIO) ratio = BirdEyeLoader.MIN_RATIO;
		state = ImViewer.LOADING_BIRD_EYE_VIEW;
		BirdEyeLoader loader = new BirdEyeLoader(component, getImage(), pDef,
				ratio);
		loader.load();
		loaders.put(BIRD_EYE_BVIEW, loader);
		/*
		if (image != null) {
			BufferedImage newImage;
			if (ratio != 1) newImage = Factory.magnifyImage(ratio, image);
			else newImage = image;
			component.setBirdEyeView(newImage);
		} else {
			BirdEyeLoader loader = new BirdEyeLoader(component, getImage());
			loader.load();
		}
		*/
		//rnd.setSelectedResolutionLevel(level);
	}

	/**
	 * Returns the size of the tile.
	 * 
	 * @return See above.
	 */
	Dimension getTileSize()
	{
		if (tileSize != null) return tileSize;
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return null;
		ResolutionLevel r = resolutionMap.get(getSelectedResolutionLevel());
		if (r == null) {
			tileSize = rnd.getTileSize();
			return tileSize; 
		}
		tileSize = new Dimension((int) Math.pow(2, r.getPowerAlongX()), 
				(int) Math.pow(2, r.getPowerAlongY()));
		return tileSize; 
	}

    /**
     * Returns the number of rows, default is <code>1</code>.
     * 
     * @return See above.
     */
    int getRows() { return numberOfRows; }
    
    /**
     * Returns the number of columns, default is <code>1</code>.
     * 
     * @return See above.
     */
    int getColumns() { return numberOfColumns; }
    
    /**
     * Returns the tiles to display.
     * 
     * @return See above.
     */
    Map<Integer, Tile> getTiles() { return tiles; }

    /** 
     * Fires an asynchronous call to load the tiles.
     * 
     * @param selection The collection of tiles to load.
     */
    void fireTileLoading(List<Tile> selection)
    {
    	Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null || selection == null) return;
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		List<Tile> list;
		list = selection;
		sortTilesByIndex(list);
		state = ImViewer.LOADING_TILES;
		TileLoader loader = new TileLoader(component, currentPixelsID, pDef, 
				list);
		loader.load();
    }
    
    /** Resets the tiles.*/
    void resetTiles()
    {
    	Iterator<Tile> i = tiles.values().iterator();
		while (i.hasNext())
			i.next().setImage(null);
    }

	/**
	 * Returns the possible resolution levels. This method should only be used
	 * when dealing with large images.
	 * 
	 * @return See above.
	 */
	int getResolutionLevels()
	{ 
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return 1; 
		return rnd.getResolutionLevels();
	}
	
	/**
	 * Returns the currently selected resolution level. This method should only 
	 * be used when dealing with large images.
	 * 
	 * @return See above.
	 */
	int getSelectedResolutionLevel()
	{
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return 0; 
		return rnd.getSelectedResolutionLevel();
	}
	
	/**
	 * Sets resolution level. This method should only be used when dealing with
	 * large images.
	 * 
	 * @param level The value to set.
	 */
	void setSelectedResolutionLevel(int level)
	{
		if (level < 0) level = 0;
		if (level >= getResolutionLevels())
			level = getResolutionLevels()-1;
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd == null) return;
		clearTileImages(tiles.values());
		tiles.clear();
		rnd.setSelectedResolutionLevel(level);
		//tileSize = null;
		initializeTiles();
	}
	
	/**
	 * Returns the size of the tiled image along the X-axis i.e.
	 * the size of a tile along the X-axis multiplied by the number of columns.
	 * 
	 * @return See above.
	 */
	int getTiledImageSizeX() { return tiledImageSizeX; }
	
	/**
	 * Returns the size of the tiled image along the Y-axis i.e.
	 * the size of a tile along the Y-axis multiplied by the number of rows.
	 * 
	 * @return See above.
	 */
	int getTiledImageSizeY() { return tiledImageSizeY; }

	/**
	 * Clears the images hosted by the tile if not <code>null</code>.
	 * 
	 * @param toClear The collection to handle.
	 */
	 void clearTileImages(Collection<Tile> toClear)
	 {
		if (toClear == null || toClear.size() == 0) return;
		Iterator<Tile> i = toClear.iterator();
		Tile tile;
		Object image;
		BufferedImage bi;
		TextureData data;
		while (i.hasNext()) {
			tile = i.next();
			image = tile.getImage();
			if (image != null) {
				if (image instanceof BufferedImage) {
					bi = (BufferedImage) image;
					bi.getGraphics().dispose();
					bi.flush();
					tile.setImage(null);
				} else {
					data = (TextureData) image;
					data.flush();
					tile.setImage(null);
				}
			}
		}
	}
	
	/** Cancels the rendering of the image.*/
	void cancelRendering()
	{
		DataLoader loader = loaders.get(IMAGE);
		if (loader != null) {
			loader.cancel();
			firstTime = true;
			state = ImViewer.LOADING_IMAGE_CANCELLED;
		}
	}
	
	/**
	 * Sets the image for the bird eye view.
	 * 
	 * @param image The image to set.
	 */
	void setBirdEyeView(BufferedImage image)
	{
		loaders.remove(BIRD_EYE_BVIEW);
		getBrowser().setBirdEyeView(image);
	}

}
