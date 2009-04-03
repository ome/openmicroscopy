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
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
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
	static final String			TITLE = "History";
	
	/** The Description of the {@link #clearButton}. */
	private static final String CLEAR_DESCRIPTION = "Clear the history.";
	
	/** Reference to the Model. */
	private ImViewerModel 	model;
	
	/** Reference to the View. */
	private ImViewerUI		view;
	
	/** Button to clear the history. */
	private JButton			clearButton;
	
	/** Canvas displaying the history. */
	private HistoryCanvas	canvas;
	
	/** UI component hosting the controls.*/
	private JPanel			toolBar;
	
	/** Initializes the components. */
	private void initComponents()
	{
		canvas = new HistoryCanvas(model);
		//pane = new JScrollPane(canvas);
		IconManager icons = IconManager.getInstance();
		clearButton = new JButton(icons.getIcon(IconManager.HISTORY_CLEAR));
		clearButton.setToolTipText(CLEAR_DESCRIPTION);
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { clearHistory(); }
		});
		toolBar = buildControls();
	}
	
	/**
	 * Builds and lays out the components hosting the various controls
	 * 
	 * @return See above.
	 */
	private JPanel buildControls()
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBorder(null);
		p.add(clearButton);
		return p;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		setBorder(null);
		//add(toolBar, BorderLayout.NORTH);
		add(canvas, BorderLayout.CENTER);
	}
	
	/**
     * Creates a new instance.
     *
     * @param view Reference to the Model. Mustn't be <code>null</code>.
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	HistoryUI(ImViewerUI view, ImViewerModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.model = model;
		this.view = view;
		initComponents();
		buildGUI();
	}
	
	/** Clears the history. */
	void clearHistory()
	{
		model.clearHistory();
		JComponent desktop = canvas.getInternalDesktop();
		desktop.removeAll();
		List nodes = model.getHistory();
		Iterator i = nodes.iterator();
		while (i.hasNext()) 
			desktop.add((HistoryItem) i.next());

		Rectangle r = getBounds();
		Rectangle bounds = canvas.getContentsBounds();
		Insets insets = canvas.getInsets();
		int w = r.width-insets.left-insets.right-4;
		if (w < 0) return;
        Dimension d = new Dimension(w, bounds.height);//bounds.getSize();
        desktop.setSize(d);
        desktop.setPreferredSize(d);
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

		Rectangle r = getBounds();
		int w = r.width;
		if (w == 0) w = view.geRestoreSize().width;
		canvas.doGridLayout(w);
		//canvas.repaint();
		//Rectangle bounds = canvas.getContentsBounds();
        //Dimension d = new Dimension(r.width, bounds.height);//bounds.getSize();
        //desktop.setSize(d);
        //desktop.setPreferredSize(d);
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
		canvas.doGridLayout(r.width);
		JScrollPane dskDecorator = canvas.getDeskDecorator();
		Rectangle bounds = node.getBounds();
		JScrollBar hbar = dskDecorator.getHorizontalScrollBar();
		hbar.setValue(hbar.getMaximum()+bounds.width);
	}
	
	/** 
	 * Returns the ideal size of the component.
	 * 
	 * @return See above.
	 */
	Dimension getIdealSize()
	{
		Dimension d = getSize();
		int height = 0;//toolBar.getPreferredSize().height;
		height += canvas.getTitleBar().getPreferredSize().height;
		//height += canvas.getInternalDesktop().getPreferredSize().height;
		height += 4*ImViewer.MINIMUM_SIZE/3;
		return new Dimension(d.width, height);
	}

}
