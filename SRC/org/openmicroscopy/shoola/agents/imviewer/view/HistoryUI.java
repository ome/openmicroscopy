/*
 * org.openmicroscopy.shoola.agents.imviewer.view.HistoryUI
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
package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class HistoryUI
	extends JPanel
{

	/** Reference to the Model. */
	private ImViewerModel model;
	
	/** The component hosting the canvas. */
	private JScrollPane 	pane;
	
	/** Slider used to control the magnification of the image. */
	private OneKnobSlider	zoomSlider;
	
	/** Button to clear the history. */
	private JButton			clearButton;
	
	/** Canvas displaying the history. */
	private HistoryCanvas	canvas;
	
	/** Clear the history. */
	private void clearHistory()
	{
		model.clearHistory();
		JComponent desktop = canvas.getInternalDesktop();
		desktop.removeAll();
		validate();
		repaint();
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		canvas = new HistoryCanvas(model);
		zoomSlider = new OneKnobSlider();
		pane = new JScrollPane(canvas);
		IconManager icons = IconManager.getInstance();
		clearButton = new JButton(icons.getIcon(IconManager.CLEAR));
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearHistory();
			}
		});
	}
	
	/**
	 * Builds and lays out the components hosting the various controls
	 * 
	 * @return See above.
	 */
	private JPanel buildControls()
	{
		JPanel p = new JPanel();
		p.add(clearButton);
		//p.add(UIUtilities.buildComponentPanelRight(zoomSlider));
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(buildControls(), BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
	}
	
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	HistoryUI(ImViewerModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		initComponents();
		buildGUI();
	}
	
	void doGridLayout()
	{
		JComponent desktop = canvas.getInternalDesktop();
		desktop.removeAll();
		List nodes = model.getHistory();
		if (nodes == null || nodes.size() == 0) return;
		Iterator i = nodes.iterator();
		while (i.hasNext()) 
			desktop.add((HistoryItem) i.next());

		Rectangle r = getBounds();
		canvas.doGridLayout(r.width);
		Rectangle bounds = canvas.getContentsBounds();
        Dimension d = new Dimension(r.width, bounds.height);//bounds.getSize();
        desktop.setSize(d);
        desktop.setPreferredSize(d);
	}
	
	/**
	 * Adds a new node to the history
	 * @param node
	 */
	void addHistoryItem(HistoryItem node)
	{ 
		if (node == null) return;
		JComponent desktop = canvas.getInternalDesktop();
		desktop.add(node);
		Rectangle r = getBounds();
		canvas.doGridLayout(r.width);
		Rectangle bounds = canvas.getContentsBounds();
        Dimension d = new Dimension(r.width, bounds.height);//bounds.getSize();
        desktop.setSize(d);
        desktop.setPreferredSize(d);
	}
	
	/**
	 * Overridden to reorganise the display of the nodes.
	 * @see JComponent#setBounds(Rectangle)
	 */
	public void setBounds(Rectangle r)
	{
		setBounds(r.x, r.y, r.width, r.height);
	}
	
	/**
	 * Overridden to reorganise the display of the nodes.
	 * @see JComponent#setBounds(int, int, int, int)
	 */
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		canvas.doGridLayout(width); 
	}
	
}
