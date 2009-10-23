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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;

/** 
 * UI component hosting the canvas displaying the history and 
 * various controls.
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

	/** The title of the component. */
	static final String	TITLE = "History";
	
	/** Reference to the Model. */
	private ImViewerModel 	model;
	
	/** Reference to the View. */
	private ImViewerUI		view;
	
	/** Canvas displaying the history. */
	private HistoryCanvas	canvas;
	
	/** 
	 * Initializes the components. 
	 * 
	 * @param controller Reference to the Control.
	 */
	private void initComponents(ImViewerControl	controller)
	{
		canvas = new HistoryCanvas(controller);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBorder(null);
		double[][] size = {{TableLayout.FILL}, {TableLayout.FILL}};
		setLayout(new TableLayout(size));
		add(canvas, "0, 0");
	}
	
	/**
     * Creates a new instance.
     *
     * @param view 		 Reference to the Model. Mustn't be <code>null</code>.
     * @param model 	 Reference to the Model. Mustn't be <code>null</code>.
     * @param controller Reference to the Control. Mustn't be <code>null</code>.
     */
	HistoryUI(ImViewerUI view, ImViewerModel model, ImViewerControl controller)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.model = model;
		this.view = view;
		initComponents(controller);
		buildGUI();
	}
	
	/** Lays out the node. */
	void doGridLayout()
	{
		JComponent desktop = canvas.getInternalDesktop();
		desktop.removeAll();
		List nodes = model.getHistory();
		if (nodes == null || nodes.size() == 0) return;
		Iterator i = nodes.iterator();
		while (i.hasNext()) 
			desktop.add((HistoryItem) i.next());

		Rectangle r = new Rectangle();//getBounds();
		int w = r.width;
		if (w == 0) w = view.geRestoreSize().width;
		canvas.doGridLayout(w, model.getHistory());
	}
	
	/**
	 * Adds a new node to the history
	 * 
	 * @param node the node to add.
	 */
	void addHistoryItem(HistoryItem node)
	{ 
		if (node == null) return;
		JComponent desktop = canvas.getInternalDesktop();
		desktop.add(node);
		Rectangle r = getBounds();
		canvas.doGridLayout(r.width, model.getHistory());
		JScrollPane dskDecorator = canvas.getDeskDecorator();
		Rectangle bounds = node.getBounds();
		JScrollBar hbar = dskDecorator.getHorizontalScrollBar();
		hbar.setValue(hbar.getMaximum()+bounds.width);
	}
	
	/** Clears the history. */
	void clearHistory()
	{
		if (canvas != null)
			canvas.clearHistory(model.getHistory());
	}
	
	/**
	 * Returns the ideal size.
	 * 
	 * @return See above.
	 */
	Dimension getIdealSize()
	{
		Dimension d = canvas.getPreferredSize(); 
		List<HistoryItem> nodes = model.getHistory();
		if (nodes != null && nodes.size() >= 1) 
			d = nodes.get(0).getPreferredSize();
		JComponent tb = canvas.getTitleBar();
		Insets insets = canvas.getInsets();
		int h = tb.getPreferredSize().height+insets.bottom+insets.top;
		return new Dimension(getPreferredSize().width, d.height+h);
	}
	
}
