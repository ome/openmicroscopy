/*
 * org.openmicroscopy.shoola.agents.datamng.ToolBar
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
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
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
class ToolBar
	extends JToolBar
{

	/** Dimension of the separator. */
	private static final Dimension	SEPARATOR = new Dimension(15, 0);
	
	private JButton					projectButton, datasetButton, imageButton;
	
	ToolBar(DataManagerCtrl control, Registry registry)
	{
		initButtons(registry);
		new ToolBarManager(control, this);
		buildToolBar();
	}
	
	/** Return the createProject button. */
	JButton getProjectButton() { return projectButton; }

	/** Return the createDataset button. */
	JButton getDatasetButton() { return datasetButton; }

	/** Return the createImage button. */
	JButton getImageButton() { return imageButton; }

	/** Initialize the control buttons. */
	private void initButtons(Registry registry)
	{
		IconManager im = IconManager.getInstance(registry);
		projectButton =  new JButton(im.getIcon(IconManager.PROJECT));
		projectButton.setToolTipText(
			UIUtilities.formatToolTipText("Create a new project."));
		datasetButton =  new JButton(im.getIcon(IconManager.DATASET));
		datasetButton.setToolTipText(
			UIUtilities.formatToolTipText("Create a new Dataset."));
		imageButton =  new JButton(im.getIcon(IconManager.IMAGE));
		imageButton.setToolTipText(
			UIUtilities.formatToolTipText("Import a new image."));
	}
	
	/** Build and lay out the tool bar. */
	private void buildToolBar()
	{
		setFloatable(false);
		add(projectButton);
		addSeparator(SEPARATOR);
		add(datasetButton);
		addSeparator(SEPARATOR);
		add(imageButton);
	}

}
