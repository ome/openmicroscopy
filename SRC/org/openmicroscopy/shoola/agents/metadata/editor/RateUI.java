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
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;


//Third-party libraries
import layout.TableLayout;

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
	
	/** Initializes the components composing the UI. */
	private void initComponents()
	{
		int n = model.getRatingCount();
		String s = "";
		selectedValue = 0;
		if (n > 0)
			selectedValue = model.getRatingAverage();
		rating = new RatingComponent(selectedValue, 
									RatingComponent.MEDIUM_SIZE);
		rating.addPropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
		s += n+NAME;
		if (n > 1) s += "s";
		ratingsLabel = new JLabel(s);
		ratingsLabel.setFont(ratingsLabel.getFont().deriveFont(Font.ITALIC));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	RateUI(EditorModel model)
	{
		super(model);
		title = TITLE;
	}
	
	/**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		initComponents();
		double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED} }; 
		
		setLayout(new TableLayout(tl));
		JLabel label = UIUtilities.setTextFont(getComponentTitle());
		label.setLabelFor(rating);
		add(label, "0, 0, l, c");
		add(rating, "2, 0, f, c");
		add(ratingsLabel, "2, 1");
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
		//if (selectedValue == -1) return false;
		int value = model.getRatingAverage();
		return (selectedValue != value);
	}

	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		selectedValue = model.getRatingAverage();
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		removeAll();
	}
	
	/**
	 * Sets the currently selected rating value.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (RatingComponent.RATE_PROPERTY.equals(name)) {
			selectedValue = (Integer) evt.getNewValue();
			firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
								Boolean.TRUE);
		}
	}
	
}
