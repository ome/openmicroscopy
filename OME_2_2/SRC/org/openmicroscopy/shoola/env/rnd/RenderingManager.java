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
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantizationException;
import org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.ExecException;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.concur.tasks.Invocation;

/** 
 * 
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

    private final Renderer        renderer;
    private PlaneDef        curPlaneDef;
    private final Future[] stackCache;
    private final CmdProcessor    cmdProcessor;
    
    private class RenderXYPlane
        implements Invocation
    {
        final Renderer rnd;
        RenderXYPlane(int z) {
            PlaneDef planeDef = new PlaneDef(PlaneDef.XY, curPlaneDef.getT());
            planeDef.setZ(z);
            rnd = renderer.makeShallowCopy(planeDef);
        }
        public Object call() throws Exception {
long start = System.currentTimeMillis();
Object ret = rnd.render();
System.err.println("Plane "+rnd.getPlaneDef().getZ()+" rendered in: "+(System.currentTimeMillis()-start));
return ret;
            //return rnd.render();  //Uses plane def set in constuctor.
        }
    }
    
    
    private void clearCache()
    {
        for (int i = 0; i < stackCache.length; ++i)
            if (stackCache[i] != null) {  //Cancel and discard any ongoing.
                stackCache[i].cancelExecution();
                stackCache[i] = null;
            }
    }
    
    private void updatePlaneDef(PlaneDef newDef)
    {
        //Passed argument will be null the very first time a RenderImage 
        //request is made.  Just grab the default one that will be used
        //by the renderer. 
        if (newDef == null) newDef = renderer.getPlaneDef();
        
        //Clear cache if user's moving to a different timepoint.
        if (curPlaneDef != null && curPlaneDef.getT() != newDef.getT())
            clearCache();
        
        //Set the current definition.
        curPlaneDef = newDef;
    }
    
    private BufferedImage extractXYImage(int z)
        throws DataSourceException, QuantizationException
    { //We assume z is in the right range.  This is checked by renderXYPlane.              
        BufferedImage plane = null;
        try {
            plane = (BufferedImage) stackCache[z].getResult();
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
    
    RenderingManager(Renderer r, CmdProcessor cp)
    {
        if (r == null) throw new NullPointerException("No renderer.");
        if (cp == null) throw new NullPointerException("No command processor."); 
        renderer = r;
        cmdProcessor = cp;
        PixelsDimensions dims = renderer.getPixelsDims();
        stackCache = new Future[dims.sizeZ];
    }
    
    BufferedImage renderXYPlane(PlaneDef pd)
        throws DataSourceException, QuantizationException
    {
        updatePlaneDef(pd);
        //if (pd == null) return renderer.render();
        //return renderer.render(pd);
        
        int minZ = Math.max(curPlaneDef.getZ()-0, 0),
            maxZ = Math.min(curPlaneDef.getZ()+0, stackCache.length-1);
//long start = System.currentTimeMillis();
        for (int z = minZ; z <= maxZ; ++z) 
            if (stackCache[z] == null) 
                stackCache[z] = cmdProcessor.exec(new RenderXYPlane(z));
//System.out.println("Threads spwaned in: "+(System.currentTimeMillis()-start));
//start = System.currentTimeMillis();
//BufferedImage ret = extractXYImage(curPlaneDef.getZ());
//System.out.println("Image extracted in: "+(System.currentTimeMillis()-start));
//return ret;
        return extractXYImage(curPlaneDef.getZ());
    }
    
    void onRenderingPropChange()
    {
//long start = System.currentTimeMillis();
        clearCache();
//System.out.println("Stack cleared in: "+(System.currentTimeMillis()-start));
    }
    
}
