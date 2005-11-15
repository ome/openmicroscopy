/*
 * org.openmicroscopy.shoola.agents.datamng.editors.dataset.CreateDatasetEditor
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
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.agents.datamng.editors.controls.CreateBar;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

import pojos.ProjectData;


/** 
 * Create Dataset widget.
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
	extends JPanel
{	

    private DataManagerCtrl                 agentCtrl;
	private CreateDatasetPane              creationPane;
	private CreateDatasetProjectsPane      projectsPane;
	private CreateDatasetImagesPane        imagesPane;
	private CreateBar                      bar;
	private CreateDatasetEditorManager     manager;
	
	public CreateDatasetEditor(DataManagerCtrl agentCtrl, DatasetData model,
                            Set projects)
	{
		this.agentCtrl = agentCtrl;
		manager = new CreateDatasetEditorManager(this, agentCtrl, model, 
                                                projects);
		creationPane = new CreateDatasetPane();
		projectsPane = new CreateDatasetProjectsPane(manager);
		imagesPane = new CreateDatasetImagesPane(manager);
		bar = new CreateBar();
		buildGUI();
		manager.initListeners();
		setSize(DataManagerUIF.EDITOR_WIDTH+100, DataManagerUIF.EDITOR_HEIGHT);
	}
    
    
    public CreateDatasetEditor(DataManagerCtrl agentCtrl, DatasetData model,
            Set projects, ProjectData project)
    {
        this.agentCtrl = agentCtrl;
        manager = new CreateDatasetEditorManager(this, agentCtrl, model, 
                                        projects);
        manager.addProject(true, project);
        creationPane = new CreateDatasetPane();
        projectsPane = new CreateDatasetProjectsPane(manager);
        imagesPane = new CreateDatasetImagesPane(manager);
        bar = new CreateBar();
        buildGUI();
        manager.initListeners();
        setSize(DataManagerUIF.EDITOR_WIDTH+100, DataManagerUIF.EDITOR_HEIGHT);
    }
	
	Registry getRegistry() { return agentCtrl.getRegistry(); }
	
	/** Return the {@link CreateDatasetEditorManager manager} of the widget. */
	CreateDatasetEditorManager getManager() { return manager; }
	
	/** Returns the TextArea displayed by {@link CreateDatasetPane}. */
	JTextArea getDescriptionArea() { return creationPane.descriptionArea; }

	/** Returns the textArea displayed by {@link CreateDatasetPane}. */
	JTextArea getNameArea() { return creationPane.nameArea; }
	
	/** Returns the save button displayed by {@link CreateDatasetEditorBar}. */
	JButton getSaveButton() { return bar.getSave(); }

	/** 
	 * Returns the select button displayed by {@link CreateDatasetProjectsPane}.
	 */
	JButton getSelectButton() { return projectsPane.selectButton; }

	/** 
	 * Returns the reset button displayed by {@link CreateDatasetProjectsPane}.
	 */
	JButton getResetProjectButton() { return projectsPane.resetButton; }
	
	/** 
	 * Returns the select button displayed by {@link CreateDatasetImagesPane}.
	 */
	JButton getSelectImageButton() { return imagesPane.selectButton; }

	/** 
	 * Returns the cancel button displayed by {@link CreateDatasetImagesPane}.
	 */
	JButton getResetImageButton() { return imagesPane.resetButton; }
    
    /** 
     * Returns the showImages button displayed 
     * by {@link CreateDatasetImagesPane}.
     */
    JButton getShowImagesButton() { return imagesPane.showImages; }
    
    /** 
     * Returns the filter button displayed by {@link CreateDatasetImagesPane}.
     */
    JButton getFilterButton() { return imagesPane.filter; }
	
    /** 
     * Returns the selection box displayed by {@link CreateDatasetImagesPane}.
     */
    JComboBox getImagesSelections() { return imagesPane.selections; }
    
	/** Forward event to the pane {@link CreateDatasetProjectsPane}. */
	void selectAllProjects() { projectsPane.setSelection(Boolean.TRUE); }

	/** Forward event to the pane {@link CreateDatasetProjectsPane}. */
	void resetSelectionProject() { projectsPane.setSelection(Boolean.FALSE); }
	
	/** Forward event to the pane {@link CreateDatasetImagesPane}. */
	void selectAllImages() { imagesPane.setSelection(Boolean.TRUE); }

	/** Forward event to the pane {@link CreateDatasetImagesPane}. */
	void resetSelectionImage() { imagesPane.setSelection(Boolean.FALSE); }
    
	/** Forward event to the pane {@link CreateDatasetImagesPane}. */
    void showImages(Set images) { imagesPane.showImages(images); }
    
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		//create and initialize the tabs
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
        Registry registry = getRegistry();
		IconManager im = IconManager.getInstance(registry);
		//TODO: specify lookup name.
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		tabs.addTab("New Dataset", im.getIcon(IconManager.DATASET), 
					creationPane);
		//tabs.addTab("Add to Projects", im.getIcon(IconManager.PROJECT), 
		//			projectsPane);
		//tabs.addTab("Add Images", im.getIcon(IconManager.IMAGE), 
		//			imagesPane);			
		tabs.setSelectedComponent(creationPane);
		tabs.setFont(font);
		tabs.setForeground(DataManagerUIF.STEELBLUE);
		TitlePanel tp = new TitlePanel("Dataset", "Create a new dataset.", 
							im.getIcon(IconManager.CREATE_DATASET_BIG));
		//set layout and add components
		setLayout(new BorderLayout(0, 0));
		add(tp, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		add(bar, BorderLayout.SOUTH);
	}

}
