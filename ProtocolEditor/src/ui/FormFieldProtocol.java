package ui;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import tree.DataField;

public class FormFieldProtocol extends FormField {
	
	JLabel fileName;
	JLabel fileNameLabel;
	
	public FormFieldProtocol(DataField dataField) {
		
		super(dataField);
		
		nameLabel.setFont(XMLView.FONT_H1);
		leftIndent.setVisible(false);
		
		horizontalBox.add(horizontalBox.createGlue());
		
		fileName = new JLabel("Protocol File: ");
		visibleAttributes.add(fileName);
		fileName.setFont(XMLView.FONT_SMALL);
		horizontalBox.add(fileName);
		
		fileNameLabel = new JLabel(dataField.getAttribute(DataField.PROTOCOL_FILE_NAME));
		fileNameLabel.setFont(XMLView.FONT_SMALL);
		fileNameLabel.setMaximumSize(new Dimension(200, 20));
		horizontalBox.add(fileNameLabel);
		
		horizontalBox.add(Box.createHorizontalStrut(5));
		
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		fileNameLabel.setText(dataField.getAttribute(DataField.PROTOCOL_FILE_NAME));
	}

}
