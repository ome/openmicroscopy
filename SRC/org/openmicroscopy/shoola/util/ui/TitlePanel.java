/*
 * org.openmicroscopy.shoola.util.ui.TitlePanel
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TitlePanel
	extends JPanel
{

	/** Default color for the background. */
	private Color	BACKGROUND = Color.WHITE;
	
	/** 
	 * Create an instance.
	 * 
	 * @param title		title displayed in header.
	 * @param text		brief summary to explain.
	 * @param icon		icon displayed in the header.
	 */
	public TitlePanel(String title, String text, Icon icon)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(buildPanel(title, text, icon));
		add(new JSeparator());
	}

	/** Build header. */
	private JPanel buildPanel(String title, String text, Icon icon)
	{
		JPanel p = new JPanel();
		p.setBackground(BACKGROUND);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(buildTextPanel(title, text));
		p.add(Box.createHorizontalGlue());
		p.add(new JLabel(icon));
		Border b = BorderFactory.createEmptyBorder(5, 10, 5, 10);
		p.setBorder(b);
		return p;
	}
	
	/** Build Panel with text displayed header. */
	private JPanel buildTextPanel(String title, String text)
	{
		JPanel p = new JPanel();
		p.setBackground(BACKGROUND);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(setLabel(title));
		p.add(new JLabel(" "+text));
		return p;
	}
	
	/** Set the font of the string to bold. */
	private JLabel setLabel(String s)
	{
		JLabel label = new JLabel(s);
		Font font = label.getFont();
		Font newFont = font.deriveFont(Font.BOLD);
		label.setFont(newFont);
		return label;
	}
	
}
