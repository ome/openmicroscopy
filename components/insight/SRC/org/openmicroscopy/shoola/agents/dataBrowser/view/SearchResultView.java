/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.ImageTableView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.view.SearchSelectionEvent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/**
 * A View for displaying a {@link SearchResultCollection}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 * @since 5.0
 */
class SearchResultView extends JPanel {

    /** Property for showing the context menu */
    public static final String CONTEXT_MENU_PROPERTY = "showPopup";

    /** Property indicating a selection event */
    public static final String SELECTION_PROPERTY = "selected";

    /** The DataObjects to be shown */
    private List<DataObject> objs = new ArrayList<DataObject>();

    /** Reference to the DataBrowserModel */
    private AdvancedResultSearchModel browserModel = null;

    /** Reference to the table displaying the nodes. */
    private SearchResultTable objsTable;

    /** Reference to the 'content' panel, either the table showing 
        the results or the panel indicating that there are no results */
    private Component contentPanel;
    
    /**
     * Initializes the components composing the display.
     * 
     * @param root
     *            The root node of the tree.
     */
    private void initComponents(ImageDisplay root) {
        
        Collection<ImageDisplay> nodes = root.getChildrenDisplay();
        for (ImageDisplay node : nodes) {
            DataObject obj = (DataObject) node.getHierarchyObject();
            objs.add(obj);
        }
        
        if(objs.isEmpty()) {
            contentPanel = new JPanel();
            ((JPanel)contentPanel).add(new JLabel("No results found"));
        }
        else { 
            objsTable = new SearchResultTable(this, objs, browserModel);
            contentPanel = objsTable;
        }
    }

    /** Builds and lays out the UI. */
    private void buildGUI() {

        setBackground(UIUtilities.BACKGROUND_COLOR);

        setLayout(new BorderLayout());

        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    /**
     * Fires a Property to display the context menu
     */
    void firePopupEvent(Point location) {
        // TODO: Disabled for now, context menu does not work for search results
        // firePropertyChange(CONTEXT_MENU_PROPERTY, null, location);
    }

    /**
     * Fires a Property indicating a selection event
     * 
     * @param nodes
     *            The selected DataObjects
     */
    void fireSelectionEvent(List<DataObject> nodes) {
        firePropertyChange(SELECTION_PROPERTY, null, createDisplays(nodes));
        TreeViewerAgent.getRegistry().getEventBus()
                .post(new SearchSelectionEvent(nodes));
    }

    /**
     * Creates a new instance.
     * 
     * @param model
     *            Reference to the Model. Mustn't be <code>null</code>.
     * @param root
     *            The root of the tree.
     */
    SearchResultView(ImageDisplay root, AdvancedResultSearchModel browserModel) {
        this.browserModel = browserModel;
        initComponents(root);
        buildGUI();
    }

    /**
     * Reloads the table (by creating a new TableModel)
     */
    void refreshTable() {
        if(objsTable!=null)
            objsTable.refreshTable();
    }

    /**
     * Creates the {@link ImageDisplay}s for the given {@link DataObject}s
     * 
     * @param dataObjs
     * @return
     */
    private List<ImageDisplay> createDisplays(Collection<DataObject> dataObjs) {
        List<ImageDisplay> result = new ArrayList<ImageDisplay>();

        for (DataObject dataObj : dataObjs) {
            ImageDisplay d = null;

            if (dataObj instanceof ImageData) {
                d = new ImageNode("", dataObj, null);
            } else if (dataObj instanceof ProjectData
                    || dataObj instanceof DatasetData
                    || dataObj instanceof ScreenData
                    || dataObj instanceof PlateData) {
                d = new ImageSet("", dataObj);
            }

            if (d != null)
                result.add(d);
        }

        return result;
    }
}
