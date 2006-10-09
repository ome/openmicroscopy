/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourPickerUI
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.colourpicker;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
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
	
	/**
	 * Createw the HSV Colour wheel, and addw the listener which will notify
     * this component when its been picked by the user.  
	 */
	void createWheel()
	{
		wheel = new HSVWheel(control);
		wheel.addListener(this);
		wheel.setPreferredSize(new Dimension(175, 175));
	}
	
	/** 
	 * Creates the HSV Slider in the UI, this will construct the slider and
	 * instantiate the values of the slider, including the range of colours the
	 * slider will display in its track. 
	 */
	void createHSVSlider()
	{
		HSV startcol = new HSV(control.getColour());
		HSV endcol = new HSV(control.getColour());
		startcol.setValue(0);
		endcol.setValue(1);
		HSVSlider = new ColourSlider(0, 255, startcol, endcol);
		HSVSlider.setColourSpace(ColourSlider.HSV_COLOURSPACE);
		HSVSlider.setChannel(ColourSlider.HSV_CHANNEL_VALUE);
		HSVSlider.setOrientation(ColourSlider.VERTICAL);
		HSVSlider.setValue((int)(control.getValue()*255));
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
	void createAlphaSlider()
	{
		Color s1 = control.getColour();
		Color s = new Color(s1.getRed(),s1.getGreen(),s1.getBlue(),0);
		Color e = new Color(s1.getRed(),s1.getGreen(),s1.getBlue(),255);
		alphaSlider = new ColourSlider(0,255,s,e);
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
	void createAlphaTextbox()
	{
		alphaTextbox = new JTextField((int) (control.getAlpha()*255)+"");
		alphaTextboxListener = new ActionListener() {		
			public void actionPerformed(ActionEvent actionEvent) {
				JTextField src = (JTextField) actionEvent.getSource();
				try
				{
				int value = Integer.parseInt(src.getText());
				if (value>=0 && value<=255)
				{
					control.setHSVColour(wheel.getHue(), wheel.getSaturation(), 
							            HSVSlider.getValue()/255.0f, 
							             value/255.0f);
					}
				}
				catch(NumberFormatException e)
				{
				}
			}
		};
	}
	
	/**
	 * Creates all the UI components of the panel. This includes, HSVSlider, 
	 * alphaSlider and colourwheel. It also initialises all the Change, Action
	 * listeners. 
	 */
	void createUI()
	{
		createWheel();
		createHSVSlider();
		createAlphaSlider();
		createAlphaTextbox();	
        JPanel container = new JPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        container.setLayout(new GridBagLayout());
    	gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1600.0;
        gbc.weighty = 600.0;
        gbc.fill = gbc.BOTH;
    	//setLayout(new FlowLayout());
        container.add(wheel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 100;
        container.add(HSVSlider,gbc);
        //
        JPanel p = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0.0;  
        p.setLayout(new GridBagLayout());
        c.fill = gbc.VERTICAL;
    	p.add(alphaSlider, c);
        c.gridx = 1;
        p.add(alphaTextbox, c);
        gbc.fill = gbc.BOTH;
    	//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new GridBagLayout());
        gbc.gridx =0;
        gbc.gridy = 0;
        gbc.weightx = 100;
        gbc.weighty = 100;
        add(container,gbc);
        //add(UIUtilities.buildComponentPanel(p));
        gbc.weighty = 15;
        gbc.gridy = 1;
        add(p,gbc);
	}

	/** Updates UI components based on chahnges to the model. */
	void refresh() 
	{
		if (active)
		{
			removeListeners();
			alphaSlider.setValue((int) (control.getAlpha()*255));
			Color col = control.getColour();
			Color start = new Color(col.getRed(), col.getGreen(), 
					col.getBlue(), 0);
			Color end = new Color(col.getRed(), col.getGreen(),
					col.getBlue(), 255);
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
	}
	
	/**
	 * Calls the wheel find puck method {@link HSVWheel#findPuck()} which 
	 * will position the puck on the colourwheel based on the current colour
	 * select by user. 
	 */
	void findPuck() { wheel.findPuck(); }
	
	/**
	 * Adds change listeners to the components. Used in conjuntion with 
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
	 * Removes change listeners to the components. Used in conjuntion with 
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


