/*
 * org.openmicroscopy.shoola.agents.datamng.editors.image.ImportImageEditor
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
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

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
public class ImportImageSelector
	extends JDialog
{

	static String 					MESSAGE = "A file with the same name and " +
											"extension already exists in " +
											"this directory. Do you " +
											"still want to save the image?";
	
	static String					TITLE = "Save Image";
	
	static String					NOTE = "[Note] The import can take time." +
											"It's coffee time ;-)";
											
	private ImportImageChooser		chooser;
	
	private ImportImageSelectorMng	manager;
	
	private ImportImageSelection	selection;
	
	/** 
	 * Create a new instance. 
	 * 
	 * @param control		reference to the controller.
	 * @param datasets		list of existing datasets.
	 */
	public ImportImageSelector(DataManagerCtrl control, List datasets)
	{
		super(control.getReferenceFrame(), "image import", true);
		IconManager im = IconManager.getInstance(control.getRegistry());
		manager = new ImportImageSelectorMng(this, control);
		initComponents(datasets);
		buildGUI(im);
		pack();
		setVisible(true);
	}
	
	/** Initialize the components. */
	private void initComponents(List datasets)
	{
		chooser = new ImportImageChooser(manager);
		selection = new ImportImageSelection(manager, datasets);
		manager.setSelectionView(selection);
		manager.attachListener();
	}
	
	/** Build and layout the GUI. */
	private void buildGUI(IconManager im) 
	{
		getContentPane().setLayout(new BorderLayout(0, 0));
		TitlePanel tp = new TitlePanel("Import Image", 
								"Import new images in an existing dataset.", 
								NOTE, im.getIcon(IconManager.IMPORT_IMAGE_BIG));			
		getContentPane().add(tp, BorderLayout.NORTH);
		getContentPane().add(chooser, BorderLayout.CENTER);
		getContentPane().add(selection, BorderLayout.SOUTH);	
	}

}
