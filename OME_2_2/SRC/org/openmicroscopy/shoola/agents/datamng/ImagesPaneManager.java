/*
 * org.openmicroscopy.shoola.agents.datamng.ExplorerImagesPaneManager
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

package org.openmicroscopy.shoola.agents.datamng;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.util.ui.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.TableSorter;

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
class ImagesPaneManager
	implements ActionListener
{

	/** Action id. */
	private static final int				NAME = 0, DATE = 1, LOAD = 2;
			
	protected static final String[]			columnNames;
	
	static {
		columnNames  = new String[2];
		columnNames[NAME] = "Name";
		columnNames[DATE] = "Date";
	}

	/** This UI component's view. */
	private ImagesPane 						view;
	
	/** The agent's control component. */
	private DataManagerCtrl 				agentCtrl;

	private ImagesTableModel				tableModel;
	
	private TableSorter 					sorter;
	
	private JButton							load;
	
	private boolean							loaded;

	ImagesPaneManager(ImagesPane view, DataManagerCtrl agentCtrl)
	{
		this.view = view;
		this.agentCtrl = agentCtrl;
		loaded = false;
		initListeners();
	}

	/** update the view when an image's name has been modified. */
	void updateImageInTable(ImageSummary is)
	{
		if (loaded) {
			int rows = sorter.getRowCount();
			ImageSummary summary;
			for (int i = 0; i < rows; i++) {
				summary = (ImageSummary) sorter.getValueAt(i, NAME);
				if (summary.getID() == is.getID()) {
					tableModel.setValueAt(is, i);
					break;
				}		
			}
		}
	}
	
	/** 
	 * Attach a mouse adapter to the tree in the view to get notified 
	 * of mouse events on the tree.
	 */
	private void initListeners()
	{
		load = view.bar.getLoad();
		load.addActionListener(this);
		load.setActionCommand(""+LOAD);		
		view.table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { onClick(e); }
			public void mouseReleased(MouseEvent e) { onClick(e); }
		});
		
	}

	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) { 
				case LOAD:
					loadImages(); break;
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}

	/** Load all the images. */
	private void loadImages()
	{
		if (!loaded) {
			JTable table = view.table;
			tableModel = new ImagesTableModel();
			sorter = new TableSorter(tableModel);  
			table.setModel(sorter);
			sorter.addMouseListenerToHeaderInTable(table);
			sorter.sortByColumn(NAME);
			setTableLayout(table);
			loaded = true;
		}
	}
	
	/** Set icons in the tableHeader. */
	private void setTableLayout(JTable table)
	{
		IconManager im = IconManager.getInstance(agentCtrl.getRegistry());
		TableIconRenderer iconHeaderRenderer = new TableIconRenderer();
		TableColumnModel tcm = table.getTableHeader().getColumnModel();
		TableColumn tc = tcm.getColumn(NAME);
		tc.setHeaderRenderer(iconHeaderRenderer);
		TableHeaderTextAndIcon 
		txt = new TableHeaderTextAndIcon(columnNames[NAME], 
				im.getIcon(IconManager.ORDER_BY_NAME_UP), 
				im.getIcon(IconManager.ORDER_BY_NAME_DOWN), "Order by name.");
		tc.setHeaderValue(txt);
		tc = tcm.getColumn(DATE);
		tc.setHeaderRenderer(iconHeaderRenderer); 
		txt = new TableHeaderTextAndIcon(columnNames[DATE], 
				im.getIcon(IconManager.ORDER_BY_DATE_UP), 
				im.getIcon(IconManager.ORDER_BY_DATE_DOWN), "Order by date.");
		tc.setHeaderValue(txt);
	}
	
	/** 
	 * Handles mouse clicks within the tree component in the view.
	 * If the mouse event is the platform popup trigger event, then the context 
	 * popup menu is brought up. Otherwise, double-clicking on a project, 
	 * dataset node brings up the corresponding property sheet dialog.
	 *
	 * @param e   The mouse event.
	 */
	private void onClick(MouseEvent e)
	{
		int selRow = view.table.getSelectedRow();
		if (selRow != -1) {
			if (e.isPopupTrigger()) {
				ImageSummary 
					target = (ImageSummary) sorter.getValueAt(selRow, NAME);
				DataManagerUIF presentation = 
						agentCtrl.getAbstraction().getPresentation();
				TreePopupMenu popup = presentation.getPopupMenu();
				popup.setTarget(target);  
				popup.show(view.table, e.getX(), e.getY());
			} 
		}	
	}

	/** 
	 * A <code>2</code>-column table model to view the summary of 
	 * the user's image.
	 * The first column contains the image names, the second column 
	 * the <code>created date</code>.
	 */
	private final class ImagesTableModel 
		extends AbstractTableModel
	{
		private final Object[]	images = agentCtrl.getUserImages().toArray();
		private Object[][] 		data = new Object[images.length][2];
		private ImagesTableModel() 
		{
			ImageSummary is;
			for (int i = 0; i < images.length; i++) {
				is = (ImageSummary) images[i];
				data[i][NAME] = is;
				data[i][DATE] = is.getDate(); 
			}
		}

		public int getColumnCount() { return 2; }
    
		public int getRowCount() { return images.length; }
    
		public String getColumnName(int col) { return columnNames[col]; }

		public Class getColumnClass(int col)
		{
			return getValueAt(0, col).getClass();
			//return JTextField.class;
		}
		
		public Object getValueAt(int row, int col) { return data[row][col]; }
		
		public boolean isCellEditable(int row, int col) { return (col == 0); }

		//only name column is editable
		public void setValueAt(Object value, int row, int col)
		{
			if (col == NAME) {
				ImageSummary is = (ImageSummary) sorter.getValueAt(row, col);
				is.setName(((ImageSummary) value).getName());
				agentCtrl.updateImage(is);
			}
		} 
		
		/** invoke when the view is updated. */
		public void setValueAt(ImageSummary is, int row)
		{
			ImageSummary summary = (ImageSummary) sorter.getValueAt(row, NAME);
			summary.setName(is.getName());
		} 
	}
	
}
