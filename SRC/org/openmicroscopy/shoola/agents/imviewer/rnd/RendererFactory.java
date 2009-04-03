/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.RendererFactory
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
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;

/** 
 * Factory to create the {@link Renderer} components.
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
public class RendererFactory
{

	/** 
	 * Factor used to determine the percentage of the range added 
	 * (resp. removed) to (resp. from) the maximum (resp. the minimum).
	 */
	private static final double RATIO = 0.2;
	
	/**
	 * Initializes the values of the passed slider.
	 * 
	 * @param slider	The slider to handle.
	 * @param absMin	The absolute minimum value.
	 * @param absMax	The absolute maximum value.
	 * @param min		The minimum value.
	 * @param max		The maximum value.
	 * @param s			The start value.
	 * @param e			The end value.
	 */
	public static void initSlider(TwoKnobsSlider slider, int absMin, int absMax, 
			int min, int max, int s, int e)
	{
        double range = (max-min)*RATIO;
        int lowestBound = (int) (min-range);
        if (lowestBound < absMin) lowestBound = absMin;
        int highestBound = (int) (max+range);
        if (highestBound > absMax) highestBound = absMax;
        slider.setValues(highestBound, lowestBound, max, min, s, e);
	}
	
    /**
     * Creates a new {@link Renderer}.
     * 
     * @param viewer        Reference to the {@link ImViewer}, this component is
     *                      for. Mustn't be <code>null</code>.
     * @param rndControl    Reference to the component that controls the
     *                      rendering settings. Mustn't be <code>null</code>.
     * @param metadataView	The view of the metadata.
     * @return See above.
     */
    public static Renderer createRenderer(ImViewer viewer,
                                            RenderingControl rndControl, 
                                            JComponent metadataView)
    {
        RendererModel model = new RendererModel(viewer, rndControl);
        RendererComponent rnd = new RendererComponent(model);
        rnd.initialize(metadataView);
        return rnd;
    }
    
}
