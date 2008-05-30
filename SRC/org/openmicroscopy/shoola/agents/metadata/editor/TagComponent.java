/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TagComponent 
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
package org.openmicroscopy.shoola.agents.metadata.editor;

//Java imports
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies
import pojos.AnnotationData;
import pojos.ExperimenterData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

/** 
 * UI component hosting a tag annotation.
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
class TagComponent
	extends JLabel
	implements MouseListener
{

	/** The annotation hosted by this component. */
	private AnnotationData 	data;
	
	/** Reference to the view. */
	private TagsUI			uiDelegate;
	
	/** The initial font. */
	private Font			defaultFont;
	
	/** 
	 * Flag indicating if the component is editable, only
	 * components owned by the currently logged in user are editable.
	 */
	private boolean			editable;
	
	/** 
	 * Formats the tool tip.
	 * 
	 * @return See above;
	 */
	private String formatToolTip()
	{
		TagAnnotationData tag = (TagAnnotationData) data;
		List descriptions = tag.getTagDescriptions();
		StringBuffer buf = new StringBuffer();
		if (descriptions == null || descriptions.size() == 0) {
			if (!editable) {
				buf.append("<html><body>");
				buf.append("<b>Owner: ");
				buf.append(EditorUtil.formatExperimenter(tag.getOwner()));
				buf.append("</b><br>");
				buf.append("</body></html>");
			}
			return buf.toString();
		}
		buf.append("<html><body>");
		if (!editable) {
			buf.append("<b>Owner: ");
			buf.append(EditorUtil.formatExperimenter(tag.getOwner()));
			buf.append("</b><br>");
		}
		Iterator i = descriptions.iterator();
		TextualAnnotationData desc;
		List l;
		Iterator j;
		while (i.hasNext()) {
			desc = (TextualAnnotationData) i.next();
			if (desc != null) {
				
				buf.append("<b>Described by: ");
				buf.append(EditorUtil.formatExperimenter(desc.getOwner()));
				buf.append("</b><br>");
				l = UIUtilities.wrapStyleWord(desc.getText());
				j = l.iterator();
				while (j.hasNext()) {
					buf.append((String) j.next());
					buf.append("<br>");
				}
			}
		}
		buf.append("</body></html>");
		return buf.toString();
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		TagAnnotationData tag = (TagAnnotationData) data;
		setText(tag.getTagValue());
		String toolTip = formatToolTip();
		if (toolTip != null) setToolTipText(toolTip);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param uiDelegate	The ui delegate.
	 * @param data			The annotation to handle.
	 * @param editable      Pass <code>true</code> to allow the user to 
	 * 						manipulate the component, <code>false</code>
	 * 						otherwise.
	 */
	TagComponent(TagsUI uiDelegate, AnnotationData data, boolean editable)
	{
		if (data == null)
			throw new IllegalArgumentException("No tag specified.");
		if (uiDelegate == null)
			throw new IllegalArgumentException("No view.");
		this.editable = editable;
		this.data = data;
		this.uiDelegate = uiDelegate;
		defaultFont = getFont();
		addMouseListener(this);
		buildGUI();
	}

	/** Resets the original font. */
	void resetFont() { setFont(defaultFont); }
	
	/** 
	 * Sets a derive font.
	 * 
	 * @param style The style of the new font.
	 */
	void setComponentFont(int style)
	{
		setFont(getFont().deriveFont(style));
		repaint();
	}
	
	/**
	 * Adds the passed string to the text.
	 * 
	 * @param separator The value to set.
	 */
	void setSeparator(String separator)
	{
		String s = getText();
		setText(s+separator);
	}
	
	/**
	 * Returns the annotation hosted by this component.
	 * 
	 * @return See above.
	 */
	AnnotationData getAnnotation() { return data; }
	
	/**
	 * Edits the annotation if the user clicks twice on the annotation.
	 * Displays the menu if right click occurs.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		setComponentFont(Font.BOLD);
		if (e.getClickCount() == 2) {
			uiDelegate.editAnnotation(e.getPoint(), this);
		} else
			if (editable)
				uiDelegate.setSelectedTag(e.isShiftDown(), this);
		
		if (e.isPopupTrigger() && editable)
			uiDelegate.showMenu(e.getPoint(), e.getComponent());
	}

	/**
	 * Resets the font.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e)
	{
		resetFont();
	}
	
	/**
	 * Displays the menu if right click occurs.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{

		if (e.isPopupTrigger()) 
			uiDelegate.showMenu(e.getPoint(), e.getComponent());
	}
	
	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {}



}
