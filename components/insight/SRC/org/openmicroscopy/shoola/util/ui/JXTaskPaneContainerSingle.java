/*
 * org.openmicroscopy.shoola.util.ui.JXTaskPaneContainerSingle
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;

//Third-party libraries
import layout.TableLayout;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

//Application-internal dependencies

/**
 * A JXTaskPaneContainer allowing no more than a JXTaskPane expanded at the 
 * same time.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class JXTaskPaneContainerSingle 
	extends JXTaskPaneContainer
	implements PropertyChangeListener
{

	/** Bound property indicating the selection of a new task pane. */
	public static final String SELECTED_TASKPANE_PROPERTY = "selectedTaskPane";

	/** The map hosting the <code>JXTaskPane</code>s. */
	private Map<JXTaskPane, Integer> panes;
	
	/** Flag indicating that a tab pane can or cannot be expanded. */
	private boolean	expandable;
	
	/** Initializes the component. */
	private void initialize()
	{
		expandable = true;
		panes = new HashMap<JXTaskPane, Integer>();
		TableLayout layout = new TableLayout();
		double[] size = {TableLayout.FILL};
		layout.setColumn(size);
		setLayout(layout);
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setBackground(UIUtilities.BACKGROUND);
	}
	
	/** Creates a new instance. */
	public JXTaskPaneContainerSingle()
	{
		initialize();
	}
	
	/**
	 * Passes <code>true</code> to allow a component to be expanded,
	 * <code>false</code> otherwise.
	 * 
	 * @param expandable The value to set.
	 */
	public void setExpandable(boolean expandable)
	{
		this.expandable = expandable;
	}
	
	/**
	 * Return the list of JXTaskPanes in the component.
	 * @return See above.
	 */
	public List<JXTaskPane> getTaskPanes()
	{
		List<JXTaskPane> list = new ArrayList<JXTaskPane>();
		Iterator<JXTaskPane> iterator = panes.keySet().iterator();
		while(iterator.hasNext())
			list.add(iterator.next());
		return list;
	}
	
	/**
	 * Returns <code>true</code> if one of the <code>JXTaskPane</code>s 
	 * is expanded, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasTaskPaneExpanded()
	{
		Component[] comps = getComponents();
		if (comps == null) return false;
		JXTaskPane pane;
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof JXTaskPane) {
				pane = (JXTaskPane) comps[i];
				if (!pane.isCollapsed()) return true;
			}
		}
		return false;
	}

	/**
	 * Overridden to attach listener to the component if it is a 
	 * <code>JXTaskPane</code>.
	 * @see JXTaskPaneContainer#add(Component)
	 */
	public void add(JXTaskPane c)
	{
		int index = panes.size();
		//c.all
		TableLayout layout = (TableLayout) getLayout();
		double h;
		if (c.isCollapsed()) h = TableLayout.PREFERRED;
		else  h = TableLayout.FILL;
		layout.insertRow(index, h);
		super.add(c, "0, "+index);
		panes.put(c, index);
		c.addPropertyChangeListener(
				UIUtilities.COLLAPSED_PROPERTY_JXTASKPANE, this);
	}

	/**
	 * Reacts to the expansion of <code>JXTaskPane</code>s.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		JXTaskPane src = (JXTaskPane) evt.getSource();
		if (!expandable) {
			src.setCollapsed(true);
			return;
		}
		TableLayout layout = (TableLayout) getLayout();
		if (src.isCollapsed()) {
			layout.setRow(panes.get(src), TableLayout.PREFERRED);
			if (hasTaskPaneExpanded()) return;
			firePropertyChange(SELECTED_TASKPANE_PROPERTY, null, src);
			return;
		}
		Container parent = src.getParent();
		Component[] comp = parent.getComponents();
		Component c;
		for (int i = 0; i < comp.length; i++) {
			c = comp[i];
			if (c instanceof JXTaskPane) {
				JXTaskPane p = (JXTaskPane) c;
				if (p != src) {
					layout.setRow(panes.get(src), TableLayout.FILL);
					p.setCollapsed(true);
					p.setSpecial(false);
				}
			}
		}
		src.setSpecial(true);
		firePropertyChange(SELECTED_TASKPANE_PROPERTY, null, src);
	}
	
}
