/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.PreviewControlBar 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.metadata.rnd;


import java.awt.FlowLayout;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.agents.metadata.actions.ManageRndSettingsAction;

/** 
 * Displays the controls
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class PreviewControlBar 
	extends JPanel
{

	/** Space between buttons. */
	static final int SPACE = 3;
	
	/** Reference to the control. */
    private RendererControl control;
    
    /** Reference to the model. */
    private RendererModel model;
    
    /**
     * Formats the specified button.
     * 
     * @param b The button to handle.
     */
    private void formatButton(AbstractButton b)
    {
    	 b.setVerticalTextPosition(AbstractButton.BOTTOM);
    	 b.setHorizontalTextPosition(AbstractButton.CENTER);
    	 b.setIconTextGap(0);
         UIUtilities.unifiedButtonLookAndFeel(b);
         b.setBackground(UIUtilities.BACKGROUND_COLOR);
    }
    
    /**
     * Returns the tool bar.
     * 
     * @return See above.
     */
    private JToolBar buildToolBar()
    {
    	JToolBar bar = new JToolBar();
    	bar.setBackground(UIUtilities.BACKGROUND_COLOR);
        bar.setBorder(null);
        bar.setRollover(true);
        bar.setFloatable(false);
        
        JButton b;
        
        if (model.isGeneralIndex()) {
            b = new JButton(control.getAction(RendererControl.SAVE));
            b.setText(ManageRndSettingsAction.NAME_SAVE);
            formatButton(b);
            bar.add(b);
            
            bar.add(Box.createHorizontalStrut(SPACE));
            b = new JButton(control.getAction(RendererControl.APPLY_TO_ALL));
            formatButton(b);
            bar.add(b);
            bar.add(new JSeparator(JSeparator.VERTICAL));
        }
        
        
        b = new JButton(control.getAction(RendererControl.RND_UNDO));
        formatButton(b);
        bar.add(b);
        bar.add(Box.createHorizontalStrut(SPACE));
        
        b = new JButton(control.getAction(RendererControl.RND_REDO));
        formatButton(b);
        bar.add(b);

        if (model.isGeneralIndex()) {
            bar.add(new JSeparator(JSeparator.VERTICAL));
    
            b = new JButton(control.getAction(RendererControl.COPY));
            formatButton(b);
            bar.add(b);
            bar.add(Box.createHorizontalStrut(SPACE));
            
            b = new JButton(control.getAction(RendererControl.PASTE));
            formatButton(b);
            bar.add(b);
        }
        
        return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBackground(UIUtilities.BACKGROUND_COLOR);
        add(buildToolBar());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param control Reference to the control.
     * @param model   Reference to the model.
     */
    PreviewControlBar(RendererControl control, RendererModel model)
    {
    	this.control = control;
    	this.model = model;
    	buildGUI();
    }
    
}
