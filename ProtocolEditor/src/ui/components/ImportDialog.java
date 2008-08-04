 /*
 * ui.components.ImportDialog 
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

package ui.components;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.util.ui.TitlePanel;

import tree.ITreeModel;
import tree.Tree;
import ui.IModel;
import ui.SelectionObserver;
import ui.XMLUpdateObserver;

/** 
* This is the base class for imported dialogs.
* Used by the TextImporter and TableImporter.
*
* @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/

public abstract class ImportDialog 
	extends CustomDialog{

	/**
	 * A reference to the interface required for opening a newly created tree.
	 */
	IModel model;
	
	/**
	 * A reference to the root of the newly created tree. 
	 */
	ITreeModel tree;
	
	/**
	 * The JEditorPane used to display and edit the text to be imported.
	 */ 
	JTextComponent textArea;
	
	
	/**
	 * The container that holds the textArea(CENTER), to which
	 * subclasses can add eg tool bars. 
	 */
	protected JPanel textAndToolbarContainer;
	
	/**
	 * Creates a new instance of this class, but doesn't build and display UI.
	 * After calling this constructor, subclasses can use setter methods
	 * to set subTitle, headerMessage and headerIcon. 
	 * They must also instantiate 
	 * 
	 * @param model
	 * @param title
	 */
	public ImportDialog(IModel model, String title) {
		
		super(title);
		
		initialise(model);
		
	}
	
	
	/**
	 * This will build the UI, placing the textArea in a JScrollPane,
	 */
	public JComponent getDialogContent() {
		
		/*
		 * A JPanel with borderLayout, which allows subclasses to add 
		 * components between the titlePanel and the textArea.
		 * Eg. adding a toolBar to the EAST of the container. 
		 */
		textAndToolbarContainer = new JPanel(new BorderLayout());
		
		
		/*
		 * The text area should have been instantiated. 
		 * But, if not, create a JTextArea here. 
		 */
		if (textArea == null) 
			textArea = initialiseTextArea();
		textArea.addFocusListener(new TextAreaFocusListener());
		
		// put the text area in a scroll pane
		Dimension scrollPaneSize = new Dimension(PANEL_WIDTH, 350);
		textArea.setMaximumSize(scrollPaneSize);
		JScrollPane scrollPane = new JScrollPane(textArea, 
				 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(scrollPaneSize);
		scrollPane.setMinimumSize(scrollPaneSize);
		
		textAndToolbarContainer.add(scrollPane, BorderLayout.CENTER);
		
		return textAndToolbarContainer;
	}
	
	/**
	 * Called by constructor. 
	 * Creates a new Tree, with the model as it's selection and update observer.
	 * 
	 * @param model		The IModel that the new tree will notify of updates.
	 * 					So, after it is presented in the UI, it will behave
	 * 					correctly. 
	 */
	public void initialise(IModel model) {
		this.model = model;
		
		SelectionObserver sO = (SelectionObserver)model;
		XMLUpdateObserver xO = (XMLUpdateObserver)model;
		
		tree = new Tree(sO, xO);
	}
	
	/**
	 * Abstract method to initialise the JTextComponent textArea. 
	 * This method can also be used as a place to set the
	 * subTitle, headerMessage and headerIcon. 
	 */
	public abstract JTextComponent initialiseTextArea();
	
	/**
	 * A focus listener added to the textArea.
	 * The first time that this component gains focus, all the text
	 * will become selected.
	 * This makes it easier for the user to paste their text in. 
	 * 
	 * @author will
	 *
	 */
	public class TextAreaFocusListener implements FocusListener {
		
		boolean selectedOnce = false;
		
		public void focusGained(FocusEvent e) {
			if (selectedOnce) return;
			
			if (e.getSource() instanceof JTextComponent) {
				JTextComponent source = (JTextComponent)e.getSource();
				int textLength = source.getText().length();
				source.setSelectionStart(0);
				source.setSelectionEnd(textLength);
				selectedOnce = true;
			}
		}
		public void focusLost(FocusEvent e) {}
		
	}
	
}

