/*
 * org.openmicroscopy.shoola.env.rnd.RenderingManager
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
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSourceException;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantizationException;
import org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.ExecException;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.concur.tasks.Invocation;

/** 
 * Manages requests to render a plane within a given pixels set and
 * also rendering-related activities such as caching.
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
class RenderingManager
{
    
    /**
     * Factory method to create a new manager to handle the the given pixels 
     * set within the specified image.
     * 
     * @param engine    Reference to the rendering engine.
     *                  Mustn't be <code>null</code>.
     * @param imageID   The id of the image the pixels set belongs to.
     * @param pixelsID  The id of the pixels set.
     * @throws MetadataSourceException If an error occurs while retrieving
     *                                  data from <i>OMEDS</i>.
     */
    static RenderingManager makeNew(RenderingEngine engine, 
                                    int imageID, int pixelsID)
        throws MetadataSourceException
    {
        //First off, create a renderer.  This factory method will load
        //all needed metadata too and check engine != null.  No need to
        //check ID's, if they're wrong OMEDS will complain loudly.
        Renderer r = Renderer.makeNew(imageID, pixelsID, engine);
        
        //Now that we have a fully initialized renderer, we can create
        //its manager.
        return new RenderingManager(r, engine);
    }
    
    /**
     * Extracts the image result of an asynchronous rendering operation.
     * We attempt to retrieve the actual image the {@link Future} is 
     * representing, possibly blocking until the image has been rendered.  
     * If the image couldn't be rendered, we just rethrow the cause of the 
     * error.
     * 
     * @param f    A handle to an asynchronous invocation of the 
     *              {@link Renderer#render() render} method on a 
     *              {@link Renderer} object.
     * @return The rendered image.
     * @throws DataSourceException If an error occurred while fetching image 
     *                              data.
     * @throws QuantizationException If an error occurred while rendering.
     */
    static BufferedImage extractXYImage(Future f)
        throws DataSourceException, QuantizationException
    {
        if (f == null) throw new NullPointerException("No future.");
        BufferedImage plane = null;
        try {
            plane = (BufferedImage) f.getResult();
        } catch (ExecException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof DataSourceException)
                throw (DataSourceException) cause;
            if (cause instanceof QuantizationException)
                throw (QuantizationException) cause;
            
            //The above exceptions are the only ones declared by the render
            //method.  If we got here, then it must be a RuntimeException.
            throw (RuntimeException) cause;
        } catch (InterruptedException ie) {
            //Should never happen as we never interrupt Swing thread.
            //However, just to be on the safe-side:
            throw new DataSourceException("Thread interrupted.", ie);
        }
        return plane;
    }
    
    
    /** Handles requests to render planes within our pixels set. */
    private final Renderer          renderer;
    
    /** Back-link to the rendering engine. */
    private final RenderingEngine   engine;
    
    /**
     * Keeps track of the XY planes that have been rendered and tries to guess 
     * the next ones that will be requested.
     */
    private NavigationHistory       history;
    
    /** 
     * Caches XY images that have been rendered or futures to (said) images 
     * that are being/have been rendered asynchronously.
     */
    private ImageFutureCache        cache;
                 
    /**
     * Utility innner class to encapsulate a rendering operation into a service
     * and run it asynchronously.
     * The plane to render is specified to the constructor and the renderer is
     * the one in use within its enclosing class.  This means the same renderer
     * is shared by all instances of this class.  The implication is that more
     * than one thread could be calling the render method at the same time.
     * Remarkably a rendering operation only reads the state of the renderering
     * environment and retrieves plane data through the renderer's data sink 
     * &#151; this latter operation is thread-safe.  So every time the state of
     * the rendering environment is changed (the action is initiated by the GUI
     * and runs within the Swing thread) any ongoing asynchronous operation will
     * be exposed to a variety of problems &#151; ranging from staleness of
     * values in lookup tables to indexes out of bounds in said tables to
     * null pointers.  Fortunately, we can simply ignore all this.  In fact, the
     * result could either be an invalid image (the operation read inconsistent
     * values) or an exception.  Either way, the future that represents the 
     * operation will never be accessed: every time a property in the rendering
     * environment changes, the onRenderingPropChange method is eventually 
     * called.  This method clears the cache, which, in turn, discards any
     * future that was added &#151; futures are added to the cache once returned
     * by this class' doCall method.
     */
    private class AsyncRenderOp implements Invocation {
        final Renderer rnd = renderer;
        final PlaneDef pd;
        AsyncRenderOp(PlaneDef pd) { this.pd = pd; }
        Future doCall() {
            CmdProcessor proc = engine.getCmdProcessor();
            return proc.exec(this);
        }
        public Object call() throws Exception { return rnd.render(pd); }
    }
    
    
    /**
     * Creates a new manager to handle the the given pixels set within the 
     * specified image.
     * 
     * @param engine    Reference to the rendering engine.
     * @param imageID   The id of the image the pixels set belongs to.
     * @param pixelsID  The id of the pixels set.
     */
    private RenderingManager(Renderer renderer, RenderingEngine engine)
    {
        this.renderer = renderer;
        this.engine = engine; 
        
        //Create the navigation history.
        PixelsDimensions dims = renderer.getPixelsDims();
        history = new NavigationHistory(2, dims.sizeZ, dims.sizeT);
        //NOTE: When we have a smarter navigation history that actually 
        //takes more than two entries into account when making predictions,
        //we'll have to specify a suitable history size.
        
        //Create the XY images cache.
        clearCache();  //Will work fine b/c we've already built history.
    }
    
    /**
     * Calculates the cache size according to what is specified in the
     * configuration file.
     * 
     * @return  The cache size.
     */
    private int getCacheSize()
    {
        Integer sz = (Integer)
            RenderingEngine.getRegistry().lookup(LookupNames.RE_CACHE_SZ);
        if (sz == null) return 1;  //No caching if entry was ripped up.
        int cacheSize = sz.intValue();  //In Mb, no caching if <=0.
        return (cacheSize <= 0) ? 1 : cacheSize*1024*1024;  
    }
    
    /**
     * Returns the maximum number of moves to be used for predicting how many
     * planes should be pre-fetched and rendered asynchronously.
     * The value comes from the configuration file.
     * 
     * @return  The maximum number of moves.
     */
    private int getMaxMoves()
    {
        Integer maxMoves = (Integer)
            RenderingEngine.getRegistry().lookup(LookupNames.RE_MAX_PRE_FETCH);
        if (maxMoves == null) return 0;  //No async if entry was ripped up.
        return maxMoves.intValue();  //history will turn <=0 into 0.
    }
    
    /**
     * Clears the current {@link #cache} (if any) and creates an empty new one.
     */
    private void clearCache()
    {
        if (cache != null)  //Called after initialization, clear previous one.
            cache.clear();
        
        int imgSz = renderer.getImageSize(new PlaneDef(PlaneDef.XY, 0));
        //NOTE: imgSz depends on rendering strategy.  However, every time
        //the setModel method is called, the onRenderingPropChange method
        //is eventually called too.  B/c onRenderingPropChange calls this
        //method, the cache is rebuilt with the (possibly) new image size.
    
        cache = new ImageFutureCache(getCacheSize(), imgSz, history);
    }
    
    /**
     * Handles rendering of XY planes.
     * 
     * @param pd    Selects an XY plane.  Mustn't be <code>null</code>.
     * @return  A buffered image ready to be displayed on screen.
     * @throws DataSourceException If an error occured while trying to pull out
     *                              data from the pixels data repository.
     * @throws QuantizationException If an error occurred while quantizing the
     *                                  pixels raw data.
     */
    private BufferedImage handleXYRendering(PlaneDef pd)
        throws DataSourceException, QuantizationException
    {
        //First off see if the image is in the cache.  If the image is being
        //rendered, wait until the asynchronous operation terminates.
        BufferedImage img = cache.extract(pd);
        if (img == null) {  
            //The image is not in cache and is not being rendered either.
            //Render in the caller's thread.  So, as in the case above, we
            //have the caller wait.  This works as a back-pressure measure
            //to avoid resource exhaustion.
            img = renderer.render(pd); 
            cache.add(pd, img);
        }
        //Add to history.  So the current move will always be the last
        //plane that was requested and sucessfully rendered.
        history.addMove(pd);
        
        //Try to guess the next moves and start an asynchronous rendering
        //operation for each of those moves.  Handles are added to the cache
        //so that if the next call to this method requests an image that is
        //being rendered asynchronously, we don't start a new operation and
        //we have the caller wait for the ongoing one to complete instead.
        PlaneDef[] nextMoves = history.guessNextMoves(getMaxMoves());
        for (int i = 0; i < nextMoves.length; i++) {
            if (!cache.contains(nextMoves[i])) {
                AsyncRenderOp op = new AsyncRenderOp(nextMoves[i]);
                cache.add(nextMoves[i], op.doCall());  //Adds a Future.
            }
        }
        
        //Finally return the requested image.
        return img;
    }
    
    /**
     * Renders the data selected by <code>pd</code> according to the current
     * rendering settings.
     * The passed argument selects a plane orthogonal to one of the <i>X</i>, 
     * <i>Y</i>, or <i>Z</i> axes.  How many wavelengths are rendered and
     * what color model is used depends on the current rendering settings.
     * 
     * @param pd    Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *              or <i>Z</i> axes.  If <code>null</code>, then the default
     *              plane is rendered.
     * @return  A buffered image ready to be displayed on screen.
     * @throws DataSourceException If an error occured while trying to pull out
     *                              data from the pixels data repository.
     * @throws QuantizationException If an error occurred while quantizing the
     *                                  pixels raw data.
     */
    BufferedImage renderPlane(PlaneDef pd) 
        throws DataSourceException, QuantizationException
    {
        if (pd == null) pd = renderer.getDefaultPlaneDef();
        if (pd.getSlice() == PlaneDef.XY) return handleXYRendering(pd);
        //Else it's a XZ or ZY plane, which requires the whole stack to be 
        //in memory.  The data sink will take care of it when the renderer
        //will request the plane data.
        return renderer.render(pd);
    }
    
    
    /**
     * Creates a proxy to access the rendering environment.
     * 
     * @return  See above.
     */
    RenderingControlProxy createRenderingControlProxy()
    {
        RenderingDef original = renderer.getRenderingDef(), copy;
        copy = original.copy();
        EventBus eventBus = RenderingEngine.getRegistry().getEventBus();
        RenderingControlImpl facade = new RenderingControlImpl(renderer);
        return new RenderingControlProxy(facade, copy, eventBus);
    }
    
    /**
     * Clears all images in cache as they're no longer valid.
     * In fact, the rendering context has changed and the 
     * {@link RenderingEngine} has called this method to notify
     * us about that.
     */
    void onRenderingPropChange()
    {
        clearCache();
    }
    
}
