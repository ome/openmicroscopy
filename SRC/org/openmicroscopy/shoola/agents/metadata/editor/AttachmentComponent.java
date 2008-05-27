/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AttachmentComponent 
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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.FileAnnotationData;

/** 
 * 
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
class AttachmentComponent 
	extends JPanel
	implements MouseListener
{

	/** The annotation hosted by this component. */
	private FileAnnotationData 	file;
	
	/** Reference to the view. */
	private AttachmentsUI 		view;
	
	/** Component displaying the icon corresponding to the file format. */
	private JPanel				iconPane;
	
	/** Component displaying the name of the file. */
	private JPanel				namePane;
	
	/** 
	 * Flag set to <code>true</code> if the currently logged in user
	 * owned the annotation. 
	 */
	private boolean				editable;
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.TEXT_DOC);;
		
		String format = file.getFileFormat();
		if (FileAnnotationData.PDF.equals(format))
			icon = icons.getIcon(IconManager.PDF_DOC);
		else if (FileAnnotationData.TEXT.equals(format))
			icon = icons.getIcon(IconManager.TEXT_DOC);
		else if (FileAnnotationData.MS_WORD.equals(format))
			icon = icons.getIcon(IconManager.WORD_DOC);
		else if (FileAnnotationData.MS_EXCEL.equals(format))
			icon = icons.getIcon(IconManager.EXCEL_DOC);
		else if (FileAnnotationData.MS_POWER_POINT.equals(format))
			icon = icons.getIcon(IconManager.PPT_DOC);
		else if (FileAnnotationData.XML.equals(format) ||
				FileAnnotationData.HTML.equals(format) ||
				FileAnnotationData.HTM.equals(format))
			icon = icons.getIcon(IconManager.XML_DOC);
		
		JLabel iconLabel = new JLabel(icon);
		
		String name = file.getFileName();
		JLabel nameLabel = new JLabel(file.getFileName());
		FontMetrics fm = nameLabel.getFontMetrics(nameLabel.getFont());
		
		int width = fm.stringWidth(name);
		int iconWith = icon.getIconWidth()+20;
		if (width > iconWith) {
			StringBuffer buf = new StringBuffer();
			buf.append("<html><body>");
			for (int i = 0; i < name.length(); i++) {
				buf.append(name.charAt(i));
				if (i%15 == 0 && i != 0) buf.append("<br>");
			}
			buf.append("</body></html>");
			nameLabel.setText(buf.toString());
		}
			
		if (editable) {
			nameLabel.addMouseListener(this);
			iconLabel.addMouseListener(this);
			addMouseListener(this);
		}

		iconPane = new JPanel();
		iconPane.add(iconLabel);
		namePane = new JPanel();
		namePane.add(nameLabel);
		setSelectedBackground(false);
		String toolTip = view.formatTootTip(file);
		iconLabel.setToolTipText(toolTip);
		nameLabel.setToolTipText(toolTip);
		setToolTipText(toolTip);
		setEnabled(editable);
		if (!editable) nameLabel.setForeground(Color.LIGHT_GRAY);
		iconLabel.setEnabled(editable);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		add(iconPane, c);
		c.gridy++;
		add(namePane, c);
		c.gridy++;
		add(Box.createVerticalStrut(10), c);
		setBackground(UIUtilities.BACKGROUND);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view	    Reference to the view. Mustn't be <code>null</code>.
	 * @param file	    The annotation hosted by this component. 
	 * 				    Mustn't be <code>null</code>.
	 * @param editable  Pass <code>true</code> if the annotation is owned 
	 * 					by the currently logged in user, <code>false</code>
	 * 					otherwise.
	 */
	AttachmentComponent(AttachmentsUI view, FileAnnotationData file, 
						boolean editable)
	{
		if (view == null)
			throw new IllegalArgumentException("No view.");
		if (file == null)
			throw new IllegalArgumentException("No annotation.");
		this.editable = editable;
		this.view = view;
		this.file = file;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Sets the background of the component when selected.
	 * 
	 * @param selected	Pass <code>true</code> to set the selected color,
	 * 					<code>false</code> to set the default color.
	 */
	void setSelectedBackground(boolean selected)
	{
		if (selected && editable) {
			iconPane.setBackground(Color.LIGHT_GRAY);
			namePane.setBackground(Color.LIGHT_GRAY);
		} else {
			setBackground(UIUtilities.BACKGROUND);
			iconPane.setBackground(UIUtilities.BACKGROUND);
			namePane.setBackground(UIUtilities.BACKGROUND);
		} 
		
		repaint();
	}
	
	/**
	 * Returns the annotation hosted by this component.
	 * 
	 * @return See above.
	 */
	FileAnnotationData getFile() { return file; }
	
	/** 
	 * Brings up a dialog box indicating to download 
	 * the file associated to the component.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		if (e.getClickCount() == 2)
			view.viewFile(this);
		else if (e.isPopupTrigger())
			view.createManagementMenu().show(e.getComponent(), e.getX(), 
					e.getY());
	}
	
	/** 
	 * Sets the selected label and shows the menu.
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		view.setSelectedFile(this);
		if (e.isPopupTrigger())
			view.createManagementMenu().show(e.getComponent(), e.getX(), 
									e.getY());
	}
	
	/**
	 * Modifies the cursor when entered.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
	{
		Object src = e.getSource();
		if (src instanceof JComponent) {
			((JComponent) src).setCursor(
					Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}
	
	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation in
	 * our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}

	/**
	 * Required by the {@link MouseListener} I/F but no-op implementation in
	 * our case.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {}

}
