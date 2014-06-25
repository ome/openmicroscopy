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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;


/**
 * A View for displaying a {@link AdvancedSearchResultCollection}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
class SearchResultView extends JPanel {

    /** The DataObjects to be shown */
    private List<DataObject> objs = new ArrayList<DataObject>();

    /** Reference to the DataBrowserModel */
    private AdvancedResultSearchModel browserModel = null;

    /** Reference to the table displaying the nodes. */
    private SearchResultTable objsTable;

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

        objsTable = new SearchResultTable(objs, browserModel);

    }

    /** Builds and lays out the UI. */
    private void buildGUI() {

        setBackground(UIUtilities.BACKGROUND_COLOR);

        setLayout(new BorderLayout());

        add(new JScrollPane(objsTable), BorderLayout.CENTER);

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
     * Reloads the table
     */
    void refreshTable() {
        objsTable.refreshTable();

    }

}
