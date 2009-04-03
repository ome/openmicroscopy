/*
 * org.openmicroscopy.shoola.util.ui.ButtonMenu
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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicArrowButton;

//Third-party libraries

//Application-internal dependencies

/** 
 * A drop-down button.
 * This button is actually composed by two sub-buttons:
 * <ul>
 *  <li>An <i>arrow button</i> to trigger the display of a popup menu.</li>
 *  <li>An <i>icon button</i> to forward mouse clicks on to an item in the
 * 		popup menu.</li>
 * </ul>
 * <p>Methods are provided to add/remove items to/from the popup menu.  When an
 * {@link AbstractButton} is {@link #addToMenu(AbstractButton, boolean) added}
 * to the menu, it's possible to specify whether mouse clicks should be tracked.
 * The <i>icon button</i> remembers which was the last item, among those that
 * have been requested to be tracked, to be clicked and sets its tooltip to be
 * that item's text &#151; that is, to the value returned by the item's 
 * <code>getText</code> method.  Any mouse click on the <i>icon button</i> will
 * then be forwarded to that menu item.</p>
 * <p>Note that the <i>icon button</i> doesn't necessarily track all menu items.
 * So it may happen that an item is clicked but not remembered.  Moreover, if
 * the lastly remembered item happens to be 
 * {@link #removeFromMenu(Component) removed} from the menu, the
 * <i>icon button</i> is reset.  This means the button doesn't point to any 
 * menu item and mouse clicks are just ignored.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ButtonMenu
	extends AbstractButton
	implements ActionListener
{
	
//TODO: provide an ad-hoc ButtonModel implementation.
//We have two buttons but we want external classes to see this as a single
//button.  This is expecially true when we add the button to a JToolBar, 
//which modifies buttons appearence if its rollover property is set to true. 
	
	
	/** Fixed width of the arrow button. */
	private static final int	ARROW_BUTTON_WIDTH = 18;
	

	/**
	 * Custom layout manager to display the arrow and icon buttons properly.
	 */
	private class DefaultLayoutManager 
		implements LayoutManager
	{
		public void layoutContainer(Container parent) 
		{
			Dimension d = iconButton.getPreferredSize();
			iconButton.setBounds(0, 0, d.width, d.height);
			arrowButton.setBounds(d.width, 0, ARROW_BUTTON_WIDTH, d.height);
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
	
	/** The icon button to trigger a click?? */
	private JButton			iconButton;
	
	/** The drop-down menu. */
	private JPopupMenu		menu;
	
	/** 
	 * Remembers the last tracked item that was clicked in the menu.
	 * Not all items in the menu are necessarily tracked.  In fact, items are
	 * tracked only on request when they're 
	 * {@link #addToMenu(AbstractButton, boolean) added} to the menu.
	 * If the lastly tracked item happens to be 
	 * {@link #removeFromMenu(Component) removed} from the menu, this field is
	 * reset to <code>null</code>. 
	 */
	private AbstractButton	lastClickedItem;
	
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setLayout(new DefaultLayoutManager());
		setBorder(null);
		setMargin(null);
		add(iconButton);
		add(arrowButton);
	}
	
	/** Forwards the click to the {@link #lastClickedItem} if any. */
	private void handleIconButtonClick() 
	{
		if (lastClickedItem != null) lastClickedItem.doClick();
	}
	
	/** Brings up the drop-down menu. */
	private void handleArrowButtonClick()
	{
		//Avoid displaying an empty menu, kinda ugly.
		if (menu.getComponents().length == 0) return;
		
		Dimension d = iconButton.getPreferredSize();
		menu.show(iconButton, 0, d.height);
	}
	
	/** Sets {@link #lastClickedItem} to <code>src</code>. */
	private void handleMenuButtonClick(AbstractButton src) 
	{
		lastClickedItem = src;
		iconButton.setToolTipText(UIUtilities.formatToolTipText(src.getText()));
	}
	
	/**
	 * Overridden to make sure that no component other than the
	 * {@link #iconButton} or {@link #arrowButton} can be added.
	 */
	protected void addImpl(Component comp, Object constraints, int index)
	{
		if (comp == iconButton || comp == arrowButton)
			super.addImpl(comp, constraints, index);
	}
	//NOTE: we may want to override the remove methods from Container as well.
	
	/**
	 * Creates a new instance.
	 * 
	 * @param icon	An icon for the <i>icon button</i>.  Mustn't be
	 * 				<code>null</code>. 
	 */
	public ButtonMenu(Icon icon) 
	{
		if (icon == null) throw new NullPointerException("No icon.");
		iconButton = new JButton(icon);
		iconButton.addActionListener(this);
		arrowButton = new BasicArrowButton(BasicArrowButton.SOUTH);
		arrowButton.addActionListener(this);
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
		if (item != null) menu.add(item);
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
		if (item instanceof AbstractButton) {
			((AbstractButton) item).removeActionListener(this);
		//NOTE: addToMenu(AbstrctButton, boolean) might have registred with
		//this button.  If so, we have to unsubscribe.  If not, unsubscribing
		//will be harmless, but will avoid the pain of tracking the buttons
		//we actually registred with.
		
			if (item == lastClickedItem) {
				lastClickedItem = null;
				iconButton.setToolTipText("");
			} 
		}
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
		if (src == iconButton) handleIconButtonClick();
		else if (src == arrowButton) handleArrowButtonClick();
		else handleMenuButtonClick((AbstractButton) src);
		//NOTE: we only register with AbstractButtons, so this is safe.
	}
	
	/** Overridden to return the right size. */
	public Dimension getPreferredSize()
	{
		Dimension d = iconButton.getPreferredSize();
		return new Dimension(d.width+ARROW_BUTTON_WIDTH, d.height);
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
		iconButton.setBorder(b);
		arrowButton.setBorder(b);
	}
	
	/** Overridden to set the rollover property of the two sub-buttons. */
	public void setRolloverEnabled(boolean enable)
	{
		super.setRolloverEnabled(enable);
		iconButton.setRolloverEnabled(enable);
		arrowButton.setRolloverEnabled(enable);
	}

}
