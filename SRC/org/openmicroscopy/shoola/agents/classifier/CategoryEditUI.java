/*
 * org.openmicroscopy.shoola.agents.classifier.CategoryEditUI
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.classifier;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version
 * @since
 */
public class CategoryEditUI extends JDialog
{
    private CategoryCtrl control;
    private CategoryUI parent;
    private CategoryGroup group;
    private Category category;
    
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JButton saveButton;
    private JButton cancelButton;
    
    private static final int NEW_GROUP_MODE = 0;
    private static final int NEW_CATEGORY_MODE = 1;
    private static final int EDIT_GROUP_MODE = 2;
    private static final int EDIT_CATEGORY_MODE = 3;
    
    private final Action newCategoryAction = new AbstractAction("Add")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
            control.newCategory(group,nameField.getText(),
                                descriptionArea.getText());
            parent.buildGroupList();
            setVisible(false);
            dispose();
        }
    };
    
    private final Action newGroupAction = new AbstractAction("Add")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
            control.newCategoryGroup(nameField.getText(),
                                     descriptionArea.getText());
            parent.buildGroupList();
            setVisible(false);
            dispose();
        }
    };
    
    private final Action saveCategoryAction = new AbstractAction("Save")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
            category.setName(nameField.getText());
            category.setDescription(descriptionArea.getText());
            control.markCategoryAsChanged(category);
            parent.buildCategoryList(group);
            setVisible(false);
            dispose();
        }
    };
    
    private final Action saveGroupAction = new AbstractAction("Save")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
            group.setName(nameField.getText());
            group.setDescription(descriptionArea.getText());
            control.markCategoryGroupAsChanged(group);
            parent.buildGroupList();
            setVisible(false);
            dispose();
        }
    };
    
    private final Action cancelAction = new AbstractAction("Cancel")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            setVisible(false);
            dispose();
        }
    };
    
    private CategoryEditUI(int mode, CategoryUI parent,
                           CategoryCtrl control,
                           CategoryGroup parentGroup,
                           CategoryGroup groupToEdit,
                           Category categoryToEdit)
    {
         super(parent,true);
         setLocationRelativeTo(parent);
         this.parent = parent;
         this.control = control;
         buildGUI();
         
         cancelAction.setEnabled(true);
         
         if(mode == NEW_GROUP_MODE)
         {
             saveButton.setAction(newGroupAction);
             cancelButton.setAction(cancelAction);
         }
         else if(mode == NEW_CATEGORY_MODE)
         {
             this.group = parentGroup;
             saveButton.setAction(newCategoryAction);
             cancelButton.setAction(cancelAction);
         }
         else if(mode == EDIT_GROUP_MODE)
         {
             this.group = groupToEdit;
             nameField.setText(groupToEdit.getName());
             descriptionArea.setText(groupToEdit.getDescription());
             saveButton.setAction(saveGroupAction);
             cancelButton.setAction(cancelAction);
         }
         else if(mode == EDIT_CATEGORY_MODE)
         {
             this.group = groupToEdit;
             this.category = categoryToEdit;
             nameField.setText(categoryToEdit.getName());
             descriptionArea.setText(categoryToEdit.getDescription());
             saveButton.setAction(saveCategoryAction);
             cancelButton.setAction(cancelAction);
         }
    }
    
    public static void showCreateGroupDialog(CategoryUI parent,
                                             CategoryCtrl control)
    {
        CategoryEditUI ui = new CategoryEditUI(NEW_GROUP_MODE,parent,
                                               control,null,null,null);
        ui.setTitle("Create Group");
        ui.pack();
        ui.show();
    }
    
    public static void showCreateCategoryDialog(CategoryUI parent,
                                                CategoryCtrl control,
                                                CategoryGroup group)
    {
        CategoryEditUI ui = new CategoryEditUI(NEW_CATEGORY_MODE,parent,
                                               control,group,null,null);
        ui.setTitle("Create Phenotype");
        ui.pack();
        ui.show();
    }
    
    public static void showEditGroupDialog(CategoryUI parent,
                                           CategoryCtrl control,
                                           CategoryGroup group)
    {
        CategoryEditUI ui = new CategoryEditUI(EDIT_GROUP_MODE,parent,
                                               control,null,group,null);
        ui.setTitle("Edit Group");
        ui.pack();
        ui.show();
    }
    
    public static void showEditCategoryDialog(CategoryUI parent,
                                              CategoryCtrl control,
                                              CategoryGroup group,
                                              Category category)
    {
        CategoryEditUI ui = new CategoryEditUI(EDIT_CATEGORY_MODE,parent,
                                               control,null,group,category);
        ui.setTitle("Edit Phenotype");
        ui.pack();
        ui.show();
    }
    
    public void buildGUI()
    {
        Container container = getContentPane();
        container.setLayout(new BorderLayout(5,5));
        
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BorderLayout(5,5));
        namePanel.add(new JLabel("Set name:"),BorderLayout.WEST);
        nameField = new JTextField();
        namePanel.add(nameField,BorderLayout.CENTER);
        container.add(namePanel,BorderLayout.NORTH);
        
        JPanel areaPanel = new JPanel();
        areaPanel.setLayout(new BorderLayout(5,5));
        areaPanel.add(new JLabel("Set description:"),BorderLayout.NORTH);
        descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(300,150));
        areaPanel.add(scrollPane,BorderLayout.CENTER);
        container.add(areaPanel,BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("Save");
        saveButton.setEnabled(true);
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(true);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);
        
        container.add(buttonPanel,BorderLayout.SOUTH);
    }
}
