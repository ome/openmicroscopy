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
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

/** 
 * The UI manager of the {@link TreePopupMenu}.
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
	 * The object (project, dataset, caegoryGroup, category or image) 
     * the menu is currently operating on.
	 */
	private DataObject				target;
    
    private int                     index;
    
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
        //DEFAULT 
        index = DataManagerCtrl.FOR_HIERARCHY;
		agentCtrl = control;
		initListeners();
	}
    
	/** 
	 * Sets the object (project, dataset, categoryGroup, category or image)
     * the menu is going to operate on. 
	 *
	 * @param t  The object for which the menu has to be brought up.
	 */
	void setTarget(DataObject t) 
	{
		target = t;
		if (target != null) {
			view.properties.setEnabled(true);
            if (target instanceof ImageSummary)
                view.view.setText(TreePopupMenu.VIEW);
            else view.view.setText(TreePopupMenu.BROWSE);
			view.view.setEnabled(true);
			view.annotate.setEnabled((target instanceof DatasetSummary) ||
                    (target instanceof ImageSummary));
			view.importImg.setEnabled((target instanceof DatasetSummary));
            view.refresh.setEnabled(!(target instanceof ImageSummary));
		} else {//root node.
            view.properties.setEnabled(false);
            view.view.setText(TreePopupMenu.BROWSE);
            view.view.setEnabled(false);
            view.annotate.setEnabled(false);
            view.importImg.setEnabled(false);
            view.refresh.setEnabled(true);
        }
	}
    
    /** 
     * Sets the pane index, One of the constants defined by 
     * {@link DataManagerCtrl} i.e. {@link DataManagerCtrl#FOR_HIERARCHY}
     * or {@link DataManagerCtrl#FOR_IMAGES}
     */
    void setIndex(int i)
    {
        if (i == DataManagerCtrl.FOR_HIERARCHY || 
                i == DataManagerCtrl.FOR_IMAGES || 
                i == DataManagerCtrl.FOR_CLASSIFICATION)
            index = i;
        else index = DataManagerCtrl.FOR_HIERARCHY; 
    }
    
	/** 
	 * Reacts to activation of the menu buttons.
	 *
	 * @param e   Represents an activation of a menu button, as a click.
	 */
	public void actionPerformed(ActionEvent e) 
	{
		Object src = e.getSource();
		if (target != null) {	
			if (src == view.properties)	agentCtrl.showProperties(target, index);
			else if (src == view.view) view(target);
			else if (src == view.annotate)   agentCtrl.annotate(target);
			else if (src == view.importImg && target instanceof DatasetSummary)
				agentCtrl.showImagesImporter(((DatasetSummary) target));
            else if (src == view.refresh) agentCtrl.refresh(target);
		} else { //root node
            if (src == view.refresh) agentCtrl.refresh(index);
            //For harry
            else if (src == view.view && index == DataManagerCtrl.FOR_HIERARCHY) 
                agentCtrl.browseRoot();  
        }
		view.setVisible(false);
	}
	
    /** View the specified dataObject. */
    private void view(Object uo)
    {
        if (uo instanceof DatasetSummary)
            agentCtrl.browseDataset(((DatasetSummary) uo));
        else if (uo instanceof ProjectSummary)
            agentCtrl.browseProject(((ProjectSummary) uo));
        else if (uo instanceof CategoryGroupData)
            agentCtrl.browseCategoryGroup(((CategoryGroupData) uo));
        else if (uo instanceof CategoryData)
            agentCtrl.browseCategory(((CategoryData) uo));
        else if (uo instanceof ImageSummary)
            agentCtrl.viewImage(((ImageSummary) uo));
    }
    
	/** Registers listeners with the view's widgets. */
	private void initListeners()
	{
		view.properties.addActionListener(this);
		view.view.addActionListener(this);
		view.refresh.addActionListener(this);
		view.annotate.addActionListener(this);
		view.importImg.addActionListener(this);
	}

}
