/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction
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

package org.openmicroscopy.shoola.agents.treeviewer.actions;



//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;

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
public abstract class TreeViewerAction
    extends AbstractAction
    implements ChangeListener, PropertyChangeListener
{

    
    /** A reference to the Model. */
    protected TreeViewer    model;
    
    /** The name of the action. */
    protected String        name;
    
    /**
     * Callback to notify of a change in the currently selected display
     * in the {@link Browser}. Subclasses override the method.
     * 
     * @param selectedDisplay The newly selected display node.
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay) {}
    
    /** 
     * Callback to notify a state change in the {@link Browser}. 
     * Subclasses override the method.
     * 
     * @param browser The browser which fired the state change.
     */
    protected void onBrowserStateChange(Browser browser) {} ;
    
    /**
     * Callback to notify that a new browser is selected.
     * Subclasses override the method.
     * 
     * @param browser The selected browser.
     */
    protected void onBrowserSelection(Browser browser) {};
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public TreeViewerAction(TreeViewer model)
    {
        super();
        setEnabled(false);
        if (model == null) throw new IllegalArgumentException("no hiViewer");
        this.model = model;
        //Attaches listener property change listener to each browser.
       
        model.addPropertyChangeListener(
                TreeViewer.SELECTED_BROWSER_PROPERTY, this);
        Map browsers = model.getBrowsers();
        Iterator i = browsers.values().iterator();
        Browser browser;
        while (i.hasNext()) {
            browser = (Browser) i.next();
            browser.addPropertyChangeListener(
                    Browser.SELECTED_DISPLAY_PROPERTY, this);
            browser.addChangeListener(this);
        }    
    }
    
    /**
     * Returns the name of the action.
     * 
     * @return See above.
     */
    public String getActionName()
    { 
        if (name == null || name.length() == 0)
            return System.getProperty(Action.NAME); 
        return name;
    }
    
    /** 
     * Subclasses implement the method.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}

    /**
     * Reacts to property changes {@link Browser#SELECTED_DISPLAY_PROPERTY}
     * event fired by the {@link Browser} and to
     * the {@link TreeViewer#SELECTED_BROWSER_PROPERTY} event.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (name.equals(TreeViewer.SELECTED_BROWSER_PROPERTY)) {
            onBrowserSelection((Browser) evt.getNewValue());
            return;
        }
        Object newValue = evt.getNewValue();
        if (newValue == null) {
            onDisplayChange(null);
            return;
        }
        if (newValue.equals(evt.getOldValue())) return;
        onDisplayChange((TreeImageDisplay) newValue);
    }
    
    /** 
     * Reacts to state changes in the {@link Browser}. 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        Object source = e.getSource();
        if (source instanceof Browser) 
            onBrowserStateChange((Browser) source);
    }
    
}
