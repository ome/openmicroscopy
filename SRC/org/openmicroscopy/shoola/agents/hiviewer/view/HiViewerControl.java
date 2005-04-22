/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerControl
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.actions.BrowserAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;

/** 
 * The HiViewer's Controller.
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
public class HiViewerControl
    implements PropertyChangeListener
{
    
    private HiViewer    view;
    private Browser     browser;
    
    
    HiViewerControl() {}
    
    public void initialize(HiViewer view, Browser browser)
    {
        this.view = view;
        this.browser = browser;
        Action[] actions = view.getActions();
        for (int i = 0; i < actions.length; i++)
            ((BrowserAction) actions[i]).setBrowser(browser);
        browser.addPropertyChangeListener(Browser.POPUP_POINT_PROPERTY, this);
        browser.addPropertyChangeListener(Browser.THUMB_SELECTED_PROPERTY, 
                                          this);
    }

    /* (non-Javadoc)
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        ImageDisplay d = browser.getSelectedDisplay();
        if (Browser.POPUP_POINT_PROPERTY.equals(pce.getPropertyName())) {
            Point p = browser.getPopupPoint();
            if (d != null && p != null) view.getPopupMenu().show(d, p.x, p.y);
        } else {  //THUMB_SELECTED_PROPERTY
            TWinManager.display((ImageNode) d);
        }
    }
    
}
