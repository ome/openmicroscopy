/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourSliderUI
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * ColourSliderUI is the RGB Slider component, which allows the user to change
 * the RGB values of the colour using the JSlider component. It also allows the
 * user to change the text box associated with each slider to do the same.  
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $$Revision: $$Date: $$)
 * </small>
 * @since OME2.2
 */
class RGBSliderUI
	extends JPanel
{
	
	/** The slider representing the red channel. */
	private ColourSlider 				redSlider;
	
    /** The slider representing the green channel. */
	private ColourSlider 				greenSlider;
	
    /** The slider representing the blue channel. */
	private ColourSlider 				blueSlider;
    
    /** The slider representing the alpha channel. */
	private ColourSlider 				alphaSlider;
	
	/** Change listener for red slider. */
	private ChangeListener				redChangeListener;

	/** Change listener for green slider. */
	private ChangeListener				greenChangeListener;
	
	/** Change listener for blue slider. */
	private ChangeListener				blueChangeListener;
	
	/** Change listener for alpha slider.  */
	private ChangeListener				alphaChangeListener;
	
	/** Action listener for red text box. */
	private ActionListener				redTextboxActionListener;
	
	/** Action listener for green text box. */
	private ActionListener				greenTextboxActionListener;
	
	/** Action listener for Blue textbox.  */
	private ActionListener				blueTextboxActionListener;
	
	/** Action listener for alpha TextBox.  */
	private ActionListener				alphaTextboxActionListener;
	
	/**
	 * Boolean variable, true if the current component is active, this is 
	 * controlled from the parent component. It tells the UI whether or not
	 * to ignore refresh events.
	 */
	private boolean						active;
	
	/**
	 * The textfield representing the colour red channel.
	 */
	private JTextField					redTextbox;
	
    /**
     * The textfield representing the colour green channel.
     */
	private JTextField					greenTextbox;
	
    /**
     * The textfield representing the blue red channel.
     */
	private JTextField					blueTextbox;
	
    /**
     * The textfield representing the alpha red channel.
     */
	private JTextField					alphaTextbox;
	
	/** The JLabel representing the colour red channel. */
	private JLabel						redLabel;
	
    /** The JLabel representing the colour green channel. */
	private JLabel						greenLabel;
	
    /** The JLabel representing the colour blue channel. */
	private JLabel						blueLabel;
	
    /** The JLabel representing the colour alpha channel. */
	private JLabel						alphaLabel;
	
	/** Current layout model, which is a gridbag controller.  */
	private GridBagLayout				layout;
	
	/** Model of the contoller. */
	private RGBControl					control;
	
	/**
	 * Creates a new instance and keeps a reference to the control c.
	 *  
	 * @param c Reference to the control. Mustn't be <code>null</code>.
	 */
	RGBSliderUI(RGBControl c)
	{
        if (c == null)
            throw new NullPointerException("No control.");
		control = c;
		createUI();
		active = false;
	}
	
	/**
	 * Sets the current component Active, called from parent control letting 
	 * this component know it should listen to refresh events. 
	 * 
	 * @param act active or not.
	 */
	void setActive(boolean act) { active = act; }
	
	/**
	 * Creates the Red slider, and changes listener, attaching the change
	 * listener to the slider. 
	 */
	void createRedSlider()
	{
		Color s1,s;
		Color e;
		
		s1 = control.getColour();
		s = new Color(0,s1.getGreen(), s1.getBlue(), 255);
		e = new Color(255,s1.getGreen(), s1.getBlue(), 255);
		
		redSlider = new ColourSlider(0, 255, s, e);
		redSlider.setValue((int) (control.getRed()*255.0));
		redChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent)
			{
				ColourSlider src = (ColourSlider) changeEvent.getSource();
				control.setRed(src.getValue()/255.0f);
			}
		};
		
		redSlider.addChangeListener(redChangeListener);
	}

	/**
	 * Creates the Green slider, and change listener, attaching the change
	 * listener to the slider. 
	 */ 
	void createGreenSlider()
	{
		Color s1,s;
		Color e1,e;
	
		s1 = control.getColour();
		s = new Color(s1.getRed(), 0, s1.getBlue(), 255);
		e1 = control.getColour();
		e = new Color(e1.getRed(), 255, s1.getBlue(), 255);
		
		greenSlider = new ColourSlider(0, 255, s, e);
		greenSlider.setValue((int) (control.getGreen()*255.0));
		
		greenChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent)
		{
			ColourSlider src = (ColourSlider) changeEvent.getSource(); 
			control.setGreen(src.getValue()/255.0f);
		}
		};
		greenSlider.addChangeListener(greenChangeListener);
		
	}

	/**
	 * Creates the Blue slider, and changes listener, attaching the change
	 * listener to the slider. 
	 */
	void createBlueSlider()
	{
		Color s1,s;
		Color e1,e;
			
		s1 = control.getColour();
		s = new Color(s1.getRed(), s1.getGreen(), 0, 255);
		e1 = control.getColour();
		e = new Color(e1.getRed(), s1.getGreen(), 255, 255);
		
		blueSlider = new ColourSlider(0, 255, s, e);
		blueSlider.setValue((int) (control.getBlue()*255.0));
		blueChangeListener = new ChangeListener() {
				public void stateChanged(ChangeEvent changeEvent)
			{
				ColourSlider src = (ColourSlider) changeEvent.getSource(); 
				control.setBlue(src.getValue()/255.0f);
			}
		};
		
		blueSlider.addChangeListener(blueChangeListener);
	}
	
	/**
	 * Creates the Alpha slider, and changes listener, attaching the change
	 * listener to the slider. 
	 */
	void createAlphaSlider()
	{
		Color s1,s;
		Color e1,e;
	
		s1 = control.getColour();
		s = new Color(s1.getRed(), s1.getGreen(), s1.getBlue(), 0);
		e1 = control.getColour();
		e = new Color(e1.getRed(), s1.getGreen(), s1.getBlue(), 255);
		
		alphaSlider = new ColourSlider(0, 255, s, e);
		alphaSlider.setValue((int) (control.getAlpha()*255.0));
		alphaChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent)
		{
			ColourSlider src = (ColourSlider) changeEvent.getSource(); 
			control.setAlpha(src.getValue()/255.0f);
		}
		};
	
		alphaSlider.addChangeListener(alphaChangeListener);
	}
	
	/**
	 * Creates the Red textbox, and action listener, attaching the action
	 * listener to the textbox. 
	 */
	void createRedTextbox()
	{
		redTextbox = new JTextField(""+(int) (control.getRed()*255));
		redTextboxActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent actionEvent) {
				JTextField src = (JTextField) actionEvent.getSource();
				try
				{
				int value = Integer.parseInt(src.getText());
				if (value>=0 && value<=255)
					control.setRed(value/255.0f);
				}
				catch(NumberFormatException e) {}
			}
		};
		redTextbox.addActionListener(redTextboxActionListener);	
	}
	
	/**
	 * Creates the Green textbox, and action listener, attaching the action
	 * listener to the textbox. 
	 */
	void createGreenTextbox()
	{
		greenTextbox = new JTextField(""+(int) (control.getGreen()*255));
		greenTextboxActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent actionEvent) {
				JTextField src = (JTextField) actionEvent.getSource();
				try
				{
				int value = Integer.parseInt(src.getText());
				if (value>=0 && value<=255)
					control.setGreen(value/255.0f);
				}
				catch(NumberFormatException e) {}
			}
		};
		greenTextbox.addActionListener(greenTextboxActionListener);
		
	}
	
	/**
	 * Creates the Blue textbox, and action listener, attaching the action
	 * listener to the textbox. 
	 */
	void createBlueTextbox()
	{

		blueTextbox = new JTextField(""+(int) (control.getBlue()*255));
		blueTextboxActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent actionEvent) {
				JTextField src = (JTextField) actionEvent.getSource();
				try
				{
				int value = Integer.parseInt(src.getText());
				if (value>=0 && value<=255)
					control.setBlue(value/255.0f);
				}
				catch(NumberFormatException e) {}
			}
		};
		blueTextbox.addActionListener(blueTextboxActionListener);
	}
	
	/**
	 * Creates the Alpha textbox, and action listener, attaching the action
	 * listener to the textbox. 
	 */
	void createAlphaTextbox()
	{
		alphaTextbox = new JTextField(""+(int) (control.getAlpha()*255));
		alphaTextboxActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent actionEvent) {
				JTextField src = (JTextField) actionEvent.getSource();
				try
				{
				int value = Integer.parseInt(src.getText());
				if (value>=0 && value<=255)
					control.setAlpha(value/255.0f);
				}
				catch(NumberFormatException e) {}
			}
		};
		alphaTextbox.addActionListener(alphaTextboxActionListener);
	}
	
	/** Creates labels, red, green, blue and alpha. */
	void createLabels()
	{
		redLabel = new JLabel("Red");
		greenLabel = new JLabel("Green");
		blueLabel = new JLabel("Blue");
		alphaLabel = new JLabel("Alpha");
	}
	
	/** Lays out the components previously created in createUI. */
	void layoutComponents()
	{
		layout = new GridBagLayout();
		this.setLayout(layout);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(redLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(redSlider, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 20;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(redTextbox,gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(greenLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(greenSlider, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 20;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(greenTextbox,gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(blueLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(blueSlider, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.weightx = 20;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(blueTextbox,gbc);
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(alphaLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(alphaSlider, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.weightx = 20;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,5,0,5);
		
		this.add(alphaTextbox,gbc);
	
	}
	
	/**
     * Creates slider UI, which consists of 4 JSliders, textfields and JLabels.
	 */
	void createUI()
	{
		createRedSlider();
		createGreenSlider();
		createBlueSlider();
		createAlphaSlider();
		
		createRedTextbox();
		createGreenTextbox();
		createBlueTextbox();
		createAlphaTextbox();
		
		createLabels();
		
		layoutComponents();
	}
	
	/**
	 * Updates the red slider of the UI, including changing the colour gradient
	 * of the slider tracks.
	 */
	void updateRedSlider()
	{
		Color s,s1,e;
		redSlider.setValue((int) (control.getRed()*255));
		s1 = control.getColour();
		s = new Color(0,s1.getGreen(), s1.getBlue(), 255);
		
		e = new Color(255,s1.getGreen(), s1.getBlue(), 255);
		redSlider.setRGBStart(s);
		redSlider.setRGBEnd(e);
	}
	
	/**
	 * Updates the green slider of the UI, including changing the colour
     * gradient of the slider tracks.
	 */
	void updateGreenSlider()
	{
		Color s,s1,e;
		greenSlider.setValue((int) (control.getGreen()*255));
		s1 = control.getColour();
		s = new Color(s1.getRed(), 0, s1.getBlue(), 255);
		
		e = new Color(s1.getRed(), 255, s1.getBlue(), 255);
		greenSlider.setRGBStart(s);
		greenSlider.setRGBEnd(e);
	}
	
	/**
	 * Updates the blue slider of the UI, including changing the colour gradient
	 * of the slider tracks.
	 */
	void updateBlueSlider()
	{
		Color s, s1, e;
		blueSlider.setValue((int) (control.getBlue()*255));
		s1 = control.getColour();
		s = new Color(s1.getRed(), s1.getGreen(), 0, 255);
		
		e = new Color(s1.getRed(), s1.getGreen(), 255, 255);
		blueSlider.setRGBStart(s);
		blueSlider.setRGBEnd(e);	
	}
	
	/**
	 * Updates the alpha slider of the UI, including changing the colour 
     * gradient of the slider tracks.
	 */
	void updateAlphaSlider()
	{
		Color s, s1, e;
		alphaSlider.setValue((int) (control.getAlpha()*255));
		s1 = control.getColour();
		s = new Color(s1.getRed(), s1.getGreen(), s1.getBlue(), 0);
		
		e = new Color(s1.getRed(), s1.getGreen(), s1.getBlue(), 255);
		alphaSlider.setRGBStart(s);
		alphaSlider.setRGBEnd(e);
	}
	
	/**
	 * Updates the sliders of the UI, including changing the colour gradient
	 * of the different slider tracks.
	 */
	void updateSliders()
	{
		updateRedSlider();
		updateBlueSlider();
		updateGreenSlider();
		updateAlphaSlider();
	}

	/** Updates the textboxes of the UI.  */
	void updateTextboxes()
	{		
		redTextbox.setText(""+(int) (control.getRed()*255));
		greenTextbox.setText(""+(int) (control.getGreen()*255));
		blueTextbox.setText(""+(int) (control.getBlue()*255));
		alphaTextbox.setText(""+(int) (control.getAlpha()*255));
	}
	
	/** 
	 * Refresh method will be called by tabbedpanelUI when the model has 
	 * changed.
	 */
	void refresh() 
	{
		if (active)
		{
			removeListeners();
			updateSliders();
			updateTextboxes();
			addListeners();
			repaint();
		}
	}

	/**
	 * Adds change listeners to the components. Used in conjuntion with 
	 * {@link #removeListeners()} to get round spurious event 
	 * triggers when updating components.
	 */
	void addListeners()
	{
		redSlider.addChangeListener(redChangeListener);
		greenSlider.addChangeListener(greenChangeListener);
		blueSlider.addChangeListener(blueChangeListener);
		alphaSlider.addChangeListener(alphaChangeListener);

		redTextbox.addActionListener(redTextboxActionListener);
		greenTextbox.addActionListener(greenTextboxActionListener);
		blueTextbox.addActionListener(blueTextboxActionListener);
		alphaTextbox.addActionListener(alphaTextboxActionListener);		
	}
	
	/**
	 * Removes change listeners to the components. Used in conjuntion with 
	 * {@link #addListeners()} to get round spurious event 
	 * triggers when updating components.
	 */
	void removeListeners()
	{
		redSlider.removeChangeListener(redChangeListener);
		greenSlider.removeChangeListener(greenChangeListener);
		blueSlider.removeChangeListener(blueChangeListener);
		alphaSlider.removeChangeListener(alphaChangeListener);

		redTextbox.removeActionListener(redTextboxActionListener);
		greenTextbox.removeActionListener(greenTextboxActionListener);
		blueTextbox.removeActionListener(blueTextboxActionListener);
		alphaTextbox.removeActionListener(alphaTextboxActionListener);
	}
	
}
