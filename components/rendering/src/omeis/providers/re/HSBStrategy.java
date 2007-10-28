/*
 * omeis.providers.re.HSBStrategy
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

// Java imports
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Third-party libraries

// Application-internal dependencies

import ome.conditions.ResourceError;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;

import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * Transforms a plane within a given pixels set into an <i>RGB</i> image. As
 * many wavelengths (channels) as desired can contribute to the final image and
 * each wavelength is mapped to a color. All this things are specified by the
 * rendering context.
 * <p>
 * When multiple wavelengths have to be combined into the final image (this is
 * the case if the rendering context specifies more than one active channel),
 * this strategy renders each wavelength in a separate thread &#151; this often
 * results in parallel rendering on multi-processor machines.
 * </p>
 * <p>
 * Thread-safety relies on the fact that the rendering context is not going to
 * change during the whole image rendering process. (This is enforced by the
 * {@link RenderingEngineImpl}; in fact, while the <code>render</code> method
 * executes, the whole component is locked.)
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
    private static Log log = LogFactory.getLog(HSBStrategy.class);

    /**
     * The maximum number of tasks (regions to split the image up into) that we
     * will be using.
     */
    private static final int maxTasks = 2;

    /**
     * Initializes the <code>sizeX1</code> and <code>sizeX2</code> fields
     * according to the specified {@link PlaneDef#getSlice() slice}.
     * 
     * @param pd
     *            Reference to the plane definition defined for the strategy.
     * @param pixels
     *            Dimensions of the pixels set.
     */
    private void initAxesSize(PlaneDef pd, Pixels pixels) {
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
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("Invalid slice ID: " + pd.getSlice()
                    + ".", nfe);
        }
    }

    /**
     * Retrieves the maximum number of reasonable tasks to schedule based on
     * image size and <i>maxTasks</i>.
     * 
     * @return the number of tasks to schedule.
     */
    private int numTasks() {
        for (int i = maxTasks; i > 0; i--) {
            if (sizeX2 % i == 0) {
                return i;
            }
        }
        return 1;
    }

    /**
     * Retrieves the wavelength data for all the active channels.
     * 
     * @return the wavelength data.
     */
    private List<Plane2D> getWavelengthData(PlaneDef pDef) {
        ChannelBinding[] channelBindings = renderer.getChannelBindings();
        Pixels metadata = renderer.getMetadata();
        PixelBuffer pixels = renderer.getPixels();
        ArrayList<Plane2D> wData = null;
        try
        {
        	RenderingStats performanceStats = renderer.getStats();
        	wData = new ArrayList<Plane2D>();

        	for (int w = 0; w < channelBindings.length; w++) {
        		if (channelBindings[w].getActive()) {
        			performanceStats.startIO(w);
        			wData.add(PlaneFactory.createPlane(pDef, w, metadata, pixels));
        			performanceStats.endIO(w);
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
     * Retrieves the color for each active channels.
     * 
     * @return the active channel color data.
     */
    private List<Color> getColors() {
        ChannelBinding[] channelBindings = renderer.getChannelBindings();
        ArrayList<Color> colors = new ArrayList<Color>();

        for (int w = 0; w < channelBindings.length; w++) {
            if (channelBindings[w].getActive()) {
                colors.add(channelBindings[w].getColor());
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
        ArrayList<QuantumStrategy> strats = new ArrayList<QuantumStrategy>();

        for (int w = 0; w < channelBindings.length; w++) {
            if (channelBindings[w].getActive()) {
                strats.add(qManager.getStrategyFor(w));
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
        ArrayList<RenderHSBRegionTask> tasks = new ArrayList<RenderHSBRegionTask>();

        // Get all objects we need to create the tasks.
        CodomainChain cc = renderer.getCodomainChain();
        RenderingStats performanceStats = renderer.getStats();
        List<Plane2D> wData = getWavelengthData(def);
        List<Color> colors = getColors();
        List<QuantumStrategy> strategies = getStrategies();

        // Create a number of rendering tasks.
        int taskCount = numTasks();
        int delta = sizeX2 / taskCount;
        for (int i = 0; i < taskCount; i++) {
            int x1Start = 0;
            int x1End = sizeX1;
            int x2Start = i * delta;
            int x2End = (i + 1) * delta;
            tasks.add(new RenderHSBRegionTask(buf, wData, strategies, cc,
            		colors, renderer.getOptimizations(),
            		x1Start, x1End, x2Start, x2End));
        }

        // Turn the list into an array an return it.
        RenderingTask[] tArray = new RenderingTask[tasks.size()];
        return tasks.toArray(tArray);
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
        RenderingStats performanceStats = renderer.getStats();
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
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
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
     * @see RenderingStrategy#render(Renderer ctx, PlaneDef planeDef)
     */
    private void render(RGBBuffer buf, PlaneDef planeDef) throws IOException,
            QuantizationException {
        RenderingStats performanceStats = renderer.getStats();

        // Initialize sizeX1 and sizeX2 according to the plane.
        initAxesSize(planeDef, renderer.getMetadata());

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
