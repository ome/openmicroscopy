/*
 * org.openmicroscopy.shoola.agents.rnd.model.ColorChooserManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.rnd.model;

//Java imports
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
class ColorChooserManager
	implements ActionListener, ChangeListener, FocusListener
{

	/** ID to handle events fired by the view {@link ColorChooser}. */
	private static final int			APPLY = 0;
	private static final int			SAVE = 1;
	private static final int			CANCEL = 2;
	private static final int			R_AREA = 3;
	private static final int			G_AREA = 4;
	private static final int			B_AREA = 5;
	
	private static final int			MAX_VALUE = 255;
	
	private static final int			widthBar = ColorPalette.WIDTH_BAR;
	
	private ColorChooser				view;
	private HSBPaneManager				hsbManager;
	private Color						colorSelected;
	private int[]						rgba;
	private int							alpha;
	
	/**
	 * Creates the manager.
	 * 
	 * @param view			reference to the view {@link ColorChooser}.
	 * @param hsbManager	referenve to {@link HSBPaneManager}.
	 * @param c				color saved in user settings.
	 * @param alpha			alpha component (GUI version).
	 */
	ColorChooserManager(ColorChooser view, HSBPaneManager hsbManager,
						int[] rgba, int alpha)
	{
		this.view = view;
		this.hsbManager = hsbManager;
		this.alpha = alpha;
		this.rgba = rgba;
		buildColor();
	}
	
	int[] getRGBA()
	{
		return rgba;
	}
	
	private void buildColor()
	{
		colorSelected = new Color(rgba[ColorChooser.RED], 
								rgba[ColorChooser.GREEN], 
								rgba[ColorChooser.BLUE],
								rgba[ColorChooser.ALPHA]);
	}
	
	/** Attach the listener. */
	void attachListeners()
	{
		view.getAlphaSlider().addChangeListener(this);
		JButton applyButton = view.getApplyButton(),
				saveButton = view.getApplyButton(),
				cancelButton = view.getCancelButton();
		applyButton.addActionListener(this);
		applyButton.setActionCommand(""+APPLY);
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		
		JTextField 	rArea = view.getRArea(),
					gArea = view.getGArea(),	
					bArea = view.getBArea();
		rArea.addActionListener(this);
		rArea.addFocusListener(this);
		rArea.setActionCommand(""+R_AREA);
		gArea.addActionListener(this);
		gArea.addFocusListener(this);
		gArea.setActionCommand(""+G_AREA);
		bArea.addActionListener(this);
		bArea.addFocusListener(this);
		bArea.setActionCommand(""+B_AREA);
	}
	
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int     index = Integer.parseInt(s);
			switch (index) { 
				case APPLY:
					applySettings();
					break;
				case CANCEL:
					cancel();
					break;
				case R_AREA:
					checkFieldValue(R_AREA);
					break;
				case G_AREA:
					checkFieldValue(G_AREA);
					break;
				case B_AREA:
					checkFieldValue(B_AREA);
			}// end switch  
		} catch(NumberFormatException nfe) {
		   throw nfe;  //just to be on the safe side...
		} 
	}
	   
	/** Handles the event fired by the slider. */
	public void stateChanged(ChangeEvent e)
	{
		JSlider source = (JSlider) e.getSource();
		updateAlpha(source.getValue());
	}
	
	/** 
	 * Handles the lost of focus on the text fields.
	 * If focus is lost while editing, then we don't consider the text
	 * currently displayed in the text fields and we reset it to the current 
	 * value.
	 */
	public void focusLost(FocusEvent e)
	{
		String valRed = view.getRArea().getText(), 
				red = ""+colorSelected.getRed();
		String valGreen = view.getGArea().getText(), 
				green = ""+colorSelected.getGreen();
		String	valBlue = view.getBArea().getText(), 
				blue = ""+colorSelected.getBlue();													
		if (valRed == null || !valRed.equals(red)) 
			view.getRArea().setText(red);
		if (valGreen == null || !valGreen.equals(green))
			view.getGArea().setText(green);
		if (valBlue == null || !valBlue.equals(blue)) 
			view.getBArea().setText(blue);
	}
    
	/** 
	 * Required by the FocusListener interface, 
	 * but empty as we don't need it. 
	 */
	public void focusGained(FocusEvent e) {} 
	
	/** 
	 * Applies the color settings to the wavelength. 
	 * */
	void applySettings()
	{
		//TODO: save and close the dialog
	}
	
	/** Close the widget. */
	void cancel()
	{
		//TODO: setVisible(false);
	}
	
	/** 
	 * Checks the value entered is valid, if yes forward it to updateColor() 
	 * method.
	 * 
	 * @param k		color component index.
	 */
	void checkFieldValue(int k)
	{
		boolean valid = false;
		int val = 0;
		JTextField	field = null;
		try {
			if (k == R_AREA) field = view.getRArea();
			else if (k == G_AREA) field = view.getGArea();
			else field = view.getBArea();	
			val =  Integer.parseInt(field.getText());
			if (0 <= val && val <= MAX_VALUE) valid = true;
		} catch(NumberFormatException nfe) {}		
		if (valid)     updateColor(val, k);  // will notify the controller
		else {
			field.selectAll();
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	/**
	 * Updates the color bars and cursors.
	 * 
	 * @param v		color component value.
	 * @param k		color component index.
	 */
	void updateColor(int v, int k)
	{
		if (k == R_AREA)
			colorSelected = new Color(v, colorSelected.getGreen(),
								colorSelected.getBlue(), alpha);
		else if (k == G_AREA)
			colorSelected = new Color(colorSelected.getRed(), v,
								colorSelected.getBlue(), alpha);
		else if (k == G_AREA)
			colorSelected = new Color(colorSelected.getRed(), 
								colorSelected.getGreen(), v, alpha);
								
		ColorPaletteManager cpm = view.getColorPalette().getManager();
		float[] vals = new float[3];								
		Color.RGBtoHSB(colorSelected.getRed(), colorSelected.getBlue(), 
						colorSelected.getGreen(), vals);
		cpm.setHSB(vals[0], vals[1], vals[2]);
		Color c = new Color(vals[0], 1f, 1f);
		view.getColorPalette().getBarH().setLineLocation(
					(int) (vals[0]*widthBar)+ColorPalette.leftBorder);
		ColorBar 	cBarS = view.getColorPalette().getBarS(),
					cBarB = view.getColorPalette().getBarB();	
		cBarS.setLineLocation((int) (vals[1]*widthBar)+ColorPalette.leftBorder);
		cBarS.setColor(c);
		cBarB.setLineLocation((int) (vals[2]*widthBar)+ColorPalette.leftBorder);																			
		cBarB.setColor(c);
		view.getColorPanel().setColor(colorSelected);
		view.repaint();
	}
	
	/** 
	 * Updates the alphaTextField and creates the color accordingly.
	 * 
	 * @param v		alpha's value. 
	 */
	void updateAlpha(int v)
	{
		view.getAlphaTextField().setText(""+v);
		//b/c Color instance expects a value in the range [0, 255].
		alpha = (int) (v*255/100); 
		Color c = new Color(colorSelected.getRed(), colorSelected.getGreen(),
							colorSelected.getBlue(), alpha);
		colorSelected = c;
		view.getColorPanel().setColor(c);
		view.repaint();
	}
	
	/** 
	 * Resets the color selected and updates the textfields accordingly.
	 * 
	 * @param c		color selected.
	 *  */
	void setColorPanel(Color c)
	{
		Color cD = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		colorSelected = cD;
		view.getColorPanel().setColor(cD);
		view.repaint();
		updateTextFields(cD);
	}
	
	/**
	 * Updates the textFields with the new color component values.
	 * 
	 * @param c		color selected.
	 */
	void updateTextFields(Color c)
	{
		view.getRArea().setText(""+c.getRed());
		view.getGArea().setText(""+c.getGreen());
		view.getBArea().setText(""+c.getBlue());
	}
	
}

