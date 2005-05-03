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
    
    protected HiViewer      model;
    
    public HiViewerAction(HiViewer model)
    {
        super();
        setEnabled(false);
        if (model == null) throw new IllegalArgumentException("no hiViewer");
        this.model = model;
        model.addChangeListener(this);
    }

    protected abstract void onDisplayChange(ImageDisplay selectedDisplay);
      
    /** 
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}

    /**
     * Reacts to property changes in the {@link Browser}.
     * Highlights the selected node, and update the status of the
     * action.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (!(evt.getNewValue().equals(evt.getOldValue())) 
                && evt.getNewValue() != null) {
            ImageDisplay oldNode, newNode;
            Colors colors = Colors.getInstance();
            newNode = (ImageDisplay) evt.getNewValue();
            newNode.setHighlight(colors.getSelectedHighLight(newNode));
            if (evt.getOldValue() != null) {
                oldNode = (ImageDisplay) evt.getOldValue();
                oldNode.setHighlight(
                        colors.getDeselectedHighLight(oldNode));
            }
        }
        onDisplayChange(model.getBrowser().getSelectedDisplay());
    }

    /** Listen to change events. */
    public void stateChanged(ChangeEvent e)
    {
        if (model.getState() == HiViewer.LOADING_THUMBNAILS) {
            model.getBrowser().addPropertyChangeListener(
                    Browser.SELECTED_DISPLAY_PROPERTY, this);
        } 
    }
    
}
