/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.conditions.ResourceError;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.enums.PixelsType;
import ome.util.PixelData;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.lut.LutReader;
import omeis.providers.re.quantum.BinaryMaskQuantizer;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * Transforms a plane within a given pixels set into an <i>RGB</i> image. As
 * many wavelengths (channels) as desired can contribute to the final image and
 * each wavelength is mapped to a color. All these things are specified by the
 * rendering context.
 * <p>
 * This strategy renders the in "regions", dividing the planar data up based
 * on {@link #maxTasks} and assigning each task to its own thread. This should
 * result in parallel rendering on multi-processor machines.
 * </p>
 * <p>
 * Thread-safety relies on the fact that the rendering context is not going to
 * change during the whole image rendering process and that each task is
 * working on its own atomic unit of work.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/22 17:09:48 $) </small>
 * @since OME2.2
 */
class HSBStrategy extends RenderingStrategy {
	
    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(HSBStrategy.class);
    
    /**
     * Retrieves the maximum number of reasonable tasks to schedule based on
     * image size and <i>maxTasks</i>.
     * 
     * @param size The width along the X2 axis.
     * @return the number of tasks to schedule.
     */
    private int numTasks(int size) {
        for (int i = maxTasks; i > 0; i--) {
            if (size % i == 0) {
                return i;
            }
        }
        return 1;
    }

    /**
     * Retrieves the wavelength data for all the active channels and overlays.
     * 
     * @return the wavelength data.
     */
    private List<Plane2D> getWavelengthData(PlaneDef pDef) {
        ChannelBinding[] channelBindings = renderer.getChannelBindings();
        Pixels metadata = renderer.getMetadata();
        PixelBuffer pixels = renderer.getPixels();
        List<Plane2D> wData = null;
        try
        {
        	RenderingStats performanceStats = renderer.getStats();
        	wData = new ArrayList<Plane2D>();

        	for (int w = 0; w < channelBindings.length; w++) {
        		if (channelBindings[w].getActive()) {
        			performanceStats.startIO(w);
        			wData.add(PlaneFactory.createPlane(pDef, w, metadata, 
        					pixels));
        			performanceStats.endIO(w);
        		}
        	}
        	Map<byte[], Integer> overlays = renderer.getOverlays();
        	if (overlays != null)
        	{
        		for (byte[] overlay : overlays.keySet())
        		{
				ome.util.PixelData data =
					new PixelData(PixelsType.VALUE_BIT, ByteBuffer.wrap(overlay));
        			wData.add(new Plane2D(pDef, metadata, data));
        		}
        	}
        }
        finally
        {
            // Make sure that the pixel buffer is cleansed properly.
            try
            {
                pixels.close();
            } 
            catch (IOException e)
            {
                log.error("Pixels could not be closed successfully.", e);
    			throw new ResourceError(
    					e.getMessage() + " Please check server log.");
            }        	
        }

        return wData;
    }

    /**
     * Returns the collection of chains.
     *
     * @return See above.
     */
    private List<CodomainChain> getChains()
    {
        List<CodomainChain> chains = renderer.getCodomainChains();
        ChannelBinding[] channelBindings = renderer.getChannelBindings();
        List<CodomainChain> list = new ArrayList<CodomainChain>();
        for (int w = 0; w < channelBindings.length; w++) {
            ChannelBinding cb = channelBindings[w];
            if (cb.getActive()) {
                list.add(chains.get(w));
            }
        }
        return list;
    }

    /**
     * Retrieves the color for each active channels.
     * 
     * @return the active channel color data.
     */
    private List<int[]> getColors() {
        ChannelBinding[] channelBindings = renderer.getChannelBindings();
        List<int[]> colors = new ArrayList<int[]>();

        for (int w = 0; w < channelBindings.length; w++) {
            ChannelBinding cb = channelBindings[w];
            if (cb.getActive()) {
                int[] theNewColor = new int[] { 
                        cb.getRed(), cb.getGreen(),
                        cb.getBlue(), cb.getAlpha() };
                colors.add(theNewColor);
            }
        }
    	Map<byte[], Integer> overlays = renderer.getOverlays();
    	if (overlays != null)
    	{
    		for (byte[] overlay : overlays.keySet())
    		{
    			Integer packedColor = overlays.get(overlay);
    			Color color = new Color(packedColor);
    			colors.add(new int[] { color.getRed(), color.getBlue(),
    					               color.getGreen(), color.getAlpha() });
    		}
    	}
        return colors;
    }

    /**
     * Retrieves the quantum strategy for each active channels
     * 
     * @return the active channel color data.
     */
    private List<QuantumStrategy> getStrategies() {
        ChannelBinding[] channelBindings = renderer.getChannelBindings();
        QuantumManager qManager = renderer.getQuantumManager();
        List<QuantumStrategy> strats = new ArrayList<QuantumStrategy>();

        for (int w = 0; w < channelBindings.length; w++) {
            if (channelBindings[w].getActive()) {
                strats.add(qManager.getStrategyFor(w));
            }
        }
    	Map<byte[], Integer> overlays = renderer.getOverlays();
    	if (overlays != null)
    	{
    		QuantumDef def = new QuantumDef();  // Just to fulfill interface
    		Pixels pixels = new Pixels();
    		PixelsType bitType = new PixelsType();
    		bitType.setValue(PixelsType.VALUE_BIT);
    		bitType.setBitSize(1);
    		pixels.setPixelsType(bitType);
    		for (int i = 0; i < overlays.size(); i++)
    		{
    			strats.add(new BinaryMaskQuantizer(def, pixels));
    		}
    	}
        return strats;
    }

    /**
     * Creates a set of rendering tasks for the image based on the calling
     * buffer type.
     * 
     * @param planeDef
     *            The plane to render.
     * @param buf
     *            The buffer to render into.
     * @return An array containing the tasks.
     */
    private RenderingTask[] makeRenderingTasks(PlaneDef def, RGBBuffer buf) {
        List<RenderHSBRegionTask> tasks = new ArrayList<RenderHSBRegionTask>();

        //RenderingStats performanceStats = renderer.getStats();
        List<Plane2D> wData = getWavelengthData(def);
        List<int[]> colors = getColors();
        List<LutReader> readers = renderer.getLutProvider().getLutReaders(
                renderer.getChannelBindings());
        List<QuantumStrategy> strategies = getStrategies();
        // Create a number of rendering tasks.
        int taskCount = numTasks(sizeX2);
        int delta = sizeX2/taskCount;
        int x1Start = 0;
        int x1End = sizeX1;
        int x2Start, x2End;
        log.info("taskCount: "+taskCount+" delta: "+delta);
        for (int i = 0; i < taskCount; i++) {
            x2Start = i*delta;
            x2End = (i+1)*delta;
            tasks.add(new RenderHSBRegionTask(buf, wData, strategies,
                    getChains(), colors, renderer.getOptimizations(),
                    x1Start, x1End, x2Start, x2End, readers));
        }

        // Turn the list into an array an return it.
        return tasks.toArray(new RenderingTask[tasks.size()]);
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
    @Override
    RGBBuffer render(Renderer ctx, PlaneDef planeDef) throws IOException,
            QuantizationException {
        // Set the context and retrieve objects we're gonna use.
        renderer = ctx;
        //RenderingStats performanceStats = renderer.getStats();
        Pixels metadata = renderer.getMetadata();

        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        initAxesSize(planeDef, metadata);
        RGBBuffer buf = getRgbBuffer();
        render(buf, planeDef);
        return buf;
    }
    
    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#renderAsPackedInt(Renderer ctx, PlaneDef planeDef)
     */
    @Override
    RGBIntBuffer renderAsPackedInt(Renderer ctx, PlaneDef planeDef)
            throws IOException, QuantizationException {
        // Set the context and retrieve objects we're gonna use.
        renderer = ctx;
        Pixels metadata = renderer.getMetadata();

        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        initAxesSize(planeDef, metadata);
        RGBIntBuffer buf = getIntBuffer();
        render(buf, planeDef);
        return buf;
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#renderAsPackedIntRGBA(Renderer ctx, PlaneDef planeDef)
     */
    @Override
    RGBAIntBuffer renderAsPackedIntAsRGBA(Renderer ctx, PlaneDef planeDef)
            throws IOException, QuantizationException {
        // Set the context and retrieve objects we're gonna use.
        renderer = ctx;
        Pixels metadata = renderer.getMetadata();

        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        initAxesSize(planeDef, metadata);
        RGBAIntBuffer buf = getRGBAIntBuffer();

        render(buf, planeDef);
        return buf;
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
    private void render(RGBBuffer buf, PlaneDef planeDef) throws IOException,
            QuantizationException {
        RenderingStats performanceStats = renderer.getStats();
        // Process each active wavelength. If their number N > 1, then
        // process N-1 async and one in the current thread. If N = 1,
        // just use the current thread.
        RenderingTask[] tasks = makeRenderingTasks(planeDef, buf);
        performanceStats.startRendering();
        int n = tasks.length;
        Future[] rndTskFutures = new Future[n]; // [0] unused.
        ExecutorService processor = Executors.newCachedThreadPool();

        while (0 < --n) {
            rndTskFutures[n] = processor.submit(tasks[n]);
        }

        // Call the task in the current thread.
        if (n == 0) {
            tasks[0].call();
        }

        // Wait for all forked tasks (if any) to complete.
        for (n = 1; n < rndTskFutures.length; ++n) {
            try {
                rndTskFutures[n].get();
            } catch (Exception e) {
                if (e instanceof QuantizationException) {
                    throw (QuantizationException) e;
                }
                throw new RuntimeException(e);
            }
        }

        // Shutdown the task processor
        processor.shutdown();

        // End the performance metrics for this rendering event.
        performanceStats.endRendering();
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#getImageSize(PlaneDef, Pixels)
     */
    @Override
    int getImageSize(PlaneDef pd, Pixels pixels) {
        initAxesSize(pd, pixels);
        return sizeX1 * sizeX2 * 3;
    }

    /**
     * Implemented as specified by the superclass.
     * 
     * @see RenderingStrategy#getPlaneDimsAsString(PlaneDef, Pixels)
     */
    @Override
    String getPlaneDimsAsString(PlaneDef pd, Pixels pixels) {
        initAxesSize(pd, pixels);
        return sizeX1 + "x" + sizeX2;
    }

}
