/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.formats.importer.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

/**
 * @author Brian W. Loranger
 *
 */
public class HistoryTaskBar extends JXPanel implements ActionListener
{
    private static final long serialVersionUID = 1L;

    JXTaskPaneContainer  tpContainer = new JXTaskPaneContainer();
    
    DefaultListModel today = new DefaultListModel(); 
    DefaultListModel yesterday = new DefaultListModel();
    DefaultListModel thisWeek = new DefaultListModel();
    DefaultListModel lastWeek = new DefaultListModel();
    DefaultListModel thisMonth = new DefaultListModel();
    
    
    /**
     * Create a new history task bar
     */
    public HistoryTaskBar() 
    {
        
        double table[][] =
        {{158}, // columns
        { TableLayout.PREFERRED}}; // rows
        
        TableLayout layout = new TableLayout(table);
       
        this.setLayout(layout);
        
        tpContainer.setBorder(null);
        tpContainer.setLayout(new VerticalLayout(0));                   
        this.add(tpContainer, "0,0");
    }
    
    /**
     * Add a new task pane to the bar
     * 
     * @param name - string name to use for task pane
     * @param list - pane's contents
     */
    public void addTaskPane( String name, JList list )
    {
        JXTaskPane taskPane = new JXTaskPane();       
        taskPane.setTitle(name);
       
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        taskPane.getContentPane().getParent().add(scrollPane);
        
        taskPane.setCollapsed(true);
        
        tpContainer.add(taskPane);
    }

    /**
     * Fire property change and retrieve a list when its clicked on
     * @param list - original list
     * @return returned list
     */
    public JList getList(JList list)
    {
        list.setCellRenderer(new ImportCellRenderer());
        //list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        
        // Add a listener for mouse clicks
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() > 0) {          // Double-click
                    // Get item index
                    int index = list.locationToIndex(evt.getPoint());
                    if (index >= 0)
                    {
                        ImportEntry entry = (ImportEntry) list.getModel().getElementAt(index);
                        firePropertyChange("QUICK_HISTORY", -1, entry.importKey);
                    }
                }
            }
        });
        return list;
    }
    

    /**
     * Update task list adding theirs to mine
     * 
     * @param list - list
     * @param mine - defaultListModel being added to 
     * @param theirs - defaultListModel being added from
     */
    public void updateList(JList list, DefaultListModel mine, DefaultListModel theirs)
    {
        mine.clear();
        for (int i =0; i < theirs.size(); i++)
        {
            mine.addElement(theirs.get(i));
        }
        list.validate();
        tpContainer.validate();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {}
}

/**
 * @author Brian W. Loranger
 *
 */
class ImportCellRenderer extends JLabel implements ListCellRenderer 
{
    private static final long serialVersionUID = 1L;

    /**
     * Set defaults for cell rendered to render correctly
     */
    public ImportCellRenderer() {
        setOpaque(true);
        setIconTextGap(0);
    }

    /* (non-Javadoc)
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        ImportEntry entry = (ImportEntry) value;
        setText(entry.getTitle());
        setIcon(entry.getImage());
        setFont(UIManager.getFont("TableCell.font"));
        
        if (isSelected) {
            //setToolTipText("test");
            setBackground(UIManager.getColor("Table.selectionBackground"));
            setForeground(Color.black);
        } else {
            setBackground(Color.white);
            setForeground(Color.black);
        }
        return this;
    }
}

/**
 * @author Brian W. Loranger
 *
 */
class ImportEntry 
{
    private final String title;
    private final String imagePath;
    private ImageIcon image;
    public final int importKey;

    public ImportEntry(String title, String imagePath, int importKey) {
        this.title = title;
        this.imagePath = imagePath;
        this.importKey = importKey;
    }

    /**
     * Returns the title string for the import entry
     * @return - title string
     */
    public String getTitle() 
    {
        return title;
    }
        
    //TODO: merge getImage with GuiCommonElements
    /**
     * @return - image icon for the 
     */
    public ImageIcon getImage() 
    {
        if (imagePath != null)
        {
            java.net.URL imgURL = GuiImporter.class.getResource(imagePath);
            if (imgURL != null)
            {
               image = new ImageIcon(imgURL);
            } else
            {
                System.err.println("Couldn't find icon: " + imagePath);
            }
        }
        return image;
    }

    // Override standard toString method to give a useful result
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return title;
    }
}