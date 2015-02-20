/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
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
public abstract class TreeViewerAction
    extends AbstractAction
    implements ChangeListener, PropertyChangeListener
{

    /** A reference to the Model. */
    protected TreeViewer model;

    /** The name of the action. */
    protected String name;

    /** The description of the action. */
    protected String description;

    /**
     * Call-back to notify of a change in the currently selected display
     * in the {@link Browser}. Subclasses override the method.
     * 
     * @param selectedDisplay The newly selected display node.
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay) {}

    /** 
     * Call-back to notify a state change in the {@link Browser}. 
     * Subclasses override the method.
     * 
     * @param browser The browser which fired the state change.
     */
    protected void onBrowserStateChange(Browser browser) {}

    /**
     * Call-back to notify that a new browser is selected.
     * Subclasses override the method.
     * 
     * @param browser The selected browser.
     */
    protected void onBrowserSelection(Browser browser) {}

    /** Call-back to notify that the display mode has changed. */
    protected void onDisplayMode() {}

    /** Call-back to notify any on-going image import. */
    protected void onDataImport() {}

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public TreeViewerAction(TreeViewer model)
    {
        super();
        setEnabled(false);
        if (model == null) throw new IllegalArgumentException("no TreeViewer");
        this.model = model;
        //Attaches listener property change listener to each browser.

        model.addPropertyChangeListener(this);
        Map<Integer, Browser> browsers = model.getBrowsers();
        Iterator<Browser> i = browsers.values().iterator();
        Browser browser;
        while (i.hasNext()) {
            browser = i.next();
            browser.addPropertyChangeListener(
                    Browser.SELECTED_TREE_NODE_DISPLAY_PROPERTY, this);
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
        if (CommonsLangUtils.isEmpty(name)) return (String) getValue(Action.NAME);
        return name;
    }

    /**
     * Returns the name of the action.
     * 
     * @return See above.
     */
    public String getActionDescription()
    { 
        if (CommonsLangUtils.isEmpty(description))
            return (String) getValue(Action.SHORT_DESCRIPTION);
        return description;
    }
    
    /** 
     * Subclasses implement the method.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}

    /**
     * Reacts to property changes
     * {@link Browser#SELECTED_TREE_NODE_DISPLAY_PROPERTY}
     * event fired by the {@link Browser} and to
     * the {@link TreeViewer#SELECTED_BROWSER_PROPERTY} event.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (TreeViewer.SELECTED_BROWSER_PROPERTY.equals(name)) {
            onBrowserSelection((Browser) evt.getNewValue());
        } else if (TreeViewer.DISPLAY_MODE_PROPERTY.equals(name)) {
            int displayMode = ((Integer) evt.getNewValue()).intValue();
            switch (displayMode) {
            case TreeViewer.SEARCH_MODE:
                setEnabled(true);
                break;
            case TreeViewer.EXPLORER_MODE:
                setEnabled(true);
                Browser browser = model.getSelectedBrowser();
                TreeImageDisplay v = null;
                if (browser != null) v = browser.getLastSelectedDisplay();
                onBrowserStateChange(browser);
                onDisplayChange(v);
                break;
            }
            onDisplayMode();
        } else if (TreeViewer.ON_COMPONENT_STATE_CHANGED_PROPERTY.equals(
                name) || TreeViewer.GROUP_CHANGED_PROPERTY.equals(name) ||
                TreeViewer.SELECTION_PROPERTY.equals(name) ||
                Browser.SELECTED_TREE_NODE_DISPLAY_PROPERTY.equals(name)) {
            Browser browser = model.getSelectedBrowser();
            TreeImageDisplay v = null;
            if (browser != null) v = browser.getLastSelectedDisplay();
            onDisplayChange(v);
        } else if (TreeViewer.IMPORT_PROPERTY.equals(name)) {
            onDataImport();
        }
    }

    /** 
     * Reacts to state changes in the {@link Browser}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        Object source = e.getSource();
        if (source instanceof Browser) onBrowserStateChange((Browser) source);
    }

}
