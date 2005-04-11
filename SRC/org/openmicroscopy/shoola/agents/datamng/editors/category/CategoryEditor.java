/*
 * org.openmicroscopy.shoola.agents.datamng.editors.category.CategoryEditor
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
package org.openmicroscopy.shoola.agents.datamng.editors.category;

//Java imports
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

/** 
 * Category's property sheet.
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
public class CategoryEditor
	extends JPanel
{
    
	/** ID to identify the tab pane. */
	static final int				   POS_MAIN = 0, POS_IMAGE = 1;
									
	/** Reference to the manager. */
	private CategoryEditorManager      manager;
	
	/** Reference to the {@link DataManagerCtrl}. */
	private DataManagerCtrl            agentCtrl;
	
	private CategoryPane               categoryPane;
	private CategoryImagesPane         imagesPane;
	private CategoryEditorBar          bar;
	private JTabbedPane                tabs;
	
	public CategoryEditor(DataManagerCtrl agentCtrl, CategoryData model)
	{
		this.agentCtrl = agentCtrl;
		manager = new CategoryEditorManager(this, agentCtrl, model);
		categoryPane = new CategoryPane(manager);
		imagesPane = new CategoryImagesPane(manager);
		bar = new CategoryEditorBar();
		buildGUI();
		manager.initListeners();
	}
	
	Registry getRegistry() { return agentCtrl.getRegistry(); } 
	
	CategoryImagesPane getImagesPane() { return imagesPane; }
	
	/**  Returns the save button displayed {@link CategoryEditorBar}. */
	JButton getSaveButton() { return bar.saveButton; }
	
	/** Returns the save button displayed {@link CategoryEditorBar}. */
	JButton getAddButton() { return bar.addButton; }
	
	/** Returns the remove button displayed in {@link CategoryImagesPane}. */
	JButton getRemoveButton() { return imagesPane.removeButton; }
	
	/** Returns the reset button displayed in {@link CategoryImagesPane}. */
	JButton getResetButton() { return imagesPane.resetButton; }
	
	/** Returns the remove button displayed in {@link CategoryImagesPane}. */
	JButton getRemoveToAddButton() { return imagesPane.removeToAddButton; }
	
	/** Returns the reset button displayed in {@link CategoryImagesPane}. */
	JButton getResetToAddButton() { return imagesPane.resetToAddButton; }
	
	/** Returns the TextArea displayed in {@link CategoryPane}. */
	JTextArea getDescriptionArea() { return categoryPane.descriptionArea; }

	/** Returns the textfield displayed in {@link CategoryPane}. */
	JTextArea getNameField() { return categoryPane.nameField; }

	/** 
	 * Set the selected tab.
	 * 
	 * @param index	index is one of the following cst 
	 * 				<code>POS_IMAGE</code>, <code>POS_MAIN</code>, 
	 * 				<code>POS_OWNER</code>.
	 */
	void setSelectedPane(int index) { tabs.setSelectedIndex(index); }
	
	/** Reset the imagesPane. */
	void rebuildComponent()
	{
		tabs.remove(POS_IMAGE);
		imagesPane.rebuildComponent();
		IconManager im = IconManager.getInstance(getRegistry());
		tabs.insertTab("Images", im.getIcon(IconManager.IMAGE), imagesPane, 
						null, POS_IMAGE);
		tabs.setSelectedIndex(POS_IMAGE);	
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		//create and initialize the tabs
		tabs = new JTabbedPane(JTabbedPane.TOP, 
										  JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		//TODO: specify lookup name.
        Registry registry = getRegistry();
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		IconManager im = IconManager.getInstance(registry);
		tabs.insertTab("Category", im.getIcon(IconManager.CATEGORY), 
                    categoryPane, null, POS_MAIN);
		tabs.insertTab("Images", im.getIcon(IconManager.IMAGE), imagesPane,
						null, POS_IMAGE);
		tabs.setSelectedComponent(categoryPane);
		tabs.setFont(font);
		tabs.setForeground(DataManagerUIF.STEELBLUE);
		TitlePanel tp = new TitlePanel("Edit Category", 
                                    "Edit an existing category.", 
									im.getIcon(IconManager.CATEGORY_BIG));
		//set layout and add components
        setLayout(new BorderLayout(0, 0));
		add(tp, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		add(bar, BorderLayout.SOUTH);	
	}

}
