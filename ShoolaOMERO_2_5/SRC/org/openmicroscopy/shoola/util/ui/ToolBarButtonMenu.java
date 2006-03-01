/*
 * org.openmicroscopy.shoola.util.ui.ToolbarButtonMenu
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.DefaultButtonModel;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicArrowButton;

//Third-party libraries

//Application-internal dependencies

/** 
 * A drop-down button, for use by a {@link TaskBar} in a {@link
 * TopWindowGroup}. Similar to {@link ButtonMenu}, but containing
 * only one item.
 *
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ToolBarButtonMenu
	extends AbstractButton
	implements ActionListener
{
	
	/** Fixed width of the arrow button. */
	private static final int	ARROW_BUTTON_WIDTH = 18;
	/** Fixed height of the arrow button. */
	private static final int	ARROW_BUTTON_HEIGHT = 24;
	

	/**
	 * Custom layout manager to display the arrow and icon buttons properly.
	 */
	private class DefaultLayoutManager 
		implements LayoutManager
	{
		public void layoutContainer(Container parent) 
		{
			arrowButton.setBounds(0, 0, ARROW_BUTTON_WIDTH, ARROW_BUTTON_HEIGHT);
		}
		public Dimension minimumLayoutSize(Container parent)
		{ return preferredLayoutSize(parent); }
		public Dimension preferredLayoutSize(Container parent)
		{ return getPreferredSize(); }
		public void removeLayoutComponent(Component comp) {}
		public void addLayoutComponent(String name, Component comp) {}	
		}
	
	/** The arrow button to trigger the display of the drop-down menu. */
	private JButton			arrowButton;
	
	
	/** The drop-down menu. */
	private JPopupMenu		menu;
	
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
	    setLayout(new DefaultLayoutManager());
		setBorder(null);
		setMargin(null);
		add(arrowButton);
	}
	
	/** Brings up the drop-down menu. */
	private void handleArrowButtonClick()
	{
	    //Avoid displaying an empty menu, kinda ugly.
		if (menu.getComponents().length == 0) return;
		Dimension d = arrowButton.getPreferredSize();
		menu.show(arrowButton, 0, d.height); 
	}
	
	/**
	 * Overridden to make sure that no component other than the
	 * {@link #iconButton} or {@link #arrowButton} can be added.
	 */
	protected void addImpl(Component comp, Object constraints, int index)
	{
		if (comp == arrowButton)
		    super.addImpl(comp, constraints, index);
	}
	//NOTE: we may want to override the remove methods from Container as well.
	
	/**
	 * Creates a new instance.
	 */
         public ToolBarButtonMenu()
	{
		arrowButton = new BasicArrowButton(BasicArrowButton.SOUTH);
		arrowButton.addActionListener(this);
		arrowButton.setEnabled(false);
		menu = new JPopupMenu();
		setModel(new DefaultButtonModel());  //TODO: replace with ad-hoc model.
		buildGUI();
	}

	/**
	 * Adds the specified component to the drop-down menu.
	 * 
	 * @param item	The component to add.  Normally a <code>JMenuItem</code> or
	 * 				a <code>JSeparator</code>.
	 */
	public void addToMenu(Component item)
	{
		if (item != null) {
			menu.add(item);
			arrowButton.setEnabled(true);
		}
	}
	
	/**
	 * Adds the specified button to the drop-down menu.
	 * 
	 * @param btn	The button to add.  Normally a <code>JMenuItem</code>.
	 * @param trackClick Specifies whether mouse clicks should be tracked.
	 */
	public void addToMenu(AbstractButton btn, boolean trackClick)
	{
		if (btn != null) menu.add(btn);
		if (trackClick) btn.addActionListener(this);
	}
	
	/**
	 * Appends a new separator at the end of the drop-down menu.
	 */
	public void addSeparator() { menu.addSeparator(); }
	
	/**
	 * Removes the specified component from the drop-down menu.
	 * 
	 * @param item	The component to remove.
	 */
	public void removeFromMenu(Component item) 
	{ 
		if (item == null) return;
		menu.remove(item);
		if (item instanceof AbstractButton) 
			((AbstractButton) item).removeActionListener(this);
		//NOTE: addToMenu(AbstrctButton, boolean) might have registred with
		//this button.  If so, we have to unsubscribe.  If not, unsubscribing
		//will be harmless, but will avoid the pain of tracking the buttons
		//we actually registred with.
		if (menu.getComponents().length == 0) 
			arrowButton.setEnabled(false);
	}
	
	/**
	 * Removes all components from the drop-down menu.
	 */
	public void clearMenu()
	{
		Component[] items = menu.getComponents();
		for (int i = 0; i < items.length; ++i) removeFromMenu(items[i]);
	}
	
	/** Demultiplexes the event to the right handler. */
	public void actionPerformed(ActionEvent ae) 
	{	
		Object src = ae.getSource();
		if (src == arrowButton) handleArrowButtonClick();
	}
	
	/** Overridden to return the right size. */
	public Dimension getPreferredSize()
	{
	    return new Dimension(ARROW_BUTTON_WIDTH,ARROW_BUTTON_HEIGHT);
	}
	
	/** Overridden to return the preferred size. */
	public Dimension getMaximumSize() { return getPreferredSize(); }
	
	/** Overridden to return the preferred size. */
	public Dimension getMinimumSize() { return getPreferredSize(); } 
	
	/** Overridden to trigger the display of the drop-down menu. */
	public void doClick() { arrowButton.doClick(); }
	
	/** Overridden to trigger the display of the drop-down menu. */
	public void doClick(int pressTime) { arrowButton.doClick(pressTime); }
	
	/** Overridden to set the border of the two sub-buttons. */
	public void setBorder(Border b)
	{
		arrowButton.setBorder(b);
	}
	
	/** Overridden to set the rollover property of the two sub-buttons. */
	public void setRolloverEnabled(boolean enable)
	{
		super.setRolloverEnabled(enable);
		arrowButton.setRolloverEnabled(enable);
	}
}
