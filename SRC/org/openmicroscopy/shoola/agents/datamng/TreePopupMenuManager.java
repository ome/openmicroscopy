/*
 * org.openmicroscopy.shoola.agents.datamng.PopupMenuManager
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

/** 
 * The UI manager of the {@link PopupMenu}.
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
class TreePopupMenuManager
	implements ActionListener 
{

	/** This UI component's view. */
	private TreePopupMenu           view; 
	
	/** The agent's control component. */
	private DataManagerCtrl			agentCtrl;
	
	/** 
	 * The object (project, dataset or image) the menu is currently
	 * operating on.
	 */
	private DataObject				target;
    
    
	/** 
	 * Creates a new instance which will register itself as appropiate with the
	 * view's widgets.
	 *
	 * @param view		This UI component's view.
	 * @param control	The agent's control component.
	 */
	TreePopupMenuManager(TreePopupMenu view, DataManagerCtrl control)
	{
		this.view = view;
		agentCtrl = control;
		initListeners();
	}
    
	/** 
	 * Sets the object (project, dataset or image) the menu is going to
	 * operate on. 
	 * The view button will be enabled only if the passed object is
	 * an image summary.
	 * The browse button will be enabled only if the passed object is
	 * a dataset summary.
	 *
	 * @param t  The object for which the menu has to be brought up.
	 */
	void setTarget(DataObject t) 
	{
		target = t;
		if (target != null) {
			view.browse.setEnabled((target instanceof DatasetSummary));
			view.view.setEnabled((target instanceof ImageSummary));
			view.annotate.setEnabled(!(target instanceof ProjectSummary));
		}
	}
    
	/** 
	 * Reacts to activation of the menu buttons.
	 *
	 * @param e   Represents an activation of a menu button, as a click.
	 */
	public void actionPerformed(ActionEvent e) 
	{
		if (target != null) {
			Object src = e.getSource();
			if (src == view.properties)	
				agentCtrl.showProperties(target);
			else if (src == view.view && target instanceof ImageSummary)       
				agentCtrl.viewImage(((ImageSummary) target));
			else if (src == view.browse && target instanceof DatasetSummary)
				agentCtrl.viewDataset(((DatasetSummary) target));
			else if (src == view.annotate && target instanceof DatasetSummary)
				agentCtrl.annotateDataset(((DatasetSummary) target));
			else if (src == view.annotate && target instanceof ImageSummary)
				agentCtrl.annotateImage(((ImageSummary) target));
		}
		view.setVisible(false);
	}
	
	/** Registers listeners with the view's widgets. */
	private void initListeners()
	{
		view.properties.addActionListener(this);
		view.view.addActionListener(this);
		view.browse.addActionListener(this);
		view.refresh.addActionListener(this);
		view.annotate.addActionListener(this);
	}

}
