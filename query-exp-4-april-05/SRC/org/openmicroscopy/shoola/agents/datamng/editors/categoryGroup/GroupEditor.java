/*
 * org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.GroupEditor
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
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
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
public class GroupEditor
    extends JDialog
{
    
    /** ID to identify the tab pane. */
    static final int                POS_MAIN = 0, POS_CATEGORY = 1;
    
    /** Reference to the manager. */
    private GroupEditorManager      manager;
    
    /** Reference to the registry. */
    private Registry                registry;
    
    private GroupEditorBar          bar;
    
    private JTabbedPane             tabs;

    private GroupPane               generalPane;
    
    GroupCategoriesPane             categoriesPane;
    
    public GroupEditor(Registry registry, DataManagerCtrl control,
                         CategoryGroupData model)
    {
        super(control.getReferenceFrame(), true);
        this.registry = registry;
        manager = new GroupEditorManager(this, control, model);
        generalPane = new GroupPane(manager);
        categoriesPane = new GroupCategoriesPane(manager);
        bar = new GroupEditorBar();
        buildGUI();
        manager.initListeners();
        setSize(DataManagerUIF.EDITOR_WIDTH, DataManagerUIF.EDITOR_HEIGHT);
    }
    
    Registry getRegistry() { return registry; } 
    
    /**  Returns the save button displayed {@link CategoryEditorBar}. */
    JButton getSaveButton() { return bar.saveButton; }
    
    /** Returns the save button displayed {@link CategoryEditorBar}. */
    JButton getAddButton() { return bar.addButton; }
    
    /** Returns the cancel button displayed in {@link CategoryEditorBar}. */
    JButton getCancelButton() { return bar.cancelButton; }
    
    /** Returns the remove button displayed in {@link GroupCategoriesPane}. */
    JButton getRemoveToAddButton() { return categoriesPane.removeToAddButton; }
    
    /** Returns the reset button displayed in {@link GroupCategoriesPane}. */
    JButton getResetToAddButton() { return categoriesPane.resetToAddButton; }
    
    /** Returns the TextArea displayed in {@link GroupPane}. */
    JTextArea getDescriptionArea() { return generalPane.descriptionArea; }

    /** Returns the textfield displayed in {@link GroupPane}. */
    JTextArea getNameField() { return generalPane.nameField; }

    /** 
     * Set the selected tab.
     * 
     * @param index index is one of the following cst 
     *              <code>POS_IMAGE</code>, <code>POS_MAIN</code>, 
     *              <code>POS_OWNER</code>.
     */
    void setSelectedPane(int index) { tabs.setSelectedIndex(index); }
    
    /** Reset the categoriesPane. */
    void rebuildComponent()
    {
        tabs.remove(POS_CATEGORY);
        categoriesPane.rebuildComponent();
        IconManager im = IconManager.getInstance(registry);
        tabs.insertTab("Categories", im.getIcon(IconManager.IMAGE), 
                        categoriesPane, null, POS_CATEGORY);
        tabs.setSelectedIndex(POS_CATEGORY);   
    }
    
    /** Build and layout the GUI. */
    private void buildGUI()
    {
        //create and initialize the tabs
        tabs = new JTabbedPane(JTabbedPane.TOP, 
                                          JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        //TODO: specify lookup name.
        Font font = (Font) registry.lookup("/resources/fonts/Titles");
        IconManager im = IconManager.getInstance(registry);
        tabs.insertTab("General", im.getIcon(IconManager.CATEGORY_GROUP), 
                        generalPane, null, POS_MAIN);
        tabs.insertTab("Categories", im.getIcon(IconManager.CATEGORY), 
                        categoriesPane, null, POS_CATEGORY);

        tabs.setSelectedComponent(generalPane);
        tabs.setFont(font);
        tabs.setForeground(DataManagerUIF.STEELBLUE);
        TitlePanel tp = new TitlePanel("Edit Group", 
                                "Edit an existing category group.", 
                                    im.getIcon(IconManager.CATEGORY_GROUP_BIG));
        //set layout and add components
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(tabs, BorderLayout.CENTER);
        c.add(bar, BorderLayout.SOUTH); 
    }

}
