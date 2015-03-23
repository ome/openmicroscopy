/*
 * org.openmicroscopy.shoola.agents.util.ui.DowngradeChooser 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.ui;


//Java imports
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.Target;
import org.openmicroscopy.shoola.env.data.util.TransformsParser;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/** 
 * Displays the downgrade sheets.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class DowngradeChooser 
	extends FileChooser
{

	/** Flag indicating to display the help page.*/
	public static final String HELP_DOWNGRADE_PROPERTY = "helpDowngrade";
	
	/** The possible downgrade schema.*/
    private List<Target> targets;
    
    /** The selection of downgrade sheets.*/
    private JComboBox box;
    
    /** The button to bring up the help page.*/
    private JButton helpButton;
	
    /** The parser used to read the downgrade sheets.*/
    private TransformsParser parser;
    
	/**
     * Creates a new instance.
     * 
     * @param owner 		The owner of this dialog.
     * @param dialogType	One of the constants defined by this class.
     * @param title 		Title of the dialog.
     * @param message 		Message of the dialog.
     * @param filters 		The list of filters.
     */
	public DowngradeChooser(JFrame owner, int dialogType, String title, 
    					String message, List<FileFilter> filters)
	{
		super(owner, dialogType, title, message, filters);
	}
	
	/** Loads the specification if available.
	 * 
	 * @throws Thrown when an error occurred while parsing the catalog.
	 */
	public void parseData(String file)
		throws Exception
	{
		helpButton = new JButton();
		UIUtilities.unifiedButtonLookAndFeel(helpButton);
		helpButton.setIcon(IconManager.getInstance().getIcon(IconManager.HELP));
		//Load the specification folder and parse the file.
		helpButton.addActionListener(new ActionListener() {
			
			/**
			 * Brings up the URL.
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				firePropertyChange(HELP_DOWNGRADE_PROPERTY,
						Boolean.valueOf(false), Boolean.valueOf(true));
			}
		});
		parser = new TransformsParser();
		parser.parse(file);
		targets = parser.getTargets();
		Collections.reverse(targets);
		//Build the UI
		Iterator<Target> i = targets.iterator();
		Object[] values = new Object[targets.size()+1];
		values[0] = parser.getCurrentSchema()+" (current)";
		int index = 1;
		while (i.hasNext()) {
			values[index] = i.next();
			index++;
		}
		box = new JComboBox(values);
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(new JLabel("Version:"));
		p.add(box);
		p.add(helpButton);
		addComponentToControls(p);
	}
	
	/**
	 * Returns the selected target.
	 * 
	 * @return See above.
	 */
	public Target getSelectedSchema()
	{
		if (box == null) return null;
		Object ho = box.getSelectedItem();
		if (ho instanceof Target) return (Target) ho;
		return null;
	}

}
