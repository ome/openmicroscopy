/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ZoomCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.ThumbnailProvider;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.layout.Layout;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Magnifies the selected {@link ImageDisplay} object.
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
                if (currentScale >= ThumbnailProvider.MAX_SCALING_FACTOR) 
                    factor = ThumbnailProvider.MAX_SCALING_FACTOR;
                else factor += INCREMENT;
                break;
            case ZOOM_OUT:
                if (currentScale <= ThumbnailProvider.MIN_SCALING_FACTOR) 
                    factor = ThumbnailProvider.MIN_SCALING_FACTOR;
                else factor -= INCREMENT;
                break;
            case ZOOM_FIT:
                factor = ThumbnailProvider.SCALING_FACTOR;    
        }
        return factor;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        ZoomVisitor visitor = new ZoomVisitor(model);
        Browser browser = model.getBrowser();
        ImageDisplay selectedDisplay = browser.getLastSelectedDisplay();
        if (selectedDisplay.getParentDisplay() == null) return;
        selectedDisplay.accept(visitor);
        if (selectedDisplay instanceof ImageSet) {
            Layout layout = LayoutFactory.createLayout(
                    			LayoutFactory.SQUARY_LAYOUT);
            layout.visit((ImageSet) selectedDisplay);
        } 
    }

}
