/*
 * org.openmicroscopy.shoola.agents.datamng.editors.image.ImportImage
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
import java.io.File;
import java.util.List;
import javax.swing.JFileChooser;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.util.filter.file.DVFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class ImportImageChooser
	extends JFileChooser
{

	private DataManagerCtrl				control;
	private ImportImageChooserMng 		manager;
	
	private ImportImageSelection		iiSelection;
	
	public ImportImageChooser(DataManagerCtrl control, List datasets)
	{
		this.control = control;
		manager = new ImportImageChooserMng(this, control);
		iiSelection = new ImportImageSelection(manager, datasets);
		manager.setSelectionView(iiSelection);
		manager.attachListener();
		createChooser();
	}

	/** Build the file chooser. */
	private void createChooser()
	{ 
		setFileSelectionMode(FILES_ONLY); 
		setMultiSelectionEnabled(true);
		DVFilter dvFilter = new DVFilter();
		setFileFilter(dvFilter);
		addChoosableFileFilter(dvFilter); 
		TIFFFilter tiffFilter = new TIFFFilter();
		setFileFilter(tiffFilter);
		addChoosableFileFilter(tiffFilter); 
		setAcceptAllFileFilterUsed(false);
		setApproveButtonToolTipText(
			UIUtilities.formatToolTipText("Select Images to import."));
		
		IconManager im = IconManager.getInstance(control.getRegistry());
		TitlePanel tp = new TitlePanel("Import Image", 
								"Import new images in an existing dataset.", 
								im.getIcon(IconManager.IMPORT_IMAGE_BIG));
		add(tp, BorderLayout.NORTH);
		add(iiSelection, BorderLayout.SOUTH);
		//Parent and approve button txt
		showDialog(control.getReferenceFrame(), "Select Images");		
	}
	
	/** Override the approveSelection method. */
	public void approveSelection()
	{
		File[] files = getSelectedFiles();
		if (files != null) {
			manager.setFilesToAdd(files);
			setSelectedFiles(null);
			setSelectedFile(null);
			return;	
		}      
		// No file selected, or file can be written - let OK action continue
		super.approveSelection();
	}
		
	/** Override the cancelSelection method. */
	public void cancelSelection() { manager.resetSelection(); }
	
	/** Close the widget. */
	void closeWindow() { super.cancelSelection(); }
	
}
