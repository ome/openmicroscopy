/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserAction
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

package org.openmicroscopy.shoola.agents.treeviewer.actions;

//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;


/** 
 * Top class that each action should extend.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class BrowserAction
    extends AbstractAction
    implements ChangeListener, PropertyChangeListener
{

    /** A reference to the Model. */
    protected Browser      model;
    
    /**
     * Callback to notify of a change in the currently selected display
     * in the {@link Browser}. Subclasses override the method.
     * 
     * @param selectedDisplay The newly selected display node.
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay) {}
    
    /** Reacts to state changes. Subclasses override the method. */
    protected void onStateChange() {}
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public BrowserAction(Browser model)
    {
        super();
        setEnabled(false);
        if (model == null) throw new IllegalArgumentException("No Model.");
        this.model = model;
        model.addChangeListener(this);
        model.addPropertyChangeListener(
        		Browser.SELECTED_TREE_NODE_DISPLAY_PROPERTY, this);
    }

    /** 
     * Subclasses need to override this method.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}
    
    /**
     * Reacts to property changes in the {@link Browser}.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getNewValue() == null) {
            onDisplayChange(null);
            return;
        }
        if (evt.getNewValue().equals(evt.getOldValue())) return;
        onDisplayChange((TreeImageDisplay) evt.getNewValue());
    }
    
    /** 
     * Reacts to state changes in the {@link Browser}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) { onStateChange(); }
    
}
