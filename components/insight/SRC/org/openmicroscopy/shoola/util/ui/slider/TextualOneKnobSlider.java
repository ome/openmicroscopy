/*
 * org.openmicroscopy.shoola.util.ui.slider.TextualOneKnobSlider 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.slider;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.NumericalTextField;

/** 
 * Component hosting a slider and the text field displaying the 
 * values. The synchronization between the components is handled by this class.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class TextualOneKnobSlider 
	extends JPanel
	implements ActionListener, ChangeListener, DocumentListener, FocusListener 
{

	/** Indicated to lay out the field on the left-hand side of the slider. */
	public static final	int		LEFT_TEXT_BOX = 0;
	
	/** Indicated to lay out the field on the left-hand side of the slider. */
	public static final	int		RIGHT_TEXT_BOX = 1;
	
	/** The slider. */
	private OneKnobSlider 		slider;
	
	/** The field hosting the start value. */
	private NumericalTextField 	field;
	
	/** The label displayed in front of the {@link #field}. */
	private JLabel				fieldLabel;
	
	/** The start value. */
	private int					start;
	
	/** One of the constants defined by this class. */
	private int					location;
	
	/** 
	 * Map used to convert the slider values into the corresponding
	 * textual value.
	 */
	private Map<Integer, Integer> converter;
	
	/**
	 * Initialises the components.
	 * 
	 * @param min   The minimum value.
	 * @param max   The maximum value.
	 * @param start The start value.
	 */
	private void initComponents(int min, int max, int start)
	{
		fieldLabel = new JLabel();
		slider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, min, max, start);
		slider.setShowArrows(false);
		int length = (""+max).length(); 
		field = new NumericalTextField(min, max);
		field.setColumns(length);
		field.setText(""+start);
		//No need to check values b/c already done by the slider.
		this.start = start;
	}
	
	/** Attaches listeners to the components. */
	private void attachListeners()
	{
		slider.addChangeListener(this);
		installFieldListener();
	}
	
	/** Installs the various listeners for the passed field. */
	private void installFieldListener()
	{
        field.addActionListener(this);
        field.addFocusListener(this);
        Document doc = field.getDocument();
        doc.addDocumentListener(this);
	}

	/** Removes the listeners to the passed component. */
	private void uninstallFieldListener()
	{
		field.removeActionListener(this);
		field.removeFocusListener(this);
		field.getDocument().removeDocumentListener(this);
	}
	
	/** Sets the start value. */
	private void setStartValue()
	{
		boolean valid = false;
		int val = 0;
		try {
            val = Integer.parseInt(field.getText());
            if (slider.getMinimum() <= val && val <= slider.getMaximum()) 
            	valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            field.selectAll();
            return;
        }
        start = val;
        slider.setValue(start);
	}
	
	/** Handles the lost of focus on text fields. */
	private void handleFocusLost()
	{
		String s = ""+start;
		String startVal = field.getText();
		if (startVal == null || !startVal.equals(s))
			field.setText(s); 
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		switch (location) {
			case LEFT_TEXT_BOX:
				
				break;
			case RIGHT_TEXT_BOX:
			default:
				break;
		}
	}
	

	/**
	 * Creates a new instance. The text box is located on the right-hand side
	 * of the slider.
	 * 
	 * @param min   The minimum value.
	 * @param max   The maximum value.
	 * @param start The start value.
	 */
	public TextualOneKnobSlider(int min, int max, int start)
	{
		this(min, max, start, RIGHT_TEXT_BOX);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param min   	The minimum value.
	 * @param max   	The maximum value.
	 * @param start 	The start value.
	 * @param location   The end value.
	 */
	public TextualOneKnobSlider(int min, int max, int start, int location)
	{
		initComponents(min, max, start);
		attachListeners();
		this.location = location;
		buildGUI();
	}
	
	/**
	 * Sets the text of the {@link #fieldLabel}.
	 * 
	 * @param value The value to set.
	 */
	public void setTextValue(String value)
	{
		if (value != null) fieldLabel.setText(value);
	}
	
	/**
	 * Sets the map used to convert slider value into the corresponding 
	 * textual value.
	 * 
	 * @param converter The value to set.
	 */
	public void setConverter(Map<Integer, Integer> converter)
	{
		this.converter = converter;
	}
	
	/**
     * Sets whether or not the slider and the text field are enabled.
     *
     * @param enabled 	Pass <code>true</code> if the components should be 
     * 					enabled, <code>false</code> otherwise.
     */
	public void setComponentsEnabled(boolean enabled)
	{
		slider.setEnabled(enabled);
		field.setEnabled(enabled);
	}
	
	/**
     * Sets whether or not the text field is enabled.
     *
     * @param enabled 	Pass <code>true</code> if the text field should be 
     * 					enabled, <code>false</code> otherwise.
     */
	public void setFieldEnabled(boolean enabled)
	{
		field.setEnabled(enabled);
	}
	
    /**
     * Depending on the source of the event. Sets the gamma value or
     * the bit resolution.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
    	int value = slider.getValue();
    	if (converter != null) field.setText(""+converter.get(value));
    	else field.setText(""+slider.getValue());
    }
	
	/**
	 * Sets the start or end value.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		setStartValue();
	}
	
	/** 
     * Handles the lost of focus on the various text fields.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * value.
     * @see FocusListener#focusLost(FocusEvent)
     */
    public void focusLost(FocusEvent e) { handleFocusLost(); }
    
	/**
	 * Updates the field whose text is modified.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { setStartValue(); }

	/**
	 * Updates the field whose text is modified.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { setStartValue(); }

	/**
	 * Required by the {@link DocumentListener} I/F but not actually needed in
     * our case, no-operation implementation.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
	/** 
     * Required by {@link FocusListener} I/F but not actually needed in
     * our case, no-operation implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}
	
}
