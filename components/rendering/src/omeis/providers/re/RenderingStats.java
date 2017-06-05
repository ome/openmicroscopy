/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re;

import java.util.HashMap;
import java.util.Map;

import omeis.providers.re.data.PlaneDef;

/**
 * Exposes methods to time the various steps in the rendering process and
 * provides a stats report. Every time the
 * {@link Renderer#render(PlaneDef) render} method is invoked a new
 * <code>RenderingStats</code> object is created that can then be accessed by
 * the current {@link RenderingStrategy} to notify start/end times of memory
 * allocation, IO, and rendering time.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/20 11:00:11 $) </small>
 * @since OME2.2
 */
public class RenderingStats
{
    /** The object whose <code>render</code> method is being timed. */
    private Renderer context;

    /** Defines the plane that the <code>render</code> is processing. */
    private PlaneDef plane;

    /** The time that it took to allocate rendering buffers in memory. */
    private long mallocTime;

    /**
     * The time that it took to retrieve the pixels data. This is a map that
     * contains an I/O measurement for each wavelength that is being rendered.
     * (The key is the wavelength index.)
     */
    private Map<Integer, Long> ioTime;

    /** The time that it took to transform the pixels data into an image. */
    private long renderingTime;

    /** The total time a call to the <code>render</code> method takes. */
    private long totalTime;

    /**
     * Helper method to build a string containing the I/O stats.
     * 
     * @return See above.
     */
    private String getIoTimeString() {
        StringBuffer buf = new StringBuffer();
        long total = 0;
        Long t;
        for (Integer key : ioTime.keySet())
        {
            t = ioTime.get(key);
            total += t;
            buf.append("c=");
            buf.append(key);
            buf.append(";");
            buf.append(t);
            buf.append(" ");
        }
        return total + " -> " + buf.toString();
    }

    /**
     * Creates a new instance. This constructor takes the current time, which is
     * then used to calculate the total time the <code>render</code> method
     * took to execute. The {@link Renderer} takes care of creating this object
     * right before the rendering process starts and then calls the
     * {@link #stop() stop} method just after the rendering process ends.
     * 
     * @param context
     *            The object whose <code>render</code> method is being timed.
     *            Assumed not to be <code>null</code>.
     * @param plane
     *            Defines the plane that the <code>render</code> is
     *            processing. Assumed not to be <code>null</code>.
     */
    public RenderingStats(Renderer context, PlaneDef plane) {
        this.context = context;
        this.plane = plane;
        ioTime = new HashMap<Integer, Long>();
        totalTime = System.currentTimeMillis();
        mallocTime = 0;
    }

    /**
     * Notifies the start of the allocation of an RGB memory buffer for the
     * rendering process.
     * 
     * @see #endMalloc()
     */
    public void startMalloc() {
        mallocTime -= System.currentTimeMillis();
    }

    /**
     * Notifies the end of the allocation of an RGB memory buffers for the
     * rendering process.
     * 
     * @see #startMalloc()
     */
    public void endMalloc() {
        mallocTime += System.currentTimeMillis();
    }

    // NOTE: The startMalloc/endMalloc can be called multiple times in the
    // HSBStrategy -- if several RGB buffers are allocated.
    // So we're tracking the series:
    // m0=0, m1=m0-s1, m2=m1+e1, m3=m2-s2, m4=m3+e2, ...
    // Where s[i] is the time taken by startMalloc upon allocation of the
    // i-th buffer and e[i] is the time taken by endMalloc when allocation
    // has completed. So after n allocations, the value of mallocTime is:
    // (e1-s1)+(e2-s2)+...+(e[n]-s[n])

    /**
     * Notifies the start of pixels data retrieval for the specified wavelength
     * (channel).
     * 
     * @param c
     *            The wavelength (channel) index.
     * @see #endIO(int)
     */
    public void startIO(int c) {
        ioTime.put(new Integer(c), new Long(System.currentTimeMillis()));
    }

    /**
     * Notifies the end of pixels data retrieval for the specified wavelength
     * (channel).
     * 
     * @param c
     *            The wavelength (channel) index.
     * @see #startIO(int)
     */
    public void endIO(int c) {
        Integer channel = new Integer(c);
        long start = ((Long) ioTime.get(channel)).longValue();
        ioTime.put(channel, new Long(System.currentTimeMillis() - start));
    }

    /**
     * Notifies the start of the trasnformation of the raw pixels data.
     * 
     * @see #endRendering()
     */
    public void startRendering() {
        renderingTime = System.currentTimeMillis();
    }

    /**
     * Notifies the end of the trasnformation of the raw pixels data.
     * 
     * @see #startRendering()
     */
    public void endRendering() {
        renderingTime = System.currentTimeMillis() - renderingTime;
    }

    /**
     * Notifies this object that the rendering process has finished. The total
     * rendering time is computed. That is, the time the <code>render</code>
     * method took to execute. The {@link #getStats() getStats} method can now
     * be invoked to retrieve the stats report.
     */
    public void stop() {
        totalTime = System.currentTimeMillis() - totalTime;
    }

    /**
     * Returns a stats report ready to be written to the log file. The report
     * includes memory allocation, IO, and rendering times as well as a summary
     * of the rendering context in which the call to the <code>
     * render</code>
     * method took place. This method only provides a meaningful report if it is
     * called <i>after </i> the {@link #stop() stop} method.
     * 
     * @return A log message embedding the stats report.
     */
    public String getStats()
    {
	String a = "--------------- RENDERING STATS ---------------\n";
    	a += String.format(
    			"CONTEXT ---- OMEIS Pixels ID: %d Plane: %s Type: %s " +
    			"PlaneData: %s Channels: %d Renderered Image: %s " +
    			"Color Model: %s\n",
    				context.getMetadata().getId(),
    				plane,
    				context.getPlaneDimsAsString(plane),
    				context.getPixelsType(),
    				ioTime.keySet().size(),
    				context.getImageSize(plane),
    				context.getRenderingDef().getModel().getValue());
    	a += String.format(
    			"TIMES (ms) ---- Memory Allocation: %d I/O: %s " +
    			"Rendering: %d Total: %d\n",
    				mallocTime,
    				getIoTimeString(),
    				renderingTime,
    				totalTime);
    	a += "-----------------------------------------------";
    	return a;
    }
}
