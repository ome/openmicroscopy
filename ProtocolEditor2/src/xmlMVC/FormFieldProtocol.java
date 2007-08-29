package xmlMVC;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import org.w3c.dom.Element;

public class FormFieldProtocol extends FormField {
	
	JLabel fileName;
	JLabel fileNameLabel;
	
	public FormFieldProtocol(DataField dataField) {
		
		super(dataField);
		
		String protocolFileName = dataField.getAttribute(DataField.PROTOCOL_FILE_NAME);
		
		nameLabel.setFont(XMLView.FONT_H1);
		leftIndent.setVisible(false);
		
		horizontalBox.add(horizontalBox.createGlue());
		
		fileName = new JLabel("Protocol File: ");
		fileName.setFont(XMLView.FONT_SMALL);
		horizontalBox.add(fileName);
		
		fileNameLabel = new JLabel(protocolFileName);
		fileNameLabel.setFont(XMLView.FONT_SMALL);
		fileNameLabel.setMaximumSize(new Dimension(200, 20));
		horizontalBox.add(fileNameLabel);
		
		horizontalBox.add(Box.createHorizontalStrut(5));
		
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}
	
//	 overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdatedOtherAttributes() {
		fileNameLabel.setText(dataField.getAttribute(DataField.PROTOCOL_FILE_NAME));
	}

}
