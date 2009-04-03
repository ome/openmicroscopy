/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotationComponent 
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
package org.openmicroscopy.shoola.agents.util.annotator.view;



//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import layout.TableLayout;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.AnnotationData;

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
class AnnotationComponent 
	extends JPanel 
	implements ActionListener, DocumentListener
{
	
	/** Bound property indicating to save the annotation. */
	static final String			SAVE_PROPERTY = "save";
	
	/** Bound property indicating to save the annotation. */
	static final String			DELETE_PROPERTY = "delete";
	
	/** Action ID indicating to save the annotation. */
	private static final int	SAVE = 0;
	
	/** Action ID indicating to remove the annotation. */
	private static final int	DELETE = 1;
	
	/** The tooltip of the save button. */
	private static final String	SAVE_TIP = "Save the annotation";
	
	/** The tooltip of the delete button. */
	private static final String	DELETE_TIP = "Delete the annotation.";
	
	/** 
	 * The save button or <code>null</code> if the component is
	 * not editable.
	 */
	private JButton 		save;
	
	/** 
	 * The delete button or <code>null</code> if the component is
	 * not editable.
	 */
	private JButton 		delete;
	
	/** Area displaying the textual annotation. */
	private MultilineLabel 	area;

	/** The original text. */
	private String			originalText;
	
	/** The annotation to handle. */
	private AnnotationData	data;
	
	/**
	 * Initializes the UI components.
	 * 
	 * @param editable 	Pass <code>true</code> if the node is editable,
	 * 					<code>false</code> otherwise.
	 * @param deletable	Pass <code>true</code> if the node
	 * 					is deletable, <code>false</code> otherwise.
	 */
	private void initialize(boolean editable, boolean deletable)
	{
		area = new MultilineLabel();
        area.setEditable(editable);
        area.setOpaque(true);
        area.setText(originalText);
        if (editable) {
        	area.getDocument().addDocumentListener(this);
        	IconManager icons = IconManager.getInstance();
        	save = new JButton(icons.getIcon(IconManager.SAVE));
        	save.setToolTipText(SAVE_TIP);
        	save.setActionCommand(""+SAVE);
        	save.addActionListener(this);
        	save.setEnabled(false);
        	if (deletable) {
        		delete = new JButton(icons.getIcon(IconManager.CLOSE));
        		delete.setToolTipText(DELETE_TIP);
        		delete.setActionCommand(""+DELETE);
        		delete.addActionListener(this);
        	}
        }
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JToolBar bar = null;
		if (save != null) {
			bar = new JToolBar();
			bar.setFloatable(false);
			bar.setRollover(true);
			bar.setBorder(null);
			bar.setOpaque(true);
			bar.add(save);
			if (delete != null) bar.add(delete);
	        
		}
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.FILL} }; //rows
        setOpaque(true);
        setLayout(new TableLayout(tl));
        if (bar != null) add(bar, "0, 0, l, c");
        add(area, "1, 0, 1, 1");
	}
	
	private void handleTextChanged()
	{
		String text = area.getText();
		save.setEnabled(!text.equals(originalText));
	}
	
	/**
	 * Creates a new component.
	 * 
	 * @param textualAnnotation	The annotation to handle.
	 * @param editable			Pass <code>true</code> if the node
	 * 							is editable, <code>false</code> otherwise.
	 * @param deletable			Pass <code>true</code> if the node
	 * 							is deletable, <code>false</code> otherwise.
	 */
	AnnotationComponent(AnnotationData textualAnnotation, boolean editable)
	{
		this(textualAnnotation, editable, true);
	}
	
	/**
	 * Creates a new component.
	 * 
	 * @param textualAnnotation	The annotation to handle.
	 * @param editable			Pass <code>true</code> if the node
	 * 							is editable, <code>false</code> otherwise.
	 * @param deletable			Pass <code>true</code> if the node
	 * 							is deletable, <code>false</code> otherwise.
	 */
	AnnotationComponent(AnnotationData textualAnnotation, boolean editable,
						boolean deletable)
	{
		this.data = textualAnnotation;
		originalText = "";
		if (data != null) originalText = data.getContentAsString();
		initialize(editable, deletable);
		buildGUI();
	}
	
	/**
	 * Sets the background of the area, the background color
	 * will change when the user clicks on the node.
	 * 
	 * @param color The color to set.
	 */
	void setAreaColor(Color color)
	{
		area.setOriginalBackground(color);
		setBackground(color);
	}
	
	/** 
	 * Sets the border of the area.
	 * 
	 * @param title The title to display.
	 */
	void setAreaBorder(String title)
	{
		UIUtilities.setBoldTitledBorder(title, area);
	}
	
	/**
	 * Sets the background of the area, the background color
	 * will change when the user clicks on the node.
	 * 
	 * @param color The color to set.
	 */
	void setAreaBackground(Color color)
	{
		if (color == null) color = area.getOriginalBackground();
		area.setBackground(color);
		setBackground(color);
	}
	
	/**
	 * Saves or deletes the annotation.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int key = Integer.parseInt(e.getActionCommand());
		switch (key) {
			case SAVE:
				String text = area.getText();
				if (text != null) text = text.trim();
				firePropertyChange(SAVE_PROPERTY, null, text);
				
				break;
			case DELETE:
				firePropertyChange(DELETE_PROPERTY, null, data);
		}
	}

	/**
	 * Sets the <code>save</code> button enabled flag when text is inserted.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { handleTextChanged(); }

	/**
	 * Sets the <code>save</code> button enabled flag when text is inserted.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { handleTextChanged(); }
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation 
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
