/*
 * omeis.providers.re.RGBStrategy
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

// Java imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Third-party libraries
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.ColorFix;
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.data.PlaneFactory;
import omeis.providers.re.data.Plane2D;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;

/**
 * Transforms a plane within a given pixels set into an <i>RGB</i> image. Up to
 * three wavelengths (channels) can contribute to the final image and each
 * wavelength is mapped to one color out of red, green, or blue. All this things
 * are specified by the rendering context.
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
class RGBStrategy extends RenderingStrategy {

    /**
     * Initialize the <code>sizeX1</code> and <code>sizeX2</code> fields
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
     * Extracts a color band depending on the <code>rgba</code> settings.
     * 
     * @param dataBuf
     *            Buffer to hold the output image's data.
     * @param color
     *            The color settings of a given wavelength.
     * @return Returns the color band selected in <code>color</code>.
     */
    private int getColorBand(ColorFix color) { // This function is not used 
        if (color.getGreen().intValue() == 255) {
            return RGBBuffer.G_BAND;
        } else if (color.getBlue().intValue() == 255) {
            return RGBBuffer.B_BAND;
        }
        return RGBBuffer.R_BAND;
    }

    /**
     * Creates a rendering task for each active wavelength.
     * 
     * @param planeDef
     *            The plane to render.
     * @param renderedDataBuf
     *            The buffer into which the rendered data will go.
     * @return An array containing the tasks.
     */
    private RenderRGBWaveTask[] makeRndTasks(PlaneDef planeDef,
            RGBBuffer renderedDataBuf) {
        ArrayList tasks = new ArrayList(3);

        // Get all objects we need to create the tasks.
        Plane2D wData;
        QuantumManager qManager = renderer.getQuantumManager();
        ChannelBinding[] cBindings = renderer.getChannelBindings();
        CodomainChain cc = renderer.getCodomainChain();
        PixelBuffer pixels = renderer.getPixels();
        Pixels metadata = renderer.getMetadata();
        RenderingStats performanceStats = renderer.getStats();

        // Create a task for each active wavelength.
        for (int w = 0; w < cBindings.length; w++) {
            if (tasks.size() == 3) {
                break; // We only render 3 w at most.
            }
            if (cBindings[w].getActive().booleanValue()) {
                // Get the raw data.
                performanceStats.startIO(w);
                wData = PlaneFactory.createPlane(planeDef, w, metadata, pixels);
                performanceStats.endIO(w);

                // Create a rendering task for this wavelength.
                /*
                 * tasks.add(new RenderRGBWaveTask( renderedDataBuf,
                 * getColorBand(cBindings[w].getColor()), wData,
                 * qManager.getStrategyFor(w), cc,
                 * cBindings[w].getColor().getAlpha().intValue(), sizeX1,
                 * sizeX2));
                 */
            }
        }

        // Turn the list into an array an return it.
        RenderRGBWaveTask[] t = new RenderRGBWaveTask[tasks.size()];
        return (RenderRGBWaveTask[]) tasks.toArray(t);
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

        // Initialize sizeX1 and sizeX2 according to the plane definition and
        // create the RGB buffer.
        initAxesSize(planeDef, renderer.getMetadata());

        // Process each active wavelength. If their number N > 1, then
        // process N-1 async and one in the current thread. If N = 1,
        // just use the current thread.
        RenderRGBWaveTask[] tasks = makeRndTasks(planeDef, buf);
        performanceStats.startRendering();
        int n = tasks.length;
        Future[] rndTskFutures = new Future[n]; // [0] unused.
        ExecutorService processor = Executors.newCachedThreadPool();
        while (0 < --n) {
            rndTskFutures[n] = processor.submit(tasks[n]);
        }
        if (n == 0) {
            tasks[0].call();
        }

        // Wait for all forked tasks (if any) to complete.
        processor.shutdown();
        for (n = 1; n < rndTskFutures.length; ++n) {
            try {
                rndTskFutures[n].get();
            } catch (Exception e) {
                if (e instanceof QuantizationException) {
                    throw (QuantizationException) e;
                }
                throw (RuntimeException) e;
                // B/c call() only throws QuantizationException, it must be RE.
            }
        }
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
