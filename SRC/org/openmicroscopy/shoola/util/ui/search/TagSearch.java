/*
 * org.openmicroscopy.shoola.util.ui.TagSearch 
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
public class TagSearch
	extends JPanel
	implements ActionListener, DocumentListener
{

	/** Bound property indicating to search for a given tag. */
	public static final String	TAG_SEARCH_PROPERTY = "tagSearch";
	
	/** Removes the text from the text field. */
	private static final int 	CLEAR = 0;
	
	/** Area where to enter the tags to search. */
	private JTextField	searchArea;
	
	/** Button to look nice. Does nothing for now. */
	private JButton		searchButton;
	
	/** Button to clear the text. */
	private JButton		clearButton;
	
	/** The Layout manager used to lay out the search area. */
	private TableLayout	layoutManager;
	
	/** UI Component hosting the various items for the search. */
	private JPanel		searchPanel;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		searchButton = new JButton(icons.getIcon(IconManager.SEARCH));
		searchButton.setEnabled(false);
		UIUtilities.setTextAreaDefault(searchButton);
		searchButton.setBorder(null);
		clearButton = new JButton(icons.getIcon(IconManager.CLEAR_DISABLED));
		//clearButton.setEnabled(false);
		UIUtilities.setTextAreaDefault(clearButton);
		clearButton.setBorder(null);
		clearButton.addActionListener(this);
		clearButton.setActionCommand(""+CLEAR);
		
		searchArea = new JTextField(15);
        UIUtilities.setTextAreaDefault(searchArea);
        searchArea.setBorder(null);
        
        searchArea.getDocument().addDocumentListener(this);
        searchArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
            	Object source = e.getSource();
            	if (source != searchArea) return;
            	switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						handleKeyEnter();
						break;
					
				}
                
            }
        });
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		double[][] pl = {{TableLayout.PREFERRED, TableLayout.FILL, 0}, //columns
				{TableLayout.PREFERRED} }; //rows\
		layoutManager = new TableLayout(pl);
		
		searchPanel = new JPanel();
		UIUtilities.setTextAreaDefault(searchPanel);
		searchPanel.setLayout(layoutManager);
		searchPanel.setBorder(
					BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		searchPanel.add(searchButton, "0, 0, f, c");
		searchPanel.add(searchArea, "1, 0, f, c");
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(true);
		//add(UIUtilities.setTextFont("Tags "));
		add(searchPanel);
	}
	
	/** 
	 * Fires a property change to search for some tags
	 */
	private void handleKeyEnter()
	{
		String text = searchArea.getText();
		if (text != null) text = text.trim();
		List<String> l = new ArrayList<String>();
		l.add(text);
		firePropertyChange(TAG_SEARCH_PROPERTY, null, l);
	}
	
	/** Removes the text from the display. */
	private void clear()
	{
		searchArea.getDocument().removeDocumentListener(this);
		searchArea.setText("");
		searchArea.getDocument().addDocumentListener(this);
		layoutManager.setColumn(2, 0);
		searchPanel.remove(clearButton);
		searchPanel.validate();
		searchPanel.repaint();
	}
	
	/** Creates a new instance, */
	public TagSearch()
	{
		initComponents();
		buildGUI();
	}

	/** 
     * Shows or hides the {@link #clearButton} when some text is entered.
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
	public void insertUpdate(DocumentEvent e)
	{
		layoutManager.setColumn(2, TableLayout.PREFERRED);
		searchPanel.add(clearButton, "2, 0, f, c");
		searchPanel.validate();
		searchPanel.repaint();
	}

	/** 
     * Shows or hides the {@link #clearButton} when some text is entered.
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
	public void removeUpdate(DocumentEvent e)
	{
		if (e.getOffset() == 0) clear();
	}
	
	/**
	 * Clears the text from the display.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLEAR:
				clear();
				break;
		}
	}
	
	/** 
     * Required by I/F but no-op implementation in our case. 
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
	public void changedUpdate(DocumentEvent e) {}

}
