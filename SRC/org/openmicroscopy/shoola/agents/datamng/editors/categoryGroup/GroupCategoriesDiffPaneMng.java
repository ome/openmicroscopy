/*
 * org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.GroupCategoriesDiffPaneManager
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

package org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.CategorySummary;

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
class GroupCategoriesDiffPaneMng
	implements ActionListener
{
	
	/** ID to handle event fired by buttons. */
	private static final int               ALL = 100;
	private static final int               CANCEL = 101;
	private static final int               SAVE = 102;
	
	/** List of categories to be added. */
	private List                           categoriesToAdd;
	
    private List                           categoriesDiff;
    
	/** Reference to the {@link GroupCategoriesDiffPane view}. */
	private GroupCategoriesDiffPane        view;
	
	private GroupEditorManager             control;

	GroupCategoriesDiffPaneMng(GroupCategoriesDiffPane view, 
							GroupEditorManager control, List categoriesDiff)
	{
			this.view = view;
			this.control = control;	
			this.categoriesDiff = categoriesDiff;	
			categoriesToAdd = new ArrayList();
			attachListeners();					
	}
	
	List getCategoriesDiff() { return categoriesDiff; }
	
	/** Attach the listeners. */
	private void attachListeners()
	{
        attachButtonListener(view.selectButton, ALL);
        attachButtonListener(view.cancelButton, CANCEL);
		attachButtonListener(view.saveButton, SAVE);
	}

    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
	/** Handle events fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) { 
				case SAVE:
					saveSelection(); break;
				case ALL:
					selectAll(); break;
				case CANCEL:
					cancelSelection();
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	void setSelected(boolean value, CategorySummary cd)
	{
		if (value) categoriesDiff.add(cd);
		else categoriesDiff.remove(cd);
		buttonsEnabled(categoriesDiff.size() != 0);
	}
	
	/** Set the buttons enabled. */
	void buttonsEnabled(boolean b)
	{
		view.selectButton.setEnabled(b);
		view.cancelButton.setEnabled(b);
		view.saveButton.setEnabled(b);
	}
	
	/** 
	 * Add (resp. remove) the dataset summary to (resp. from) the list of
	 * dataset summary objects to add to the new project.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param ds		dataset summary to add or remove
	 */
	void addCategory(boolean value, CategorySummary cd) 
	{
		if (value)	{
			if (!categoriesToAdd.contains(cd))	categoriesToAdd.add(cd);
		} else 	categoriesToAdd.remove(cd);
	}
	
	/** Add the selection to the datasets. */
	private void saveSelection()
	{
		if (categoriesToAdd.size() != 0) {
			Iterator i = categoriesToAdd.iterator();
			while (i.hasNext())
                categoriesDiff.remove(i.next());
			buttonsEnabled(categoriesDiff.size() != 0);
			control.addCategoriesSelection(categoriesToAdd);
			categoriesToAdd.removeAll(categoriesToAdd);
		}
		view.setVisible(false);
	}
	
	/** Select All datasets.*/
	private void selectAll()
	{
		view.selectButton.setEnabled(false);
		view.setSelection(new Boolean(true));
	}
	
	/** Cancel the selection. */
	private void cancelSelection()
	{
		view.selectButton.setEnabled(true);
		view.setSelection(new Boolean(false));
	}
	
}
