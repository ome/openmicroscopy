/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROIAssistant 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog uses to propagate ROIs
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ROIAssistant
	extends JDialog
	implements ActionListener
{	
	
	/** Action command ID to accept the current ROI assistant results.*/
	private static final int CLOSE = 0;

	/** 
	 * The table showing the ROI and allowing the user to propagate the selected
	 * ROI through time and Z-section. 
	 */	
	private 	ROIAssistantTable		table;
	
	/**
	 * The model which will define the ROI's displayed in the table.
	 */
	private 	ROIAssistantModel 		model;
	
	/** Text field showing the current type of the selected shape. */
	private 	JTextField				shapeType;
	
	/** Text field showing the x coordinate of the selected shape. */
	private 	JTextField				xCoord;
	
	/** Text field showing the y coordinate of the selected shape. */
	private 	JTextField				yCoord;
	
	/** Text field showing the width of the selected shape. */
	private 	JTextField				width;
	
	/** Text field showing the height of the selected shape. */
	private 	JTextField				height;
	
	/** Text field showing the description of the selected shape. */
	private 	JTextField 				description;
	
	/** Checkbox which is selected if the user has selected to add an ROI. */
	private 	JRadioButton 			addButton;
	
	/** Checkbox which is selected if the user has selected to remove an ROI. */
	private 	JRadioButton			removeButton;
		
	/** The scroll pane of the Table. */
	private 	JScrollPane				scrollPane;
	
	/** button closes windows. */
	private 	JButton					closeButton;
	
	/** Model for the measurement tool. */
	private 	MeasurementViewerUI		view;
		
	/** The initial shape selected when launching the ROIAssistant. */
	private 	ROIShape 				initialShape;
	
	/** The panel showing the corner z\t graphic. */
	private 	JPanel					cornerPane;
	
	/**
	 * Maps the coordinate to a cell in the table.
	 * 
	 * @param coord see above.
	 * @return see above.
	 */
	private Point mapCoordToCell(Coord3D coord)
	{
		int w = table.getColumnWidth();
		int h = table.getRowHeight();
		int x = coord.getTimePoint()*w; 
		int y = (table.getRowCount()-coord.getZSection())*h;
		return new Point(x, y);
	}
	
	/** Create the UI for the Assistant. */
	private void buildUI()
	{
		JPanel panel = new JPanel();
		JPanel infoPanel = createInfoPanel();
		JPanel shapePanel = createShapePanel();
		createAcceptButton();
		createCornerPane();
		scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
		scrollPane.setHorizontalScrollBar(
				scrollPane.createHorizontalScrollBar());
		
		JPanel scrollPanel = new JPanel();
		scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.X_AXIS));
		scrollPanel.add(Box.createHorizontalStrut(10));
		scrollPanel.add(scrollPane);
		scrollPanel.add(Box.createHorizontalStrut(10));
		scrollPanel.add(createActionPanel());
		scrollPanel.add(Box.createHorizontalStrut(10));
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createVerticalStrut(10));
		panel.add(scrollPanel);
		panel.add(Box.createVerticalStrut(10));
		panel.add(shapePanel);
		panel.add(Box.createVerticalStrut(10));
		panel.add(closeButton);
		panel.add(Box.createVerticalStrut(10));
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(infoPanel, BorderLayout.NORTH);
		c.add(panel, BorderLayout.CENTER);
		setSize(550,530);
	}
	
	/**
	 * Create the table and model.
	 *  
	 * @param numRow The number of z sections in the image. 
	 * @param numCol The number of time points in the image. 
	 * @param currentPlane the current plane of the image.
	 * @param selectedROI The ROI which will be propagated.
	 */
	private void createTable(int numRow, int numCol, Coord3D currentPlane, 
							ROI selectedROI)
	{
		model = new ROIAssistantModel(numRow, numCol, currentPlane, 
									selectedROI);
		table = new ROIAssistantTable(model);

		table.addMouseListener(new java.awt.event.MouseAdapter() 
		{
			public void mousePressed(java.awt.event.MouseEvent e) 
			{
				int col = table.getSelectedColumn();
				int row = table.getSelectedRow();
				if(row < 0 || row >= table.getRowCount() || 
							col < 0 || col > table.getColumnCount())
					return;
				ROIShape shape = table.getShapeAt(row, col);
				if (shape != null)
				{
					initialShape = shape;
					shapeType.setText(shape.getFigure().getType());
					description.setText(
							(String) shape.getFigure().getAttribute(
									MeasurementAttributes.TEXT));
					xCoord.setText(shape.getFigure().getStartPoint().getX()+"");
					yCoord.setText(shape.getFigure().getStartPoint().getY()+"");
					width.setText(Math.abs(
							shape.getFigure().getEndPoint().getX()-
							shape.getFigure().getStartPoint().getX())+"");
					height.setText(Math.abs(
							shape.getFigure().getEndPoint().getY()-
							shape.getFigure().getStartPoint().getY())+"");
				}

			}

			public void mouseReleased(java.awt.event.MouseEvent e) 
			{
				if (initialShape == null) return;
				int[] col = table.getSelectedColumns();
				int[] row = table.getSelectedRows();
				for (int i = 0 ; i < row.length ; i++)
					row[i] = (table.getRowCount()-row[i])-1;
				int mincol = col[0];
				int maxcol = col[0];
				int minrow = row[0];
				int maxrow = row[0];

				for (int i = 0 ; i < col.length; i++)
				{
					mincol = Math.min(mincol, col[i]);
					maxcol = Math.max(maxcol, col[i]);
				}

				for (int i = 0 ; i < row.length; i++)
				{
					minrow = Math.min(minrow, row[i]);
					maxrow = Math.max(maxrow, row[i]);
				}

				if(minrow < 0 || maxrow > table.getRowCount() || 
						mincol < 0 || maxcol > table.getColumnCount())
					return;

				int boundrow;
				int boundcol;
				if (maxcol != initialShape.getT()) boundcol = maxcol;
				else boundcol = mincol;
				if (maxrow != initialShape.getZ()) boundrow = maxrow;
				else boundrow = minrow;

				if (addButton.isSelected())
					view.propagateShape(initialShape, boundcol, boundrow);
				if (removeButton.isSelected())
					view.deleteShape(initialShape, boundcol, boundrow);
				initialShape=null;
				table.repaint();
			}
		});
   
	}
	
	/**
	 * Create the corner pane. 
	 */
	private void createCornerPane()
	{
		cornerPane = new JPanel();
		cornerPane.setLayout(new BorderLayout());
		JLabel icon = new JLabel();
		icon.setIcon(IconManager.getInstance().getIcon(IconManager.CORNERICON));
		cornerPane.add(icon, BorderLayout.CENTER);
	}
	
	/**
	 * Creates the action panel is the panel which holds the buttons to choose 
	 * the action to perform on the ROI. 
	 * 
	 * @return See above.
	 */
	private JPanel createActionPanel()
	{
		JPanel actionPanel = new JPanel();
		addButton = new JRadioButton("Add ROI");
		removeButton = new JRadioButton("Remove ROI");
		ButtonGroup group = new ButtonGroup();
		addButton.setSelected(true);
		group.add(addButton);
		group.add(removeButton);
		actionPanel.setLayout(new BorderLayout());
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		subPanel.add(addButton);
		subPanel.add(removeButton);
		actionPanel.add(subPanel, BorderLayout.NORTH);
		return actionPanel;
	}
	
	/**
	 * Creates the info panel at the top the the dialog, 
	 * showing a little text about the ROI Assistant. 
	 * 
	 * @return See above.
	 */
	private JPanel createInfoPanel()
	{
		JPanel infoPanel = new TitlePanel("ROI Assistant", 
				"The ROI Assistant allows you to create an ROI " +
				"which extends \n" +
				"through time and z-sections.", 
				IconManager.getInstance().getIcon(IconManager.WIZARD_48));
		return infoPanel;
	}
	
	/** 
	 * Creates the shape panel which shows the parameters of the initial shape. 
	 * 
	 * @return See above. 
	 */
	private JPanel createShapePanel()
	{
		JPanel shapePanel = new JPanel();
		shapeType = new JTextField();
		description = new JTextField();
		xCoord = new JTextField();
		yCoord = new JTextField();
		width = new JTextField();
		height = new JTextField();
		JLabel shapeTypeLabel = new JLabel("Shape Type ");
		JLabel xCoordLabel = new JLabel("X Coord");
		JLabel yCoordLabel = new JLabel("Y Coord");
		JLabel widthLabel = new JLabel("Width");
		JLabel heightLabel = new JLabel("Height");
		JLabel descriptionLabel = new JLabel("Description");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(createLabelText(shapeTypeLabel, shapeType));
		panel.add(Box.createVerticalStrut(5));
		panel.add(createLabelText(descriptionLabel, description));
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
		panel2.add(createLabelText(xCoordLabel, xCoord));
		panel2.add(Box.createVerticalStrut(5));
		panel2.add(createLabelText(yCoordLabel, yCoord));
		
		JPanel panel3 = new JPanel();
		panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
		panel3.add(createLabelText(widthLabel, width));
		panel3.add(Box.createVerticalStrut(5));
		panel3.add(createLabelText(heightLabel, height));
		
		shapePanel.setLayout(new BoxLayout(shapePanel, BoxLayout.X_AXIS));
		shapePanel.add(panel);
		shapePanel.add(Box.createHorizontalStrut(10));
		shapePanel.add(panel2);
		shapePanel.add(Box.createHorizontalStrut(10));
		shapePanel.add(panel3);
		
		return shapePanel;
	}
	
	/** 
	 * Creates a panel with label and textfield.
	 * 
	 * @param l label		The label to layout.
	 * @param t textfield	The field to layout.
	 * @return see above.
	 */
	private JPanel createLabelText(JLabel l, JTextField t)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(l);
		panel.add(t);
		UIUtilities.setDefaultSize(l, new Dimension(80,22));
		UIUtilities.setDefaultSize(t, new Dimension(80,22));
		return panel;
	}
	
	/** Creates the accept button to close on click. */
	private void createAcceptButton()
	{
		closeButton = new JButton("Close");
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
	}

	/** Closes the ROIAssistant window. */
	private void closeAssistant()
	{
		setVisible(false);
		this.dispose();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param numRow		The number of z-sections in the image. 
	 * @param numCol 		The numer of time points in the image. 
	 * @param currentPlane 	The current plane of the image.
	 * @param selectedROI 	The ROI which will be propagated.
	 * @param view a reference to the view. 
	 */
	ROIAssistant(int numRow, int numCol, Coord3D currentPlane, 
						ROI selectedROI, MeasurementViewerUI view)
	{
		super(view);
		this.view = view;
		initialShape = null;
		//this.setAlwaysOnTop(true);
		this.setModal(true);
		createTable(numRow, numCol,currentPlane, selectedROI);
		buildUI();

		JList rowHeader = new JList(new HeaderListModel(table.getRowCount()));
		//table.setDefaultRenderer(JComponent.class, new TableComponentCellRenderer());

		rowHeader.setFixedCellHeight(table.getRowHeight());
		rowHeader.setFixedCellWidth(table.getColumnWidth());
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));
        scrollPane.setRowHeaderView(rowHeader);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerPane);
        JViewport viewPort = scrollPane.getViewport();
        Point point = mapCoordToCell(currentPlane);
		int x = (int) Math.max((point.getX()-6*table.getColumnWidth()), 0);
		int y = (int) Math.max((point.getY()-6*table.getColumnWidth()), 0);
		
		viewPort.setViewPosition(new Point(x, y));
		
	}

	/**
	 * Reacts to event fired by the various controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{
		int id = -1;
		try
		{
			id = Integer.parseInt(evt.getActionCommand());
			switch (id)
			{
				case CLOSE:
					closeAssistant();
					break;
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/**
	 * Class to define the row header data, this is the Z section 
	 * count in the ROIAssistant.
	 */
	class HeaderListModel
		extends AbstractListModel
	{

		/** The header values. */
		private String[] headers;
    
		/**
		 * Instantiate the header values with a count from n to 1. 
		 * @param n see above.
		 */
		public HeaderListModel(int n)
		{
			headers = new String[n];
			for (int i = 0; i<n; i++) 
				headers[i] = ""+(n-i);
		}
    
		/** 
		 * Get the size of the header. 
		 * @return see above.
		 */
		public int getSize(){ return headers.length; }
    
		/** 
		 * Get the header object at index.
		 * @param index see above. 
		 * @return see above.
		 */
		public Object getElementAt(int index) { return headers[index]; }
    
	}

	/**
	 * The renderer for the row header. 
	 */
	class RowHeaderRenderer
    	extends JLabel 
    	implements ListCellRenderer
    {
    
		/** 
		 * Instantiate row renderer for table.
		 * @param table see above.
		 */
		public RowHeaderRenderer(JTable table)
		{
			if (table != null) 
			{
				JTableHeader header = table.getTableHeader();
				setOpaque(true);
				setBorder(UIManager.getBorder("TableHeader.cellBorder"));
				setHorizontalAlignment(CENTER);
				setHorizontalTextPosition(CENTER);
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
			}
		}
    
		/**
		 * Return the component for the renderer.
		 * @param list the list containing the headers render context.
		 * @param value the value to be rendered.
		 * @param index the index of the rendered object. 
		 * @param isSelected is the  current header selected.
		 * @param cellHasFocus has the cell focus.
		 * @return the render component. 
		 */
		public Component getListCellRendererComponent(JList list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus)
		{
			setText((value == null) ? "" : value.toString());
			return this;
		}
    
    }
}


