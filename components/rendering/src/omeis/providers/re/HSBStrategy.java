/*
 * omeis.providers.re.HSBStrategy
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

package omeis.providers.re;

//Java imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;//j.m
import java.util.concurrent.Executors;//j.m
import java.util.concurrent.Future;//j.m

//Third-party libraries

//Application-internal dependencies

//j.mimport ome.util.concur.tasks.CmdProcessor;
//j.mimport ome.util.concur.tasks.Future;

import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;

//j.mimport omeis.env.Env;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;

/** 
 * Transforms a plane within a given pixels set into an <i>RGB</i> image.
 * As many wavelengths (channels) as desired can contribute to the final image 
 * and each wavelength is mapped to a color.  All
 * this things are specified by the rendering context.
 * <p>When multiple wavelengths have to be combined into the final image (this
 * is the case if the rendering context specifies more than one active channel),
 * this strategy renders each wavelength in a separate thread &#151; this often
 * results in parallel rendering on multi-processor machines.</p>
 * <p>Thread-safety relies on the fact that the rendering context is not going
 * to change during the whole image rendering process.  (This is enforced by
 * the {@link RenderingEngineImpl}; in fact, while the <code>render</code> 
 * method executes, the whole component is locked.)</p>
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.10 $ $Date: 2005/06/22 17:09:48 $)
 * </small>
 * @since OME2.2
 */
class HSBStrategy
    extends RenderingStrategy
{
    
    /** 
     * The number of pixels on the <i>X1</i>-axis.
     * This is the <i>X</i>-axis in the case of an <i>XY</i> or <i>XZ</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>ZY</i> plane.
     */
    private int         sizeX1;
    
    /** 
     * The number of pixels on the X2-axis.
     * This is the <i>Y</i>-axis in the case of an <i>XY</i> or <i>ZY</i> plane.
     * Otherwise it is the <i>Z</i>-axis &#151; <i>XZ</i> plane. 
     */
    private int         sizeX2;
    
    /** The rendering context. */
    private Renderer    renderer;
    
    
    /** 
     * Initializes the <code>sizeX1</code> and <code>sizeX2</code> fields
     * according to the specified {@link PlaneDef#getSlice() slice}.
     * 
     * @param pd        Reference to the plane definition defined for the
     *                  strategy.
     * @param pixels    Dimensions of the pixels set.
     */
    private void initAxesSize(PlaneDef pd, Pixels pixels)
    {
        try {
            switch (pd.getSlice()) {
                case PlaneDef.XY:
                    sizeX1 = pixels.getSizeX().intValue(); // TODO int?
                    sizeX2 = pixels.getSizeY().intValue();
                    break;
                case PlaneDef.XZ:
                    sizeX1 = pixels.getSizeX().intValue();
                    sizeX2 = pixels.getSizeZ().intValue();
                    break;
                case PlaneDef.ZY:
                    sizeX1 = pixels.getSizeZ().intValue();
                    sizeX2 = pixels.getSizeY().intValue();
            }     
        } catch(NumberFormatException nfe) {   
            throw new RuntimeException("Invalid slice ID: "+pd.getSlice()+".", 
                                        nfe);
        } 
    }
    
    /**
     * Creates a rendering task for each active wavelength.
     * 
     * @param planeDef The plane to render.
     * @return An array containing the tasks.
     */
    private RenderHSBWaveTask[] makeRndTasks(PlaneDef planeDef)
    {
        ArrayList tasks = new ArrayList();
        
        //Get all objects we need to create the tasks. 
        Plane2D wData;
        ChannelBinding[] cBindings = 
                renderer.getChannelBindings();
        CodomainChain cc = renderer.getCodomainChain();
        Pixels metadata = renderer.getMetadata();
        PixelBuffer pixels = renderer.getPixels();
        QuantumManager qManager = renderer.getQuantumManager();
        RenderingStats performanceStats = renderer.getStats();
        RGBBuffer channelBuf;
        
        //Create a task for each active wavelength.
        for (int w = 0; w < cBindings.length; w++) {
            if (cBindings[w].getActive().booleanValue()) {
                //Allocate the RGB buffer for this wavelength.
                performanceStats.startMalloc();
                channelBuf = new RGBBuffer(sizeX1, sizeX2);
                performanceStats.endMalloc();
                
                //Get the raw data.
                performanceStats.startIO(w);
                wData = PlaneFactory.createPlane(planeDef, w, metadata, pixels);
                performanceStats.endIO(w);
                
                //Create a rendering task for this wavelength.
                tasks.add(new RenderHSBWaveTask(channelBuf, wData, 
                                                qManager.getStrategyFor(w), 
                                                cc, cBindings[w].getColor(), 
                                                sizeX1, sizeX2));
            }
        }
        
        //Turn the list into an array an return it.
        RenderHSBWaveTask[] t = new RenderHSBWaveTask[tasks.size()];
        return (RenderHSBWaveTask[]) tasks.toArray(t);
    }
    
    /**
     * Implemented as specified by superclass.
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
    RGBBuffer render(Renderer ctx, PlaneDef planeDef)
        throws IOException, QuantizationException
    {
        //Set the rendering context for the current invocation.
        renderer = ctx;
        RenderingStats performanceStats = renderer.getStats();
        
        //Initialize sizeX1 and sizeX2 according to the plane.
        initAxesSize(planeDef, renderer.getMetadata());

        //Process each active wavelength.  If their number N > 1, then 
        //process N-1 async and one in the current thread.  If N = 1, 
        //just use the current thread.
        RenderHSBWaveTask[] tasks = makeRndTasks(planeDef);
        performanceStats.startRendering();
        int n = tasks.length;
        Future[] rndTskFutures = new Future[n];  //[0] unused.
        //CmdProcessor processor = Env.getProcessor();
        ExecutorService processor = Executors.newCachedThreadPool();//j.m
        
        while (0 < --n)
            rndTskFutures[n] = processor.submit(tasks[n]); //j.m exec(tasks[n]);
        RGBBuffer rndDataBuf = null;
        byte[] red = null, green = null, blue = null;
        if (n == 0) {
            rndDataBuf = (RGBBuffer) tasks[0].call(); 
            red = rndDataBuf.getRedBand();
            green = rndDataBuf.getGreenBand();
            blue = rndDataBuf.getBlueBand();
        }
    
        //Wait for all forked tasks (if any) to complete.
        //When a task completes, assemble its RGB buffer into rndDataBuf.
        RGBBuffer taskBuffer;
        int x1, x2, pix;
        byte[] r, g, b;
        for (n = 1; n < rndTskFutures.length; ++n) {
            try {
                taskBuffer = (RGBBuffer) rndTskFutures[n].get();//j.m getResult();
                r = taskBuffer.getRedBand();
                g = taskBuffer.getGreenBand();
                b = taskBuffer.getBlueBand();
                for (x2 = 0; x2 < sizeX2; ++x2) {
                    for (x1 = 0; x1 < sizeX1; ++x1) {
                        pix = sizeX1*x2+x1;
                        red[pix] = (byte) (red[pix]+r[pix]);
                        green[pix] = (byte) (green[pix]+g[pix]);
                        blue[pix] = (byte) (blue[pix]+b[pix]);
                    } 
                } 
            } catch (Exception e) {
                if (e instanceof QuantizationException)
                    throw (QuantizationException) e;
                throw (RuntimeException) e;  
                //B/c call() only throws QuantizationException, it must be RE.
            }
        }
        performanceStats.endRendering();
        
        if (rndDataBuf == null)  //No active channel, return a black image. 
            return new RGBBuffer(sizeX1, sizeX2);
        
        //Done.
        return rndDataBuf;
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see RenderingStrategy#getImageSize(PlaneDef, Pixels)
     */
    int getImageSize(PlaneDef pd, Pixels pixels)
    {
        initAxesSize(pd, pixels);
        return sizeX1*sizeX2*3;
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see RenderingStrategy#getPlaneDimsAsString(PlaneDef, Pixels)
     */
    String getPlaneDimsAsString(PlaneDef pd, Pixels pixels)
    {
        initAxesSize(pd, pixels);
        return sizeX1+"x"+sizeX2;
    }
    
}
