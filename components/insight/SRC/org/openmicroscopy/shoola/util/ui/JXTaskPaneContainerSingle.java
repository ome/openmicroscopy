/*
 * org.openmicroscopy.shoola.util.ui.JXTaskPaneContainerSingle
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;


//Third-party libraries
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
	
	private GridBagLayout layout = new GridBagLayout();
	
	/** Initializes the component. */
	private void initialize()
	{
		expandable = true;
		panes = new HashMap<JXTaskPane, Integer>();
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
	 * Get the number of task panes currently in expanded state
	 * @return See above
	 */
	private int getTaskPaneExpandedCount()
    {
        Component[] comps = getComponents();
        if (comps == null) 
            return -1;
        JXTaskPane pane;
        int count = 0;
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JXTaskPane) {
                pane = (JXTaskPane) comps[i];
                if (!pane.isCollapsed()) 
                    count++;
            }
        }
        return count;
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
	 * Overridden to attach listener to the component if it is a 
	 * <code>JXTaskPane</code>.
	 * @see JXTaskPaneContainer#add(Component)
	 */
    public void add(JXTaskPane component)
	{
	    component.setAnimated(false);
		int index = panes.size();
		panes.put(component, index);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = index;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTH;
        super.add(component, c);
		component.addPropertyChangeListener(
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
			src.setSpecial(false);
			return;
		}
		Container parent = src.getParent();
		Component[] comp = parent.getComponents();
		Component c;
		JXTaskPane p;
		if (src.isCollapsed()) {
			if (getTaskPaneExpandedCount()<2) 
			    return;
			for (int i = 0; i < comp.length; i++) {
				c = comp[i];
				if (c instanceof JXTaskPane) {
					p = (JXTaskPane) c;
					if (p == src) {
                        GridBagConstraints con = layout.getConstraints(p);
                        con.fill = GridBagConstraints.HORIZONTAL;
                        con.weighty = 0;
                        layout.setConstraints(p, con);
					}
				}
			}
			firePropertyChange(SELECTED_TASKPANE_PROPERTY, null, src);
			return;
		}
		
		for (int i = 0; i < comp.length; i++) {
			c = comp[i];
			if (c instanceof JXTaskPane) {
				p = (JXTaskPane) c;
                if (p != src) {
                    p.setCollapsed(true);
                    p.setSpecial(false);
                    GridBagConstraints con = layout.getConstraints(p);
                    con.fill = GridBagConstraints.HORIZONTAL;
                    con.weighty = 0;
                    layout.setConstraints(p, con);
                } else {
                    GridBagConstraints con = layout.getConstraints(p);
                    con.fill = GridBagConstraints.BOTH;
                    con.weighty = 1;
                    layout.setConstraints(p, con);
                }
			}
		}
		src.setSpecial(true);
		firePropertyChange(SELECTED_TASKPANE_PROPERTY, null, src);
	}
	
}
