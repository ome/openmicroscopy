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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

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

	/** Reference to the Control. */
	private EditorControl controller;
	
	private JToolBar createBar()
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setRollover(true);
		bar.setBorder(null);
		JButton b = new JButton(controller.getAction(EditorControl.CREATE));
		bar.add(b);
		return bar;
	}
	
	/** Builds and lays out the UI. */
    private void buildGUI()
    {
    	JPanel bars = new JPanel();
    	bars.setBorder(null);
        bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
        bars.add(createBar());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(bars);
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
