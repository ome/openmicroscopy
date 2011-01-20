/*
 * org.openmicroscopy.shoola.agents.metadata.util.BasicAnalyseDialog
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OptionsDialog;

/**
 * Basic modal dialog used to collect parameters for analysis routines.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */

public class BasicAnalyseDialog 
	extends OptionsDialog
	implements PropertyChangeListener
{

	/** Bound property indicating to collect the parameters. */
	public static final String ANALYSE_PARAMETERS_PROPERTY = "analyseParameters";
	
	/** The component hosting the id of the image to analyze. */
	private NumericalTextField field;
	
	/** Initializes the dialog. */
	private void initialize()
	{
		field = new NumericalTextField();
		setYesEnabled(false);
		field.addPropertyChangeListener(this);
		field.setColumns(5);
		setYesText("Analyse");
		hideNoButton();
		addCancelButton();
		setResizable(true);
		setSize(250, 200);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
		JPanel p = new JPanel();
		//p.setBackground(UIUtilities.WINDOW_BACKGROUND_COLOR);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(new JLabel("Image ID: "));
		p.add(field);
		addBodyComponent(p);
		setSize(450, 200);
	}
	
	/**
	 * Overridden to fire a property with the id of the image to analyze.
	 * @see OptionsDialog#onYesSelection()
	 */
	protected void onYesSelection()
	{
		Number text = field.getValueAsNumber();
		String oldValue = null;
		if (text == null) oldValue = "";
		firePropertyChange(ANALYSE_PARAMETERS_PROPERTY, oldValue, text);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The owner of this dialog
	 * @param icon	 The icon displayed in the header.
	 */
	public BasicAnalyseDialog(JFrame parent, Icon icon)
	{
		super(parent, "Analyse", "Enter information about the image to analyse", 
				icon);
		initialize();
		buildGUI();
	}

	/**
	 * Listens to property fired by the numerical text field.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (NumericalTextField.TEXT_UPDATED_PROPERTY.equals(name)) {
			Number n = field.getValueAsNumber();
			setYesEnabled(n != null);
		}
	}
	
}
