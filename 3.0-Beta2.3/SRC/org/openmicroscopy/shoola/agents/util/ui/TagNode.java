/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.TagNode 
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
package org.openmicroscopy.shoola.agents.util.ui;





//Java imports
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;

/** 
 * Component hosting a tag or a tag set.
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
public class TagNode
	extends JPanel//JLabel
	implements MouseListener
{

	/** Indicates that the icon passed is used to remove the tag. */
	public static final int		REMOVE_TYPE = 0;
	
	/** Indicates that the icon passed is used to add a tag. */
	public static final int		ADD_TYPE = 1;
	
	/** Bound property indicating that the tag is selected. */
	public static final String	TAG_SELECTED_PROPERTY = "tagSelected";
	
	/** Bound property indicating that the tag set is selected. */
	public static final String	TAG_SET_SELECTED_PROPERTY = "tagSetSelected";
	
	/** Bound property indicating to browse the tag or tag sets. */
	public static final String	BROWSE_PROPERTY = "browse";
	
	/** Bound property indicating to delete the tag or tag sets. */
	public static final String	DELETE_PROPERTY = "delete";
	
	/** Bound property indicating to add the tag. */
	public static final String	ADD_PROPERTY = "add";
	
	/** Bound property indicating to edit the tag. */
	public static final String	EDIT_PROPERTY = "edit";
	
	/** Foreground color when the node is highlighted. */
	private static final Color HIGHLIGHT_COLOR = Color.BLUE;
	
	/** The size of the editor. */
	private static final Dimension	EDITOR_SIZE = new Dimension(200, 150);
	
	/** The original foreground color. */
	private Color 			originalColor;
	
	/** The original font. */
	private Font 			originalFont;
	
	/** The original font. */
	private Font 			deriveFont;
	
	/** The object hosted by this component. */
	private DataObject 		node;
	
	/** The label displaying the text. */
	private JLabel			label;
	
	/** The label displaying the text. */
	private JLabel			iconLabel;
	
	/**
	 * One out of the following constants: {@link #REMOVE_TYPE} or 
	 * {@link #ADD_TYPE}.
	 */
	private int				index;
	
	/** Component used to edit the data object. */
	private MultilineLabel	editor;
	
	/** Menu used to edit the tag. */
	private JPopupMenu		editorMenu;
	
	/** Sets the text and the tooltip of the node. */
	private void setNodeTexts()
	{
		label = new JLabel();
		if (node instanceof CategoryData) {
			CategoryData tag = (CategoryData) node;
			label.setText(tag.getName());
			label.setToolTipText(tag.getDescription());
		} else if (node instanceof CategoryGroupData) {
			CategoryGroupData tagSet = (CategoryGroupData) node;
			label.setText(tagSet.getName());
			label.setToolTipText(tagSet.getDescription());
		}
	}
	
	/** 
	 * Edits the data object. 
	 * 
	 * @param point The location of the mouse click.
	 */
	private void edit(Point point)
	{
		if (editor == null) {
			editor = new MultilineLabel(label.getToolTipText());
			editor.setEditable(true);
			editor.addKeyListener(new KeyAdapter() {
				
				/** 
				 * Saves the data when the user presses enter
				 * @see MouseAdapter#keyPressed(KeyEvent)
				 */
				public void keyPressed(KeyEvent e)
				{
	            	switch (e.getKeyCode()) {
						case KeyEvent.VK_ENTER:
							String s = editor.getText();
							boolean fire = false;
							if (node instanceof CategoryData) {
								((CategoryData) node).setDescription(s);
								fire = true;
							} else if (node instanceof CategoryGroupData) {
								((CategoryGroupData) node).setDescription(s);
								fire = true;
							}
							if (fire) {
								editorMenu.setVisible(false);
								label.setToolTipText(s);
								firePropertyChange(EDIT_PROPERTY, null, node);
							}
								
							break;
						
					}
				}
			
			});
			JScrollPane pane = new JScrollPane(editor);
			editor.setPreferredSize(EDITOR_SIZE);
			editorMenu = new JPopupMenu();
			editorMenu.add(pane);
		} else editor.setText(label.getToolTipText());
		
		editorMenu.show(this, point.x, point.y+10);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param node	The node hosted by this component.
	 * @param icon 	The icon to display.
	 * @param index	The index attached to the icon.
	 */
	public TagNode(DataObject node, Icon icon, int index)
	{
		if (node == null)
			throw new IllegalArgumentException("No node.");
		this.node = node;
		setNodeTexts();
		label.addMouseListener(this);
		originalColor = getForeground();
		originalFont = getFont();
		deriveFont = getFont().deriveFont(Font.BOLD);
		iconLabel = new JLabel(icon);
		iconLabel.addMouseListener(this);
		this.index = index;
		add(iconLabel);
		add(label);
	}
	
	/** HighLights the node. */
	public void highLightNode()
	{
		label.setForeground(HIGHLIGHT_COLOR);
		label.setFont(deriveFont);
	}
	
	/** Resets the default values for the node. */
	public void resetNodeDisplay()
	{
		label.setForeground(originalColor);
		label.setFont(originalFont);
	}
	
	/**
	 * Returns the id of the object hosted by this node.
	 * 
	 * @return See above.
	 */
	public long getObjectID() { return node.getId(); }
	
	/**
	 * Returns <code>true</code> if the node contains the tag,
	 * <code>false</code> otherwise.
	 * 
	 * @param tagID The id of the tag to handle.
	 * @return See above.
	 */
	public boolean containsNode(long tagID)
	{
		if (node instanceof CategoryGroupData) {
			CategoryGroupData tagSet = (CategoryGroupData) node;
			Set tags = tagSet.getCategories();
			if (tags == null) return false;
			Iterator i = tags.iterator();
			DataObject tag;
			while (i.hasNext()) {
				tag = (DataObject) i.next();
				if (tagID == tag.getId()) return true;
			}
			return false;
		}
		return false;
	}
	
	/**
	 * Fires property chang to browse the tag or the tag set.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		if (e.getSource() == iconLabel) {
			switch (index) {
				case REMOVE_TYPE:
					firePropertyChange(DELETE_PROPERTY, null, node);
					break;
				case ADD_TYPE:
					firePropertyChange(ADD_PROPERTY, null, node);
					break;
			}
			
		} else {
			if (e.getClickCount() == 2)
				firePropertyChange(BROWSE_PROPERTY, null, node);
			else 
				edit(e.getPoint());
		}
	}

	/**
	 * Fires property chamge when the mouse entered in the node
	 * to highlight related nodes.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if (node instanceof CategoryData) {
			firePropertyChange(TAG_SELECTED_PROPERTY, null, this);
		} else if (node instanceof CategoryGroupData) {
			firePropertyChange(TAG_SET_SELECTED_PROPERTY, null, this);
		}
	}

	/**
	 * Fires property change when the mouse entered in the node
	 * to highlight related nodes.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e)
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		if (node instanceof CategoryData) {
			firePropertyChange(TAG_SELECTED_PROPERTY, null, null);
		} else if (node instanceof CategoryGroupData) {
			firePropertyChange(TAG_SET_SELECTED_PROPERTY, null, null);
		}
	}
	
	/**
	 * Requires by the {@link MouseListener} I/F but no-op implementation
	 * in our case
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}

	/**
	 * Requires by the {@link MouseListener} I/F but no-op implementation
	 * in our case
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {}

}
