/*
 * org.openmicroscopy.shoola.agents.browser.BrowserModelListener
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Defines an interface that is responsive to changes made to the browser
 * model by a controller or external source.  The BrowserView should
 * implement this interface in order to listen to external changes in the
 * model (and redraw itself appropriately)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface BrowserModelListener
{
    /**
     * Indicates that the model has updated, and the view should
     * redraw its children, or repaint, or something to indicate the
     * underlying change.  This isn't very informative, but the view
     * should know how to draw a model each time.
     */
    public void modelUpdated();
    
    /**
     * Changes the major UI mode of the browser model.
     * @param className The browser mode class which this applies to.
     * @param mode The new mode of the model.
     */
    public void modeChanged(String className, BrowserMode mode);
    
    /**
     * Instructs the view to repaint, as the underlying thumbnail drawing
     * methods have changed.
     *
     */
    public void paintMethodsChanged();
    
    /**
     * Indicates that the specified thumbnails have been selected.
     * @param thumbnails The array of selected thumbnails.
     */
    public void thumbnailsSelected(Thumbnail[] thumbnails);
    
    /**
     * Indicates that the specified thumbnails have been deselected.
     * @param thumbnails The array of deselected thumbnails.
     */
    public void thumbnailsDeselected(Thumbnail[] thumbnails);
}
