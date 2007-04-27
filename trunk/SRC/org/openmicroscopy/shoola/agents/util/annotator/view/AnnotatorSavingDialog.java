/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorSavingDialog 
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
package org.openmicroscopy.shoola.agents.util.annotator.view;



//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
class AnnotatorSavingDialog 
	extends JDialog
	implements ActionListener
{

	/** Indicates that the dialog is launched for the {@link Annotator}. */
	static final int 				ANNOTATOR = 0;
	
	/** 
	 * Indicates that the dialog is launched for the 
	 * {@link AnnotatorEditor}. 
	 */
	static final int 				ANNOTATOR_EDITOR = 1;
	
	/** Bound property indicating to annotate all nodes. */
	static final String				ANNOTATE_ALL_PROPERTY = "annotateAll";
	
	/** Bound property indicating to annotate the selected node. */
	static final String				ANNOTATE_ONE_PROPERTY = "annotateOne";
	
	/** The title of the dialog. */
	private static final String		TITLE = "Save annotation";
	
	/** The beginning of the default text. */
	private static final String 	TEXT_START = "Do you want to " +
										"apply this annotation only to ";
	
	/** 
	 * The end of the default text if the window is for 
	 * the {@link Annotator}.
	 */
	private static final String 	TEXT_ANNOTATOR_END = " or to all items " +
										"in the annotation list?";
	
	/** 
	 * The end of the default text if the window is for 
	 * the {@link AnnotatorEditor}.
	 */
	private static final String 	TEXT_ANNOTATOR_EDITOR_END = " or to all " +
										"selected items?";
	
	/** The default size of the window. */
	private static final Dimension	DEFAULT_SIZE = new Dimension(400, 200);
	 
	/** Action ID associated to the {@link #allButton}. */
	private static final int		ALL_ID = 0;
	
	/** Action ID associated to the {@link #oneButton}. */
	private static final int		ONE_ID = 1;
	
	/** Action ID associated to the {@link #cancelButton}. */
	private static final int		CANCEL_ID = 2;
	
	/** One of the constants defined by this class. */
	private int 	index;
	
	/** The text to display. */
	private String	text;
	
	/** Button used to save the annotations for all items. */
	private JButton	allButton;
	
	/** Button used to save the annotations for the selected item. */
	private JButton oneButton;
	
	/** Button to close and dispose of the window. */
	private JButton	cancelButton;
	
	/** Sets the default properties of the dialog. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Controls if the passed index is supported.
	 * 
	 * @param i The value to control.
	 */
	private void checkIndex(int i)
	{
		switch (i) {
			case ANNOTATOR:
			case ANNOTATOR_EDITOR:
				return;
	
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/**
	 * Initializes the components composing the display.
	 * 
	 * @param s The name of the selected item.
	 */
	private void initComponents(String s)
	{
		allButton = new JButton("All");
		allButton.setActionCommand(""+ALL_ID);
		allButton.addActionListener(this);
		oneButton = new JButton("One");
		oneButton.setActionCommand(""+ONE_ID);
		oneButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand(""+CANCEL_ID);
		cancelButton.addActionListener(this);
		getRootPane().setDefaultButton(allButton);
		text = TEXT_START+s; 
		if (index == ANNOTATOR) text += TEXT_ANNOTATOR_END;
		else text += TEXT_ANNOTATOR_EDITOR_END;
	}
	
	/**
	 * Builds the UI component displaying the annotations.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.ANNOTATION_48);
		JPanel commentPanel = new JPanel();
        int iconSpace = 0;
        int iconHeight = 0;
        if (icon != null) {
        	iconSpace = icon.getIconWidth()+20;
        	iconHeight = icon.getIconHeight()+10;
        }
        
        double tableSize[][] =  
        		{{iconSpace, (160 - iconSpace), TableLayout.FILL}, // columns
        		{iconHeight, TableLayout.FILL}};
        TableLayout layout = new TableLayout(tableSize);
        commentPanel.setLayout(layout);  
        commentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (icon != null)
        	commentPanel.add(new JLabel(icon), "0, 0");
        commentPanel.add(UIUtilities.buildTextPane(text), "1, 0, 2, 1");
		return commentPanel;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		
		JPanel mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		double tableSize[][] = {{TableLayout.FILL, 100, 5, 100, 5, 100, 5}, // columns
								{TableLayout.FILL, 40}}; // rows
        TableLayout layout = new TableLayout(tableSize);
        mainPanel.setLayout(layout);       
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(buildBody(), "0, 0, 6, 0");
        //Add the buttons.
        mainPanel.add(allButton, "1, 1, f, c");
        mainPanel.add(oneButton, "3, 1, f, c");
        mainPanel.add(cancelButton, "5, 1, f, c");
        //mainPanel.add(sendButton, "3, 1, f, c");
        getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Initializes the component.
	 * 
	 * @param index	One of the constant defined by this class.
	 * @param name	The name of the currently selected item.
	 */
	private void initialize(int index, String name)
	{
		checkIndex(index);
		setProperties();
		this.index = index;
		initComponents(name);
		buildGUI();
		setSize(DEFAULT_SIZE);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner	The owner of this dialog.
	 * @param index	One of the constant defined by this class.
	 * @param name	The name of the currently selected item.
	 */
	AnnotatorSavingDialog(JDialog owner, int index, String name)
	{
		super(owner);
		initialize(index, name);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param owner	The owner of this dialog.
	 * @param index	One of the constant defined by this class.
	 * @param name	The name of the currently selected item.
	 */
	AnnotatorSavingDialog(JFrame owner, int index, String name)
	{
		super(owner);
		initialize(index, name);
	}
	
	/**
	 * Handles events fired by buttons.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
	            case CANCEL_ID:
	            	close();
	            	break;
	            case ONE_ID:
	            	firePropertyChange(ANNOTATE_ONE_PROPERTY, Boolean.FALSE, 
	            						Boolean.TRUE);
	            	close();
	            	break;
	            case ALL_ID:
	            	firePropertyChange(ANNOTATE_ALL_PROPERTY, Boolean.FALSE, 
    						Boolean.TRUE);
	            	close();
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
	}
	
}
