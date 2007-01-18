/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ZoomCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Magnifies the selected {@link ImageDisplay}s.
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
public class ZoomCmd
    implements ActionCmd
{

    /** Identifies the <i>Zoom in</i> action. */
    public static final int     ZOOM_IN = 0;
    
    /** Identifies the <i>Zoom out</i> action. */
    public static final int     ZOOM_OUT = 1;
    
    /** Identifies the <i>Zoom fit</i> action. */
    public static final int     ZOOM_FIT = 2;
    
    /** The value by the magnification factor is incremented. */
    private static final double INCREMENT = 0.25;
    
    /** Reference to the model. */
    private HiViewer    model;
    
    /** One of the indexes defined by this class. */
    private static int  index;
    
    /**
     * Checks if the index is supported.
     * 
     * @param index The passed index.
     * @return <code>true</code> if supported.
     */
    private boolean checkIndex(int index)
    {
        switch (index) {
            case ZOOM_IN:
            case ZOOM_OUT:
            case ZOOM_FIT:    
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param i The zoom index. One of the indexes defined by this class.
     */
    public ZoomCmd(HiViewer model, int i)
    {
        if (model == null)
            throw new IllegalArgumentException("No model.");
        if (!checkIndex(i))
            throw new IllegalArgumentException("Index not valid.");
        this.model = model;
        index = i;
    }
 
    /**
     * Updates the passed magnification factor according to the index. 
     * 
     * @param currentScale The current magnification factor.
     * @return The updated magnification factor.
     */
    static double calculateFactor(double currentScale)
    {
        double factor = currentScale;
        switch (index) {
            case ZOOM_IN:
                if (currentScale >= Thumbnail.MAX_SCALING_FACTOR) 
                    factor = Thumbnail.MAX_SCALING_FACTOR;
                else factor += INCREMENT;
                break;
            case ZOOM_OUT:
                if (currentScale <= Thumbnail.MIN_SCALING_FACTOR) 
                    factor = Thumbnail.MIN_SCALING_FACTOR;
                else factor -= INCREMENT;
                break;
            case ZOOM_FIT:
                factor = Thumbnail.SCALING_FACTOR;    
        }
        return factor;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        ZoomVisitor visitor = new ZoomVisitor(model);
        Browser browser = model.getBrowser();
        if (browser == null) return;
        int layout = browser.getSelectedLayout();
        if (layout == LayoutFactory.FLAT_LAYOUT) {
            browser.accept(visitor, ImageDisplayVisitor.IMAGE_NODE_ONLY);
        } else {
            ImageDisplay selectedDisplay = browser.getLastSelectedDisplay();
            if (selectedDisplay == null) return;
            if (selectedDisplay.getParentDisplay() == null) return;
            selectedDisplay.accept(visitor);
        }
        model.layoutZoomedNodes();
    }

}
