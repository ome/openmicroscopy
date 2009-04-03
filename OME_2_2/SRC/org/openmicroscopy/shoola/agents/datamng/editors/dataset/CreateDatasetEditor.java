/*
 * org.openmicroscopy.shoola.agents.datamng.editors.CreateDatasetEditor
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

package org.openmicroscopy.shoola.agents.datamng.editors.dataset;

//Java imports
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
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
public class CreateDatasetEditor
	extends JDialog
{	

	private Registry 					registry;
	private CreateDatasetPane 			creationPane;
	private CreateDatasetProjectsPane	projectsPane;
	private CreateDatasetImagesPane		imagesPane;
	private CreateDatasetEditorBar		bar;
	private CreateDatasetEditorManager	manager;
	
	public CreateDatasetEditor(Registry registry, DataManagerCtrl control,
								DatasetData model, List projects, List images)
	{
		super(control.getReferenceFrame(), true);
		this.registry = registry;
		manager = new CreateDatasetEditorManager(this, control, model, projects,
												images);
		creationPane = new CreateDatasetPane(manager);
		projectsPane = new CreateDatasetProjectsPane(manager);
		imagesPane = new CreateDatasetImagesPane(manager);
		bar = new CreateDatasetEditorBar();
		buildGUI();
		manager.initListeners();
		setSize(DataManager.EDITOR_WIDTH+100, DataManager.EDITOR_HEIGHT);
	}
	
	Registry getRegistry() { return registry; }
	
	/** Return the {@link CreateDatasetEditorManager manager} of the widget. */
	CreateDatasetEditorManager getManager() { return manager; }
	
	/** Returns the TextArea displayed in {@link CreateDatasetPane}. */
	JTextArea getDescriptionArea() { return creationPane.getDescriptionArea(); }

	/** Returns the textArea displayed in {@link CreateDatasetPane}. */
	JTextArea getNameField() { return creationPane.getNameField(); }
	
	/** Returns the save button displayed in {@link CreateDatasetEditorBar}. */
	JButton getSaveButton() { return bar.getSaveButton(); }

	/** 
	 * Returns the cancel button displayed in 
	 * {@link CreateDatasetEditorBar}.
	 */
	JButton getCancelButton() { return bar.getCancelButton(); }
	/** 
	 * Returns the select button displayed in {@link CreateDatasetProjectsPane}.
	 */
	JButton getSelectButton() { return projectsPane.getSelectButton(); }

	/** 
	 * Returns the reset button displayed in {@link CreateDatasetProjectsPane}.
	 */
	JButton getResetProjectButton() { return projectsPane.getResetButton(); }
	
	/** 
	 * Returns the select button displayed in {@link CreateDatasetImagesPane}.
	 */
	JButton getSelectImageButton() { return imagesPane.getSelectButton(); }

	/** 
	 * Returns the cancel button displayed in {@link CreateDatasetImagesPane}.
	 */
	JButton getResetImageButton() { return imagesPane.getResetButton(); }
	
	/** Forward event to the pane {@link CreateDatasetProjectsPane}. */
	void selectAllProjects()
	{
		projectsPane.setSelection(new Boolean(true));
	}

	/** Forward event to the pane {@link CreateDatasetProjectsPane}. */
	void resetSelectionProject()
	{
		projectsPane.setSelection(new Boolean(false));
	}
	
	/** Forward event to the pane {@link CreateDatasetImagesPane}. */
	void selectAllImages()
	{
		imagesPane.setSelection(new Boolean(true));
	}

	/** Forward event to the pane {@link CreateDatasetImagesPane}. */
	void resetSelectionImage()
	{
		imagesPane.setSelection(new Boolean(false));
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		IconManager im = IconManager.getInstance(registry);
		//TODO: specify lookup name.
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		tabs.addTab("New Dataset", im.getIcon(IconManager.DATASET), 
					creationPane);
		tabs.addTab("Add to Projects", im.getIcon(IconManager.PROJECT), 
					projectsPane);
		tabs.addTab("Add Images", im.getIcon(IconManager.IMAGE), 
					imagesPane);			
		tabs.setSelectedComponent(creationPane);
		tabs.setFont(font);
		tabs.setForeground(DataManager.STEELBLUE);
		TitlePanel tp = new TitlePanel("Dataset", "Create a new dataset.", 
							im.getIcon(IconManager.CREATE_DATASET_BIG));
		//set layout and add components
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tp, BorderLayout.NORTH);
		getContentPane().add(tabs, BorderLayout.CENTER);
		getContentPane().add(bar, BorderLayout.SOUTH);
	}

}
