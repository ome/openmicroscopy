/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapGroupBar
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
package org.openmicroscopy.shoola.agents.browser.colormap;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.shoola.agents.browser.datamodel.CategoryTree;

/**
 * The bar that contains the category-selection combo box.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorMapGroupBar extends JPanel
{
    private CategoryTree categoryTree;
    private JComboBox comboBox;
    private Set listenerSet;
    
    public ColorMapGroupBar()
    {
        init();
        buildUI();
        comboBox.setEnabled(false);
    }
    
    public ColorMapGroupBar(CategoryTree tree)
    {
        if(tree == null) {
            init();
            buildUI();
            comboBox.setEnabled(false);
            return;
        }
        else
        {
            this.categoryTree = tree;
            comboBox.setEnabled(true);
            init();
            buildUI();
        }
    }
    
    public void setCategoryTree(CategoryTree model)
    {
        categoryTree = model;
        if(categoryTree == null)
        {
            buildInactiveBox();
        }
        else
        {
            buildComboBox(categoryTree);
        }
        repaint();
    }
    
    public void init()
    {
        listenerSet = new HashSet();
    }
    
    private void buildInactiveBox()
    {
        Object[] defaultObject = new String[]{"Select a class"};
        comboBox.setModel(new DefaultComboBoxModel(defaultObject));
        notifyDeselection();
        comboBox.setEnabled(false);
        comboBox.removeItemListener(selectionListener);
    }
    
    private void buildComboBox(CategoryTree tree)
    {
        if(tree == null) {
            buildInactiveBox();
            return;
        }
        
        List groupList = categoryTree.getCategoryGroups();
        String[] groupNames = new String[groupList.size()+1];
        groupNames[0] = "Select a class:";
        for(int i=0;i<groupList.size();i++)
        {
            CategoryGroup group = (CategoryGroup)groupList.get(i);
            groupNames[i+1] = group.getName();
        }
        comboBox.setModel(new DefaultComboBoxModel(groupNames));
        comboBox.setEnabled(true);
        // just in case, remove (will need to add if shifting from null)
        comboBox.removeItemListener(selectionListener);
        comboBox.addItemListener(selectionListener);
    }
    
    public void buildUI()
    {
        comboBox = new JComboBox();
        if(categoryTree == null)
        {
            buildInactiveBox();
        }
        else
        {
            buildComboBox(categoryTree);
        }
        
        setLayout(new BorderLayout(2,2));
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(new JLabel("Select class:"));
        add(labelPanel,BorderLayout.NORTH);
        add(comboBox,BorderLayout.CENTER);
    }
    
    public void addListener(ColorMapGroupListener listener)
    {
        if(listener != null)
        {
            listenerSet.add(listener);
        }
    }
    
    public void removeListener(ColorMapGroupListener listener)
    {
        if(listener != null)
        {
            listenerSet.remove(listener);
        }
    }
    
    private void notifyListeners(CategoryGroup selectedGroup)
    {
        if(selectedGroup == null)
        {
            return;
        }
        for(Iterator iter = listenerSet.iterator(); iter.hasNext();)
        {
            ColorMapGroupListener listener =
                (ColorMapGroupListener)iter.next();
            listener.groupSelected(selectedGroup);
        }
    }
    
    private void notifyDeselection()
    {
        for(Iterator iter = listenerSet.iterator(); iter.hasNext();)
        {
            ColorMapGroupListener listener =
                (ColorMapGroupListener)iter.next();
            listener.groupsDeselected();
        }
    }
    
    private final ItemListener selectionListener = new ItemListener()
    {
        public void itemStateChanged(ItemEvent arg0)
        {
            if(arg0.getStateChange() == ItemEvent.SELECTED)
            {
                System.err.println("item selected");
                int index = comboBox.getSelectedIndex()-1;
                if(index == -1) return;
                CategoryGroup group = categoryTree.getCategoryGroup(index);
                System.err.println("Selected group: "+group.getName());
                notifyListeners(group);
            }
            if(arg0.getStateChange() == ItemEvent.DESELECTED)
            {
                int index = comboBox.getSelectedIndex()-1;
                if(index < 0)
                {
                    notifyDeselection();
                }
            }
        }
    };
    
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        comboBox.setEnabled(enabled);
    }
}
