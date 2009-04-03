/*
 * org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider 
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import layout.TableLayout;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Component hosting a two knobs slider and the text fields displaying the 
 * values. The synchronisation between the components is handled by this class.
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
public class TextualTwoKnobsSlider 
	extends JPanel
	implements ActionListener, DocumentListener, FocusListener, 
	PropertyChangeListener
{

	/** Indicates to layout all the components. */
	private static final int	LAYOUT_ALL = 0;
	
	/** Indicates to layout all the fields only. */
	private static final int	LAYOUT_FIELDS = 1;
	
	/** The id of the action linked to the {@link #startField}. */
	private static final int 	START = 0;
	
	/** The id of the action linked to the {@link #startField}. */
	private static final int 	END = 1;
	
	/** The name of the property used to identify the text field. */
	private static final String NAME_DOC = "name";
	
	/** The slider. */
	private TwoKnobsSlider 		slider;
	
	/** The field hosting the start value. */
	private NumericalTextField 	startField;
	
	/** The field hosting the end value. */
	private NumericalTextField 	endField;
	
	/** The label displayed in front of the {@link #startField}. */
	private JLabel				startLabel;
	
	/** The label displayed in front of the {@link #endField}. */
	private JLabel				endLabel;
	
	/** The label displayed in front of the {@link #slider}. */
	private JLabel				sliderLabel;
	
	/** The start value. */
	private int					start;
	
	/** The end value. */
	private int					end;
	
	/** Attaches listeners to the components. */
	private void attachListeners()
	{
		slider.addPropertyChangeListener(this);
		installFieldListener(startField, START);
		installFieldListener(endField, END);
	}
	
	/**
	 * Installs the various listeners for the passed field.
	 * 
	 * @param field	The text field to handle.
	 * @param id	The id of the action command
	 */
	private void installFieldListener(JTextField field, int id)
	{
		field.setActionCommand(""+id);  
        field.addActionListener(this);
        field.addFocusListener(this);
        Document doc = field.getDocument();
        doc.addDocumentListener(this);
        doc.putProperty(NAME_DOC, ""+id);
	}
	
	/**
	 * Removes the listeners to the passed component.
	 * 
	 * @param field The component to handle.
	 */
	private void uninstallFieldListener(JTextField field)
	{
		field.removeActionListener(this);
		field.removeFocusListener(this);
		field.getDocument().removeDocumentListener(this);
	}
	
	/**
	 * Initialises the components.
	 * 
	 * @param min   The minimum value.
	 * @param max   The maximum value.
	 * @param start The start value.
	 * @param end   The end value.
	 */
	private void initComponents(int min, int max, int start, int end)
	{
		sliderLabel = new JLabel();
		startLabel = new JLabel("Start");
		endLabel = new JLabel("End");
		slider = new TwoKnobsSlider(min, max, start, end);
		setSliderPaintingDefault(false);
		int length = (""+max).length(); 
		startField = new NumericalTextField(start, end);
		startField.setColumns(length);
		endField = new NumericalTextField(start, end);
		endField.setColumns(length);
		startField.setText(""+start);
		endField.setText(""+end);
		//No need to check values b/c already done by the slider.
		this.start = start;
		this.end = end;
	}
	
	/** Sets the start value. */
	private void setStartValue()
	{
		boolean valid = false;
		int val = 0;
		try {
            val = Integer.parseInt(startField.getText());
            if (slider.getPartialMinimum() <= val && val < end) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            startField.selectAll();
            return;
        }
        start = val;
        endField.setMinimum(start);
        slider.setStartValue(start);
	}
	
	/** Sets the end value. */
	private void setEndValue()
	{
		boolean valid = false;
		int val = 0;
		try {
            val = Integer.parseInt(endField.getText());
            if (start < val && val <= slider.getPartialMaximum()) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            endField.selectAll();
            return;
            
        }
        end = val;
        startField.setMaximum(end);
        slider.setEndValue(end);
	}
	
	/**
	 * Synchronises the slider and the {@link #startField} when the 
	 * left knob is moved.
	 * 
	 * @param value The value to set.
	 */
	private void synchStartValue(int value)
	{
		start = value;
		uninstallFieldListener(startField);
		startField.setText(""+start);
		endField.setMinimum(start);
		installFieldListener(startField, START);
	}
	
	/**
	 * Synchronises the slider and the {@link #endField} when the 
	 * right knob is moved.
	 * 
	 * @param value The value to set.
	 */
	private void synchEndValue(int value)
	{
		end = value;
		uninstallFieldListener(endField);
		endField.setText(""+end);
		startField.setMaximum(end);
		installFieldListener(endField, END);
	}
	
	/**
	 * Updates the field linked to the passed document.
	 * 
	 * @param doc The document to handle.
	 */
	private void updateTextValue(Document doc)
	{
		String value = (String) doc.getProperty(NAME_DOC);
		int index = Integer.parseInt(value);
		switch (index) {
			case START:
				setStartValue();
				break;
			case END:
				setEndValue();
				break;
		}
	}
	
	/** Handles the lost of focus on text fields. */
	private void handleFocusLost()
	{
		String s = ""+start;
		String e = ""+end;
		String startVal = startField.getText();
		String endVal = endField.getText();
		if (startVal == null || !startVal.equals(s))
			startField.setText(s);        
		if (endVal == null || !endVal.equals(e)) 
			endField.setText(e);
	}
	
	/**
	 * Builds and lays out the UI component hosting the text fields.
	 * 
	 * @return See above.
	 */
	private JPanel buildFieldsPane()
	{
		JPanel p = new JPanel();
		int charWidth = getFontMetrics(getFont()).charWidth('m');
		Insets insets = endField.getInsets();
		int length = endField.getColumns();
		int x = insets.left+length*charWidth+insets.left;
		Dimension d = startField.getPreferredSize();
		startField.setPreferredSize(new Dimension(x, d.height));
		d = endField.getPreferredSize();
		endField.setPreferredSize(new Dimension(x, d.height));
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(new GridBagLayout());
		c.weightx = 0;        
		c.anchor = GridBagConstraints.WEST;
		p.add(startLabel, c);
		c.gridx = 1;
		c.ipadx = x;
		c.weightx = 0.5;
		p.add(UIUtilities.buildComponentPanel(startField), c);
		c.gridx = 2;
		c.ipadx = 0;
		c.weightx = 0;
		p.add(endLabel, c);
		c.gridx = 3;
		c.ipadx = x;
		c.weightx = 0.5;
		p.add(UIUtilities.buildComponentPanel(endField), c);
		return p;
	}
	
	/** Creates a default instance. */
	public TextualTwoKnobsSlider()
	{
		this(TwoKnobsSlider.DEFAULT_MIN, TwoKnobsSlider.DEFAULT_MAX);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param min   The minimum value.
	 * @param max   The maximum value.
	 */
	public TextualTwoKnobsSlider(int min, int max)
	{
		this(min, max, min, max);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param min   The minimum value.
	 * @param max   The maximum value.
	 * @param start The start value.
	 * @param end   The end value.
	 */
	public TextualTwoKnobsSlider(int min, int max, int start, int end)
	{
		initComponents(min, max, start, end);
		attachListeners();
	}
	
	/**
	 * Sets the text of the {@link #startLabel}.
	 * 
	 * @param text The value to set.
	 */
	public void setStartLabelText(String text)
	{
		startLabel.setText(text);
	}
	
	/**
	 * Sets the text of the {@link #endLabel}.
	 * 
	 * @param text The value to set.
	 */
	public void setEndLabelText(String text)
	{
		endLabel.setText(text);
	}
	
	/**
	 * Sets the text of the {@link #sliderLabel}.
	 * 
	 * @param text The value to set.
	 */
	public void setSliderLabelText(String text)
	{
		sliderLabel.setText(text);
	}
	
	/**
	 * Returns the start value. 
	 * 
	 * @return See above.
	 */
	public int getStartValue() { return start; }
	
	/**
	 * Returns the end value. 
	 * 
	 * @return See above.
	 */
	public int getEndValue() { return end; }
	
	/** Lays out the components. */
	public void layoutComponents() { layoutComponents(LAYOUT_ALL); }
	
	/**
	 * Lays out the components identified by the layout index.
	 * 
	 * @param index The layout index.
	 */
	public void layoutComponents(int index)
	{
		switch (index) {
			case LAYOUT_FIELDS:
				add(buildFieldsPane());
				break;
			case LAYOUT_ALL:
			default:
				double size[][] =
		        {{TableLayout.PREFERRED, TableLayout.PREFERRED},  // Columns
		         {TableLayout.PREFERRED, TableLayout.PREFERRED}}; // Rows
				JPanel content = new JPanel();
				content.setLayout(new TableLayout(size));
				content.add(sliderLabel, "0, 0");
				content.add(UIUtilities.buildComponentPanel(slider), "1, 0");
				content.add(buildFieldsPane(), "1, 1");
				add(content);
		}
	}
	
	/**
	 * Sets the painting default of the slider.
	 * 
	 * @param paint Pass <code>true</code> to paint labels and ticks,
	 * 				<code>false</code> otherwise.
	 */
	public void setSliderPaintingDefault(boolean paint)
	{
		slider.setPaintLabels(paint);
		slider.setPaintEndLabels(paint);
		slider.setPaintTicks(paint);
	}

	/**
	 * Updates the text field related to the knob moved.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)) {
			Integer value = (Integer) evt.getNewValue();
			synchStartValue(value);
		} else if (TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
			Integer value = (Integer) evt.getNewValue();
			synchEndValue(value);
		}
	}
	
	/**
	 * Sets the start or end value.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case START:
				setStartValue();
				break;
			case END:
				setEndValue();
		}
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
	public void insertUpdate(DocumentEvent e)
	{  
		updateTextValue(e.getDocument());
	}

	/**
	 * Updates the field whose text is modified.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		updateTextValue(e.getDocument());
	}

	/**
	 * Required by the {@link DocumentListener} I/F but not actually needed in
     * our case, no op implementation.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
	/** 
     * Required by {@link FocusListener} I/F but not actually needed in
     * our case, no op implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}
	
}
