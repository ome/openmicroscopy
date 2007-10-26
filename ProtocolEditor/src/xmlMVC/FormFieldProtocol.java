package xmlMVC;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

public class FormFieldProtocol extends FormField {
	
	public FormFieldProtocol(DataField dataField) {
		
		super(dataField);
		
		nameLabel.setFont(XMLView.FONT_H1);
		leftIndent.setVisible(false);
		
		horizontalBox.add(horizontalBox.createGlue());
		
		horizontalBox.add(Box.createHorizontalStrut(5));
		
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}
}
