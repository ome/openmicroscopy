/*
 * org.openmicroscopy.shoola.agents.browser.BrowserModelAdapter
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
 * Adapter class for BrowserModelListener. 
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserModelAdapter implements BrowserModelListener
{
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#modeChanged(java.lang.String, org.openmicroscopy.shoola.agents.browser.BrowserMode)
     */
    public void modeChanged(String className, BrowserMode mode)
    {
        // TODO Auto-generated method stub

    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#modelUpdated()
     */
    public void modelUpdated()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#paintMethodsChanged()
     */
    public void paintMethodsChanged()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailAdded(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public void thumbnailAdded(Thumbnail t)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailRemoved(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public void thumbnailRemoved(Thumbnail t)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsAdded(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsAdded(Thumbnail[] t)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsDeselected(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsDeselected(Thumbnail[] thumbnails)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsRemoved(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsRemoved(Thumbnail[] t)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsSelected(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsSelected(Thumbnail[] thumbnails)
    {
        // TODO Auto-generated method stub

    }


}
