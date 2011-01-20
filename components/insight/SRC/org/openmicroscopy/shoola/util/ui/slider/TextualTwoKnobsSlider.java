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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Component hosting a two knobs slider and the text fields displaying the 
 * values. Synchronizes the various components.
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
	PropertyChangeListener, KeyListener
{

	/** Indicates to layout all the components. */
	public static final int		LAYOUT_ALL = 0;
	
	/** Indicates to layout all the fields only. */
	public static final int		LAYOUT_FIELDS = 1;
	
	/** Indicates to layout the slider only. */
	public static final int		LAYOUT_SLIDER = 2;
	
	/** Indicates to layout the slider and label only. */
	public static final int		LAYOUT_SLIDER_AND_LABEL = 3;
	
	/** The id of the action linked to the {@link #startField}. */
	public static final int 	START = 0;
	
	/** The id of the action linked to the {@link #startField}. */
	public static final int 	END = 1;
	
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
	
	/** Attaches the listeners to the components. */
	private void attachListeners()
	{
		attachSliderListeners();
		installFieldListeners(startField, START);
		installFieldListeners(endField, END);
	}
	
	/** Removes the listeners from the components. */
	private void removeListeners()
	{
		removeSliderListeners();
		uninstallFieldListeners(startField);
		uninstallFieldListeners(endField);
	}
	
	/** Attaches the listeners to the slider. */
	private void attachSliderListeners()
	{
		slider.addPropertyChangeListener(this);
	}
	
	/** Removes the listener attached to the slider. */
	private void removeSliderListeners()
	{
		slider.removePropertyChangeListener(this);
	}
	
	/**
	 * Installs the various listeners for the passed field.
	 * 
	 * @param field	The text field to handle.
	 * @param id	The id of the action command.
	 */
	private void installFieldListeners(JTextField field, int id)
	{
		field.setActionCommand(""+id);  
        field.addActionListener(this);
        field.addFocusListener(this);
        field.addKeyListener(this);
        Document doc = field.getDocument();
        doc.addDocumentListener(this);
        doc.putProperty(NAME_DOC, ""+id);
	}
	
	/**
	 * Removes the listeners to the passed component.
	 * 
	 * @param field The component to handle.
	 */
	private void uninstallFieldListeners(JTextField field)
	{
		//field.removeActionListener(this);
		field.removeFocusListener(this);
		//field.getDocument().removeDocumentListener(this);
	}
	
	/**
	 * Initializes the components.
	 * 
	 * @param absMin The absolute minimum value.
	 * @param absMax The absolute maximum value.
	 * @param min    The minimum value.
	 * @param max    The maximum value.
	 * @param start  The start value.
	 * @param end    The end value.
	 */
	private void initComponents(int absMin, int absMax, int min, int max, 
			int start, int end)
	{
		
		sliderLabel = new JLabel();
		startLabel = new JLabel("Start");
		endLabel = new JLabel("End");
		slider = new TwoKnobsSlider(absMin, absMax, min, max, start, end);
		setSliderPaintingDefault(false);
		int length = (""+max).length(); 
		startField = new NumericalTextField(absMin, absMax);
		startField.setColumns(length);
		endField = new NumericalTextField(absMin, absMax);
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
            //if (slider.getPartialMinimum() <= val && val < end) valid = true;
            if (startField.getMinimum() <= val && val < end) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            startField.selectAll();
            return;
        }
        start = val;
        //endField.setMinimum(start);
        if (start > slider.getPartialMinimum()) {
        	val = slider.getPartialMinimum();
        }
        removeSliderListeners();
        int old = slider.getStartValue();
        slider.setStartValue(val);
       
        firePropertyChange(TwoKnobsSlider.KNOB_RELEASED_PROPERTY, old, val);
        attachSliderListeners();
	}
	
	/** Sets the end value. */
	private void setEndValue()
	{
		boolean valid = false;
		int val = 0;
		try {
            val = Integer.parseInt(endField.getText());
            //if (start < val && val <= slider.getPartialMaximum()) valid = true;
            if (start < val && val <= endField.getMaximum()) valid = true;
        } catch(NumberFormatException nfe) {}
        if (!valid) {
            endField.selectAll();
            return;
        }
        end = val;
        if (end > slider.getPartialMaximum()) {
        	val = slider.getPartialMaximum();
        }
        removeSliderListeners();
        //startField.setMaximum(end);
        int old = slider.getEndValue();
        slider.setEndValue(val);
        firePropertyChange(TwoKnobsSlider.KNOB_RELEASED_PROPERTY, old, val);
        attachSliderListeners();
	}
	
	/**
	 * Synchronizes the slider and the {@link #startField} when the 
	 * left knob is moved.
	 * 
	 * @param value The value to set.
	 */
	private void synchStartValue(int value)
	{
		start = value;
		uninstallFieldListeners(startField);
		startField.setText(""+start);
		//endField.setMinimum(start);
		installFieldListeners(startField, START);
	}
	
	/**
	 * Synchronizes the slider and the {@link #endField} when the 
	 * right knob is moved.
	 * 
	 * @param value The value to set.
	 */
	private void synchEndValue(int value)
	{
		end = value;
		uninstallFieldListeners(endField);
		endField.setText(""+end);
		//startField.setMaximum(end);
		installFieldListeners(endField, END);
	}
	
	/**
	 * Updates the field linked to the passed document.
	 * 
	 * @param doc The document to handle.
	 */
	private void updateTextValue(Document doc)
	{
		if (slider == null || endField == null || startField == null) return;
		String value = (String) doc.getProperty(NAME_DOC);
		int index = Integer.parseInt(value);
		Integer v;
		Integer ref;
		switch (index) {
			case START:
				//setStartValue();
				v = (Integer) startField.getValueAsNumber();
				ref = (Integer) endField.getValueAsNumber();
				if (ref == null || v == null) return;
				//if (ref > v) startField.setMaximum(slider.getPartialMaximum());
				break;
			case END:
				v = (Integer) endField.getValueAsNumber();
				ref = (Integer) startField.getValueAsNumber();
				if (ref == null || v == null) return;
				//if (ref > v) endField.setMinimum(slider.getPartialMinimum());
				//setEndValue();
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
		this(min, max, min, max, start, end);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param absMin The absolute minimum value of the slider.
	 * @param absMax The absolute maximum value of the slider.
	 * @param min    The minimum value.
	 * @param max    The maximum value.
	 * @param start  The start value.
	 * @param end    The end value.
	 */
	public TextualTwoKnobsSlider(int absMin, int absMax, int min, int max, 
			int start, int end)
	{
		initComponents(absMin, absMax, min, max, start, end);
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
			case LAYOUT_SLIDER:
				add(slider);
				break;
			case LAYOUT_SLIDER_AND_LABEL:
				double size[][] =
		        {{TableLayout.PREFERRED, TableLayout.PREFERRED},  // Columns
		         {TableLayout.PREFERRED}}; // Rows
				JPanel content = new JPanel();
				content.setLayout(new TableLayout(size));
				content.add(sliderLabel, "0, 0");
				content.add(UIUtilities.buildComponentPanel(slider), "1, 0");
				add(content);
				break;
			case LAYOUT_ALL:
			default:
				double size1[][] =
		        {{TableLayout.PREFERRED, TableLayout.PREFERRED},  // Columns
		         {TableLayout.PREFERRED, TableLayout.PREFERRED}}; // Rows
				JPanel content1 = new JPanel();
				content1.setLayout(new TableLayout(size1));
				content1.add(sliderLabel, "0, 0");
				content1.add(UIUtilities.buildComponentPanel(slider), "1, 0");
				content1.add(buildFieldsPane(), "1, 1");
				add(content1);
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
	 * Returns the slider hosted by this component.
	 * 
	 * @return See above.
	 */
	public TwoKnobsSlider getSlider() { return slider; }
	
	/**
	 * Resets the default value of the slider.
	 * 
	 * @param absoluteMax 	The absolute maximum value of the slider.
	 * @param absoluteMin 	The absolute minimum value of the slider.
	 * @param max       	The maximum value.
	 * @param min       	The minimum value.
	 * @param start     	The value of the start knob.
	 * @param end       	The value of the end knob.
	 */
	public void setValues(int absoluteMax, int absoluteMin, 
			int max, int min, int start, int end)
	{
		setValues(absoluteMax, absoluteMin, absoluteMax, absoluteMin, max, min,
				start, end);
	}
	
	/**
	 * Resets the default value of the slider.
	 * 
	 * @param absoluteMaxSlider	The absolute maximum value of the slider.
	 * @param absoluteMinSlider The absolute minimum value of the slider.
	 * @param absoluteMaxText 	The absolute maximum value of the slider.
	 * @param absoluteMinText 	The absolute minimum value of the slider.
	 * @param max       		The maximum value.
	 * @param min       		The minimum value.
	 * @param start     		The value of the start knob.
	 * @param end       		The value of the end knob.
	 */
	public void setValues(int absoluteMaxSlider, int absoluteMinSlider, 
			int absoluteMaxText, int absoluteMinText, 
			int max, int min, int start, int end)
	{
		slider.setValues(absoluteMaxSlider, absoluteMinSlider, max, min, start, 
				end);
		removeListeners();
		int length = (""+max).length(); 
		startField.setColumns(length);
		endField.setColumns(length);
		endField.setMaximum(absoluteMaxText);
		endField.setMinimum(absoluteMinText);
		startField.setMaximum(absoluteMaxText);
		startField.setMinimum(absoluteMinText);
		endField.setText(""+end);
		startField.setText(""+start);
		//endField.setMinimum(absoluteMinText);
		//startField.setMaximum(absoluteMaxText);
		this.start = start;
		this.end = end;
		attachListeners();
	}
	
	
	/**
	 * Returns the text field corresponding to the passed index.
	 * 
	 * @param index The index identifying the component.
	 * @return See above.
	 */
	public JComponent getFieldComponent(int index)
	{
		switch (index) {
			case START: return startField;
			case END: return endField;
		}
		return null;
	}
	
	/**
	 * Sets the value of the slider and the text field.
	 * 
	 * @param s The start value to set.
	 * @param e The end value to set.
	 */
	public void setInterval(int s, int e)
	{
		removeListeners();
		endField.setText(""+e);
		startField.setText(""+s);
		slider.setStartValue(s);
		slider.setEndValue(e);
		//endField.setMinimum(s);
		//startField.setMaximum(e);
		start = s;
		end = e;
		attachListeners();
	}
	
	/**
	 * Overridden to set the text fields and the slider enabled.
	 * @see JPanel#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		slider.setEnabled(enabled);
		endField.setEnabled(enabled);
		startField.setEnabled(enabled);
	}
	
	/**
	 * Overridden to set the background color.
	 * @see JPanel#setBackground(Color c)
	 */
	public void setBackground(Color c)
	{
		super.setBackground(c);
		if (slider != null) slider.setBackground(c);
		if (endField != null) endField.setBackground(c);
		if (startField != null) startField.setBackground(c);
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
		firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
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
		
		//updateTextValue(e.getDocument());
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

    /**
     * Sets the start or end value depending on the selected fields
     * @see KeyListener#keyPressed(KeyEvent)
     */
	public void keyPressed(KeyEvent e)
	{
		if (KeyEvent.VK_ENTER == e.getKeyCode()) {
			Object source = e.getSource();
			if (source == startField) setStartValue();
			else if (source == endField) setEndValue();
		}
	}

	/** 
     * Required by {@link KeyListener} I/F but not actually needed in
     * our case, no-operation implementation.
     * @see KeyListener#keyReleased(KeyEvent)
     */ 
	public void keyReleased(KeyEvent e) {}

	/** 
     * Required by {@link KeyListener} I/F but not actually needed in
     * our case, no-operation implementation.
     * @see KeyListener#keyTyped(KeyEvent)
     */ 
	public void keyTyped(KeyEvent e) {}
	
}
