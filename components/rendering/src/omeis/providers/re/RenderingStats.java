/*
 * omeis.providers.re.RenderingStats
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
//j.m import omeis.env.log.LogMessage;
import omeis.providers.re.data.PlaneDef;

/** 
 * Exposes methods to time the various steps in the rendering process and
 * provides a stats report.
 * Every time the {@link Renderer#render(PlaneDef) render} method is invoked
 * a new <code>RenderingStats</code> object is created that can then be
 * accessed by the current {@link RenderingStrategy} to notify start/end times
 * of memory allocation, IO, and rendering time.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.4 $ $Date: 2005/06/20 11:00:11 $)
 * </small>
 * @since OME2.2
 */
public class RenderingStats
{   
    
    //Fields for the stats log message -- see getStats().
    private static final String RENDERING_STATS = 
        "--------------- RENDERING STATS ---------------";
    private static final String CONTEXT = "CONTEXT";
    private static final String CONTEXT_UNDERLINE = "-------";
    private static final String OMEIS_PIXELS_ID = "OMEIS Pixels ID: ";
    private static final String PLANE = "Plane: ";
    private static final String PLANE_DATA = "Plane Data: ";
    private static final String SPACE = " ";
    private static final String CHANNELS = "Channels: ";
    private static final String RENDERED_IMAGE = "Rendered Image: ";
    private static final String BYTES = " bytes";
    private static final String COLOR_MODEL = "Color Model: ";
    private static final String TIMES = "TIMES (ms)";
    private static final String TIMES_UNDERLINE = "----------";
    private static final String MEMORY_ALLOCATION = "Memory Allocation: ";
    private static final String IO = "I/O: ";
    private static final String RENDERING = "Rendering: ";
    private static final String TOTAL = "Total: ";
    private static final String CODOMAIN_MAPS = "Codomain maps: ";
    private static final String BOTTOM_LINE = 
        "-----------------------------------------------";
    
    //Fields for the stats log message -- see getIoTimeString().
    private static final String C_EQUALS = "c=";
    private static final String COLON = ":";
    private static final String ARROW = " -> ";
    
    
    /** The object whose <code>render</code> method is being timed. */
    private Renderer context;
    
    /** Defines the plane that the <code>render</code> is processing. */
    private PlaneDef plane;
    
    /** The time that it took to allocate rendering buffers in memory. */
    private long     mallocTime;
    
    /**
     * The time that it took to retrieve the pixels data.
     * This is a map that contains an I/O measurement for each wavelength 
     * that is being rendered.  (The key is the wavelength index.)
     */
    private Map      ioTime;
    
    /** The time that it took to transform the pixels data into an image. */
    private long     renderingTime;
    
    /** The total time a call to the <code>render</code> method takes. */
    private long     totalTime;
    
    
    /**
     * Helper method to build a string containing the I/O stats.
     * 
     * @return See above.
     */
    private String getIoTimeString()
    {
        StringBuffer buf = new StringBuffer();
        Integer i;
        Long t;
        long total = 0;
        Iterator k = ioTime.keySet().iterator();
        while (k.hasNext()) {
            i = (Integer) k.next();
            t = (Long) ioTime.get(i);
            total += t.longValue();
            buf.append(C_EQUALS);
            buf.append(i);
            buf.append(COLON);
            buf.append(t);
            buf.append(SPACE);
        }
        return total+ARROW+buf.toString();
    }
    
    /**
     * Creates a new instance.
     * This constructor takes the current time, which is then used to calculate
     * the total time the <code>render</code> method took to execute.  The
     * {@link Renderer} takes care of creating this object right before the
     * rendering process starts and then calls the {@link #stop() stop} method
     * just after the rendering process ends.
     * 
     * @param context   The object whose <code>render</code> method is being 
     *                  timed. Assumed not to be <code>null</code>.
     * @param plane     Defines the plane that the <code>render</code> is 
     *                  processing. Assumed not to be <code>null</code>.
     */
    public RenderingStats(Renderer context, PlaneDef plane)
    {
        this.context = context;
        this.plane = plane;
        ioTime = new HashMap();
        totalTime = System.currentTimeMillis();
        mallocTime = 0;
    }
    
    /**
     * Notifies the start of the allocation of an RGB memory buffer for the
     * rendering process.
     * 
     * @see #endMalloc()
     */
    public void startMalloc() { mallocTime -= System.currentTimeMillis(); }
    
    /**
     * Notifies the end of the allocation of an RGB memory buffers for the
     * rendering process.
     * 
     * @see #startMalloc()
     */
    public void endMalloc() { mallocTime += System.currentTimeMillis(); }
    
    //NOTE: The startMalloc/endMalloc can be called multiple times in the
    //HSBStrategy -- if several RGB buffers are allocated.
    //So we're tracking the series:
    //m0=0, m1=m0-s1, m2=m1+e1, m3=m2-s2, m4=m3+e2, ...
    //Where s[i] is the time taken by startMalloc upon allocation of the
    //i-th buffer and e[i] is the time taken by endMalloc when allocation
    //has completed.  So after n allocations, the value of mallocTime is:
    //(e1-s1)+(e2-s2)+...+(e[n]-s[n])
    
    /**
     * Notifies the start of pixels data retrieval for the specified wavelength
     * (channel).
     * 
     * @param c The wavelength (channel) index.
     * @see #endIO(int)
     */
    public void startIO(int c)
    {
        ioTime.put(new Integer(c), new Long(System.currentTimeMillis()));
    }
    
    /**
     * Notifies the end of pixels data retrieval for the specified wavelength
     * (channel).
     * 
     * @param c The wavelength (channel) index.
     * @see #startIO(int)
     */
    public void endIO(int c)
    {
        Integer channel = new Integer(c);
        long start = ((Long) ioTime.get(channel)).longValue();
        ioTime.put(channel, new Long(System.currentTimeMillis()-start));
    }
    
    /**
     * Notifies the start of the trasnformation of the raw pixels data.
     * 
     * @see #endRendering()
     */
    public void startRendering() { renderingTime = System.currentTimeMillis(); }
    
    /**
     * Notifies the end of the trasnformation of the raw pixels data.
     * 
     * @see #startRendering()
     */
    public void endRendering() 
    { 
        renderingTime = System.currentTimeMillis()-renderingTime; 
    }
    
    /**
     * Notifies this object that the rendering process has finished.
     * The total rendering time is computed.  That is, the time the
     * <code>render</code> method took to execute.
     * The {@link #getStats() getStats} method can now be invoked to
     * retrieve the stats report.
     */
    public void stop() 
    {
        totalTime = System.currentTimeMillis()-totalTime;
    }
    
    /**
     * Returns a stats report ready to be written to the log file.
     * The report includes memory allocation, IO, and rendering times as well
     * as a summary of the rendering context in which the call to the <code>
     * render</code> method took place.
     * This method only provides a meaningful report if it is called <i>after
     * </i> the {@link #stop() stop} method.
     * 
     * @return A log message embedding the stats report.
     */
    public String getStats() // j.m
    {
        //j.mLogMessage msg = new LogMessage();
    	//BELOW msg.print-->StringBuilder();
    	StringBuilder sb = new StringBuilder(2048);
        String n = "\n";
    	sb.append(n);//TODO
        sb.append(RENDERING_STATS);
        sb.append(n);
        sb.append(CONTEXT);
        sb.append(CONTEXT_UNDERLINE);
        sb.append(OMEIS_PIXELS_ID);
        sb.append(context.getMetadata().getId());
        sb.append(PLANE);
        sb.append(plane);
        sb.append(PLANE_DATA);
        sb.append(context.getPlaneDimsAsString(plane));
        sb.append(SPACE);
        sb.append(context.getPixelsType());
        sb.append(CHANNELS);
        sb.append(ioTime.keySet().size());
        sb.append(RENDERED_IMAGE);
        sb.append(context.getImageSize(plane));
        sb.append(BYTES);
        sb.append(COLOR_MODEL);
        sb.append(context.getRenderingDef().getModel()); // FIXME is this ok?
        sb.append(CODOMAIN_MAPS);
        sb.append(context.getCodomainChain());
        sb.append(n);
        sb.append(TIMES);
        sb.append(TIMES_UNDERLINE);
        sb.append(MEMORY_ALLOCATION);
        sb.append(mallocTime);
        sb.append(IO);
        sb.append(getIoTimeString());
        sb.append(RENDERING);
        sb.append(renderingTime);
        sb.append(TOTAL);
        sb.append(totalTime);
        sb.append(n);
        sb.append(BOTTOM_LINE);
        return sb.toString(); //j.m
    }
    
}
