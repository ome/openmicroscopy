/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourMenuUI
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a UI which represents the colours as a list in the menu, currently it
 * shows the colour as an icon and the colourname, all of which are added from 
 * the constructor. (this may change)
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
public class ColourSwatchUI 
	extends JPanel
{
	
	/** Reference to the Colour model. */
	private RGBControl		control;
	
	/** List of colours. */
	private JList			colourlist;	
	
	/** Scroll pane which contains the JList component. */
	private JScrollPane		scrollpane;	
		
	  /** The slider representing the alpha channel. */
	private ColourSlider	alphaSlider;
	
	/** Change listener for alpha slider.  */
	private ChangeListener	alphaChangeListener;
	
	/** The textfield representing the alpha red channel. */
	private JTextField		alphaTextbox;
	
	  /** The JLabel representing the colour alpha channel. */
	private JLabel			alphaLabel;
	
	/** Action listener for alpha TextBox.  */
	private ActionListener	alphaTextboxActionListener;

	/**
	 * Boolean variable, true if the current component is active, this is 
	 * controlled from the parent component. It tells the UI whether or not
	 * to ignore refresh events.
	 */
	private boolean			active;
	

	/**
	 * Creates the Alpha slider, and changes listener, attaching the change
	 * listener to the slider. 
	 */
	private void createAlphaSlider()
	{
		Color s1, s;
		Color e1, e;
	
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
	 * Creates the Alpha textbox, and action listener, attaching the action
	 * listener to the textbox. 
	 */
	private void createAlphaTextbox()
	{
		alphaLabel = new JLabel("Alpha");
		alphaTextbox = new JTextField(""+(int) (control.getAlpha()*255));
		alphaTextbox.setColumns(TabbedPaneUI.TEXTBOX_COLUMN);
		alphaTextboxActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent actionEvent) {
				JTextField src = (JTextField) actionEvent.getSource();
				try {
					int value = Integer.parseInt(src.getText());
					if (value >=0 && value <=255)
						control.setAlpha(value/255.0f);
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
		alphaTextbox.addActionListener(alphaTextboxActionListener);
	}
	
	/**
	 * Create the colours and add the renderer {@link ColourListRenderer} to 
	 * the JList object. 
	 */
	private void createColours()
	{
		colourlist = new JList(
				new Object[] {	
						new Object[] {Color.blue, "Blue"},
						new Object[] {Color.green, "Green"},
						new Object[] {Color.red, "Red"},
						new Object[] {Color.white, "White"},
						new Object[] {Color.black, "Black"},
						new Object[] {Color.gray, "Gray"},
						new Object[] {Color.orange, "Orange"},
						new Object[] {Color.yellow, "Yellow"},
						new Object[] {new Color(75, 0, 130), "Indigo"},
						new Object[] {new Color(238, 130, 238), "Violet"},
							
				});
		colourlist.setCellRenderer(new ColourListRenderer());
	}
	
	/**
	 * Create the UI which includes adding the colour list (JList) to the 
	 * scrollpane.
	 */
	private void createUI()
	{
		createColours();
		createAlphaSlider();
		createAlphaTextbox();
		colourlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		colourlist.setLayoutOrientation(JList.VERTICAL);
		colourlist.setVisibleRowCount(-1);
		colourlist.addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e)
				{
					Object[] obj = (Object[]) 
					colourlist.getModel().getElementAt(((JList) e.getSource()).
							getLeadSelectionIndex());
					control.setColour((Color) obj[0]);	
		}

		});
		
		scrollpane = new JScrollPane(colourlist);
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 80;
		gbc.weighty = 580;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 10);
		this.add(scrollpane, gbc);
		gbc.gridy = 1;
		gbc.weightx = 80;
		gbc.weighty = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 0, 5);
		
		this.add(alphaLabel, gbc);
		gbc.gridy = 2;
		this.add(alphaSlider, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 20;
		this.add(alphaTextbox, gbc);
	}
	
	/**
	 * Create the UI and attach the control c.
	 * 
	 * @param c  Reference to the control. Mustn't be <code>null</code>.
	 */
	ColourSwatchUI(RGBControl c)
	{
		if (c == null) throw new IllegalArgumentException("No control.");
		control = c;
		createUI();
		active = false;
	}

	/**
	 * Sets the current component Active, called from parent control letting 
	 * this component know it should listen to refresh events. 
	 * 
	 * @param act 	Pass <code>true</code> to set the component to active,
	 * 				<code>false</code> otherwise..
	 */
	void setActive(boolean act) { active = act; }
	
	
	/** 
	 * Refresh method will be called by tabbedpanelUI when the model has 
	 * changed.
	 */
	void refresh() 
	{
		if (!(active)) return;
		removeListeners();
		updateSliders();
		updateTextboxes();
		addListeners();
		repaint();
	}

	/**
	 * Adds change listeners to the components. Used in conjuntion with 
	 * {@link #removeListeners()} to get round spurious event 
	 * triggers when updating components.
	 */
	void addListeners()
	{
		alphaSlider.addChangeListener(alphaChangeListener);
		alphaTextbox.addActionListener(alphaTextboxActionListener);		
	}
	
	/**
	 * Removes change listeners to the components. Used in conjuntion with 
	 * {@link #addListeners()} to get round spurious event 
	 * triggers when updating components.
	 */
	void removeListeners()
	{
		alphaSlider.removeChangeListener(alphaChangeListener);
		alphaTextbox.removeActionListener(alphaTextboxActionListener);
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
	void updateSliders() { updateAlphaSlider(); }

	/** Updates the textboxes of the UI.  */
	void updateTextboxes()
	{		
		alphaTextbox.setText(""+(int) (control.getAlpha()*255));
	}
	
}


