/*
 * org.openmicroscopy.shoola.agents.metadata.editor.RateUI 
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
package org.openmicroscopy.shoola.agents.metadata.editor;

//Java imports
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.RatingAnnotationData;

/** 
 * UI component displaying the rate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class RateUI 
	extends AnnotationUI 
	implements PropertyChangeListener
{

	/** The title associated to this component. */
	static final String 		TITLE = "Rate: ";
	
	/** Term indicating the number of ratings. */
	private static final String NAME = " rating";
	
	/** Component used to rate the object. */
	private RatingComponent rating;
	
	/** The number of ratings for the edited object. */
	private JLabel			ratingsLabel;
	
	/** The last selected value. */
	private int				selectedValue;
	
	/** The initial value of the rating. */
	private int 			initialValue;
	
	/** Initializes the components composing the UI. */
	private void initComponents()
	{
		selectedValue = 0;
		initialValue = selectedValue;
		rating = new RatingComponent(selectedValue, 
									RatingComponent.MEDIUM_SIZE);
		rating.addPropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
		
		ratingsLabel = new JLabel();
		ratingsLabel.setFont(ratingsLabel.getFont().deriveFont(Font.ITALIC));
	}
	
	/** Lays out the UI. */
	private void layoutUI()
	{
		JLabel label = UIUtilities.setTextFont(getComponentTitle());
		label.setLabelFor(rating);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(label);
		add(rating);
		add(ratingsLabel);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	RateUI(EditorModel model)
	{
		super(model);
		initComponents();
		title = TITLE;
		layoutUI();
	}
	
	/**
	 * Overridden to lay out the rating.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		//set values.
		int n = model.getRatingCount();
		String s = "";
		selectedValue = 0;
		if (n > 0) selectedValue = model.getRatingAverage();
		initialValue = selectedValue;
		rating.setValue(selectedValue);
		s += n+NAME;
		if (n > 1) s += "s";
		ratingsLabel.setText("("+s+")");
		revalidate();
		repaint();
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }
	
	/**
	 * Returns the collection of rating to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove() { return null; }

	/**
	 * Returns the collection of urls to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{
		List<AnnotationData> l = new ArrayList<AnnotationData>();
		int value = model.getRatingAverage();
		if (selectedValue != value)
			l.add(new RatingAnnotationData(selectedValue));
		return l;
	}
	
	/**
	 * Returns <code>true</code> if annotation to save.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		return (selectedValue != initialValue);
	}

	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		selectedValue = 0;//model.getRatingAverage();
		initialValue = 0;
		rating.removePropertyChangeListener(RatingComponent.RATE_PROPERTY, 
											this);
		rating.setValue(selectedValue);
		rating.addPropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
		ratingsLabel.setText("("+0+NAME+")");
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() { clearData(); }
	
	/**
	 * Sets the currently selected rating value.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (RatingComponent.RATE_PROPERTY.equals(name)) {
			int newValue = (Integer) evt.getNewValue();
			if (newValue != selectedValue) {
				selectedValue = newValue;
				firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
			}
		}
	}
	
}
