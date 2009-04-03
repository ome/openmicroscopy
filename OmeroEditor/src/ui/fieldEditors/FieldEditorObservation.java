package ui.fieldEditors;


import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import tree.IDataFieldObservable;
import ui.components.InfoLabel;
import ui.components.OLSLinkPanel;

public class FieldEditorObservation extends FieldEditor {
	
	public FieldEditorObservation(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		String infoMessage = "<br>An observation is a Phenotype or Measurement you intend to "
			+ "score on multiple experimental samples. <br>" +
					"This can be defined by the Entity, Attribute, Units(optional) and observation 'Data Type'. " +
					"Use of Ontology terms is encouraged because ontologies are great! " +
					"For example<br>" +
					"<u>Time of anaphase onset:</u><br>" +
					"- Data Type: Integer<br>" +
					"- Entity: 'Metaphase/Anaphase transition'<br>" +
					"- Attribute: 'Time'<br>" +
					"- Units: 'minutes'<br>" +
					"<u>Normal metaphase alignment:</u><br>" +
					"- Data Type: True/False (boolean)<br>" +
					"- Entity: 'Metaphase plate congression<br>" +
					"- Attribute: 'Normal'<br>" +
					"- no units";
		
		JLabel infoLabel = new InfoLabel(infoMessage);
		
		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.add(infoLabel, BorderLayout.CENTER);
		
		attributeFieldsPanel.add(infoPanel);
		
		// link to the OLS web-site
		attributeFieldsPanel.add(new OLSLinkPanel());
	}
}
