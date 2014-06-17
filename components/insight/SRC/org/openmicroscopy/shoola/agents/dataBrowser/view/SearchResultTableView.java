/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.ImageTableView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Embed the {@link ImageTable}.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since OME3.0
 */
class SearchResultTableView extends JPanel {

    /**
     * Bound property indicating that the nodes in the table have been selected.
     */
    static final String TABLE_NODES_SELECTION_PROPERTY = "tableNodesSelection";

    /** Reference to the table displaying the nodes. */
    private SearchResultTable table;

    /** Reference to the model. */
    private DataBrowserModel model;

    /** Builds and lays out the UI. */
    private void buildGUI() {
        setLayout(new BorderLayout());
        setBackground(UIUtilities.BACKGROUND_COLOR);
        JScrollPane pane = new JScrollPane(table);
        pane.setBackground(UIUtilities.BACKGROUND_COLOR);
        add(pane, BorderLayout.CENTER);
    }

    /**
     * Creates a new instance.
     * 
     * @param model
     *            Reference to the Model. Mustn't be <code>null</code>.
     * @param root
     *            The root of the tree.
     */
    SearchResultTableView(DataBrowserModel model) {
        if (model == null)
            throw new IllegalArgumentException("No model.");
        this.model = model;
        table = new SearchResultTable();
        buildGUI();
    }

    /**
     * Returns the {@link ViewerSorter}.
     * 
     * @return See above.
     */
    ViewerSorter getSorter() {
        return model.getSorter();
    }

}
