/*
 * util.ui.DatasetTable 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package util.ui;


//Java imports
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

//Third-party libraries

//Application-internal dependencies

import omeroj.service.OmeroJService;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DatasetTree
{
	/** Link to the data service which is part of the ServiceFactory. */
	OmeroJService	service;
	
	DatasetModel model;
	DatasetView view;
	
	/** 
	 * Create the DatasetTree which accesses the datasets in the DataService
	 * @param dataService see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public DatasetTree(OmeroJService service) 
		throws DSOutOfServiceException, DSAccessException
	{
		this.service = service;
		model = new DatasetModel(service);
		view = new DatasetView(model.getTree());
	}

	/** 
	 * Create the tree in a JPanel and return the panel.
	 * @return see above.
	 */
	public JPanel createTree()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(view, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * Get the current selection in the tree. This should be a Project, 
	 * or dataset. 
	 * @return see above.
	 */
	public DefaultMutableTreeNode getSelection()
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        view.getLastSelectedPathComponent();
		return node;
	}
}


