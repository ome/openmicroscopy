package ui.components;

import java.awt.Dimension;

import javax.swing.JComboBox;

public class CustomComboBox extends JComboBox {
	
	
	private int width;
	private int height;
	
	private int maximumWidth =0;
	
	public CustomComboBox(String[] items) {
		super(items);
		setBackground(null);
	}
	public CustomComboBox() {
		super();
		setBackground(null);
	}
	
	public void setMaximumWidth(int maxWidth) {
		maximumWidth = maxWidth;
		int h = (int)super.getPreferredSize().getHeight();
		
		setMaximumSize(new Dimension(maximumWidth, h));
	}

	public Dimension getPreferredSize() {
		
		//super.getLayout().layoutContainer(this);
		
		Dimension size = super.getPreferredSize();
			// getEditor().getEditorComponent().getPreferredSize();
			//super.getLayout().minimumLayoutSize(this);
			//super.getPreferredSize();
		if (maximumWidth == 0) {
			width = (int)size.getWidth() + 20;
		} else {
			width = maximumWidth;
		}
		height = (int)size.getHeight();
		
		// setMaximumSize(new Dimension(300, height));
		
		return new Dimension(width, height);
	}

}
