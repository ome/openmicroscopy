/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer
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
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import ome.model.display.CodomainMapContext;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * 
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
public interface Renderer
    extends ObservableComponent
{
    
    /** 
     * Bound property name indicating to render the plane with the 
     * new rendering settings. 
     */
    public final static String  RENDER_PLANE_PROPERTY = "render_plane";
    
    /** 
     * Bound property name indicating that the contrast stretching selection
     * is available.
     */
    public final static String  CONTRAST_STRETCHING_PROPERTY = 
                                                    "contrastStretching";
    
    /** 
     * Bound property name indicating that the plane slicing selection
     * is available.
     */
    public final static String  PLANE_SLICING_PROPERTY =  "planeSlicing";
    
    /** Bound property name indicating that a new channel is selected. */
    public final static String  SELECTED_CHANNEL_PROPERTY = "selectedChannel";
    
    /**
     * Returns the current state.
     * 
     * @return See above
     */
    public int getState();
    
    /** Closes and disposes. */
    public void discard();
    
    /** Moves the window to front and de-iconifies it necessary. */
    public void moveToFront();

    /**
     * Updates the codomain map corresponding to the specified 
     * {@link CodomainMapContext}.
     * 
     * @param ctx The codomain map context.
     */
    void updateCodomainMap(CodomainMapContext ctx);
    
    /**
     * Removes the codomain map identified by the class from the list of 
     * codomain transformations.
     * 
     * @param mapType The codomain map context type.
     */
    void removeCodomainMap(Class mapType);
    
    /**
     * Adds the codomain map identified by the class to the list of 
     * codomain transformations.
     * 
     * @param mapType The codomain map context type.
     */
    void addCodomainMap(Class mapType);
    
    /** 
     * Sets the pixels intensity interval for the
     * currently selected channel.
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     * @param released  If <code>true</code>, we fire a property change event
     *                  to render a new plane.
     */
    void setInputInterval(double s, double e, boolean released);

    
    /** 
     * Sets the sub-interval of the device space. 
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     * @param released  If <code>true</code>, we fire a property change event
     *                  to render a new plane.
     */
    void setCodomainInterval(int s, int e, boolean released);
    
    /**
     * Sets the bit resolution and updates the image.
     * 
     * @param v The new bit resolution.
     */
    void setBitResolution(int v);
    
    /**
     * Sets the selected channel.
     * 
     * @param c The new selected channel.
     * @param b Flag to fire a property change. Pass <code>true</code> to fire
     *          a property change, <code>false</code> otherwise.
     */
    void setSelectedChannel(int c, boolean b);
    
    /**
     * Sets the family and updates the image.
     * 
     * @param family The new family value.
     */
    void setFamily(String family);
    
    /**
     * Sets the coefficient identifying a curve in the family
     * and updates the image.
     * 
     * @param k The new curve scoefficient.
     */
    void setCurveCoefficient(double k);
    
    /**
     * Sets the noise reduction flag to select the mapping algorithm
     * and updates the image.
     * 
     * @param b The noise reduction flag.
     */
    void setNoiseReduction(boolean b);
    
    /**
     * Returns the top model of this component.
     * 
     * @return See above.
     */
    ImViewer getParentModel();
    
    CodomainMapContext getCodomainMapContext(Class mapType);
    
}
