/*
 * org.openmicroscopy.shoola.env.rnd.RenderingControlProxy
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
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sun.awt.image.IntegerInterleavedRaster;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.CodomainMapContext;
import ome.model.display.QuantumDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;


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
    
    /** The size of an XY image. */
    private int                     xyImgSize;
    
    /** The cache containing XY images. */
    private XYCache                 xyCache;
    
    /** The size of the cache. No caching if <= 0.*/
    private int                     sizeCache;
    
    private ChannelMetadata[]       metadata;
    
    private RndProxyDef             rndDef;
    
    /**
     * Returns the size of the cache.
     * 
     * @return See above.
     */
    private int getCacheSize()
    {
        if (sizeCache <= 0) sizeCache = 0;
        return sizeCache*1024*1024;
    }
    
    /**
     * Creates a new {@link BufferedImage} from the specified array of 
     * packed integers.
     * 
     * @param sizeX The size along the X-axis. 
     * @param sizeY The size along the Y-axis.
     * @param buf   The array of packed integers.
     * @return See above.
     */
    private BufferedImage createImage(int sizeX, int sizeY, int[] buf)
    {
        DataBuffer j2DBuf = new DataBufferInt(buf, sizeX*sizeY, 0); 
        SinglePixelPackedSampleModel sampleModel =
            new SinglePixelPackedSampleModel(
                      DataBuffer.TYPE_INT, sizeX, sizeY, sizeX,                                                                                                      
                      new int[] {
                          0x00ff0000,    // Red
                          0x0000ff00,    // Green
                          0x000000ff//,    // Blue
                      });
        WritableRaster raster = 
            new IntegerInterleavedRaster(sampleModel, j2DBuf, new Point(0, 0));
      
        ColorModel colorModel = new DirectColorModel(32,
                                        0x00ff0000,    // Red
                                        0x0000ff00,    // Green
                                        0x000000ff//,    // Blue
                                       );
        return new BufferedImage(colorModel, raster, false, null);
    }

    /**
     * Retrieves from the cache the buffered image representing the specified
     * plane definition. Note that only the images corresponding to an XY-plane
     * are cached.
     * 
     * @param pd    The specified {@link PlaneDef plane definition}.
     * @return The corresponding bufferedImage.
     */
    private BufferedImage getFromCache(PlaneDef pd)
    {
        BufferedImage img = null;
        if (pd.getSlice() == PlaneDef.XY) {  //We only cache XY images.
            
            if (xyCache != null) {
                img = xyCache.extract(pd);
            } else {
                //Okay, let's see if we can activate the xyCache. In order to 
                //do that, the dimensions of the pixels array and the xyImgSize
                //have to be available. 
                //This happens if at least one XY plane has been rendered.  
                //Note that doing remote calls upfront to eagerly instantiate 
                //the xyCache is in most cases a total waste: the client is 
                //likely to call getPixelsDims() before an image is ever 
                //rendered and until an XY plane is not requested it's pointless
                //to have a cache.
                if (xyImgSize != 0) {
                    int cacheSize = getCacheSize();
                    NavigationHistory nh = new NavigationHistory(
                                                  cacheSize/xyImgSize, 
                                                  getPixelsDimensionsZ(), 
                                                  getPixelsDimensionsT());
                    xyCache = new XYCache(cacheSize, xyImgSize, nh);
                }
            }
        }
        return img;
    }
    
    /**
     * Caches the specified image if it corresponds to an XYPlane.
     * 
     * @param pd    The plane definition.
     * @param img   The buffered image to cache.
     */
    private void cache(PlaneDef pd, BufferedImage img)
    {
        if (pd.getSlice() == PlaneDef.XY) {
            //We only cache XY images.
            if (xyCache != null) xyCache.add(pd, img);
        }
    }
    
    /** Clears the cache. */
    private void invalidateCache()
    {
        if (xyCache != null) xyCache.clear();
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
    
    /** Initializes the cached rendering settings to speed up process. */
    private void initialize()
    {
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
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param servant   The service to render a pixels set.
     *                  Mustn't be <code>null</code>.
     * @param pixDims   The dimensions in microns of the pixels set.
     *                  Mustn't be <code>null</code>.
     * @param m         The channel metadata.                
     * @param sizeCache The size of the cache.
     */
    RenderingControlProxy(RenderingEngine servant, PixelsDimensions pixDims,
                          List m, int sizeCache)
    {
        if (servant == null)
            throw new NullPointerException("No rendering engine.");
        if (pixDims == null)
            throw new NullPointerException("No pixels dimensions.");
        this.servant = servant;
        this.pixDims = pixDims;
        this.sizeCache = sizeCache;
        pixs = servant.getPixels();
        families = servant.getAvailableFamilies(); 
        models = servant.getAvailableModels();
        rndDef = new RndProxyDef();
        initialize();
        metadata = new ChannelMetadata[m.size()];
        Iterator i = m.iterator();
        int k = 0;
        while (i.hasNext()) {
            metadata[k] = (ChannelMetadata) i.next();
            k++;  
        }
    }

    /** 
     * Resets the size of the cache.
     * 
     * @param size The new size.
     */
    void resetCacheSize(int size)
    {
        if (xyCache != null) xyCache.resetCacheSize(size);
    }
    
    /**
     * Renders the specified {@link PlaneDef plane}.
     * 
     * @param pDef The plane to render
     * @return The rendered image.
     */
    synchronized BufferedImage render(PlaneDef pDef)
    {
        if (pDef == null) 
            throw new IllegalArgumentException("Plane def cannot be null.");
        //See if the requested image is in cache.
        
        BufferedImage img = getFromCache(pDef);
        if (img != null) return img;
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

        int[] buf = servant.renderAsPackedInt(pDef);
        img = createImage(sizeX1, sizeX2, buf);
        cache(pDef, img);
        return img;
    }
    
    /** Shuts down the service. */
    void shutDown() { servant.close(); }
    
    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setModel(String)
     */
    public void setModel(String value)
    { 
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
     }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getModel()
     */
    public String getModel()
    { 
        return rndDef.getColorModel();
        //return servant.getModel().getValue(); 
    }

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
    { 
        servant.setDefaultZ(z);
        rndDef.setDefaultZ(z);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setDefaultT(int)
     */
    public void setDefaultT(int t)
    { 
        servant.setDefaultT(t);
        rndDef.setDefaultT(t);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setQuantumStrategy(int)
     */
    public void setQuantumStrategy(int bitResolution)
    {
        //TODO: need to convert value.
        checkBitResolution(bitResolution);
        servant.setQuantumStrategy(bitResolution);
        rndDef.setBitResolution(bitResolution);
        invalidateCache();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setCodomainInterval(int, int)
     */
    public void setCodomainInterval(int start, int end)
    {
        servant.setCodomainInterval(start, end);
        invalidateCache();
        rndDef.setCodomain(start, end);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setQuantizationMap(int, String, double, boolean)
     */
    public void setQuantizationMap(int w, String value, double coefficient,
                                    boolean noiseReduction)
    {
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
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelFamily(int)
     */
    public String getChannelFamily(int w)
    { 
        return rndDef.getChannel(w).getFamily();
        //return servant.getChannelFamily(w).getValue();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelNoiseReduction(int)
     */
    public boolean getChannelNoiseReduction(int w)
    {
        return rndDef.getChannel(w).isNoiseReduction();
        //return servant.getChannelNoiseReduction(w);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelCurveCoefficient(int)
     */
    public double getChannelCurveCoefficient(int w)
    {
        return rndDef.getChannel(w).getCurveCoefficient();
        //return servant.getChannelCurveCoefficient(w);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setChannelWindow(int, double, double)
     */
    public void setChannelWindow(int w, double start, double end)
    {
        servant.setChannelWindow(w, start, end);
        rndDef.getChannel(w).setInterval(start, end);
        invalidateCache();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelWindowStart(int)
     */
    public double getChannelWindowStart(int w)
    {
        return rndDef.getChannel(w).getInputStart();
        //return servant.getChannelWindowStart(w);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getChannelWindowEnd(int)
     */
    public double getChannelWindowEnd(int w)
    {
        return rndDef.getChannel(w).getInputEnd();
        //return servant.getChannelWindowEnd(w);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setRGBA(int, Color)
     */
    public void setRGBA(int w, Color c)
    {
        servant.setRGBA(w, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        rndDef.getChannel(w).setRGBA(c.getRed(), c.getGreen(), c.getBlue(),
                                     c.getAlpha());
        invalidateCache();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#getRGBA(int)
     */
    public Color getRGBA(int w)
    {
        //int[] rgba = servant.getRGBA(w);
        int[] rgba = rndDef.getChannel(w).getRGBA();
        return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#setActive(int, boolean)
     */
    public void setActive(int w, boolean active)
    { 
        servant.setActive(w, active);
        rndDef.getChannel(w).setActive(active);
        invalidateCache();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#isActive(int)
     */
    public boolean isActive(int w)
    { 
        return rndDef.getChannel(w).isActive();
        //return servant.isActive(w);
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#addCodomainMap(CodomainMapContext)
     */
    public void addCodomainMap(CodomainMapContext mapCtx)
    {
        //servant.addCodomainMap(mapCtx);
        invalidateCache();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#updateCodomainMap(CodomainMapContext)
     */
    public void updateCodomainMap(CodomainMapContext mapCtx)
    {
        //servant.updateCodomainMap(mapCtx);
        invalidateCache();
    }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#removeCodomainMap(CodomainMapContext)
     */
    public void removeCodomainMap(CodomainMapContext mapCtx)
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
    public void saveCurrentSettings() { servant.saveCurrentSettings(); }

    /** 
     * Implemented as specified by {@link RenderingControl}. 
     * @see RenderingControl#resetDefaults()
     */
    public void resetDefaults()
    { 
        servant.resetDefaults();
        initialize();
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
        List l = new ArrayList(families.size());
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

}
