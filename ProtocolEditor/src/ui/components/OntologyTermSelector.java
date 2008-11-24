package ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import ols.Ontologies;
import ols.OntologyLookUp;

import tree.DataField;
import tree.IAttributeSaver;
import ui.formFields.FormField;

public class OntologyTermSelector extends JPanel {
	
	IAttributeSaver dataField;
	String attributeId;
	
	JLabel nameLabel;
	
	CustomComboBox ontologySelector;
	CustomComboBox ontologyTermSelector;
	int listIndex = 0;
	OntologyTermKeyListener ontologyTermListener;
	TermSelectionListener termSelectionListener;
	FocusListener componentFocusListener = new ComponentFocusListener();
	
	public static final String ONTOLOGY_ID_NAME_SEPARATOR = "   ";

	public OntologyTermSelector(IAttributeSaver dataField, String attributeId, String termLabel) {
		
		
		/* need a String[] of ontology Id-Name pairs
		 * get a map of these from my Ontologies class
		 * then convert to String array. 
		 */
		
		LinkedHashMap<String, String> allOntologies = Ontologies.getInstance().getSupportedOntologies();
		
		String[] ontologyIds = new String[allOntologies.size()];
		String[] ontologyNames = new String[allOntologies.size()];
		
		// copy map to array
		int index=0;
		for (Iterator i = allOntologies.keySet().iterator(); i.hasNext();){
			String key = (String) i.next();
			String name = allOntologies.get(key);
			ontologyIds[index] = key;
			ontologyNames[index] = key + ONTOLOGY_ID_NAME_SEPARATOR + name;
			index++;
		}
		
		buildPanel(dataField, attributeId, termLabel, ontologyNames);
		
	}
	
	public OntologyTermSelector(IAttributeSaver dataField, String attributeId, String termLabel, String[] ontologies) {
		buildPanel(dataField, attributeId, termLabel, ontologies);
	}
	
	
	
	protected void buildPanel(IAttributeSaver dataField, String attributeId, String termLabel, String[] ontologies) {
		
		this.setLayout(new BorderLayout());
		this.setBackground(null);
		
		this.dataField = dataField;
		this.attributeId = attributeId;

		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBackground(null);
	
		nameLabel = new JLabel(termLabel);
		
	
		// make a new comboBox with the ontology Names
		ontologySelector = new CustomComboBox(ontologies);
		ontologySelector.addFocusListener(componentFocusListener);
		ontologySelector.setMaximumRowCount(25);
		ontologySelector.setMaximumWidth(200);
		//ontologySelector.setMaximumSize(new Dimension(200, 50));
		
		// make an editable comboBox (auto-complete) for ontology Terms
		ontologyTermSelector = new CustomComboBox();
		ontologyTermSelector.setEditable(true);
		ontologyTermSelector.getEditor().getEditorComponent().
				addFocusListener(componentFocusListener);
		ontologyTermSelector.setMaxPreferredWidth(400);
		ontologyTermListener = new OntologyTermKeyListener();
		ontologyTermSelector.getEditor().getEditorComponent().
				addKeyListener(ontologyTermListener);
		termSelectionListener = new TermSelectionListener();
		ontologyTermSelector.addActionListener(termSelectionListener);
		JPanel ontologyTermSelectorPanel = new JPanel(new BorderLayout());
		ontologyTermSelectorPanel.setBackground(null);
		ontologyTermSelectorPanel.add(ontologyTermSelector, BorderLayout.WEST);

		
		horizontalBox.add(nameLabel);
		horizontalBox.add(ontologySelector);
		horizontalBox.add(ontologyTermSelectorPanel);
		
		this.add(horizontalBox, BorderLayout.WEST);
		
		// get value from dataField and display. 
		dataFieldUpdated();
	}
	
	public void dataFieldUpdated() {
		// this has term ID and term Name, joined with ONTOLOGY_ID_NAME_SEPARATOR
		String termIdName = dataField.getAttribute(attributeId);
		
		ontologyTermSelector.removeActionListener(termSelectionListener);
		
		if (termIdName == null) {
			ontologyTermSelector.setSelectedItem("");
		} else {
			setCurrentOntology(Ontologies.getOntologyIdFromTermId(termIdName));
			ontologyTermSelector.setSelectedItem(termIdName);
			ontologyTermSelector.validate();
		}
		
		ontologyTermSelector.addActionListener(termSelectionListener);
	}
	
	/*
	 * KeyListener for auto-complete typing into ontologyTermSelector
	 * if > 3 letters, getTermsById() from OntologyLookUp
	 * use the Map of terms to populate the drop-down menu
	 */
	public class OntologyTermKeyListener implements KeyListener {
		
		
		public void keyReleased(KeyEvent event) {

			int keyCode = event.getKeyCode();
			
			// System.out.println(keyCode);
			
			// ignore events from the Enter key, since this will be processed by ActionListener
			if ((keyCode == Event.ENTER)) {	// return key
				if (listIndex > -1)
				ontologyTermSelector.setSelectedItem(ontologyTermSelector.getItemAt(listIndex));
				return;
			}
				 
			// use a listIndex to keep track of user moving up and down the popupMenu with keys
			// Can't find any other way of keeping track of highlighted item, since calling
			// getSelectedItem() always returns the item in the editableTextBox.
			// If the user hits enter (see above) the item at the current index is 
			// loaded into the editable text box, using setSelectedItem();
			if (keyCode == KeyEvent.VK_DOWN) {
				if (listIndex < ontologyTermSelector.getItemCount()-1)
				listIndex++;
				return;
			}
			if (keyCode == KeyEvent.VK_UP) {
				if (listIndex >1)
					listIndex--;
				return;
			}
			
			// ignore left and right buttons
			if ((keyCode == KeyEvent.VK_RIGHT) || (keyCode == KeyEvent.VK_LEFT)) 
				return;
			
			JTextComponent source = (JTextComponent)event.getSource();
			String input = source.getText();
			
			// don't auto-complete unless 3 characters have been entered
			if (input.length() < 3) return;
			
			ontologyTermSelector.removeActionListener(termSelectionListener);
			ontologyTermSelector.removeAllItems();
			
			// reset this, since items have changed. 
			listIndex = 0;
			
			//ontologyTermSelector.addItem(input);

			// don't auto-complete unless 3 characters have been entered
			//if (input.length() < 3) return;
			
			String currentOntologyId = getCurrentOntologyId();
			
			Map autoCompleteOptions = OntologyLookUp.getTermsByName(input, currentOntologyId);
			
			for (Iterator i = autoCompleteOptions.keySet().iterator(); i.hasNext();){
				String key = (String) i.next();
				String name = key + ONTOLOGY_ID_NAME_SEPARATOR + autoCompleteOptions.get(key).toString();
				ontologyTermSelector.addItem(name);
			}
			ontologyTermSelector.setPopupVisible(true);

			//ontologyTermSelector.getPopupMenuListeners();
			source.setText(input);
			ontologyTermSelector.addActionListener(termSelectionListener);
		}
		public void keyPressed(KeyEvent arg0) {}
		public void keyTyped(KeyEvent event) {}
	}
	
	

	public class TermSelectionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			
			String selection = ontologyTermSelector.getSelectedItem().toString();
			//System.out.println(ontologyTermSelector.getSelectedItem().toString());
			
			JComboBox sourceComboBox = (JComboBox)event.getSource();
			// System.out.println(sourceComboBox.getModel().getSelectedItem().toString());
			
			/*
			 * if the user hits enter to select the first item in auto-complete list,
			 *  the selection (selectedItem) will simply be the 
			 * stuff they have typed in (not an item from the list)
			 * A test for this is whether it contains the ID_NAME_SEPARATOR, contained by all list items.
			 */
			if (!selection.contains(ONTOLOGY_ID_NAME_SEPARATOR)) {
				// just use the first item from the list - if it's not empty (should be highlighted)
				if (sourceComboBox.getItemCount() > 0) {
					selection = sourceComboBox.getItemAt(0).toString();
				}
				
				// now, remove listeners, in order to manually set the selected item
				sourceComboBox.getEditor().getEditorComponent().removeKeyListener(ontologyTermListener);
				sourceComboBox.removeActionListener(termSelectionListener);
				
				// and select the item    so far so good. 
				sourceComboBox.setSelectedItem(selection);
				
				// now add back listeners
				// these both fire, but this doesn't do much (keyListener gets return key). 
				sourceComboBox.getEditor().getEditorComponent().addKeyListener(ontologyTermListener);
				sourceComboBox.addActionListener(termSelectionListener);
				
			}
			
			// would be nice to do this when user uses mouse to select item,
			// because this causes the selected item and the listIndex to get out of sync. 
			// BUT the line below would incorrectly reset listIndex when "Enter" is hit
			// because getSelectedIndex() would return the LAST selected item (not the new one).
			// This fails (BUG) if the user selects using the up and down keys after using the mouse
			// for a previous selection, because the listIndex has become out of sync.
			//listIndex = sourceComboBox.getSelectedIndex();
				
			// would be better to do this when focus lost,
			// since this actionPerformed seems to fire several times (2 or 3 times).
			dataField.setAttribute(attributeId, selection, true);
		}
	}
	
	public String getCurrentOntologyId() {
		
		String ontologyId = ontologySelector.getSelectedItem().toString();
		
		return getOntologyIdFromIdAndName(ontologyId);
	}
	
	public static String getOntologyIdFromIdAndName(String IdAndName) {
		// ontologyIds in ontologySelector may be eg "PATO", or may be "PATO	patoName"
		int idNameSeparatorIndex = IdAndName.indexOf(ONTOLOGY_ID_NAME_SEPARATOR);
		if(idNameSeparatorIndex > 0) {
			IdAndName = IdAndName.substring(0, idNameSeparatorIndex);
		}
		return IdAndName;
	}
	
	public void setCurrentOntology(String ontologyId) {
		
		if (ontologyId == null)
			return;
		
		int termCount = ontologySelector.getItemCount();
		for (int i=0; i<termCount; i++) {
			if(ontologySelector.getItemAt(i).toString().startsWith(ontologyId)) {
				ontologySelector.setSelectedIndex(i);
				return;
			}
		}
	}
	
	public void setOntologyComboBoxMaxWidth(int width) {
		ontologySelector.setMaximumWidth(width);
	}
	
	public void setLabelMinWidth(int width) {
		int h = (int)nameLabel.getPreferredSize().getHeight();
		
		nameLabel.setPreferredSize(new Dimension(width, h));
		nameLabel.setMinimumSize(new Dimension(width, h));
	}
	
	public void removeAllItems() {
		ontologyTermSelector.removeActionListener(termSelectionListener);
		ontologyTermSelector.removeKeyListener(ontologyTermListener);
		ontologyTermSelector.removeAllItems();
		ontologyTermSelector.addActionListener(termSelectionListener);
		ontologyTermSelector.addKeyListener(ontologyTermListener);
	}
	
	public Object getSelectedItem() {
		return ontologyTermSelector.getSelectedItem();
	}
	public void setSelectedItem(Object anObject) {
		ontologyTermSelector.removeActionListener(termSelectionListener);
		ontologyTermSelector.removeKeyListener(ontologyTermListener);
		ontologyTermSelector.setSelectedItem(anObject);
		ontologyTermSelector.addActionListener(termSelectionListener);
		ontologyTermSelector.addKeyListener(ontologyTermListener);
	}
	
	public class ComponentFocusListener implements FocusListener {
		public void focusGained(FocusEvent e) {
			OntologyTermSelector.this.firePropertyChange(FormField.HAS_FOCUS, false, true);
		}
		public void focusLost(FocusEvent e) {}
	}
	
	/**
	 * Enable or disable all the components of the OntologyTermSelector
	 */
	public void setEnabled(boolean enabled) {
		ontologySelector.setEnabled(enabled);
		ontologyTermSelector.setEnabled(enabled);
	}
	
}
