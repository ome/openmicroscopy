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
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.Target;
import org.openmicroscopy.shoola.env.data.util.TransformsParser;
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

	 /** The possible downgrade schema.*/
    private List<Target> targets;
    
    /** The selection of downgrade sheets.*/
    private JComboBox box;
    
	/** Loads the specification if available.*/
	private void parseData()
	{
		//Load the specification folder and parse the file.
		TransformsParser parser = new TransformsParser();
		try {
			parser.parse();
			targets = parser.getTargets();
			//Build the UI
			Iterator<Target> i = targets.iterator();
			Object[] values = new Object[targets.size()+1];
			values[0] = "";
			int index = 1;
			while (i.hasNext()) {
				values[index] = i.next();
				index++;
			}
			box = new JComboBox(values);
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("Downgrade to:"));
			p.add(box);
			addComponentToControls(p);
		} catch (Exception e) {
			//ignore
		}
	}
	
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
		parseData();
	}
	
	/**
	 * Returns the selected target.
	 * 
	 * @return See above.
	 */
	public Target getSelectedSchema()
	{
		Object ho = box.getSelectedItem();
		if (ho instanceof Target) return (Target) ho;
		return null;
	}

}
