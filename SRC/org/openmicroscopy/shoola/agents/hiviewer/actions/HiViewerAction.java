/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.HiViewerAction
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

package org.openmicroscopy.shoola.agents.hiviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Top class that each action should extend.
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
public abstract class HiViewerAction
    extends AbstractAction
    implements ChangeListener, PropertyChangeListener
{
    
    /** A reference to the Model. */
    protected HiViewer      model;
    
    /**
     * Callback to notify of a change in the currently selected display
     * in the {@link Browser}. Subclasses override the method.
     * 
     * @param selectedDisplay The newly selected display node.
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay) {}
    
    /**
     * Callback to notify of a state change in the  {@link Browser}.
     * Subclasses override the method.
     */
    protected void onStateChange() {}
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public HiViewerAction(HiViewer model)
    {
        super();
        setEnabled(false);
        if (model == null) throw new IllegalArgumentException("no hiViewer");
        this.model = model;
        model.addChangeListener(this);
    }
 
    /** 
     * Subclasses should implement the method.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}

    /**
     * Reacts to property changes in the {@link Browser}.
     * Highlights the selected node, and update the status of the
     * action.
     * @see #propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        Browser browser = model.getBrowser();
        if (browser != null) onDisplayChange(browser.getLastSelectedDisplay());
        else onDisplayChange(null);
    }

    /** 
     * Listens to {@link Browser} change events. 
     * @see #stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        int state = model.getState();
        if ((state == HiViewer.LOADING_THUMBNAILS) || 
            (state == HiViewer.READY && model.getBrowser().getImages().size()
                        == 0)) {
            model.getBrowser().addPropertyChangeListener(
                    Browser.SELECTED_DISPLAY_PROPERTY, this);
            model.getBrowser().addPropertyChangeListener(
                    Browser.LAYOUT_PROPERTY, this);
            onStateChange();
        }
    }
    
}
