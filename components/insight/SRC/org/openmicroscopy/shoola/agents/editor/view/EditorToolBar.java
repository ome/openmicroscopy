/*
 * org.openmicroscopy.shoola.agents.editor.view.EditorToolBar 
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
package org.openmicroscopy.shoola.agents.editor.view;


//Java imports
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;

//Third-party libraries

//Application-internal dependencies

/** 
 * The tool bar of {@link Editor}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class EditorToolBar 
	extends JPanel
{
	/** The Actions that are displayed in the File toolbar */
	static Integer[] 				FILE_ACTIONS = {
						EditorControl.NEW_BLANK_FILE,
						EditorControl.OPEN_LOCAL_FILE, 
						EditorControl.OPEN_WWW_FILE,
						EditorControl.SAVE_FILE
						};

	/** Reference to the Control. */
	private EditorControl controller;
	
	private JToolBar createBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setRollover(true);
		bar.setBorder(null);
		
		for (int i=0; i<FILE_ACTIONS.length; i++) {
			addAction(FILE_ACTIONS[i], bar);
		}
		return bar;
	}
	
	/**
	 * Convenience method for getting an {@link Action} from the 
	 * {@link #controller}, creating a {@link CustomButton} and adding
	 * it to the component;
	 * 
	 * @param actionId		Action ID, e.g. {@link EditorControl#CLOSE_EDITOR}
	 * @param comp			The component to add the button. 
	 */
	private void addAction(int actionId, JComponent comp)
	{
		JButton b = new CustomButton(controller.getAction(actionId));
		b.setText("");
		comp.add(b);
	}
	
	/** Builds and lays out the UI. */
    private void buildGUI()
    {
    	JPanel toolBars = new JPanel();
    	toolBars.setBorder(null);
        toolBars.setLayout(new BoxLayout(toolBars, BoxLayout.X_AXIS));
        toolBars.add(createBar());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(toolBars);
    	/*
        JPanel bars = new JPanel(), outerPanel = new JPanel();
        bars.setBorder(null);
        bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
        bars.add(createManagementBar());
        //bars.add(createEditBar());
        bars.add(createSearchBar());
        outerPanel.setBorder(null);
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
        outerPanel.add(bars);
        outerPanel.add(Box.createRigidArea(HBOX));
        outerPanel.add(Box.createRigidArea(HBOX));
        outerPanel.add(Box.createHorizontalGlue());  
       
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
       
        add(UIUtilities.buildComponentPanel(outerPanel));
         */
    }

    /**
     * Creates a new instance.
     * 
     * @param controller	Reference to the control. 
     *                      Mustn't be <code>null</code>.
     */
	EditorToolBar(EditorControl controller)
	{
		if (controller == null) 
			throw new NullPointerException("No controller.");
		this.controller = controller;
		buildGUI();
	}
	
}
