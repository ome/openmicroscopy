/*
 * org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.CreateGroupEditor
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
import org.openmicroscopy.shoola.agents.datamng.editors.controls.CreateBar;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

/** 
 * Widget to create a new CategoryGroup.
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
public class CreateGroupEditor
    extends JDialog
{
  
    private Registry                    registry;
    private CreateGroupPane             groupPane;
    private CreateBar                   bar;
    private CreateGroupEditorMng        manager;
    
    public CreateGroupEditor(DataManagerCtrl control)
    {
        super(control.getReferenceFrame(), true);
        this.registry = control.getRegistry();
        manager = new CreateGroupEditorMng(this, control);
        groupPane = new CreateGroupPane();
        bar = new CreateBar();
        getSaveButton().setEnabled(true);
        buildGUI();
        manager.initListeners();
        setSize(DataManagerUIF.EDITOR_WIDTH, DataManagerUIF.EDITOR_HEIGHT);
    }
    
    Registry getRegistry() { return registry; }
    
    /** Returns the widget {@link CreateGroupEditorMng manager}. */
    CreateGroupEditorMng getManager() { return manager; }
    
    JTextArea getGroupName() { return groupPane.nameArea; }
    
    JTextArea getGroupDescription() { return groupPane.descriptionArea; }
    
    JButton getSaveButton() { return bar.getSave(); }
    
    JButton getCancelButton() { return bar.getCancel(); }

    
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
        tabs.setSelectedComponent(groupPane);
        tabs.setFont(font);
        tabs.setForeground(DataManagerUIF.STEELBLUE);
        TitlePanel tp = new TitlePanel("Category group", 
                        "Create a new group.", 
                        im.getIcon(IconManager.CREATE_GROUP_BIG));
        //set layout and add components
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(tabs, BorderLayout.CENTER);
        c.add(bar, BorderLayout.SOUTH);
    }
    
}
