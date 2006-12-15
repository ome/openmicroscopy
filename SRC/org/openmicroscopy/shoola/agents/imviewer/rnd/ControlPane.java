/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.ControlPane
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.rnd;




//Java imports
import javax.swing.Icon;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Top-class that each pane hosting mapping controls should extend.
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
abstract class ControlPane
    extends JPanel
{
    
    /** The index of the domain. */
    static final int    DOMAIN_PANE_INDEX = 0;
    
    /** The index of the codomain. */
    static final int    CODOMAIN_PANE_INDEX = 1;
    
    /** Bounds property indicating that a family is selected. */
    static final String FAMILY_PROPERTY = "family";
    
    /** Bounds property indicating that a family is selected. */
    static final String GAMMA_PROPERTY = "gamma";
    
    /** Bounds property indicating that a family is selected. */
    static final String BIT_RESOLUTION_PROPERTY = "bit_resolution";
    
    //static final String CHANNEL_SELECTION_PROPERTY = "channel_selection";
    
    /** Reference to the Model.*/
    protected RendererModel     model;
    
    /** Reference to the Control.*/
    protected RendererControl   controller;

    /** Reference to the View. */
    protected RendererUI        view;
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     */
    protected ControlPane(RendererModel model, RendererControl controller)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (controller == null) throw new NullPointerException("No control.");
        this.model = model;
        this.controller = controller;
    }
    
    /**
     * Returns the name of the pane.
     * 
     * @return See above
     */
    protected abstract String getPaneName();
    
    /**
     * Returns the icon attached to the pane.
     * 
     * @return See above.
     */
    protected abstract Icon getPaneIcon();
    
    /**
     * Returns the description of the pane.
     * 
     * @return See above.
     */
    protected abstract String getPaneDescription();
    
    /**
     * Returns the index of the component. One of the constants defined 
     * by this class.
     * 
     * @return See above.
     */
    protected abstract int getPaneIndex();

    /** Resets the default rendering settings. */
    protected abstract void resetDefaultRndSettings();
    
}
