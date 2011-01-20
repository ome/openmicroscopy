/*
 * org.openmicroscopy.shoola.util.ui.search.SearchHelp 
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog presenting how to use the Search widget.
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
public class SearchHelp 
	extends JDialog
{

	/** Button to close the window. */
	private JButton closeButton;
	
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				close();
		
			}
		
		});
		getRootPane().setDefaultButton(closeButton);
	}
	
	/** 
	 * Builds and lays out the main component.
	 * 
	 * @return See above.
	 */
	private JPanel buildMain()
	{
		JPanel content = new JPanel();
		content.setBorder(new TitledBorder(""));
		content.add(new JLabel(formatText()));
		return content;
	}
	
	/**
	 * Builds and lays out the various controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildControl()
	{
		JPanel content = new JPanel();
		content.add(closeButton);
		return UIUtilities.buildComponentPanelRight(content);
	}
	
	/** 
	 * Formats and returns the help text.
	 * 
	 * @return See above. 
	 */
	private String formatText()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body bgcolor=#F0F0F0>");
		buf.append("<h3 bgcolor=#FFFFF0>Wildcard Searches</h3>");
		buf.append("<p>To perform a single character wildcard search use the " +
				"\"?\" symbol.</p>");
		buf.append("<p>To perform a multiple character wildcard search " +
				"use the \"*\" symbol.</p>");
		buf.append("<h3 bgcolor=#FFFFF0>Boosting a Term</h3>");
		buf.append("<p>Boosting allows you to control the relevance " +
				"of a document by boosting its term. </p> " +
				"<p>To boost a term use the caret, \"^\", symbol with a boost" +
				" factor (a number) <br> at the end of the term you are " +
				"searching." +
				"</p> <p>The higher the boost factor, the more relevant the " +
				"term will be.</p>");
		buf.append("</body></html>");
		return buf.toString();
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		IconManager icons = IconManager.getInstance();
		TitlePanel title = new TitlePanel("Search Tips", "Tips about search",
							icons.getIcon(IconManager.HELP_48));
		c.add(title, BorderLayout.NORTH);
		c.add(buildMain(), BorderLayout.CENTER);
		c.add(buildControl(), BorderLayout.SOUTH);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param owner The owner of the frame.
	 */
	public SearchHelp(JFrame owner)
	{
		super(owner);
		setModal(true);
		setResizable(false);
		initComponents();
		buildGUI();
		setSize(520, 350);
	}
	
}
