/*
 * org.openmicroscopy.shoola.env.ui.ChangesDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Indicates the progress of the save before closing or switching.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ChangesDialog 
	extends JDialog
{

	/** Bound property indicating that all the tasks are done. */
	static final String	 DONE_PROPERTY = "Done";
	
	/** The title of the dialog. */
	private static final String TITLE = "Saving data";
	
	/** Displayed the progress. */
	private JLabel 			status;
	
	/** Displayed the progress. */
	private JProgressBar 	progressBar;
	
	/** The number of tasks. */
	private int 		 	count;
	
	/** The nodes to handle. */
	private List<Object> 	nodes;
	
	/** The security context.*/
	private SecurityContext ctx;
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setTitle(TITLE);
		//setModal(true);
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		status = new JLabel("Saving...");
		progressBar = new JProgressBar(0, nodes.size());
		if (nodes.size() <= 1)
			progressBar.setIndeterminate(true);
		progressBar.setValue(0);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(progressBar);
		p.add(UIUtilities.buildComponentPanel(status));
		getContentPane().add(p, BorderLayout.CENTER);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param totalTask The number of tasks to perform.
	 * @param ctx The security context.
	 */
	ChangesDialog(JFrame owner, List<Object> nodes, SecurityContext ctx)
	{
		super(owner);
		count = 0;
		this.ctx = ctx;
		this.nodes = nodes;
		setProperties();
		initComponents();
		buildGUI();
		setSize(400, 300);
	}
	
	/**
	 * Sets the progress status.
	 * 
	 * @param node The node to set.
	 * @param count
	 */
	void setStatus(Object node)
	{
		nodes.remove(node);
		count++;
		String text = "";
		if (node instanceof AgentSaveInfo) {
			text = "Saved "+((AgentSaveInfo) node).getName();
		}
		
		if (nodes.size() == 0) {
			firePropertyChange(DONE_PROPERTY, null, ctx);
		} else {
			status.setText(text);
			progressBar.setValue(count);
		}
	}
	
}
