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

package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.Color;
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
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.IColorChooser;

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
	private static final int			CANCEL = 1;
	private static final int			R_AREA = 2;
	private static final int			G_AREA = 3;
	private static final int			B_AREA = 4;
	
	private static final int			MAX_VALUE = 255;
	
	private static final int			widthBar = ColorPalette.WIDTH_BAR;
	
	private ColorChooser				view;
	private IColorChooser				component;
	private Color						colorSelected;
	private int[]						rgba;
	private int							alpha;
	
	/** Channel index. */
	private int							index;
	
	/**
	 * Creates the manager.
	 * 
	 * @param view			reference to the view {@link ColorChooser}.
	 * @param component    	reference to UI component invoking the 
     *                      {@link ColorChooser} dialog.
	 * @param c				color saved in user settings.
	 */
	ColorChooserManager(ColorChooser view, IColorChooser component,
						int[] rgba, int index)
	{
		this.view = view;
		this.component = component;
		this.alpha = rgba[ColorChooser.ALPHA];
		this.rgba = rgba;
		this.index = index;
		initColor();
	}
	
	
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		int index = Integer.parseInt(s);
		try {
			switch (index) { 
				case APPLY:
					applySettings();
					break;
				case CANCEL:
					view.setVisible(false);
					break;
				case R_AREA:
					checkFieldValue(R_AREA);
					break;
				case G_AREA:
					checkFieldValue(G_AREA);
					break;
				case B_AREA:
					checkFieldValue(B_AREA);
					break;
			}// end switch  
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
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
		String valRed = view.rArea.getText(), 
				red = ""+colorSelected.getRed();
		String valGreen = view.gArea.getText(), 
				green = ""+colorSelected.getGreen();
		String	valBlue = view.bArea.getText(), 
				blue = ""+colorSelected.getBlue();													
		if (valRed == null || !valRed.equals(red)) 
			view.rArea.setText(red);
		if (valGreen == null || !valGreen.equals(green))
			view.gArea.setText(green);
		if (valBlue == null || !valBlue.equals(blue)) 
			view.bArea.setText(blue);
	}
    
	/** Applies the color settings to the wavelength. */
	void applySettings()
	{
		component.setColor(index, colorSelected);
		view.setVisible(false);
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
		JTextField field = null;
		try {
			if (k == R_AREA) field = view.rArea;
			else if (k == G_AREA) field = view.gArea;
			else field = view.bArea;	
			val =  Integer.parseInt(field.getText());
			if (0 <= val && val <= MAX_VALUE) valid = true;
		} catch(NumberFormatException nfe) {}		
		if (valid)	updateColor(val, k);  // will notify the controller
		else {
			field.selectAll();
			Registry registry = component.getRegistry();
			UserNotifier un = registry.getUserNotifier();
			un.notifyInfo("Invalid value", 
				"Please enter a value between 0 and "+MAX_VALUE);
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
								
		ColorPaletteManager cpm = view.cp.getManager();
		float[] vals = new float[3];								
		Color.RGBtoHSB(colorSelected.getRed(), colorSelected.getBlue(), 
						colorSelected.getGreen(), vals);
		cpm.setHSB(vals[0], vals[1], vals[2]);
		Color c = new Color(vals[0], 1f, 1f);
		view.cp.cbH.setLineLocation(
					(int) (vals[0]*widthBar)+ColorPalette.leftBorder);
		ColorBar cBarS = view.cp.cbS, cBarB = view.cp.cbB;	
		cBarS.setLineLocation((int) (vals[1]*widthBar)+ColorPalette.leftBorder);
		cBarS.setColor(c);
		cBarB.setLineLocation((int) (vals[2]*widthBar)+ColorPalette.leftBorder);																			
		cBarB.setColor(c);
		view.colorPanel.setColor(colorSelected);
		view.repaint();
	}
	
	/** 
	 * Updates the alphaTextField and creates the color accordingly.
	 * 
	 * @param v		alpha's value. 
	 */
	void updateAlpha(int v)
	{
		view.alphaTextField.setText(""+v);
		//b/c Color instance expects a value in the range [0, 255].
		alpha = (v*255/100); 
		Color c = new Color(colorSelected.getRed(), colorSelected.getGreen(),
							colorSelected.getBlue(), alpha);
		colorSelected = c;
		view.colorPanel.setColor(c);
		view.repaint();
	}
	
	/** 
	 * Resets the color selected and updates the textfields accordingly.
	 * 
	 * @param c		color selected.
	 */
	void setColorPanel(Color c)
	{
		Color cD = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		colorSelected = cD;
		view.colorPanel.setColor(cD);
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
		view.rArea.setText(""+c.getRed());
		view.gArea.setText(""+c.getGreen());
		view.bArea.setText(""+c.getBlue());
	}
	
    int[] getRGBA(){ return rgba; }
   
    /** Attach the listeners. */
    void attachListeners()
    {
        view.alphaSlider.addChangeListener(this);
        buttonListener(view.applyButton, APPLY);
        buttonListener(view.cancelButton, CANCEL);
        textFieldListener(view.rArea, R_AREA);
        textFieldListener(view.gArea, G_AREA);
        textFieldListener(view.bArea, B_AREA);
    }
    
    private void buttonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    private void textFieldListener(JTextField field, int id)
    {
        field.addActionListener(this);
        field.addFocusListener(this);
        field.setActionCommand(""+id);
    }
    
    
    /** initializes the color to be displayed. */
    private void initColor()
    {
        colorSelected = new Color(rgba[ColorChooser.RED], 
                                rgba[ColorChooser.GREEN], 
                                rgba[ColorChooser.BLUE],
                                rgba[ColorChooser.ALPHA]);
    }
    
	/** 
	 * Required by I/F but not actually needed in our case, 
	 * no op implementation.
	 */ 
	public void focusGained(FocusEvent e) {} 
	
}

