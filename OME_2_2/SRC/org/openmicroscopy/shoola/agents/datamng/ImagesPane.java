/*
 * org.openmicroscopy.shoola.agents.datamng.ExplorerImagePane
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ImagesPane
	extends JPanel
{

	/** This UI component's controller and model. */
	private ImagesPaneManager		manager;

	ImagesPaneBar					bar;
	
	/** The table used to display the list of images. */
	JTable       					table;
	
	JScrollPane						scrollPane;
	
	/** 
	 * Creates a new instance.
	 *
	 *@param    agentCtrl   The agent's control component.
	 */
	ImagesPane(DataManagerCtrl agentCtrl, Registry registry)
	{
		initComponents();
		bar = new ImagesPaneBar(registry);
		manager = new ImagesPaneManager(this, agentCtrl);
		buildGUI();
	}
	
	/** Initializes the table and the scrollPane. */
	void initComponents()
	{
		table = new JTable();
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(table);
	}
	
	/** Return the manager of the component. */
	ImagesPaneManager getManager() { return manager; }
	
	/** Builds and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(bar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}
	
}
