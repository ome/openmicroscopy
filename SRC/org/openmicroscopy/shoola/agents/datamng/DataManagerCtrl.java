/*
 * org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl
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
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

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
public class DataManagerCtrl
	implements ActionListener
{

	static final int		PROJECT_ITEM = 0;
	static final int		DATASET_ITEM = 1;
	static final int		IMAGE_ITEM = 2;
	
	private DataManager		abstraction;
	
	public DataManagerCtrl(DataManager	abstraction)
	{
		this.abstraction = abstraction;
	}
	
	/** 
	 * Returns the abstraction component of this agent.
	 *
	 * @return  See above.
	 */
	DataManager getAbstraction()
	{
		return abstraction;
	}
	
	/** Attach listener to a menu Item. */
	void menuItemListener(JMenuItem item, int id)
	{
		item.setActionCommand(""+id);
		item.addActionListener(this);
	}
	 
	/** 
	 * Brings up a suitable property sheet dialog for the specified
	 * <code>target</code>.
	 *
	 * @param   target  	A project, dataset or image. 
	 * 						If you pass anything different this method does
	 *						nothing.
	 */
	void showProperties(Object target)
	{
		DataManagerUIF presentation = abstraction.getPresentation();
		if (target == null)    return;
		if (target instanceof ProjectSummary) {
			ProjectData project = abstraction.getProject(
									((ProjectSummary) target).getID());
			presentation.showProjectPS(project);     
		} else if (target instanceof DatasetSummary) {
			DatasetData dataset = abstraction.getDataset(
									((DatasetSummary) target).getID());											
			presentation.showDatasetPS(dataset);
		} else if (target instanceof ImageSummary) {
			ImageData image = abstraction.getImage(
									((ImageSummary) target).getID());
			presentation.showImagePS(image);
		}
	}

	/**Handles event fired by menu. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		DataManagerUIF presentation = abstraction.getPresentation();
		try {
		   int     index = Integer.parseInt(s);
		   switch (index) { 
				case PROJECT_ITEM:
					presentation.showCreateProject(new ProjectData());
				   	break;
				case DATASET_ITEM:
					presentation.showCreateDataset(new DatasetData());
					break;	
				   	
		   }// end switch  
		} catch(NumberFormatException nfe) {  //impossible if IDs are set correctly 
			   throw nfe;  //just to be on the safe side...
		} 
	}
	
		
}
