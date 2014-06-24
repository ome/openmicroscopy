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


//Java imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


//Third-party libraries













import org.apache.batik.ext.swing.GridBagConstants;
import org.jdesktop.swingx.JXTaskPane;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

class SearchResultView
	extends JPanel
{
    
    List<DataObject> objs = new ArrayList<DataObject>();
    List<DataObject> projDs = new ArrayList<DataObject>();
    List<DataObject> image = new ArrayList<DataObject>();
    List<DataObject> scrPla = new ArrayList<DataObject>();

    /** Reference to the table displaying the nodes. */
    private SearchResultTable                       objsTable;
    
	/** Reference to the table displaying the nodes. */
	private SearchResultTable 			projDsTable;
	
	/** Reference to the table displaying the nodes. */
        private SearchResultTable                       imageTable;
        
        /** Reference to the table displaying the nodes. */
        private SearchResultTable                       scrPlaTable;
	
        private boolean singleTable = true;
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param root The root node of the tree.
	 */
	private void initComponents(ImageDisplay root)
	{
	    Set<ImageDisplay> nodes = root.getChildrenDisplay();
	    for(ImageDisplay node : nodes) {
	        DataObject obj = (DataObject) node.getHierarchyObject();
	        
	        if(singleTable) {
	            objs.add(obj);
	        }
	        else if(obj instanceof ProjectData || obj instanceof DatasetData)
	            projDs.add(obj);
	        else if(obj instanceof ImageData)
	            image.add(obj);
	        else if(obj instanceof ScreenData || obj instanceof PlateData)
	            scrPla.add(obj);
	    }
	    
            if (singleTable) {
                objsTable = new SearchResultTable(objs);
            } else {
                projDsTable = new SearchResultTable(projDs);
                imageTable = new SearchResultTable(image);
                scrPlaTable = new SearchResultTable(scrPla);
            }
	}
	
	/** Builds and lays out the UI. */
        private void buildGUI() {
            
            setBackground(UIUtilities.BACKGROUND_COLOR);
            
            if(singleTable) {
                setLayout(new BorderLayout());
                add(new JScrollPane(objsTable), BorderLayout.CENTER);
            }
            else {
                setLayout(new GridBagLayout());
                
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = 0;
                
                JXTaskPane projDsPane = EditorUtil.createTaskPane("Projects/Datasets");
                projDsPane.add(new JScrollPane(projDsTable));
                projDsPane.setCollapsed(projDs.isEmpty());
                c.gridy = 0;
                add(projDsPane, c);
        
                JXTaskPane imagePane = EditorUtil.createTaskPane("Images");
                imagePane.add(new JScrollPane(imageTable));
                imagePane.setCollapsed(image.isEmpty());
                c.gridy = 1;
                add(imagePane, c);
        
                JXTaskPane scrPlaPane = EditorUtil.createTaskPane("Screens/Plates");
                scrPlaPane.add(new JScrollPane(scrPlaTable));
                scrPlaPane.setCollapsed(scrPla.isEmpty());
                c.gridy = 2;
                add(scrPlaPane, c);
            }
        }
	
        /** 
         * Creates a new instance. 
         * 
         * @param model Reference to the Model. Mustn't be <code>null</code>.
         * @param root  The root of the tree.
         */
        SearchResultView(ImageDisplay root)
        {
                   this(root, true);
        }
        
	/** 
	 * Creates a new instance. 
	 * 
	 * @param model	Reference to the Model. Mustn't be <code>null</code>.
	 * @param root 	The root of the tree.
	 */
	SearchResultView(ImageDisplay root, boolean singleTable)
	{
	        this.singleTable = singleTable;
		initComponents(root);
		buildGUI();
	}
	
        void refreshTable() {
            if (singleTable) {
                objsTable.refreshTable();
            } else {
                projDsTable.refreshTable();
                imageTable.refreshTable();
                scrPlaTable.refreshTable();
            }
        }
	
}
