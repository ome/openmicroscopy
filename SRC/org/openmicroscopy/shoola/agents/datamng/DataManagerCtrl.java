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
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DataObject;
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
	implements ActionListener, InternalFrameListener
{
	
	/**ID used to handle events. */
	static final int			DM_VISIBLE = 0;
	static final int			PROJECT_ITEM = 1;
	static final int			DATASET_ITEM = 2;
	static final int			IMAGE_ITEM = 3;
	
	private DataManager			abstraction;
	
	DataManagerCtrl(DataManager	abstraction)
	{
		this.abstraction = abstraction;
	}
	
	/** Bring up a suitable dialog. */
	public void showDialog(JDialog dialog)
	{
		abstraction.getPresentation().showPS(dialog);
	}
	
	/** 
	 * Attach an InternalFrameListener to the 
	 * {@link ViewerUIF presentaion}.
	 */
	void attachListener() 
	{
		abstraction.getPresentation().addInternalFrameListener(this);
	}
	
	/** Return the abstraction. */
	DataManager getAbstraction() {return abstraction; }
	
	/** Attach listener to a menuItem or a button. */
	void attachItemListener(AbstractButton item, int id)
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
	void showProperties(DataObject target)
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
	
	/** Forward the call to the {@link DataManager abstraction}. */
	void viewImage(ImageSummary is)
	{
		int[] pxSets = is.getPixelsIDs();
		//TODO: select pixels if more than one!
		abstraction.viewImage(is.getID(), pxSets[0], is.getName());
	}
	
	/** Forward the call to the {@link DataManager abstraction}. */
	void viewDataset(DatasetSummary ds)
	{ 
		abstraction.viewDataset(ds.getID());
	}

	/** Forward event to the {@link DataManager abstraction}. */
	void annotateDataset(DatasetSummary ds)
	{
		abstraction.annotateDataset(ds.getID(), ds.getName());
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	void annotateImage(ImageSummary is)
	{
		abstraction.annotateImage(is.getID(), is.getName());
	}
	
	/**Handles event fired by menu. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int index = Integer.parseInt(s);
			switch (index) { 
				case DM_VISIBLE:
					showPresentation();
					break;
				case PROJECT_ITEM:
					createProject();
					break;
				case DATASET_ITEM:
					createDataset();
					break;	   	
			}
		} catch(NumberFormatException nfe) {  
			   throw nfe;  //just to be on the safe side...
		} 
	}
	
	/** Display or not the {@link DataManagerUIF presentation}. */
	private void showPresentation()
	{
		DataManagerUIF presentation = abstraction.getPresentation();
		if (presentation != null) {
			if (presentation.isClosed()) abstraction.showPresentation();	
			if (presentation.isIcon()) abstraction.deiconifyPresentation();	
			abstraction.setMenuSelection(true);
		}  		
	}	
	
	/** Forward event to the {@link DataManagerUIF presentation}. */
	void createProject()
	{
		DataManagerUIF presentation = abstraction.getPresentation();
		List datasets = abstraction.getUserDatasets();
		presentation.showCreateProject(new ProjectData(), datasets);
	}

	/** Forward event to the {@link DataManagerUIF presentation}. */
	void createDataset()
	{	
		DataManagerUIF presentation = abstraction.getPresentation();
		List projects = abstraction.getUserProjects();
		List images = abstraction.getUserImages();
		presentation.showCreateDataset(new DatasetData(), projects, images);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public List getDatasetsDiff(ProjectData data)
	{
		return abstraction.getDatasetsDiff(data);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public List getImagesDiff(DatasetData data)
	{
		return abstraction.getImagesDiff(data);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void addProject(ProjectData pd)
	{
		abstraction.createProject(pd);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void addDataset(List projects, List images, DatasetData dd)
	{
		abstraction.createDataset(projects, images, dd);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void updateProject(ProjectData pd, List toRemove, List toAdd,
							 boolean nameChange)
	{
		abstraction.updateProject(pd, toRemove, toAdd, nameChange);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void updateDataset(DatasetData dd, List toRemove, List toAdd, 
								boolean nameChange)
	{
		abstraction.updateDataset(dd, toRemove, toAdd, nameChange);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void updateImage(ImageData id, boolean nameChange)
	{
		abstraction.updateImage(id, nameChange);
	}
	

	/** Select the checkBox in menu. */
	public void internalFrameOpened(InternalFrameEvent e)
	{
		abstraction.setMenuSelection(true);
	}
	
	/** De-select the checkBox in menu. */
	public void internalFrameClosing(InternalFrameEvent e)
	{
		abstraction.setMenuSelection(false);
	}

	/** De-select the checkBox in menu. */
	public void internalFrameClosed(InternalFrameEvent e) 
	{
		abstraction.setMenuSelection(false);
	}
	
	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void internalFrameDeactivated(InternalFrameEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void internalFrameDeiconified(InternalFrameEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void internalFrameIconified(InternalFrameEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void internalFrameActivated(InternalFrameEvent e) {}
	
}
