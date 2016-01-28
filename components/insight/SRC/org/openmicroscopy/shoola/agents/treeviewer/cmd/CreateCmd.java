/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.cmd;

import org.openmicroscopy.shoola.agents.events.importer.LoadImporter;
import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.event.EventBus;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 *  Displays the editor to create a new <code>DataObject</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class CreateCmd
    implements ActionCmd
{

    /** Indicates to create a <code>Project</code>. */
    public static final int PROJECT = 0;

    /** Indicates to create a <code>Dataset</code>. */
    public static final int DATASET = 1;

    /** Indicates to create a <code>Tag</code>. */
    public static final int TAG = 2;

    /** Indicates to create a <code>Screen</code>. */
    public static final int SCREEN = 3;

    /** Indicates to create a <code>Screen</code>. */
    public static final int PLATE = 4;

    /** Indicates to import an <code>Image</code>. */
    public static final int IMAGE = 5;

    /** Indicates to create a <code>Tag Set</code>. */
    public static final int TAG_SET = 6;

    /** Indicates to create a <code>Group</code>. */
    public static final int GROUP = 7;

    /** Indicates to create a <code>Experimenter</code>. */
    public static final int EXPERIMENTER = 8;

    /** Reference to the model. */
    private TreeViewer  model;

    /**
     * The <code>DataObject</code> corresponding to a constant
     * defined by this class.
     */
    private DataObject  userObject;

    /** Flag indicating if the node to create has a parent. */
    private boolean		withParent;

    /**
     * Checks that the specified type is currently supported
     * and returns the corresponding <code>DataObject</code>.
     * 
     * @param type The type to check.
     * @return See above.
     */
    private DataObject checkNodeType(int type)
    {
        switch (type) {
            case PROJECT: return new ProjectData();
            case DATASET: return new DatasetData(); 
            case SCREEN: return new ScreenData(); 
            case TAG: return new TagAnnotationData("foo");
            case TAG_SET: return new TagAnnotationData("foo", true);
            case PLATE: return new PlateData();
            case IMAGE: return new ImageData();
            case GROUP: return new GroupData();
            case EXPERIMENTER: return new ExperimenterData();
            default:
                throw new IllegalArgumentException("Type not supported");
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param type One of the constants defined by this class.
     */
    public CreateCmd(TreeViewer model, int type)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        userObject = checkNodeType(type);
        this.model = model;
        withParent = true;
    }

    /**
     * Sets to <code>true</code> if the node will have a parent,
     * <code>false</code> otherwise.
     *
     * @param withParent The value to set.
     */
    public void setWithParent(boolean withParent)
    {
    	this.withParent = withParent;
    }

    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        if (userObject == null) return; //shouldn't happen.
        if (userObject instanceof ImageData) {
            Browser browser = model.getDefaultBrowser();
            if (withParent) browser = model.getSelectedBrowser();
            else {
                Browser selectedBrowser = model.getSelectedBrowser();
                if (selectedBrowser != null) {
                    switch (selectedBrowser.getBrowserType()) {
                    case Browser.SCREENS_EXPLORER:
                    case Browser.PROJECTS_EXPLORER:
                        browser = selectedBrowser;
                    }
                }
            }
            TreeImageDisplay display = null;
            long userId = -1;
            if (withParent) {
                display = browser.getLastSelectedDisplay();
                ExperimenterData exp = browser.getNodeOwner(display);
                if (exp != null) userId = exp.getId();
            }

            LoadImporter event = null;
            int type = BrowserSelectionEvent.PROJECT_TYPE;
            switch (browser.getBrowserType()) {
            case Browser.SCREENS_EXPLORER:
                type = BrowserSelectionEvent.SCREEN_TYPE;
            }
            event = new LoadImporter(display, type);
            event.setGroup(browser.getSecurityContext(
                    browser.getLastSelectedDisplay()).getGroupID());
            event.setUser(userId);
            EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
            bus.post(event);
        } else {
            model.createDataObject(userObject, withParent);
        }
    }

}
