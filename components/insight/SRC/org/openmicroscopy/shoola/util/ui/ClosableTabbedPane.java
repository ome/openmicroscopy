/*
 * org.openmicroscopy.shoola.util.ui.ClosableTabbedPane 
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
import java.awt.Component;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * A tabbed pane handling component with a close button.
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
public class ClosableTabbedPane
	extends JTabbedPane
	implements ChangeListener, PropertyChangeListener
{

	/** Bound property indicating that an element has been closed. */
	public static final String	CLOSE_TAB_PROPERTY = "closeTab";
	
	/** Creates the customized UI.*/
	private void createUI()
	{
		ClosableTabbedPaneUI ui = new ClosableTabbedPaneUI(this);
    	setUI(ui);
    	addMouseMotionListener(ui);
	}
	
	/**
     * Creates an empty <code>TabbedPane</code> with a default
     * tab placement of <code>JTabbedPane.TOP</code>.
     */
	public ClosableTabbedPane()
	{
		 this(TOP, WRAP_TAB_LAYOUT);
	}
	
	/**
     * Creates an empty <code>TabbedPane</code> with the specified tab placement
     * of either: <code>JTabbedPane.TOP</code>, <code>JTabbedPane.BOTTOM</code>,
     * <code>JTabbedPane.LEFT</code>, or <code>JTabbedPane.RIGHT</code>.
     *
     * @param tabPlacement The placement for the tabs relative to the content.
     */
    public ClosableTabbedPane(int tabPlacement)
    {
    	 this(tabPlacement, WRAP_TAB_LAYOUT);
    }
    
    /**
     * Creates an empty <code>TabbedPane</code> with the specified tab placement
     * and tab layout policy.  Tab placement may be either: 
     * <code>JTabbedPane.TOP</code>, <code>JTabbedPane.BOTTOM</code>,
     * <code>JTabbedPane.LEFT</code>, or <code>JTabbedPane.RIGHT</code>.
     * Tab layout policy may be either: <code>JTabbedPane.WRAP_TAB_LAYOUT</code>
     * or <code>JTabbedPane.SCROLL_TAB_LAYOUT</code>.
     *
     * @param tabPlacement 		The placement for the tabs relative to the 
     * 							content.
     * @param tabLayoutPolicy 	The policy for laying out tabs when all tabs 
     * 							will not fit on one run.
     */
    public ClosableTabbedPane(int tabPlacement, int tabLayoutPolicy)
    {
    	super(tabPlacement, tabLayoutPolicy);
    	createUI();
    	setFocusable(false);
    	addChangeListener(this);
    	//Since user can change the font while the application is running
    	//add a listener
    	Toolkit tk = Toolkit.getDefaultToolkit();
    	tk.addPropertyChangeListener(UIUtilities.HINTS_PROPERTY, this);
    }
    
    /** 
     * Removes all components excepted the selected one from the 
     * tabbed pane.
     */
    void removeOthers()
    {
        int tabCount = getTabCount();
        int index = getSelectedIndex();
        while (tabCount-- > 0) {
        	if (index != tabCount) 
        		removeTabAt(tabCount);
        }
    }
    
    /**
     * Inserts the passed component.
     * 
     * @param component The component to add.
     */
    public void insertClosableComponent(ClosableTabbedPaneComponent component)
    {
    	if (component == null) return;
    	insertTab(component.getName(), component.getIcon(), component, 
    			component.getDescription(), component.getIndex());
    	setSelectedComponent(component);
    }
    
    /**
	 * Overridden to remove components from the tab one by one.
	 * @see JTabbedPane#removeAll()
	 */
    public void removeAll()
    {
    	setSelectedIndex(-1);
        int tabCount = getTabCount();
        while (tabCount-- > 0)
            removeTabAt(tabCount);
    }
    
    /**
	 * Overridden to fire a property change indicating that 
	 * component is removed from the tab pane.
	 * @see JTabbedPane#remove(int)
	 */
    public void remove(int index) { removeTabAt(index); }
    
	/**
	 * Overridden to fire a property change indicating that 
	 * component is removed from the tab pane.
	 * @see JTabbedPane#removeTabAt(int)
	 */
	public void removeTabAt(int index)
	{
		Component c = getComponentAt(index);
		if (c instanceof ClosableTabbedPaneComponent) {
			firePropertyChange(CLOSE_TAB_PROPERTY, null, c);
		}
		if (ui instanceof ClosableTabbedPaneUI) {
			((ClosableTabbedPaneUI) ui).resetDefault();
		} else {
			createUI();
		}
		super.removeTabAt(index);
		int n = getTabCount();
		if (n == 0) return;

		if (index > n) setSelectedComponent(getComponentAt(n-1));
	}

	/**
	 * Overridden to insert the tab at the end or requests focus on 
	 * the added component.
	 * @see JTabbedPane#addTab(String, Icon, Component)
	 */
	public void addTab(String title, Icon icon, Component component)
	{
		insertTab(title, icon, component, "", getTabCount());
	}
	
	/**
	 * Overridden to insert the tab at the end or requests focus on 
	 * the added component.
	 * @see JTabbedPane#insertTab(String, Icon, Component, String, int)
	 */
	public void insertTab(String title, Icon icon, Component component, 
						String tip, int index)
	{
		int addIndex = indexOfComponent(component);
		if (addIndex != -1) {
			setSelectedComponent(component);
			return;
		}
		if (ui instanceof ClosableTabbedPaneUI) {
			((ClosableTabbedPaneUI) ui).resetDefault();
		} else {
			createUI();
		}
		super.insertTab(title, icon, component, tip, getTabCount());
		
	}
	
	/**
	 * Overridden to request the focus on the selected component.
	 * @see JTabbedPane#setSelectedComponent(Component)
	 */
	public void setSelectedComponent(Component component)
	{
		super.setSelectedComponent(component);
		if (component != null) component.requestFocus();
	}
	
	/**
	 * Sets the background of the selected tabbed pane.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		setOpaque(true);
		for (int i = 0; i < getTabCount(); i++) {
			if (getSelectedIndex() != i)
				setBackgroundAt(i, getBackground());
		}
	}

	/**
	 * Reacts to font changes while the application is running.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (UIUtilities.HINTS_PROPERTY.equals(name))
			createUI();
	}
	
}
