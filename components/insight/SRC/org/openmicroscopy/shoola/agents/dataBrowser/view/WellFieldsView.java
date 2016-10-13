/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import omero.gateway.model.WellSampleData;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.RollOverNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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

	/** The currently selected well. */
	private JLabel				 selectedNode;
	
	/** The currently selected field. */
	private JLabel				 selectedField;
	
	/** The magnification factor. */
	private double				 magnification;
	
	/** The magnification not preserving the scale. */
	private double				 magnificationUnscaled;
	
	/** Flag to indicate if thumbnails are loading */
	private boolean loading = false;
	
	/** The scroll pane */
	private JScrollPane pane;
	
	/** Initializes the components. */
	private void initComponents()
	{
		magnificationUnscaled = MAGNIFICATION_UNSCALED_MIN;
		selectedField = new JLabel();
		WellImageSet node = model.getSelectedWell();
		selectedNode = new JLabel();
		if (node != null) {
			selectedNode.setText(DEFAULT_WELL_TEXT+node.getWellLocation());
		}		
		nodes = null;
		
		canvas = new RowFieldCanvas(this);

        canvas.addMouseListener(new MouseAdapter() {

            /**
             * Launches the viewer if the number of click is <code>2</code>.
             * 
             * @see MouseListener#mouseEntered(MouseEvent)
             */
            public void mouseReleased(MouseEvent e) {
                WellSampleNode node = canvas.getNode(e.getPoint());
                if (node != null) {
                    model.setSelectedField(node);
                    if (e.getClickCount() == 2)
                        controller.viewDisplay(node);
                    canvas.refreshUI();
                }
            }

            public void mousePressed(MouseEvent e) {
            }

        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {

            /**
             * Sets the node which has to be zoomed when the roll over flag is
             * turned on. Note that the {@link ImageNode}s are the only nodes
             * considered.
             * 
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
                        buffer.append(DEFAULT_FIELD_TEXT + (node.getIndex()+1));
                        buffer.append("\n");
                        buffer.append("x=" + node.getPositionX() + ", " + "y="
                                + node.getPositionY());
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
	}
	
    /**
     * Checks if the provided field is the currently selected field
     * 
     * @param n
     *            The field
     * @return See above.
     */
    boolean isSelected(WellSampleNode n) {
        return n.getIndex() == model.getSelectedFieldIndex();
    }
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
	    removeAll();
		setBorder(new LineBorder(new Color(99, 130, 191)));
		setLayout(new BorderLayout(0, 0));
		setBackground(UIUtilities.BACKGROUND);
		pane = new JScrollPane(canvas);
		add(pane, BorderLayout.CENTER);
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
     * Returns the fields to display if any.
     * 
     * @return See above.
     */
    List<WellSampleNode> getNodes() {
        return nodes;
    }
	
    /**
     * Load the thumbnails for the selected wells
     * 
     * @param wells
     *            The selected wells
     */
    void loadFields(List<WellImageSet> wells) {
        Dimension thumbDim = magnification > 0 ? new Dimension(
                (int) (Factory.THUMB_DEFAULT_WIDTH * magnification),
                (int) (Factory.THUMB_DEFAULT_HEIGHT * magnification)) : null;

        if (wells == null || wells.isEmpty()) {
            canvas.clear(Collections.emptyList(), -1, thumbDim);
        }

        // sort
        Collections.sort(wells, new Comparator<WellImageSet>() {
            @Override
            public int compare(WellImageSet o1, WellImageSet o2) {
                if (o1.getRow() > o2.getRow())
                    return 1;
                else if (o1.getRow() < o2.getRow())
                    return -1;
                else {
                    if (o1.getColumn() > o2.getColumn())
                        return 1;
                    else if (o1.getColumn() < o2.getColumn())
                        return -1;
                }
                return 0;
            }
        });

        if (loading) {
            boolean selectionChanged = false;
            Set<Long> ids = new HashSet<Long>();
            for (WellImageSet well : wells) {
                for (WellSampleNode n : well.getWellSamples()) {
                    WellSampleData d = (WellSampleData) n.getHierarchyObject();
                    ids.add(d.getImage().getId());
                }
            }

            for (WellSampleNode n : this.nodes) {
                WellSampleData d = (WellSampleData) n.getHierarchyObject();
                if (!ids.contains(d.getImage().getId())) {
                    selectionChanged = true;
                    break;
                }
                ids.remove(d.getImage().getId());
            }

            selectionChanged = !ids.isEmpty();

            if (!selectionChanged)
                return;
        }

        int nFields = 0;
        nodes = new ArrayList<WellSampleNode>();
        List<String> titles = new ArrayList<String>();
        for (WellImageSet well : wells) {
            nodes.addAll(well.getWellSamples());
            nFields = Math.max(nFields, well.getWellSamples().size());
            titles.add(well.getTitle());
        }

        canvas.clear(titles, nFields, thumbDim);

        HashSet<Point> toLoad = new HashSet<Point>();
        for (WellSampleNode node : nodes) {
            if (!node.getThumbnail().isThumbnailLoaded()) {
                Point p = new Point(node.getRow(), node.getColumn());
                toLoad.add(p);
            }
        }

        if (!toLoad.isEmpty()) {
            loading = true;
            ArrayList<Point> tmp = new ArrayList<Point>(toLoad.size());
            tmp.addAll(toLoad);
            model.loadFields(tmp);
            return;
        }
    }
	
    /**
     * Update the thumbnail for a particular field
     * 
     * @param node
     *            The field
     * @param complete
     *            Flag to indicate that all fields have been loaded
     */
    void updateFieldThumb(WellSampleNode node, boolean complete) {
        loading = !complete;
        canvas.updateFieldThumb(node);
    }
	
	/** 
	 * Sets the magnification factor.
	 * 
	 * @param factor The value to set.
	 */
	void setMagnificationFactor(double factor)
	{
		magnification = factor;
		canvas.refreshUI();
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
		canvas.refreshUI();
	}
	
	/**
	 * Returns the magnification factor.
	 * 
	 * @return See above.
	 */
	double getMagnificationUnscaled() { return magnificationUnscaled; }
	
}
