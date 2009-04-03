/*
 * org.openmicroscopy.shoola.agents.datamng.editors.category.CreateCategoryImagesPane
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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.table.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableSorter;

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
class CreateCategoryImagesPane
    extends JPanel
{
    
    static final String[]                   listOfItems;
    
    static final int                        IMAGES_IMPORTED = 0;
    static final int                        IMAGES_USED = 1;
    static final int                        IMAGES_GROUP = 2;
    static final int                        IMAGES_SYSTEM = 3;
    private static final int                MAX_ID = 3;
    
    static {
        listOfItems = new String[MAX_ID+1];
        listOfItems[IMAGES_IMPORTED] = "All images I own";
        listOfItems[IMAGES_USED] = "All images in my datasets";
        listOfItems[IMAGES_GROUP] = "All images in my group";
        listOfItems[IMAGES_SYSTEM] = "All images";
    }
    
    /** Action id. */
    private static final int               NAME = 0, SELECT = 1;
    
    protected static final String[]        columnNames;

    static {
        columnNames  = new String[2];
        columnNames[NAME] = "Name";
        columnNames[SELECT] = "Select";
    }
        
    JButton                                 selectButton, resetButton, 
                                            showImages;
    
    /** List of images we wish to display. */
    JComboBox                               selections;
    
    /** Reference to the manager. */
    private CreateCategoryEditorMng         manager;

    private ImagesTableModel                imagesTM;
    
    private TableSorter                     sorter;
    
    private JPanel                          componentsPanel, selectionsPanel;
    
    CreateCategoryImagesPane(CreateCategoryEditorMng manager)
    {
        this.manager = manager;
        initComponents();
        buildGUI();
    }

    /** Select or not all images. */
    void setSelection(Object val)
    {
        int countCol = imagesTM.getColumnCount()-1;
        for (int i = 0; i < imagesTM.getRowCount(); i++)
                imagesTM.setValueAt(val, i, countCol); 
    }

    /** Rebuild the component to display the existing images. */ 
    void showImages(List images)
    {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(componentsPanel);
        p.add(selectionsPanel);
        add(p);
        //add(Box.createRigidArea(DataManagerUIF.VBOX));
        //add(componentsPanel);
        //add(Box.createRigidArea(DataManagerUIF.VBOX));
        if (images != null && images.size() != 0) {
            add(buildImagesPanel(images));
            setButtonsEnabled(true);
        }
        manager.getView().repaint();
    }

    /** Set the buttons enabled. */
    private void setButtonsEnabled(boolean b)
    {
        selectButton.setEnabled(b);
        resetButton.setEnabled(b);
        //showImages.setEnabled(b);
    }
    
    /** Initializes the components. */
    private void initComponents()
    {
        //select button
        selectButton = new JButton("Select All");
        selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        selectButton.setToolTipText(
            UIUtilities.formatToolTipText("Select all images."));
        
        //cancel button
        resetButton = new JButton("Reset");
        resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetButton.setToolTipText(
            UIUtilities.formatToolTipText("Cancel selection."));
        
        //ShowImages button
        showImages = new JButton("Show images");
        showImages.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showImages.setToolTipText(
            UIUtilities.formatToolTipText("Show list of imported images."));
        setButtonsEnabled(false);
        selections = new JComboBox(listOfItems);
    }

    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        buildComponentsPanel();
        selectionsPanel = UIUtilities.buildComponentPanel(selections);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(componentsPanel);
        add(selectionsPanel);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
    }

    /** Display the buttons in a JPanel. */
    private void buildComponentsPanel()
    {
        componentsPanel = new JPanel();
        componentsPanel.setLayout(new BoxLayout(componentsPanel, 
                                    BoxLayout.X_AXIS));
        componentsPanel.add(resetButton);
        componentsPanel.add(Box.createRigidArea(DataManagerUIF.HBOX));
        componentsPanel.add(selectButton);
        componentsPanel.add(Box.createRigidArea(DataManagerUIF.HBOX));
        componentsPanel.add(showImages);
        componentsPanel.setOpaque(false); //make panel transparent
    }
    
    /** Build panel with table containing the images to add. */
    private JPanel buildImagesPanel(List images)
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        //images table
        imagesTM = new ImagesTableModel(images);
        JTable table = new JTable();
        sorter = new TableSorter(imagesTM);  
        table.setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(table);
        setTableLayout(table);
        table.setBackground(DataManagerUIF.STEELBLUE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setPreferredScrollableViewportSize(DataManagerUIF.VP_DIM);
        //wrap table in a scroll pane and add it to the panel
        JScrollPane pane = new JScrollPane(table);
        p.add(pane);
        return p;
    }
    
    /** Set icons in the tableHeader. */
    private void setTableLayout(JTable table)
    {
        IconManager im = IconManager.getInstance(
                            manager.getView().getRegistry());
        TableIconRenderer iconHeaderRenderer = new TableIconRenderer();
        TableColumnModel tcm = table.getTableHeader().getColumnModel();
        TableColumn tc = tcm.getColumn(NAME);
        tc.setHeaderRenderer(iconHeaderRenderer);
        TableHeaderTextAndIcon 
        txt = new TableHeaderTextAndIcon(columnNames[NAME], 
                im.getIcon(IconManager.ORDER_BY_NAME_UP),
                im.getIcon(IconManager.ORDER_BY_NAME_DOWN), 
                "Order images by name.");
        tc.setHeaderValue(txt);
        tc = tcm.getColumn(SELECT);
        tc.setHeaderRenderer(iconHeaderRenderer); 
        txt = new TableHeaderTextAndIcon(columnNames[SELECT], 
                im.getIcon(IconManager.ORDER_BY_SELECTED_UP), 
                im.getIcon(IconManager.ORDER_BY_SELECTED_DOWN),
                "Order by selected images.");
        tc.setHeaderValue(txt);
    }

    /** 
     * A <code>3</code>-column table model to view the summary of 
     * image to add to a new dataset.
     * The first column contains the images ID and the 
     * second column the names, the third one a check box.
     * The first two cells are not editable, the third one is. 
     */
    private class ImagesTableModel
        extends AbstractTableModel
    {
        private Object[]      images;
        private Object[][]    data;

        private ImagesTableModel(List imgs)
        {
            images = imgs.toArray();
            data = new Object[images.length][2];
            for (int i = 0; i < images.length; i++) {
                data[i][0] = (ImageSummary) images[i];
                data[i][1] = new Boolean(false);
            }
        }

        public int getColumnCount() { return 2; }

        public int getRowCount() { return images.length; }

        public String getColumnName(int col) { return columnNames[col]; }
    
        public Class getColumnClass(int c)
        {
            return getValueAt(0, c).getClass();
        }

        public Object getValueAt(int row, int col) { return data[row][col]; }
    
        public boolean isCellEditable(int row, int col) {  return (col == 1); }
        
        public void setValueAt(Object value, int row, int col)
        {
            data[row][col] = value;
            ImageSummary is = (ImageSummary) sorter.getValueAt(row, NAME);
            fireTableCellUpdated(row, col);
            manager.addImage(((Boolean) value).booleanValue(), is);
        }
    }
    
}

