/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.RollOverNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;

/** 
 * Displays all the fields of a given well.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class WellFieldsView
	extends JPanel
{

	/** The minimum value for the magnification w/o respect of distance. */
	static final int 			MAGNIFICATION_UNSCALED_MIN = 1;
	
	/** The maximum value for the magnification w/o respect of distance. */
	static final int 			MAGNIFICATION_UNSCALED_MAX = 4;
	
	/** Indicates to lay out the fields in a row. */
	static final int			ROW_LAYOUT = 0;
	
	/** Indicates to lay out the fields in a spatial position. */
	static final int			SPATIAL_LAYOUT = 1;
	
	/** Indicates the default layout. */
	static final int			DEFAULT_LAYOUT = SPATIAL_LAYOUT;
	
	/** The width of the canvas. */
	static final int 			DEFAULT_WIDTH = 512;
	
	/** The height of the canvas. */
	static final int 			DEFAULT_HEIGHT = 512;
	
	/** The text for the selected well. */
	private static final String	DEFAULT_WELL_TEXT = "Well: ";
	
	/** The text for the selected field. */
	private static final String	DEFAULT_FIELD_TEXT = "Field #";
	
	/** The grid representing the plate. */
	//private PlateGrid 			grid;
	
	/** Reference to the model. */
	private WellsModel 			model;
	
	/** Reference to the controller. */
	private DataBrowserControl 	controller;
	
	/** Component displaying the thumbnails. */
	private WellFieldsCanvas	canvas;
	
	/** The collection of nodes to display. */
	private List<WellSampleNode> nodes;
	
	/** The type of layout of the fields. */
	private int					 layoutFields;
	
	/** The currently selected well. */
	private JLabel				 selectedNode;
	
	/** The currently selected field. */
	private JLabel				 selectedField;
	
	/** The magnification factor. */
	private double				 magnification;
	
	/** The magnification not preserving the scale. */
	private double				 magnificationUnscaled;
	
	/** The component displaying the plate grid. */
	private JXTaskPane			plateTask;
	
	/** Initializes the components. */
	private void initComponents()
	{
		magnificationUnscaled = MAGNIFICATION_UNSCALED_MIN;
		layoutFields = DEFAULT_LAYOUT;
		selectedField = new JLabel();
		WellImageSet node = model.getSelectedWell();
		selectedNode = new JLabel();
		if (node != null) {
			selectedNode.setText(DEFAULT_WELL_TEXT+node.getWellLocation());
		}
		/*
		grid = new PlateGrid(model.getRowSequenceIndex(), 
				model.getColumnSequenceIndex(), model.getValidWells(), 
				model.getRows(), model.getColumns());
		grid.addPropertyChangeListener(controller);
		
		WellImageSet node = model.getSelectedWell();
		selectedNode = new JLabel();
		if (node != null) {
			selectedNode.setText(DEFAULT_WELL_TEXT+node.getWellLocation());
			grid.selectCell(node.getRow(), node.getColumn());
		}
		*/
		canvas = new WellFieldsCanvas(this);
		canvas.addMouseListener(new MouseAdapter() {

			/**
			 * Launches the viewer if the number of click is <code>2</code>.
			 * @see MouseListener#mouseEntered(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				WellSampleNode node = canvas.getNode(e.getPoint());
				if (node != null) {
					model.setSelectedField(node);
					if (e.getClickCount() == 2)
						controller.viewDisplay(node);
				}
			}

			/**
			 * Displays the field's metadata.
			 * @see MouseListener#mouseEntered(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				//WellSampleNode node = canvas.getNode(e.getPoint());
				//if (node != null) model.setSelectedField(node);
			}

		});
		canvas.addMouseMotionListener(new MouseMotionAdapter() {

			/**
			 * Sets the node which has to be zoomed when the roll over flag
			 * is turned on. Note that the {@link ImageNode}s are the only nodes
			 * considered.
			 * @see MouseMotionListener#mouseMoved(MouseEvent)
			 */
			public void mouseMoved(MouseEvent e) {
				if (model.getBrowser().isRollOver()) {
					Point p = e.getPoint();
					WellSampleNode node = canvas.getNode(p);
					SwingUtilities.convertPointToScreen(p, canvas);
					model.getBrowser().setRollOverNode(
							new RollOverNode(node, p));
				} else {
					Point p = e.getPoint();
					WellSampleNode node = canvas.getNode(p);
					if (node != null) {
						StringBuffer buffer = new StringBuffer();
						buffer.append(DEFAULT_FIELD_TEXT+node.getIndex());
						buffer.append("\n");
						buffer.append("x="+node.getPositionX()+", " +
								"y="+node.getPositionY());
						String s = buffer.toString();
						canvas.setToolTipText(s);
						selectedField.setText(s);
					} else {
						canvas.setToolTipText("");
						selectedField.setText("");
					}
				}
			}


		});
		nodes = null;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(new LineBorder(new Color(99, 130, 191)));
		setLayout(new BorderLayout(0, 0));
		JScrollPane pane = new JScrollPane(canvas);
		pane.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		add(pane, BorderLayout.CENTER);
		/*
		JPanel p = new JPanel();
		double[][] size = {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
				TableLayout.FILL}};
		p.setLayout(new TableLayout(size));
		p.add(grid, "0, 0, 0, 2");
		p.add(selectedNode, "2, 0, LEFT, TOP");
		p.add(selectedField, "2, 1, LEFT, TOP");
		
		plateTask = EditorUtil.createTaskPane("Plate");
		plateTask.add(UIUtilities.buildComponentPanel(p));
		plateTask.setCollapsed(false);
		add(plateTask, BorderLayout.SOUTH);
		*/
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	 	Reference to the model.
	 * @param controller 	Reference to the control.
	 * @param magnification The default magnification.
	 */
	WellFieldsView(WellsModel model, DataBrowserControl controller, double
			magnification)
	{
		this.model = model;
		this.controller = controller;
		this.magnification = magnification;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Sets the index indicating how to layout the fields.
	 * 
	 * @param layoutFields The value to set.
	 */
	void setLayoutFields(int layoutFields) { this.layoutFields = layoutFields; }
	
	/**
	 * Returns the index identifying the type of layout of the fields.
	 * 
	 * @return See above
	 */
	int getLayoutFields() { return layoutFields; }
	
	/** 
	 * Returns the fields to display if any.
	 * 
	 * @return See above.
	 */
	List<WellSampleNode> getNodes() { return nodes; }
	
	/**
	 * Displays the passed fields.
	 * 
	 * @param nodes The nodes hosting the fields.
	 */
	void displayFields(List<WellSampleNode> nodes)
	{
		this.nodes = nodes;
		if (nodes != null && nodes.size() > 0) {
			WellSampleNode node = nodes.get(0);
			if (node != null) {
				selectedNode.setText(DEFAULT_WELL_TEXT+
						node.getParentWell().getWellLocation());
				selectedNode.repaint();
			}
		}
		canvas.repaint();
	}
	
	/** 
	 * Sets the magnification factor.
	 * 
	 * @param factor The value to set.
	 */
	void setMagnificationFactor(double factor)
	{
		magnification = factor;
		canvas.repaint();
	}
	
	/**
	 * Returns the magnification factor.
	 * 
	 * @return See above.
	 */
	double getMagnification() { return magnification; }
	
	/** 
	 * Sets the magnification factor.
	 * 
	 * @param factor The value to set.
	 */
	void setMagnificationUnscaled(double factor)
	{
		magnificationUnscaled = factor;
		canvas.repaint();
	}
	
	/**
	 * Returns the magnification factor.
	 * 
	 * @return See above.
	 */
	double getMagnificationUnscaled() { return magnificationUnscaled; }
	
}
