/*
 * org.openmicroscopy.shoola.agents.classifier.CategoryUI
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.TopFrame;

/**
 * The UI for displaying lists of categories for a particular dataset.
 *  
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class CategoryUI extends JDialog
{
    private Registry registry;
    private TopFrame topFrame;
    private CategoryCtrl controller;
    
    private JList groupList = new JList();
    private JList categoryList = new JList();
    
    private DefaultListModel groupModel;
    private DefaultListModel categoryModel;
    
    private JButton newGroupButton;
    private JButton editButton;
    private JButton newCategoryButton;
    
    private boolean pendingNewGroup = false;
    private boolean pendingNewCategory = false;
    private boolean changingGroup = false;
    private boolean changingCategory = false;
    
    private int selectedGroup = -1;
    private int selectedCategory = -1;
    
    private CategoryUI refCopy;
    
    private final Action editGroupAction = new AbstractAction("Edit")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            int selectedIndex = groupList.getSelectedIndex();
            CategoryGroup group =
                (CategoryGroup)controller.getCategoryGroups().get(selectedIndex);
            CategoryEditUI.showEditGroupDialog(refCopy,controller,group);
        }
    };
    
    private final Action editCategoryAction = new AbstractAction("Edit")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            int selectedGroup = groupList.getSelectedIndex();
            int selectedCategory = categoryList.getSelectedIndex();
            CategoryGroup group =
                (CategoryGroup)controller.getCategoryGroups().get(selectedGroup);
            Category category =
                (Category)controller.getCategories(group).get(selectedCategory);
            CategoryEditUI.showEditCategoryDialog(refCopy,controller,group,category);
        }
    };
    
    private final Action saveAction = new AbstractAction("Save Changes")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            controller.save();
            setVisible(false);
            dispose();
            controller.close();
        }
    };
    
    private final Action cancelAction = new AbstractAction("Cancel")
    {
        public void actionPerformed(ActionEvent arg0)
        {
            setVisible(false);
            dispose();
            controller.close();
        }
    };
    
    public CategoryUI(CategoryCtrl control, Registry registry)
    {
        super(registry.getTopFrame().getFrame());
        setTitle("Categories: "+control.getDatasetName());
        refCopy = this; // my everpresent inner class hack
        this.topFrame = registry.getTopFrame();
        if(control == null || registry == null)
        {
            throw new IllegalArgumentException("Parameters cannot be null.");
        }
        
        this.controller = control;
        this.registry = registry;
        
        groupModel = new DefaultListModel();
        categoryModel = new DefaultListModel();
        
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        final Component refCopy = this;
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent arg0)
            {
                if(controller.isSaved())
                {
                    setVisible(false);
                    dispose();
                    controller.close();
                }
                else
                {
                    Object[] options = {"Save","Don't Save","Cancel"};
                    int status = JOptionPane.showOptionDialog(refCopy,
                                    "Would you like to keep the changes?",
                                    "Save Annotation",
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[0]);
                    if(status == JOptionPane.YES_OPTION)
                    {
                        // TODO add extraction from text area -> controller
                        controller.save();
                        setVisible(false);
                        dispose();
                        controller.close();
                    }
                    else if(status == JOptionPane.NO_OPTION)
                    {
                        setVisible(false);
                        dispose();
                        controller.close();
                    }
                    else if(status == JOptionPane.CANCEL_OPTION)
                    {
                        // do nothing
                    }
                }
            }
        });
        
        buildGroupList();
        buildGUI();
        setLocationRelativeTo(topFrame.getFrame());
        pack();
    }
    
    public void buildGroupList()
    {
        groupModel.clear();
        if(controller.getCategoryGroups() == null) return;
        List theList = controller.getCategoryGroups();
        for(Iterator iter = theList.iterator(); iter.hasNext();)
        {
            CategoryGroup group = (CategoryGroup)iter.next();
            groupModel.addElement(group.getName());
        }
        groupList.setModel(groupModel);
        repaint();
    }
    
    public void buildCategoryList(CategoryGroup parent)
    {
        categoryModel.clear();
        System.err.println(parent.getName());
        if(controller.getCategories(parent) == null) return;
        List theList = controller.getCategories(parent);
        for(Iterator iter = theList.iterator(); iter.hasNext();)
        {
            Category category = (Category)iter.next();
            System.err.println(category.getName());
            categoryModel.addElement(category.getName());
        }
        categoryList.setModel(categoryModel);
        repaint();
    }
    
    private void buildGUI()
    {
        Container container = getContentPane();
        container.setLayout(new BorderLayout(2,2));
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(1,2));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(2,2));
        JPanel leftLabelPanel = new JPanel();
        leftLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        leftLabelPanel.add(new JLabel("Groups:"));
        leftPanel.add(leftLabelPanel,BorderLayout.NORTH);
        
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                newCategoryButton.setEnabled(true);
                categoryList.setEnabled(true);
                if(groupList.getSelectedIndex() != -1)
                {
                    selectedGroup = groupList.getSelectedIndex();
                }
                CategoryGroup group =
                    (CategoryGroup)controller.getCategoryGroups().get(selectedGroup);
                buildCategoryList(group);
                repaint();
            }
        });
        
        groupList.addFocusListener(new FocusAdapter()
        {
            public void focusLost(FocusEvent arg0)
            {
                if(arg0.getOppositeComponent() != editButton)
                {
                    editButton.setEnabled(false);
                }
            }
            
            public void focusGained(FocusEvent arg0)
            {
                editButton.setEnabled(true);
                editButton.setAction(editGroupAction);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(groupList);
        scrollPane.setPreferredSize(new Dimension(150,225));
        leftPanel.add(scrollPane,BorderLayout.CENTER);
        listPanel.add(leftPanel);
        
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(2,2));
        JPanel rightLabelPanel = new JPanel();
        rightLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        rightLabelPanel.add(new JLabel("Phenotypes:"));
        rightPanel.add(rightLabelPanel,BorderLayout.NORTH);
        
        categoryList.setEnabled(false);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                int i = categoryList.getSelectedIndex();
                if(i != -1) selectedCategory = i;
                CategoryGroup group =
                    (CategoryGroup)controller.getCategoryGroups().get(selectedGroup);
                Category cat =
                    (Category)controller.getCategories(group).get(selectedCategory);
                repaint();
            }
        });
        
        categoryList.addFocusListener(new FocusAdapter()
        {
            public void focusLost(FocusEvent arg0)
            {
                if(arg0.getOppositeComponent() != editButton)
                {
                    editButton.setEnabled(false);
                }
            }
            
            public void focusGained(FocusEvent arg0)
            {
                editButton.setEnabled(true);
                editButton.setAction(editCategoryAction);
            }
        });
        
        JScrollPane rightScrollPane = new JScrollPane(categoryList);
        rightScrollPane.setPreferredSize(new Dimension(150,225));
        rightPanel.add(rightScrollPane,BorderLayout.CENTER);
        listPanel.add(rightPanel);
        
        JPanel detailsPanel = new JPanel();
        detailsPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
        detailsPanel.setLayout(new BorderLayout(2,2));
        detailsPanel.add(listPanel,BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        
        newGroupButton = new JButton("New Group");
        newGroupButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                CategoryEditUI.showCreateGroupDialog(refCopy,controller);
            }
        });  
        
        buttonPanel.add(newGroupButton);
        editButton = new JButton("Edit");
        buttonPanel.add(editButton);
        
        newCategoryButton = new JButton("New Phenotype");
        newCategoryButton.setEnabled(false);
        newCategoryButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                CategoryGroup group =
                    (CategoryGroup)controller.getCategoryGroups().get(selectedGroup);
                CategoryEditUI.showCreateCategoryDialog(refCopy,controller,group);
            }
        });
        
        buttonPanel.add(newCategoryButton);
        detailsPanel.add(buttonPanel,BorderLayout.SOUTH);
        container.add(detailsPanel,BorderLayout.CENTER);
        
        JPanel savePanel = new JPanel();
        savePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save");
        saveButton.setAction(saveAction);
        savePanel.add(saveButton);
        savePanel.add(Box.createHorizontalStrut(10));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAction(cancelAction);
        savePanel.add(cancelButton);
        container.add(savePanel,BorderLayout.SOUTH);
    }
}
