/*
 * org.openmicroscopy.shoola.agents.datamng.editors.image.ImportImageEditorManager
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
class ImportImageSelectorMng
	implements ActionListener
{
	
	/** Action ID to handle events. */
	static final int				IMPORT = 0;
	static final int				CANCEL = 1;
	static final int				REMOVE = 2;
	
	private ImportImageSelector		view;
	
	private DataManagerCtrl			control;
	
	/** 
	 * List of datasets to import and list of datasets to remove from the 
	 * import list.
	 */
	private List 					filesToImport, filesToRemove;
	
	private ImportImageSelection 	selection;
	
	ImportImageSelectorMng(ImportImageSelector view, 
								DataManagerCtrl control)
	{
		this.view = view;
		this.control = control;
		filesToImport = new ArrayList();
		filesToRemove = new ArrayList();
	}

	/** Attach listeners. */
	void attachListener()
	{
		JButton saveButton = selection.getBar().getSaveButton(), 
				cancelButton = selection.getBar().getCancelButton(),
				removeButton = selection.getBar().getRemoveButton();
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+IMPORT);
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		removeButton.addActionListener(this);
		removeButton.setActionCommand(""+REMOVE);
	}
	
	List getFilesToImport() { return filesToImport; }
	
	List getFilesToRemove() { return filesToRemove; }
	
	void setSelectionView(ImportImageSelection selection)
	{
		this.selection = selection;
	}
	
	/** Update the list of files to add. */
	void setFilesToAdd(File[] files)
	{
		File file;
		for (int i = 0; i < files.length; i++) {
			file = files[i];
			if (!filesToImport.contains(file)) filesToImport.add(file);
		}
		selection.rebuildComponent();
		validate();
	}
	
	/** 
	 * Remove or not the specified file from the queue of files to import.
	 * 
	 * @param b		<code>true</code> if the file has to be removed, 
	 * 				<code>false</code> otherwise.
	 * @param file	file to remove.
	 */
	void setFilesToRemove(boolean b, File file)
	{
		if (b) filesToRemove.add(file);
		else {
			if (filesToRemove.contains(file)) filesToRemove.remove(file);
		}
	}
	
	/** Reset the selection. */
	void resetSelection() 
	{
		if (filesToImport.size() != 0) {
			filesToImport.removeAll(filesToImport);
			selection.rebuildComponent();
			validate();
		}
	}

	/** Handle event fired by buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) { 
				case IMPORT:
					importImages(); break;
				case CANCEL:
					cancel(); break;
				case REMOVE:
					removeSelectedFiles();
			} 
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	/** Import images in the selected dataset. */
	private void importImages()
	{
		JComboBox cb = selection.getSelectionPane().getExistingDatasets();
		int datasetID = ((DatasetSummary) cb.getSelectedItem()).getID();
		if (filesToImport.size() == 0) {
			UserNotifier un = control.getRegistry().getUserNotifier();
			un.notifyInfo("Import images", "Please select at least one image.");
		} else control.importImages(filesToImport, datasetID); 
	}
	
	/** Close the widget, doesn't save changes. */
	private void cancel()
	{ 
		view.setVisible(false);
		view.dispose();
	}
	
	/** 
	 * Remove all the pre-selected files from the queue.
	 * 
	 * @param index	file index. 
	 */
	private void removeSelectedFiles()
	{
		Iterator i = filesToRemove.iterator();
		while (i.hasNext()) 
			filesToImport.remove((File) i.next());
			
		if (filesToRemove.size() != 0) {
			filesToRemove.removeAll(filesToRemove);
			selection.rebuildComponent();
			validate();
		}
	}
	
	private void validate()
	{
		view.validate();
		view.pack();
	}
	
}
