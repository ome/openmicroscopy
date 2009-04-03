 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.OntologyTermEditor 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.OntologyTermParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomTextField;
import org.openmicroscopy.shoola.agents.editor.uiComponents.DropDownMenu;
import org.openmicroscopy.shoola.agents.editor.util.Ontologies;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;

/** 
 * This is a UI component for editing an Ontology term. Users can choose an
 * ontology from a list, then use an auto-complete field to enter a term
 * from that ontology. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class OntologyTermEditor 
	extends AbstractParamEditor 
	implements PropertyChangeListener,
	DocumentListener
{
	/**
	 * A drop-down menu for changing the ontology
	 */
	private DropDownMenu 			ontologyChooser; 
	
	/**
	 * Text component for entering the ontology term. 
	 * Document Listener on this field launches auto-complete. 
	 */
	private JTextComponent			termSelector;
	
	/**
	 * A popup to display the auto-complete terms from the ontology search. 
	 */
	HistoryDialog 					popup;

	/**
	 * Initialises the UI components. 
	 */
	private void initialise() 
	{
		// A list of ontologies in "GO   GeneOntology" format
		String[] ontologyList = Ontologies.getInstance().getOntologyNameList();
		ontologyChooser = new DropDownMenu(ontologyList);
		ontologyChooser.addPropertyChangeListener(DropDownMenu.SELECTION, this);
		
		termSelector = new CustomTextField(250);	// min width text field
	}
	
	/**
	 * Lays out the UI, setting the display of the Ontology term. 
	 */
	private void buildUI()
	{
		add(ontologyChooser);
		add(termSelector);
		
		IAttributes param = getParameter();
		String ontologyID = param.getAttribute(OntologyTermParam.ONTOLOGY_ID);
		String termID = param.getAttribute(OntologyTermParam.TERM_ID);
		String termName = param.getAttribute(OntologyTermParam.TERM_NAME);
		
		if (ontologyID != null) {
			ontologyChooser.setText(ontologyID);
		} else {
			ontologyChooser.setText("Ontology");
		}
		
		if (termID != null) {
			// this String is in the same format as provided by the Ontologies
			// class for auto-complete of this field. 
			termSelector.setText(ontologyID + ":" + termID + 
				Ontologies.ONTOLOGY_ID_NAME_SEPARATOR + 
				(termName == null ? "" : termName));
		}
		termSelector.getDocument().addDocumentListener(this);
	}
	
	/**
	 * This method is called by the DocumentListener on the 
	 * {@link #termSelector}.
	 * It adds auto-complete functionality, getting matching terms from the 
	 * Ontology Lookup Service and displaying them in the HistoryDialog.
	 */
	private void autoComplete() {
		Rectangle rect = termSelector.getBounds();
		String text = termSelector.getText();
		/*
		 * Don't auto-complete for less than 3 letters
		 */
		if (text.length() < 3) return;

		// get the ontology
		String ontologyID = ontologyChooser.getText();
		if ((ontologyID == null) || (ontologyID.length() == 0)) return;
		
		// get matching terms from OLS
		String[] matchingTerms = Ontologies.getTermsByName(text, ontologyID);
		if (matchingTerms.length == 0) return;
		
		// show in a pop-up
		popup = new HistoryDialog(matchingTerms, 300);
		popup.addPropertyChangeListener(
				HistoryDialog.SELECTION_PROPERTY, this);
		popup.show(ontologyChooser, 0, rect.height);
		
		// need focus back in text box, so user can keep typing. 
		termSelector.requestFocusInWindow();
	}
	
	/**
	 * Creates an instance of this UI. Builds UI and sets display to show
	 * current Ontology term. 
	 * 
	 * @param param
	 */
	public OntologyTermEditor(IAttributes param) {
		super(param);

		initialise();
		
		buildUI();
	}

	/**
	 * Implemented as specified by the {@link ITreeEditComp} interface.
	 * Returns "Ontology Term"
	 * 
	 * @see ITreeEditComp#getEditDisplayName();
	 */
	public String getEditDisplayName() {
		return "Ontology Term";
	}

	/**
	 * Implemented as specified by the {@link PropertyChangeListener} interface.
	 * Handles changes to the selection of Ontology by {@link #ontologyChooser}
	 * and choosing a term from the {@link #popup} auto-complete.
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		// Change in the ontology selected? 
		if (ontologyChooser.equals(evt.getSource())) {
			String ontology = evt.getNewValue() +"";
			String ontologyID = Ontologies.getOntologyIdFromOntology(ontology);
			//ontologyChooser.setText(ontologyID);
			
			// need to update term Name and ID.
			Map<String, String> newTerm = new HashMap<String, String>();
			newTerm.put(OntologyTermParam.ONTOLOGY_ID, ontologyID);
			newTerm.put(OntologyTermParam.TERM_ID, null);
			newTerm.put(OntologyTermParam.TERM_NAME, null);
			
			// need this to refresh the dialog parameter editor
			ontologyChooser.setText(ontologyID);
			termSelector.setText("");
			firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);
			
			// perform edit and add to undo/redo
			attributeEdited(OntologyTermParam.ONTOLOGY_ID, newTerm);
			
			return;
		}
		
		// Change in the term selected? 
		if (popup.equals(evt.getSource())) {
			
			String term = popup.getSelectedTextValue() + "";
			String termID = Ontologies.getTermIdFromB3(term);
			String termName = Ontologies.getTermNameFromB3(term);
			
			// need to update term Name and ID.
			Map<String, String> newTerm = new HashMap<String, String>();
			newTerm.put(OntologyTermParam.TERM_ID, termID);
			newTerm.put(OntologyTermParam.TERM_NAME, termName);
			
			// this is required to refresh the dialog parameter editor
			termSelector.getDocument().removeDocumentListener(this);
			termSelector.setText(popup.getSelectedTextValue() +"");
			termSelector.getDocument().addDocumentListener(this);
			firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);
			
			// perform edit and add to undo/redo
			attributeEdited(OntologyTermParam.TERM_ID, newTerm);
			
			return;
		}
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Launches the auto-complete dialog (calls {@link #autoComplete()}
	 * 
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {
		autoComplete();
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Launches the auto-complete dialog (calls {@link #autoComplete()}
	 * 
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) {
		autoComplete();
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Launches the auto-complete dialog (calls {@link #autoComplete()}
	 * 
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) {
		autoComplete();
	}

}
