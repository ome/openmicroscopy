/*
 * org.openmicroscopy.shoola.env.rnd.RenderingControlProxy
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

package org.openmicroscopy.shoola.env.rnd;

//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;


//Third-party libraries
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

//Application-internal dependencies
import ome.conditions.RemovedSessionException;
import ome.conditions.SessionTimeoutException;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.CodomainMapContext;
import ome.model.display.QuantumDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.cache.CacheService;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.image.geom.Factory;



/** 
 * UI-side implementation of the {@link RenderingControl} interface.
 * Runs in the Swing thread.
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
class RenderingControlProxy
	implements RenderingControl
{
 
	/** Default error message. */
	private static final String	ERROR = "An error occured while trying to " +
										"set the ";
	
    /** The dimensions in microns of a pixel. */
    private final PixelsDimensions  pixDims;
    
    /** List of supported families. */
    private final List              families;
    
    /** List of supported models. */
    private final List              models;
    
    /** The pixels set to render. */
    private final Pixels            pixs;
    
    /** Reference to service to render pixels set. */
    private RenderingEngine         servant;
    
    /** The cache containing XY images. */
    //private XYCache                 xyCache;
    
    /** The id of the cache associated to this rpoxy. */
    private int						cacheID;
    
    /** The channel metadata. */
    private ChannelMetadata[]       metadata;
    
    /** Local copy of the rendering settings used to speed-up the client. */
    private RndProxyDef             rndDef;
    
    /** Indicates if the compression level. */
    private int						compression;
    
    /** Helper reference to the registry. */
    private Registry				context;
    
    /** The size of the cache. */
    private int						cacheSize;
    
    /** The size of the image. */
    private int						imageSize;
    
    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Methods in this class are required to fill in a meaningful context
     * message.
     * 
     * @param e			The exception.
     * @param message	The context message.  
     * @param message
     * @throws RenderingServiceException A rendering problem
     * @throws DSOutOfServiceException A connection problem.
     */
    private void handleException(Throwable e, String message)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	//Throwable cause = e.getCause();
    	if (e instanceof RemovedSessionException || 
    			e instanceof SessionTimeoutException) {
    		shutDown();
    		throw new DSOutOfServiceException(message+"\n\n"+
					printErrorText(e), e);
    	}
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
     * @param pd    The specified {@link PlaneDef plane definition}.
     * @return The corresponding bufferedImage.
     */
    private Object getFromCache(PlaneDef pd)
    {
        // We only cache XY images.
    	/*
        if (pd.getSlice() == PlaneDef.XY && xyCache != null) {  
        	return xyCache.extract(pd);
        }
        */
    	if (pd.getSlice() == PlaneDef.XY && cacheID >= 0) {
    		return context.getCacheService().getElement(cacheID, pd);
    	}
        return null;
    }
    
    /**
     * Caches the specified image if it corresponds to an XYPlane.
     * 
     * @param pd    	The plane definition.
     * @param object	The buffered image to cache or the bytes array.
     */
    private void cache(PlaneDef pd, Object object)
    {
        if (pd.getSlice() == PlaneDef.XY) {
            //We only cache XY images.
            //if (xyCache != null) xyCache.add(pd, object);
        	if (cacheID >= 0) {
        		context.getCacheService().addElement(cacheID, pd, object);
        	}
        	
        }
    }
    
    /** Clears the cache. */
    private void invalidateCache()
    {
        //if (xyCache != null) xyCache.clear();
    	if (cacheID >= 0) context.getCacheService().clearCache(cacheID);
    }
    
    /** Clears the cache and releases memory. */
    private void eraseCache()
    {
    	invalidateCache();
    	context.getCacheService().removeCache(cacheID);
    	
    }
    
    /**
     * Initializes the cache for the specified plane.
     * 
     * @param pDef The plane of reference.
     */
    private void initializeCache(PlaneDef pDef)
    {
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
    	if (pDef.getSlice() == PlaneDef.XY) 
    		cacheID = context.getCacheService().createCache(
					CacheService.IN_MEMORY, cacheSize/imageSize);
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
	 * @param red   The red component in the range [0, 255] in the default sRGB
	 * 				space.
	 * @param green The green component in the range [0, 255] in the default 
	 * 				sRGB space.
	 * @param blue  The blue component in the range [0, 255] in the default sRGB
	 * 				space.
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
	 * @param red   The red component in the range [0, 255] in the default sRGB
	 * 				space.
	 * @param green The green component in the range [0, 255] in the default 
	 * 				sRGB space.
	 * @param blue  The blue component in the range [0, 255] in the default sRGB
	 * 				space.
	 * @return See above.
	 */
	private boolean isRightChannelColor(int index, int red, int green, int blue)
	{
		int[] rgba = rndDef.getChannel(index).getRGBA();
		return (rgba[0] == red && rgba[1] == green && rgba[2] == blue);
	}
	
    /** Initializes the cached rendering settings to speed up process. */
    private void initialize()
    {
    	rndDef.setTypeSigned(servant.isPixelsTypeSigned());
        rndDef.setDefaultZ(servant.getDefaultZ());
        rndDef.setDefaultT(servant.getDefaultT());
        QuantumDef qDef = servant.getQuantumDef();
        rndDef.setBitResolution(qDef.getBitResolution().intValue());
        rndDef.setColorModel(servant.getModel().getValue());
        rndDef.setCodomain(qDef.getCdStart().intValue(), 
                            qDef.getCdEnd().intValue());
        
        ChannelBindingsProxy cb;
        for (int i = 0; i < pixs.getSizeC().intValue(); i++) {
            cb = rndDef.getChannel(i);
            if (cb == null) {
                cb = new ChannelBindingsProxy();
                rndDef.setChannel(i, cb);
            }
            cb.setActive(servant.isActive(i));
            cb.setInterval(servant.getChannelWindowStart(i), 
                            servant.getChannelWindowEnd(i));
            cb.setQuantization(servant.getChannelFamily(i).getValue(), 
                    servant.getChannelCurveCoefficient(i), 
                    servant.getChannelNoiseReduction(i));
            cb.setRGBA(servant.getRGBA(i));
            cb.setLowerBound(servant.getPixelsTypeLowerBound(i));
            cb.setUpperBound(servant.getPixelsTypeUpperBound(i));
        }
    }
    
    
    private void tmpSolutionForNoiseReduction()
    {
    	 //DOES NOTHING TMP SOLUTION.
        try {
        	for (int i = 0; i < pixs.getSizeC().intValue(); i++) {
    			setQuantizationMap(i, getChannelFamily(i), 
    					getChannelCurveCoefficient(i), false);
    		}
		} catch (Exception e) {
			
		}
    }
    
    /** 
     * Sets the color.
     * 
     * @param w 	The index of the channel.
     * @param rgba	The color to set.
     * @throws RenderingServiceException	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
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
     *            or <i>Z</i> axes.
	 * @return See above.
	 * @throws RenderingServiceException 	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	private BufferedImage renderPlaneCompressed(PlaneDef pDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		//Need to adjust the cache.
		Object array = getFromCache(pDef);
		try {
			if (array != null) 
				return ImageIO.read(new ByteArrayInputStream((byte[]) array));
			
			byte[] values = servant.renderCompressed(pDef);
			imageSize = values.length;
			initializeCache(pDef);
			cache(pDef, values);
			JPEGImageDecoder decoder = 
				JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(values));
			return decoder.decodeAsBufferedImage();
		} catch (Throwable e) {
			handleException(e, ERROR+"cannot render the compressed image.");
		}
		return null;
	}
	
	/**
	 * Renders the image without compression.
	 * 
	 * @param pDef A plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *            or <i>Z</i> axes.
	 * @return See above.
	 * @throws RenderingServiceException 	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	private BufferedImage renderPlaneUncompressed(PlaneDef pDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		//See if the requested image is in cache.
        BufferedImage img = (BufferedImage) getFromCache(pDef);
        if (img != null) return img;
        try {
            int[] buf = servant.renderAsPackedInt(pDef);
            int sizeX1, sizeX2;
            switch (pDef.getSlice()) {
                case PlaneDef.XZ:
                    sizeX1 = pixs.getSizeX().intValue();
                    sizeX2 = pixs.getSizeZ().intValue();
                    break;
                case PlaneDef.ZY:
                    sizeX1 = pixs.getSizeZ().intValue();
                    sizeX2 = pixs.getSizeY().intValue();
                    break;
                case PlaneDef.XY:
                default:
                    sizeX1 = pixs.getSizeX().intValue();
                    sizeX2 = pixs.getSizeY().intValue();
                    break;
            }
            imageSize = 3*buf.length;
            initializeCache(pDef);
            img = Factory.createImage(buf, 32, sizeX1, sizeX2);
            cache(pDef, img);
		} catch (Throwable e) {
			handleException(e, ERROR+"cannot render the plane.");
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
	 * @throws RenderingServiceException 	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	private BufferedImage renderProjectedCompressed(int startZ, int endZ, 
			                               int stepping, int type)
		throws RenderingServiceException, DSOutOfServiceException
	{
		try {
			
			byte[] values = servant.renderProjectedCompressed(type, 
					getDefaultT(), stepping, startZ, endZ);
			
			JPEGImageDecoder decoder = 
				JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(values));
			return decoder.decodeAsBufferedImage();
		} catch (Throwable e) {
			handleException(e, ERROR+"cannot render projected selection.");
		}
		return null;
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
	 * @throws RenderingServiceException 	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	private BufferedImage renderProjectedUncompressed(int startZ, int endZ, 
            int stepping, int type)
		throws RenderingServiceException, DSOutOfServiceException
	{
        BufferedImage img = null;
        try {
            int[] buf = servant.renderProjectedAsPackedInt(type, 
					getDefaultT(), stepping, startZ, endZ);
            int sizeX1 = pixs.getSizeX().intValue();
            int sizeX2 = pixs.getSizeY().intValue();
            img = Factory.createImage(buf, 32, sizeX1, sizeX2);
		} catch (Throwable e) {
			handleException(e, ERROR+"cannot render projected selection.");
		}
        
        return img;
	}
	
    /**
     * Creates a new instance.
     * 
     * @param context		Helper reference to the registry.
     * @param re   			The service to render a pixels set.
     *                  	Mustn't be <code>null</code>.
     * @param pixels   		The pixels set.
     *                  	Mustn't be <code>null</code>.
     * @param m         	The channel metadata. 
     * @param compression  	Pass <code>0</code> if no compression otherwise 
	 * 						pass the compression used.
	 * @param rndDef		Local copy of the rendering settings used to 
	 * 						speed-up the client.
	 * @param cacheSize		The desired size of the cache.
     */
    RenderingControlProxy(Registry context, RenderingEngine re, Pixels pixels,
    					List m, int compression, RndProxyDef rndDef, 
    					int cacheSize)
    {
        if (re == null)
            throw new NullPointerException("No rendering engine.");
        if (pixels == null)
            throw new NullPointerException("No pixels set.");
        if (context == null)
            throw new NullPointerException("No registry.");
        this.context = context;
        this.cacheSize = cacheSize;
        servant = re;
        pixs = pixels;//servant.getPixels();
        this.pixDims = pixels.getPixelsDimensions();
        families = servant.getAvailableFamilies(); 
        models = servant.getAvailableModels();
        cacheID = -1;
        imageSize = 1;
        this.compression = compression;
        if (rndDef == null) {
        	this.rndDef = new RndProxyDef();
        	initialize();
        } else {
        	this.rndDef = rndDef;
        	ChannelBindingsProxy cb;
        	for (int i = 0; i < pixs.getSizeC().intValue(); i++) {
                cb = rndDef.getChannel(i);
                cb.setLowerBound(servant.getPixelsTypeLowerBound(i));
                cb.setUpperBound(servant.getPixelsTypeUpperBound(i));
            }
        }
       
        //tmpSolutionForNoiseReduction();
        metadata = new ChannelMetadata[m.size()];
        Iterator i = m.iterator();
        ChannelMetadata cm;
        while (i.hasNext()) {
        	cm = (ChannelMetadata) i.next();
            metadata[cm.getIndex()] = cm;
        }
    }

    /**
     * Resets the rendering engine.
     * 
     * @param servant	The value to set.
     * @param rndDef	Local copy of the rendering settings used to 
	 * 					speed-up the client.
     */
    void resetRenderingEngine(RenderingEngine servant, RndProxyDef rndDef)
    {
    	if (servant == null) return;
    	DataServicesFactory.isSessionAlive(context);
    	invalidateCache();
    	this.servant = servant;
    	if (rndDef == null) {
        	initialize();
        } else {
        	this.rndDef = rndDef;
        	ChannelBindingsProxy cb;
        	for (int i = 0; i < pixs.getSizeC().intValue(); i++) {
                cb = rndDef.getChannel(i);
                cb.setLowerBound(servant.getPixelsTypeLowerBound(i));
                cb.setUpperBound(servant.getPixelsTypeUpperBound(i));
            }
        }
    }
    
    /**
     * Reloads the rendering engine.
     * 
     * @param servant The value to set.
     */
    void setRenderingEngine(RenderingEngine servant)
    {
    	if (servant == null) return;
    	DataServicesFactory.isSessionAlive(context);
    	this.servant = servant;
    	
    	// reset default of the rendering engine.
    	if (rndDef == null) return;
    	servant.setDefaultZ(rndDef.getDefaultZ());
    	servant.setDefaultT(rndDef.getDefaultT());
    	servant.setQuantumStrategy(rndDef.getBitResolution());
    	Iterator k = models.iterator();
        RenderingModel model;
        String value = rndDef.getColorModel();
        while (k.hasNext()) {
            model= (RenderingModel) k.next();
            if (model.getValue().equals(value)) 
                servant.setModel(model); 
        }
    	servant.setCodomainInterval(rndDef.getCdStart(), rndDef.getCdEnd());
    	
        ChannelBindingsProxy cb;
        
        Family family;
        int[] rgba;
        for (int i = 0; i < pixs.getSizeC().intValue(); i++) {
            cb = rndDef.getChannel(i);
            servant.setActive(i, cb.isActive());
            servant.setChannelWindow(i, cb.getInputStart(), cb.getInputEnd());
            k = families.iterator();
            value = cb.getFamily();
            while (k.hasNext()) {
                family= (Family) k.next();
                if (family.getValue().equals(value)) {
                	servant.setQuantizationMap(i, family, 
                			cb.getCurveCoefficient(), cb.isNoiseReduction());
                  
                }
            }
            rgba = cb.getRGBA();
            servant.setRGBA(i, rgba[0], rgba[1], rgba[2], rgba[3]);
        }
    }
        
    /** Shuts down the service. */
    void shutDown()
    { 
    	try {
    		servant.close();
    		//remove the cache.
    		context.getCacheService().removeCache(cacheID);
		} catch (Exception e) {} 
    }
    
    /** Resets the size of the cache.
     * 
     * @param size The size, in bytes, of the cache.
     */
    void setCacheSize(int size)
    {
    	if (imageSize == 0) imageSize = 1;
    	context.getCacheService().setCacheSize(cacheID, size/imageSize);
    }
    
    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setModel(String)
     */
    public void setModel(String value)
    	throws RenderingServiceException, DSOutOfServiceException
    { 
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		Iterator i = models.iterator();
            RenderingModel model;
            while (i.hasNext()) {
                model= (RenderingModel) i.next();
                if (model.getValue().equals(value)) {
                    servant.setModel(model); 
                    rndDef.setColorModel(value);
                    invalidateCache();
                }
            }
		} catch (Exception e) {
			rndDef.setColorModel(value);
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
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		servant.setDefaultZ(z);
            rndDef.setDefaultZ(z);
		} catch (Exception e) {
			rndDef.setDefaultZ(z);
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
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		servant.setDefaultT(t);
            rndDef.setDefaultT(t);
		} catch (Exception e) {
			rndDef.setDefaultT(t);
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
        //TODO: need to convert value.
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		checkBitResolution(bitResolution);
            servant.setQuantumStrategy(bitResolution);
            rndDef.setBitResolution(bitResolution);
            invalidateCache();
		} catch (Exception e) {
			rndDef.setBitResolution(bitResolution);
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
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		servant.setCodomainInterval(start, end);
            rndDef.setCodomain(start, end);
            invalidateCache();
		} catch (Exception e) {
			rndDef.setCodomain(start, end);
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
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		List list = servant.getAvailableFamilies();
            Iterator i = list.iterator();
            Family family;
            while (i.hasNext()) {
                family= (Family) i.next();
                if (family.getValue().equals(value)) {
                    servant.setQuantizationMap(w, family, coefficient, 
                                                noiseReduction);
                    rndDef.getChannel(w).setQuantization(value, coefficient, 
                                                noiseReduction);
                    invalidateCache();
                }
            }
		} catch (Exception e) {
			rndDef.getChannel(w).setQuantization(value, coefficient, 
                    noiseReduction);
			handleException(e, ERROR+"quantization map.");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelFamily(int)
     */
    public String getChannelFamily(int w)
    { 
        return rndDef.getChannel(w).getFamily();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelNoiseReduction(int)
     */
    public boolean getChannelNoiseReduction(int w)
    {
        return rndDef.getChannel(w).isNoiseReduction();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelCurveCoefficient(int)
     */
    public double getChannelCurveCoefficient(int w)
    {
        return rndDef.getChannel(w).getCurveCoefficient();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setChannelWindow(int, double, double)
     */
    public void setChannelWindow(int w, double start, double end)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		servant.setChannelWindow(w, start, end);
            rndDef.getChannel(w).setInterval(start, end);
            invalidateCache();
		} catch (Exception e) {
			rndDef.getChannel(w).setInterval(start, end);
			handleException(e, ERROR+"input channel for: "+w+".");
		}  
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelWindowStart(int)
     */
    public double getChannelWindowStart(int w)
    {
        return rndDef.getChannel(w).getInputStart();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelWindowEnd(int)
     */
    public double getChannelWindowEnd(int w)
    {
        return rndDef.getChannel(w).getInputEnd();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setRGBA(int, Color)
     */
    public void setRGBA(int w, Color c)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		servant.setRGBA(w, c.getRed(), c.getGreen(), c.getBlue(), 
    						c.getAlpha());
    		rndDef.getChannel(w).setRGBA(c.getRed(), c.getGreen(), c.getBlue(),
    						c.getAlpha());
    		invalidateCache();
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
        int[] rgba = rndDef.getChannel(w).getRGBA();
        return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setActive(int, boolean)
     */
    public void setActive(int w, boolean active)
    	throws RenderingServiceException, DSOutOfServiceException
    { 
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		servant.setActive(w, active);
            rndDef.getChannel(w).setActive(active);
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
        return rndDef.getChannel(w).isActive();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#addCodomainMap(CodomainMapContext)
     */
    public void addCodomainMap(CodomainMapContext mapCtx)
    	throws RenderingServiceException, DSOutOfServiceException
    {
    	//servant.addCodomainMap(mapCtx);
        invalidateCache();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#updateCodomainMap(CodomainMapContext)
     */
    public void updateCodomainMap(CodomainMapContext mapCtx)
    	throws RenderingServiceException, DSOutOfServiceException
    {
        //servant.updateCodomainMap(mapCtx);
        invalidateCache();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#removeCodomainMap(CodomainMapContext)
     */
    public void removeCodomainMap(CodomainMapContext mapCtx)
    	throws RenderingServiceException, DSOutOfServiceException
    {
        //servant.removeCodomainMap(mapCtx);
        invalidateCache();
    }

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
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		servant.saveCurrentSettings();
    		return rndDef;
		} catch (Throwable e) {
			handleException(e, ERROR+"save current settings.");
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
    	DataServicesFactory.isSessionAlive(context);
    	try {
    		 servant.resetDefaultsNoSave();
    		 setModel(HSB);
    		 invalidateCache();
    		 initialize();
    		 //tmpSolutionForNoiseReduction();
		} catch (Throwable e) {
			handleException(e, ERROR+"default settings.");
		}
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getPixelsSizeX()
     */
    public float getPixelsSizeX()
    {
        if (pixDims.getSizeX() == null) return 1;
        return pixDims.getSizeX().floatValue();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getPixelsSizeY()
     */
    public float getPixelsSizeY()
    {
        if (pixDims.getSizeY() == null) return 1;
        return pixDims.getSizeY().floatValue();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getPixelsSizeZ()
     */
    public float getPixelsSizeZ()
    {
        if (pixDims.getSizeY() == null) return 1;
        return pixDims.getSizeZ().floatValue();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getPixelsDimensionsX()
     */
    public int getPixelsDimensionsX() { return pixs.getSizeX().intValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getPixelsDimensionsY()
     */
    public int getPixelsDimensionsY() { return pixs.getSizeY().intValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getPixelsDimensionsZ()
     */
    public int getPixelsDimensionsZ() { return pixs.getSizeZ().intValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getPixelsDimensionsT()
     */
    public int getPixelsDimensionsT() { return pixs.getSizeT().intValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getPixelsDimensionsC()
     */
    public int getPixelsDimensionsC() { return pixs.getSizeC().intValue(); }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getFamilies()
     */
    public List getFamilies()
    { 
        List<String> l = new ArrayList<String>(families.size());
        Iterator i= families.iterator();
        while (i.hasNext())
            l.add(((Family) i.next()).getValue());
        return l;
    }
    
    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelData(int)
     */
    public ChannelMetadata getChannelData(int w) { return metadata[w]; }
    
    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelData()
     */
    public ChannelMetadata[] getChannelData() { return metadata; }

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

	/** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#resetMappingSettings(RndProxyDef)
     */
	public void resetMappingSettings(RndProxyDef rndDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndDef == null)
			throw new IllegalArgumentException("No rendering settings to " +
					"set");
		if (rndDef.getNumberOfChannels() != getPixelsDimensionsC())
			throw new IllegalArgumentException("Rendering settings not " +
					"compatible.");
		DataServicesFactory.isSessionAlive(context);
		setCodomainInterval(rndDef.getCdStart(), rndDef.getCdEnd());
		setQuantumStrategy(rndDef.getBitResolution());
		ChannelBindingsProxy c;
		for (int i = 0; i < getPixelsDimensionsC(); i++) {
			c = rndDef.getChannel(i);
			if (c != null) {
				setChannelWindow(i, c.getInputStart(), c.getInputEnd());
				setQuantizationMap(i, c.getFamily(), c.getCurveCoefficient(), 
									c.isNoiseReduction());
			}		
		}
	}
	
	/** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#resetSettings(RndProxyDef)
     */
	public void resetSettings(RndProxyDef rndDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (rndDef == null)
			throw new IllegalArgumentException("No rendering settings to " +
					"set");
		if (rndDef.getNumberOfChannels() != getPixelsDimensionsC())
			throw new IllegalArgumentException("Rendering settings not " +
					"compatible.");
		DataServicesFactory.isSessionAlive(context);
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
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}. 
	 * @see RenderingControl#getPixelsTypeLowerBound(int)
	 */
	public double getPixelsTypeLowerBound(int w)
	{
		return rndDef.getChannel(w).getLowerBound();
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}. 
	 * @see RenderingControl#getPixelsTypeUpperBound(int)
	 */
	public double getPixelsTypeUpperBound(int w)
	{
		return rndDef.getChannel(w).getUpperBound();
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}. 
	 * @see RenderingControl#isPixelsTypeSigned()
	 */
	public boolean isPixelsTypeSigned() { return rndDef.isTypeSigned(); }
	
	/** 
	 * Implemented as specified by {@link RenderingControl}. 
	 * @see RenderingControl#validatePixels(Pixels)
	 */
	public boolean validatePixels(Pixels pixels)
	{
		if (pixels == null) return false;
		DataServicesFactory.isSessionAlive(context);
		if (getPixelsDimensionsC() != pixels.getSizeC().intValue())
			return false;
		if (getPixelsDimensionsY() != pixels.getSizeY().intValue())
			return false;
		if (getPixelsDimensionsX() != pixels.getSizeX().intValue())
			return false;
		String s = pixels.getPixelsType().getValue();
		String value = pixs.getPixelsType().getValue();
		if (!value.equals(s)) return false;
		return true;
	}
	
	/** 
	 * Implemented as specified by {@link RenderingControl}. 
	 * @see RenderingControl#renderPlane(PlaneDef)
	 */
    public BufferedImage renderPlane(PlaneDef pDef)
    	throws RenderingServiceException, DSOutOfServiceException
    {
        if (pDef == null) 
            throw new IllegalArgumentException("Plane def cannot be null.");
        DataServicesFactory.isSessionAlive(context);
        if (isCompressed()) return renderPlaneCompressed(pDef);
        return renderPlaneUncompressed(pDef);
    }
    
    /** 
	 * Implemented as specified by {@link RenderingControl}. 
	 * @see RenderingControl#setCompression(int)
	 */
	public void setCompression(int compression)
	{
		DataServicesFactory.isSessionAlive(context);
		this.compression = compression;
		float f = PixelsServicesFactory.getCompressionQuality(compression);
		servant.setCompressionLevel(f);
		eraseCache();
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
		DataServicesFactory.isSessionAlive(context);
    	try {
    		 servant.resetDefaultsNoSave();
    		 setModel(HSB);
    		 List list = servant.getAvailableFamilies();
    		 ChannelMetadata m;
    		 Iterator j;
    		 Family family;
    		 for (int i = 0; i < pixs.getSizeC().intValue(); i++) {
    			 j = list.iterator();
    			 while (j.hasNext()) {
    				 family= (Family) j.next();
    				 if (family.getValue().equals(getChannelFamily(i))) {
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
		} catch (Throwable e) {
			handleException(e, ERROR+"default settings.");
		}
	}

	/** 
	 * Implemented as specified by {@link RenderingControl}. 
	 * @see RenderingControl#renderProjected(int, int, int, int)
	 */
	public BufferedImage renderProjected(int startZ, int endZ, int stepping, 
			                           int type, List<Integer> channels) 
		throws RenderingServiceException, DSOutOfServiceException
	{
		DataServicesFactory.isSessionAlive(context);
		List<Integer> active = getActiveChannels();
		for (int i = 0; i < getPixelsDimensionsC(); i++) 
			setActive(i, false);
	
		Iterator<Integer> j = channels.iterator();
		while (j.hasNext()) 
			setActive(j.next(), true);
		BufferedImage img;

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
		DataServicesFactory.isSessionAlive(context);
		if (rndDef == null)
			throw new IllegalArgumentException("No rendering settings to " +
					"set");
		DataServicesFactory.isSessionAlive(context);
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
		for (int i = 0; i < getPixelsDimensionsC(); i++) {
			if (isActive(i)) active.add(new Integer(i));
		}
		return active;
	}
	
}
