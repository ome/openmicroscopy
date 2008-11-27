/*
 * org.openmicroscopy.shoola.agents.util.tagging.util.TaggedMenuPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.tagging.util;





//Java imports
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;

/** 
 * Component acting as a menu item.
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
public class TaggedMenuPane 	
	extends JPanel
	implements ActionListener
{

	/** Bounds property indicating to remove the tag from the image. */
	public static final String	REMOVE_PROPERTY = "remove";
	
	/** Bounds property indicating to browse the tag. */
	public static final String	BROWSE_PROPERTY = "browse";
	
	/** Action Id indicating to browse the category. */
	private static final int 	BROWSE = 0;
	
	/** Action Id indicating to remove the tag from the image. */
	private static final int 	REMOVE = 1;
	
	/** The category hosted by this node. */
	private CategoryData data;
	
	/** Initializes the components and lays them out. */
	private void buildComponents()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		IconManager icons = IconManager.getInstance();
		JButton b = new JButton(icons.getIcon(IconManager.BROWSE));
		b.setToolTipText("Browse the images with the tag.");
		UIUtilities.unifiedButtonLookAndFeel(b);
		b.setActionCommand(""+BROWSE);
		b.addActionListener(this);
		add(b);
		b = new JButton(icons.getIcon(IconManager.CANCEL));
		b.setToolTipText("Remove the tag.");
		UIUtilities.unifiedButtonLookAndFeel(b);
		b.setActionCommand(""+REMOVE);
		b.addActionListener(this);
		add(b);
		JLabel l = new JLabel(data.getName());
		l.setToolTipText(data.getDescription());
		add(l);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data hosted by this node. Mustn't be <code>null</code>.
	 */
	TaggedMenuPane(CategoryData data)
	{
		if (data == null)
			throw new IllegalArgumentException("No tag specified.");
		this.data = data;
		buildComponents();
	}

	/**
	 * Fires a property change event when the user clicks on one of the 
	 * buttons.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case REMOVE:
				firePropertyChange(REMOVE_PROPERTY, null, data);
				break;
			case BROWSE:
				firePropertyChange(BROWSE_PROPERTY, null, data);
		}
	}
	
}
