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
import javax.swing.JLabel;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
{

	/** The title associated to this component. */
	static final String 		TITLE = "Rate: ";
	
	/** Term indicating the number of ratings. */
	private static final String NAME = " rating";
	
	/** Component used to rate the object. */
	private RatingComponent rating;
	
	/** The number of ratings for the edited object. */
	private JLabel			ratingsLabel;
	
	/** Initializes the components composing the UI. */
	private void initComponents()
	{
		int n = model.getRatingCount();
		String s = "";
		if (n == 0)
			rating = new RatingComponent();
		else rating = new RatingComponent(model.getRatingAverage(), 
									RatingComponent.MEDIUM_SIZE);
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
		double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED} }; //rows
		
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
	
}
