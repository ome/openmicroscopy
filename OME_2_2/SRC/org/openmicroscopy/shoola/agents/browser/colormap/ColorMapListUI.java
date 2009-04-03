/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapListUI
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
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;

/**
 * The UI widget which contains the color list.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorMapListUI extends JPanel
                            implements ColorMapList
{
    private CategoryGroup categoryGroup;
    private JList categoryListUI;
    private ColorPairModel colorModel;
    private Set selectionListeners;
    
    public ColorMapListUI()
    {
        init();
        buildUI();
    }
    
    public ColorMapListUI(ColorPairModel model)
    {
        init();
        buildUI();
        buildModel(model);
    }
    
    public ColorPairModel getModel()
    {
        return colorModel;
    }

    
    public void setModel(ColorPairModel model)
    {
        colorModel = model;
        buildModel(model);
        repaint();
    }
    
    private void init()
    {
        selectionListeners = new HashSet();
    }
    
    private void buildUI()
    {
        setLayout(new BorderLayout());
        categoryListUI = new JList();
        categoryListUI.setCellRenderer(new ColorCellRenderer());
        categoryListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(categoryListUI);
        scrollPane.setPreferredSize(new Dimension(200,200));
        scrollPane.setSize(new Dimension(200,200));
        add(scrollPane,BorderLayout.CENTER);
        
        categoryListUI.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent arg0)
            {
                int i = arg0.getFirstIndex();
                ColorPair pair = colorModel.get(i);
                Category category = pair.getCategory();
                notifyListeners(category);
            }
        });
    }
    
    private void buildModel(ColorPairModel model)
    {
        if(model == null)
        {
            categoryListUI.setModel(new DefaultListModel());
            categoryListUI.setEnabled(false);
        }
        else
        {
            notifyDeselection();
            categoryListUI.setEnabled(true);
            DefaultListModel listModel = new DefaultListModel();
            for(int i=0;i<model.size();i++)
            {
                ColorPair pair = model.get(i);
                listModel.addElement(pair);
            }
            categoryListUI.setModel(listModel);
        }
    }
    
    private void notifyListeners(Category category)
    {
        for(Iterator iter = selectionListeners.iterator(); iter.hasNext();)
        {
            ColorMapCategoryListener listener =
                (ColorMapCategoryListener)iter.next();
            listener.categorySelected(category);
        }
    }
    
    private void notifyDeselection()
    {
        for(Iterator iter = selectionListeners.iterator(); iter.hasNext();)
        {
            ColorMapCategoryListener listener =
                (ColorMapCategoryListener)iter.next();
            listener.categoriesDeselected();
        }
    }
    
    public void addListener(ColorMapCategoryListener listener)
    {
        if(listener != null)
        {
            selectionListeners.add(listener);
        }
    }
    
    public void removeListener(ColorMapCategoryListener listener)
    {
        if(listener != null)
        {
            selectionListeners.remove(listener);
        }
    }
    
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        categoryListUI.setEnabled(enabled);
    }
}
