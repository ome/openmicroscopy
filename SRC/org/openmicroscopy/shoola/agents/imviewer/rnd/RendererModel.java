/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.RendererModel
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;



//Java imports
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import ome.model.display.CodomainMapContext;
import ome.model.display.ContrastStretchingContext;
import ome.model.display.PlaneSlicingContext;
import ome.model.display.ReverseIntensityContext;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;


/** 
 * The Model component in the <code>Renderer</code> MVC triad.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class RendererModel
{

    /** Identifies the minimum value of the device space. */
    static final int    CD_START = 0;
    
    /** Identifies the maximum value of the device space. */
    static final int    CD_END = 255;
    
    /** Flag to select a 1-bit depth (<i>=2^1-1</i>) output interval. */
    static final int   DEPTH_1BIT = RenderingControl.DEPTH_1BIT;

    /** Flag to select a 2-bit depth (<i>=2^2-1</i>) output interval. */
    static final int   DEPTH_2BIT = RenderingControl.DEPTH_2BIT;
    
    /** Flag to select a 3-bit depth (<i>=2^3-1</i>) output interval. */
    static final int   DEPTH_3BIT = RenderingControl.DEPTH_3BIT;
    
    /** Flag to select a 4-bit depth (<i>=2^4-1</i>) output interval. */
    static final int   DEPTH_4BIT = RenderingControl.DEPTH_4BIT;
    
    /** Flag to select a 5-bit depth (<i>=2^5-1</i>) output interval. */
    static final int   DEPTH_5BIT = RenderingControl.DEPTH_5BIT;
    
    /** Flag to select a 6-bit depth (<i>=2^6-1</i>) output interval. */
    static final int   DEPTH_6BIT = RenderingControl.DEPTH_6BIT;
    
    /** Flag to select a 7-bit depth (<i>=2^7-1</i>) output interval. */
    static final int   DEPTH_7BIT = RenderingControl.DEPTH_7BIT;
    
    /** Flag to select a 8-bit depth (<i>=2^8-1</i>) output interval. */
    static final int   DEPTH_8BIT = RenderingControl.DEPTH_8BIT;
    
    /** Identifies the <code>Linear</code> family. */
    static final String LINEAR = RenderingControl.LINEAR;
    
    /** Identifies the <code>Exponential</code> family. */
    static final String LOGARITHMIC = RenderingControl.LOGARITHMIC;
    
    /** Reference to the {@link ImViewer viewer}. */
    private ImViewer            parentModel;
    
    /** Reference to the component that embeds this model. */
    private Renderer            component;
    
    /** Reference to the rendering control. */
    private RenderingControl    rndControl;
    
    private int                 state;
    
    /** The index of the selected channel. */
    private int                 selectedChannelIndex;
    
    /** Flag to denote if the widget is visible or not. */
    private boolean             visible;
    
    /**
     * Creates a new instance.
     * 
     * @param parentModel   Reference to the {@link ImViewer}.
     *                      Mustn't be <code>null</code>.
     * @param rndControl    Reference to the component that controls the
     *                      rendering settings. Mustn't be <code>null</code>.
     */
    RendererModel(ImViewer parentModel, RenderingControl rndControl)
    {
        if (parentModel == null) 
            throw new NullPointerException("No parent model.");
        if (rndControl == null)
            throw new NullPointerException("No rendering control.");
        this.parentModel = parentModel;
        this.rndControl = rndControl;
        visible = false;
        //state = Renderer.READY;
    }
    
    /**
     * Returns the status of the window.
     * 
     * @return See above.
     */
    boolean isVisible() { return visible; }
    
    /**
     * Called by the <code>Renderer</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Renderer component) { this.component = component; }

    void discard() 
    {
        //state = Renderer.DISCARDED;
    }
    
    int getState() { return state; }

    /**
     * Sets the upper bound of the pixels intensity interval.
     * 
     * @param end The upper bound to set.
     */
    void setInputEnd(double end)
    {
       rndControl.setChannelWindow(selectedChannelIndex, getWindowStart(), 
                                   end);
    } 

    /**
     * Sets the pixels intensity interval for the
     * currently selected channel.
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     */
    void setInputInterval(double s, double e)
    {
        rndControl.setChannelWindow(selectedChannelIndex, s, e);
    }

    /**
     * Returns the upper bound of the sub-interval of the device space.
     * 
     * @return See above.
     */
    int getCodomainEnd()
    { 
        return rndControl.getQuantumDef().getCdEnd().intValue();
    }
    
    /**
     * Returns the lower bound of the sub-interval of the device space.
     * 
     * @return See above.
     */
    int getCodomainStart()
    { 
        return rndControl.getQuantumDef().getCdStart().intValue();
    }
    
    /**
     * Sets the sub-interval of the device space. 
     * 
     * @param s The lower bound of the interval.
     * @param e The upper bound of the interval.
     */
    void setCodomainInterval(int s, int e)
    {
        rndControl.setCodomainInterval(s, e);
    }

    /**
     * Sets the quantum strategy.
     * 
     * @param v The bit resolution defining the strategy.
     */
    void setBitResolution(int v)
    {
        rndControl.setQuantumStrategy(v);
    }

    /**
     * Sets the selected channel.
     * 
     * @param index The index of the selected channel.
     */
    void setSelectedChannel(int index) { selectedChannelIndex = index; }

    /**
     * Sets, for the currently selected channel, the family used during 
     * the mapping process.
     * 
     * @param family The family to set.
     */
    void setFamily(String family)
    {
        boolean b = rndControl.getChannelNoiseReduction(selectedChannelIndex);
        double k = rndControl.getChannelCurveCoefficient(selectedChannelIndex);
        rndControl.setQuantizationMap(selectedChannelIndex, family, k, b);
    }

    /**
     * Selects one curve in the family.
     * 
     * @param k The coefficient identifying a curve within a family.
     */
    void setCurveCoefficient(double k)
    {
        boolean b = rndControl.getChannelNoiseReduction(selectedChannelIndex);
        String family = rndControl.getChannelFamily(selectedChannelIndex);
        rndControl.setQuantizationMap(selectedChannelIndex, family, k, b);
    } 
    
    /**
     * Turns on and off the noise reduction algortihm mapping.
     * 
     * @param b Pass <code>true</code>  to turn it on,
     *          <code>false</code> otherwise.
     */
    void setNoiseReduction(boolean b)
    {
        String family = rndControl.getChannelFamily(selectedChannelIndex);
        double k = rndControl.getChannelCurveCoefficient(selectedChannelIndex);
        rndControl.setQuantizationMap(selectedChannelIndex, family, k, b);
    }

    /**
     * Upates the specified {@link CodomainMapContext context}.
     * 
     * @param ctx The context to update.
     */
    void updateCodomainMap(CodomainMapContext ctx)
    {
        rndControl.updateCodomainMap(ctx);
    }

    /**
     * Returns the codomain map context corresponding to the specified 
     * <code>codomain</code> class. Returns <code>null</code> if there is no
     * context matching the class.
     * 
     * @param mapType The class corresponding to the context to retrieve.
     * @return See above.
     */
    CodomainMapContext getCodomainMap(Class mapType)
    {
        List maps = getCodomainMaps();
        Iterator i = maps.iterator();
        CodomainMapContext ctx;
        while (i.hasNext()) {
            ctx = (CodomainMapContext) i.next();
            if (ctx.getClass().equals(mapType)) return ctx;
        }
        return null;
    }
    
    /**
     * Returns a read-only list of {@link CodomainMapContext}s using during
     * the mapping process in the device space.
     * 
     * @return See above.
     */
    List getCodomainMaps() { return rndControl.getCodomainMaps(); }
    
    /**
     * Returns a reference to the {@link ImViewer viewer}.
     * 
     * @return See above.
     */
    ImViewer getParentModel() { return parentModel; }

    /**
     * Removes the codomain map identified by the class from the chain of 
     * codomain transformations.
     * 
     * @param mapType   The type to identify the codomain map.
     */
    void removeCodomainMap(Class mapType)
    {
        CodomainMapContext ctx = getCodomainMap(mapType);
        if (ctx != null) rndControl.removeCodomainMap(ctx);
    }

    /**
     * Adds the codomain map identified by the class to the chain of 
     * codomain transformations.
     * 
     * @param mapType   The type to identify the codomain map.
     */
    void addCodomainMap(Class mapType)
    {
        if (mapType.equals(ReverseIntensityContext.class)) {
            ReverseIntensityContext riCtx = new ReverseIntensityContext();
            riCtx.setReverse(Boolean.TRUE);
            rndControl.addCodomainMap(riCtx);
        } else if (mapType.equals(PlaneSlicingContext.class)) {
            
        } else if (mapType.equals(ContrastStretchingContext.class)) {
            
        }
    }

    /** 
     * Returns the index of the currently selected channel. 
     * 
     * @return See above.
     */
    int getSelectedChannel() { return selectedChannelIndex; }

    /**
     * Returns a list of available mapping families.
     * 
     * @return See above.
     */
    List getFamilies() { return rndControl.getFamilies(); }
    
    /**
     * Returns the mapping family used for to map the selected channel.
     * 
     * @return See above.
     */
    String getFamily()
    {
        return rndControl.getChannelFamily(selectedChannelIndex);
    }
    
    /**
     * Returns the map selected in the family for the selected channel.
     * 
     * @return See above.
     */
    double getCurveCoefficient()
    {
        
        return rndControl.getChannelCurveCoefficient(selectedChannelIndex);
    }
    
    /**
     * Returns the bit resolution value.
     * 
     * @return See above.
     */
    int getBitResolution()
    {
        return rndControl.getQuantumDef().getBitResolution().intValue();
    }
    
    /**
     * Returns <code>true</code> if the noise reduction flag is turned on 
     * for the selected channel, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isNoiseReduction()
    {
        return rndControl.getChannelNoiseReduction(selectedChannelIndex);
    }
    
    /**
     * Returns a list of <code>Channel Data</code> objects.
     * 
     * @return See above.
     */
    ChannelMetadata[] getChannelData()
    {
        return rndControl.getChannelData();
    }
    
    /**
     * Returns the global minimum of the currently selected channel.
     * 
     * @return See above.
     */
    double getGlobalMin()
    {
        return rndControl.getChannelData(selectedChannelIndex).getGlobalMin();
    }
    
    /**
     * Returns the global maximum of the currently selected channel.
     * 
     * @return See above.
     */
    double getGlobalMax()
    {
        return rndControl.getChannelData(selectedChannelIndex).getGlobalMax();
    }
    
    /**
     * Returns the lower bound of the pixels intensity interval of the 
     * currently selected channel.
     * 
     * @return See above.
     */
    double getWindowStart()
    {
        return rndControl.getChannelWindowStart(selectedChannelIndex);
    }
    
    /**
     * Returns the upper bound of the pixels intensity interval of the 
     * currently selected channel.
     * 
     * @return See above.
     */
    double getWindowEnd()
    {
        return rndControl.getChannelWindowEnd(selectedChannelIndex);
    }
    
}
