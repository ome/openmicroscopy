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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

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
	private Color		backgroundColor = Color.WHITE;
	
	/** 
	 * The preferred size of the widget that displays the  message.
	 * Only the part of text that fits into this display area will be displayed.
	 */
	protected static final Dimension	MSG_AREA_SIZE = new Dimension(150, 80);
	
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
		add(buildPanel(title, text, null, icon));
		add(new JSeparator());
	}

	/** 
	 * Create an instance.
	 * 
	 * @param title		title displayed in header.
	 * @param text		brief summary to explain.
	 * @param note		note to add.
	 * @param icon		icon displayed in the header.
	 */
	public TitlePanel(String title, String text, String note, Icon icon)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(buildPanel(title, text, note, icon));
		add(new JSeparator());
	}

    /** 
     * Create an instance.
     * 
     * @param title     title displayed in header.
     * @param text      brief summary to explain.
     * @param component JComponent to display in the header.
     */
    public TitlePanel(String title, String text, JComponent c)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(buildPanel(title, text, null, c));
        add(new JSeparator());
    }

    /** 
     * Create an instance.
     * 
     * @param title     title displayed in header.
     * @param text      brief summary to explain.
     * @param note      note to add.
     * @param icon      icon displayed in the header.
     */
    public TitlePanel(String title, String text, String note, JComponent c)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(buildPanel(title, text, note, c));
        add(new JSeparator());
    }
    
    /** Build header. */
    private JPanel buildPanel(String title, String text, String note, 
                            JComponent c)
    {
        JPanel p = new JPanel();
        p.setBackground(backgroundColor);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(buildTextPanel(title, text, note));
        p.add(Box.createHorizontalGlue());
        p.add(c);
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return p;
    }
	/** Build header. */
	private JPanel buildPanel(String title, String text, String note, Icon icon)
	{
		JPanel p = new JPanel();
		p.setBackground(backgroundColor);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(buildTextPanel(title, text, note));
		p.add(Box.createHorizontalGlue());
		p.add(new JLabel(icon));
		p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		return p;
	}

	/** Build Panel with text displayed in the header. */
	private JPanel buildTextPanel(String title, String text, String note)
	{
		JPanel p = new JPanel(), pAll = new JPanel();
		p.setBackground(backgroundColor);
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(gridbag);
		JLabel label = setTitleLabel(title);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(label, c);
		p.add(label);
		c.gridy = 1;
		if (title != null) {
			label = new JLabel(" "+text);
			gridbag.setConstraints(label, c);
			p.add(label);
		} else {
			MultilineLabel ml = setMessageLabel(" "+text);
			gridbag.setConstraints(ml, c);
			p.add(ml);
		}
		if (note != null) {
			c.gridy = 2;
			MultilineLabel nl = setNoteLabel(" "+note);
			gridbag.setConstraints(nl, c);
			p.add(nl);
		} 
		//necessary to align panel on the left.
		pAll.setLayout(new FlowLayout(FlowLayout.LEFT));
		pAll.setBackground(backgroundColor);
		pAll.add(p);
		return pAll;
	}
	
	/** Set the font of the string to bold. */
	private JLabel setTitleLabel(String s)
	{
		JLabel label = new JLabel(s);
		Font font = label.getFont();
		Font newFont = font.deriveFont(Font.BOLD);
		label.setFont(newFont);
		label.setBackground(Color.PINK);
		return label;
	}
	
	/** Set the font to italic. */
	private MultilineLabel setMessageLabel(String s)
	{
		MultilineLabel label = new MultilineLabel(s);
		label.setPreferredSize(MSG_AREA_SIZE);
		return label;
	}
	
	/** Set the font to italic. */
	private MultilineLabel setNoteLabel(String s)
	{
		MultilineLabel label = new MultilineLabel(s);
		Font font = label.getFont();
		Font newFont = font.deriveFont(Font.ITALIC);
		label.setFont(newFont);
		return label;
	}
	
}
