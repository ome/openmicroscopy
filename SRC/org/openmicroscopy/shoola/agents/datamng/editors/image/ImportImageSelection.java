/*
 * org.openmicroscopy.shoola.agents.datamng.editors.image.ImportImageSelection
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

package org.openmicroscopy.shoola.agents.datamng.editors.image;



//Java imports
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

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
class ImportImageSelection
	extends JPanel
{
	
	private ImportImageChooserMng 		manager;
	
	private ImportImageBar				bar;
	private ImportImageSelectionPane 	selectionPane;
	
	public ImportImageSelection(ImportImageChooserMng manager, List datasets)
	{
		this.manager = manager;
		bar = new ImportImageBar();
		selectionPane = new ImportImageSelectionPane(datasets, manager);
		buildGUI();
	}
	
	ImportImageBar getBar() { return bar; }
	
	ImportImageSelectionPane getSelectionPane() { return selectionPane; }
	
	/** Re-build the component. */
	void rebuildComponent()
	{
		remove(selectionPane);
		selectionPane.rebuildComponent();
		add(selectionPane, BorderLayout.CENTER);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(selectionPane, BorderLayout.CENTER);
		add(bar, BorderLayout.SOUTH);		
	}
	
}
