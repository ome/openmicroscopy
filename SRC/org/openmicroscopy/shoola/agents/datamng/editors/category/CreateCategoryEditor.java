/*
 * org.openmicroscopy.shoola.agents.datamng.editors.category.CreateCategoryEditor
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
import java.awt.Dimension;
import java.awt.Font;
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
import org.openmicroscopy.shoola.util.ui.TitlePanel;

import pojos.CategoryGroupData;

/** 
 * Create Category widget.
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
public class CreateCategoryEditor
    extends JPanel
{
        
    static final Dimension              MAX_SCROLL = new Dimension(150, 60);
    
    private DataManagerCtrl             agentCtrl;
    private CreateCategoryPane          categoryPane;
    private CreateCategoryImagesPane    imagePane;
    private CreateBar                   bar;
    private CreateCategoryEditorMng     manager;
    
    private CategoryGroupData           selectedGroup;
    
    public CreateCategoryEditor(DataManagerCtrl agentCtrl, Set groups, 
                            int selectedCategoryGroupID)
    {
        this.agentCtrl = agentCtrl;
        manager = new CreateCategoryEditorMng(this, agentCtrl);
        categoryPane = new CreateCategoryPane(groups, selectedCategoryGroupID);
        imagePane = new CreateCategoryImagesPane(manager);
        bar = new CreateBar();
        getSaveButton().setEnabled(true);
        buildGUI();
        manager.initListeners();
        setSize(DataManagerUIF.EDITOR_WIDTH, DataManagerUIF.EDITOR_HEIGHT);
    }
    
    public CreateCategoryEditor(DataManagerCtrl agentCtrl,
                                CategoryGroupData group)
    {
        selectedGroup = group;
        this.agentCtrl = agentCtrl;
        manager = new CreateCategoryEditorMng(this, agentCtrl);
        categoryPane = new CreateCategoryPane();
        imagePane = new CreateCategoryImagesPane(manager);
        bar = new CreateBar();
        getSaveButton().setEnabled(true);
        buildGUI();
        manager.initListeners();
        setSize(DataManagerUIF.EDITOR_WIDTH, DataManagerUIF.EDITOR_HEIGHT);
    }
    
    Registry getRegistry() { return agentCtrl.getRegistry(); }
    
    /** Returns the widget {@link CreateCategoryEditorMng manager}. */
    CreateCategoryEditorMng getManager() { return manager; }
    
    JTextArea getCategoryName() { return categoryPane.nameArea; }
    
    JTextArea getCategoryDescription() { return categoryPane.descriptionArea; }
    
    //JComboBox getExistingGroups() { return categoryPane.groups; }
    
    JButton getSaveButton() { return bar.getSave(); }
    
    JButton getSelectButton() { return imagePane.selectButton; }
    
    JButton getResetButton() { return imagePane.resetButton; }
    
    JButton getShowImagesButton() { return imagePane.showImages; }
    
    JButton getFilterButton() { return imagePane.filter; }
    
    JComboBox getImagesSelection() { return imagePane.selections; }
    
    /** List of imageSummary object. */
    void showImages(Set images) { imagePane.showImages(images); }
    
    void selectAllImages() { imagePane.setSelection(Boolean.TRUE); }
    
    void resetImageSelection() { imagePane.setSelection(Boolean.FALSE); }
    
    CategoryGroupData getSelectedCategoryGroup()
    {
        return selectedGroup;
        //return categoryPane.getSelectedCategoryGroup();
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
                                          JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        Registry registry = getRegistry();
        IconManager im = IconManager.getInstance(registry);
        //TODO: specify lookup name.
        Font font = (Font) registry.lookup("/resources/fonts/Titles");
        tabs.addTab("Category", im.getIcon(IconManager.CATEGORY), 
                    categoryPane);
        //tabs.addTab("Add Existing Images", im.getIcon(IconManager.IMAGE), 
        //            imagePane);
        tabs.setSelectedComponent(categoryPane);
        tabs.setFont(font);
        tabs.setForeground(DataManagerUIF.STEELBLUE);
        TitlePanel tp = new TitlePanel("Category", "Create a new category.", 
                        im.getIcon(IconManager.CREATE_CATEGORY_BIG));
        //set layout and add components
        setLayout(new BorderLayout(0, 0));
        add(tp, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);
    }

}
