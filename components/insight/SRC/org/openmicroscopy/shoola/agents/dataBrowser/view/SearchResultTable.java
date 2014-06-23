/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;

import pojos.DataObject;

public class SearchResultTable extends JPanel {

    ImageDisplay root;
    SearchResultView view;
    DataBrowserModel model;
    
    public SearchResultTable(ImageDisplay root, SearchResultView view,
            DataBrowserModel model) {
        this.root = root;
        this.view = view;
        this.model = model;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    
    public void refreshTable() {
        removeAll();
        for(ImageDisplay node : model.getNodes()) {
            add(new JLabel(node.getName()));
        }
        validate();
    }
    
    public void setHighlightedNodes(List<ImageDisplay> nodes) {
        
    }
    
    public void setSelectedNodes(List<DataObject> objects) {
        
    }
    
}
