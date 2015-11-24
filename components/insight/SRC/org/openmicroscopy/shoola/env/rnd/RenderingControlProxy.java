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
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import omero.LockTimeout;
import omero.api.RenderingEnginePrx;
import omero.api.ResolutionDescription;
import omero.model.Family;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Pixels;
import omero.model.QuantumDef;
import omero.model.RenderingModel;
import omero.model.enums.UnitsLength;
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.LookupNames;

import omero.gateway.cache.CacheService;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.ConnectionExceptionHandler;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.RenderingServiceException;
import omero.log.LogMessage;

import org.openmicroscopy.shoola.env.rnd.data.ResolutionLevel;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.image.io.WriterImage;

import omero.gateway.model.ChannelData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.PixelsData;

/** 
 * UI-side implementation of the {@link RenderingControl} interface.
 * Runs in the Swing thread.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
class RenderingControlProxy
	implements RenderingControl
{
 
	/** Default error message. */
	private static final String ERROR = "An error occurred while trying to " +
										"set the ";
	
	/** Default error message. */
	private static final String ERROR_RENDER = "An error occurred while " +
			"rendering ";
	
	/** The Red Color index. */
	private static final Integer RED_INDEX = 0;
	
	/** The Green Color index. */
	private static final Integer GREEN_INDEX = 1;
	
	/** The Blue Color index. */
	private static final Integer BLUE_INDEX = 2;
	
	/** The Non-Primary Color index. */
	private static final Integer NON_PRIMARY_INDEX = -1;
	
	/** The maximum number of retry.*/
	private static final int MAX_RETRY = 2;
	
    /** List of supported families. */
    private List families;
    
    /** List of supported models. */
    private List models;
    
    /** The pixels set to render. */
    private Pixels pixs;
    
    /** Reference to service to render pixels set. */
    private RenderingEnginePrx servant;

    /** The id of the cache associated to this proxy. */
    private int cacheID;
    
    /** The channel metadata. */
    private ChannelData[] metadata;
    
    /** Local copy of the rendering settings used to speed-up the client. */
    private RndProxyDef rndDef;
    
    /** Indicates if the compression level. */
    private int compression;
    
    /** Helper reference to the registry. */
    private Registry context;
    
    /** The size of the cache. */
    private int cacheSize;
    
    /** The size of the image. */
    private int imageSize;
    
    /** The rendering settings. */
    private Map<String, List<RndProxyDef>> settings;
    
    /** The possible resolution levels if it is a big image.*/
    private int resolutionLevels;
    
    /** The selected resolution level.*/
    private int selectedResolutionLevel;
    
    /** The size of a tile. */
    private Dimension tileSize;
    
    /** Flag indicating that the image is a big image or not.*/
    private Boolean bigImage;
    
	/** The associated rendering controls.*/
	private List<RenderingControl> slaves;
	
	/** Time of the last interaction.*/
	private long lastAction;
	
	/** Flag indicating if the rendering engine is already shut down or not.*/
	private boolean shutDown;
	
	/** The security context associated to the control.*/
	private SecurityContext ctx;
	
	/** The number of retry.*/
	private int retry;
	
    /**
     * Maps the color channel Red to {@link #RED_INDEX}, Blue to 
     * {@link #BLUE_INDEX}, Green to {@link #GREEN_INDEX} and
     * non primary colors map to {@link #NON_PRIMARY_COLOUR}.
     * 
     * @param channel
     * @return see above.
     */
    private Integer colourIndex(int channel)
    {
    	if (isChannelBlue(channel)) return BLUE_INDEX;
    	if (isChannelRed(channel)) return RED_INDEX;
    	if (isChannelGreen(channel)) return GREEN_INDEX;
    	return NON_PRIMARY_INDEX;
    }

    /**
     * Handles only connection error. Returns <code>true</code> if it is not a
     * connection error, <code>false</code> otherwise.
     * 
     * @param e The exception to handle.
     * @return See above.
     */
    private boolean handleConnectionException(Throwable e)
    {
        ConnectionExceptionHandler handler = new ConnectionExceptionHandler();
        int index = handler.handleConnectionException(e);
        if (index < 0) return true;
        log("Handle Exception:"+index);
        context.getTaskBar().sessionExpired(index);
        return index == ConnectionExceptionHandler.LOST_CONNECTION;
    }

    /**
     * Logs the specified message.
     * 
     * @param error The message to log.
     */
    private void log(String error)
    {
        context.getLogger().debug(this, error);
    }

    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Methods in this class are required to fill in a meaningful context
     * message.
     * 
     * @param e The exception.
     * @param message The context message.
     * @throws RenderingServiceException A rendering problem
     * @throws DSOutOfServiceException A connection problem.
     */
    private void handleException(Throwable e, String message)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	if (shutDown) return;
    	retry = 0;
    	if (e instanceof Ice.OperationNotExistException) {
    	    RenderingServiceException ex = new RenderingServiceException(e);
            ex.setIndex(RenderingServiceException.OPERATION_NOT_SUPPORTED);
            throw ex;
    	}
    	if (!handleConnectionException(e))
			throw new RenderingServiceException(message+"\n\n"+ 
					printErrorText(e), e);
    }

    /**
	 * Utility method to print the error message
	 * 
	 * @param e The exception to handle.
	 * @return  See above.
	 */
	private String printErrorText(Throwable e)
	{
		if (e == null) return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

    /**
     * Retrieves from the cache the buffered image representing the specified
     * plane definition. Note that only the images corresponding to an XY-plane
     * are cached.
     * 
     * @param pd The specified {@link PlaneDef plane definition}.
     * @return The corresponding bufferedImage.
     */
    private Object getFromCache(PlaneDef pd)
    {
        // We only cache XY images.
    	if (pd.slice == omero.romio.XY.value && cacheID >= 0) {
    		int index = pd.z+getPixelsDimensionsZ()*pd.t;
    		return context.getCacheService().getElement(cacheID, index);
    	}
        return null;
    }
    
    /**
     * Caches the specified image if it corresponds to an XYPlane.
     * 
     * @param pd The plane definition.
     * @param object The buffered image to cache or the bytes array.
     */
    private void cache(PlaneDef pd, Object object)
    {
    	if (isBigImage()) return;
        if (pd.slice == omero.romio.XY.value) {
            //We only cache XY images.
            //if (xyCache != null) xyCache.add(pd, object);
        	if (cacheID >= 0) {
        		int index = pd.z+getPixelsDimensionsZ()*pd.t;
        		//context.getCacheService().addElement(cacheID,
        			//	Integer.valueOf(index), object);
        	}
        }
    }
    
    /** Clears the cache. */
    private void invalidateCache()
    {
    	if (isBigImage()) return;
    	if (cacheID >= 0) context.getCacheService().clearCache(cacheID);
    }
    
    /** Clears the cache and releases memory. */
    private void eraseCache()
    {
    	if (isBigImage()) return;
    	invalidateCache();
    	if (cacheID >=0)
    		context.getCacheService().removeCache(cacheID);
    }
    
    /**
     * Initializes the cache for the specified plane.
     * 
     * @param pDef The plane of reference.
     */
    private void initializeCache(PlaneDef pDef)
    {
    	if (isBigImage()) return;
    	//if (xyCache != null) return;
    	if (cacheID >= 0) return;
    	/*
    	if (pDef.getSlice() == PlaneDef.XY && xyCache == null) {
    		//    		Okay, let's see if we can activate the xyCache.
            //In order to 
            //do that, the dimensions of the pixels array and the 
            //xyImgSize have to be available. 
            //This happens if at least one XY plane has been rendered.
            //Note that doing remote calls upfront to eagerly 
            //instantiate the xyCache is in most cases a total waste:
            //the client is  likely to call getPixelsDims() before an
            //image is ever  rendered and until an XY plane is 
            //not requested it's pointless to have a cache.
            xyCache = CachingService.createXYCache(pixs.getId(), length,
            				getPixelsDimensionsZ(), getPixelsDimensionsT());
    	}
    	*/
    	if (pDef.slice == omero.romio.XY.value) {
    		try {
    			cacheID = context.getCacheService().createCache(
    					CacheService.IN_MEMORY, cacheSize/imageSize);
			} catch (Exception e) {
				//log the error for example if the cache manager could not
				//be initialized.
				LogMessage msg = new LogMessage();
				msg.print("Initialize cache");
				msg.print(e);
				context.getLogger().error(this, msg);
			}
    	}
    }
  
    /**
     * Checks if the passed bit resolution is supported.
     * 
     * @param v The value to check.
     */
    private void checkBitResolution(int v)
    {
        switch (v) {
            case DEPTH_1BIT:
            case DEPTH_2BIT:
            case DEPTH_3BIT:
            case DEPTH_4BIT:
            case DEPTH_5BIT:
            case DEPTH_6BIT:
            case DEPTH_7BIT:
            case DEPTH_8BIT:
                return;
            default:
                throw new IllegalArgumentException("Bit resolution " +
                        "not supported.");
        }
    }
    
	/**
	 * Returns if <code>true</code> if one of the channels is of the specified
	 * color, <code>false</code> otherwise.
	 * 
	 * @param red The red component in the range [0, 255] in the default sRGB
	 * space.
	 * @param green The green component in the range [0, 255] in the default
	 *  sRGB space.
	 * @param blue The blue component in the range [0, 255] in the default sRGB
	 * space.
	 * @return See above.
	 */
	private boolean isRightColor(int red, int green, int blue)
	{
		for (int i = 0; i < getPixelsDimensionsC(); i++) {
			if (isActive(i)) {
				if (isRightChannelColor(i, red, green, blue))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns if <code>true</code> if the channel is of the specified
	 * color, <code>false</code> otherwise.
	 * 
	 * @param index The index of the channel.
	 * @param red The red component in the range [0, 255] in the default sRGB
	 * space.
	 * @param green The green component in the range [0, 255] in the default
	 * sRGB space.
	 * @param blue The blue component in the range [0, 255] in the default sRGB
	 * space.
	 * @return See above.
	 */
	private boolean isRightChannelColor(int index, int red, int green, int blue)
	{
		int[] rgba = rndDef.getChannel(index).getRGBA();
		return (rgba[0] == red && rgba[1] == green && rgba[2] == blue);
	}
	
	/**
	 * Returns the size.
	 * 
	 * @param pDef The plane object to handle.
	 * @return See above.
	 */
	private Point getSize(PlaneDef pDef)
	{
		int sizeX1, sizeX2;
        switch (pDef.slice) {
            case omero.romio.XZ.value:
                sizeX1 = pixs.getSizeX().getValue();
                sizeX2 = pixs.getSizeZ().getValue();
                break;
            case omero.romio.ZY.value:
                sizeX1 = pixs.getSizeZ().getValue();
                sizeX2 = pixs.getSizeY().getValue();
                break;
            case omero.romio.XY.value:
            default:
                sizeX1 = pixs.getSizeX().getValue();
                sizeX2 = pixs.getSizeY().getValue();
                if (pDef.region != null) {
                	sizeX1 = pDef.region.width;
                	sizeX2 = pDef.region.height;
                }
                break;
        }
        return new Point(sizeX1, sizeX2);
	}
	
    /** Initializes the cached rendering settings to speed up process. */
    private void initialize()
    {
    	try {
    		rndDef.setTypeSigned(servant.isPixelsTypeSigned());
    		rndDef.setDefaultZ(servant.getDefaultZ());
    		rndDef.setDefaultT(servant.getDefaultT());
    		QuantumDef qDef = servant.getQuantumDef();
    		rndDef.setBitResolution(qDef.getBitResolution().getValue());
    		rndDef.setColorModel(servant.getModel().getValue().getValue());
    		rndDef.setCodomain(qDef.getCdStart().getValue(), 
    				qDef.getCdEnd().getValue());

    		ChannelBindingsProxy cb;
    		ChannelData channel;
    		for (int i = 0; i < metadata.length; i++) {
    			channel = metadata[i];
    			cb = rndDef.getChannel(channel.getIndex());
    			if (cb == null) {
    				cb = new ChannelBindingsProxy();
    				rndDef.setChannel(channel.getIndex(), cb);
    			}
    			cb.setActive(servant.isActive(i));
    			cb.setInterval(servant.getChannelWindowStart(i),
    					servant.getChannelWindowEnd(i));
    			cb.setQuantization(
    					servant.getChannelFamily(i).getValue().getValue(),
    					servant.getChannelCurveCoefficient(i),
    					servant.getChannelNoiseReduction(i));
    			cb.setRGBA(servant.getRGBA(i));
    			cb.setLowerBound(servant.getPixelsTypeLowerBound(i));
    			cb.setUpperBound(servant.getPixelsTypeUpperBound(i));
    		}
    		tmpSolutionForNoiseReduction();
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.print("Initialize proxy");
			msg.print(e);
			context.getLogger().error(this, msg);
		}
    }
    
    
    private void tmpSolutionForNoiseReduction()
    {
    	 //DOES NOTHING TMP SOLUTION.
        try {
        	for (int i = 0; i < pixs.getSizeC().getValue(); i++) {
    			setQuantizationMap(i, getChannelFamily(i), 
    					getChannelCurveCoefficient(i), false);
    		}
		} catch (Exception e) {
			
		}
    }
    
    /** 
     * Sets the color.
     * 
     * @param w The index of the channel.
     * @param rgba The color to set.
     * @throws RenderingServiceException If an error occurred while setting
     * the value.
     * @throws DSOutOfServiceException If the connection is broken.
     * @see RenderingControl#setRGBA(int, Color)
     */
    private void setRGBA(int w, int[] rgba)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	try {
    		servant.setRGBA(w, rgba[0], rgba[1], rgba[2], rgba[3]);
    		rndDef.getChannel(w).setRGBA(rgba[0], rgba[1], rgba[2], rgba[3]);
    		invalidateCache();
		} catch (Exception e) {
			handleException(e, ERROR+"color for: "+w+".");
		}
    }

	/**
	 * Renders the compressed image.
	 * 
	 * @param pDef A plane orthogonal to one of the <i>X</i>, <i>Y</i>,
	 *             or <i>Z</i> axes.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while setting
     * the value.
     * @throws DSOutOfServiceException If the connection is broken.
	 */
	private BufferedImage renderCompressedBI(PlaneDef pDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		//Need to adjust the cache.
		//Object array = getFromCache(pDef);
		try {
			byte[] values = servant.renderCompressed(pDef);
			imageSize = values.length;
			return WriterImage.bytesToImage(values);
		} catch (Throwable e) {
			if (e instanceof LockTimeout && retry < MAX_RETRY) { //retry
				retry++;
				return renderCompressedBI(pDef);
			}
			handleException(e, ERROR_RENDER+"the compressed image.");
		} 
		return null;
	}
	
	/**
	 * Renders the image without compression.
	 * 
	 * @param pDef A plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *            or <i>Z</i> axes.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while setting
     * the value.
     * @throws DSOutOfServiceException If the connection is broken.
	 */
	private BufferedImage renderUncompressed(PlaneDef pDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		//See if the requested image is in cache.
        BufferedImage img = (BufferedImage) getFromCache(pDef);
        //if (img != null) return img;
        try {
        	int[] buf = servant.renderAsPackedInt(pDef);
            Point p = getSize(pDef);
            imageSize = 3*buf.length;
            initializeCache(pDef);
            img = Factory.createImage(buf, 32, p.x, p.y);
            cache(pDef, img);
		} catch (Throwable e) {
			if (e instanceof LockTimeout && retry < MAX_RETRY) { //retry
				retry++;
				return renderUncompressed(pDef);
			}
			handleException(e, ERROR_RENDER+"the uncompressed plane.");
		}
        
        return img;
	}

	/**
	 * Projects the selected section of the optical sections
	 * and renders a compressed image.
	 * 
	 * @param startZ   The first optical section.
	 * @param endZ     The last optical section.
	 * @param stepping The stepping of the projection.
	 * @param type     The projection type.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while setting 
     * the value.
     * @throws DSOutOfServiceException If the connection is broken.
	 */
	private BufferedImage renderProjectedCompressed(int startZ, int endZ,
		int stepping, int type)
		throws RenderingServiceException, DSOutOfServiceException
	{
		try {
			byte[] values = servant.renderProjectedCompressed(
					ProjectionParam.convertType(type), 
					getDefaultT(), stepping, startZ, endZ);
			
			return WriterImage.bytesToImage(values);
		} catch (Throwable e) {
			if (e instanceof LockTimeout && retry < MAX_RETRY) { //retry
				retry++;
				return renderProjectedCompressed(startZ, endZ, stepping, type);
			}
			handleException(e, ERROR_RENDER+"the projected selection.");
		}
		return null;
	}
	
	/**
	 * Projects the selected section of the optical sections
	 * and renders a compressed image.
	 * 
	 * @param startZ The first optical section.
	 * @param endZ The last optical section.
	 * @param stepping The stepping of the projection.
	 * @param type The projection type.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while setting 
     * the value.
     * @throws DSOutOfServiceException If the connection is broken.
	 */
	private BufferedImage renderProjectedUncompressed(int startZ, int endZ,
            int stepping, int type)
		throws RenderingServiceException, DSOutOfServiceException
	{
        BufferedImage img = null;
        try {
            int[] buf = servant.renderProjectedAsPackedInt(
            		ProjectionParam.convertType(type), 
					getDefaultT(), stepping, startZ, endZ);
            int sizeX1 = pixs.getSizeX().getValue();
            int sizeX2 = pixs.getSizeY().getValue();
            img = Factory.createImage(buf, 32, sizeX1, sizeX2);
		} catch (Throwable e) {
			if (e instanceof LockTimeout && retry < MAX_RETRY) { //retry
				retry++;
				return renderProjectedUncompressed(startZ, endZ, stepping,
						type);
			}
			handleException(e, ERROR_RENDER+"the projected selection.");
		}
        
        return img;
	}
	
	/** Checks if the proxy is still alive.*/
	private void isSessionAlive()
		throws RenderingServiceException
	{
    	lastAction = System.currentTimeMillis();
    	boolean b = false;
		try {
			b = context.getImageService().isAlive(ctx);
			if (!b) {
			    context.getTaskBar().sessionExpired(
			            ConnectionExceptionHandler.NETWORK);
			}
		} catch (DSOutOfServiceException e) {
			RenderingServiceException ex = new RenderingServiceException(e);
			ex.setIndex(RenderingServiceException.CONNECTION);
			throw ex;
		}
	}

	/**
	 * Returns the identifier of the user currently logged in.
	 * 
	 * @return See above.
	 */
	private long getUserID()
	{
	    ExperimenterData exp = (ExperimenterData) context.lookup(
                LookupNames.CURRENT_USER_DETAILS);
	    return exp.getId();
	}

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param context Helper reference to the registry.
     * @param re The service to render a pixels set.
     * Mustn't be <code>null</code>.
     * @param pixels The pixels set. Mustn't be <code>null</code>.
     * @param m The channel metadata. 
     * @param compression Pass <code>0</code> if no compression otherwise
	 * 					  pass the compression used.
	 * @param rndDefs Local copy of the rendering settings used to
	 * speed-up the client.
	 * @param cacheSize The desired size of the cache.
     */
    RenderingControlProxy(Registry context, SecurityContext ctx,
    		RenderingEnginePrx re, Pixels pixels, List<ChannelData> m,
    		int compression, List<RndProxyDef> rndDefs, int cacheSize)
    {
        if (re == null)
            throw new NullPointerException("No rendering engine.");
        if (pixels == null)
            throw new NullPointerException("No pixels set.");
        if (context == null)
            throw new NullPointerException("No registry.");
        if (ctx == null)
            throw new NullPointerException("No security context.");
        this.ctx = ctx;
        slaves = new ArrayList<RenderingControl>();
        resolutionLevels = -1;
        selectedResolutionLevel = -1;
        lastAction = System.currentTimeMillis();
        shutDown = false;
        this.cacheSize = cacheSize;
        this.context = context;
        servant = re;
        pixs = pixels;
        families = null;
        models = null;
        try {
        	families = servant.getAvailableFamilies();
            models = servant.getAvailableModels();
            cacheID = -1;
            imageSize = 1;
            this.compression = compression;
            metadata = new ChannelData[m.size()];
            Iterator<ChannelData> j = m.iterator();
            ChannelData cm;
            while (j.hasNext()) {
            	cm = j.next();
                metadata[cm.getIndex()] = cm;
            }
            if (rndDefs.size() < 1) {
                this.rndDef = context.getImageService().getSettings(ctx,
                        servant.getRenderingDefId());
            	initialize();
            } else {
            	this.rndDef = rndDefs.get(0);
            	ChannelBindingsProxy cb;
            	for (int i = 0; i < pixs.getSizeC().getValue(); i++) {
                    cb = rndDef.getChannel(i);
                    cb.setLowerBound(servant.getPixelsTypeLowerBound(i));
                    cb.setUpperBound(servant.getPixelsTypeUpperBound(i));
                }
            }
            tmpSolutionForNoiseReduction();
		} catch (Exception e) {
		}
    }

    /**
     * Reloads the settings after a saveAs w/o creating a thumbnail
     * This method should only be invoked after a save as to update the
     * other rendering engine.
     *
     * @param rndId The identifier of the rendering settigns.
     *  @throws RenderingServiceException If an error occurred while setting
     *                                    the value.
     * @throws DSOutOfServiceException    If the connection is broken.
     */
    void loadRenderingSettings(long rndId)
       throws RenderingServiceException, DSOutOfServiceException
    {
        isSessionAlive();
        try {
            servant.loadRenderingDef(rndId);
            servant.load();
        } catch (Throwable e) {
            handleException(e, "An error occurred while loading the settings.");
        }
    }

    /**
     * Returns <code>true</code> if the rendering engine is still active,
     * <code>false</code> otherwise.
     * 
     * @param timeout The time after which the engine is considered to be
     * inactive.
     * @return See above.
     */
    boolean isProxyActive(long timeout)
    {
    	long time = System.currentTimeMillis();
    	return time-lastAction < timeout;
    }
    
    /** Sets the rendering control associated to the main control.*/
    void setSlaves(List<RenderingControl> slaves)
    {
    	if (slaves == null) return;
    	this.slaves = slaves;
    }
    
    /**
     * Resets the rendering engine.
     * 
     * @param servant The value to set.
     * @param rndDef Local copy of the rendering settings used to speed-up the 
     * client.
	 * @throws RenderingServiceException If an error occurred while setting 
     * the value.
     * @throws DSOutOfServiceException If the connection is broken.
     */
    void resetRenderingEngine(RenderingEnginePrx servant, RndProxyDef rndDef)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	if (servant == null) return;
    	try {
			this.servant.close();
		} catch (Exception e) {
		    log("Error while closing the rendering engine "+e);
		}
    	invalidateCache();
    	this.servant = servant;
    	shutDown = false;
    	lastAction = System.currentTimeMillis();
    	try {
    		if (rndDef == null) {
            	initialize();
            } else {
            	this.rndDef = rndDef;
            	ChannelBindingsProxy cb;
            	for (int i = 0; i < pixs.getSizeC().getValue(); i++) {
                    cb = rndDef.getChannel(i);
                    cb.setLowerBound(servant.getPixelsTypeLowerBound(i));
                    cb.setUpperBound(servant.getPixelsTypeUpperBound(i));
                }
            }
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering engine.");
		}
    }
    
    /**
     * Reloads the rendering engine.
     * 
     * @param servant The value to set.
     * @throws RenderingServiceException If an error occurred while setting 
     * the value.
     * @throws DSOutOfServiceException If the connection is broken.
     */
    void setRenderingEngine(RenderingEnginePrx servant)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	if (servant == null) return;
    	this.servant = servant;
    	shutDown = false;
    	lastAction = System.currentTimeMillis();
    	// reset default of the rendering engine.
    	if (rndDef == null) return;
    	try {
    		servant.setDefaultZ(rndDef.getDefaultZ());
        	servant.setDefaultT(rndDef.getDefaultT());
        	servant.setQuantumStrategy(rndDef.getBitResolution());
        	Iterator k = models.iterator();
            RenderingModel model;
            String value = rndDef.getColorModel();
            while (k.hasNext()) {
                model= (RenderingModel) k.next();
                if (model.getValue().getValue().equals(value)) 
                    servant.setModel(model); 
            }
        	servant.setCodomainInterval(rndDef.getCdStart(), rndDef.getCdEnd());
        	
            ChannelBindingsProxy cb;
            
            Family family;
            int[] rgba;
            for (int i = 0; i < pixs.getSizeC().getValue(); i++) {
                cb = rndDef.getChannel(i);
                servant.setActive(i, cb.isActive());
                servant.setChannelWindow(i, cb.getInputStart(),
                		cb.getInputEnd());
                k = families.iterator();
                value = cb.getFamily();
                while (k.hasNext()) {
                    family= (Family) k.next();
                    if (family.getValue().getValue().equals(value)) {
                    	servant.setQuantizationMap(i, family,
                    			cb.getCurveCoefficient(),
                    			cb.isNoiseReduction());
                      
                    }
                }
                rgba = cb.getRGBA();
                servant.setRGBA(i, rgba[0], rgba[1], rgba[2], rgba[3]);
            }
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering engine.");
		}
    }

    /** 
     * Shuts down the service. Returns <code>true</code> if the proxy
     * was already shut down, <code>false</code> otherwise.
     * 
     * @param keepCache Pass <code>true</code> to keep the cache,
     *                  <code>false</code> otherwise.
     * @return See above.
     */
    boolean shutDown(boolean keepCache)
    {
    	if (shutDown) return shutDown;
    	try {
    		if (!keepCache && cacheID >= 0)
    			context.getCacheService().removeCache(cacheID);
    		Iterator<RenderingControl> j = slaves.iterator();
			while (j.hasNext())
				((RenderingControlProxy) j.next()).shutDown();
		} catch (Exception e) {
		    log(e.toString());
		}
    	shutDown = true;
    	return false;
    }
    
    /** Shuts down the service. */
    void shutDown() { shutDown(false); }
    
	/**
	 * Resets the size of the cache.
	 * 
	 * @param size The size, in bytes, of the cache.
	 */
	void setCacheSize(int size)
	{
		if (imageSize == 0) imageSize = 1;
		if (cacheID >= 0)
			context.getCacheService().setCacheEntries(cacheID, size/imageSize);
	}
	
    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#setModel(String)
     */
    public void setModel(String value)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		Iterator i = models.iterator();
            RenderingModel model;
            while (i.hasNext()) {
                model= (RenderingModel) i.next();
                if (model.getValue().getValue().equals(value)) {
                    servant.setModel(model);
                    rndDef.setColorModel(value);
                    invalidateCache();
                }
            }
            Iterator<RenderingControl> j = slaves.iterator();
			while (j.hasNext())
				j.next().setModel(value);
		} catch (Exception e) {
			handleException(e, ERROR+"model.");
		}
     }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getModel()
     */
    public String getModel() { return rndDef.getColorModel(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getDefaultZ()
     */
    public int getDefaultZ() { return rndDef.getDefaultZ(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getDefaultT()
     */
    public int getDefaultT() { return rndDef.getDefaultT(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#setDefaultZ(int)
     */
    public void setDefaultZ(int z)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		int maxZ = getPixelsDimensionsZ();
    		if (z < 0) z = 0;
    		if (z >= maxZ) z = maxZ-1;
    		servant.setDefaultZ(z);
            rndDef.setDefaultZ(z);
            Iterator<RenderingControl> i = slaves.iterator();
			while (i.hasNext())
				i.next().setDefaultZ(z);
		} catch (Exception e) {
			handleException(e, ERROR+"default Z.");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#setDefaultT(int)
     */
    public void setDefaultT(int t)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		int maxT = getPixelsDimensionsT();
    		if (t < 0) t = 0;
    		if (t >= maxT) t = maxT-1;
    		servant.setDefaultT(t);
            rndDef.setDefaultT(t);
            Iterator<RenderingControl> i = slaves.iterator();
			while (i.hasNext())
				i.next().setDefaultT(t);
		} catch (Exception e) {
			handleException(e, ERROR+"default T.");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#setQuantumStrategy(int)
     */
    public void setQuantumStrategy(int bitResolution)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		checkBitResolution(bitResolution);
            servant.setQuantumStrategy(bitResolution);
            rndDef.setBitResolution(bitResolution);
            invalidateCache();
            Iterator<RenderingControl> j = slaves.iterator();
			while (j.hasNext())
				j.next().setQuantumStrategy(bitResolution);
		} catch (Exception e) {
			handleException(e, ERROR+"bit resolution.");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#setCodomainInterval(int, int)
     */
    public void setCodomainInterval(int start, int end)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		servant.setCodomainInterval(start, end);
            rndDef.setCodomain(start, end);
            Iterator<RenderingControl> i = slaves.iterator();
            while (i.hasNext())
                i.next().setCodomainInterval(start, end);
            invalidateCache();
		} catch (Exception e) {
			handleException(e, ERROR+"codomain interval.");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#setQuantizationMap(int, String, double, boolean)
     */
    public void setQuantizationMap(int w, String value, double coefficient,
                                    boolean noiseReduction)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		List list = servant.getAvailableFamilies();
            Iterator i = list.iterator();
            Family family;
            while (i.hasNext()) {
                family = (Family) i.next();
                if (family.getValue().getValue().equals(value)) {
                    servant.setQuantizationMap(w, family, coefficient,
                                                noiseReduction);
                    rndDef.getChannel(w).setQuantization(value, coefficient,
                                                noiseReduction);
                    invalidateCache();
                }
            }
            Iterator<RenderingControl> j = slaves.iterator();
			while (j.hasNext())
				j.next().setQuantizationMap(w, value, coefficient,
						noiseReduction);
		} catch (Exception e) {
			handleException(e, ERROR+"quantization map.");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getChannelFamily(int)
     */
    public String getChannelFamily(int w)
    { 
    	ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return "";
        return rndDef.getChannel(w).getFamily();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getChannelNoiseReduction(int)
     */
    public boolean getChannelNoiseReduction(int w)
    {
    	ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return false;
        return channel.isNoiseReduction();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelCurveCoefficient(int)
     */
    public double getChannelCurveCoefficient(int w)
    {
    	ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return 1;
        return channel.getCurveCoefficient();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#setChannelWindow(int, double, double)
     */
    public void setChannelWindow(int w, double start, double end)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		servant.setChannelWindow(w, start, end);
            rndDef.getChannel(w).setInterval(start, end);
            Iterator<RenderingControl> i = slaves.iterator();
    		while (i.hasNext())
    			i.next().setChannelWindow(w, start, end);
            invalidateCache();
		} catch (Exception e) {
			handleException(e, ERROR+"input channel for: "+w+".");
		}  
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getChannelWindowStart(int)
     */
    public double getChannelWindowStart(int w)
    {
    	ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return 0;
        return channel.getInputStart();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getChannelWindowEnd(int)
     */
    public double getChannelWindowEnd(int w)
    {
    	ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return 0;
        return channel.getInputEnd();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setRGBA(int, Color)
     */
    public void setRGBA(int w, Color c)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		servant.setRGBA(w, c.getRed(), c.getGreen(), c.getBlue(),
    						c.getAlpha());
    		rndDef.getChannel(w).setRGBA(c.getRed(), c.getGreen(), c.getBlue(),
    						c.getAlpha());
    		invalidateCache();
    		Iterator<RenderingControl> j = slaves.iterator();
			while (j.hasNext())
				j.next().setRGBA(w, c);
		} catch (Exception e) {
			handleException(e, ERROR+"color for: "+w+".");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getRGBA(int)
     */
    public Color getRGBA(int w)
    {
    	ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return Color.black;
        int[] rgba = channel.getRGBA();
        return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setActive(int, boolean)
     */
    public void setActive(int w, boolean active)
    	throws RenderingServiceException, DSOutOfServiceException
    { 
    	isSessionAlive();
    	try {
    		servant.setActive(w, active);
            rndDef.getChannel(w).setActive(active);
            Iterator<RenderingControl> i = slaves.iterator();
    		while (i.hasNext())
    			i.next().setActive(w, active);
            invalidateCache();
		} catch (Exception e) {
			handleException(e, ERROR+"active channel for: "+w+".");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#isActive(int)
     */
    public boolean isActive(int w)
    { 
    	ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return false;
        return channel.isActive();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#addCodomainMap(CodomainMapContext)
     */
    /*
    public void addCodomainMap(CodomainMapContext mapCtx)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	//servant.addCodomainMap(mapCtx);
        invalidateCache();
    }
*/
    
    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#updateCodomainMap(CodomainMapContext)
     */
    /*
    public void updateCodomainMap(CodomainMapContext mapCtx)
    	throws RenderingServiceException, DSOutOfServiceException
    {
        //servant.updateCodomainMap(mapCtx);
        invalidateCache();
    }
    */

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#removeCodomainMap(CodomainMapContext)
     */
    /*
    public void removeCodomainMap(CodomainMapContext mapCtx)
    	throws RenderingServiceException, DSOutOfServiceException
    {
        //servant.removeCodomainMap(mapCtx);
        invalidateCache();
    }
    */

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getCodomainMaps()
     */
    public List getCodomainMaps()
    {
        // TODO Auto-generated method stub
        return new ArrayList(0);
    }
    
    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#saveCurrentSettings()
     */
    public RndProxyDef saveCurrentSettings()
            throws RenderingServiceException, DSOutOfServiceException
            {
        isSessionAlive();
        Iterator<RenderingControl> i = slaves.iterator();
        try {
            long userID = getUserID();
            long ownerID = rndDef.getOwnerID();
            if (userID == ownerID) {
                servant.saveCurrentSettings();
                while (i.hasNext())
                    i.next().saveCurrentSettings();
                return rndDef.copy();
            } else {
                long id = servant.saveAsNewSettings();
                rndDef = context.getImageService().getSettings(ctx, id);
                while (i.hasNext()) {
                    ((RenderingControlProxy) i.next()).loadRenderingSettings(id);
                }
                return rndDef.copy();
            }
        } catch (Throwable e) {
            handleException(e, "An error occurred while saving the current " +
                    "settings.");
        }
        return null;
            }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#resetDefaults()
     */
    public void resetDefaults()
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	isSessionAlive();
    	try {
    		servant.resetDefaultSettings(false);
    		Iterator<RenderingControl> i = slaves.iterator();
    		while (i.hasNext())
				i.next().resetDefaults();
    		invalidateCache();
    		initialize();
		} catch (Throwable e) {
			handleException(e, ERROR+"default settings.");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getPixelsPhysicalSizeX()
     */
    public Length getPixelsPhysicalSizeX()
    {
        if (pixs.getPhysicalSizeX() == null) 
            return new LengthI(1, UnitsLength.PIXEL);
        return pixs.getPhysicalSizeX();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getPixelsPhysicalSizeY()
     */
    public Length getPixelsPhysicalSizeY()
    {
        if (pixs.getPhysicalSizeY() == null) 
            return new LengthI(1, UnitsLength.PIXEL);
        return pixs.getPhysicalSizeY();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getPixelsPhysicalSizeZ()
     */
    public Length getPixelsPhysicalSizeZ()
    {
        if (pixs.getPhysicalSizeZ() == null) 
            return new LengthI(1, UnitsLength.PIXEL);
        return pixs.getPhysicalSizeZ();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getPixelsDimensionsX()
     */
    public int getPixelsDimensionsX() { return pixs.getSizeX().getValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getPixelsDimensionsY()
     */
    public int getPixelsDimensionsY() { return pixs.getSizeY().getValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getPixelsDimensionsZ()
     */
    public int getPixelsDimensionsZ() { return pixs.getSizeZ().getValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getPixelsDimensionsT()
     */
    public int getPixelsDimensionsT() { return pixs.getSizeT().getValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getPixelsDimensionsC()
     */
    public int getPixelsDimensionsC() { return pixs.getSizeC().getValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getFamilies()
     */
    public List getFamilies()
    { 
        List<String> l = new ArrayList<String>(families.size());
        Iterator i= families.iterator();
        while (i.hasNext())
            l.add(((Family) i.next()).getValue().getValue());
        return l;
    }
    
    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getChannelData(int)
     */
    public ChannelData getChannelData(int w) { return metadata[w]; }
    
    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getChannelData()
     */
    public ChannelData[] getChannelData() { return metadata; }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getCodomainStart()
     */
    public int getCodomainStart() { return rndDef.getCdStart(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getCodomainEnd()
     */
    public int getCodomainEnd() { return rndDef.getCdEnd(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getBitResolution()
     */
    public int getBitResolution() { return rndDef.getBitResolution(); }

    /** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#hasActiveChannelBlue()
     */
	public boolean hasActiveChannelBlue() { return isRightColor(0, 0, 255); }

	/** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#hasActiveChannelGreen()
     */
	public boolean hasActiveChannelGreen() { return isRightColor(0, 255, 0); }
	
	/** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#hasActiveChannelRed()
     */
	public boolean hasActiveChannelRed() { return isRightColor(255, 0, 0); }
	
	/** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#isChannelRed(int)
     */
	public boolean isChannelRed(int index)
	{
		if (index < 0 || index > getPixelsDimensionsC()) return false;
		return isRightChannelColor(index, 255, 0, 0);
	}
	
	/** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#isChannelBlue(int)
     */
	public boolean isChannelBlue(int index)
	{
		if (index < 0 || index > getPixelsDimensionsC()) return false;
		return isRightChannelColor(index, 0, 0, 255);
	}
	
	/** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#isChannelGreen(int)
     */
	public boolean isChannelGreen(int index)
	{
		if (index < 0 || index > getPixelsDimensionsC()) return false;
		return isRightChannelColor(index, 0, 255, 0);
	}

	/** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#getRndSettingsCopy()
     */
	public RndProxyDef getRndSettingsCopy() { return rndDef.copy(); }
	
	public void resetSettings(RndProxyDef rndDef)
                throws RenderingServiceException, DSOutOfServiceException
        {
	    resetSettings(rndDef, false);
        }
	
	/** 
     * Implemented as specified by {@link RenderingControl}.
     * @see RenderingControl#resetSettings(RndProxyDef, boolean)
     */
	public void resetSettings(RndProxyDef rndDef, boolean includeZT)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndDef == null)
			throw new IllegalArgumentException("No rendering settings to " +
					"set");
		if (rndDef.getNumberOfChannels() != getPixelsDimensionsC())
			throw new IllegalArgumentException("Rendering settings not " +
					"compatible.");
		if (includeZT) {
		    setDefaultT(rndDef.getDefaultT());
		    setDefaultZ(rndDef.getDefaultZ());
		}
		setModel(rndDef.getColorModel());
		setCodomainInterval(rndDef.getCdStart(), rndDef.getCdEnd());
		setQuantumStrategy(rndDef.getBitResolution());
		ChannelBindingsProxy c;
		for (int i = 0; i < getPixelsDimensionsC(); i++) {
			c = rndDef.getChannel(i);
			if (c != null) {
				setRGBA(i, c.getRGBA());
				setChannelWindow(i, c.getInputStart(), c.getInputEnd());
				setQuantizationMap(i, c.getFamily(), c.getCurveCoefficient(),
									c.isNoiseReduction());
				setActive(i, c.isActive());
			}
		}
		Iterator<RenderingControl> i = slaves.iterator();
		while (i.hasNext())
			i.next().resetSettings(rndDef);
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getPixelsTypeLowerBound(int)
	 */
	public double getPixelsTypeLowerBound(int w)
	{
		ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return 0;
		return channel.getLowerBound();
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getPixelsTypeUpperBound(int)
	 */
	public double getPixelsTypeUpperBound(int w)
	{
		ChannelBindingsProxy channel = rndDef.getChannel(w);
    	if (channel == null) return 0;
		return channel.getUpperBound();
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#isPixelsTypeSigned()
	 */
	public boolean isPixelsTypeSigned() { return rndDef.isTypeSigned(); }
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#validatePixels(PixelsData)
	 */
	public boolean validatePixels(PixelsData pixels)
	{
		if (pixels == null) return false;
		long id = pixs.getDetails().getGroup().getId().getValue();
		if (id != pixels.getGroupId()) return false;
		if (getPixelsDimensionsC() != pixels.getSizeC()) return false;
		if (getPixelsDimensionsY() != pixels.getSizeY()) return false;
		if (getPixelsDimensionsX() != pixels.getSizeX()) return false;
		String s = pixels.getPixelType();
		String value = pixs.getPixelsType().getValue().getValue();
		if (!value.equals(s)) return false;
		return true;
	}
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#render(PlaneDef)
	 */
    public BufferedImage render(PlaneDef pDef)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	return render(pDef, compression);
    }
    
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#render(PlaneDef, int)
	 */
    public BufferedImage render(PlaneDef pDef, int value)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	if (pDef == null) 
             throw new IllegalArgumentException("Plane def cannot be null.");
    	try {
    	    context.getImageService().isAlive(ctx);
			servant.ice_ping();
		} catch (Exception e) {
			return null;
		}
    	retry = 0;
    	//since this method is always invoked after another change in
    	//the settings and due to the fact that the proxy is usually invoked
    	//in the swing thread.
    	if (value != compression) setCompression(value);
    	BufferedImage img;
        if (isCompressed()) img = renderCompressedBI(pDef);
        else img = renderUncompressed(pDef);
        if (value != compression) setCompression(compression);
        return img;
    }
    
    /** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#setCompression(int)
	 */
	public void setCompression(int compression)
	{
		try {
			isSessionAlive();
			float f = PixelsServicesFactory.getCompressionQuality(compression);
			rndDef.setCompression(f);
			servant.setCompressionLevel(f);
			this.compression = compression;
			Iterator<RenderingControl> i = slaves.iterator();
			while (i.hasNext())
				i.next().setCompression(compression);
			eraseCache();
		} catch (Exception e) {}
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#isCompressed()
	 */
	public boolean isCompressed()
	{ 
		return (compression != RenderingControl.UNCOMPRESSED);
	}
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getCompressionLevel()
	 */
	public int getCompressionLevel() { return compression; }

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#setOriginalRndSettings()
	 */
	public void setOriginalRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		isSessionAlive();
		try {
    		servant.resetDefaultSettings(false);
    		if (getPixelsDimensionsC() > 1) setModel(RGB);
    		List list = servant.getAvailableFamilies();
    		ChannelData m;
    		Iterator j;
    		Family family;
    		String value;
    		for (int i = 0; i < pixs.getSizeC().getValue(); i++) {
    			j = list.iterator();
    			while (j.hasNext()) {
    				family= (Family) j.next();
    				value = family.getValue().getValue();
    				if (value.equals(getChannelFamily(i))) {
    					servant.setQuantizationMap(i, family, 
    							getChannelCurveCoefficient(i), false);
    				}
    			}
    			m = getChannelData(i);
    			servant.setChannelWindow(i, m.getGlobalMin(),
    					m.getGlobalMax());
    		}

    		invalidateCache();
    		initialize();
    		Iterator<RenderingControl> i = slaves.iterator();
			while (i.hasNext())
				i.next().setOriginalRndSettings();
		} catch (Throwable e) {
			handleException(e, ERROR+"default settings.");
		}
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#renderProjected(int, int, int, int, List)
	 */
	public BufferedImage renderProjected(int startZ, int endZ, int stepping,
			                           int type, List<Integer> channels) 
		throws RenderingServiceException, DSOutOfServiceException
	{
		List<Integer> active = getActiveChannels();
		for (int i = 0; i < getPixelsDimensionsC(); i++) 
			setActive(i, false);
	
		Iterator<Integer> j = channels.iterator();
		while (j.hasNext()) 
			setActive(j.next(), true);
		BufferedImage img;
		retry = 0;
        if (isCompressed()) 
        	img = renderProjectedCompressed(startZ, endZ, stepping, type);
        else img = renderProjectedUncompressed(startZ, endZ, stepping, type);
        //reset
        j = active.iterator();
        while (j.hasNext()) 
			setActive(j.next(), true);
        return img;
	}


	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#copyRenderingSettings(RndProxyDef, List)
	 */
	public void copyRenderingSettings(RndProxyDef rndToCopy,
							List<Integer> indexes) 
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndDef == null)
			throw new IllegalArgumentException("No rendering settings to set");
		setModel(rndToCopy.getColorModel());
		setCodomainInterval(rndToCopy.getCdStart(), rndToCopy.getCdEnd());
		setQuantumStrategy(rndToCopy.getBitResolution());
		int defaultT = rndToCopy.getDefaultT();
		int maxT = getPixelsDimensionsT();
		if (defaultT >= 0 && defaultT < maxT)
			setDefaultT(rndToCopy.getDefaultT());
		ChannelBindingsProxy c;
		Iterator<Integer> j = indexes.iterator();
		Integer index;
		int k = 0;
		while (j.hasNext()) {
			index = j.next();
			c = rndToCopy.getChannel(index);
			if (c != null) {
				setRGBA(k, c.getRGBA());
				setChannelWindow(k, c.getInputStart(), c.getInputEnd());
				setQuantizationMap(k, c.getFamily(),
								c.getCurveCoefficient(),
									c.isNoiseReduction());
				setActive(k, c.isActive());
			}
			k++;
		}
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getActiveChannels()
	 */
	public List<Integer> getActiveChannels()
	{
		List<Integer> active = new ArrayList<Integer>();
		for (int i = 0; i < getPixelsDimensionsC(); i++)
			if (isActive(i)) active.add(Integer.valueOf(i));
		return active;
	}

	/** 
         * Implemented as specified by {@link RenderingControl}.
         * @see RenderingControl#isSameSettings(RndProxyDef, boolean)
         */
        public boolean isSameSettings(RndProxyDef def, boolean checkPlane)
        {
            return isSameSettings(def, checkPlane, false);
        }
        
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#isSameSettings(RndProxyDef, boolean, boolean)
	 */
	public boolean isSameSettings(RndProxyDef def, boolean checkPlane, boolean checkInactiveChannels)
	{
		if (def == null) return false;
		if (checkPlane) {
			if (def.getDefaultZ() != getDefaultZ()) return false;
			if (def.getDefaultT() != getDefaultT()) return false;
		}
		if (def.getBitResolution() != getBitResolution()) return false;
		if (def.getCdEnd() != getCodomainEnd()) return false;
		if (def.getCdStart() != getCodomainStart()) return false;
		if (!def.getColorModel().equals(getModel())) return false;
		if (def.getNumberOfChannels() != getPixelsDimensionsC()) return false;
		ChannelBindingsProxy channel;
		int[] rgba;
		Color color;
		Map<Integer, ChannelBindingsProxy> oldChannels =
			new HashMap<Integer, ChannelBindingsProxy>();
		for (int i = 0; i < getPixelsDimensionsC(); i++) {
			channel = def.getChannel(i);
			if (channel.isActive()) {
				oldChannels.put(i, channel);
			}	
		}
		
		List<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < getPixelsDimensionsC(); i++) {
			if (isActive(i)) {
				indexes.add(i);
			}
		}
		
		if (indexes.size() != oldChannels.size()) return false;
		
		if(checkInactiveChannels) {
                    for (int i = 0; i < getPixelsDimensionsC(); i++) {
                        channel = def.getChannel(i);
                        oldChannels.put(i, channel);
                    }
        
                    for (int i = 0; i < getPixelsDimensionsC(); i++) {
                        indexes.add(i);
                    }
		}
		
		Iterator<Integer> j = oldChannels.keySet().iterator();
		int i;
		while (j.hasNext()) {
			i = j.next();
			if (!(indexes.contains(i))) return false;
			channel = oldChannels.get(i);
			if (channel.getInputStart() != getChannelWindowStart(i))
				return false;
			if (channel.getInputEnd() != getChannelWindowEnd(i))
				return false;
			if (channel.getCurveCoefficient() != getChannelCurveCoefficient(i))
				return false;
			if (!channel.getFamily().equals(getChannelFamily(i)))
				return false;
			if (channel.isNoiseReduction() != getChannelNoiseReduction(i))
				return false;
			rgba = channel.getRGBA();
			color = getRGBA(i);
			if (rgba[0] != color.getRed()) return false;
			if (rgba[1] != color.getGreen()) return false;
			if (rgba[2] != color.getBlue()) return false;
			if (rgba[3] != color.getAlpha()) return false;
		}
		return true;
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getPixelsID()
	 */
	public long getPixelsID() { return pixs.getId().getValue(); }

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#isActiveImageRGB(List)
	 */
	public boolean isMappedImageRGB(List channels)
	{
		if (channels == null) channels = getActiveChannels();
		if (channels.size() == 0) return false;
		Set<Integer> rgb = new HashSet<Integer>();
    	int cIndex;
    	int index;
		Iterator i = channels.iterator();
    	while (i.hasNext()) {
			index = (Integer) i.next();
			cIndex = colourIndex(index);
			if (cIndex != NON_PRIMARY_INDEX) {
				if (rgb.contains(cIndex)) return false;
				else rgb.add(cIndex);
			} else return false;
		}
    	return true;
	}
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#setOverlays(long, Map)
	 */
	public void setOverlays(long tableID, Map<Long, Integer> overlays)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (tableID < 0) return;
		try {
			invalidateCache();
			//servant.setOverlays(omero.rtypes.rlong(tableID), 
			//		pixs.getImage().getId(), overlays);
		} catch (Exception e) {
			handleException(e, ERROR+"overlays.");
		}
		
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getResolutionLevels()
	 */
	public int getResolutionLevels()
	{
		try {
			if (resolutionLevels < 0)
				resolutionLevels = servant.getResolutionLevels();
		} catch (Exception e) {
			resolutionLevels = 1;
		}
		return resolutionLevels;
	}
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getSelectedResolutionLevel()
	 */
	public int getSelectedResolutionLevel()
	{
		try {
			if (selectedResolutionLevel < 0)
				selectedResolutionLevel = servant.getResolutionLevel();
		} catch (Exception e) {
			selectedResolutionLevel = 0;
		}
		return selectedResolutionLevel;
	}
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#setSelectedResolutionLevel(int)
	 */
	public void setSelectedResolutionLevel(int level)
		throws RenderingServiceException, DSOutOfServiceException
	{
		tileSize = null;
		if (level > getResolutionLevels())
			level = getResolutionLevels();
		isSessionAlive();
		try {
			servant.setResolutionLevel(level);
			selectedResolutionLevel = level;
			Iterator<RenderingControl> j = slaves.iterator();
			while (j.hasNext())
				j.next().setSelectedResolutionLevel(level);
		} catch (Exception e) {
			handleException(e, ERROR+" resolution level: "+level);
		}
	}
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getTileSize()
	 */
	public Dimension getTileSize()
		throws RenderingServiceException, DSOutOfServiceException
	{
		try {
			if (tileSize == null) {
				int[] values = servant.getTileSize();
				tileSize = new Dimension(values[0], values[1]);
			}
		} catch (Exception e) {
			handleException(e, "An error occurred while retrieving " +
					"the tile size.");
		}
		return tileSize;
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#isBigImage()
	 */
	public boolean isBigImage()
	{
		if (bigImage != null) return bigImage.booleanValue();
		try {
			bigImage = servant.requiresPixelsPyramid();
			return bigImage.booleanValue();
		} catch (Exception e) {}
		return false;
	}
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#isBigImage()
	 */
	public List<RenderingControl> getSlaves() { return slaves; }
	
	/** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#isShutDown()
	 */
    public boolean isShutDown() { return shutDown; }

    /** 
	 * Implemented as specified by {@link RenderingControl}.
	 * @see RenderingControl#getResolutionDescriptions()
	 */
    public List<ResolutionLevel> getResolutionDescriptions()
    		throws RenderingServiceException, DSOutOfServiceException
    {
    	List<ResolutionLevel> levels = new ArrayList<ResolutionLevel>();
    	Dimension d;
    	int sizeX = getPixelsDimensionsX();
    	int sizeY = getPixelsDimensionsY();
    	if (!isBigImage()) {
    		d = new Dimension(sizeX, sizeY);
    		levels.add(new ResolutionLevel(0, d, d));
    		return levels;
    	}
    	try {
			ResolutionDescription[] v = servant.getResolutionDescriptions();
    		ResolutionLevel level;
    		ResolutionDescription r;
    		int n = v.length-1;
			for (int i = n; i >= 0; i--) {
    			r = v[i];
				setSelectedResolutionLevel(n-i);
				d = new Dimension(r.sizeX, r.sizeY);
				level = new ResolutionLevel(n-i, getTileSize(), d);
				level.setRatio((double) r.sizeX/sizeX,
						(double) r.sizeY/sizeY);
				levels.add(level);
			}
		} catch (Exception e) {
			handleException(e, "An error occurred while retrieving " +
					"the resolutions.");
		}
    	return levels;
    }

}
