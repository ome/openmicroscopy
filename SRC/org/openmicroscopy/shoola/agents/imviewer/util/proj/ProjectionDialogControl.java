/*
 * org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionDialogControl 
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
package org.openmicroscopy.shoola.agents.imviewer.util.proj;




//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;

/** 
 * The projection controller.
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
class ProjectionDialogControl 
	implements ActionListener, FocusListener, PropertyChangeListener
{
	
	/** Action id to project the selection and view the result. */
	static final int PREVIEW = 1;
	
	/** Action id to project the whole image. */
	static final int PROJECT = 2;
	
	/** Action id to set the first optical section. */
	static final int START_Z = 3;
	
	/** Action id to set the last optical section. */
	static final int END_Z = 4;
	
	/** Reference to the model. */
	private ProjectionDialog model;
	
	private Map<JTextField, FieldDocumentListener> listeners;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	ProjectionDialogControl(ProjectionDialog model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		listeners = new HashMap<JTextField, FieldDocumentListener>();
	}
	
	/**
	 * Attaches the listeners to the passed component.
	 * 
	 * @param field The component to handle.
	 * @param id    The action command id.
	 */
	void attachFieldListeners(JTextField field, int id)
	{
		field.setActionCommand(""+id);  
        field.addActionListener(this);
        field.addFocusListener(this);
        FieldDocumentListener l = new FieldDocumentListener(id, model);
        listeners.put(field, l);
        field.getDocument().addDocumentListener(l);
	}
	
	/**
	 * Removes the listeners to the passed component.
	 * 
	 * @param field The component to handle.
	 */
	void removeFieldListeners(JTextField field)
	{
		field.removeActionListener(this);
		field.removeFocusListener(this);
		FieldDocumentListener l = listeners.get(field);
		//field.getDocument().removeDocumentListener(l);
		//listeners.remove(l);
	}
	
	/** 
	 * Sets the datasets containing the image to project.
	 * 
	 * @param datasets The datasets containing the image.
	 */
	public void setContainers(Collection datasets)
	{
		ProjectionSavingDialog d = new ProjectionSavingDialog(model, datasets);
		UIUtilities.centerAndShow(d);
	}
	
	/**
	 * Previews, projects the image or sets the interval to project/preview.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case PREVIEW:
				model.preview();
				break;
			case PROJECT:
				model.loadDatasets();
				break;
			case START_Z:
				model.setStartZ();
				break;
			case END_Z:
				model.setEndZ();
		}
	}

	/**
	 * Sets the value of the optical sections.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)) {
			Integer value = (Integer) evt.getNewValue();
			model.updateStartField(value);
		} else if (TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
			Integer value = (Integer) evt.getNewValue();
			model.updateEndField(value);
		}
	}
	
	/** 
     * Handles the lost of focus on the various text fields.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * value.
     * @see FocusListener#focusLost(FocusEvent)
     */
    public void focusLost(FocusEvent e) { model.handleFocusLost(); }
    
	/** 
     * Required by {@link FocusListener} I/F but not actually needed in
     * our case, no op implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}
    
}
