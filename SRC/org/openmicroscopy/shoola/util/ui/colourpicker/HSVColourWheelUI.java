/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourPickerUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.ui.colourpicker;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.colour.HSV;

/** 
 * This is the UI Panel which contains the HSV elements including Colour
 * Wheel, Value slider and alpha slider. It is all incorporated as part of 
 * tabbedUI 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class HSVColourWheelUI 
	extends JPanel
	implements ChangeListener
{
	
	/** The preferred size of the wheel. */
	private static final Dimension WHEEL_SIZE = new Dimension(175, 175);
	
	/**
	 * Wheel is an HSVWheel which will display the HSV Colourwheel as an image. 
	 */
	private HSVWheel			wheel;
	
	/**
	 * HSVStrip displays the HSV value as a colour strip showing the range of
	 * colours which can be chosen, going from min to max value. 
	 */
	private ColourSlider 		HSVSlider;
		
	/**
	 * alphaStrip displays the alpha value as a colour strip showing the range 
	 * of colours which can be chosen, going from min to max value. 
	 */
	private ColourSlider 		alphaSlider;
		
	/** Displays the current value of the alphaStrip Slider. */
	private JTextField			alphaTextbox;

	/** Change listener of the HSVSlider. */
	private ChangeListener		HSVListener;
	
	/** Change listener of the alpha slider. */
	private ChangeListener		alphaListener;
	
	/** Action listener of the alpha textbox. */
	private ActionListener		alphaTextboxListener;
	
	/**
	 * Boolean variable, true if the current component is active, this is 
	 * controlled from the parent component. It tells the UI whether or not
	 * to ignore refresh events.
	 */
	private boolean 			active;
	
	/** Reference to the colour model. */
	private RGBControl 			control;
	
	/**
	 * Creates all the UI components of the panel. This includes, HSVSlider, 
	 * alphaSlider and colourwheel. It also initialises all the Change, Action
	 * listeners. 
	 */
	private void createUI()
	{
		createWheel();
		createHSVSlider();
		createAlphaSlider();
		createAlphaTextbox();	
		int width = HSVSlider.getPreferredSize().width;
		
		double[][] tl = {{TableLayout.PREFERRED, 5, width}, //columns
							{TableLayout.FILL, TableLayout.PREFERRED} }; //rows
		setLayout(new TableLayout(tl));
		JPanel empty = new JPanel();
        empty.setOpaque(true);
        add(wheel, "0, 0");
        add(empty, "1, 0");
        		add(HSVSlider, "2, 0");
        add(alphaSlider, "0, 1");
        empty = new JPanel();
        empty.setOpaque(true);
        add(empty, "1, 1");
        JPanel p = new JPanel();
        p.add(alphaTextbox);
        add(p, "2, 1, LEFT, TOP");
	}

	/**
	 * Creates the HSV Colour wheel, and adds the listener which will notify
     * this component when its been picked by the user.  
	 */
	private void createWheel()
	{
		wheel = new HSVWheel(control);
		wheel.addListener(this);
		wheel.setPreferredSize(WHEEL_SIZE);
	}
	
	/** 
	 * Creates the HSV Slider in the UI, this will construct the slider and
	 * instantiate the values of the slider, including the range of colours the
	 * slider will display in its track. 
	 */
	private void createHSVSlider()
	{
		HSV startcol = new HSV(control.getColour());
		HSV endcol = new HSV(control.getColour());
		startcol.setValue(0);
		endcol.setValue(1);
		HSVSlider = new ColourSlider(0, 255, startcol, endcol);
		HSVSlider.setColourSpace(ColourSlider.HSV_COLOURSPACE);
		HSVSlider.setChannel(ColourSlider.HSV_CHANNEL_VALUE);
		HSVSlider.setOrientation(ColourSlider.VERTICAL);
		HSVSlider.setValue((int) (control.getValue()*255));
		HSVListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent)
			{
				ColourSlider src = (ColourSlider) changeEvent.getSource();
				control.setHSVColour(wheel.getHue(), wheel.getSaturation(), 
								     src.getValue()/255.0f, 
								     alphaSlider.getValue()/255.0f);
			}
		};
		HSVSlider.addChangeListener(HSVListener);
	}
	
	/** 
	 * Creates the Alpha Slider in the UI, this will construct the slider and
	 * instantiate the values of the slider, including the range of colours the
	 * slider will display in its track. 
	 */
	private void createAlphaSlider()
	{
		Color s1 = control.getColour();
		Color s = new Color(s1.getRed(), s1.getGreen(), s1.getBlue(), 0);
		Color e = new Color(s1.getRed(), s1.getGreen(), s1.getBlue(), 255);
		alphaSlider = new ColourSlider(0, 255, s, e);
		alphaSlider.setColourSpace(ColourSlider.RGB_COLOURSPACE);
		alphaSlider.setOrientation(ColourSlider.HORIZONTAL);
		alphaSlider.setValue((int) (control.getAlpha()*255));
		alphaListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent)
			{
				control.setHSVColour(wheel.getHue(), wheel.getSaturation(), 
						        HSVSlider.getValue()/255.0f, 
						        alphaSlider.getValue()/255.0f);
			}
		};
		alphaSlider.addChangeListener(alphaListener);
	}
	
	/** 
	 * Creates the AlphaTextbox in the UI, this will construct the textbox and
	 * instantiates the values of the textbox. 
	 */
	private void createAlphaTextbox()
	{
		alphaTextbox = new JTextField((int) (control.getAlpha()*255)+"");
		alphaTextbox.setColumns(TabbedPaneUI.TEXTBOX_COLUMN);
		alphaTextboxListener = new ActionListener() {		
			public void actionPerformed(ActionEvent actionEvent) {
				JTextField src = (JTextField) actionEvent.getSource();
				try {
					int value = Integer.parseInt(src.getText());
					if (value >= 0 && value <= 255)
						control.setHSVColour(wheel.getHue(), 
											wheel.getSaturation(), 
								            HSVSlider.getValue()/255.0f, 
								             value/255.0f);
					else {
						ColourPicker.invalidColorValue();
						alphaTextbox.setText(""+(int) (control.getAlpha()*255));
					}
				} catch(NumberFormatException e) {
					ColourPicker.invalidColorValue();
					alphaTextbox.setText(""+(int) (control.getAlpha()*255));
				}
			}
		};
	}
	
	/**
	 * HSVPicker constructor, control c is the control, which also contains
	 * the a RGBModel.
	 * 
	 * @param c Reference to the control. Mustn't be <code>null</code>.
	 */
	HSVColourWheelUI(RGBControl c)
	{
        if (c == null)
            throw new NullPointerException("No control.");
		control = c;
		createUI();
		active = false;
	}

	/** Updates the UI components based on changes to the model. */
	void refresh() 
	{
		if (!(active)) return;
		removeListeners();
		alphaSlider.setValue((int) (control.getAlpha()*255));
		Color col = control.getColour();
		Color start = new Color(col.getRed(), col.getGreen(), col.getBlue(), 0);
		Color end = new Color(col.getRed(), col.getGreen(), col.getBlue(), 255);
		alphaSlider.setRGBStart(start);
		alphaSlider.setRGBEnd(end);
		alphaTextbox.setText((int) (control.getAlpha()*255)+"");
		
		wheel.findPuck();
		wheel.refresh();
		alphaTextbox.repaint();
		
		HSV startcol = new HSV(wheel.getHue(), wheel.getSaturation(), 1, 1);
		HSV endcol = new HSV(wheel.getHue(), wheel.getSaturation(), 0, 1);
		HSVSlider.setHSVStart(startcol);
		HSVSlider.setHSVEnd(endcol);
		HSVSlider.setValue((int) (control.getValue()*255));
		
		wheel.repaint();
		HSVSlider.repaint();
		alphaSlider.repaint();
		addListeners();
		repaint();
	}
	
	/**
	 * Calls the wheel find puck method {@link HSVWheel#findPuck()} which 
	 * will position the puck on the colourwheel based on the current colour
	 * select by user. 
	 */
	void findPuck() { wheel.findPuck(); }
	
	/**
	 * Adds change listeners to the components. Used in conjunction with 
	 * {@link #removeListeners()} to get round spurious event 
	 * triggers when updating components.
	 */
	void addListeners()
	{
		HSVSlider.addChangeListener(HSVListener);
		alphaSlider.addChangeListener(alphaListener);
		alphaTextbox.addActionListener(alphaTextboxListener);
	}

	/**
	 * Removes change listeners to the components. Used in conjunction with 
	 * {@link #addListeners()} to get round spurious event 
	 * triggers when updating components.
	 */
	void removeListeners()
	{
		HSVSlider.removeChangeListener(HSVListener);
		alphaSlider.removeChangeListener(alphaListener);
		alphaTextbox.removeActionListener(alphaTextboxListener);
	}
	
	/**
	 * Sets the current component is active or inactive, this is 
	 * controlled from the parent component. It tells the UI whether or not
	 * to ignore refresh events.
	 * 
	 * @param act  Pass <code>true</code> to activate the component,
     *              <code>false</code> otherwise.
	 */
	void setActive(boolean act) { active = act;	}

	/**
	 * Sets the Colour of the model, using the HS values from 
	 * the HSVWheel, Value from HSVSlider and alpha from alphaSlider.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) 
	{
		control.setHSVColour(wheel.getHue(), wheel.getSaturation(), 
				                HSVSlider.getValue()/255.0f, 
				                alphaSlider.getValue()/255.0f);
		refresh();
	}
	
}


