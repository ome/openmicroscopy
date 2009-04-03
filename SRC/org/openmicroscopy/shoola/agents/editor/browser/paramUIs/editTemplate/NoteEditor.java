 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.NoteEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate;

//Java imports

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.ExperimentInfoPanel;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.model.Note;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A panel for editing name and content of a step-note.
 * A {@link PropertyChangeListener} for the 
 * {@link ITreeEditComp#VALUE_CHANGED_PROPERTY} needs to be provided to handle
 * the editing (added to the nameEditor and contentEditor components);
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class NoteEditor 
	extends JPanel 
	implements ActionListener {
	
	/** The note we are editing */
	private Note 						note;
	
	/** The parent that listens for changes	*/
	private PropertyChangeListener 		parent;
	
	/**  A bound property indicating that this note should be deleted. */
	public static final String 			NOTE_DELETED = "noteDeleted";
	
	/**
	 * Builds the UI. 
	 */
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ExperimentInfoPanel.LIGHT_YELLOW);
		Border lineBorder = BorderFactory.createMatteBorder(1, 1, 1, 1,
                UIUtilities.LIGHT_GREY);
		setBorder(lineBorder);
		
		Border eb = new EmptyBorder(3,4,3,4);
		
		// add name field
		AttributeEditLine nameEditor = new AttributeEditNoLabel
												(note, Note.NAME, "Note Name");
		nameEditor.addPropertyChangeListener
								(ITreeEditComp.VALUE_CHANGED_PROPERTY, parent);
		nameEditor.setBackground(null);
		nameEditor.setBorder(eb);
		
		// A text box to edit content
		AttributeEditArea contentEditor = new AttributeEditArea(note, 
												Note.CONTENT, "Note Content");
		contentEditor.addPropertyChangeListener
								(ITreeEditComp.VALUE_CHANGED_PROPERTY, parent);
		contentEditor.setBorder(eb);
		
		
		//  tool bar (same as ParamToolBar), only holds the delete button 
		JToolBar rightToolBar = new JToolBar();
		rightToolBar.setBackground(null);
		rightToolBar.setFloatable(false);
		Border bottomLeft = BorderFactory.createMatteBorder(0, 1, 1, 0,
                UIUtilities.LIGHT_GREY);
		rightToolBar.setBorder(bottomLeft);
		
		// Delete note button
		IconManager iM = IconManager.getInstance();
		Icon delete = iM.getIcon(IconManager.DELETE_ICON_12);
		JButton deleteButton = new CustomButton(delete);
		deleteButton.addActionListener(this);
		deleteButton.setToolTipText("Delete this note");
		rightToolBar.add(deleteButton);
		
		Box titleToolBar = Box.createHorizontalBox();
		nameEditor.setAlignmentY(Component.TOP_ALIGNMENT);
		titleToolBar.add(nameEditor);
		rightToolBar.setAlignmentY(Component.TOP_ALIGNMENT);
		titleToolBar.add(rightToolBar);
		
		add(titleToolBar);
		add(contentEditor);
	}
	
	/**
	 * Creates an instance. 
	 * Builds the UI. 
	 * 
	 * @param note		The note we're editing
	 * @param parent	The parent that handles the edits. 
	 */
	public NoteEditor(Note note, PropertyChangeListener parent) {
		this.note = note;
		this.parent = parent;
		
		addPropertyChangeListener(NOTE_DELETED, parent);
		buildUI();
	}
	
	/**
	 * Returns the note that this class is editing. 
	 * 
	 * @return
	 */
	public Note getNote() { 	return note; }

	/**
	 * Implemented as specified by the {@link ActionListener} interface.
	 * Handles Delete-Note action.
	 * 
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
		firePropertyChange(NOTE_DELETED, false, true);
	}

}
