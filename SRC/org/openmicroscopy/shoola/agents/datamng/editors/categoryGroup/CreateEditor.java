/*
 * org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.CreateEditor
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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.agents.datamng.editors.controls.CreateBar;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
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
public class CreateEditor
    extends JDialog
{
    
    static final Dimension              MAX_SCROLL = new Dimension(150, 60);
    
    private Registry                    registry;
    private CreateGroupPane             groupPane;
    private CreateCategoryPane          categoryPane;
    private CreateImagesPane            imagesPane;
    private CreateBar                   bar;
    private CreateEditorManager         manager;
    
    public CreateEditor(DataManagerCtrl control, CategoryGroupData[] groups, 
                        CategorySummary[] data, List images)
    {
        super(control.getReferenceFrame(), true);
        this.registry = control.getRegistry();
        manager = new CreateEditorManager(this, control);
        groupPane = new CreateGroupPane(groups);
        categoryPane = new CreateCategoryPane(data);
        imagesPane = new CreateImagesPane(manager, images);
        bar = new CreateBar();
        getSaveButton().setEnabled(true);
        buildGUI();
        manager.initListeners();
        setSize(DataManagerUIF.EDITOR_WIDTH, DataManagerUIF.EDITOR_HEIGHT);
    }
    
    Registry getRegistry() { return registry; }
    
    /** Returns the widget {@link CreateEditorManager manager}. */
    CreateEditorManager getManager() { return manager; }
    
    JTextArea getNameGroup() { return groupPane.nameArea; }
    
    JTextArea getDescriptionGroup() { return groupPane.descriptionArea; }
    
    JTextArea getNameCategory() { return categoryPane.nameArea; }
    
    JTextArea getDescriptionCategory() { return categoryPane.descriptionArea; }
    
    JList getListGroup() { return groupPane.existingGroups; }
    
    JList getListCategory() { return categoryPane.existingCategories; }
    
    JButton getSaveButton() { return bar.getSave(); }
    
    JButton getCancelButton() { return bar.getCancel(); }
    
    JButton getSelectButton() { return imagesPane.selectButton; }
    
    JButton getResetButton() { return imagesPane.resetButton; }
    
    /** Forward event to the pane {@link CreateImagesPane}. */
    void selectAllImages(Boolean b)
    {
        imagesPane.setSelection(b);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, 
                                          JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        IconManager im = IconManager.getInstance(registry);
        //TODO: specify lookup name.
        Font font = (Font) registry.lookup("/resources/fonts/Titles");
        tabs.addTab("Group", im.getIcon(IconManager.CATEGORY_GROUP), 
                    groupPane);
        tabs.addTab("Categories", im.getIcon(IconManager.CATEGORY), 
                    categoryPane);
        tabs.addTab("Images", im.getIcon(IconManager.IMAGE), imagesPane);
        tabs.setSelectedComponent(groupPane);
        tabs.setFont(font);
        tabs.setForeground(DataManagerUIF.STEELBLUE);
        TitlePanel tp = new TitlePanel("Category group and category", 
                        "Create a new group and category.", 
                        im.getIcon(IconManager.CREATE_CG_BIG));
        //set layout and add components
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(tabs, BorderLayout.CENTER);
        c.add(bar, BorderLayout.SOUTH);
    }
    
}
