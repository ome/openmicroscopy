package ui.components;

import javax.swing.JLabel;

import ui.XMLView;

public class InfoLabel extends JLabel {
	
	public InfoLabel() {
		super();
		this.setFont(XMLView.FONT_SMALL);
	}
	
	public InfoLabel(String text) {
		super(addHtmlTags(text));
		this.setFont(XMLView.FONT_SMALL);
	}
	
	public void setText(String text) {
		super.setText(addHtmlTags(text));
	}

	public static String addHtmlTags(String text) {
		return "<html>" + text + "</html>";
	}
}
