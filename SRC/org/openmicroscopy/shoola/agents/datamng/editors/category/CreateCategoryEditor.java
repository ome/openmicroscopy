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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
public class CreateCategoryEditor
    extends JDialog
{
        
    static final Dimension              MAX_SCROLL = new Dimension(150, 60);
    
    private Registry                    registry;
    private CreateCategoryPane          categoryPane;
    private CreateCategoryImagesPane    imagePane;
    private CreateBar                   bar;
    private CreateCategoryEditorMng     manager;
    
    public CreateCategoryEditor(DataManagerCtrl control, List groups)
    {
        super(control.getReferenceFrame(), true);
        this.registry = control.getRegistry();
        manager = new CreateCategoryEditorMng(this, control);
        categoryPane = new CreateCategoryPane(groups);
        imagePane = new CreateCategoryImagesPane(manager);
        bar = new CreateBar();
        getSaveButton().setEnabled(true);
        buildGUI();
        manager.initListeners();
        setSize(DataManagerUIF.EDITOR_WIDTH, DataManagerUIF.EDITOR_HEIGHT);
    }
    
    Registry getRegistry() { return registry; }
    
    /** Returns the widget {@link CreateCategoryEditorMng manager}. */
    CreateCategoryEditorMng getManager() { return manager; }
    
    JTextArea getCategoryName() { return categoryPane.nameArea; }
    
    JTextArea getCategoryDescription() { return categoryPane.descriptionArea; }
    
    JComboBox getExistingGroups() { return categoryPane.groups; }
    
    JButton getSaveButton() { return bar.getSave(); }
    
    JButton getCancelButton() { return bar.getCancel(); }
    
    JButton getSelectButton() { return imagePane.selectButton; }
    
    JButton getResetButton() { return imagePane.resetButton; }
    
    JButton getShowImagesButton() { return imagePane.showImages; }
    
    /** List of imageSummary object. */
    void showImages(List images)
    {
        imagePane.showImages(images);
    }
    
    void selectAllImages()
    {
        imagePane.setSelection(Boolean.TRUE);
    }
    
    void resetImageSelection()
    {
        imagePane.setSelection(Boolean.FALSE);  
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
        tabs.addTab("Category", im.getIcon(IconManager.CATEGORY), 
                    categoryPane);
        tabs.addTab("Add Existing Images", im.getIcon(IconManager.IMAGE), 
                    imagePane);
        tabs.setSelectedComponent(categoryPane);
        tabs.setFont(font);
        tabs.setForeground(DataManagerUIF.STEELBLUE);
        TitlePanel tp = new TitlePanel("Category", "Create a new category.", 
                        im.getIcon(IconManager.CREATE_CATEGORY_BIG));
        //set layout and add components
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(tabs, BorderLayout.CENTER);
        c.add(bar, BorderLayout.SOUTH);
    }

}
