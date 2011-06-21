/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.ErrorDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.fsimporter.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The dialog displaying the import exception.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ErrorDialog 
	extends JDialog
{

	/** The title of the component.*/
	private static final String TITLE = "Import Error";
	
	/** Brief description of the dialog purpose. */
	private static final String	TEXT = "Follow the exception returned " +
			"while attempting to import the image.";

	/** The error to display.*/
	private Throwable error;
	
	/** The button used to close the dialog.*/
	private JButton closeButton;
	
	/** Initializes the components composing the display.*/
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
	
	/** Closes the dialog.*/
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Builds the UI component displaying the exception.
	 * 
	 * @return See above.
	 */
	private JTextPane buildExceptionArea()
	{
		JTextPane pane = UIUtilities.buildExceptionArea();
		StyledDocument document = pane.getStyledDocument();
		Style style = pane.getLogicalStyle();
		//Get the full debug text
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        try {
        	document.insertString(document.getLength(), sw.toString(), style);
        } catch (BadLocationException e) {}
        
        return pane;
	}
	
	/** Builds and lays out the UI.*/
	private void buildUI()
	{
		Container c = getContentPane();
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, TEXT, 
				icons.getIcon(IconManager.DELETE_48));
		c.add(tp, BorderLayout.NORTH);
		JScrollPane pane = new JScrollPane(buildExceptionArea());
		c.add(pane, BorderLayout.CENTER);
		c.add(UIUtilities.buildComponentPanelRight(buildToolBar()),
				BorderLayout.SOUTH);
	}
	
    /**
     *  Builds and lays out the tool bar. 
     *  
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JPanel toolBar = new JPanel();
        toolBar.add(closeButton);
        return toolBar;
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the frame.
	 * @param error The error to display.
	 */
	public ErrorDialog(JFrame parent, Throwable error)
	{
		super(parent);
		setTitle("Import Error");
		setModal(true);
		this.error = error;
		initComponents();
		buildUI();
		setSize(500, 600);
	}
	
}
