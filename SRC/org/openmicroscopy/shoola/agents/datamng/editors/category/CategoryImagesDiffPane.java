/*
 * org.openmicroscopy.shoola.agents.datamng.editors.DatasetImagesDiffPane
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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import org.openmicroscopy.shoola.util.ui.table.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.table.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class CategoryImagesDiffPane
	extends JDialog
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
    
    /** List of images we wish to display. */
    JComboBox                               selections;
    
	/** Action id. */
	private static final int				NAME = 0, SELECT = 1;
			
	protected static final String[]			columnNames;
	
	static {
		columnNames  = new String[2];
		columnNames[NAME] = "Name";
		columnNames[SELECT] = "Select";
	}
	
	JButton							         selectButton, cancelButton, 
											 saveButton, showImages, filter;
											
	private ImagesTableModel 				imagesTM;
	
	private TableSorter 					sorter;
	
	/** Reference to the control of the main widget. */
	private CategoryEditorManager 			control;
	
	private JPanel							contents, componentsPanel, 
                                            selectionsPanel;
	
	private IconManager						im;
	
	private CategoryImagesDiffPaneManager	manager;
	
	CategoryImagesDiffPane(CategoryEditorManager control)
	{
		super(control.getAgentControl().getReferenceFrame(), 
                "List of existing images", true);
		this.control = control;
		im = IconManager.getInstance(control.getView().getRegistry());
		initComponents();
		manager = new CategoryImagesDiffPaneManager(this, control);
		buildGUI();
	}
	
	/** 
	 * Return the {@link CategoryImagesDiffPaneManager manager} of the widget.
	 */
	CategoryImagesDiffPaneManager getManager() { return manager; }
	
	JPanel getContents() { return contents; }
	
	/** Select or not all images. */
	void setSelection(Object val)
	{
		int countCol = imagesTM.getColumnCount()-1;
		for (int i = 0; i < imagesTM.getRowCount(); i++)
			imagesTM.setValueAt(val, i, countCol);
	}
	
    /** Remove the table with list of images. */
    void removeDisplay()
    {
        contents.removeAll();
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(componentsPanel);
        p.add(selectionsPanel);
        contents.add(p);
        pack();
    }
    
    /** Display the specified list of images. */
    void showImages(List images)
    {
        contents.removeAll();
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(componentsPanel);
        p.add(selectionsPanel);
        contents.add(p);
        if (images != null && images.size() != 0) 
           contents.add(buildImagesPanel(images)); 
        pack();
    }
    
	/** initializes the controls. */
	private void initComponents()
	{
		//remove button
		selectButton = new JButton("Select All");
		selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		selectButton.setToolTipText(
			UIUtilities.formatToolTipText("Select all the images."));
		//cancel button
		cancelButton = new JButton("Reset");
		cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cancelButton.setToolTipText(
			UIUtilities.formatToolTipText("Cancel selection."));
		//save button
		saveButton = new JButton("Add");
		saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		saveButton.setToolTipText(
			UIUtilities.formatToolTipText("Add the images to the category."));
		
        showImages = new JButton("Available Images");
        showImages.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showImages.setToolTipText(
            UIUtilities.formatToolTipText("Show available images."));
        //Filters images
        IconManager im = IconManager.getInstance(
                control.getAgentControl().getRegistry());
        filter = new JButton(im.getIcon(IconManager.FILTER));
        filter.setToolTipText(
            UIUtilities.formatToolTipText("Filters..."));
        selections = new JComboBox(listOfItems);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
        String s = " Select images to add to"
                    +control.getCategoryData().getName()+".";
        TitlePanel tp = new TitlePanel("Add images", s, 
                            im.getIcon(IconManager.IMAGE_BIG));
        buildComponentsPanel();
        selectionsPanel = UIUtilities.buildComponentPanel(selections);
        contents = new JPanel();
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(componentsPanel);
        p.add(selectionsPanel);
        contents.add(p);
        getContentPane().add(tp, BorderLayout.NORTH);
        getContentPane().add(contents, BorderLayout.CENTER);
        setSize(DataManagerUIF.ADD_WIN_WIDTH+100, 
                DataManagerUIF.ADD_WIN_HEIGHT);
	}
    
    /** Display the buttons in a JPanel. */
    private void buildComponentsPanel()
    {
        componentsPanel = new JPanel();
        componentsPanel.setLayout(new BoxLayout(componentsPanel, 
                                    BoxLayout.X_AXIS));
        componentsPanel.add(cancelButton);
        componentsPanel.add(Box.createRigidArea(DataManagerUIF.HBOX));
        componentsPanel.add(selectButton);
        componentsPanel.add(Box.createRigidArea(DataManagerUIF.HBOX));
        componentsPanel.add(saveButton);
        componentsPanel.add(Box.createRigidArea(DataManagerUIF.HBOX));
        componentsPanel.add(showImages);
        componentsPanel.add(Box.createRigidArea(DataManagerUIF.HBOX));
        componentsPanel.add(filter);
        componentsPanel.setOpaque(false); //make panel transparent
    }
	
	/** Build panel with table. */
	JPanel buildImagesPanel(List images)
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		//datasets table
		imagesTM = new ImagesTableModel(images);
		JTable t = new JTable();
		sorter = new TableSorter(imagesTM);  
		t.setModel(sorter);
		sorter.addMouseListenerToHeaderInTable(t);
		setTableLayout(t);
		
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setPreferredScrollableViewportSize(DataManagerUIF.EXTENDED_VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		p.add(new JScrollPane(t));
		return p;
	}

	/** Set icons in the tableHeader. */
	private void setTableLayout(JTable table)
	{
		IconManager im = IconManager.getInstance(
							control.getView().getRegistry());
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
	 * datasets contained in the project.
	 * The first column contains the datasets ID and the 
	 * second column the names. Cells are not editable. 
	 */
	private class ImagesTableModel
		extends AbstractTableModel
	{
		private final String[]    columnNames = {"Name", "Add"};
		private Object[]          images;
		private Object[][]        data;
		
		private ImagesTableModel(List imagesDiff)
		{
            images = imagesDiff.toArray();
            data = new Object[images.length][2];
			for (int i = 0; i < images.length; i++) {
				data[i][0] = (ImageSummary) images[i];
				data[i][1] = Boolean.FALSE;
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

		public boolean isCellEditable(int row, int col) { return (col == 1); }
		
		public void setValueAt(Object value, int row, int col)
		{
			data[row][col] = value;
			fireTableCellUpdated(row, col);
			ImageSummary is = (ImageSummary) sorter.getValueAt(row, NAME);
			manager.addImage(((Boolean) value).booleanValue(), is);
		}
	}
	
}
