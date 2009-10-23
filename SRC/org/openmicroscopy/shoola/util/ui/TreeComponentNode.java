/*
 * org.openmicroscopy.shoola.util.ui.TreeComponentNode
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;



//Java imports
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

//Third-party libraries
import info.clearthought.layout.TableLayout; 

//Application-internal dependencies

/** 
 * Component displaying a tree like arrow and a UI component:
 * one when the node is collapsed and a second one when the node is
 * expanded.
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
class TreeComponentNode 
	extends JPanel
{

	/** Bound property indicating the displayed component. */
	static final String EXPANDED_NODE_PROPERTY = "expandedNode";
	
	/** The label hosting the selected icon. */
	private JLabel 			iconLabel;
	
	/** The {@link #elapse} component. */
	private JComponent		elapse;
	
	/** The {@link #collapse} component. */
	private JComponent		collapse;
	
	/** Flag indicating the component selected. */
	private boolean 		expanded;
	
	/** The layout manager. */
	private TableLayout 	layout;
	
	/** Listener used to expand/collapse the node. */
	private MouseAdapter	adapter;
	
	/** Updates the display. */
	void updateDisplay()
	{
		removeAll();
		buildGUI();
		validate();
		repaint();
		firePropertyChange(EXPANDED_NODE_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		add(iconLabel, "0, 0, LEFT, CENTER");
		if (expanded) {
			layout.setRow(1, TableLayout.FILL);
			add(elapse, "1, 0, 1, 1");
		} else {
			layout.setRow(1, TableLayout.PREFERRED);
			add(collapse, "1, 0, 1, 1");
		}
	}

	/** 
	 * Initializes the components. 
	 */
	private void initialize()
	{
		iconLabel = new JLabel();
		adapter = new MouseAdapter() {
		
			public void mouseReleased(MouseEvent e)
			{ 
				expanded = !expanded;
				updateDisplay(); 
			}
		};
		iconLabel.addMouseListener(adapter);
		//set the layout 
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
						{20, TableLayout.FILL} }; //rows
		layout = new TableLayout(tl);
		setLayout(layout);
	}
	
	/** 
	 * Sets the color of the border of the passed component.
	 * 
	 * @param comp    The component to handle.
	 * @param enabled Pass <code>true</code> to allow operation, 
	 * 				  <code>false</code> otherwise.
	 */
	private void setComponentColor(JComponent comp, boolean enabled)
	{
		Border b = comp.getBorder();
		if (b instanceof TitledBorder) {
			TitledBorder tb = (TitledBorder) b;
			if (enabled) tb.setTitleColor(Color.BLACK);
			else tb.setTitleColor(Color.GRAY);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elapse	The component to display when the node is expanded.
	 * @param collapse	The component to display when the node is collapsed.
	 */
	TreeComponentNode(JComponent elapse, JComponent collapse)
	{
		this(elapse, collapse, true);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elapse	The component to display when the node is expanded.
	 * @param collapse	The component to display when the node is collapsed.
	 * @param expanded 	Pass <code>true</code> to expand the node, 
	 * 					<code>false</code> to collapse it.
	 */
	TreeComponentNode(JComponent elapse, JComponent collapse, 
							boolean expanded)
	{
		if (collapse == null || elapse == null)
			throw new IllegalArgumentException("No components to lay out.");
		this.collapse = collapse;
		this.elapse = elapse;
		this.expanded = expanded;
		initialize();
		buildGUI();
	}

	/**
	 * Sets the {@link #expanded} flag used to set the correct icon.
	 * 
	 * @param expanded	The value to set.
	 */
	void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}
	
	/**
	 * Sets the icons.
	 * 
	 * @param collapseIcon The collapse icon.
	 * @param elapseIcon	The elapse icon.
	 */
	void setIcons(Icon collapseIcon, Icon elapseIcon)
	{
		if (expanded) iconLabel.setIcon(elapseIcon);
		else iconLabel.setIcon(collapseIcon);
	}
	
	/**
	 * Returns <code>true</code> if the node is expanded,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isExpanded() { return expanded; }
	
	/**
	 * Allows or not the user to expand/collapse the node.
	 * 
	 * @param enabled Pass <code>true</code> to allow operation, 
	 * 				<code>false</code> otherwise.
	 */
	void setNodeEnabled(boolean enabled)
	{
		iconLabel.setEnabled(enabled);
		iconLabel.removeMouseListener(adapter);
		if (enabled) iconLabel.addMouseListener(adapter);
		setComponentColor(collapse, enabled);
		setComponentColor(elapse, enabled);
	}
	
}
