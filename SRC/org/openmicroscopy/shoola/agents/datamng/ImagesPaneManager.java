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
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

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
	private static final int		LOAD = 0, BY_NAME = 1, BY_DATE = 2;
	
	/** This UI component's view. */
	private ImagesPane 				view;
	
	/** The agent's control component. */
	private DataManagerCtrl 		agentCtrl;

	private ImagesTableModel		tableModel;
	
	private JButton					load, orderByName, orderByDate;
	
	private boolean					loaded;
	
	ImagesPaneManager(ImagesPane view, DataManagerCtrl agentCtrl)
	{
		this.view = view;
		this.agentCtrl = agentCtrl;
		loaded = false;
		initListeners();
	}

	/** 
	 * Attach a mouse adapter to the tree in the view to get notified 
	 * of mouse events on the tree.
	 */
	private void initListeners()
	{
		load = view.bar.getLoad();
		orderByName = view.bar.getOrderByName();
		orderByDate = view.bar.getOrderByDate();
		load.addActionListener(this);
		load.setActionCommand(""+LOAD);		
		orderByName.addActionListener(this);
		orderByName.setActionCommand(""+BY_NAME);
		orderByDate.addActionListener(this);
		orderByDate.setActionCommand(""+BY_DATE);
		/*
		view.tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { onClick(e); }
			public void mouseReleased(MouseEvent e) { onClick(e); }
		});
		*/
	}

	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) { 
				case LOAD:
					loadImages(); break;
				case BY_NAME:
					orderByName(); break;
				case BY_DATE:
					orderByDate(); break;
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}

	private void loadImages()
	{
		if (!loaded) {
			tableModel = new ImagesTableModel();
			view.table.setModel(tableModel);
			loaded = true;
			setButtonsEnabled(loaded);
		}
	}
	
	private void orderByName()
	{
		if (loaded) {
		}

	}
	
	private void orderByDate()
	{
		if (loaded) {
		}
	}
	
	private void setButtonsEnabled(boolean b)
	{
		orderByDate.setEnabled(b);
		orderByName.setEnabled(b);
	}
	

	/** 
	 * Handles mouse clicks within the tree component in the view.
	 * If the mouse event is the platform popup trigger event, then the context 
	 * popup menu is brought up. Otherwise, double-clicking on a image, 
	 * node brings up the corresponding property sheet dialog.
	 *
	 * @param e   The mouse event.
	 */
	private void onClick(MouseEvent e)
	{
		
	}
	
	/** 
	 * A <code>2</code>-column table model to view the summary of 
	 * the user's image
	 * The first column contains the image names, the second column 
	 * the <code>created date</code>.
	 */
	private final class ImagesTableModel 
		extends AbstractTableModel
	{
		private final String[]	columnNames = {"Name", "Date"};
		private final Object[]	images = agentCtrl.getUserImages().toArray();
		private Object[][] 		data = new Object[images.length][2];
		private ImagesTableModel() 
		{
			ImageSummary is;
			for (int i = 0; i < images.length; i++) {
				is = (ImageSummary) images[i];
				data[i][0] = is.getName();
				data[i][1] = ""; //TODO: is.getDate();
			}
		}

		public int getColumnCount() { return 2; }
    
		public int getRowCount() { return images.length; }
    
		public String getColumnName(int col) { return columnNames[col]; }

		public Class getColumnClass(int col)
		{
			return getValueAt(0, col).getClass();
		}
		
		public Object getValueAt(int row, int col) { return data[row][col]; }
		
		public boolean isCellEditable(int row, int col) {
			boolean isEditable = false;
			if (col == 0) isEditable = true;
			return isEditable;
		}

		//only name column is editable
		public void setValueAt(Object value, int row, int col)
		{
			//TODO
		} 
	}

}
